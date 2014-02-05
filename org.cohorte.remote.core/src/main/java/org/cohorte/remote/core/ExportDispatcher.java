/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cohorte.remote.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.remote.ExportEndpoint;
import org.cohorte.remote.IExportEndpointListener;
import org.cohorte.remote.IExportsDispatcher;
import org.cohorte.remote.IRemoteServicesConstants;
import org.cohorte.remote.IServiceExporter;
import org.cohorte.remote.utilities.RSUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * Implementation of the export dispatcher
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-dispatcher-factory")
@Provides(specifications = IExportsDispatcher.class)
@Instantiate(name = "cohorte-remote-dispatcher")
public class ExportDispatcher implements IExportsDispatcher, ServiceListener {

    /** iPOJO dependency ID */
    private static final String ID_EXPORTERS = "exporters";

    /** iPOJO dependency ID */
    private static final String ID_LISTENERS = "listeners";

    /** Bundle context */
    private final BundleContext pBundleContext;

    /** UID -&gt; ExportEndpoint */
    private final Map<String, ExportEndpoint> pEndpoints = new LinkedHashMap<String, ExportEndpoint>();

    /** Service Exporters */
    @Requires(id = ID_EXPORTERS, optional = true)
    private List<IServiceExporter> pExporters;

    /** Framework UID */
    private String pFrameworkUid;

    /** Endpoints listeners **/
    @Requires(id = ID_LISTENERS, optional = true)
    private List<IExportEndpointListener> pListeners;

    /** The log service */
    @Requires
    private LogService pLogger;

    /** Service reference -&gt; UIDs */
    private final Map<ServiceReference<?>, Collection<String>> pServiceUids = new LinkedHashMap<ServiceReference<?>, Collection<String>>();

    /** UID -&gt; Exporter */
    private final Map<String, IServiceExporter> pUidExporter = new LinkedHashMap<String, IServiceExporter>();

    /** Validation flag */
    private boolean pValidated = false;

    /**
     * Component constructed
     * 
     * @param aContext
     *            the bundle context
     */
    public ExportDispatcher(final BundleContext aContext) {

        pBundleContext = aContext;
    }

    /**
     * A new service exporter has been bound
     * 
     * @param aExporter
     *            The new service exporter
     */
    @Bind(id = ID_EXPORTERS)
    private void bindExporter(final IServiceExporter aExporter) {

        if (!pValidated) {
            // Not yet validated
            return;
        }

        // Tell the exporter to export already known services
        for (final ServiceReference<?> svcRef : pServiceUids.keySet()) {
            // Compute the endpoint name
            final String name = computeEndpointName(
                    (String) svcRef
                            .getProperty(IRemoteServicesConstants.PROP_ENDPOINT_NAME),
                    (Long) svcRef.getProperty(Constants.SERVICE_ID));

            try {
                // Create the endpoint
                final ExportEndpoint endpoint = aExporter.exportService(svcRef,
                        name, pFrameworkUid);
                if (endpoint == null) {
                    // Export refused
                    continue;
                }

                // Store the endpoint
                final String uid = endpoint.getUid();
                pEndpoints.put(uid, endpoint);
                pUidExporter.put(uid, aExporter);
                pServiceUids.get(svcRef).add(uid);

                // Call listeners
                for (final IExportEndpointListener listener : pListeners) {
                    listener.endpointsAdded(new ExportEndpoint[] { endpoint });
                }

            } catch (final Exception ex) {
                pLogger.log(LogService.LOG_ERROR, "Error exporting service: "
                        + ex, ex);
            }
        }
    }

    /**
     * A new listener has been bound
     * 
     * @param aListener
     *            An ExportEndpoint listener
     */
    @Bind(id = ID_LISTENERS)
    private void bindListener(final IExportEndpointListener aListener) {

        if (!pEndpoints.isEmpty()) {
            aListener.endpointsAdded(pEndpoints.values().toArray(
                    new ExportEndpoint[0]));
        }
    }

    /**
     * Computes the end point name according to service properties
     * 
     * @param aEndpointName
     *            Pelix endpoint name (optional)
     * @param aServiceId
     *            Constant Service ID
     * @return The computed end point name
     */
    private String computeEndpointName(final String aEndpointName,
            final Long aServiceId) {

        if (aEndpointName != null && !aEndpointName.isEmpty()) {
            return aEndpointName;
        }

        return "service_" + aServiceId;
    }

    /**
     * Computes the exported configurations to filter exporters. Returns null if
     * no configuration has been specified
     * 
     * @param aExportedConfigs
     *            The value of the {@link Constants#SERVICE_EXPORTED_CONFIGS}
     *            service property
     * @return An array of configurations, or null
     */
    private String[] computeExportedConfigs(final Object aExportedConfigs) {

        if (aExportedConfigs == null || aExportedConfigs.equals("*")) {
            // No configuration specified or all accepted
            return null;
        }

        if (aExportedConfigs instanceof CharSequence) {
            // Single configuration
            return new String[] { aExportedConfigs.toString() };

        } else if (aExportedConfigs instanceof String[]) {
            // Already an array of strings
            return (String[]) aExportedConfigs;

        } else if (aExportedConfigs instanceof Collection) {
            // Convert the list into an array
            return ((Collection<?>) aExportedConfigs).toArray(new String[0]);
        }

        // Unknown content: accept all
        return null;
    }

    /**
     * Exports the given service using all available matching providers
     * 
     * @param aSvcRef
     *            Reference to the exported service
     */
    private synchronized void exportService(final ServiceReference<?> aSvcRef) {

        // Check exporters
        if (pExporters.isEmpty()) {
            pLogger.log(LogService.LOG_WARNING, "No exporters yet");
            return;
        }

        // Get export configurations
        final String[] configs = computeExportedConfigs(aSvcRef
                .getProperty(Constants.SERVICE_EXPORTED_CONFIGS));

        // Get matching exporters
        final Collection<IServiceExporter> exporters = new LinkedList<IServiceExporter>();
        if (configs == null) {
            // No filter on export configurations
            exporters.addAll(pExporters);

        } else {
            // Only accept part of the exporters
            for (final IServiceExporter exporter : pExporters) {
                if (exporter.handles(configs)) {
                    exporters.add(exporter);
                }
            }
        }

        if (exporters.isEmpty()) {
            pLogger.log(LogService.LOG_WARNING,
                    "No exporter for " + Arrays.toString(configs));
        }

        // Compute the endpoint name
        final String name = computeEndpointName(
                (String) aSvcRef
                        .getProperty(IRemoteServicesConstants.PROP_ENDPOINT_NAME),
                (Long) aSvcRef.getProperty(Constants.SERVICE_ID));

        // Create endpoints
        final Collection<ExportEndpoint> endpoints = new LinkedList<ExportEndpoint>();
        final Collection<String> serviceUids = getServiceUids(aSvcRef);

        for (final IServiceExporter exporter : exporters) {
            try {
                // Create the endpoint
                final ExportEndpoint endpoint = exporter.exportService(aSvcRef,
                        name, pFrameworkUid);
                if (endpoint == null) {
                    // Export refused
                    continue;
                }

                // Store the endpoint
                final String uid = endpoint.getUid();
                endpoints.add(endpoint);
                pEndpoints.put(uid, endpoint);
                pUidExporter.put(uid, exporter);
                serviceUids.add(uid);

            } catch (final Exception ex) {
                pLogger.log(LogService.LOG_ERROR, "Error exporting service: "
                        + ex, ex);
            }
        }

        if (endpoints.isEmpty()) {
            pLogger.log(LogService.LOG_WARNING, "No endpoint created for "
                    + aSvcRef);
            return;
        }

        // Call listeners
        final ExportEndpoint[] endpointsArray = endpoints
                .toArray(new ExportEndpoint[endpoints.size()]);
        for (final IExportEndpointListener listener : pListeners) {
            listener.endpointsAdded(endpointsArray);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IExportsDispatcher#getEndpoint(java.lang.String)
     */
    @Override
    public synchronized ExportEndpoint getEndpoint(final String aUid) {

        return pEndpoints.get(aUid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.pelix.IExportsDispatcher#getEndpoints()
     */
    @Override
    public synchronized ExportEndpoint[] getEndpoints() {

        return pEndpoints.values().toArray(new ExportEndpoint[0]);
    }

    /**
     * Retrieves the endpoints UIDs associated to the given service reference.
     * Creates a new set if necessary.
     * 
     * @param aReference
     *            A service reference
     * @return The set of endpoints UIDs associated to the service
     */
    private Collection<String> getServiceUids(
            final ServiceReference<?> aReference) {

        Collection<String> uids = pServiceUids.get(aReference);
        if (uids == null) {
            uids = new LinkedHashSet<String>();
            pServiceUids.put(aReference, uids);
        }

        return uids;
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Update validation flag
        pValidated = false;

        // Unregister from service events
        pBundleContext.removeServiceListener(this);

        // Clean up
        pFrameworkUid = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
     * ServiceEvent)
     */
    @Override
    public void serviceChanged(final ServiceEvent aEvent) {

        final ServiceReference<?> svcRef = aEvent.getServiceReference();

        switch (aEvent.getType()) {
        case ServiceEvent.REGISTERED:
            // New service to export
            exportService(svcRef);
            break;

        case ServiceEvent.MODIFIED:
            if (pServiceUids.containsKey(svcRef)) {
                // Already known service
                updateService(svcRef);

            } else {
                // New match
                exportService(svcRef);
            }

        case ServiceEvent.MODIFIED_ENDMATCH:
        case ServiceEvent.UNREGISTERING:
            // Service must not be exported
            unexportService(svcRef);

        default:
            break;
        }
    }

    /**
     * Removes the endpoints associated to the given service
     * 
     * @param aSvcRef
     *            Unexported Service reference
     */
    private synchronized void unexportService(final ServiceReference<?> aSvcRef) {

        // Get UIDs of service endpoints
        final Collection<String> uids = pServiceUids.remove(aSvcRef);
        if (uids == null) {
            return;
        }

        for (final String uid : uids) {
            // Remote from storage
            final ExportEndpoint endpoint = pEndpoints.remove(uid);
            final IServiceExporter exporter = pUidExporter.remove(uid);

            // Delete endpoint
            exporter.unexportService(endpoint);

            // Call listeners
            for (final IExportEndpointListener listener : pListeners) {
                listener.endpointRemoved(endpoint);
            }
        }
    }

    /**
     * Updates the properties associated to an exported service
     * 
     * @param aSvcRef
     *            The reference to an exported service
     */
    private synchronized void updateService(final ServiceReference<?> aSvcRef) {

        final Collection<String> uids = pServiceUids.get(aSvcRef);
        if (uids == null) {
            // Unknown service reference
            return;
        }

        for (final String uid : uids) {
            // Get related information
            final IServiceExporter exporter = pUidExporter.get(uid);
            final ExportEndpoint endpoint = pEndpoints.get(uid);

            // Compute its new name
            final String newName = computeEndpointName(
                    (String) aSvcRef
                            .getProperty(IRemoteServicesConstants.PROP_ENDPOINT_NAME),
                    (Long) aSvcRef.getProperty(Constants.SERVICE_ID));

            // Update export
            try {
                // FIXME: compute/ignore old properties
                final Map<String, Object> oldProperties = null;

                exporter.updateExport(endpoint, newName, oldProperties);

                // Call listeners
                for (final IExportEndpointListener listener : pListeners) {
                    listener.endpointUpdated(endpoint, oldProperties);
                }

            } catch (final IllegalArgumentException ex) {
                // New name refused
                pLogger.log(LogService.LOG_ERROR,
                        "Error updating service properties: " + ex, ex);

                // Remove this endpoint
                exporter.unexportService(endpoint);
                for (final IExportEndpointListener listener : pListeners) {
                    listener.endpointRemoved(endpoint);
                }
            }
        }
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Update validation flag
        pValidated = true;

        // Setup the isolate UID
        pFrameworkUid = RSUtils.setupUID(pBundleContext,
                IRemoteServicesConstants.ISOLATE_UID);

        // Prepare the LDAP filter
        final String filter = String.format("(|(%s=*)(%s=*))",
                Constants.SERVICE_EXPORTED_CONFIGS,
                Constants.SERVICE_EXPORTED_INTERFACES);

        try {
            // Export existing services
            final ServiceReference<?>[] svcRefs = pBundleContext
                    .getServiceReferences((String) null, filter);
            if (svcRefs != null) {
                for (final ServiceReference<?> svcRef : svcRefs) {
                    exportService(svcRef);
                }
            }

            // Register a service listener
            pBundleContext.addServiceListener(this, filter);

        } catch (final InvalidSyntaxException ex) {
            // Bad luck
            ex.printStackTrace();
        }
    }
}

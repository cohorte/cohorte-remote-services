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

import java.util.Collection;
import java.util.LinkedHashMap;
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
import org.cohorte.remote.IRemoteServicesConstants;
import org.cohorte.remote.pelix.IImportEndpointListener;
import org.cohorte.remote.pelix.IImportsRegistry;
import org.cohorte.remote.pelix.IServiceImporter;
import org.cohorte.remote.pelix.ImportEndpoint;
import org.cohorte.remote.utilities.RSUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * Implementation of the imports registry
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-registry-factory")
@Provides(specifications = IImportsRegistry.class)
@Instantiate(name = "cohorte-remote-registry")
public class ImportsRegistry implements IImportsRegistry {

    /** iPOJO dependency ID */
    private static final String ID_IMPORTERS = "importers";

    /** iPOJO dependency ID */
    private static final String ID_LISTENERS = "listeners";

    /** Bundle context */
    private final BundleContext pBundleContext;

    /** Framework UID -&gt; Endpoints */
    private final Map<String, Collection<ImportEndpoint>> pFrameworks = new LinkedHashMap<String, Collection<ImportEndpoint>>();

    /** Framework UID */
    private String pFrameworkUid;

    /** Service Importers */
    @Requires(id = ID_IMPORTERS, optional = true)
    private List<IServiceImporter> pImporters;

    /** Endpoints listeners */
    @Requires(id = ID_LISTENERS, optional = true)
    private List<IImportEndpointListener> pListeners;

    /** The log service */
    @Requires
    private LogService pLogger;

    /** Endpoint UID -gt; Endpoint */
    private final Map<String, ImportEndpoint> pRegistry = new LinkedHashMap<String, ImportEndpoint>();

    /** Validation flag */
    private boolean pValidated = false;

    /**
     * Component constructed
     * 
     * @param aContext
     *            the bundle context
     */
    public ImportsRegistry(final BundleContext aContext) {

        pBundleContext = aContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IImportsRegistry#add(org.cohorte.remote.pelix
     * .ImportEndpoint)
     */
    @Override
    public synchronized boolean add(final ImportEndpoint aEndpoint) {

        final String uid = aEndpoint.getUid();
        final String fwUid = aEndpoint.getFrameworkUid();

        if (pFrameworkUid.equals(fwUid)) {
            // Avoid to import our own services
            pLogger.log(LogService.LOG_DEBUG,
                    "ImportEndpoint with same framework UID");
            return false;
        }

        // Check if the endpoint already exists
        if (pRegistry.containsKey(uid)) {
            pLogger.log(LogService.LOG_DEBUG, "Already known endpoint: " + uid);
            return false;
        }

        // Store the endpoint
        pRegistry.put(uid, aEndpoint);
        if (fwUid != null && !fwUid.isEmpty()) {
            addToFramework(fwUid, aEndpoint);
        }

        // Notify listeners
        for (final IImportEndpointListener listener : pListeners) {
            listener.endpointAdded(aEndpoint);
        }

        return true;
    }

    /**
     * Associates the given endpoint to a framework UID
     * 
     * @param aFwUid
     *            A framework UID
     * @param aEndpoint
     *            An imported endpoint
     */
    private synchronized void addToFramework(final String aFwUid,
            final ImportEndpoint aEndpoint) {

        Collection<ImportEndpoint> endpoints = pFrameworks.get(aFwUid);
        if (endpoints == null) {
            // Create the collection
            endpoints = new LinkedList<ImportEndpoint>();
            pFrameworks.put(aFwUid, endpoints);
        }

        // Store the endpoint
        endpoints.add(aEndpoint);
    }

    /**
     * A new endpoint listener has been bound
     * 
     * @param aListener
     *            An endpoint listener
     */
    @Bind(id = ID_LISTENERS)
    private void bindListener(final IImportEndpointListener aListener) {

        if (pValidated) {
            for (final ImportEndpoint endpoint : pRegistry.values()) {
                aListener.endpointAdded(endpoint);
            }
        }
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Update validation flag
        pValidated = false;

        // Clean up
        pFrameworkUid = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IImportsRegistry#lostFramework(java.lang.String)
     */
    @Override
    public synchronized void lostFramework(final String aFrameworkUid) {

        // Pop the endpoints associated to the framework
        final Collection<ImportEndpoint> endpoints = pFrameworks
                .remove(aFrameworkUid);
        if (endpoints == null) {
            // Nothing to do
            return;
        }

        for (final ImportEndpoint endpoint : endpoints) {
            // Remove the endpoint
            pRegistry.remove(endpoint.getUid());

            // Notify listeners
            for (final IImportEndpointListener listener : pListeners) {
                listener.endpointRemoved(endpoint);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.pelix.IImportsRegistry#remove(java.lang.String)
     */
    @Override
    public synchronized void remove(final String aUid) {

        final ImportEndpoint endpoint = pRegistry.remove(aUid);
        if (endpoint == null) {
            // Unknown endpoint
            pLogger.log(LogService.LOG_WARNING, "Unknown import endpoint UID: "
                    + aUid);
            return;
        }

        // Remove it from its framework
        final Collection<ImportEndpoint> frameworkEndpoints = pFrameworks
                .get(endpoint.getFrameworkUid());
        if (frameworkEndpoints != null) {
            frameworkEndpoints.remove(endpoint);

            if (frameworkEndpoints.isEmpty()) {
                // Remove framework entry if there is no more endpoint from it
                pFrameworks.remove(endpoint.getFrameworkUid());
            }
        }

        // Notify listeners
        for (final IImportEndpointListener listener : pListeners) {
            listener.endpointRemoved(endpoint);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.pelix.IImportsRegistry#update(java.lang.String,
     * java.util.Map)
     */
    @Override
    public synchronized void update(final String aUid,
            final Map<String, Object> aNewProperties) {

        final ImportEndpoint endpoint = pRegistry.remove(aUid);
        if (endpoint == null) {
            // Unknown endpoint, ignore it
            return;
        }

        // Replace the stored properties
        final Map<String, Object> oldProperties = endpoint.getProperties();
        endpoint.setProperties(aNewProperties);

        // Notify listeners
        for (final IImportEndpointListener listener : pListeners) {
            listener.endpointUpdated(endpoint, oldProperties);
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
    }
}

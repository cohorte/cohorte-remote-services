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
package org.cohorte.remote.jsonrpc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.remote.ExportEndpoint;
import org.cohorte.remote.IRemoteServicesConstants;
import org.cohorte.remote.IServiceExporter;
import org.cohorte.remote.utilities.BundlesClassLoader;
import org.cohorte.remote.utilities.RSUtils;
import org.jabsorb.ng.JSONRPCBridge;
import org.jabsorb.ng.JSONRPCServlet;
import org.jabsorb.ng.client.HTTPSessionFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

/**
 * Implementation of the COHORTE JABSORB-RPC service exporter
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-exporter-jabsorb-factory")
@Provides(specifications = IServiceExporter.class)
@Instantiate(name = "cohorte-remote-exporter-jabsorb")
public class JabsorbRpcExporter implements IServiceExporter {

    /** HTTP service port property */
    private static final String HTTP_SERVICE_PORT = "org.osgi.service.http.port";

    /** HTTPService dependency ID */
    private static final String IPOJO_ID_HTTP = "http.service";

    /** Supported export configurations */
    @Property(name = Constants.REMOTE_CONFIGS_SUPPORTED, value = "{jabsorbrpc}")
    private String[] pConfigurations;

    /** The bundle context */
    private final BundleContext pContext;

    /** Exported services: Name -gt; ExportEndpoint */
    private final Map<String, ExportEndpoint> pEndpoints = new LinkedHashMap<String, ExportEndpoint>();

    /** Framework UID */
    private String pFrameworkUid;

    /** HTTP service port */
    private int pHttpPort;

    /** HTTP service, to host the Jabsorb servlet */
    @Requires(id = IPOJO_ID_HTTP, filter = "(" + HTTP_SERVICE_PORT + "=*)")
    private HttpService pHttpService;

    /** The JSON-RPC bridge (Jabsorb) */
    private JSONRPCBridge pJsonRpcBridge;

    /** The logger */
    @Requires
    private LogService pLogger;

    /** Name of the Jabsorb servlet */
    @Property(name = "endpoint.servlet.name",
            value = IJabsorbRpcConstants.DEFAULT_SERVLET_NAME)
    private String pServletName;

    /**
     * Component constructed
     * 
     * @param aContext
     *            The bundle context
     */
    public JabsorbRpcExporter(final BundleContext aContext) {

        pContext = aContext;
    }

    /**
     * HTTP service ready: store its listening port
     * 
     * @param aHttpService
     *            The bound service
     * @param aServiceProperties
     *            The HTTP service properties
     */
    @Bind(id = IPOJO_ID_HTTP)
    private void bindHttpService(final HttpService aHttpService,
            final Map<?, ?> aServiceProperties) {

        final Object rawPort = aServiceProperties.get(HTTP_SERVICE_PORT);

        if (rawPort instanceof Number) {
            // Get the integer
            pHttpPort = ((Number) rawPort).intValue();

        } else if (rawPort instanceof CharSequence) {
            // Parse the string
            pHttpPort = Integer.parseInt(rawPort.toString());

        } else {
            // Unknown port type
            pLogger.log(LogService.LOG_WARNING, "Couldn't read access port="
                    + rawPort);
            pHttpPort = -1;
        }

        pLogger.log(LogService.LOG_INFO, "JABSORB-RPC endpoint bound to port="
                + pHttpPort);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IServiceExporter#exportService(org.osgi.framework
     * .ServiceReference, java.lang.String, java.lang.String)
     */
    @Override
    public synchronized ExportEndpoint exportService(
            final ServiceReference<?> aReference, final String aName,
            final String aFramworkUid) throws BundleException {

        // Prefer the name given in properties, if any
        String name = (String) aReference
                .getProperty(IJabsorbRpcConstants.PROP_ENDPOINT_NAME);
        if (name == null) {
            name = aName;
        }

        if (pEndpoints.containsKey(name)) {
            pLogger.log(LogService.LOG_ERROR,
                    "Already use JABSORB-RPC endpoint: " + name);
            return null;
        }

        // Get the service
        final Object service = pContext.getService(aReference);

        // Prepare extra properties
        final Map<String, Object> extraProps = new LinkedHashMap<String, Object>();

        // ... endpoint name
        extraProps.put(IJabsorbRpcConstants.PROP_ENDPOINT_NAME, name);

        // ... HTTP Accesses
        extraProps.put(IJabsorbRpcConstants.PROP_HTTP_ACCESSES, getAccesses());

        // Prepare the endpoint bean
        final ExportEndpoint endpoint = new ExportEndpoint(UUID.randomUUID()
                .toString(), pFrameworkUid, pConfigurations, name, aReference,
                service, extraProps);

        // Register the object in the Jabsorb bridge
        pJsonRpcBridge.registerObject(name, service);

        // Store information
        pEndpoints.put(name, endpoint);
        return endpoint;
    }

    /**
     * Prepares the String containing the list of URLs to access the Jabsorb
     * bridge servlet
     * 
     * @return A comma-separated list of URLs
     */
    private String getAccesses() {

        // Forge the URI manually, as '{' and '}' are forbidden in URIs
        // (that's why we use it)
        final StringBuilder builder = new StringBuilder("http://{server}");

        if (pHttpPort > 0) {
            // Port given
            builder.append(":").append(pHttpPort);
        }

        if (!pServletName.startsWith("/")) {
            // Add path starting slash if necessary
            builder.append("/");
        }

        builder.append(pServletName);
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IServiceExporter#handles(java.lang.String[])
     */
    @Override
    public boolean handles(final String[] aConfigurations) {

        if (aConfigurations == null) {
            // null = "match all"
            return true;
        }

        // Look for a match in configurations
        for (final String config : aConfigurations) {
            for (final String handledConfig : pConfigurations) {
                if (handledConfig.equals(config)) {
                    // Got a match
                    return true;
                }
            }
        }

        // No match
        return false;
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        // Clean up
        stopJabsorbBridge();
        pFrameworkUid = null;

        pLogger.log(LogService.LOG_INFO, "JABSORB-RPC exporter gone");
    }

    /**
     * Sets up the Jabsorb bridge
     */
    private void startJabsorbBridge() {

        // Register the Jabsorb servlet
        try {
            pHttpService.registerServlet(pServletName, new JSONRPCServlet(),
                    null, null);

        } catch (final Exception ex) {
            pLogger.log(LogService.LOG_INFO,
                    "Error registering the JABSORB-RPC servlet (Jabsorb)", ex);
        }

        // Set the bridge
        pJsonRpcBridge = JSONRPCBridge.getGlobalBridge();

        // Set the serializer class loader
        final BundlesClassLoader classLoader = new BundlesClassLoader(pContext);
        JSONRPCBridge.getSerializer().setClassLoader(classLoader);

        // Set the HTTP session provider
        HTTPSessionFactory
                .setHTTPSessionProvider(new JabsorbHttpSessionProvider());
    }

    /**
     * Cleans up the Jabsorb bridge references.
     */
    private void stopJabsorbBridge() {

        // Unregister the servlet
        pHttpService.unregister(pServletName);

        // Destroy end points
        final ExportEndpoint[] endpoints = pEndpoints.values().toArray(
                new ExportEndpoint[0]);
        for (final ExportEndpoint endpoint : endpoints) {
            try {
                // Release the service, unregister the endpoint
                unexportService(endpoint);

            } catch (final Exception ex) {
                // Just log the error
                pLogger.log(LogService.LOG_WARNING,
                        "Error unregistering service: " + ex, ex);
            }
        }

        // Clean up references
        HTTPSessionFactory.setHTTPSessionProvider(null);
        JSONRPCBridge.getSerializer().setClassLoader(null);
        pJsonRpcBridge = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IServiceExporter#unexportService(org.cohorte
     * .remote.pelix.ExportEndpoint)
     */
    @Override
    public synchronized void unexportService(final ExportEndpoint aEndpoint) {

        // Pop the endpoint
        if (pEndpoints.remove(aEndpoint.getName()) != null) {
            // Destroy the endpoint
            pJsonRpcBridge.unregisterObject(aEndpoint.getName());

            // Release the service
            pContext.ungetService(aEndpoint.getReference());

        } else {
            // Unknown endpoint
            pLogger.log(LogService.LOG_WARNING, "Unknown endpoint: "
                    + aEndpoint);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IServiceExporter#updateExport(org.cohorte.remote
     * .pelix.ExportEndpoint, java.lang.String, java.util.Map)
     */
    @Override
    public synchronized void updateExport(final ExportEndpoint aEndpoint,
            final String aNewName, final Map<String, Object> aOldProperties) {

        if (pEndpoints.containsKey(aNewName)) {
            // Reject the new name
            throw new IllegalArgumentException("New name of " + aEndpoint
                    + " is already in use: " + aNewName);
        }

        // Update storage
        pEndpoints.put(aNewName, pEndpoints.remove(aEndpoint.getName()));

        // Update the endpoint
        aEndpoint.setName(aNewName);
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Setup the isolate UID
        pFrameworkUid = RSUtils.setupUID(pContext,
                IRemoteServicesConstants.ISOLATE_UID);

        // Start the bridge
        startJabsorbBridge();

        pLogger.log(LogService.LOG_INFO, "JABSORB-RPC exporter ready, port="
                + pHttpPort);
    }
}

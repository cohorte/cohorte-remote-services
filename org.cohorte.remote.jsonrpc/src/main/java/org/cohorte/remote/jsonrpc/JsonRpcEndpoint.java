/**
 * Copyright 2013 isandlaTech
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.remote.IEndpointHandler;
import org.cohorte.remote.beans.EndpointDescription;
import org.cohorte.remote.utilities.BundlesClassLoader;
import org.jabsorb.ng.JSONRPCBridge;
import org.jabsorb.ng.JSONRPCServlet;
import org.jabsorb.ng.client.HTTPSessionFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

/**
 * Implementation of the COHORTE JSON-RPC end point handler. Uses Jabsorb.
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-endpoint-jsonrpc-factory")
@Provides(specifications = IEndpointHandler.class)
@Instantiate(name = "cohorte-remote-endpoint-jsonrpc")
public class JsonRpcEndpoint implements IEndpointHandler {

    /** HTTP service port property */
    private static final String HTTP_SERVICE_PORT = "org.osgi.service.http.port";

    /** HTTPService dependency ID */
    private static final String IPOJO_ID_HTTP = "http.service";

    /** The bundle context */
    private final BundleContext pBundleContext;

    /** Service -&gt; End point description mapping */
    private final Map<ServiceReference<?>, EndpointDescription[]> pEndpointsDescriptions = new HashMap<ServiceReference<?>, EndpointDescription[]>();

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

    /** The registered end points names list */
    private final List<String> pRegisteredEndpoints = new ArrayList<String>();

    /** Name of the Jabsorb servlet */
    @Property(name = "endpoint.servlet.name",
            value = IJsonRpcConstants.DEFAULT_SERVLET_NAME)
    private String pServletName;

    /**
     * Prepares the component
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public JsonRpcEndpoint(final BundleContext aBundleContext) {

        super();
        pBundleContext = aBundleContext;
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
            pLogger.log(LogService.LOG_WARNING,
                    String.format("Couldn't read access port=%d", rawPort));
            pHttpPort = -1;
        }

        pLogger.log(LogService.LOG_INFO,
                String.format("JSON-RPC endpoint bound to port=%d", pHttpPort));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.IEndpointHandler#createEndpoint(java.util.Set,
     * org.osgi.framework.ServiceReference)
     */
    @Override
    public EndpointDescription[] createEndpoint(
            final Set<String> aExportedInterfaces,
            final ServiceReference<?> aServiceReference) {

        if (aServiceReference == null) {
            return null;
        }

        // Copy service properties in a map
        final Map<String, String> serviceProperties = getServicePropertiesMap(aServiceReference);

        // Compute a end point name
        final String endPointName = generateEndpointName(serviceProperties);

        // Get a reference to the service
        final Object serviceInstance = pBundleContext
                .getService(aServiceReference);
        if (serviceInstance == null) {
            pLogger.log(LogService.LOG_ERROR,
                    "The service reference to export as no associated instance.");
            return null;
        }

        // Create the end point
        /*
         * FIXME: avoid to export all public methods
         * 
         * pJsonRpcBridge.registerObject(endPointName, serviceInstance,
         * interfaceClass);
         */
        pJsonRpcBridge.registerObject(endPointName, serviceInstance);

        // Keep a track of the end point
        pRegisteredEndpoints.add(endPointName);

        // Prepare the end point description to return
        final EndpointDescription endpointDescription = new EndpointDescription(
                IJsonRpcConstants.EXPORT_CONFIGS[0], endPointName,
                IJsonRpcConstants.EXPORT_PROTOCOL,
                makeEndpointUri(endPointName), pHttpPort);

        // Make an array
        final EndpointDescription[] result = new EndpointDescription[] { endpointDescription };

        // Store the information
        pEndpointsDescriptions.put(aServiceReference, result);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IEndpointHandler#destroyEndpoint(org.osgi.framework
     * .ServiceReference)
     */
    @Override
    public boolean destroyEndpoint(final ServiceReference<?> aServiceReference) {

        final String endpointName = (String) aServiceReference
                .getProperty(PROP_ENDPOINT_NAME);
        if (!pRegisteredEndpoints.contains(endpointName)) {
            // Unknown service
            return false;
        }

        // Destroy the end point
        pJsonRpcBridge.unregisterObject(endpointName);
        pEndpointsDescriptions.remove(aServiceReference);
        pRegisteredEndpoints.remove(endpointName);

        // We do not use the service anymore
        pBundleContext.ungetService(aServiceReference);

        return true;
    }

    /**
     * Prepares an end point name, based on service properties
     * 
     * @param aServiceProperties
     *            Properties of the exported service
     * @return An end point name, never null
     */
    private String generateEndpointName(
            final Map<String, String> aServiceProperties) {

        // Compute a end point name
        String endpointName = aServiceProperties.get(PROP_ENDPOINT_NAME);
        if (endpointName == null) {
            endpointName = "service"
                    + aServiceProperties.get(Constants.SERVICE_ID);
        }

        return endpointName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.IEndpointHandler#getEndpoints(org.osgi.framework.
     * ServiceReference)
     */
    @Override
    public EndpointDescription[] getEndpoints(
            final ServiceReference<?> aServiceReference) {

        final EndpointDescription[] result = pEndpointsDescriptions
                .get(aServiceReference);

        if (result == null) {
            // Never return null
            return new EndpointDescription[0];
        }

        return result;
    }

    /**
     * Retrieves the service properties as a String -&gt; String map
     * 
     * @param aServiceReference
     *            Service reference
     * @return The service properties, as a map
     */
    private Map<String, String> getServicePropertiesMap(
            final ServiceReference<?> aServiceReference) {

        // Get the service properties keys
        final String[] propertyKeys = aServiceReference.getPropertyKeys();

        if (propertyKeys == null) {
            // Very unlikely case - return an empty map
            return new HashMap<String, String>();
        }

        // Prepare the result
        final Map<String, String> result = new HashMap<String, String>(
                propertyKeys.length);

        for (final String key : propertyKeys) {
            final Object value = aServiceReference.getProperty(key);

            if (value == null) {
                // Keep null values
                result.put(key, null);

            } else {
                // Convert others
                result.put(key, String.valueOf(value));
            }
        }

        return result;
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidatePojo() {

        // Clean up the bridge
        stopJabsorbBridge();

        // Clear structures
        pEndpointsDescriptions.clear();
        pRegisteredEndpoints.clear();

        pLogger.log(LogService.LOG_INFO, "JSON-RPC endpoint handler gone");
    }

    /**
     * Generates the URI to access the given end point
     * 
     * @param aEndpointName
     *            A end point name
     * @return The URI to access the end point
     */
    private String makeEndpointUri(final String aEndpointName) {

        final StringBuilder builder = new StringBuilder(pServletName);
        builder.append("/").append(aEndpointName);

        return builder.toString();
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
                    "Error registering the JSON-RPC servlet (Jabsorb)", ex);
        }

        // Set the bridge
        pJsonRpcBridge = JSONRPCBridge.getGlobalBridge();

        // Set the serializer class loader
        final BundlesClassLoader classLoader = new BundlesClassLoader(
                pBundleContext);
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
        final ServiceReference<?>[] references = pEndpointsDescriptions
                .keySet().toArray(new ServiceReference<?>[0]);
        for (final ServiceReference<?> svcRef : references) {
            // Destroys the end point and frees the service
            destroyEndpoint(svcRef);
        }

        // Clean up references
        HTTPSessionFactory.setHTTPSessionProvider(null);
        JSONRPCBridge.getSerializer().setClassLoader(null);
        pJsonRpcBridge = null;
    }

    /**
     * Component validated
     */
    @Validate
    public void validatePojo() {

        // Be sure to have clean members
        pEndpointsDescriptions.clear();
        pRegisteredEndpoints.clear();

        // Start the bridge
        startJabsorbBridge();

        pLogger.log(LogService.LOG_INFO, String.format(
                "JSON-RPC endpoint handler ready, port=%d", pHttpPort));
    }
}

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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.remote.IRemoteServiceClientHandler;
import org.cohorte.remote.beans.EndpointDescription;
import org.cohorte.remote.beans.RemoteServiceRegistration;
import org.cohorte.remote.utilities.BundleClass;
import org.cohorte.remote.utilities.BundlesClassLoader;
import org.jabsorb.ng.client.Client;
import org.jabsorb.ng.client.ISession;
import org.jabsorb.ng.client.TransportRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * Implementation of the COHORTE JSON-RPC client. Uses Jabsorb.
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-client-jsonrpc-factory")
@Provides(specifications = IRemoteServiceClientHandler.class)
@Instantiate(name = "cohorte-remote-client-jsonrpc")
public class JsonRpcClient implements IRemoteServiceClientHandler {

    /** Bundles providing classes */
    private final Map<Bundle, List<Class<?>>> pBundleClasses = new HashMap<Bundle, List<Class<?>>>();

    /** The bundle context */
    private final BundleContext pBundleContext;

    /** The logger */
    @Requires
    private LogService pLogger;

    /** Service ID -&gt; Classes missing for a full end point proxy */
    private final Map<String, List<String>> pMissingClasses = new HashMap<String, List<String>>();

    /** Proxy -&gt; Jabsorb Client map */
    private final Map<Object, Client> pProxies = new HashMap<Object, Client>();

    /** Proxy -&gt; Service ID */
    private final Map<Object, String> pServiceIds = new HashMap<Object, String>();

    /**
     * Sets up the Jabsorb client
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public JsonRpcClient(final BundleContext aBundleContext) {

        super();
        pBundleContext = aBundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceClientHandler#bundleStarted(org.osgi
     * .framework.Bundle)
     */
    @Override
    public Collection<String> bundleStarted(final Bundle aBundle) {

        // IDs of the services that must be updated
        final List<String> serviceIds = new LinkedList<String>();

        for (final Entry<String, List<String>> entry : pMissingClasses
                .entrySet()) {
            for (final String clazz : entry.getValue()) {
                try {
                    // Try to load the class
                    aBundle.loadClass(clazz);

                    // Class found: the service can be updated
                    serviceIds.add(entry.getKey());

                    // Test next service
                    break;

                } catch (final ClassNotFoundException ex) {
                    // Class not found, continue
                }
            }
        }

        // Clean up the missing classes map (as we it will be updated
        // afterwards)
        for (final String serviceId : serviceIds) {
            pMissingClasses.remove(serviceId);
        }

        return serviceIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceClientHandler#bundleStopped(org.osgi
     * .framework.Bundle)
     */
    @Override
    public Collection<String> bundleStopped(final Bundle aBundle) {

        // Get & clean the bundle classes list
        final List<Class<?>> classes = pBundleClasses.remove(aBundle);

        // IDs of the services that must be updated
        final List<String> serviceIds = new LinkedList<String>();

        // Update all proxies using one of those classes
        for (final Object proxy : pProxies.keySet()) {
            for (final Class<?> proxyInterface : proxy.getClass()
                    .getInterfaces()) {
                if (classes.contains(proxyInterface)) {
                    // This proxy must be updated
                    serviceIds.add(pServiceIds.get(proxy));
                    continue;
                }
            }
        }

        return serviceIds;
    }

    /**
     * Creates the proxy for the given interface at the given end point
     * 
     * @param aEndpoint
     *            An end point description
     * @param aClasses
     *            The interfaces exported at the end point
     * @return A proxy to that end point
     */
    private Object createProxy(final EndpointDescription aEndpoint,
            final Class<?>[] aClasses) {

        // Prepare a bundle class loader
        final BundlesClassLoader classLoader = new BundlesClassLoader(
                pBundleContext);

        // Create the Jabsorb client
        final ISession session = TransportRegistry.i().createSession(
                aEndpoint.computeURI());
        final Client client = new Client(session, classLoader);

        // Create the proxy
        final Object proxy = client.openProxy(aEndpoint.getEndpointName(),
                new BundlesClassLoader(pBundleContext), aClasses);

        // Store it
        pProxies.put(proxy, client);
        return proxy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceClientHandler#destroyProxy(java.lang
     * .Object)
     */
    @Override
    public synchronized void destroyProxy(final Object aProxy) {

        // Clear references
        final String serviceId = pServiceIds.remove(aProxy);
        pMissingClasses.remove(serviceId);

        // Destroy the proxy object
        if (pProxies.containsKey(aProxy)) {
            // Close the proxy
            final Client client = pProxies.get(aProxy);
            if (client != null) {
                pProxies.get(aProxy).closeProxy(aProxy);
            }

            // Remove its reference
            pProxies.remove(aProxy);
        }
    }

    /**
     * Looks for a known configuration in the exported ones
     * 
     * @param aEndpoints
     *            End points descriptions
     * @return The first handled description, null if none found
     */
    private EndpointDescription filterEndpoints(
            final EndpointDescription[] aEndpoints) {

        for (final EndpointDescription endpoint : aEndpoints) {

            // Test if the service has a JSON-RPC export configuration
            final String exportedConfig = endpoint.getExportedConfig();
            for (final String config : IJsonRpcConstants.EXPORT_CONFIGS) {
                if (config.equals(exportedConfig)) {
                    return endpoint;
                }
            }
        }

        return null;
    }

    /**
     * Tries to get the last exported interface of the given ones
     * 
     * @param aServiceId
     *            ID of the imported service, to report missing interfaces
     * @param aInterfacesNames
     *            Exported interfaces names
     * @return The accessible interfaces classes, or null if no interface has
     *         been given
     * @throws ClassNotFoundException
     *             None of the given interfaces is accessible
     */
    private Class<?>[] filterKnownInterfaces(final String aServiceId,
            final Collection<String> aInterfacesNames)
            throws ClassNotFoundException {

        // Invalid parameter
        if (aInterfacesNames == null || aInterfacesNames.isEmpty()) {
            pLogger.log(LogService.LOG_ERROR, "No/Empty interface list");
            return null;
        }

        // Keep track of unknown classes
        final List<String> unknownClasses = new LinkedList<String>();

        // Find all accessible classes
        final List<Class<?>> classes = new LinkedList<Class<?>>();
        for (final String interfaceName : aInterfacesNames) {
            if (interfaceName == null || interfaceName.isEmpty()) {
                // Invalid interface name
                continue;
            }

            // Finding the class using Class.forName(interfaceName) won't work.
            // Only look into active bundles (not resolved ones)
            final BundleClass foundClass = BundleClass.findClassInBundles(
                    pBundleContext.getBundles(), interfaceName, false);
            if (foundClass != null) {
                // Found an interface
                final Class<?> interfaceClass = foundClass.getLoadedClass();
                classes.add(interfaceClass);

                // Store the interface provider
                final Bundle bundle = foundClass.getBundle();
                List<Class<?>> bundleClasses = pBundleClasses.get(bundle);
                if (bundleClasses == null) {
                    bundleClasses = new LinkedList<Class<?>>();
                    pBundleClasses.put(bundle, bundleClasses);
                }
                bundleClasses.add(interfaceClass);

            } else {
                // Unknown class name
                unknownClasses.add(interfaceName);
            }
        }

        // No interface found at all
        if (classes.isEmpty()) {
            pLogger.log(LogService.LOG_ERROR, "No interface found in: "
                    + aInterfacesNames);
            throw new ClassNotFoundException(aInterfacesNames.toString());
        }

        // Some interfaces are missing
        if (!unknownClasses.isEmpty()) {
            pMissingClasses.put(aServiceId, unknownClasses);
            pLogger.log(LogService.LOG_WARNING, "Some interfaces are missing: "
                    + unknownClasses);
        }

        // Return the classes array
        return classes.toArray(new Class<?>[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceClientHandler#getRemoteProxy(org.cohorte
     * .remote.beans.RemoteServiceRegistration, java.util.Collection)
     */
    @Override
    public Object getRemoteProxy(final RemoteServiceRegistration aRegistration,
            final Collection<String> aFilteredInterfaces)
            throws ClassNotFoundException {

        if (aRegistration == null) {
            pLogger.log(LogService.LOG_ERROR, "Invalid service event");
            return null;
        }

        // Get available end points
        final EndpointDescription foundEndpoint = filterEndpoints(aRegistration
                .getEndpoints());
        if (foundEndpoint == null) {
            // Not exported with JSON-RPC
            pLogger.log(LogService.LOG_WARNING,
                    "Service is not exported with JSON-RPC");
            return null;
        }

        // Get the service ID
        final String serviceId = aRegistration.getServiceId();

        // Get the interface class
        final Class<?>[] interfaceClasses = filterKnownInterfaces(serviceId,
                aFilteredInterfaces);
        if (interfaceClasses != null) {
            // Create the proxy and store it
            final Object proxy = createProxy(foundEndpoint, interfaceClasses);
            pServiceIds.put(proxy, serviceId);
            return proxy;

        } else {
            // No interface could be found... (already logged information)
            return null;
        }
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public synchronized void invalidatePojo() {

        // Clean up all proxies
        for (final Entry<Object, Client> entry : pProxies.entrySet()) {

            // Close the proxy
            final Client client = entry.getValue();
            if (client != null) {
                client.closeProxy(entry.getKey());
            }
        }

        // Clear the lists
        pBundleClasses.clear();
        pMissingClasses.clear();
        pProxies.clear();
        pServiceIds.clear();

        pLogger.log(LogService.LOG_INFO,
                "COHORTE JSON-RPC Remote-Services proxy Gone");
    }

    /**
     * Component validated
     */
    @Validate
    public void validatePojo() {

        // Be sure to start from nothing...
        pProxies.clear();

        pLogger.log(LogService.LOG_INFO,
                "COHORTE JSON-RPC Remote-Services proxy Ready");
    }
}

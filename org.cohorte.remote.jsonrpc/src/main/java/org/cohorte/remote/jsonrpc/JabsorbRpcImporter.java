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

import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Requires;
import org.cohorte.remote.pelix.IServiceImporter;
import org.cohorte.remote.pelix.ImportEndpoint;
import org.cohorte.remote.utilities.BundleClass;
import org.cohorte.remote.utilities.BundlesClassLoader;
import org.jabsorb.ng.client.Client;
import org.jabsorb.ng.client.ISession;
import org.jabsorb.ng.client.TransportRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

/**
 * Implementation of the COHORTE JABSORB-RPC service importer
 * 
 * @author Thomas Calmant
 */
public class JabsorbRpcImporter implements IServiceImporter {

    /** Supported export configurations */
    @Property(name = Constants.REMOTE_CONFIGS_SUPPORTED,
            value = "{jabsorb-rpc}")
    private String[] pConfigurations;

    /** The bundle context */
    private final BundleContext pContext;

    /** The logger */
    @Requires
    private LogService pLogger;

    /** Java proxy -gt; Jabsorb Client */
    private final Map<Object, Client> pProxies = new LinkedHashMap<Object, Client>();

    /** Imported services: Endpoint UID -gt; ServiceRegistration */
    private final Map<String, ServiceRegistration<?>> pRegistrations = new LinkedHashMap<String, ServiceRegistration<?>>();

    /**
     * Component constructed
     * 
     * @param aContext
     *            The bundle context
     */
    public JabsorbRpcImporter(final BundleContext aContext) {

        pContext = aContext;
    }

    /**
     * Creates the Java proxy object to use the remote service, using Jabsorb
     * Client.
     * 
     * @param aName
     *            Endpoint name
     * @param aAccessUrl
     *            URL to the remote Jabsorb bridge servlet
     * @param aClasses
     *            Object interfaces
     * @return The proxy object
     */
    private Object createProxy(final String aName, final String aAccessUrl,
            final Class<?>[] aClasses) {

        // Prepare a bundle class loader
        final BundlesClassLoader classLoader = new BundlesClassLoader(pContext);

        // Create the Jabsorb client
        final ISession session = TransportRegistry.i()
                .createSession(aAccessUrl);
        final Client client = new Client(session, classLoader);

        // Create the proxy
        final Object proxy = client.openProxy(aName, classLoader, aClasses);

        // Store it
        pProxies.put(proxy, client);
        return proxy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IImportEndpointListener#endpointAdded(org.cohorte
     * .remote.pelix.ImportEndpoint)
     */
    @Override
    public synchronized void endpointAdded(final ImportEndpoint aEndpoint) {

        // Check if the export configurations match a known one
        boolean supportedConfig = false;
        final String[] configurations = aEndpoint.getConfigurations();
        for (final String config : configurations) {
            for (final String handledConfig : pConfigurations) {
                if (handledConfig.equals(config)) {
                    supportedConfig = true;
                    break;
                }
            }
        }

        if (!supportedConfig) {
            // Unknown export configuration, ignore
            return;
        }

        // Get the access URL
        final String rawAccessUrl = (String) aEndpoint.getProperties().get(
                IJsonRpcConstants.PROP_HTTP_ACCESSES);
        if (rawAccessUrl == null || rawAccessUrl.isEmpty()) {
            pLogger.log(LogService.LOG_WARNING, "No access URL given: "
                    + aEndpoint);
            return;
        }

        // FIXME: Get the first URL in the list
        final String accessUrl = rawAccessUrl.split(",")[0];
        if (aEndpoint.getServer() != null) {
            accessUrl.replace("{server}", aEndpoint.getServer());
        }

        pLogger.log(LogService.LOG_DEBUG, "Chosen access: " + accessUrl);

        // Check if endpoint is known
        if (pRegistrations.containsKey(aEndpoint.getUid())) {
            return;
        }

        // Compute the name
        final String name = (String) aEndpoint.getProperties().get(
                IJsonRpcConstants.PROP_ENDPOINT_NAME);
        if (name == null || name.isEmpty()) {
            pLogger.log(LogService.LOG_ERROR, "Remote endpoint has no name: "
                    + aEndpoint);
            return;
        }

        pLogger.log(LogService.LOG_DEBUG, "Importing " + aEndpoint
                + " with name: " + name);

        // Load interface classes
        final Class<?>[] classes;
        try {
            classes = loadInterfaces(aEndpoint.getSpecifications());

        } catch (final ClassNotFoundException ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "No specification class could be loaded: " + ex, ex);
            return;
        }

        // Register the service
        final Object service = createProxy(name, accessUrl, classes);
        final ServiceRegistration<?> svcReg = pContext.registerService(
                aEndpoint.getSpecifications(), service,
                new Hashtable<String, Object>(aEndpoint.getProperties()));

        // Store references
        pRegistrations.put(aEndpoint.getUid(), svcReg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IImportEndpointListener#endpointRemoved(org.
     * cohorte.remote.pelix.ImportEndpoint)
     */
    @Override
    public synchronized void endpointRemoved(final ImportEndpoint aEndpoint) {

        final String uid = aEndpoint.getUid();
        final ServiceRegistration<?> svcReg = pRegistrations.remove(uid);
        if (svcReg == null) {
            // Unknown endpoint
            pLogger.log(LogService.LOG_DEBUG, "Unknown endpoint: " + uid);
        }

        svcReg.unregister();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IImportEndpointListener#endpointUpdated(org.
     * cohorte.remote.pelix.ImportEndpoint, java.util.Map)
     */
    @Override
    public synchronized void endpointUpdated(final ImportEndpoint aEndpoint,
            final Map<String, Object> aOldProperties) {

        final ServiceRegistration<?> svcReg = pRegistrations.get(aEndpoint
                .getUid());
        if (svcReg == null) {
            // Unknown endpoint
            return;
        }

        // Update service properties
        svcReg.setProperties(new Hashtable<String, Object>(aEndpoint
                .getProperties()));
    }

    /**
     * Tries to load remote service interfaces
     * 
     * @param aSpecifications
     *            Remote service interfaces
     * @return An array of interfaces classes, or null
     * @throws ClassNotFoundException
     *             No specification class found
     */
    private Class<?>[] loadInterfaces(final String[] aSpecifications)
            throws ClassNotFoundException {

        // Invalid parameter
        if (aSpecifications == null || aSpecifications.length == 0) {
            pLogger.log(LogService.LOG_ERROR, "No/Empty interface list");
            return null;
        }

        // Keep track of unknown classes
        final List<String> unknownClasses = new LinkedList<String>();

        // Find all accessible classes
        final List<Class<?>> classes = new LinkedList<Class<?>>();
        for (final String interfaceName : aSpecifications) {
            if (interfaceName == null || interfaceName.isEmpty()) {
                // Invalid interface name
                continue;
            }

            // Finding the class using Class.forName(interfaceName) won't work.
            // Only look into active bundles (not resolved ones)
            final BundleClass foundClass = BundleClass.findClassInBundles(
                    pContext.getBundles(), interfaceName, false);
            if (foundClass != null) {
                // Found an interface
                final Class<?> interfaceClass = foundClass.getLoadedClass();
                classes.add(interfaceClass);

            } else {
                // Unknown class name
                unknownClasses.add(interfaceName);
            }
        }

        // No interface found at all
        if (classes.isEmpty()) {
            final String specificationsString = Arrays
                    .toString(aSpecifications);
            pLogger.log(LogService.LOG_ERROR, "No interface found in: "
                    + specificationsString);
            throw new ClassNotFoundException(specificationsString);
        }

        // Some interfaces are missing
        if (!unknownClasses.isEmpty()) {
            pLogger.log(LogService.LOG_WARNING, "Some interfaces are missing: "
                    + unknownClasses);
        }

        // Return the classes array
        return classes.toArray(new Class<?>[0]);
    }
}

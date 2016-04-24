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
package org.cohorte.ecf.provider.jabsorb.host;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.cohorte.ecf.provider.jabsorb.JabsorbConstants;
import org.jabsorb.ng.JSONRPCBridge;
import org.jabsorb.ng.JSONRPCServlet;
import org.jabsorb.ng.client.HTTPSessionFactory;
import org.jabsorb.ng.client.IHTTPSession;
import org.jabsorb.ng.client.IHTTPSessionProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * A BluePrint component that keeps track of all registered {@link HttpService}.
 * It simplifies the retrieval of all HTTP accesses to be sent by the host
 * container.
 * 
 * @author Thomas Calmant
 */
public class JabsorbHttpServiceComponent {

    /** Possible HTTP Service properties to get its listening port */
    private static final String[] HTTP_PORT_PROPERTIES = { "http.port",
            "org.osgi.service.http.port" };

    /** Singleton instance */
    private static JabsorbHttpServiceComponent sInstance;

    /**
     * Returns the instance of this component
     * 
     * @return The instance of this component
     */
    public static JabsorbHttpServiceComponent getInstance() {

        return sInstance;
    }

    /** Registered endpoints */
    private final Set<String> pEndpoints = new LinkedHashSet<String>();

    /** Ports of the HTTP Services this component is bound to */
    private final Set<Integer> pHttpPorts = new LinkedHashSet<Integer>();

    /** The Jabsorb bridge */
    private JSONRPCBridge pJabsorbRpcBridge;

    /**
     * Empty constructor
     */
    public JabsorbHttpServiceComponent() {

    }

    void activate(final BundleContext aCtxt) throws Exception {
        // Prepare the bridge
        pJabsorbRpcBridge = JSONRPCBridge.getGlobalBridge();

        // Set the HTTP session provider
        HTTPSessionFactory.setHTTPSessionProvider(new IHTTPSessionProvider() {
			@Override
			public IHTTPSession newHTTPSession(URI arg0) throws Exception {
				return new JabsorbHttpSession(arg0);
			}});
        
        sInstance = this;
    }

    /**
     * Declarative-Services - New HTTP Service
     * 
     * @param aHttpService
     *            The HTTP service
     * @param aProperties
     *            The service properties
     */
    void bindHttpService(final HttpService aHttpService,
            final Map<String, ?> aProperties) {

        // Get the port
        final int port = extractHttpPort(aProperties);
        if (port > 0) {
            pHttpPorts.add(port);
        }

        // Register the Jabsorb servlet
        try {
            aHttpService.registerServlet(JabsorbConstants.SERVER_DEFAULT_SERVLETPATH,
                    new JSONRPCServlet(), null, null);

        } catch (final ServletException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (final NamespaceException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    void deactivate() throws Exception {
        // Clean up
        pEndpoints.clear();
        sInstance = null;
    }

    /**
     * Tries to extract the port an HTTP service is listening to, according to
     * its properties
     * 
     * @param aProperties
     *            HTTP Service properties
     * @return The port it is listening to, or -1
     */
    private int extractHttpPort(final Map<String, ?> aProperties) {

        for (final String property : HTTP_PORT_PROPERTIES) {
            // Get the string value
            final String portStr = (String) aProperties.get(property);
            if (portStr != null) {
                try {
                    // Convert the string
                    final int port = Integer.parseInt(portStr);
                    if (port > 0) {
                        return port;
                    }

                } catch (final NumberFormatException ex) {
                    // Invalid string, continue
                    continue;
                }
            }
        }

        // No valid integer found
        return -1;
    }

    /**
     * Registers a service with the given name
     * 
     * @param aEndpointName
     *            An endpoint name
     * @param aService
     *            The service object
     * @throws IllegalArgumentException
     *             Already known endpoint name
     */
    public void registerEndpoint(final String aEndpointName,
            final Object aService) {

        if (pEndpoints.contains(aEndpointName)) {
            throw new IllegalArgumentException("Already known endpoint name: "
                    + aEndpointName);
        }

        pJabsorbRpcBridge.registerObject(aEndpointName, aService);
    }

    /**
     * Declarative-Services - HTTP Service gone
     * 
     * @param aHttpService
     *            The HTTP service
     * @param aProperties
     *            The service properties
     */
    protected void unbindHttpService(final HttpService aHttpService,
            final Map<String, ?> aProperties) {

        // Unregister the servlet from the lost service
        aHttpService.unregister(JabsorbConstants.SERVER_DEFAULT_SERVLETPATH);

        // Forget the port
        final int port = extractHttpPort(aProperties);
        if (port > 0) {
            pHttpPorts.remove(port);
        }

    }

    /**
     * Unregisters the service that has been registered with the given endpoint
     * name
     * 
     * @param aEndpointName
     *            An endpoint name
     */
    public void unregisterEndpoint(final String aEndpointName) {

        pJabsorbRpcBridge.unregisterObject(aEndpointName);
    }
}

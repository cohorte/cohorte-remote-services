/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.host;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.cohorte.ecf.provider.jabsorb.JabsorbConstants;
import org.cohorte.remote.utilities.BundlesClassLoader;
import org.eclipse.ecf.remoteservice.servlet.HttpServiceComponent;
import org.jabsorb.ng.JSONRPCBridge;
import org.jabsorb.ng.JSONRPCServlet;
import org.jabsorb.ng.client.HTTPSessionFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * All methods are implemented by HttpServiceComponent
 * 
 * @author Thomas Calmant
 */
public class JabsorbHttpServiceComponent extends HttpServiceComponent {

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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.servlet.HttpServiceComponent#activate(org
     * .osgi.framework.BundleContext)
     */
    @Override
    protected void activate(final BundleContext aCtxt) throws Exception {

        // Prepare the bridge
        pJabsorbRpcBridge = JSONRPCBridge.getGlobalBridge();

        // Set the HTTP session provider
        HTTPSessionFactory
                .setHTTPSessionProvider(new JabsorbHttpSessionProvider());

        // Set the serializer class loader
        JSONRPCBridge.getSerializer().setClassLoader(
                new BundlesClassLoader(aCtxt));

        // Let the component be activated
        super.activate(aCtxt);
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
    protected void bindHttpService(final HttpService aHttpService,
            final Map<String, ?> aProperties) {

        // Let the parent to the binding
        super.bindHttpService(aHttpService);

        // Get the port
        final int port = extractHttpPort(aProperties);
        if (port > 0) {
            pHttpPorts.add(port);
        }

        // Register the Jabsorb servlet
        try {
            aHttpService.registerServlet(JabsorbConstants.HOST_SERVLET_PATH,
                    new JSONRPCServlet(), null, null);

        } catch (final ServletException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (final NamespaceException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.servlet.HttpServiceComponent#deactivate()
     */
    @Override
    protected void deactivate() throws Exception {

        System.out.println("deactivate");

        // Clean up
        pEndpoints.clear();
        JSONRPCBridge.getSerializer().setClassLoader(null);
        sInstance = null;

        // Deactivate the component
        super.deactivate();
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
     * Retrieves all possible accesses to the bound HTTP services
     * 
     * @return An array of URI strings, can be empty but never null
     */
    public String[] getAccesses() {

        final Set<String> accesses = new LinkedHashSet<String>();

        // Get interfaces
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface
                    .getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                // For each interface...
                final NetworkInterface netInterface = interfaces.nextElement();
                final Enumeration<InetAddress> addresses = netInterface
                        .getInetAddresses();
                while (addresses.hasMoreElements()) {
                    // For each address of this interface...
                    final InetAddress address = addresses.nextElement();

                    // Prepare a URI
                    for (final int port : pHttpPorts) {
                        try {
                            accesses.add(new URI("http", null, address
                                    .getHostAddress(), port,
                                    JabsorbConstants.HOST_SERVLET_PATH, null,
                                    null).toString());

                        } catch (final URISyntaxException ex) {
                            // Bad URI
                            System.err.println("Bad URI: " + ex);
                        }
                    }
                }
            }

        } catch (final SocketException ex) {
            System.err.println("Can't compute socket accesses");
        }

        // No service available
        return accesses.toArray(new String[0]);
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
        aHttpService.unregister(JabsorbConstants.HOST_SERVLET_PATH);

        // Forget the port
        final int port = extractHttpPort(aProperties);
        if (port > 0) {
            pHttpPorts.remove(port);
        }

        // Let the parent clean up
        super.unbindHttpService(aHttpService);
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

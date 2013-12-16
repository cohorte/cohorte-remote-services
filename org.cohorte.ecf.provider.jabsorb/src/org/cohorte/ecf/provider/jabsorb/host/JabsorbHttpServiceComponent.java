/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.host;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.cohorte.ecf.provider.jabsorb.JabsorbConstants;
import org.cohorte.remote.utilities.BundlesClassLoader;
import org.eclipse.ecf.remoteservice.servlet.HttpServiceComponent;
import org.jabsorb.ng.JSONRPCBridge;
import org.jabsorb.ng.JSONRPCServlet;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * All methods are implemented by HttpServiceComponent
 * 
 * @author Thomas Calmant
 */
public class JabsorbHttpServiceComponent extends HttpServiceComponent {

    /** Registered endpoints */
    private final Set<String> pEndpoints = new LinkedHashSet<String>();

    /** The Jabsorb bridge */
    private JSONRPCBridge pJabsorbRpcBridge;

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

        // Set the serializer class loader
        JSONRPCBridge.getSerializer().setClassLoader(
                new BundlesClassLoader(aCtxt));

        // Let the component be activated
        super.activate(aCtxt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.servlet.HttpServiceComponent#bindHttpService
     * (org.osgi.service.http.HttpService)
     */
    @Override
    protected void bindHttpService(final HttpService aHttpService) {

        // Let the parent to the binding
        super.bindHttpService(aHttpService);

        // Register the Jabsorb servlet
        try {
            aHttpService.registerServlet(JabsorbConstants.HOST_SERVLET_PATH,
                    new JSONRPCServlet(), null, null);

            // FIXME: Set the HTTP session provider
            // HTTPSessionFactory
            // .setHTTPSessionProvider(new JabsorbHttpSessionProvider());

        } catch (ServletException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (NamespaceException ex) {
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

        // Clean up
        pEndpoints.clear();
        JSONRPCBridge.getSerializer().setClassLoader(null);

        // Deactivate the component
        super.deactivate();
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.servlet.HttpServiceComponent#unbindHttpService
     * (org.osgi.service.http.HttpService)
     */
    @Override
    protected void unbindHttpService(final HttpService aHttpService) {

        // Unregister the servlet from the lost service
        aHttpService.unregister(JabsorbConstants.HOST_SERVLET_PATH);

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

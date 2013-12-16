/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.host;

import java.util.Dictionary;

import org.cohorte.ecf.provider.jabsorb.Activator;
import org.cohorte.ecf.provider.jabsorb.Utilities;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.IOSGiRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.servlet.ServletServerContainer;
import org.osgi.framework.ServiceReference;

/**
 * @author Thomas Calmant
 */
public class JabsorbHostContainer extends ServletServerContainer implements
        IOSGiRemoteServiceContainerAdapter {

    /** The Jabsorb bridge */
    private JabsorbHttpServiceComponent pBridge;

    /** The endpoint name */
    private String pEndpointName;

    /** Service reference */
    private ServiceReference<?> pReference;

    /**
     * @param aId
     */
    public JabsorbHostContainer(final ID aId,
            final JabsorbHttpServiceComponent aBridge) {

        super(aId);
        pBridge = aBridge;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.servlet.ServletServerContainer#dispose()
     */
    @Override
    public void dispose() {

        if (pEndpointName != null) {
            pBridge.unregisterEndpoint(pEndpointName);
        }

        if (pReference != null) {
            Activator.getContext().ungetService(pReference);
        }

        // Clean up
        pBridge = null;
        pReference = null;
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.servlet.ServletServerContainer#
     * registerRemoteService(java.lang.String[], java.lang.Object,
     * java.util.Dictionary)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public IRemoteServiceRegistration registerRemoteService(
            final String[] aClazzes, final Object aService,
            final Dictionary aProperties) {

        // Call the parent
        IRemoteServiceRegistration service = super.registerRemoteService(
                aClazzes, aService, aProperties);

        // Compute the endpoint name
        try {
            pEndpointName = Utilities.getEndpointName(aProperties);

        } catch (ECFException ex) {
            throw new IllegalArgumentException(
                    "Can't generate an endpoint name", ex);
        }

        // Register the service to the bridge
        pBridge.registerEndpoint(pEndpointName, aService);

        return service;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IOSGiRemoteServiceContainerAdapter#
     * registerRemoteService(java.lang.String[],
     * org.osgi.framework.ServiceReference, java.util.Dictionary)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public IRemoteServiceRegistration registerRemoteService(
            final String[] aClazzes, final ServiceReference aServiceReference,
            final Dictionary aProperties) {

        // Grab the service
        pReference = aServiceReference;
        Object service = Activator.getContext().getService(pReference);

        // Call the parent
        IRemoteServiceRegistration serviceReg = super.registerRemoteService(
                aClazzes, service, aProperties);

        // Compute the endpoint name
        try {
            pEndpointName = Utilities.getEndpointName(aServiceReference);

        } catch (ECFException ex) {
            throw new IllegalArgumentException(
                    "Can't generate an endpoint name", ex);
        }

        // Register the service to the bridge
        pBridge.registerEndpoint(pEndpointName, service);

        return serviceReg;
    }
}

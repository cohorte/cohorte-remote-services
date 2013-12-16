/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.host;

import java.util.Dictionary;

import org.cohorte.ecf.provider.jabsorb.Utilities;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.servlet.ServletServerContainer;

/**
 * @author Thomas Calmant
 */
public class JabsorbHostContainer extends ServletServerContainer {

    /** The Jabsorb bridge */
    private final JabsorbHttpServiceComponent pBridge;

    /** The endpoint name */
    private String pEndpointName;

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

        // Clean up
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
}

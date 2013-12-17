/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.host;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.cohorte.ecf.provider.jabsorb.Activator;
import org.cohorte.ecf.provider.jabsorb.JabsorbConstants;
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
     * @see org.eclipse.ecf.remoteservice.IOSGiRemoteServiceContainerAdapter#
     * registerRemoteService(java.lang.String[],
     * org.osgi.framework.ServiceReference, java.util.Dictionary)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public IRemoteServiceRegistration registerRemoteService(
            final String[] aClazzes, final ServiceReference aServiceReference,
            final Dictionary aProperties) {

        // Compute the endpoint name
        try {
            pEndpointName = Utilities.getEndpointName(aServiceReference);

        } catch (final ECFException ex) {
            throw new IllegalArgumentException(
                    "Can't generate an endpoint name", ex);
        }

        // Copy properties
        final Properties properties = new Properties();
        final Enumeration keys = aProperties.keys();
        while (keys.hasMoreElements()) {
            final Object key = keys.nextElement();
            properties.put(key, aProperties.get(key));
        }

        // Add Jabsorb provider properties
        // ... endpoint name
        properties.put(JabsorbConstants.PROP_ENDPOINT_NAME, pEndpointName);

        // ... HTTP accesses
        final String[] accesses = pBridge.getAccesses();
        properties.put(JabsorbConstants.PROP_HTTP_ACCESSES, accesses);

        // Grab the service
        pReference = aServiceReference;
        final Object service = Activator.getContext().getService(pReference);

        // Register the service to the bridge
        pBridge.registerEndpoint(pEndpointName, service);

        // Call the parent to make the registration bean
        // ... properties not given to the endpoint description...
        return super.registerRemoteService(aClazzes, service, properties);
    }
}

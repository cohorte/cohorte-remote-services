/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.client;

import org.cohorte.ecf.provider.jabsorb.JabsorbConstants;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceCallPolicy;
import org.eclipse.ecf.remoteservice.client.AbstractClientContainer;
import org.eclipse.ecf.remoteservice.client.IRemoteCallable;
import org.eclipse.ecf.remoteservice.client.IRemoteServiceClientContainerAdapter;
import org.eclipse.ecf.remoteservice.client.RemoteServiceClientRegistration;

/**
 * Jabsorb ECF container
 * 
 * @author Thomas Calmant
 */
public class JabsorbContainer extends AbstractClientContainer implements
        IRemoteServiceClientContainerAdapter {

    /**
     * Sets up the container
     * 
     * @param containerID
     *            The container ID
     */
    public JabsorbContainer(final ID containerID) {

        super(containerID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.client.AbstractClientContainer#
     * createRemoteService
     * (org.eclipse.ecf.remoteservice.client.RemoteServiceClientRegistration)
     */
    @Override
    protected IRemoteService createRemoteService(
            final RemoteServiceClientRegistration aRegistration) {

        try {
            // Prepare a new client service
            return new JabsorbClientService(this, aRegistration);

        } catch (final ECFException ex) {
            // TODO: log exception
            ex.printStackTrace();

            // Return null in case of error
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.IContainer#getConnectNamespace()
     */
    @Override
    public Namespace getConnectNamespace() {

        return IDFactory.getDefault().getNamespaceByName(
                JabsorbConstants.IDENTITY_NAMESPACE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.client.AbstractClientContainer#
     * prepareEndpointAddress(org.eclipse.ecf.remoteservice.IRemoteCall,
     * org.eclipse.ecf.remoteservice.client.IRemoteCallable)
     */
    @Override
    protected String prepareEndpointAddress(final IRemoteCall aCall,
            final IRemoteCallable aCallable) {

        // Not used
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * setRemoteServiceCallPolicy
     * (org.eclipse.ecf.remoteservice.IRemoteServiceCallPolicy)
     */
    @Override
    public boolean setRemoteServiceCallPolicy(
            final IRemoteServiceCallPolicy aPolicy) {

        // No policy handling
        return false;
    }
}

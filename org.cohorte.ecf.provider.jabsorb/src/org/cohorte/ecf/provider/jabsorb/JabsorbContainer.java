/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
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
 * TODO: Create remote service (client)
 * 
 * TODO: Provide services (host)
 * 
 * @author Thomas Calmant
 */
public class JabsorbContainer extends AbstractClientContainer implements
        IRemoteServiceClientContainerAdapter {

    public JabsorbContainer(final ID containerID) {

        super(containerID);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected IRemoteService createRemoteService(
            final RemoteServiceClientRegistration registration) {

        // TODO Auto-generated method stub
        return null;
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

    @Override
    protected String prepareEndpointAddress(final IRemoteCall call,
            final IRemoteCallable callable) {

        // TODO Auto-generated method stub
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
            final IRemoteServiceCallPolicy policy) {

        // No policy handling
        return false;
    }
}

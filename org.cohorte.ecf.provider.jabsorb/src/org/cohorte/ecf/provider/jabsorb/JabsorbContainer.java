/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb;

import java.util.Dictionary;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.remoteservice.IOSGiRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceCallPolicy;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * @author thomas
 * 
 */
public class JabsorbContainer implements IOSGiRemoteServiceContainerAdapter,
        IRemoteServiceContainerAdapter, IContainer {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.IContainer#addListener(org.eclipse.ecf.core.
     * IContainerListener)
     */
    @Override
    public void addListener(final IContainerListener aListener) {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * addRemoteServiceListener
     * (org.eclipse.ecf.remoteservice.IRemoteServiceListener)
     */
    @Override
    public void addRemoteServiceListener(final IRemoteServiceListener aListener) {

        // TODO Auto-generated method stub

    }

    @Override
    @SuppressWarnings("rawtypes")
    public IFuture asyncGetRemoteServiceReferences(final ID aTarget,
            final ID[] aIdFilter, final String aClazz, final String aFilter) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public IFuture asyncGetRemoteServiceReferences(final ID aTarget,
            final String aClazz, final String aFilter) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public IFuture asyncGetRemoteServiceReferences(final ID[] aIdFilter,
            final String aClazz, final String aFilter) {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.core.IContainer#connect(org.eclipse.ecf.core.identity.ID,
     * org.eclipse.ecf.core.security.IConnectContext)
     */
    @Override
    public void connect(final ID aTargetID,
            final IConnectContext aConnectContext)
            throws ContainerConnectException {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * createRemoteFilter(java.lang.String)
     */
    @Override
    public IRemoteFilter createRemoteFilter(final String aFilter)
            throws InvalidSyntaxException {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.IContainer#disconnect()
     */
    @Override
    public void disconnect() {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.IContainer#dispose()
     */
    @Override
    public void dispose() {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(final Class aArg0) {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * getAllRemoteServiceReferences(java.lang.String, java.lang.String)
     */
    @Override
    public IRemoteServiceReference[] getAllRemoteServiceReferences(
            final String aClazz, final String aFilter)
            throws InvalidSyntaxException {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.IContainer#getConnectedID()
     */
    @Override
    public ID getConnectedID() {

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

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.identity.IIdentifiable#getID()
     */
    @Override
    public ID getID() {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#getRemoteService
     * (org.eclipse.ecf.remoteservice.IRemoteServiceReference)
     */
    @Override
    public IRemoteService getRemoteService(
            final IRemoteServiceReference aReference) {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * getRemoteServiceID(org.eclipse.ecf.core.identity.ID, long)
     */
    @Override
    public IRemoteServiceID getRemoteServiceID(final ID aContainerID,
            final long aContainerRelativeID) {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * getRemoteServiceNamespace()
     */
    @Override
    public Namespace getRemoteServiceNamespace() {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * getRemoteServiceReference(org.eclipse.ecf.remoteservice.IRemoteServiceID)
     */
    @Override
    public IRemoteServiceReference getRemoteServiceReference(
            final IRemoteServiceID aServiceID) {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * getRemoteServiceReferences(org.eclipse.ecf.core.identity.ID,
     * org.eclipse.ecf.core.identity.ID[], java.lang.String, java.lang.String)
     */
    @Override
    public IRemoteServiceReference[] getRemoteServiceReferences(
            final ID aTarget, final ID[] aIdFilter, final String aClazz,
            final String aFilter) throws InvalidSyntaxException,
            ContainerConnectException {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * getRemoteServiceReferences(org.eclipse.ecf.core.identity.ID,
     * java.lang.String, java.lang.String)
     */
    @Override
    public IRemoteServiceReference[] getRemoteServiceReferences(
            final ID aTarget, final String aClazz, final String aFilter)
            throws InvalidSyntaxException, ContainerConnectException {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * getRemoteServiceReferences(org.eclipse.ecf.core.identity.ID[],
     * java.lang.String, java.lang.String)
     */
    @Override
    public IRemoteServiceReference[] getRemoteServiceReferences(
            final ID[] aIdFilter, final String aClazz, final String aFilter)
            throws InvalidSyntaxException {

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * registerRemoteService(java.lang.String[], java.lang.Object,
     * java.util.Dictionary)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public IRemoteServiceRegistration registerRemoteService(
            final String[] aClazzes, final Object aService,
            final Dictionary aProperties) {

        // TODO Auto-generated method stub
        return null;
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

        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.IContainer#removeListener(org.eclipse.ecf.core.
     * IContainerListener)
     */
    @Override
    public void removeListener(final IContainerListener aListener) {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * removeRemoteServiceListener
     * (org.eclipse.ecf.remoteservice.IRemoteServiceListener)
     */
    @Override
    public void removeRemoteServiceListener(
            final IRemoteServiceListener aListener) {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * setConnectContextForAuthentication
     * (org.eclipse.ecf.core.security.IConnectContext)
     */
    @Override
    public void setConnectContextForAuthentication(
            final IConnectContext aConnectContext) {

        // TODO Auto-generated method stub

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

        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * ungetRemoteService(org.eclipse.ecf.remoteservice.IRemoteServiceReference)
     */
    @Override
    public boolean ungetRemoteService(final IRemoteServiceReference aReference) {

        // TODO Auto-generated method stub
        return false;
    }

}

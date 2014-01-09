/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.host;

import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IExtendedRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;

/**
 * @author Thomas Calmant
 */
public class ExtendedRemoteServiceRegistration implements
        IExtendedRemoteServiceRegistration {

    /** Endpoint extra properties */
    private final Map<String, Object> pExtraProperties = new LinkedHashMap<String, Object>();

    /** The underlying remote service registration */
    private final IRemoteServiceRegistration pRegistration;

    /**
     * Sets up the extended registration wrapper
     * 
     * @param aRegistration
     *            The underlying remote service registration
     * @param aExtraProperties
     *            The properties to add to the endpoint description
     */
    public ExtendedRemoteServiceRegistration(
            final IRemoteServiceRegistration aRegistration,
            final Map<String, Object> aExtraProperties) {

        pRegistration = aRegistration;
        if (aExtraProperties != null) {
            pExtraProperties.putAll(aExtraProperties);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#getContainerID()
     */
    @Override
    public ID getContainerID() {

        return pRegistration.getContainerID();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IExtendedRemoteServiceRegistration#
     * getExtraProperties()
     */
    @Override
    public Map<String, Object> getExtraProperties() {

        return pExtraProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#getID()
     */
    @Override
    public IRemoteServiceID getID() {

        return pRegistration.getID();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#getProperty(
     * java.lang.String)
     */
    @Override
    public Object getProperty(final String aKey) {

        return pRegistration.getProperty(aKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#getPropertyKeys
     * ()
     */
    @Override
    public String[] getPropertyKeys() {

        return pRegistration.getPropertyKeys();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#getReference()
     */
    @Override
    public IRemoteServiceReference getReference() {

        return pRegistration.getReference();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#setProperties
     * (java.util.Dictionary)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void setProperties(final Dictionary aProperties) {

        pRegistration.setProperties(aProperties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#unregister()
     */
    @Override
    public void unregister() {

        pRegistration.unregister();
    }
}

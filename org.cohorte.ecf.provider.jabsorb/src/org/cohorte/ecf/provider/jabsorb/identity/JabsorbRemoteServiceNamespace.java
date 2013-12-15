/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.identity;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.remoteservice.RemoteServiceID;

/**
 * @author thomas
 * 
 */
public class JabsorbRemoteServiceNamespace extends Namespace {

    /** Name, as in plugin.xml */
    public static final String NAME = "ecf.namespace.jabsorb.remoteservice";

    /** Jabsorb Remote Service Scheme */
    private static final String REMOTE_SERVICE_SCHEME = "jabsorb_rs";

    /** Serial UID */
    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.core.identity.Namespace#createInstance(java.lang.Object
     * [])
     */
    @Override
    public ID createInstance(final Object[] parameters)
            throws IDCreateException {

        // Check parameters
        if (parameters == null || parameters.length != 2) {
            throw new IDCreateException(
                    "Incorrect parameters for Jabsorb RS ID creation");
        }

        // Create the service ID
        // 0: container ID (ID)
        // 1: container relative ID (long)
        return new RemoteServiceID(this, (ID) parameters[0],
                ((Long) parameters[1]).longValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.identity.Namespace#getScheme()
     */
    @Override
    public String getScheme() {

        return REMOTE_SERVICE_SCHEME;
    }
}

/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.identity;

import java.net.URI;
import java.util.Arrays;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;

/**
 * The Jabsorb default transport namespace (jabsorb://).
 * 
 * Based on the R-OSGi implementation.
 * 
 * @author Thomas Calmant
 */
public class JabsorbNamespace extends Namespace {

    /** Namespace scheme */
    public static final String NAMESPACE_SCHEME = "jabsorb";

    /** Serial UID */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new ID within this namespace.
     * 
     * @param parameters
     *            the parameter to pass to the ID.
     * @return the new ID
     * @throws IDCreateException
     *             if the creation fails.
     * @see org.eclipse.ecf.core.identity.Namespace#createInstance(java.lang.Object[])
     */
    @Override
    public ID createInstance(final Object[] parameters)
            throws IDCreateException {

        System.out.println("create install with parameters="
                + Arrays.toString(parameters));

        if (parameters == null || parameters.length != 1) {
            throw new IDCreateException(
                    "Incorrect parameters for Jabsorb ID creation");
        }

        try {
            String uriString = getInitStringFromExternalForm(parameters);
            if (uriString == null) {
                uriString = (String) parameters[0];
            }

            return new JabsorbID(this, URI.create(uriString));

        } catch (Exception e) {
            throw new IDCreateException("Could not create JabsorbID", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.identity.Namespace#getScheme()
     */
    @Override
    public String getScheme() {

        return NAMESPACE_SCHEME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.identity.Namespace#getSupportedParameterTypes()
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Class[][] getSupportedParameterTypes() {

        return new Class[][] { { String.class } };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.identity.Namespace#getSupportedSchemes()
     */
    @Override
    public String[] getSupportedSchemes() {

        return new String[] { NAMESPACE_SCHEME };
    }
}

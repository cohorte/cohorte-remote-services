/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.identity;

import java.net.URISyntaxException;
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

    /** Name, as in plugin.xml */
    public static final String NAME = "ecf.namespace.jabsorb";

    /** Namespace scheme */
    public static final String NAMESPACE_SCHEME = "jabsorb";

    /** Serial UID */
    private static final long serialVersionUID = 1L;

    /** Singleton instance */
    private static Namespace sInstance;

    /**
     * Get the singleton of this namespace
     * 
     * @return The instance
     */
    public static Namespace getDefault() {

        if (sInstance == null) {
            sInstance = new JabsorbNamespace();
        }

        return sInstance;
    }

    /**
     * Singleton constructor
     */
    private JabsorbNamespace() {

        // Set up the namespace
        initialize(NAME, "Jabsorb namespace");
    }

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
            return new JaborbID((String) parameters[0]);

        } catch (URISyntaxException ex) {
            // Problem
            throw new IDCreateException(getName() + " createInstance()", ex);
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
     * @see org.eclipse.ecf.core.identity.Namespace#getSupportedSchemes()
     */
    @Override
    public String[] getSupportedSchemes() {

        return new String[] { NAMESPACE_SCHEME };
    }
}

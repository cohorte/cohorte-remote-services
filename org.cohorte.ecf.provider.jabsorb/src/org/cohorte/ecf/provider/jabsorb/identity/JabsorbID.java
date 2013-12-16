/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.identity;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.URIID;

/**
 * @author Thomas Calmant
 */
public class JabsorbID extends URIID {

    /** Serial UID */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new Jabsorb UID from an URI string
     * 
     * @param aNamespace
     *            The parent namespace
     * @param aUri
     *            The URI of a remote service
     * @throws URISyntaxException
     *             Invalid URI string
     */
    public JabsorbID(final Namespace aNamespace, final URI aUri) {

        super(aNamespace, aUri);
    }

    /**
     * Returns the URI string representation
     */
    @Override
    public String toString() {

        return "JabsorbID[" + getName() + "]";
    }
}

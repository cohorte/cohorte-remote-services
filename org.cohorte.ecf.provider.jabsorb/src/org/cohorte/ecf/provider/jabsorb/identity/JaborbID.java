/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.identity;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.ecf.core.identity.BaseID;

/**
 * @author thomas
 */
public class JaborbID extends BaseID {

    /** Serial UID */
    private static final long serialVersionUID = 1L;

    /** Internal URI */
    private final URI pUri;

    /**
     * Creates a new Jabsorb UID from an URI string
     * 
     * @param aUriString
     *            The URI of a remote service
     * @throws URISyntaxException
     *             Invalid URI string
     */
    public JaborbID(final String aUriString) throws URISyntaxException {

        super(JabsorbNamespace.getDefault());
        pUri = new URI(aUriString);
    }

    /**
     * Returns the internal URI
     * 
     * @return
     */
    public URI getUri() {

        return pUri;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.core.identity.BaseID#namespaceCompareTo(org.eclipse.ecf
     * .core.identity.BaseID)
     */
    @Override
    protected int namespaceCompareTo(final BaseID aId) {

        // Compare by name (URI representation)
        return getName().compareTo(aId.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.core.identity.BaseID#namespaceEquals(org.eclipse.ecf.
     * core.identity.BaseID)
     */
    @Override
    protected boolean namespaceEquals(final BaseID aId) {

        if (aId instanceof JaborbID) {
            // Compare by URI
            JaborbID other = (JaborbID) aId;
            return pUri.equals(other.pUri);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.identity.BaseID#namespaceGetName()
     */
    @Override
    protected String namespaceGetName() {

        return pUri.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.identity.BaseID#namespaceHashCode()
     */
    @Override
    protected int namespaceHashCode() {

        // Return the URI hash code
        return pUri.hashCode();
    }

    /**
     * Returns the ID external form, i.e. the URI string representation
     */
    @Override
    public String toExternalForm() {

        return pUri.toString();
    }

    /**
     * Returns the URI string representation
     */
    @Override
    public String toString() {

        return pUri.toString();
    }
}

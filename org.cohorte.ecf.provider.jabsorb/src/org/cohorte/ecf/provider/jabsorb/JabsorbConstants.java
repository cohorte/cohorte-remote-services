package org.cohorte.ecf.provider.jabsorb;

/**
 * Definition of Jabsorb constants
 * 
 * @author Thomas Calmant
 */
public interface JabsorbConstants {

    /** Host container name */
    String CONTAINER_HOST = "ecf.jabsorb";

    /** Service ID on the remote side */
    String ENDPOINT_SERVICE_ID = "endpoint.service.id ";

    /** Path to the host servlet */
    String HOST_SERVLET_PATH = "/JABSORB-RPC";

    /** Jabsorb identity namespace */
    String IDENTITY_NAMESPACE = "ecf.namespace.jabsorb";

    /** Jabsorb configuration, as in plugin.xml */
    String JABSORB_CONFIG = "ecf.jabsorb";

    /** Array form of the configuration */
    String[] JABSORB_CONFIGS = new String[] { JABSORB_CONFIG };

    /** Name of the endpoint */
    String PROP_ENDPOINT_NAME = JABSORB_CONFIG + ".name";

    /** HTTP accesses (comma-separated URL String) */
    String PROP_HTTP_ACCESSES = JABSORB_CONFIG + ".accesses";
}

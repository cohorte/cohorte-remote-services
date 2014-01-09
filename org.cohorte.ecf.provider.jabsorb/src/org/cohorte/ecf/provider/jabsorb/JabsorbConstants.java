/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cohorte.ecf.provider.jabsorb;

/**
 * Definition of Jabsorb constants
 * 
 * @author Thomas Calmant
 */
public interface JabsorbConstants {

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

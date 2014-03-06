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
package org.cohorte.remote;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents an end point to access an imported service, using Pelix model
 * 
 * @author Thomas Calmant
 */
public class ImportEndpoint {

    /** Export configuration */
    private final String[] pConfigurations;

    /** Host framework UID */
    private final String pFrameworkUid;

    /** Endpoint name */
    private final String pName;

    /** Service properties */
    private final Map<String, Object> pProperties = new LinkedHashMap<String, Object>();

    /** Source server address, set by the discovery service */
    private String pServer;

    /** Service specifications */
    private final String[] pSpecifications;

    /** Endpoint UID */
    private final String pUid;

    /**
     * Sets up the bean
     * 
     * @param aUid
     *            Endpoint UID
     * @param aFramework
     *            Endpoint host UID
     * @param aConfigurations
     *            Kinds of the endpoint
     * @param aName
     *            Endpoint name
     * @param aSpecifications
     *            Specifications of the exported service
     * @param aProperties
     *            Exported service properties
     */
    public ImportEndpoint(final String aUid, final String aFramework,
            final String[] aConfigurations, final String aName,
            final String[] aSpecifications,
            final Map<String, Object> aProperties) {

        pUid = aUid;
        pFrameworkUid = aFramework;
        pName = aName;
        pConfigurations = Arrays
                .copyOf(aConfigurations, aConfigurations.length);
        pSpecifications = EndpointUtils.extractSpecifications(aSpecifications);
        if (aProperties != null) {
            pProperties.putAll(aProperties);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        // Comparison based on UID
        if (aObj instanceof ImportEndpoint) {
            return pUid.equals(((ImportEndpoint) aObj).pUid);
        }

        return false;
    }

    /**
     * @return the configurations
     */
    public String[] getConfigurations() {

        return Arrays.copyOf(pConfigurations, pConfigurations.length);
    }

    /**
     * @return the frameworkUid
     */
    public String getFrameworkUid() {

        return pFrameworkUid;
    }

    /**
     * @return the name
     */
    public String getName() {

        return pName;
    }

    /**
     * @return the properties
     */
    public synchronized Map<String, Object> getProperties() {

        return new HashMap<String, Object>(pProperties);
    }

    /**
     * @return the server
     */
    public String getServer() {

        return pServer;
    }

    /**
     * @return the specifications
     */
    public String[] getSpecifications() {

        return Arrays.copyOf(pSpecifications, pSpecifications.length);
    }

    /**
     * @return the uid
     */
    public String getUid() {

        return pUid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        // Use UID string hash code
        return pUid.hashCode();
    }

    /**
     * Updates endpoint properties
     * 
     * @param aProperties
     */
    public synchronized void setProperties(final Map<String, Object> aProperties) {

        pProperties.clear();
        if (aProperties != null) {
            pProperties.putAll(aProperties);
        }
    }

    /**
     * Lets a discovery provider set the address from which it heard about this
     * endpoint
     * 
     * @param aServer
     *            the server to set
     */
    public void setServer(final String aServer) {

        if (aServer.contains(":") && !aServer.startsWith("[")) {
            // IPv6 without square brackets: protect it
            pServer = "[" + aServer + "]";
        } else {
            // Direct copy
            pServer = aServer;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "ImportEndpoint(uid=" + pUid + ", framework=" + pFrameworkUid
                + ", configurations=" + Arrays.toString(pConfigurations)
                + ", specs=" + Arrays.toString(pSpecifications) + ")";
    }
}

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
package org.cohorte.remote.beans;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Description of an end point description
 * 
 * @author Thomas Calmant
 */
public class EndpointDescription implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** End point name, as in service properties (can be null) */
    private String pEndpointName;

    /** End point UID (mandatory) */
    private String pEndpointUid;

    /** Associated "service.exported.configs" value */
    private String[] pExportedConfigs;

    /**
     * Default constructor
     */
    public EndpointDescription() {

        // Do nothing
    }

    /**
     * Sets up the end point description
     * 
     * @param aEndpointUid
     *            End point UID (mandatory and unique)
     * @param aEndpointName
     *            End point name, as in service properties (can be null)
     * @param aExportedConfigs
     *            Exported configurations
     */
    public EndpointDescription(final String aEndpointUid,
            final String aEndpointName, final String[] aExportedConfigs) {

        pEndpointUid = aEndpointUid;
        pEndpointName = aEndpointName;

        setExportedConfigs(aExportedConfigs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        if (!(aObj instanceof EndpointDescription)) {
            return false;
        }

        final EndpointDescription other = (EndpointDescription) aObj;

        // Equality by UID
        return pEndpointUid.equals(other.pEndpointUid);
    }

    /**
     * Retrieves the end point name, as in service properties (can be null)
     * 
     * @return the end point name
     */
    public String getEndpointName() {

        return pEndpointName;
    }

    /**
     * Retrieves the end point UID, unique and mandatory
     * 
     * @return the endpoint UID
     */
    public String getEndpointUid() {

        return pEndpointUid;
    }

    /**
     * Retrieves the associated "service.exported.configs" value
     * 
     * @return the associated "service.exported.configs" value
     */
    public String[] getExportedConfigs() {

        return pExportedConfigs;
    }

    /**
     * Sets the name of the end point
     * 
     * @param aEndpointName
     *            the name of the end point
     */
    public void setEndpointName(final String aEndpointName) {

        pEndpointName = aEndpointName;
    }

    /**
     * Sets the end point UID
     * 
     * @param aEndpointUid
     *            the UID of the end point
     */
    public void setEndpointUid(final String aEndpointUid) {

        pEndpointUid = aEndpointUid;
    }

    /**
     * Sets the exported configurations
     * 
     * @param aExportedConfigs
     *            the exported configurations to set
     */
    public void setExportedConfigs(final String[] aExportedConfigs) {

        // Make a copy of the array, to avoid the caller to mess with it
        final int nbConfigs = aExportedConfigs.length;
        pExportedConfigs = new String[nbConfigs];
        System.arraycopy(aExportedConfigs, 0, pExportedConfigs, 0, nbConfigs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("EndpointDescription(");
        builder.append("uid=").append(pEndpointUid);
        builder.append(", name=").append(pEndpointName);
        builder.append(", configs=").append(Arrays.toString(pExportedConfigs));
        builder.append(")");

        return builder.toString();
    }
}

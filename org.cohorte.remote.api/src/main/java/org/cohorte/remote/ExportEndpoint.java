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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/**
 * Represents an export end point (one per group of configuration types), using
 * Pelix model
 * 
 * @author Thomas Calmant
 */
public class ExportEndpoint {

    /** Export configuration */
    private final String[] pConfigurations;

    /** Exported specifications */
    private String[] pExportedSpecs;

    /** Host framework UID */
    private final String pFrameworkUid;

    /** The exported service */
    private final Object pInstance;

    /** Endpoint name */
    private String pName;

    /** Service properties */
    private final Map<String, Object> pProperties = new LinkedHashMap<String, Object>();

    /** Service reference */
    private final ServiceReference<?> pReference;

    /** Endpoint UID */
    private final String pUid;

    /**
     * Sets up members
     * 
     * @param aUid
     *            Endpoint UID
     * @param aFrameworkUid
     *            Host framework UID
     * @param aConfigurations
     *            Kinds of endpoint (xmlrpc, ...)
     * @param aName
     *            Name of the endpoint
     * @param aServiceReference
     *            {@link ServiceReference} of the exported service
     * @param aService
     *            The exported service
     * @param aProperties
     *            Extra properties
     * @throws IllegalArgumentException
     *             Empty UID or no interface exported
     */
    public ExportEndpoint(final String aUid, final String aFrameworkUid,
            final String[] aConfigurations, final String aName,
            final ServiceReference<?> aServiceReference, final Object aService,
            final Map<String, Object> aProperties) {

        if (aUid == null || aUid.isEmpty()) {
            throw new IllegalArgumentException("Empty ExportEndpoint UID");
        }

        // Copy given information
        pUid = aUid;
        pFrameworkUid = aFrameworkUid;
        pName = aName;
        pReference = aServiceReference;
        pInstance = aService;

        // Copy configurations in a new array
        pConfigurations = Arrays
                .copyOf(aConfigurations, aConfigurations.length);

        // Copy properties
        if (aProperties != null) {
            pProperties.putAll(aProperties);
        }

        // Compute exported specifications
        computeSpecifications();
        if (pExportedSpecs.length == 0) {
            throw new IllegalArgumentException(
                    "Service without exported specifications");
        }
    }

    /**
     * Computes the list of exported specifications
     */
    private void computeSpecifications() {

        // Service properties
        final Collection<String> specs = new HashSet<String>(
                Arrays.asList((String[]) pReference
                        .getProperty(Constants.OBJECTCLASS)));
        Object rawExportedSpecs = pReference
                .getProperty(Constants.SERVICE_EXPORTED_INTERFACES);

        // Result list
        final Collection<String> filteredSpecs = new LinkedList<String>();

        if (rawExportedSpecs instanceof String) {
            if ("*".equals(rawExportedSpecs)) {
                // Export all specifications
                filteredSpecs.addAll(specs);

            } else if (specs.contains(rawExportedSpecs)) {
                // Only one specification is exported
                filteredSpecs.add(rawExportedSpecs.toString());

            } else {
                // Nothing to export
                pExportedSpecs = new String[0];
                return;
            }

        } else if (rawExportedSpecs instanceof String[]) {
            // Convert the array to a collection
            rawExportedSpecs = Arrays.asList((String[]) rawExportedSpecs);
        }

        if (rawExportedSpecs instanceof Collection) {
            @SuppressWarnings("unchecked")
            final Collection<String> exportedSpecs = (Collection<String>) rawExportedSpecs;
            for (final String exportedSpec : exportedSpecs) {
                if (specs.contains(exportedSpec)) {
                    exportedSpecs.add(exportedSpec);
                }
            }
        }

        // Format specifications strings
        final Collection<String> formattedSpecs = EndpointUtils
                .formatSpecifications(filteredSpecs);

        // Store the result
        pExportedSpecs = formattedSpecs
                .toArray(new String[filteredSpecs.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object aObj) {

        // Comparison based on UID
        if (aObj instanceof ExportEndpoint) {
            return pUid.equals(((ExportEndpoint) aObj).pUid);
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
     * @return the exportedSpecs
     */
    public String[] getExportedSpecs() {

        return Arrays.copyOf(pExportedSpecs, pExportedSpecs.length);
    }

    /**
     * @return the frameworkUid
     */
    public String getFrameworkUid() {

        return pFrameworkUid;
    }

    /**
     * Returns the exported service
     * 
     * @return the exported service
     */
    Object getInstance() {

        return pInstance;
    }

    /**
     * @return the name
     */
    public String getName() {

        return pName;
    }

    /**
     * Returns merged properties
     * 
     * @return the service properties merged with extra ones
     */
    public Map<String, Object> getProperties() {

        final Map<String, Object> properties = new LinkedHashMap<String, Object>();

        // Get service properties
        for (final String key : pReference.getPropertyKeys()) {
            properties.put(key, pReference.getProperty(key));
        }

        // Merge with local properties
        properties.putAll(pProperties);

        // Some properties must not be merged
        properties.put(Constants.OBJECTCLASS,
                pReference.getProperty(Constants.OBJECTCLASS));
        properties.put(Constants.SERVICE_ID,
                pReference.getProperty(Constants.SERVICE_ID));

        return properties;
    }

    /**
     * @return the reference
     */
    public ServiceReference<?> getReference() {

        return pReference;
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
     * Sets the new name of the endpoint
     * 
     * @param aNewName
     *            The new endpoint name
     */
    public void setName(final String aNewName) {

        pName = aNewName;
    }

    /**
     * Converts this bean into a map, as used by the Pelix dispatcher servlet
     * 
     * @return A map describing this endpoint
     */
    public Map<String, Object> toMap() {

        final Map<String, Object> result = new LinkedHashMap<String, Object>();

        // Simple attributes
        result.put("uid", pUid);
        result.put("sender", pFrameworkUid);
        result.put("name", pName);

        // Use getters to get valid values
        result.put("configurations", getConfigurations());
        result.put("specifications", getExportedSpecs());
        result.put("properties", getProperties());

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "ExportEndpoint(uid=" + pUid + ", types=" + pConfigurations
                + ", specs=" + Arrays.toString(pConfigurations) + ")";
    }
}

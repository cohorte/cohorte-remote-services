/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb;

import java.util.Dictionary;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.client.RemoteServiceClientRegistration;

/**
 * @author Thomas Calmant
 */
public class Utilities {

    /**
     * Generates an endpoint name according to the properties in the given
     * dictionary
     * 
     * @param aDictionary
     *            Properties of a service
     * @return The endpoint name
     * @throws ECFException
     *             Values used to generate a name are null or invalid
     */
    @SuppressWarnings("rawtypes")
    public static String getEndpointName(final Dictionary aDictionary)
            throws ECFException {

        return getEndpointName(
                (String) aDictionary.get(JabsorbConstants.ENDPOINT_NAME),
                (Long) aDictionary.get(JabsorbConstants.ENDPOINT_SERVICE_ID));
    }

    /**
     * Generates an endpoint name according to the properties from the given
     * registration bean
     * 
     * @param aRegistration
     *            Remote Service registration bean
     * @return The endpoint name
     * @throws ECFException
     *             Values used to generate a name are null or invalid
     */
    public static String getEndpointName(
            final RemoteServiceClientRegistration aRegistration)
            throws ECFException {

        return getEndpointName(
                (String) aRegistration
                        .getProperty(JabsorbConstants.ENDPOINT_NAME),
                (Long) aRegistration
                        .getProperty(JabsorbConstants.ENDPOINT_SERVICE_ID));
    }

    /**
     * Generates an endpoint name from the given property values
     * 
     * @param aEndpointName
     *            Endpoint name property value
     * @param aServiceId
     *            Remote serivce ID property value
     * @return The endpoint name (given or generated)
     * @throws ECFException
     *             Both given values are null or incorrect
     */
    public static String getEndpointName(final String aEndpointName,
            final Long aServiceId) throws ECFException {

        if (aEndpointName != null && !aEndpointName.isEmpty()) {
            // Valid endpoint name
            return aEndpointName;
        }

        if (aServiceId != null && aServiceId != 0) {
            return "service_" + aServiceId;
        }

        throw new ECFException("No endpoint name to generate");
    }

    /**
     * Hidden constructor
     */
    private Utilities() {

    }
}

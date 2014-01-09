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

import java.util.Dictionary;
import java.util.regex.Pattern;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.remoteservice.client.RemoteServiceClientRegistration;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/**
 * Utility methods for host and client containers
 * 
 * @author Thomas Calmant
 */
public class Utilities {

    /** Access URIs separator */
    public static final String URI_SEPARATOR = ",";

    /**
     * Transforms the given string of URIs into a list of URIs
     * 
     * @param aAccessProperty
     *            A string made with {@link #makeAccesses(String[])}
     * @return An array of URIs (can be empty)
     */
    public static String[] getAccesses(final String aAccessProperty) {

        return aAccessProperty.split(Pattern.quote(URI_SEPARATOR));
    }

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
                (String) aDictionary.get(JabsorbConstants.PROP_ENDPOINT_NAME),
                (Long) aDictionary.get(JabsorbConstants.ENDPOINT_SERVICE_ID),
                (Long) aDictionary.get(Constants.SERVICE_ID));
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
                        .getProperty(JabsorbConstants.PROP_ENDPOINT_NAME),
                (Long) aRegistration
                        .getProperty(JabsorbConstants.ENDPOINT_SERVICE_ID),
                null);
    }

    /**
     * Generates an endpoint name according to the properties from the given
     * service reference
     * 
     * @param aServiceReference
     *            A service reference
     * @return The endpoint name
     * @throws ECFException
     *             Values used to generate a name are null or invalid
     */
    public static String getEndpointName(
            final ServiceReference<?> aServiceReference) throws ECFException {

        return getEndpointName(
                (String) aServiceReference
                        .getProperty(JabsorbConstants.PROP_ENDPOINT_NAME),
                (Long) aServiceReference
                        .getProperty(JabsorbConstants.ENDPOINT_SERVICE_ID),
                (Long) aServiceReference.getProperty(Constants.SERVICE_ID));
    }

    /**
     * Generates an endpoint name from the given property values
     * 
     * @param aEndpointName
     *            Endpoint name property value
     * @param aRemoteId
     *            Remote service ID property value (for consumer)
     * @param aServiceId
     *            Local service ID (for host)
     * @return The endpoint name (given or generated)
     * @throws ECFException
     *             Both given values are null or incorrect
     */
    public static String getEndpointName(final String aEndpointName,
            final Long aRemoteId, final Long aServiceId) throws ECFException {

        if (aEndpointName != null && !aEndpointName.isEmpty()) {
            // Valid endpoint name
            return aEndpointName;

        } else if (aRemoteId != null && aRemoteId != 0) {
            return "service_" + aRemoteId;

        } else if (aServiceId != null && aServiceId != 0) {
            return "service_" + aServiceId;
        }

        throw new ECFException("No endpoint name to generate");
    }

    /**
     * Logs (and traces) a message
     * 
     * @param aLevel
     *            Log level
     * @param aMethodName
     *            Calling method name
     * @param aClass
     *            Class of the calling method
     * @param aMessage
     *            Message to log
     */
    public static void log(final int aLevel, final String aMethodName,
            final Class<?> aClass, final String aMessage) {

        log(aLevel, aMessage, aClass, aMessage, null);
    }

    /**
     * Logs (and traces) a message and its error
     * 
     * @param aLevel
     *            Log level
     * @param aMethodName
     *            Calling method name
     * @param aClass
     *            Class of the calling method
     * @param aMessage
     *            Message to log
     * @param aThrowable
     *            An exception
     */
    public static void log(final int aLevel, final String aMethodName,
            final Class<?> aClass, final String aMessage,
            final Throwable aThrowable) {

        // Trace the message
        if (aThrowable != null) {
            traceDebug(aMethodName, aClass, aMessage + ": " + aThrowable);

        } else {
            traceDebug(aMethodName, aClass, aMessage);
        }

        // Forge a log message
        final StringBuilder logMessage = new StringBuilder();
        if (aClass != null) {
            // Set the class name
            logMessage.append(aClass.getName()).append(".");
        }

        // Set the method name and the message
        logMessage.append(aMethodName).append("(): ").append(aMessage);

        // Log it
        Activator.get().log(aLevel, aMessage, aThrowable);
    }

    /**
     * Prepares a string containing all access URIs, separated by
     * {@link #URI_SEPARATOR}
     * 
     * @param aAccessURIs
     *            A list of access URIs
     * @return A string (can be empty)
     */
    public static String makeAccesses(final String[] aAccessURIs) {

        final StringBuilder builder = new StringBuilder();
        for (final String uri : aAccessURIs) {
            // Separate URIs with a ','
            builder.append(uri).append(',');
        }

        // Remove the trailing ','
        if (builder.length() != 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    /**
     * Traces a message using the ECF tracing API
     * 
     * @param aMethodName
     *            Tracing method
     * @param aDebugOption
     *            Eclipse tracing flag name (e.g. ecf.jabsorb/debug)
     * @param aClass
     *            Class of the tracing method
     * @param aMessage
     *            Message to trace
     */
    public static void trace(final String aMethodName,
            final String aDebugOption, final Class<?> aClass,
            final String aMessage) {

        Trace.trace(Activator.PLUGIN_ID, aDebugOption, aClass, aMethodName,
                aMessage);
    }

    /**
     * Traces a message using the ECF tracing API
     * 
     * @param aMethodName
     *            Tracing method
     * @param aClass
     *            Class of the tracing method
     * @param aMessage
     *            Message to trace
     */
    public static void traceDebug(final String aMethodName,
            final Class<?> aClass, final String aMessage) {

        Trace.trace(Activator.PLUGIN_ID, "debug", aClass, aMethodName, aMessage);
    }

    /**
     * Hidden constructor
     */
    private Utilities() {

    }
}

/**
 * Copyright 2013 isandlaTech
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
package org.cohorte.remote.multicast.beans;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.cohorte.remote.pelix.ImportEndpoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an end point description as returned by the Pelix Remote Services
 * dispatcher servlet.
 * 
 * @author Thomas Calmant
 */
public class PelixEndpointDescription {

    /** The configurations exporting the end point */
    private final String[] pConfigurations;

    /** The name of the end point */
    private final String pName;

    /** The end point properties */
    private final Map<String, Object> pProperties;

    /** The UID of the isolate sending the description */
    private final String pSender;

    /** Server address, used as a format entry in the access URL */
    private String pServerAddress;

    /** The specifications of the end point */
    private final String[] pSpecifications;

    /** The UID of the end point */
    private final String pUID;

    /**
     * Parses the given JSON object to construct the bean
     * 
     * @param aJsonObject
     *            A JSON representation of the end point
     * @throws JSONException
     *             Error parsing the JSON object
     */
    public PelixEndpointDescription(final JSONObject aJsonObject)
            throws JSONException {

        // Basic values
        pSender = aJsonObject.getString("sender");
        pUID = aJsonObject.getString("uid");
        pName = aJsonObject.getString("name");

        // Properties
        pProperties = ParseUtils.jsonToMap(aJsonObject
                .getJSONObject("properties"));

        // Configurations
        pConfigurations = extractStrings(aJsonObject
                .getJSONArray("configurations"));

        // Specifications
        pSpecifications = extractStrings(aJsonObject
                .getJSONArray("specifications"));
    }

    /**
     * Extracts the strings from the given JSON array and returns them as an
     * array of strings
     * 
     * @param aJsonArray
     *            A JSON array
     * @return A String array
     * @throws JSONException
     *             Error parsing the array
     */
    private String[] extractStrings(final JSONArray aJsonArray)
            throws JSONException {

        final Collection<String> tempSet = new LinkedList<String>();
        for (final Object item : ParseUtils.jsonToList(aJsonArray)) {
            if (item instanceof CharSequence) {
                tempSet.add(item.toString());
            }
        }

        return tempSet.toArray(new String[tempSet.size()]);
    }

    /**
     * @return the configurations
     */
    public String[] getConfigurations() {

        return Arrays.copyOf(pConfigurations, pConfigurations.length);
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
    public Map<String, Object> getProperties() {

        return pProperties;
    }

    /**
     * @return the sender
     */
    public String getSender() {

        return pSender;
    }

    /**
     * @return the serverAddress
     */
    public String getServerAddress() {

        return pServerAddress;
    }

    /**
     * @return the specifications
     */
    public String[] getSpecifications() {

        return Arrays.copyOf(pSpecifications, pConfigurations.length);
    }

    /**
     * @return the uID
     */
    public String getUID() {

        return pUID;
    }

    /**
     * @param aServerAddress
     *            the serverAddress to set
     */
    public void setServerAddress(final String aServerAddress) {

        pServerAddress = aServerAddress;
    }

    /**
     * Converts this bean into an ImportEndpoint
     * 
     * @return An ImportEndpoint bean
     */
    public ImportEndpoint toImportEndpoint() {

        final ImportEndpoint endpoint = new ImportEndpoint(pUID, pSender,
                pConfigurations, pName, pSpecifications, pProperties);
        endpoint.setServer(pServerAddress);
        return endpoint;
    }
}

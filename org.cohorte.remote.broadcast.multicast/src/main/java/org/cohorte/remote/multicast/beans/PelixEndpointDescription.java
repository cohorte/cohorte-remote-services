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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cohorte.remote.beans.EndpointDescription;
import org.cohorte.remote.beans.RemoteServiceRegistration;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an end point description as returned by the Pelix Remote Services
 * dispatcher servlet.
 * 
 * @author Thomas Calmant
 */
public class PelixEndpointDescription {

    /** The kind of end point */
    private final String pKind;

    /** The name of the end point */
    private final String pName;

    /** The end point properties */
    private final Map<String, Object> pProperties;

    /** The UID of the isolate sending the description */
    private final String pSender;

    private String pServerAddress;

    /** The specifications of the end point */
    private final List<String> pSpecifications = new LinkedList<String>();

    /** The UID of the end point */
    private final String pUID;

    /** The URL to access the end point */
    private final String pURL;

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
        pKind = aJsonObject.getString("kind");
        pName = aJsonObject.getString("name");
        pURL = aJsonObject.getString("url");

        // Properties
        pProperties = ParseUtils.jsonToMap(aJsonObject
                .getJSONObject("properties"));

        // Specifications
        for (final Object item : ParseUtils.jsonToList(aJsonObject
                .getJSONArray("specifications"))) {
            if (item instanceof CharSequence) {
                pSpecifications.add(item.toString());
            }
        }
    }

    /**
     * @return the kind
     */
    public String getKind() {

        return pKind;
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
    public List<String> getSpecifications() {

        return pSpecifications;
    }

    /**
     * @return the uID
     */
    public String getUID() {

        return pUID;
    }

    /**
     * @return the url
     */
    public String getURL() {

        if (pServerAddress != null && !pServerAddress.isEmpty()) {
            // Replace the "server" variable by the known address
            return pURL.replace("{server}", pServerAddress);
        }

        // Return the untouched URL
        return pURL;
    }

    /**
     * @param aServerAddress
     *            the serverAddress to set
     */
    public void setServerAddress(final String aServerAddress) {

        pServerAddress = aServerAddress;
    }

    /**
     * Converts this bean into a remote service registration
     * 
     * @return A RemoteServiceRegistration bean
     * @throws MalformedURLException
     *             Invalid end point access URL
     */
    public RemoteServiceRegistration toRegistration()
            throws MalformedURLException {

        // Parse the access URL
        final URL url = new URL(getURL());

        // Make an end point bean
        final EndpointDescription endpoint = new EndpointDescription(pKind,
                pName, url.getProtocol(), url.getPath(), url.getPort());
        endpoint.resolveHost(pServerAddress);

        // Make a registration bean
        return new RemoteServiceRegistration(pSender, pSpecifications,
                pProperties, Arrays.asList(endpoint));
    }
}

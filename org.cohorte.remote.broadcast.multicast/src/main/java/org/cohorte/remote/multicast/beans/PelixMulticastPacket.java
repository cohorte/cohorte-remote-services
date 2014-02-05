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

import java.util.Map;

import org.cohorte.remote.dispatcher.beans.ParseUtils;
import org.cohorte.remote.multicast.IPacketConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the content of an end point event packet sent over multicast by
 * Pelix remote services.
 * 
 * @author Thomas Calmant
 */
public class PelixMulticastPacket {

    /** Access to the dispatcher servlet */
    private final String pAccessPath;

    /** Port to access the dispatcher servlet */
    private final int pAccessPort;

    /** Kind of event */
    private final String pEvent;

    /** New properties (in case of an update event) */
    private final Map<String, Object> pNewProperties;

    /** Raw JSON object */
    private final JSONObject pRaw;

    /** Sender UID */
    private final String pSender;

    /** Endpoint UID */
    private final String pUID;

    /** Endpoints UIDs */
    private final String[] pUIDs;

    /**
     * Constructs the end point according to the JSON object
     * 
     * @param aJsonObject
     *            A JSON object
     * @throws JSONException
     *             Error parsing the JSON object
     */
    public PelixMulticastPacket(final JSONObject aJsonObject)
            throws JSONException {

        // Keep a reference to the raw object
        pRaw = aJsonObject;

        // Basic
        pSender = aJsonObject.getString(IPacketConstants.KEY_SENDER);
        pEvent = aJsonObject.getString(IPacketConstants.KEY_EVENT);

        // The end point UID is optional (absent of discovery/discovered events)
        pUID = aJsonObject.optString(IPacketConstants.KEY_ENDPOINT_UID);

        // There can be multiple UIDs (add)
        final JSONArray rawUIDs = aJsonObject
                .optJSONArray(IPacketConstants.KEY_ENDPOINT_UIDS);
        if (rawUIDs != null) {
            pUIDs = new String[rawUIDs.length()];
            for (int i = 0; i < rawUIDs.length(); i++) {
                pUIDs[i] = (String) rawUIDs.get(i);
            }

        } else {
            // No array of UIDs
            pUIDs = null;
        }

        // Access
        final JSONObject access = aJsonObject
                .getJSONObject(IPacketConstants.KEY_ACCESS);
        pAccessPath = access.getString(IPacketConstants.KEY_ACCESS_PATH);
        pAccessPort = access.getInt(IPacketConstants.KEY_ACCESS_PORT);

        // Extra properties
        pNewProperties = ParseUtils.jsonToMap(aJsonObject
                .optJSONObject(IPacketConstants.KEY_ENDPOINT_NEW_PROPERTIES));
    }

    /**
     * @return the accessPath
     */
    public String getAccessPath() {

        return pAccessPath;
    }

    /**
     * @return the accessPort
     */
    public int getAccessPort() {

        return pAccessPort;
    }

    /**
     * @return the event
     */
    public String getEvent() {

        return pEvent;
    }

    /**
     * @return the new end point properties (in case of update)
     */
    public Map<String, Object> getNewProperties() {

        return pNewProperties;
    }

    /**
     * @return the raw JSON object
     */
    public JSONObject getRaw() {

        return pRaw;
    }

    /**
     * @return the sender
     */
    public String getSender() {

        return pSender;
    }

    /**
     * @return the end point UID
     */
    public String getUID() {

        return pUID;
    }

    /**
     * @return the endpoints UIDs
     */
    public String[] getUIDs() {

        if (pUIDs == null) {
            return null;
        }

        // Return a copy of the array
        final String[] copy = new String[pUIDs.length];
        System.arraycopy(pUIDs, 0, copy, 0, pUIDs.length);
        return copy;
    }

    /**
     * Tests if this packet comes from the given sender
     * 
     * @param aSender
     *            The UID of a sender
     * @return True if this packet comes from sender
     */
    public boolean isFromSender(final String aSender) {

        return pSender.equals(aSender);
    }
}

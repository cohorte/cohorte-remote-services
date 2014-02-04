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
package org.cohorte.remote.multicast;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cohorte.remote.IRemoteServiceRepository;
import org.cohorte.remote.beans.EndpointDescription;
import org.cohorte.remote.beans.RemoteServiceRegistration;
import org.cohorte.remote.multicast.beans.PelixEndpointDescription;
import org.cohorte.remote.utilities.RSUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;

/**
 * The servlet that publishes the content of the exported services registry
 * 
 * @author Thomas Calmant
 */
class RegistryServlet extends HttpServlet {

    /** JSON mime-type */
    private static final String JSON_TYPE = "application/json";

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The parent multicast broadcaster */
    private final MulticastBroadcaster pBroadcaster;

    /** The isolate UID */
    private final String pIsolateUID;

    /** The exported services repository */
    private final IRemoteServiceRepository pRepository;

    /**
     * Sets up members
     */
    public RegistryServlet(final String aIsolateUID,
            final MulticastBroadcaster aBroadcaster,
            final IRemoteServiceRepository aRepository) {

        pIsolateUID = aIsolateUID;
        pBroadcaster = aBroadcaster;
        pRepository = aRepository;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(final HttpServletRequest aReq,
            final HttpServletResponse aResp) throws ServletException,
            IOException {

        // Get the path given after the servlet path
        final String extra = aReq.getPathInfo();
        if (extra == null) {
            // No order given
            aResp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request path");
            return;
        }

        // Split it (extra will start with a '/')
        final String[] parts = extra.substring(1).split("/");
        if (parts[0].equals("endpoint")) {
            // /endpoint/<uid>
            if (parts.length < 2) {
                // Missing the UID
                aResp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Endpoint UID is missing");
                return;

            } else {
                // Send the response
                sendEndpointDict(aResp, parts[1]);
            }

        } else if (parts[0].equals("endpoints")) {
            // /endpoints
            sendEndpoints(aResp);

        } else {
            // Unknown path
            aResp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException,
            IOException {

        // Get the body of the request
        final byte[] rawContent = RSUtils.inputStreamToBytes(req
                .getInputStream());
        final String strContent = new String(rawContent);

        try {
            // Parse it
            final JSONArray jsonEndpoints = new JSONArray(strContent);

            // Prepare the list of end points
            final String senderAddr = req.getRemoteAddr();
            final List<PelixEndpointDescription> endpoints = new ArrayList<>(
                    jsonEndpoints.length());
            for (int i = 0; i < jsonEndpoints.length(); i++) {
                final JSONObject endpoint = jsonEndpoints.getJSONObject(i);
                System.out.println("ENDPOINT:\n" + endpoint);

                final PelixEndpointDescription converted = new PelixEndpointDescription(
                        endpoint);
                converted.setServerAddress(senderAddr);
                System.out.println("Converted: " + converted);
                endpoints.add(converted);
            }

            // Let them be registered
            pBroadcaster.handleDiscovered(endpoints);

            // Success
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setHeader("Content-Type", "text/plain");
            resp.getWriter().print("OK");

        } catch (final JSONException ex) {
            // Error
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setHeader("Content-Type", "text/plain");
            ex.printStackTrace(resp.getWriter());
        }
    }

    /**
     * Retrieves the exported end points in a JSON array
     * 
     * @return a JSON array
     */
    protected JSONArray getJsonEndpoints() {

        // Get our registrations
        final RemoteServiceRegistration[] regBeans = pRepository
                .getLocalRegistrations();

        // Convert the objects to maps
        final List<Object> regMaps = new LinkedList<Object>();
        for (final RemoteServiceRegistration registration : regBeans) {

            final Map<String, Object> regMap = registrationToMap(registration);
            if (regMap != null) {
                // Avoid registrations without end point
                regMaps.add(regMap);
            }
        }

        // Convert to JSON
        return new JSONArray(regMaps);
    }

    /**
     * Converts a Cohorte Remote Services registration bean to a Pelix remote
     * services end point. Returns null if no end point is stored in the
     * registration bean.
     * 
     * @param aRegistration
     *            A remote service registration
     * @return The corresponding Pelix representation, or null
     */
    private Map<String, Object> registrationToMap(
            final RemoteServiceRegistration aRegistration) {

        final EndpointDescription[] regEndpoints = aRegistration.getEndpoints();
        if (regEndpoints == null || regEndpoints.length == 0) {
            // No end points
            return null;
        }

        // Find a JSON-RPC end point
        EndpointDescription foundEndpoint = null;
        for (final EndpointDescription endpoint : regEndpoints) {
            for (final String exportedConfig : endpoint.getExportedConfigs()) {
                if (exportedConfig.contains("json")) {
                    foundEndpoint = endpoint;
                    break;
                }
            }
        }

        if (foundEndpoint == null) {
            // No JSON end point, try the first one
            foundEndpoint = regEndpoints[0];
        }

        // Filter the properties (remove the specifications)
        final Map<String, Object> properties = new LinkedHashMap<String, Object>(
                aRegistration.getServiceProperties());
        properties.remove(Constants.OBJECTCLASS);

        // Prepare the end point map
        final Map<String, Object> endpoint = new LinkedHashMap<String, Object>();

        // Found in the registration...
        endpoint.put("sender", pIsolateUID);
        endpoint.put("uid", aRegistration.getServiceId());
        endpoint.put("specifications", aRegistration.getExportedInterfaces());
        endpoint.put("properties", properties);

        // Found in the end point
        endpoint.put("configurations", foundEndpoint.getExportedConfigs());
        endpoint.put("name", foundEndpoint.getEndpointName());
        return endpoint;
    }

    /**
     * Sends the representation of the end point matching the given ID
     * 
     * @param aResp
     *            Servlet response
     * @param aRegistrationUID
     *            The UID of the registration (the service ID)
     * @throws IOException
     *             Error writing to the client
     */
    private void sendEndpointDict(final HttpServletResponse aResp,
            final String aRegistrationUID) throws IOException {

        // The requested one
        RemoteServiceRegistration requested = null;

        // Get all registrations
        final RemoteServiceRegistration[] regBeans = pRepository
                .getLocalRegistrations();
        for (final RemoteServiceRegistration registration : regBeans) {
            if (registration.getServiceId().equals(aRegistrationUID)) {
                // Found it !
                requested = registration;
                break;
            }
        }

        if (requested == null) {
            // Unknown ID
            aResp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Unknown end point ID: " + aRegistrationUID);
            return;
        }

        // Convert the object to a map
        final Map<String, Object> regMap = registrationToMap(requested);
        if (regMap == null) {
            // Nothing to do
            aResp.sendError(HttpServletResponse.SC_NO_CONTENT,
                    "No valid end point for ID:" + aRegistrationUID);
            return;
        }

        // Convert to JSON
        final JSONObject jsonContent = new JSONObject(regMap);

        // Send
        sendJson(aResp, jsonContent.toString());
    }

    /**
     * Sends the whole content of the repository
     * 
     * @param aResp
     *            Servlet response
     * @throws IOException
     *             Error writing to the client
     */
    private void sendEndpoints(final HttpServletResponse aResp)
            throws IOException {

        // Get our registrations
        final RemoteServiceRegistration[] regBeans = pRepository
                .getLocalRegistrations();

        // Convert the objects to maps
        final List<Object> regMaps = new LinkedList<Object>();
        for (final RemoteServiceRegistration registration : regBeans) {

            final Map<String, Object> regMap = registrationToMap(registration);
            if (regMap != null) {
                // Avoid registrations without end point
                regMaps.add(regMap);
            }
        }

        // Convert to JSON
        final JSONArray jsonContent = new JSONArray(regMaps);

        // Send
        sendJson(aResp, jsonContent.toString());
    }

    /**
     * Writes a JSON response
     * 
     * @param aResp
     *            Servlet response
     * @param aJsonString
     *            JSON string
     * @throws IOException
     *             Error writing to the client
     */
    private void sendJson(final HttpServletResponse aResp,
            final String aJsonString) throws IOException {

        // Setup headers
        aResp.setStatus(HttpServletResponse.SC_OK);
        aResp.setContentType(JSON_TYPE);
        aResp.setContentLength(aJsonString.length());

        // Write the content
        final PrintWriter writer = aResp.getWriter();
        writer.print(aJsonString);
        writer.flush();
    }
}

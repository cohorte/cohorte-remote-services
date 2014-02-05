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
package org.cohorte.remote.dispatcher.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cohorte.remote.ExportEndpoint;
import org.cohorte.remote.IExportsDispatcher;
import org.cohorte.remote.IImportsRegistry;
import org.cohorte.remote.dispatcher.beans.PelixEndpointDescription;
import org.cohorte.remote.utilities.RSUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The servlet that publishes the content of the exported services registry
 * 
 * @author Thomas Calmant
 */
class RegistryServlet extends HttpServlet {

    /** JSON MIME-type */
    private static final String JSON_TYPE = "application/json";

    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    /** The exported services repository */
    private final IExportsDispatcher pDispatcher;

    /** Imported endpoints registry */
    private final IImportsRegistry pRegistry;

    /**
     * Sets up members
     * 
     * @param aRegistry
     *            Registry of imported endpoints
     * @param aDispatcher
     *            The export endpoints dispatcher
     */
    public RegistryServlet(final IImportsRegistry aRegistry,
            final IExportsDispatcher aDispatcher) {

        pRegistry = aRegistry;
        pDispatcher = aDispatcher;
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
            for (int i = 0; i < jsonEndpoints.length(); i++) {
                final JSONObject jsonEndpoint = jsonEndpoints.getJSONObject(i);

                // Parse the endpoint description
                final PelixEndpointDescription parsed = new PelixEndpointDescription(
                        jsonEndpoint);
                parsed.setServerAddress(senderAddr);

                // Register it
                pRegistry.add(parsed.toImportEndpoint());
            }

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
     * Sends the representation of the end point matching the given ID
     * 
     * @param aResp
     *            Servlet response
     * @param aEndpointUID
     *            The UID of an exported endpoint
     * @throws IOException
     *             Error writing to the client
     */
    private void sendEndpointDict(final HttpServletResponse aResp,
            final String aEndpointUID) throws IOException {

        // Get the requested endpoint
        final ExportEndpoint endpoint = pDispatcher.getEndpoint(aEndpointUID);
        if (endpoint == null) {
            // Unknown endpoint
            aResp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Unknown endpoint UID: " + aEndpointUID);
            return;
        }

        // Convert it to JSON
        final JSONObject jsonContent = new JSONObject(endpoint.toMap());

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

        // Get our endpoints
        final ExportEndpoint[] endpoints = pDispatcher.getEndpoints();

        // Convert them to maps
        final List<Object> endpointsMaps = new LinkedList<Object>();
        for (final ExportEndpoint endpoint : endpoints) {
            endpointsMaps.add(endpoint.toMap());
        }

        // Convert to JSON
        final JSONArray jsonContent = new JSONArray(endpointsMaps);

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

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
package org.cohorte.remote.jsonrpc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import org.jabsorb.ng.client.ClientError;
import org.jabsorb.ng.client.HTTPSession;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Overrides the Jabsorb implementation of its HTTP Session
 * 
 * @author Thomas Calmant
 */
public class JabsorbHttpSession extends HTTPSession {

    /** The session URL */
    private final URL pUrl;

    /**
     * Sets up the HTTP session
     * 
     * @param aUri
     *            The URI to use for communication
     * @throws MalformedURLException
     *             The given URI is malformed
     */
    public JabsorbHttpSession(final URI aUri) throws MalformedURLException {

        super(aUri);
        pUrl = aUri.toURL();
    }

    /**
     * Sends a POST request to the session URL with the given content
     * 
     * @param aRequestContent
     *            Request content
     * @return The result page
     * @throws ClientError
     *             Something wrong happened
     */
    protected String getUrlPostResult(final byte[] aRequestContent) {

        // Open a connection
        HttpURLConnection httpConnection = null;
        Scanner scanner = null;
        try {
            // Open the connection and cast it
            final URLConnection connection = pUrl.openConnection();
            if (!(connection instanceof HttpURLConnection)) {
                throw new ClientError("Unknown URL connection for : " + pUrl);
            }

            httpConnection = (HttpURLConnection) connection;

            // Make the connection writable (POST)
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);

            // Set up the headers
            httpConnection
                    .addRequestProperty("Content-Type", JSON_CONTENT_TYPE);
            httpConnection.addRequestProperty("Content-Length",
                    Integer.toString(aRequestContent.length));

            // Set POST data
            httpConnection.getOutputStream().write(aRequestContent);

            // Wait for an answer
            final int responseCode = httpConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new ClientError("Got HTTP Status " + responseCode
                        + " for URL " + pUrl);
            }

            // Use a scanner to read the response content See here for more
            // information:
            // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
            scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\A");
            return scanner.next();

        } catch (final IOException e) {
            // Convert error class
            throw new ClientError(e);

        } finally {
            // In any case, free the connection
            if (httpConnection != null) {
                httpConnection.disconnect();
            }

            // ... and close the scanner
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jabsorb.ng.client.HTTPSession#sendAndReceive(org.json.JSONObject)
     */
    @Override
    public JSONObject sendAndReceive(final JSONObject aMessage) {

        try {
            // Get the result and parse it
            final String result = getUrlPostResult(aMessage.toString()
                    .getBytes());
            return new JSONObject(result);

        } catch (final JSONException e) {
            throw new ClientError(e);
        }
    }
}

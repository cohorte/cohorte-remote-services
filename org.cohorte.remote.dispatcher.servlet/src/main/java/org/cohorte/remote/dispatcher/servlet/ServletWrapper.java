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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceController;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.remote.IRemoteServicesConstants;
import org.cohorte.remote.dispatcher.beans.PelixEndpointDescription;
import org.cohorte.remote.pelix.ExportEndpoint;
import org.cohorte.remote.pelix.IDispatcherServlet;
import org.cohorte.remote.pelix.IExportsDispatcher;
import org.cohorte.remote.pelix.IImportsRegistry;
import org.cohorte.remote.pelix.ImportEndpoint;
import org.cohorte.remote.utilities.RSUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

/**
 * Registers the servlet service to the first HTTP service seen
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-dispatcher-servlet-factory")
@Provides(specifications = IDispatcherServlet.class)
@Instantiate(name = "cohorte-remote-dispatcher-servlet")
public class ServletWrapper implements IDispatcherServlet {

    /** HTTP service port property */
    private static final String HTTP_SERVICE_PORT = "org.osgi.service.http.port";

    /** HTTPService dependency ID */
    private static final String IPOJO_ID_HTTP = "http.service";

    /** Exported endpoints dispatcher */
    @Requires
    private IExportsDispatcher pDispatcher;

    /** The HTTP server port */
    private int pHttpPort;

    /** The HTTP service */
    @Requires(id = IPOJO_ID_HTTP, filter = "(" + HTTP_SERVICE_PORT + "=*)")
    private HttpService pHttpService;

    /** Log service */
    @Requires
    private LogService pLogger;

    /** Imported services registry */
    @Requires
    private IImportsRegistry pRegistry;

    /** The service controller */
    @ServiceController
    private boolean pServiceController;

    /** The servlet object */
    private RegistryServlet pServlet;

    /** The servlet registration path */
    @Property(name = "servlet.path", value = "/pelix-dispatcher")
    private String pServletPath;

    /**
     * HTTP service ready
     * 
     * @param aHttpService
     *            The bound service
     * @param aServiceProperties
     *            The HTTP service properties
     */
    @Bind(id = IPOJO_ID_HTTP)
    private void bindHttpService(final HttpService aHttpService,
            final Map<?, ?> aServiceProperties) {

        final Object rawPort = aServiceProperties.get(HTTP_SERVICE_PORT);

        if (rawPort instanceof Number) {
            // Get the integer
            pHttpPort = ((Number) rawPort).intValue();

        } else if (rawPort instanceof CharSequence) {
            // Parse the string
            pHttpPort = Integer.parseInt(rawPort.toString());

        } else {
            // Unknown port type
            pLogger.log(LogService.LOG_WARNING, "Couldn't read access port "
                    + rawPort);
            pHttpPort = -1;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IDispatcherServlet#filterProperties(java.lang
     * .String, java.util.Map)
     */
    @Override
    public Map<String, Object> filterProperties(final String aFrameworkUID,
            final Map<String, Object> aProperties) {

        // Add the "imported" property
        aProperties.put(Constants.SERVICE_IMPORTED, true);

        // Replace the "exported configs"
        final Object configs = aProperties
                .remove(Constants.SERVICE_EXPORTED_CONFIGS);
        if (configs != null) {
            aProperties.put(Constants.SERVICE_IMPORTED_CONFIGS, configs);
        }

        // Clear other export properties
        aProperties.remove(Constants.SERVICE_EXPORTED_INTENTS);
        aProperties.remove(Constants.SERVICE_EXPORTED_INTENTS_EXTRA);
        aProperties.remove(Constants.SERVICE_EXPORTED_INTERFACES);

        // Add the framework UID
        aProperties.put(IRemoteServicesConstants.PROP_FRAMEWORK_UID,
                aFrameworkUID);

        return aProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.pelix.IDispatcherServlet#getPath()
     */
    @Override
    public String getPath() {

        return pServletPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.pelix.IDispatcherServlet#getPort()
     */
    @Override
    public int getPort() {

        return pHttpPort;
    }

    /**
     * Returns the response of a HTTP server, or throws an exception
     * 
     * @param aAddress
     *            Server address
     * @param aPort
     *            Server port
     * @param aPath
     *            Request URI
     * @return The raw response of the server
     */
    private String grabData(final InetAddress aAddress, final int aPort,
            final String aPath) {

        // Forge the URL
        final URL url;
        try {
            url = new URL("http", aAddress.getHostAddress(), aPort, aPath);

        } catch (final MalformedURLException ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Couldn't forge the URL to access: " + aAddress + " : "
                            + aPort + " - " + aPath, ex);
            return null;
        }

        // Open the connection
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.connect();

            // Flush the request
            final int responseCode = httpConnection.getResponseCode();
            if (responseCode != HttpServletResponse.SC_OK) {
                // Incorrect answer
                pLogger.log(LogService.LOG_WARNING, "Error: " + url
                        + " responded with code " + responseCode);
                return null;
            }

            // Get the response content
            final byte[] rawResult = RSUtils.inputStreamToBytes(httpConnection
                    .getInputStream());

            // Construct corresponding string
            return new String(rawResult);

        } catch (final IOException ex) {
            // Connection error
            pLogger.log(LogService.LOG_ERROR,
                    "Error requesting information from " + url.toString(), ex);

        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IDispatcherServlet#grabEndpoint(java.net.InetAddress
     * , int, java.lang.String, java.lang.String)
     */
    @Override
    public ImportEndpoint grabEndpoint(final InetAddress aAddress,
            final int aPort, final String aPath, final String aEndpointUID) {

        // Get the raw servlet result
        final String rawResponse = grabData(aAddress, aPort, aPath
                + "/endpoint/" + aEndpointUID);
        if (rawResponse == null || rawResponse.isEmpty()) {
            // No response
            pLogger.log(LogService.LOG_WARNING, "No response from the server "
                    + aAddress + " for end point " + aEndpointUID);
            return null;
        }

        try {
            // Parse it
            final JSONObject rawEndpoint = new JSONObject(rawResponse);

            // Convert the result
            final PelixEndpointDescription endpoint = new PelixEndpointDescription(
                    rawEndpoint);
            endpoint.setServerAddress(aAddress.getHostAddress());
            return endpoint.toImportEndpoint();

        } catch (final JSONException ex) {
            // Invalid response
            pLogger.log(LogService.LOG_WARNING,
                    "Invalid response from the server " + aAddress
                            + " for end point " + aEndpointUID + "\n"
                            + rawResponse, ex);
        }

        return null;
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        if (pServlet != null) {
            pHttpService.unregister(pServletPath);
            pServlet = null;
        }

        pLogger.log(LogService.LOG_INFO, "Dispatcher servlet gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IDispatcherServlet#sendDiscovered(java.lang.
     * String, int, java.lang.String)
     */
    @Override
    public void sendDiscovered(final String aHost, final int aPort,
            final String aPath) {

        // Prepare our endpoints
        final Collection<Map<String, Object>> endpointsMaps = new LinkedList<Map<String, Object>>();
        for (final ExportEndpoint endpoint : pDispatcher.getEndpoints()) {
            endpointsMaps.add(endpoint.toMap());
        }

        // Convert the list to JSON
        final String data = new JSONArray(endpointsMaps).toString();

        // Prepare the path to the servlet endpoints
        final StringBuilder servletPath = new StringBuilder(aPath);
        if (!aPath.endsWith("/")) {
            servletPath.append("/");
        }
        servletPath.append("endpoints");

        final URL url;
        try {
            url = new URL("http", aHost, aPort, aPath);

        } catch (final MalformedURLException ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Error forging URL to send a discovered packet: " + ex, ex);
            return;
        }

        // Send a POST request
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) url.openConnection();

            // POST message
            httpConnection.setRequestMethod("POST");
            httpConnection.setUseCaches(false);
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            // Headers
            httpConnection.setRequestProperty("Content-Type",
                    "application/json");

            // After fields, before content
            httpConnection.connect();

            // Write the event in the request body, if any
            final OutputStream outStream = httpConnection.getOutputStream();

            try {
                outStream.write(data.getBytes());
                outStream.flush();

            } finally {
                // Always be nice...
                outStream.close();
            }

            // Flush the request
            final int responseCode = httpConnection.getResponseCode();
            final String responseData = new String(
                    RSUtils.inputStreamToBytes(httpConnection.getInputStream()));

            if (responseCode != HttpURLConnection.HTTP_OK) {
                pLogger.log(LogService.LOG_WARNING,
                        "Error sending a 'discovered' packet: " + responseCode
                                + " - " + responseData);
                return;
            }

        } catch (final IOException ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Error sending the 'discovered' packet: " + ex, ex);

        } finally {
            // Clean up
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Preparation: deactivate the service
        pServiceController = false;

        // Set up the servlet
        pServlet = new RegistryServlet(pRegistry, pDispatcher);
        try {
            pHttpService.registerServlet(pServletPath, pServlet, null, null);

        } catch (final Exception ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Error registering the dispatcher servlet. Abandon.", ex);
            invalidate();
            return;
        }

        pLogger.log(LogService.LOG_INFO, "Dispatcher servlet ready");

        // No error: activate the service
        pServiceController = true;
    }
}

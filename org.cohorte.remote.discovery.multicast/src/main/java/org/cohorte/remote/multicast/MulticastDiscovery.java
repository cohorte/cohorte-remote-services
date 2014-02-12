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
package org.cohorte.remote.multicast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceController;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.remote.ExportEndpoint;
import org.cohorte.remote.IDispatcherServlet;
import org.cohorte.remote.IExportEndpointListener;
import org.cohorte.remote.IExportsDispatcher;
import org.cohorte.remote.IImportsRegistry;
import org.cohorte.remote.IRemoteServicesConstants;
import org.cohorte.remote.ImportEndpoint;
import org.cohorte.remote.multicast.beans.PelixMulticastPacket;
import org.cohorte.remote.multicast.utils.IPacketListener;
import org.cohorte.remote.multicast.utils.MulticastHandler;
import org.cohorte.remote.utilities.RSUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * Remote services discovery based on Multicast packets
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-discovery-muticast-factory")
@Provides(specifications = IExportEndpointListener.class)
public class MulticastDiscovery implements IExportEndpointListener,
        IPacketListener {

    /** UTF-8 charset name */
    private static final String CHARSET_UTF8 = "UTF-8";

    /** The bundle context */
    private final BundleContext pBundleContext;

    /** Exported endpoints dispatcher */
    @Requires
    private IExportsDispatcher pDispatcher;

    /** The dispatcher servlet */
    @Requires
    private IDispatcherServlet pDispatcherServlet;

    /** Framework UID */
    private String pFrameworkUID;

    /** Log service */
    @Requires
    private LogService pLogger;

    /** The multicast socket */
    private MulticastHandler pMulticast;

    /** The multicast group */
    @Property(name = "multicast.group", value = "239.0.0.1")
    // ff05::5
    private String pMulticastGroup;

    /** The multicast port */
    @Property(name = "multicast.port", value = "42000")
    private int pMulticastPort;

    /** Imported services registry */
    @Requires
    private IImportsRegistry pRegistry;

    /** The service controller: active only if the validation succeeded */
    @ServiceController
    private boolean pServiceController;

    /**
     * Sets up members
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public MulticastDiscovery(final BundleContext aBundleContext) {

        pBundleContext = aBundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IExportEndpointListener#endpointRemoved(org.
     * cohorte.remote.pelix.ExportEndpoint)
     */
    @Override
    public void endpointRemoved(final ExportEndpoint aEndpoint) {

        sendPacket(makeEndpointMap(IPacketConstants.EVENT_REMOVE, aEndpoint));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IExportEndpointListener#endpointsAdded(org.cohorte
     * .remote.pelix.ExportEndpoint[])
     */
    @Override
    public void endpointsAdded(final ExportEndpoint[] aEndpoints) {

        sendPacket(makeEndpointsMap(IPacketConstants.EVENT_ADD, aEndpoints));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.pelix.IExportEndpointListener#endpointUpdated(org.
     * cohorte.remote.pelix.ExportEndpoint, java.util.Map)
     */
    @Override
    public void endpointUpdated(final ExportEndpoint aEndpoint,
            final Map<String, Object> aOldProperties) {

        sendPacket(makeEndpointMap(IPacketConstants.EVENT_UPDATE, aEndpoint));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.multicast.utils.IPacketListener#handleError(java.lang
     * .Exception)
     */
    @Override
    public boolean handleError(final Exception aException) {

        // Log the error
        pLogger.log(LogService.LOG_ERROR,
                "Error reading a packet from the multicast handler", aException);

        // Continue if the exception is not "important"
        return !(aException instanceof SocketException || aException instanceof NullPointerException);
    }

    /**
     * Handles an endpoint event packet
     * 
     * @param aEndpointPacket
     *            The received packet
     * @param aSenderAddress
     *            Sender address
     */
    private void handleEvent(final PelixMulticastPacket aEndpointPacket,
            final InetAddress aSenderAddress) {

        final String event = aEndpointPacket.getEvent();
        if (IPacketConstants.EVENT_ADD.equals(event)) {
            // New endpoints
            final String path = aEndpointPacket.getAccessPath();
            final int port = aEndpointPacket.getAccessPort();

            for (final String uid : aEndpointPacket.getUIDs()) {
                // Grab each endpoint
                final ImportEndpoint endpoint = pDispatcherServlet
                        .grabEndpoint(aSenderAddress, port, path, uid);
                if (endpoint != null) {
                    pRegistry.add(endpoint);
                }
            }

        } else if (IPacketConstants.EVENT_REMOVE.equals(event)) {
            // Endpoint removed
            pRegistry.remove(aEndpointPacket.getUID());

        } else if (IPacketConstants.EVENT_UPDATE.equals(event)) {
            // Endpoint updated
            final Map<String, Object> newProperties = aEndpointPacket
                    .getNewProperties();
            pRegistry.update(aEndpointPacket.getUID(), newProperties);

        } else {
            // Unknown event
            pLogger.log(LogService.LOG_WARNING, "Unknown multicast event: "
                    + event);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.multicast.utils.IPacketListener#handlePacket(java.
     * net.SocketAddress, byte[])
     */
    @Override
    public void handlePacket(final InetSocketAddress aSender,
            final byte[] aContent) {

        // Read the packet content
        final String content;
        try {
            content = new String(aContent, CHARSET_UTF8).trim();

        } catch (final UnsupportedEncodingException ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Error reading multicast packet data", ex);
            return;
        }

        // Parse it from JSON
        final PelixMulticastPacket endpointPacket;
        try {
            final JSONObject parsed = new JSONObject(content);
            endpointPacket = new PelixMulticastPacket(parsed);

        } catch (final JSONException ex) {
            // Log
            pLogger.log(LogService.LOG_ERROR,
                    "Error parsing a multicast packet", ex);
            return;
        }

        // Avoid handling our own packets
        if (endpointPacket.isFromSender(pFrameworkUID)) {
            return;
        }

        // Get information about the sender
        final InetAddress senderAddress = aSender.getAddress();

        // Get the kind of event
        final String event = endpointPacket.getEvent();

        // Dispatch...
        if (IPacketConstants.EVENT_DISCOVERY.equals(event)) {
            // Discovery request: send a packet back
            pDispatcherServlet.sendDiscovered(senderAddress.getHostAddress(),
                    endpointPacket.getAccessPort(),
                    endpointPacket.getAccessPath());

        } else {
            // Handle an end point event
            handleEvent(endpointPacket, senderAddress);
        }
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidate() {

        if (pMulticast != null) {
            try {
                pMulticast.stop();

            } catch (final IOException ex) {
                pLogger.log(LogService.LOG_ERROR,
                        "Error stopping the multicast receiver", ex);
            }

            pMulticast.setLogger(null);
            pMulticast = null;
        }

        pLogger.log(LogService.LOG_INFO, "Multicast broadcaster gone");
    }

    /**
     * Prepares the common part of multicast events, including the access to the
     * servlet
     * 
     * @param aEvent
     *            Kind of event (see {@link IPacketConstants})
     * @return A map with basic informations
     */
    private Map<String, Object> makeBasicMap(final String aEvent) {

        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put(IPacketConstants.KEY_SENDER, pFrameworkUID);
        result.put(IPacketConstants.KEY_EVENT, aEvent);

        // Set the servlet access
        final Map<String, Object> access = new LinkedHashMap<String, Object>();
        access.put(IPacketConstants.KEY_ACCESS_PATH,
                pDispatcherServlet.getPath());
        access.put(IPacketConstants.KEY_ACCESS_PORT,
                pDispatcherServlet.getPort());
        result.put(IPacketConstants.KEY_ACCESS, access);

        return result;
    }

    /**
     * Prepares an event packet containing a single endpoint
     * 
     * @param aEvent
     *            Kind of event (update or remove)
     * @param aEndpoint
     *            An ExportEndpoint bean
     * @return The content of an event packet
     */
    private Map<String, Object> makeEndpointMap(final String aEvent,
            final ExportEndpoint aEndpoint) {

        // Prepare basic information
        final Map<String, Object> packet = makeBasicMap(aEvent);

        // Add endpoint information
        packet.put(IPacketConstants.KEY_ENDPOINT_UID, aEndpoint.getUid());

        // Update, using filtered properties
        if (IPacketConstants.EVENT_UPDATE.equals(aEvent)) {
            packet.put(IPacketConstants.KEY_ENDPOINT_NEW_PROPERTIES,
                    aEndpoint.makeImportProperties());
        }

        return packet;
    }

    /**
     * Prepares an event packet containing a multiple endpoints
     * 
     * @param aEvent
     *            Kind of event (add)
     * @param aEndpoints
     *            An array of ExportEndpoint beans
     * @return The content of an event packet
     */
    private Map<String, Object> makeEndpointsMap(final String aEvent,
            final ExportEndpoint[] aEndpoints) {

        // Prepare basic information
        final Map<String, Object> packet = makeBasicMap(aEvent);

        // Add endpoint information
        final String[] uids = new String[aEndpoints.length];
        for (int i = 0; i < aEndpoints.length; i++) {
            uids[i] = aEndpoints[i].getUid();
        }

        packet.put(IPacketConstants.KEY_ENDPOINT_UIDS, uids);
        return packet;
    }

    /**
     * Sends a discovery packet, requesting others to indicate their services
     */
    private void sendDiscovery() {

        sendPacket(makeBasicMap(IPacketConstants.EVENT_DISCOVERY));
    }

    /**
     * Sends a packet to the multicast, with the given content
     * 
     * @param aContent
     *            Content of the packet
     */
    private void sendPacket(final Map<String, ?> aContent) {

        sendPacket(aContent, null, 0);
    }

    /**
     * Sends a packet to the given target, with the given content
     * 
     * @param aContent
     *            Content of the packet
     * @param aTarget
     *            Target address
     * @param aPort
     *            Target port
     */
    private void sendPacket(final Map<String, ?> aContent,
            final InetAddress aTarget, final int aPort) {

        // Convert data to JSON
        final String data = new JSONObject(aContent).toString();

        try {
            // Convert to bytes
            final byte[] rawData = data.getBytes(CHARSET_UTF8);

            // Send bytes
            if (aTarget == null) {
                pMulticast.send(rawData);

            } else {
                pMulticast.send(rawData, aTarget, aPort);
            }

        } catch (final UnsupportedEncodingException ex) {
            // Log
            pLogger.log(LogService.LOG_ERROR, "System does not support "
                    + CHARSET_UTF8, ex);

        } catch (final IOException ex) {
            // Log
            pLogger.log(LogService.LOG_ERROR,
                    "Error sending a multicast packet", ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder("MulticastDiscovery({");
        // End point
        builder.append(pMulticastGroup).append("}:").append(pMulticastPort);

        // Service state
        builder.append(", ");
        if (pServiceController) {
            builder.append("up and running");
        } else {
            builder.append("stopped");
        }
        builder.append(")");

        return builder.toString();
    }

    /**
     * Component validated
     */
    @Validate
    public void validate() {

        // Preparation: deactivate the service
        pServiceController = false;

        // Setup the isolate UID
        pFrameworkUID = RSUtils.setupUID(pBundleContext,
                IRemoteServicesConstants.ISOLATE_UID);

        // Compute the group address
        InetAddress groupAddress;
        try {
            groupAddress = InetAddress.getByName(pMulticastGroup);

        } catch (final UnknownHostException ex) {
            pLogger.log(LogService.LOG_ERROR,
                    "Error computing the multicast group address", ex);
            invalidate();
            return;
        }

        // Create the handler
        pMulticast = new MulticastHandler(this, groupAddress, pMulticastPort);
        pMulticast.setLogger(pLogger);

        try {
            // Start it
            pMulticast.start();

        } catch (final IOException ex) {
            // Error...
            pLogger.log(LogService.LOG_ERROR,
                    "Error starting the multicast receiver. Abandon.", ex);
            invalidate();
            return;
        }

        pLogger.log(LogService.LOG_INFO, "Multicast broadcaster ready");

        // No error: activate the service
        pServiceController = true;

        // Also send a discovery request
        sendDiscovery();
    }
}

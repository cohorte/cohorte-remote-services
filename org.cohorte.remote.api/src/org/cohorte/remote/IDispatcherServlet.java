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
package org.cohorte.remote;

import java.net.InetAddress;

/**
 * Specifies the dispatcher servlet
 * 
 * @author Thomas Calmant
 */
public interface IDispatcherServlet {

    /**
     * Retrieves the path the servlet is registered to
     * 
     * @return A URI path
     */
    String getPath();

    /**
     * Returns the port the HTTP service is listening to
     * 
     * @return A HTTP port
     */
    int getPort();

    /**
     * Retrieves the description of an endpoint from a dispatcher servlet
     * 
     * @param aAddress
     *            Address of the server hosting the servlet
     * @param aPort
     *            Port the server is listening to
     * @param aPath
     *            Path to the servlet
     * @param aEndpointUID
     *            UID of an endpoint
     * @return The description of the endpoint, or null
     */
    ImportEndpoint grabEndpoint(InetAddress aAddress, int aPort, String aPath,
            String aEndpointUID);

    /**
     * Sends a "discovered" HTTP POST request to the dispatcher servlet of the
     * framework that has been discovered
     * 
     * @param aHost
     *            Address for the discovered framework
     * @param aPort
     *            HTTP port of the discovered framework
     * @param aPath
     *            Path to its dispatcher servlet
     */
    void sendDiscovered(String aHost, int aPort, String aPath);
}

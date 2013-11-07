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
package org.cohorte.remote;

import org.cohorte.remote.beans.RemoteServiceEvent;

/**
 * Defines the Remote Service Broadcaster (RSB)
 * 
 * @author Thomas Calmant
 */
public interface IRemoteServiceBroadcaster {

    /**
     * Sends a request to all other isolates to send notifications about their
     * current exported services.
     * 
     * They'll communicate directly with the sender RSR to register their end
     * points.
     */
    RemoteServiceEvent[] requestAllEndpoints();

    /**
     * Sends a request to the given isolate to send notifications about its
     * current exported services.
     * 
     * @param aIsolateUID
     *            An isolate UID
     * @return The services exported by the given isolate
     */
    RemoteServiceEvent[] requestEndpoints(String aIsolateUID);

    /**
     * Sends the given event to all other isolates
     * 
     * @param aEvent
     *            Remote service event to be sent
     */
    void sendNotification(RemoteServiceEvent aEvent);
}

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
 * Describes a remote service event listener
 * 
 * @author Thomas Calmant
 */
public interface IRemoteServiceEventListener {

    /**
     * Notifies the listener that an isolate has been lost
     * 
     * @param aIsolateUID
     *            The UID of the lost isolate
     */
    void handleIsolateLost(String aIsolateUID);

    /**
     * Notifies the listener that an isolate has been registered
     * 
     * @param aIsolateUID
     *            The UID of the new isolate
     */
    void handleIsolateReady(String aIsolateUID);

    /**
     * Notifies the listener that a remote event has been received.
     * 
     * @param aServiceEvent
     *            The remote service event
     */
    void handleRemoteEvent(RemoteServiceEvent aServiceEvent);
}

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

import org.cohorte.remote.beans.RemoteServiceRegistration;

/**
 * Description of an RSR (Remote Service Repository)
 * 
 * @author Thomas Calmant
 */
public interface IRemoteServiceRepository {

    /**
     * Retrieves all local end points registered to this RSR.
     * 
     * @return All exported service known by this RSR
     */
    RemoteServiceRegistration[] getLocalRegistrations();

    /**
     * Registers an exported service
     * 
     * @param aRegistration
     *            An exported service registration
     * @return True if the registration succeeded
     */
    boolean registerExportedService(RemoteServiceRegistration aRegistration);

    /**
     * Unregisters an exported service
     * 
     * @param aRegistration
     *            An exported service registration
     * @return True if the registration succeeded
     */
    boolean unregisterExportedService(RemoteServiceRegistration aRegistration);
}

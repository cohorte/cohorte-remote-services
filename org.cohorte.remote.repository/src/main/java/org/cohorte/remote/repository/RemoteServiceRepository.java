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
package org.cohorte.remote.repository;

import java.util.HashSet;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.cohorte.remote.IRemoteServiceRepository;
import org.cohorte.remote.beans.RemoteServiceRegistration;
import org.osgi.service.log.LogService;

/**
 * Implementation of an RSR
 * 
 * @author Thomas Calmant
 */
@Component(name = "cohorte-remote-repository-factory")
@Provides(specifications = IRemoteServiceRepository.class)
@Instantiate(name = "cohorte-remote-repository")
public class RemoteServiceRepository implements IRemoteServiceRepository {

    /** Log service, injected by iPOJO */
    @Requires
    private LogService pLogger;

    /** Exported service registrations */
    private final Set<RemoteServiceRegistration> pRegistrations = new HashSet<RemoteServiceRegistration>();

    /*
     * (non-Javadoc)
     * 
     * @see org.cohorte.remote.IRemoteServiceRepository#getLocalRegistrations()
     */
    @Override
    public synchronized RemoteServiceRegistration[] getLocalRegistrations() {

        return pRegistrations.toArray(new RemoteServiceRegistration[0]);
    }

    /**
     * Component invalidated
     */
    @Invalidate
    public void invalidatePojo() {

        pLogger.log(LogService.LOG_INFO,
                "COHORTE Remote Service Repository Gone");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.psem2m.isolates.services.remote.IRemoteServiceRepository#
     * registerExportedService
     * (org.psem2m.isolates.services.remote.beans.RemoteServiceRegistration)
     */
    @Override
    public synchronized boolean registerExportedService(
            final RemoteServiceRegistration aRegistration) {

        if (aRegistration != null) {
            pRegistrations.add(aRegistration);
            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cohorte.remote.IRemoteServiceRepository#unregisterExportedService
     * (org.cohorte.remote.beans.RemoteServiceRegistration)
     */
    @Override
    public synchronized boolean unregisterExportedService(
            final RemoteServiceRegistration aRegistration) {

        return pRegistrations.remove(aRegistration);
    }

    /**
     * Component validated
     */
    @Validate
    public void validatePojo() {

        pLogger.log(LogService.LOG_INFO,
                "COHORTE Remote Service Repository Ready");
    }
}

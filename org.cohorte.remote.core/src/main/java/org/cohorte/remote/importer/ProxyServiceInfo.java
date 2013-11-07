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
package org.cohorte.remote.importer;

import org.cohorte.remote.beans.RemoteServiceRegistration;
import org.osgi.framework.ServiceRegistration;

/**
 * Stored information about proxied services
 * 
 * @author Thomas Calmant
 */
public class ProxyServiceInfo {

    /** The proxy object */
    private final Object pProxy;

    /** The remote service registration bean */
    private final RemoteServiceRegistration pRemoteRegistration;

    /** The service registration information */
    private final ServiceRegistration<?> pServiceRegistration;

    /**
     * Sets up the bean
     * 
     * @param aRemoteRegistration
     *            Remote service registration
     * @param aLocalRegistration
     *            Local service registration information
     * @param aProxy
     *            Service proxy object
     */
    public ProxyServiceInfo(
            final RemoteServiceRegistration aRemoteRegistration,
            final ServiceRegistration<?> aLocalRegistration, final Object aProxy) {

        pRemoteRegistration = aRemoteRegistration;
        pServiceRegistration = aLocalRegistration;
        pProxy = aProxy;
    }

    /**
     * Retrieves the proxy object
     * 
     * @return the proxy object
     */
    public Object getProxy() {

        return pProxy;
    }

    /**
     * Retrieves the remote service registration
     * 
     * @return the remote service registration
     */
    public RemoteServiceRegistration getRemoteRegistration() {

        return pRemoteRegistration;
    }

    /**
     * Retrieves the service registration information
     * 
     * @return the service registration information
     */
    public ServiceRegistration<?> getServiceRegistration() {

        return pServiceRegistration;
    }
}

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

import java.util.Set;

import org.cohorte.remote.beans.EndpointDescription;
import org.osgi.framework.ServiceReference;

/**
 * Represents an end point handler
 * 
 * @author Thomas Calmant
 */
public interface IEndpointHandler {

    /** Name of the interface to be exported */
    String INTERFACE_NAME = "endpoint.interface.name";

    /** End point service property name */
    String PROP_ENDPOINT_NAME = "endpoint.name";

    /**
     * Create all end points needed for the specified service
     * 
     * @param aExportedInterfaces
     *            Interfaces to export from the given service
     * @param aServiceReference
     *            A reference to the service to be exported
     * @return A description of all created end points, null on error
     */
    EndpointDescription[] createEndpoint(Set<String> aExportedInterfaces,
            ServiceReference<?> aServiceReference);

    /**
     * Destroys the end point(s) associated to the given service
     * 
     * @param aServiceReference
     *            A service reference
     * @return True on success
     */
    boolean destroyEndpoint(ServiceReference<?> aServiceReference);

    /**
     * Retrieves all end points associated to the given service reference
     * 
     * @param aServiceReference
     *            A reference to the exported service
     * @return All associated end points, or an empty array (never null)
     */
    EndpointDescription[] getEndpoints(ServiceReference<?> aServiceReference);
}

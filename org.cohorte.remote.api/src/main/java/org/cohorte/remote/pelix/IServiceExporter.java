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
package org.cohorte.remote.pelix;

import java.util.Map;

import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

/**
 * Specifies a service exporter
 * 
 * @author Thomas Calmant
 */
public interface IServiceExporter {

    /**
     * Exports a service and returns the corresponding ExportEndpoint
     * 
     * @param aReference
     *            {@link ServiceReference} of the exported service
     * @param aName
     *            Endpoint name
     * @param aFramworkUid
     *            Framework UID
     * @return The ExportEndpoint bean
     * @throws IllegalArgumentException
     *             Endpoint name already used
     * @throws BundleException
     *             Error using the service
     */
    ExportEndpoint exportService(ServiceReference<?> aReference, String aName,
            String aFramworkUid) throws BundleException;

    /**
     * Tests if the exporter can export a service with the given configurations
     * 
     * @param aConfigurations
     *            The service export configurations
     * @return True if this exporter can handle (one of) those configurations
     */
    boolean handles(String[] aConfigurations);

    /**
     * Deletes an export endpoint
     * 
     * @param aEndpoint
     *            The export endpoint to delete
     */
    void unexportService(ExportEndpoint aEndpoint);

    /**
     * Updates an export endpoint
     * 
     * @param aEndpoint
     *            A {@link ExportEndpoint} bean
     * @param aNewName
     *            Future endpoint name
     * @param aOldProperties
     *            Previous properties
     * @throws IllegalArgumentException
     *             Rename refused
     */
    void updateExport(ExportEndpoint aEndpoint, String aNewName,
            Map<String, Object> aOldProperties);
}

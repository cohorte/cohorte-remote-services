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

/**
 * Specifies an export endpoint listener
 * 
 * @author Thomas Calmant
 */
public interface IExportEndpointListener {

    /**
     * An export endpoint has been removed
     * 
     * @param aEndpoint
     *            The removed endpoint
     */
    void endpointRemoved(ExportEndpoint aEndpoint);

    /**
     * Multiple endpoints have been created for export
     * 
     * @param aEndpoints
     *            An array of exported endpoints
     */
    void endpointsAdded(ExportEndpoint[] aEndpoints);

    /**
     * An endpoint has been updated
     * 
     * @param aEndpoint
     *            Updated endpoint
     * @param oldProperties
     *            Previous properties
     */
    void endpointUpdated(ExportEndpoint aEndpoint,
            Map<String, Object> oldProperties);
}

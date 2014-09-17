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

/**
 * Remote Services constants
 *
 * @author Thomas Calmant
 */
public interface IRemoteServicesConstants {

    /** COHORTE: Force/Refuse service export */
    String COHORTE_SERVICE_EXPORT = "cohorte.service.export";

    /**
     * Contains the isolate Unique IDentifier, a String.
     *
     * This might be a Framework property (read-only), or a generated System
     * property.
     */
    String ISOLATE_UID = "cohorte.isolate.uid";

    /** End point service property name */
    String PROP_ENDPOINT_NAME = "endpoint.name";

    /**
     * List of specifications that must never exported. Acts as a filter when
     * exporting all other specifications with the "service.exported.interfaces"
     * property set to "*"
     */
    String PROP_EXPORT_REJECT = "pelix.remote.export.reject";

    /**
     * The UID of the framework that exports the service. This service property
     * is set by the discoverer, when it parses an end point event packet.
     */
    String PROP_FRAMEWORK_UID = "pelix.remote.framework.uid";

    /** Exported interfaces synonyms (String[]) */
    String PROP_SYNONYMS = "pelix.remote.synonyms";

    /** Imported service flag (boolean) */
    String SERVICE_IMPORTED = "service.imported";

    /** ID of the isolate publishing the imported service (String) */
    String SERVICE_IMPORTED_FROM = "service.imported.from";

    /** Unknown isolate ID constant */
    String UNKNOWN_ISOLATE_ID = "unknown";
}

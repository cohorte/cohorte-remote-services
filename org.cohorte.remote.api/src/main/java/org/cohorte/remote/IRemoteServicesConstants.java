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
     * Remote Service Importer exclusion filter
     * 
     * Default : "" Format : "filter1,filter2"
     */
    String FILTERS_EXCLUDE = "org.cohorte.remote.filters.exclude";

    /**
     * Remote Service Importer inclusion filter
     * 
     * Default : "*" Format : "filter1,filter2"
     */
    String FILTERS_INCLUDE = "org.cohorte.remote.filters.include";

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
     * The UID of the framework that exports the service. This service property
     * is set by the discoverer, when it parses an end point event packet.
     */
    String PROP_FRAMEWORK_UID = "pelix.remote.framework.uid";

    /** Imported service flag (boolean) */
    String SERVICE_IMPORTED = "service.imported";

    /** ID of the isolate publishing the imported service (String) */
    String SERVICE_IMPORTED_FROM = "service.imported.from";

    /** Exported interfaces synonyms (String[]) */
    String SYNONYM_INTERFACES = "cohorte.remote.synonyms";

    /** Unknown isolate ID constant */
    String UNKNOWN_ISOLATE_ID = "unknown";
}

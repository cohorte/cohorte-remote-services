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
package org.cohorte.remote.multicast;

/**
 * Defines the constant keys and values used in Pelix remote services discovery
 * 
 * @author Thomas Calmant
 */
public interface IPacketConstants {

    /** A remote service have been added */
    String EVENT_ADD = "add";

    /** Reply to a discovery packet */
    String EVENT_DISCOVERED = "discovered";

    /** Request for all isolates to signal themselves */
    String EVENT_DISCOVERY = "discovery";

    /** A remote service have been removed */
    String EVENT_REMOVE = "remove";

    /** A remote service have been updated */
    String EVENT_UPDATE = "update";

    /** The access to the storage servlet */
    String KEY_ACCESS = "access";

    /** Path to the servlet */
    String KEY_ACCESS_PATH = "path";

    /** Port to the servlet HTTP server */
    String KEY_ACCESS_PORT = "port";

    /** New properties of an updated endpoint */
    String KEY_ENDPOINT_NEW_PROPERTIES = "new_properties";

    /** The UID of an endpoint */
    String KEY_ENDPOINT_UID = "uid";

    /** The UIDs of multiple endpoints */
    String KEY_ENDPOINT_UIDS = "uids";

    /** The kind of event */
    String KEY_EVENT = "event";

    /** The UID of the sender of the event */
    String KEY_SENDER = "sender";
}

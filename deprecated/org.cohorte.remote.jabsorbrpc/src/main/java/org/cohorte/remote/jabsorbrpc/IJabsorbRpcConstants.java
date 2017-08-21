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
package org.cohorte.remote.jabsorbrpc;

/**
 * @author Thomas Calmant
 * 
 */
public interface IJabsorbRpcConstants {

    /** Default Jabsorb servlet name */
    String DEFAULT_SERVLET_NAME = "/JABSORB-RPC";

    /** The exported configuration : jabsorb-rpc */
    String[] EXPORT_CONFIGS = { "jabsorb-rpc" };

    /** HTTP protocol name */
    String EXPORT_PROTOCOL = "http";

    /** Name of the endpoint */
    String PROP_ENDPOINT_NAME = "ecf.jabsorb.name";

    /** HTTP accesses (comma-separated String) */
    String PROP_HTTP_ACCESSES = "ecf.jabsorb.accesses";
}

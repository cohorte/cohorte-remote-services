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
package org.cohorte.remote.jsonrpc;

/**
 * @author Thomas Calmant
 * 
 */
public interface IJsonRpcConstants {

    /** Default Jabsorb servlet name */
    String DEFAULT_SERVLET_NAME = "/JSON-RPC";

    /** The exported configuration : json-rpc */
    String[] EXPORT_CONFIGS = { "jsonrpc", "json-rpc", "*" };

    /** HTTP protocol name */
    String EXPORT_PROTOCOL = "http";
}

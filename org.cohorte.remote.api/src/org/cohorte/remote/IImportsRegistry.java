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
package org.cohorte.remote;

import java.util.Map;

/**
 * Specifies the imported endpoints registry
 * 
 * @author Thomas Calmant
 */
public interface IImportsRegistry {

    /**
     * Registers an end point and notifies listeners. Does nothing if the
     * endpoint UID was already known.
     * 
     * @param aEndpoint
     *            The discovered endpoint
     * @return True if the end point has been used
     */
    boolean add(ImportEndpoint aEndpoint);

    /**
     * Unregisters all the end points associated to the given framework UID
     * 
     * @param aFrameworkUid
     *            The UID of a framework
     */
    void lostFramework(String aFrameworkUid);

    /**
     * Unregisters an end point and notifies listeners
     * 
     * @param aUid
     *            The UID of the end point to unregister
     */
    void remove(String aUid);

    /**
     * Updates an end point and notifies listeners
     * 
     * @param aUid
     *            The UID of the end point
     * @param aNewProperties
     *            The new properties of the end point
     */
    void update(String aUid, Map<String, Object> aNewProperties);
}

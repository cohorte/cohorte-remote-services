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

import java.net.MalformedURLException;
import java.net.URI;

import org.jabsorb.ng.client.IHTTPSession;
import org.jabsorb.ng.client.IHTTPSessionProvider;

/**
 * Implementation of an HTTPSession provider for Jabsorb
 * 
 * @author Thomas Calmant
 */
public class JabsorbHttpSessionProvider implements IHTTPSessionProvider {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jabsorb.ng.client.IHTTPSessionProvider#newHTTPSession(java.net.URI)
     */
    @Override
    public IHTTPSession newHTTPSession(final URI aUri)
            throws MalformedURLException {

        return new JabsorbHttpSession(aUri);
    }
}

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
package org.cohorte.ecf.provider.jabsorb.identity;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.URIID;

/**
 * @author Thomas Calmant
 */
public class JabsorbID extends URIID {

    /** Serial UID */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new Jabsorb UID from an URI string
     * 
     * @param aNamespace
     *            The parent namespace
     * @param aUri
     *            The URI of a remote service
     * @throws URISyntaxException
     *             Invalid URI string
     */
    public JabsorbID(final Namespace aNamespace, final URI aUri) {

        super(aNamespace, aUri);
    }

    /**
     * Returns the URI string representation
     */
    @Override
    public String toString() {

        return "JabsorbID[" + getName() + "]";
    }
}

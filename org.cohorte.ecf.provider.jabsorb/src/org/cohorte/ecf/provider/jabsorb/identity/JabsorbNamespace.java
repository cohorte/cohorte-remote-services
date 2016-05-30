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

import org.cohorte.ecf.provider.jabsorb.JabsorbConstants;
import org.eclipse.ecf.core.identity.URIID.URIIDNamespace;

/**
 * The Jabsorb default transport namespace (jabsorb://).
 * 
 * Based on the R-OSGi implementation.
 * 
 * @author Thomas Calmant
 */
public class JabsorbNamespace extends URIIDNamespace {

	private static final long serialVersionUID = 4315928629875372101L;
	private static JabsorbNamespace INSTANCE;
	
	/**
	 * The singleton instance of this namespace is created (and registered
	 * as a Namespace service) in the Activator class for this bundle.
	 * The singleton INSTANCE may then be used by both server and client.
	 */
	public JabsorbNamespace() {
		super(JabsorbConstants.NAMESPACE_NAME, "Jabsorb Namespace");
		INSTANCE = this;
	}

	public static JabsorbNamespace getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String getScheme() {
		return "jabsorb";
	}
}

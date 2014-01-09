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
package org.cohorte.ecf.provider.jabsorb;

import java.util.Dictionary;
import java.util.Map;

import org.cohorte.ecf.provider.jabsorb.client.JabsorbClientContainer;
import org.cohorte.ecf.provider.jabsorb.host.JabsorbHostContainer;
import org.cohorte.ecf.provider.jabsorb.host.JabsorbHttpServiceComponent;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator;
import org.eclipse.ecf.remoteservice.servlet.ServletServerContainerInstantiator;
import org.osgi.framework.Constants;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

/**
 * Called by ECF to instantiate server and client containers
 * 
 * Defined as an extension point in plugin.xml.
 * 
 * @author Thomas Calmant
 */
public class JabsorbContainerInstantiator extends
        ServletServerContainerInstantiator implements
        IRemoteServiceContainerInstantiator {

    /** Supported intents */
    private static final String[] SUPPORTED_INTENTS = { "passByValue",
            "exactlyOnce", "ordered" };

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.core.provider.BaseContainerInstantiator#createInstance
     * (org.eclipse.ecf.core.ContainerTypeDescription, java.lang.Object[])
     */
    @Override
    public IContainer createInstance(
            final ContainerTypeDescription aDescription,
            final Object[] aParameters) throws ContainerCreateException {

        // Get the HTTP component
        final JabsorbHttpServiceComponent httpComponent = JabsorbHttpServiceComponent
                .getInstance();
        if (httpComponent == null) {
            throw new ContainerCreateException("HTTP component not activated");
        }

        // Check parameters
        if (aParameters == null || aParameters.length == 0) {
            throw new ContainerCreateException(
                    "No parameter given to create a Jabsorb container.");
        }

        // The parameter is a map
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) aParameters[0];

        // Check import flag
        final boolean serviceImported = map
                .containsKey(Constants.SERVICE_IMPORTED);

        // Generate an ID
        final ID containerId = generateID(map, !serviceImported);

        if (serviceImported) {
            // Import
            System.out.println("Imported !");
            System.out.println("Props=" + map);

            return new JabsorbClientContainer(containerId, map);

        } else {
            System.out.println("Exported !");

            // Create the container instance
            return new JabsorbHostContainer(containerId, httpComponent);
        }
    }

    /**
     * Uses the ID found in the properties, or generates a random one
     * 
     * @param aProperties
     *            Container instance parameters
     * @return A Jabsorb ID
     */
    private ID generateID(final Map<String, Object> aProperties,
            final boolean aExport) {

        String id = null;
        if (!aExport) {
            // Grab the endpoint ID value
            id = (String) aProperties.get(RemoteConstants.ENDPOINT_ID);
        }

        if (id == null || id.isEmpty()) {
            // No ID given, generate one
            id = "uuid:" + java.util.UUID.randomUUID().toString();
        }

        // Return an ID object
        return IDFactory.getDefault().createID(
                JabsorbConstants.IDENTITY_NAMESPACE, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator#
     * getImportedConfigs(org.eclipse.ecf.core.ContainerTypeDescription,
     * java.lang.String[])
     */
    @Override
    public String[] getImportedConfigs(
            final ContainerTypeDescription aDescription,
            final String[] aExporterSupportedConfigs) {

        if (aExporterSupportedConfigs == null) {
            return null;
        }

        // Look for Jabsorb configuration in exported ones
        for (final String exporterConfig : aExporterSupportedConfigs) {
            if (JabsorbConstants.JABSORB_CONFIG.equals(exporterConfig)) {
                return JabsorbConstants.JABSORB_CONFIGS;
            }
        }

        // No match
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator#
     * getPropertiesForImportedConfigs
     * (org.eclipse.ecf.core.ContainerTypeDescription, java.lang.String[],
     * java.util.Dictionary)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Dictionary getPropertiesForImportedConfigs(
            final ContainerTypeDescription aDescription,
            final String[] aImportedConfigs,
            final Dictionary aExportedProperties) {

        // Return all properties
        return aExportedProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.provider.BaseContainerInstantiator#
     * getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
     */
    @Override
    public String[] getSupportedAdapterTypes(
            final ContainerTypeDescription description) {

        // Necessary to be called with the ServiceReference parameter
        return getInterfacesAndAdaptersForClass(JabsorbHostContainer.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator#
     * getSupportedConfigs(org.eclipse.ecf.core.ContainerTypeDescription)
     */
    @Override
    public String[] getSupportedConfigs(
            final ContainerTypeDescription aDescription) {

        return JabsorbConstants.JABSORB_CONFIGS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedIntents
     * (org.eclipse.ecf.core.ContainerTypeDescription)
     */
    @Override
    public String[] getSupportedIntents(
            final ContainerTypeDescription description) {

        return SUPPORTED_INTENTS;
    }
}

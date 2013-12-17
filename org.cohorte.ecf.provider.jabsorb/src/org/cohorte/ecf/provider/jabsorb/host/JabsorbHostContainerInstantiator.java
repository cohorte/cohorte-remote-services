/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.host;

import java.util.Dictionary;
import java.util.Map;

import org.cohorte.ecf.provider.jabsorb.JabsorbConstants;
import org.cohorte.ecf.provider.jabsorb.JabsorbContainer;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator;
import org.eclipse.ecf.remoteservice.servlet.ServletServerContainerInstantiator;

/**
 * @author Thomas Calmant
 */
public class JabsorbHostContainerInstantiator extends
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

        if (aParameters == null) {
            // Import
            System.out.println("Imported !");

            // FIXME: Generate an ID
            final String uuid = "uuid:"
                    + java.util.UUID.randomUUID().toString();
            final ID containerID = IDFactory.getDefault().createID(
                    JabsorbConstants.IDENTITY_NAMESPACE, uuid);

            return new JabsorbContainer(containerID);

        } else {
            System.out.println("Exported !");

            // FIXME: Make the ID
            @SuppressWarnings("unchecked")
            final Map<String, Object> map = (Map<String, Object>) aParameters[0];

            // Use the given ID
            final String id = (String) map.get("id");
            final ID containerId = IDFactory.getDefault().createID(
                    JabsorbConstants.IDENTITY_NAMESPACE, id);

            // Create the container instance
            return new JabsorbHostContainer(containerId, httpComponent);
        }
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

        // TODO: maybe return the Jabsorb URI and endpoint name ?
        System.out.println("Exported properties: " + aExportedProperties);
        return null;
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

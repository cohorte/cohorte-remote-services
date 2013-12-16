/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb;

import java.util.Dictionary;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;

/**
 * @author thomas
 * 
 */
public class JabsorbContainerInstantiator implements IContainerInstantiator,
        IRemoteServiceContainerInstantiator {

    /** Jabsorb intents */
    private static final String[] JABSORB_INTENTS = { "passByValue",
            "exactlyOnce", "ordered" };

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org
     * .eclipse.ecf.core.ContainerTypeDescription, java.lang.Object[])
     */
    @Override
    public IContainer createInstance(
            final ContainerTypeDescription description,
            final Object[] parameters) throws ContainerCreateException {

        // TODO Auto-generated method stub
        return null;
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
        for (String exporterConfig : aExporterSupportedConfigs) {
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
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedAdapterTypes
     * (org.eclipse.ecf.core.ContainerTypeDescription)
     */
    @Override
    public String[] getSupportedAdapterTypes(
            final ContainerTypeDescription description) {

        return new String[] { IRemoteServiceContainerAdapter.class.getName(),
                IContainer.class.getName() };
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

        return JABSORB_INTENTS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.provider.IContainerInstantiator#
     * getSupportedParameterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Class[][] getSupportedParameterTypes(
            final ContainerTypeDescription description) {

        // Parameters usable in createInstance
        return new Class[][] { new Class[] {}, new Class[] { ID.class } };
    }

}

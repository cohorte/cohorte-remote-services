/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.host;

import java.util.Arrays;

import org.cohorte.ecf.provider.jabsorb.JabsorbConstants;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.remoteservice.servlet.ServletServerContainerInstantiator;

/**
 * @author Thomas Calmant
 */
public class JabsorbHostContainerInstantiator extends
        ServletServerContainerInstantiator {

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
        JabsorbHttpServiceComponent httpComponent = (JabsorbHttpServiceComponent) JabsorbHttpServiceComponent
                .getDefault();
        if (httpComponent == null) {
            throw new ContainerCreateException("HTTP component not activated");
        }

        // TODO: Make the ID
        System.out.println("Host Cont Inst params = "
                + Arrays.toString(aParameters));
        ID containerId = IDFactory.getDefault().createID(
                JabsorbConstants.IDENTITY_NAMESPACE, aParameters);

        // Create the container instance
        return new JabsorbHostContainer(containerId, httpComponent);
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
}

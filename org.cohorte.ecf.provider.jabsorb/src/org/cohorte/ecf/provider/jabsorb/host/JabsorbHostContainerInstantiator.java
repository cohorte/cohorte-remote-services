/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.host;

import java.util.Arrays;
import java.util.Map;

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
        JabsorbHttpServiceComponent httpComponent = JabsorbHttpServiceComponent
                .getInstance();
        if (httpComponent == null) {
            throw new ContainerCreateException("HTTP component not activated");
        }

        // FIXME: Make the ID
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) aParameters[0];
        String id = (String) map.get("id");

        System.out.println("Host Cont Inst params = "
                + Arrays.toString(aParameters));
        ID containerId = IDFactory.getDefault().createID(
                JabsorbConstants.IDENTITY_NAMESPACE, id);

        // Create the container instance
        return new JabsorbHostContainer(containerId, httpComponent);
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
}

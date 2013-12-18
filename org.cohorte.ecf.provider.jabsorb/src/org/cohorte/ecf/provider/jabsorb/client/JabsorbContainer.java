/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.client;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cohorte.ecf.provider.jabsorb.Activator;
import org.cohorte.ecf.provider.jabsorb.JabsorbConstants;
import org.cohorte.remote.utilities.BundlesClassLoader;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceCallPolicy;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.client.AbstractClientContainer;
import org.eclipse.ecf.remoteservice.client.IRemoteCallable;
import org.eclipse.ecf.remoteservice.client.IRemoteServiceClientContainerAdapter;
import org.eclipse.ecf.remoteservice.client.RemoteCallable;
import org.eclipse.ecf.remoteservice.client.RemoteServiceClientRegistration;
import org.osgi.framework.Constants;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

/**
 * Jabsorb ECF client container
 * 
 * @author Thomas Calmant
 */
public class JabsorbContainer extends AbstractClientContainer implements
        IRemoteServiceClientContainerAdapter {

    /** Service registration */
    private IRemoteServiceRegistration pRegistration;

    /** Service properties */
    private final Map<String, Object> pServiceProperties = new HashMap<String, Object>();

    /**
     * Sets up the container
     * 
     * @param containerID
     *            The container ID
     * @param aProperties
     *            Imported service properties
     */
    public JabsorbContainer(final ID containerID,
            final Map<String, Object> aProperties) {

        // Set up the container
        super(containerID);

        // Store properties
        if (aProperties != null) {
            pServiceProperties.putAll(aProperties);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.client.AbstractClientContainer#connect(
     * org.eclipse.ecf.core.identity.ID,
     * org.eclipse.ecf.core.security.IConnectContext)
     */
    @Override
    public void connect(final ID aTargetID,
            final IConnectContext aConnectContext)
            throws ContainerConnectException {

        // Do the stuff (connection ID)
        super.connect(aTargetID, aConnectContext);

        // Extract class names
        final String[] classNames = extractClassesNames();
        if (classNames == null) {
            throw new ContainerConnectException(
                    "Unreadable service specifications");
        }

        // Prepare a class loader
        final BundlesClassLoader classLoader = new BundlesClassLoader(
                Activator.getContext());

        // Prepare callables and the list of really usable classes
        final Collection<IRemoteCallable[]> callables = new LinkedList<IRemoteCallable[]>();
        final Set<String> loadedClassNames = new LinkedHashSet<String>();

        for (final String className : classNames) {
            try {
                // Load the class
                final Class<?> clazz = classLoader.loadClass(className);

                // Make a callable for all its methods
                final IRemoteCallable[] classCallables = makeCallables(clazz);
                if (classCallables.length > 0) {
                    // Store'em
                    callables.add(classCallables);
                    loadedClassNames.add(className);
                }

            } catch (final ClassNotFoundException ex) {
                // Bad luck
                System.err.println("Couldn't load class " + className);
            }
        }

        if (loadedClassNames.isEmpty()) {
            // Really bad luck
            throw new ContainerConnectException(
                    "Not a single class has been loaded");
        }

        // Make a dictionary from the map
        final Properties properties = new Properties();
        properties.putAll(pServiceProperties);

        // Force the endpoint ID
        forceEndpointServiceId(properties);

        // Register callables
        pRegistration = registerCallables(
                loadedClassNames.toArray(new String[0]),
                callables.toArray(new IRemoteCallable[0][]), properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.client.AbstractClientContainer#
     * createRemoteService
     * (org.eclipse.ecf.remoteservice.client.RemoteServiceClientRegistration)
     */
    @Override
    protected IRemoteService createRemoteService(
            final RemoteServiceClientRegistration aRegistration) {

        try {
            // Prepare a new client service
            return new JabsorbClientService(this, aRegistration);

        } catch (final ECFException ex) {
            // TODO: log exception
            ex.printStackTrace();

            // Return null in case of error
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.client.AbstractClientContainer#disconnect()
     */
    @Override
    public void disconnect() {

        // Let the parent do its job
        super.disconnect();

        // Unregister
        if (pRegistration != null) {
            pRegistration.unregister();
            pRegistration = null;
        }
    }

    /**
     * Extracts the names of the service interfaces
     * 
     * @return A list of class names, null if none found
     */
    private String[] extractClassesNames() {

        // Get classes list
        final Object rawClasses = pServiceProperties.get(Constants.OBJECTCLASS);
        if (rawClasses == null) {
            // Property not found
            return null;

        } else if (rawClasses instanceof String) {
            // Single specification
            return new String[] { (String) rawClasses };

        } else if (rawClasses instanceof String[]) {
            // Standard format
            return (String[]) rawClasses;
        }

        return null;
    }

    private void forceEndpointServiceId(final Properties aProperties) {

        // Service ID of the remote side (OSGi)
        final Object rawEndpointServiceId = aProperties
                .get(RemoteConstants.ENDPOINT_SERVICE_ID);
        if (rawEndpointServiceId == null) {
            // Nothing to do
            return;
        }

        // Convert it to long
        long endpointServiceId;
        try {
            endpointServiceId = ((Long) rawEndpointServiceId).longValue();

        } catch (final ClassCastException ex) {
            System.err.println("Invalid class from endpoint service ID :" + ex);
            return;
        }

        if (endpointServiceId <= 0) {
            // Nothing to do
            return;
        }

        // Set the ECF remote service ID
        aProperties.put(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID,
                endpointServiceId);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.IContainer#getConnectNamespace()
     */
    @Override
    public Namespace getConnectNamespace() {

        return IDFactory.getDefault().getNamespaceByName(
                JabsorbConstants.IDENTITY_NAMESPACE);
    }

    /**
     * Prepares an array of {@link IRemoteCallable} objects from the public
     * methods of the given class
     * 
     * @param aClass
     *            A class object
     * @return An array of callables
     */
    private IRemoteCallable[] makeCallables(final Class<?> aClass) {

        final List<IRemoteCallable> callables = new LinkedList<IRemoteCallable>();

        // Keep a list a methods with the same name
        final Set<String> methodNames = new LinkedHashSet<String>();

        // Make a callable for all its methods
        final String className = aClass.getName();
        for (final Method method : aClass.getMethods()) {

            // Only look at public methods
            if (Modifier.isPublic(method.getModifiers())) {
                final String methodName = method.getName();
                if (methodNames.contains(methodName)) {
                    // Ignore
                    continue;
                }

                // Make the callable
                final IRemoteCallable callable = new RemoteCallable(
                        method.getName(), className, null, null);

                // Store it
                callables.add(callable);
                methodNames.add(methodName);
            }
        }

        return callables.toArray(new IRemoteCallable[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.client.AbstractClientContainer#
     * prepareEndpointAddress(org.eclipse.ecf.remoteservice.IRemoteCall,
     * org.eclipse.ecf.remoteservice.client.IRemoteCallable)
     */
    @Override
    protected String prepareEndpointAddress(final IRemoteCall aCall,
            final IRemoteCallable aCallable) {

        // Not used
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter#
     * setRemoteServiceCallPolicy
     * (org.eclipse.ecf.remoteservice.IRemoteServiceCallPolicy)
     */
    @Override
    public boolean setRemoteServiceCallPolicy(
            final IRemoteServiceCallPolicy aPolicy) {

        // No policy handling
        return false;
    }
}

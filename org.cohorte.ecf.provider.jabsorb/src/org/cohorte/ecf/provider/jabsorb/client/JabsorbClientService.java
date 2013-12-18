/**
 * 
 */
package org.cohorte.ecf.provider.jabsorb.client;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.cohorte.ecf.provider.jabsorb.Activator;
import org.cohorte.ecf.provider.jabsorb.JabsorbConstants;
import org.cohorte.ecf.provider.jabsorb.Utilities;
import org.cohorte.remote.utilities.BundlesClassLoader;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.client.AbstractClientContainer;
import org.eclipse.ecf.remoteservice.client.AbstractClientService;
import org.eclipse.ecf.remoteservice.client.IRemoteCallable;
import org.eclipse.ecf.remoteservice.client.RemoteServiceClientRegistration;
import org.jabsorb.ng.client.Client;
import org.jabsorb.ng.client.ISession;
import org.jabsorb.ng.client.TransportRegistry;

/**
 * Jabsorb remote service client
 * 
 * @author Thomas Calmant
 */
public class JabsorbClientService extends AbstractClientService {

    /** The Jabsorb client */
    private final Client pClient;

    /** Service interfaces */
    private final List<Class<?>> pInterfaces = new LinkedList<Class<?>>();

    /** A classloader that walks through bundles */
    private final ClassLoader pLoader;

    /** The service proxy */
    private final Object pProxy;

    /**
     * Sets up the client
     * 
     * @param aContainer
     * @param aRegistration
     * @throws Exception
     *             Error preparing the proxy
     */
    public JabsorbClientService(final AbstractClientContainer aContainer,
            final RemoteServiceClientRegistration aRegistration)
            throws ECFException {

        super(aContainer, aRegistration);

        // Setup the class loader
        pLoader = new BundlesClassLoader(Activator.getContext());

        // Setup the client
        pClient = setupClient();

        // Setup the proxy
        pProxy = createProxy();
    }

    /**
     * Creates a proxy for this service
     * 
     * @return A proxy object
     * @throws ECFException
     *             Error generating the endpoint name, or no service interface
     *             found
     */
    private Object createProxy() throws ECFException {

        // Load service classes
        pInterfaces.clear();
        for (final String className : registration.getClazzes()) {
            try {
                pInterfaces.add(pLoader.loadClass(className));

            } catch (final ClassNotFoundException ex) {
                // Ignore unknown class
                System.err.println("Class not loaded: " + className);
            }
        }

        // If not class has been loaded, raise an error
        if (pInterfaces.isEmpty()) {
            throw new ECFException("No class found in: "
                    + Arrays.toString(registration.getClazzes()));
        }

        // Create the proxy
        return pClient.openProxy(Utilities.getEndpointName(registration),
                pLoader, pInterfaces.toArray(new Class<?>[0]));
    }

    /**
     * Clean up
     * 
     * @see org.eclipse.ecf.remoteservice.AbstractRemoteService#dispose()
     */
    @Override
    public void dispose() {

        // Close the proxy
        pClient.closeProxy(pProxy);

        // Clean up the list of classes
        pInterfaces.clear();

        super.dispose();
    }

    /**
     * Looks for the first method in the service interfaces that has the same
     * name and number of arguments.
     * 
     * @param aFullMethodName
     *            A method name, that be prefixed with the full class name
     * @return A method object or null
     */
    private Method getMethod(final String aFullMethodName, final int aNbArgs) {

        // Extracted method name
        String methodName;

        // List of classes where to find the method
        final List<Class<?>> classes = new LinkedList<Class<?>>();

        // Separate class and method names
        final int methodIndex = aFullMethodName.lastIndexOf('.');
        if (methodIndex == -1) {
            // Not found, got the method name only
            methodName = aFullMethodName;

            // Look into all classes
            classes.addAll(pInterfaces);

        } else {
            // Separate names
            final String className = aFullMethodName.substring(0, methodIndex);
            methodName = aFullMethodName.substring(methodIndex + 1);

            // Find the valid class
            for (final Class<?> clazz : pInterfaces) {
                if (clazz.getName().equals(className)) {
                    // Look into only one class
                    classes.add(clazz);
                    break;
                }
            }
        }

        // Look for the method object
        for (final Class<?> clazz : pInterfaces) {
            final Method[] methods = clazz.getMethods();
            for (final Method method : methods) {
                // Test method name and number of arguments
                if (Modifier.isPublic(method.getModifiers())
                        && method.getName().equals(methodName)
                        && method.getParameterTypes().length == aNbArgs) {
                    // Found a match
                    return method;
                }
            }
        }

        // Method not found
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ecf.remoteservice.client.AbstractClientService#invokeRemoteCall
     * (org.eclipse.ecf.remoteservice.IRemoteCall,
     * org.eclipse.ecf.remoteservice.client.IRemoteCallable)
     */
    @Override
    protected Object invokeRemoteCall(final IRemoteCall aCall,
            final IRemoteCallable aCallable) throws ECFException {

        // Normalize parameters
        Object[] parameters = aCall.getParameters();
        if (parameters == null) {
            parameters = new Object[0];
        }

        // Look for the method
        final Method method = getMethod(aCall.getMethod(), parameters.length);
        if (method == null) {
            throw new ECFException("Can't find a method called "
                    + aCall.getMethod() + " with " + parameters.length
                    + " arguments");
        }

        try {
            // Call the method
            return pClient.invoke(pProxy, method, aCall.getParameters());

        } catch (final Throwable ex) {
            // Encapsulate the exception
            throw new ECFException("Error calling remote method: "
                    + ex.getMessage(), ex);
        }
    }

    /**
     * Sets up the client according to the registration
     */
    private Client setupClient() {

        // Get the accesses
        final String[] accesses = Utilities.getAccesses((String) registration
                .getProperty(JabsorbConstants.PROP_HTTP_ACCESSES));

        // Get the first one
        // FIXME: get the first **valid** one...
        final String uri = accesses[0];
        System.out.println("Accesses: " + Arrays.toString(accesses));
        System.out.println("Chosen..: " + uri);

        // Prepare the session
        final ISession session = TransportRegistry.i().createSession(uri);

        // Set up the client
        return new Client(session, pLoader);
    }
}

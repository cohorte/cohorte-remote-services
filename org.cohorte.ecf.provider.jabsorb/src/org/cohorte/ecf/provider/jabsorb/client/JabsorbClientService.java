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
package org.cohorte.ecf.provider.jabsorb.client;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.cohorte.ecf.provider.jabsorb.Utilities;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.client.AbstractClientContainer;
import org.eclipse.ecf.remoteservice.client.AbstractRSAClientService;
import org.eclipse.ecf.remoteservice.client.RemoteServiceClientRegistration;
import org.jabsorb.ng.client.Client;
import org.jabsorb.ng.client.ISession;
import org.jabsorb.ng.client.TransportRegistry;
import org.osgi.service.log.LogService;

/**
 * Jabsorb remote service client, based on a Jabsorb {@link Client}
 * 
 * @author Thomas Calmant
 */
public class JabsorbClientService extends AbstractRSAClientService {

    /** The Jabsorb client */
    private final Client pClient;

    /** Service interfaces */
    private final List<Class<?>> pInterfaces = new LinkedList<Class<?>>();

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
                pInterfaces.add(this.getClass().getClassLoader().loadClass(className));

            } catch (final ClassNotFoundException ex) {
                // Ignore unknown class
                Utilities.log(LogService.LOG_WARNING, "createProxy",
                        getClass(), "Class not loaded: " + className);
            }
        }

        // If not class has been loaded, raise an error
        if (pInterfaces.isEmpty()) {
            throw new ECFException("No class found in: "
                    + Arrays.toString(registration.getClazzes()));
        }

        // Create the proxy
        String serviceKey = String.valueOf(registration.getProperty(Constants.SERVICE_ID));
        
        return pClient.openProxy(serviceKey,
                this.getClass().getClassLoader(), pInterfaces.toArray(new Class<?>[pInterfaces.size()]));
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

    /**
     * Sets up the client according to the registration properties
     */
    private Client setupClient() {

        final String uri = (String) registration.getProperty(Constants.ENDPOINT_ID);
        if (uri == null) 
        	throw new NullPointerException("Remote service property "+Constants.ENDPOINT_ID+" is not set");

        // Prepare the session
        final ISession session = TransportRegistry.i().createSession(uri);

        // Set up the client
        return new Client(session);
    }

	@Override
	protected Object invokeAsync(RSARemoteCall remoteCall) throws ECFException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object invokeSync(RSARemoteCall remoteCall) throws ECFException {
        // Normalize parameters
        Object[] parameters = remoteCall.getParameters();
        if (parameters == null) {
            parameters = new Object[0];
        }

        // Look for the method
        final Method method = getMethod(remoteCall.getMethod(), parameters.length);
        if (method == null) {
            throw new ECFException("Can't find a method called "
                    + remoteCall.getMethod() + " with " + parameters.length
                    + " arguments");
        }

        try {
            // Call the method
            return pClient.invoke(pProxy, method, remoteCall.getParameters());

        } catch (final Throwable ex) {
            // Encapsulate the exception
            throw new ECFException("Error calling remote method: "
                    + ex.getMessage(), ex);
        }
	}
}

/**
 * Copyright 2013 isandlaTech
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
package org.cohorte.remote.utilities;

import javax.swing.text.Utilities;

import org.osgi.framework.BundleContext;

/**
 * "Fake" Class loader, trying to load a class that could be in one of the
 * active/resolved bundles
 * 
 * @author Thomas Calmant
 */
public class BundlesClassLoader extends ClassLoader {

    /** The bundle context */
    private final BundleContext pBundleContext;

    /**
     * Prepares the "class loader"
     * 
     * @param aBundleContext
     *            The bundle context
     */
    public BundlesClassLoader(final BundleContext aBundleContext) {

        super();
        pBundleContext = aBundleContext;
    }

    /**
     * Tries to load the given class in the 'local' class loader, then in all
     * bundles.
     * 
     * @param aName
     *            The class to be loaded
     * @return The loaded class
     * @throws ClassNotFoundException
     *             The class could not be found
     * 
     * @see Utilities#findClassInBundles(org.osgi.framework.Bundle[], String)
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass(final String aName) throws ClassNotFoundException {

        // Try the "local" class loader first
        try {
            return Class.forName(aName);

        } catch (final ClassNotFoundException ex) {
            /*
             * The class has not been found in the current class loader, look
             * somewhere else
             */

        } catch (final LinkageError e) {
            /*
             * The class exists in the current class loader, but the OSGi
             * framework forbids to load it or one of its dependencies. Try
             * another one.
             */
        }

        // Try the bundles loaders
        final BundleClass foundClass = BundleClass.findClassInBundles(
                pBundleContext.getBundles(), aName, true);
        if (foundClass == null) {
            throw new ClassNotFoundException("Class not found: " + aName);
        }

        return foundClass.getLoadedClass();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "BundleLoader(" + super.toString() + ")";
    }
}

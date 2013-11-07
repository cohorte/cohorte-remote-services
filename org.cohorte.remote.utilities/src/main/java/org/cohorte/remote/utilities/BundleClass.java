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

import org.osgi.framework.Bundle;

/**
 * Stores a reference to a class loaded from a bundle, and to its source bundle
 * 
 * @author Thomas Calmant
 */
public class BundleClass {

    /**
     * Tries to load the given class by looking into all available bundles.
     * 
     * @param aBundles
     *            An array containing all bundles to search into
     * @param aClassName
     *            Name of the class to load
     * @param aAllowResolvedBundles
     *            Allows to look into bundles in RESOLVED state
     * @return A BundleClass instance, null if not found
     */
    public static BundleClass findClassInBundles(final Bundle[] aBundles,
            final String aClassName, final boolean aAllowResolvedBundles) {

        if (aBundles == null) {
            // No bundles to look into
            return null;
        }

        // Prepare the state mask
        int stateMask = Bundle.ACTIVE;
        if (aAllowResolvedBundles) {
            stateMask |= Bundle.RESOLVED;
        }

        for (final Bundle bundle : aBundles) {
            // Check if the bundle state passes the mask
            final int bundleState = bundle.getState();
            if ((bundleState | stateMask) != 0) {
                try {
                    final Class<?> clazz = bundle.loadClass(aClassName);
                    return new BundleClass(bundle, clazz);

                } catch (final ClassNotFoundException e) {
                    // Class not found, try next bundle...
                }
            }
        }

        return null;
    }

    /** The bundle providing the class */
    private final Bundle pBundle;

    /** The loaded class */
    private final Class<?> pClass;

    /**
     * Sets up the bean
     * 
     * @param aBundle
     *            The bundle providing the class
     * @param aClass
     *            The loaded class
     */
    public BundleClass(final Bundle aBundle, final Class<?> aClass) {

        pBundle = aBundle;
        pClass = aClass;
    }

    /**
     * Retrieves the bundle providing the class
     * 
     * @return the bundle
     */
    public Bundle getBundle() {

        return pBundle;
    }

    /**
     * Retrieves the loaded class
     * 
     * @return the loaded class
     */
    public Class<?> getLoadedClass() {

        return pClass;
    }
}

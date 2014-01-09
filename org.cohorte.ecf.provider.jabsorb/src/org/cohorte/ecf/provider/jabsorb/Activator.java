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

import org.eclipse.ecf.core.util.SystemLogService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Keeps track of the bundle context
 * 
 * @author Thomas Calmant
 */
public class Activator implements BundleActivator {

    /** The ID of this plugin */
    public final static String PLUGIN_ID = "org.cohorte.ecf.provider.jabsorb";

    /** This activator */
    private static Activator sSingleton;

    /**
     * Returns the activator singleton
     * 
     * @return the activator instance
     */
    public static Activator get() {

        return sSingleton;
    }

    /** The {@link LogService} tracker */
    private ServiceTracker<LogService, LogService> logServiceTracker = null;

    /** Bundle context */
    private BundleContext pContext;

    /**
     * Retrieves the bundle context
     * 
     * @return the bundle context
     */
    public BundleContext getContext() {

        return pContext;
    }

    /**
     * Retrieves a log service
     * 
     * @return A {@link LogService}
     */
    private synchronized LogService getLogService() {

        if (logServiceTracker == null) {
            logServiceTracker = new ServiceTracker<LogService, LogService>(
                    pContext, LogService.class, null);
            logServiceTracker.open();
        }

        LogService logService = logServiceTracker.getService();
        if (logService == null) {
            logService = new SystemLogService(PLUGIN_ID);
        }

        return logService;
    }

    /**
     * Logs a message using the log service
     * 
     * @param aLevel
     * @param aMessage
     * @param aThrowable
     */
    public void log(final int aLevel, final String aMessage,
            final Throwable aThrowable) {

        getLogService().log(aLevel, aMessage, aThrowable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(final BundleContext bundleContext) {

        sSingleton = this;
        pContext = bundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext bundleContext) {

        if (logServiceTracker != null) {
            logServiceTracker.close();
            logServiceTracker = null;
        }

        sSingleton = null;
        pContext = null;
    }
}

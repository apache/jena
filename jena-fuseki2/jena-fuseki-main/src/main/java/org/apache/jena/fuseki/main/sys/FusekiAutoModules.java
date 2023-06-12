/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.fuseki.main.sys;

import java.util.List;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.Version;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.slf4j.Logger;

/**
 * Control of {@link FusekiAutoModule} found via {@link ServiceLoader}.
 */
public class FusekiAutoModules {

    private static final Logger LOG = Fuseki.serverLog;
    private static final Object lock = new Object();

    public static final String logLoadingProperty = "fuseki.logLoading";
    public static final String envLogLoadingProperty = "FUSEKI_LOGLOADING";

    private static boolean allowDiscovery = true;
    private static boolean enabled = true;
    private static FusekiModules altFusekiModules = null;

    /*package*/ static boolean logModuleLoading() {
        return Lib.isPropertyOrEnvVarSetToTrue(logLoadingProperty, envLogLoadingProperty);
    }

    /*package*/ static Logger logger() {
        return LOG;
    }

    /**
     * Enable/disable discovery of modules using the service loader.
     * The default is 'enabled'.
     */
    public static void enable(boolean setting) {
        enabled = setting;
    }

    /** Whether the system loaded modules are enabled. */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Setup discovery of modules using the service loader.
     * This replaces any current setup.
     */
    public static void setup() {
        if ( ! isEnabled() )
            return;
        // Setup and discover now
        autoModules = createServiceLoaderModules();
    }

    /**
     * Load the the system wide Fuseki modules if it has not already been loaded.
     * If disabled, return an empty  FusekiModules
     */
    public static FusekiModules load() {
        if ( ! enabled )
            return FusekiModules.empty();
        if ( altFusekiModules != null )
            return altFusekiModules;
        return getServiceLoaderModules().load();
    }

    // -- ServiceLoader machinery.

    // testing
    /*package*/ static void reset() {
        autoModules = null;
    }

    /**
     * Ignore any discovered modules and use the given FusekiModules.
     * Pass null to clear a previous setting.
     */
    static void setSystemDefault(FusekiModules fusekiModules) {
        altFusekiModules = fusekiModules;
    }

    // Single auto module controller.
    private static FusekiServiceLoaderModules autoModules = null;

    private static FusekiServiceLoaderModules getServiceLoaderModules() {
        if ( autoModules == null )
            setup();
        return autoModules;
    }

    private static FusekiServiceLoaderModules createServiceLoaderModules() {
        FusekiServiceLoaderModules newAutoModules = new FusekiServiceLoaderModules();
        newAutoModules.setDiscovery();
        return newAutoModules;
    }

    /**
     * Use {@link java.util.ServiceLoader} to find {@link FusekiModule}
     * available via the classpath or modules.
     * <p>
     * These are the modules used when building a {@link FusekiServer} if
     * {@link FusekiServer.Builder#setFusekiModules} is not used.
     */
    private static class FusekiServiceLoaderModules {

        // This keeps the list of discovered Fuseki modules.
        private ServiceLoader<FusekiAutoModule> serviceLoader = null;

        private FusekiServiceLoaderModules() { }

        private void setDiscovery() {
            serviceLoader = discover();
        }

        /**
         * Discover FusekiModules via {@link java.util.ServiceLoader}.
         * This step does not create the module objects.
         */
        private ServiceLoader<FusekiAutoModule> discover() {
            // Look for the 4.8.0 name (FusekiModule) which (4.9.0) is split into
            // FusekiModule (interface) and FusekiAutoModule (this is loaded by ServiceLoader)
            // Remove sometime!
            discoveryWarnLegacy();

            Class<FusekiAutoModule> moduleClass = FusekiAutoModule.class;
            ServiceLoader<FusekiAutoModule> newServiceLoader = null;
            synchronized (this) {
                try {
                    newServiceLoader = ServiceLoader.load(moduleClass, this.getClass().getClassLoader());
                } catch (ServiceConfigurationError ex) {
                    FmtLog.error(LOG, ex, "Problem with service loading for %s", moduleClass.getName());
                    throw ex;
                }
                if ( LOG.isDebugEnabled() ) {
                    newServiceLoader.stream().forEach(provider->{
                        FmtLog.info(LOG, "Fuseki Module: %s", provider.type().getSimpleName());
                    });
                }
            }
            return newServiceLoader;
        }

        private void discoveryWarnLegacy() {
            Class<FusekiModule> moduleClass = FusekiModule.class;
            try {
                ServiceLoader<FusekiModule> newServiceLoader = ServiceLoader.load(moduleClass, this.getClass().getClassLoader());
                newServiceLoader.stream().forEach(provider->{
                    FmtLog.warn(FusekiAutoModules.class, "Ignored: \"%s\" : legacy use of interface FusekiModule which has changed to FusekiAutoModule", provider.type().getSimpleName());
                });
            } catch (ServiceConfigurationError ex) {
                // Ignore - we were only checking.
            }
        }

        /**
         * Instantiate modules found using the ServiceLoader.
         * Each call to {@code load()} creates a new object for the FusekiModule.
         * {@code start()} on each module has not been called.
         */
        private FusekiModules load() {
            if ( serviceLoader == null ) {
                FmtLog.error(LOG, "Discovery step has not happened or it failed. Call FusekiSystemModules.discovery before FusekiSystemModules.load()");
                throw new FusekiConfigException("Discovery not performed");
            }

            Function<ServiceLoader.Provider<FusekiAutoModule>, FusekiAutoModule> mapper = provider -> {
                try {
                    FusekiAutoModule afmod =  provider.get();
                    return afmod;
                } catch (ServiceConfigurationError ex) {
                    FmtLog.error(LOG, ex,
                                 "Error instantiating class %s for %s", provider.type().getName(), FusekiModule.class.getName());
                    return null;
                }
            };

            // Create auto module object, skip loads in error, sort automodules into level a order.
            List<FusekiAutoModule> autoMods = serviceLoader.stream()
                    .map(mapper)
                    .filter(Objects::nonNull)
                    .sorted((x,y)-> Integer.compare(x.level(), y.level()))
                    .collect(Collectors.toList());
            // Start, and convert to FusekeiModules (generics issue)
            List<FusekiModule> fmods = autoMods.stream().map(afmod->{
                afmod.start();
                return afmod;
            }).collect(Collectors.toList());

            fmods.forEach(m->{
                String name = m.name();
                if ( name == null )
                    name = m.getClass().getSimpleName();
                FmtLog.info(LOG, "Module: %s (%s)",
                                 name,
                                 Version.versionForClass(m.getClass()).orElse("unknown"));
            });

            return FusekiModules.create(fmods);
        }
    }
}

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

import org.apache.jena.atlas.lib.Version;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.slf4j.Logger;

/**
 * Management of {@link FusekiAutoModule automatically loaded modules} found via {@link ServiceLoader}.
 */
public class FusekiAutoModules {

    private static final Logger LOG = Fuseki.serverLog;
    private static final Object lock = new Object();

    // Remember last loading.
    private static FusekiModules currentLoadedModules = null;
    // ServiceLoader
    private static ServiceLoader<FusekiAutoModule> serviceLoader = null;

    /**
     * Return the current (last loaded) Fuseki auto-modules.
     */
    static FusekiModules get() {
        if ( currentLoadedModules != null )
            return currentLoadedModules;
        synchronized(lock) {
            if ( currentLoadedModules == null )
                load();
            return currentLoadedModules;
        }
    }

    /**
     * Load FusekiAutoModules. This call reloads the modules every call.
     */
    private static FusekiModules load() {
        synchronized (lock) {
            if ( serviceLoader == null )
                serviceLoader = createServiceLoader();
            currentLoadedModules = loadAutoModules(serviceLoader);
        }
        return currentLoadedModules;
    }

    /**
     * For testing only.
     */
    static void reset() {
        currentLoadedModules = null;
    }

    // -- ServiceLoader machinery.

    /**
     * Discover FusekiModules via {@link java.util.ServiceLoader}.
     * This step does not create the module objects.
     */
    private static ServiceLoader<FusekiAutoModule> createServiceLoader() {
        Class<FusekiAutoModule> moduleClass = FusekiAutoModule.class;
        ServiceLoader<FusekiAutoModule> newServiceLoader = null;
        synchronized (lock) { // Not necessary if createServiceLoader() only called from load(). But harmless.
            try {
                newServiceLoader = ServiceLoader.load(moduleClass, FusekiAutoModules.class.getClassLoader());
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

    /**
     * Instantiate modules found using a ServiceLoader.
     * Each call to {@code load()} creates a new object for the FusekiModule.
     * {@code start()} on each module has not been called.
     */
    private static FusekiModules loadAutoModules(ServiceLoader<FusekiAutoModule> serviceLoader) {
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

        // Create auto-module object, skip loads in error, sort auto-modules into level order.
        List<FusekiAutoModule> autoMods = serviceLoader.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .sorted((x,y)-> Integer.compare(x.level(), y.level()))
                .toList();
        // Start, and convert to FusekiModules (generics issue)
        List<FusekiModule> fmods = autoMods.stream().map(afmod->{
            afmod.start();
            return (FusekiModule)afmod;
        }).toList();

        fmods.forEach(m->{
            String name = m.name();
            if ( name == null )
                name = m.getClass().getSimpleName();
            String verStr = Version.versionForClass(m.getClass()).orElse(null);
            if ( verStr == null )
                FmtLog.info(LOG, "Module: %s", name);
            else
                FmtLog.info(LOG, "Module: %s (%s)", name, verStr);
        });

        return FusekiModules.create(fmods);
    }
}

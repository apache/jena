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

import org.apache.jena.atlas.lib.Version;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.slf4j.Logger;

/**
 * Use {@link java.util.ServiceLoader} to find {@link FusekiModule}
 * available via the classpath or modules.
 * <p>
 * These are the modules used when building a {@link FusekiServer} if
 * {@link FusekiServer.Builder#setFusekiModules} is not used.
 */

class FusekiSystemModules {

    private static Logger LOG = FusekiModulesCtl.logger();
    private static FusekiSystemModules modules = null;

    /*package*/ static void reset() {
        modules = null;
    }

    /*package*/ static void setup() {
        // Setup and discover now (normal route from InitFuseki)
        modules = new FusekiSystemModules();
    }

    public static FusekiSystemModules get() {
        if ( modules == null && FusekiModulesCtl.isEnabled() )
            modules = new FusekiSystemModules();
        return modules;
    }

    // This keeps the list of discovered Fuseki modules.
    private ServiceLoader<FusekiModule> serviceLoader = null;

    private FusekiSystemModules() {
        serviceLoader = discover();
    }

    /**
     * Discover FusekiModules via {@link java.util.ServiceLoader}.
     * This step does not create the module objects.
     */
    public ServiceLoader<FusekiModule> discover() {
        Class<FusekiModule> moduleClass = FusekiModule.class;
        ServiceLoader<FusekiModule> newServiceLoader = null;
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

    /**
     * Instantiate modules found using the ServiceLoader.
     * Each call to {@code load()} creates a new object for the FusekiModule.
     * {@code start()} on each module has not been called.
     */
    public FusekiModules load() {
        if ( serviceLoader == null ) {
            FmtLog.error(LOG, "Discovery step has not happened or it failed. Call FusekiSystemModules.discovery before FusekiSystemModules.load()");
            throw new FusekiConfigException("Discovery not performed");
        }

        Function<ServiceLoader.Provider<FusekiModule>, FusekiModule> mapper = provider -> {
            try {
                FusekiModule fmod =  provider.get();
                return fmod;
            } catch (ServiceConfigurationError ex) {
                FmtLog.error(LOG, ex,
                             "Error instantiating class %s for %s", provider.type().getName(), FusekiModule.class.getName());
                return null;
            }
        };

        List<FusekiModule> fmods = serviceLoader.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .sorted((x,y)-> Integer.compare(x.level(), y.level()))
                .collect(Collectors.toList());
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

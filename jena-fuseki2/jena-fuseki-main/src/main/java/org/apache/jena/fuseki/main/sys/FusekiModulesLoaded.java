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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.base.module.Subsystem;
import org.apache.jena.fuseki.Fuseki;
import org.slf4j.Logger;

/** Control of fuseki modules loaded via ServiceLoader */
public class FusekiModulesLoaded {

    private static final Logger LOG = Fuseki.serverLog;
    private static final Object lock = new Object();

    // Record of what is loaded.
    private static List<FusekiModule> loadedModules = null;

    // Loaded modules as FusekiModules
    private static FusekiModules loaded = null;

    public static final String logLoadingProperty = "fuseki.logLoading";
    public static final String envLogLoadingProperty = "FUSEKI_LOGLOADING";
    private static boolean enabled = true;

    private static boolean logModuleLoading() {
        return Lib.isPropertyOrEnvVarSetToTrue(logLoadingProperty, envLogLoadingProperty);
    }

    /** Enable/disable loaded modules. */
    public static void enable(boolean setting) {
        synchronized(lock) {
            if ( setting ) {
                if ( ! enabled )
                    reload();
            } else {
                // Clear.
                loadedModules.forEach(FusekiModule::stop);
                loadedModules = List.of();
                loaded = FusekiModules.empty;
            }
            enabled = setting;
        }
    }

    /* package */ static void init() {
        load();
        FusekiModulesSystem.set(loaded);
    }

    /** Whether the system loaded modules are enabled. */
    public static boolean isEnabled() {
        return enabled;
    }

    /** The system wide list of Fuseki modules. */
    public static FusekiModules loaded() {
        return loaded;
    }

    /** Load the the system wide Fuseki modules if it has not already been loaded. */
    public static void load() {
        if ( ! enabled )
            return;
        if ( loadedModules == null )
            reload();
    }

    /**
     * Load and set system wide Fuseki modules.
     */
    public static void reload() {
        boolean logProcess = logModuleLoading();
        synchronized(lock) {
            List<FusekiModule> thisLoad = new ArrayList<>();
            Subsystem<FusekiModule> subsystem = new Subsystem<FusekiModule>(FusekiModule.class);
            if ( logProcess )
                subsystem.debug(true);
            subsystem.initialize();
            subsystem.forEach(thisLoad::add);
            loadedModules = List.copyOf(thisLoad);
            if ( logProcess ) {
                if ( loadedModules.isEmpty() )
                    LOG.info("No modules loaded");
                else
                    loadedModules.forEach(x->FmtLog.info(LOG, "Loaded: %s", x.name()));
            }

            loaded = FusekiModules.create(loadedModules);
            enabled = true;
        }
    }

    /** Reload modules and set as the system modules. */
    public static void resetSystem() {
        reload();
        FusekiModulesSystem.set(loaded);
    }
}

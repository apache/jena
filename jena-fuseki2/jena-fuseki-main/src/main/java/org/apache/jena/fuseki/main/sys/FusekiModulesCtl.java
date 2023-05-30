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

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.fuseki.Fuseki;
import org.slf4j.Logger;

/**
 * Control of fuseki modules found via ServiceLoader after loading.
 */
public class FusekiModulesCtl {

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

    /** Setup discovery of modules using the service loader. */
    public static void setup() {
        if ( isEnabled() )
            FusekiSystemModules.setup();
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
        return FusekiSystemModules.get().load();
    }

    /**
     * Ignore any discovered modules and use the given FusekiModules.
     * Pass null to clear a previous setting.
     */
    static void setSystemDefault(FusekiModules fusekiModules) {
        altFusekiModules = fusekiModules;
    }
}

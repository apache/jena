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

import java.util.Set;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.rdf.model.Model;

/** Call points for FusekiModule extensions */
public class FusekiModuleStep {

    /**
     * Call at the start of "build" step.
     * The builder has been set according to the configuration.
     * The "configModel" parameter is set if a configuration file was used else it is null.
     */
    public static void prepare(FusekiModules modules, FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) {
        modules.forEach(module -> module.prepare(serverBuilder, datasetNames, configModel));
    }

    /**
     * The DataAccessPointRegistry that will be used to build the server.
     *
     */
    public static void configured(FusekiModules modules, FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        modules.forEach(module -> module.configured(serverBuilder, dapRegistry, configModel));
    }

    /**
     * The outcome of the "build" step.
     */
    public static void server(FusekiServer server) {
        server.getModules().forEach(module -> module.server(server));
    }

    /**
     * Called just before {@code server.start()} called.
     */
    public static void serverBeforeStarting(FusekiServer server) {
        server.getModules().forEach(module -> module.serverBeforeStarting(server));
    }

    /**
     * Called just after {@code server.start()} called.
     */
    public static void serverAfterStarting(FusekiServer server) {
        server.getModules().forEach(module -> module.serverAfterStarting(server));
    }

    /**
     * Called just after {@code server.stop()} has happened.
     * Often, this is not called - the server, or application containing the server,
     * simply exits the JVM or is killed externally.
     */
    public static void serverStopped(FusekiServer server) {
        server.getModules().forEach(module -> module.serverStopped(server));
    }

    /**
     * Server reload.
     * Return true if reload happened, else false.
     * @see FusekiBuildCycle#serverConfirmReload
     * @see FusekiBuildCycle#serverReload
     */
    public static boolean serverReload(FusekiServer server) {
        for ( FusekiModule fmod : server.getModules().asList() ) {
            if ( ! fmod.serverConfirmReload(server) ) {
                FmtLog.warn(Fuseki.configLog, "Can not reload : module %s", fmod.name());
                return false;
            }
        }
        server.getModules().forEach(module -> module.serverReload(server));
        return true;
    }
}

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

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.rdf.model.Model;

public class FusekiMonitor {

    /**
     * Call at the start of "build" step.
     * The builder has been set according to the configuration.
     * The "configModel" parameter is set if a configuration file was used else it is null.
     */
    public static void configuration(FusekiServer.Builder builder, DataAccessPointRegistry dapRegistry, Model configModel) {
        FusekiModules.forEachModule((n,m) -> m.configuration(builder, dapRegistry, configModel));
    }

    /**
     * The outcome of the "build" step.
     */
    public static void server(FusekiServer server) {
        FusekiModules.forEachModule((n,m) -> m.server(server));
    }

    /**
     * Called just before {@code server.start()} called.
     */
    public static void serverStarting(FusekiServer server) {
        FusekiModules.forEachModule((n,m) -> m.serverStarting(server));
    }

    /**
     * Called just after {@code server.start()} called.
     */
    public static void serverStarted(FusekiServer server) {
        FusekiModules.forEachModule((n,m) -> m.serverStarting(server));
    }

    /**
     * Called just after {@code server.stop()} has happened.
     * Often, this is not called - the server, or application containing the server,
     * simply exits the JVM or is killed externally.
     */
    public static void serverStopped(FusekiServer server) {
        FusekiModules.forEachModule((n,m) -> m.serverStopped(server));
    }
}

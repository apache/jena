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

import java.util.UUID;

import org.apache.jena.base.module.SubsystemLifecycle;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.rdf.model.Model;

/**
 * Module interface for Fuseki.
 * <p>
 * A module is additional code, usually in a separate jar, but can also be part of the application code.
 */
public interface FusekiModule extends SubsystemLifecycle {
    /**
     * Unique (within this server) name to identify this module.
     * The default is to generate an UUID.
     */
    public default String name() { return UUID.randomUUID().toString(); }

    /** Module loaded */
    @Override
    public default void start() {}

    // ---- Build cycle

    /**
     * Call at the start of "build" step.
     * The builder has been set according to the configuration.
     * The "configModel" parameter is set if a configuration file was used else it is null.
     */
    public default void configuration(FusekiServer.Builder builder, DataAccessPointRegistry dapRegistry, Model configModel) {}

    /** Built, not started, about to be returned to the builder caller. */
    public default void server(FusekiServer server) { }

    /** Server starting - called just before server.start happens. */
    public default void serverStarting(FusekiServer server) { }

    /** Server started - called just after server.start happens,before server, .start() returns to the application,. */
    public default void serverStarted(FusekiServer server) { }

    /** Server stopping.
     * Do not rely on this to clear up external resources.
     * Usually there is no stop phase and the JVM just exits or is killed externally.
     *
     */
    public default void serverStopped(FusekiServer server) { }

    /** Module unloaded */
    @Override
    public default void stop() {}

    // Maybe later.
//    // ---- Execution.
//
//    public default void action(HttpAction action) { }
//    public default void action(Endpoint endpoint) { }
}

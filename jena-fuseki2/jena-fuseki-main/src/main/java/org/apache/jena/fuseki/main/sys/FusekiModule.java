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

import org.apache.jena.base.module.SubsystemLifecycle;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.rdf.model.Model;

/**
 * Module interface for Fuseki.
 * <p>
 * A module is additional code, usually in a separate jar, but it can be part of
 * the application code. Calls are made to each module at certain points in the
 * lifecycle of a Fuseki server.
 * <p>
 *  A module must provide a no-argument constructor if it is to be loaded automatically.
 * <ul>
 * <li>{@linkplain #start()} - called when the module is loaded.</li>
 * <li>{@linkplain #configuration} -- called at the beginning of the
 *     {@link org.apache.jena.fuseki.main.FusekiServer.Builder#build() FusekiServer.Builder build()}
 *      step. This call can manipulate the server configuration.</li>
 * <li>{@linkplain #server(FusekiServer)} -- called at the end of the "build" step.</li>
 * <li>{@linkplain #serverBeforeStarting(FusekiServer)} -- called before {@code server.start} happens.</li>
 * <li>{@linkplain #serverAfterStarting(FusekiServer)} -- called after {@code server.start} happens.</li>
 * <li>{@linkplain #serverStopped(FusekiServer)} -- call after {@code server.stop}, but only if a clean shutdown happens.
 *     Servers may simply exit without shutdown phase.
 *     The JVM may exit or be killed without clean shutdown.
 *     Modules must not rely on a call to {@code serverStopped} happening.</li>
 * </ul>
 */
public interface FusekiModule extends SubsystemLifecycle {
    /**
     * Display name id to identify this module.
     * <p>
     * Modules are loaded once by the service loader
     * <p>
     * Modules added programmatically should be added once only.
     */
    public String name();

    /** Module loaded */
    @Override
    public default void start() { }

    // ---- Build cycle

    /**
     * Called at the start of "build" step. The builder has been set according to the
     * configuration. The "configModel" parameter is set if a configuration file was
     * used otherwise it is null.
     * <p>
     * The default implementation is to call
     * {@link #configDataAccessPoint(FusekiServer.Builder, DataAccessPoint, Model)}
     * for each {@link DataAccessPoint}.
     * <pre>
     *   dapRegistry.accessPoints().forEach(accessPoint->configDataAccessPoint(builder, accessPoint, configModel));
     * </pre>
     * <p>
     * If overriding this method, the implementation can invoke this iteration by calling
     * {@code FusekiModule.super.configuration(builder, dapRegistry, configModel)}.
     */
    public default void configuration(FusekiServer.Builder builder, DataAccessPointRegistry dapRegistry, Model configModel) {
        dapRegistry.accessPoints().forEach(accessPoint->configDataAccessPoint(builder, accessPoint, configModel));
    }

    /**
     * This method is called for each {@link DataAccessPoint}
     * by the default implementation of {@link #configuration}.
     */
    public default void configDataAccessPoint(FusekiServer.Builder builder, DataAccessPoint dap, Model configModel) {}

    /**
     * Built, not started, about to be returned to the builder caller.
     */
    public default void server(FusekiServer server) { }

    /**
     * Server starting - called just before server.start happens.
     */
    public default void serverBeforeStarting(FusekiServer server) { }

    /**
     * Server started - called just after server.start happens, and before server
     * .start() returns to the application.
     */
    public default void serverAfterStarting(FusekiServer server) { }

    /** Server stopping.
     * Do not rely on this to clear up external resources.
     * Usually there is no stop phase and the JVM just exits or is killed externally.
     *
     */
    public default void serverStopped(FusekiServer server) { }

    /** Module unloaded : do not rely on this happening. */
    @Override
    public default void stop() {}

    // Maybe later.
//    // ---- Execution.
//
//    public default void action(HttpAction action) { }
//    public default void action(Endpoint endpoint) { }
}

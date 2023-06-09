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

import org.apache.jena.base.module.SubsystemLifecycle;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.rdf.model.Model;

/**
 * Module interface for Fuseki with loading via ServiceLoader from mixin jars.
 * <p>
 * A module is additional code, usually in a separate jar, but it can be part of
 * the application code. Calls are made to each module at certain points in the
 * lifecycle of a Fuseki server.
 * <p>
 * A module must provide a no-argument constructor if it is to be loaded automatically.
 * <p>
 *
 * Automatically loaded Fuseki modules:
 * <ul>
 * <li>{@linkplain #start()} -- called when the module is loaded and instantiated.</li>
 * <li>{@linkplain #stop} -- modules finishes. This is unlikely to be called in practice and there is no guarantee of a clean shutdown.
 * </ul>
 * When a server is being built:
 * * <ul>
 * <li>{@linkplain #prepare}
 *      -- called at the beginning of the
 *     {@link org.apache.jena.fuseki.main.FusekiServer.Builder#build() FusekiServer.Builder build()}
 *      step. This call can manipulate the server configuration. This is the usual operation for customizing a server.</li>
 * <li>{@linkplain #configured} -- called after the DataAccessPoint registry has been built.</li>
 * <li>{@linkplain #server(FusekiServer)} -- called at the end of the "build" step before
 *     {@link org.apache.jena.fuseki.main.FusekiServer.Builder#build() FusekiServer.Builder build()}
 *     returns.</li>
 * </ul>
 * At server start-up:
 * <ul>
 * <li>{@linkplain #serverBeforeStarting(FusekiServer)} -- called before {@code server.start} happens.</li>
 * <li>{@linkplain #serverAfterStarting(FusekiServer)} -- called after {@code server.start} happens.</li>
 * <li>{@linkplain #serverStopped(FusekiServer)} -- call after {@code server.stop}, but only if a clean shutdown happens.
 *     Servers may simply exit without a shutdown phase.
 *     The JVM may exit or be killed without clean shutdown.
 *     Modules must not rely on a call to {@code serverStopped} happening.</li>
 * </ul>
 */
public interface FusekiAutoModule extends FusekiModule, SubsystemLifecycle {
    // Inherited javadoc.
    // Methods mentioned here to show all method interfaces in one place.

    @Override
    public String name();

    // ---- SubsystemLifecycle

    @Override
    public default void start() { }

    @Override
    public default void stop() {}

    // ---- Build cycle

    @Override
    public default void prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) { }

    @Override
    public default void configured(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        dapRegistry.accessPoints().forEach(accessPoint->configDataAccessPoint(accessPoint, configModel));
    }

    @Override
    public default void configDataAccessPoint(DataAccessPoint dap, Model configModel) {}

    @Override
    public default void server(FusekiServer server) { }

    @Override
    public default void serverBeforeStarting(FusekiServer server) { }

    // ---- Server start-stop.

    @Override
    public default void serverAfterStarting(FusekiServer server) { }

    @Override
    public default void serverStopped(FusekiServer server) { }
}

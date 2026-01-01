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

import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.runner.ServerArgs;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.rdf.model.Model;

/**
 * Module extension interface for Fuseki.
 * <p>
 * A module is additional code and it can be part of the application code.
 * Calls are made to each module at certain points in the lifecycle of a Fuseki server.
 * <p>
 * A module must provide a no-argument constructor if it is to be loaded automatically.
 * See {@link FusekiAutoModule}.
 * <p>
 * When a server is being built {@link FusekiBuildCycle}:
 * <ul>
 * <li>{@linkplain #prepare}
 *      -- called at the beginning of the
 *     {@link org.apache.jena.fuseki.main.FusekiServer.Builder#build() FusekiServer.Builder build()}
 *      step. This call can manipulate the server configuration. This is the usual operation for customizing a server.</li>
 * <li>{@linkplain #configured} &ndash; called after the DataAccessPoint registry has been built.</li>
 * <li>{@linkplain #server(FusekiServer)} &ndash; called at the end of the "build" step before
 *     {@link org.apache.jena.fuseki.main.FusekiServer.Builder#build() FusekiServer.Builder build()}
 *     returns.</li>
 * </ul>
 * At server start-up and stopping{@link FusekiStartStop}:
 * <ul>
 * <li>{@linkplain #serverBeforeStarting(FusekiServer)} &ndash; called before {@code server.start} happens.</li>
 * <li>{@linkplain #serverAfterStarting(FusekiServer)} &ndash; called after {@code server.start} happens.</li>
 * <li>{@linkplain #serverStopped(FusekiServer)} &ndash; call after {@code server.stop}, but only if a clean shutdown happens.
 *     N.B. Servers may simply exit without a shutdown phase.
 *     The JVM may exit or be killed without clean shutdown.
 *     Modules must not rely on a call to {@code serverStopped} happening.</li>
 * </ul>
 * Modules can also be involed in command line argument processing {@link FusekiServerArgsHandler}:
 * <ul>
 * <li>
 *    {@link #serverArgsModify} &ndash; called before command line processing.
 *    This call can register or modify the argument setup to be used to parse the command line.
 * </li>
 * <li>
 *   {@link #serverArgsPrepare} &ndash; called after parsing the command line and
 *   recording the command line settings in {@link ServerArgs}.
 *   Argument handlers can record their own argument values and flags.
 * </li>
 * <li>
 *   {@link #serverArgsBuilder} &ndash; called after the {@link ServerArgs} have
 *   been used to construct a server builder.
 * </li>
 * </ul>
 */
public interface FusekiModule extends FusekiServerArgsHandler, FusekiBuildCycle, FusekiStartStop, FusekiActionCycle {
    // Gather all interface methods together.
    // Inherited javadoc.

    /**
     * A display name to identify this module.
     * The name defaults to the class simple name.
     */
    public default String name() { return this.getClass().getSimpleName(); }

    // ---- FusekiServerArgsHandler

    /** {@inheritDoc} */
    @Override
    public default void serverArgsModify(CmdGeneral fusekiCmd, ServerArgs serverArgs) { }

    /** {@inheritDoc} */
    @Override
    public default void serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs) { }

    /** {@inheritDoc} */
    @Override
    public default void serverArgsBuilder(FusekiServer.Builder serverBuilder, Model configModel) {}

    // ---- FusekiBuildCycle

    /** {@inheritDoc} */
    @Override
    public default void prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) { }

    /** {@inheritDoc} */
    @Override
    public default void configured(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        dapRegistry.accessPoints().forEach(accessPoint->configDataAccessPoint(accessPoint, configModel));
    }

    /** {@inheritDoc} */
    @Override
    public default void configDataAccessPoint(DataAccessPoint dap, Model configModel) {}

    /** {@inheritDoc} */
    @Override
    public default void server(FusekiServer server) { }

    /** {@inheritDoc} */
    @Override
    public default boolean serverConfirmReload(FusekiServer server) { return true; }

    /** {@inheritDoc} */
    @Override
    public default void serverReload(FusekiServer server) { }

    // ---- FusekiStartStop

    /** {@inheritDoc} */
    @Override
    public default void serverBeforeStarting(FusekiServer server) { }

    /** {@inheritDoc} */
    @Override
    public default void serverAfterStarting(FusekiServer server) { }

    /** {@inheritDoc} */
    @Override
    public default void serverStopped(FusekiServer server) { }

    // ---- FusekiActionCycle
    // Currently, a placeholder.s
}

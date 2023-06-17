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

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.FusekiServer.Builder;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.rdf.model.Model;

/**
 * Interface for extension code to modify the building of a {@link FusekiServer}.
 * <p>
 * When a server is being built:
 * <ul>
 * <li>{@linkplain #prepare}
 *      -- called at the beginning of the
 *     {@link org.apache.jena.fuseki.main.FusekiServer.Builder#build() FusekiServer.Builder build()}
 *      step. This call can manipulate the server configuration. This is the usual operation for customizing a server.</li>
 * <li>{@linkplain #configured} -- called after the DataAccessPoint registry has been built.</li>
 * <li>{@linkplain #server(FusekiServer)} -- called at the end of the "build" step before
 *     {@link org.apache.jena.fuseki.main.FusekiServer.Builder#build() FusekiServer.Builder build()}
 *     returns.</li>
 * </ul>
 */
public interface FusekiBuildCycle {
    /**
     * Display name to identify this module.
     */
    public String name();

    // ---- Build cycle

    /**
     * Called at the start of "build" step. The builder has been set according to the
     * configuration of API calls and parsing configuration files. No build actions have been carried out yet.
     * The module can make further FusekiServer.{@link Builder} calls.
     * The "configModel" parameter is set if a configuration file was used otherwise it is null.
     * <p>
     * This is the main point for customization of server.
     * <p>
     * It can add and modify the data services being built, and also add servlets and servlet filters.
     *
     * @param serverBuilder
     *      The FusekiServer.Builder
     * @param datasetNames
     *      The names of DataServices configured by API calls and configuration file.
     * @param configModel
     */
    public default void prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) { }

     /**
      * Called after the DataAccessPointRegistry has been built.
      * <p>
      * The default implementation is to call {@link #configDataAccessPoint(DataAccessPoint, Model)}
      * for each {@link DataAccessPoint}.
      * <pre>
      *    dapRegistry.accessPoints().forEach(accessPoint{@literal ->}configDataAccessPoint(accessPoint, configModel));
      * </pre>
      */
    public default void configured(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        dapRegistry.accessPoints().forEach(accessPoint->configDataAccessPoint(accessPoint, configModel));
    }

    /**
     * This method is called for each {@link DataAccessPoint} by the default
     * implementation of {@link #configured} after the new servers
     * DataAccessPointRegistry has been built.
     */
    public default void configDataAccessPoint(DataAccessPoint dap, Model configModel) {}

    /**
     * Built, not started, about to be returned to the builder caller.
     */
    public default void server(FusekiServer server) { }

    /**
     * Confirm or reject a request to reload.
     * <p>
     * Not all servers or server modules may be able to reload. This is reload of a
     * live server that continued to execute while reload happens and which completes
     * all outstanding requests at the time of the reload request.
     * <p>
     * Return true if reload is possible. If all modules return true the reload
     * continues and {@link #serverReload} will be called. If any module returns
     * false, the reload is not performed.
     */
    public default boolean serverConfirmReload(FusekiServer server) { return true; }

    /**
     * A running server has been asked to reload its configuration.
     * <p>
     * Certain server feature will not change.
     * <ul>
     * <li>http and https connectiosn are preserved.
     * <li>Port number</li>
     * <li>JVM process</li>
     * </ul>
     */
    public default void serverReload(FusekiServer server) { }
}

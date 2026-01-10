/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.main.runner;

import java.util.function.Consumer;

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Setup details (command line, config file) from command line processing.
 * This is built by {@link FusekiArgs#process}.
 * This is processed by {@link FusekiArgs#applyArgs}.
 */
public class ServerArgs {

    /*package*/ static int UNSET_PORT = -1;

    public ServerArgs() { }

    // General
    public boolean quietLogging           = false;
    public boolean verboseLogging         = false;

    /** Loopback */
    public boolean loopback               = false;

    /** Server port. This is the port (server.getPort) when both http and https are active. */
    public int httpPort                   = UNSET_PORT;
    // https
    public int httpsPort                  = UNSET_PORT;
    public String httpsKeysDetails        = null;

    // Jetty server configuration file.
    public String jettyConfigFile         = null;

    /** The dataset name (canonical form) */
    public String  datasetPath            = null;
    /** Allow update */
    public boolean allowUpdate            = false;

    /**
     * FusekiModules to use during the server build
     */
    public FusekiModules fusekiModules    = null;

    public boolean withCORS               = true;
    public String corsConfigFile          = null;
    public boolean withPing               = false;
    public boolean withStats              = false;
    public boolean withMetrics            = false;
    public boolean withCompact            = false;

    // Either a dataset setup from the command line (delayed creation of the dataset) ...
    // The consumer should set the "dataset" field and the description field.
    public Consumer<ServerArgs> dsgMaker  = null;
    public DatasetGraph dataset           = null;
    /** RDFS dataset - only when dataset is defined on the command line. */
    public Graph rdfsSchemaGraph          = null;

    // ... or configuration file.
    public String serverConfigFile        = null;
    public Model serverConfigModel        = null;

    /** Allow no datasets without it being an error. This is not a command argument. */
    public boolean allowEmpty             = false;
    public SetupType setup                = SetupType.UNSET;
    /** Start without a dataset or configuration (this is {@code --empty}) */
    public boolean startEmpty             = false;

    /** General query processor servlet */
    public String addGeneralQueryProc     = null;

    public boolean validators             = false;
    /** An informative label */
    public String datasetDescription      = null;
    public String servletContextPath      = null;
    public String contentDirectory        = null;

    // Server authentication
    public AuthScheme authScheme          = null;
    public String passwdFile              = null;
    public String realm                   = null;

    /** Don't process standard arguments. This is a not a command argument.*/
    public boolean bypassStdArgs          = false;
}

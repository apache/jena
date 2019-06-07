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

package org.apache.jena.fuseki.main.cmds;

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.sparql.core.DatasetGraph;

/** Setup details (command line, config file) from command line processing.
 *  This is built by {@link FusekiMain#exec}.
 *  This is processed by {@link FusekiMain#buildServer}.
 */
class ServerConfig {
    /** Server port. This is the http port when both http and https are active. */
    public int port;
    /** Loopback */
    public boolean   loopback         = false;
    /** The dataset name */
    public String    datasetPath      = null;
    /** Allow update */
    public boolean   allowUpdate      = false;

    public boolean   verboseLogging   = false;

    public boolean withPing           = false;
    public boolean withStats          = false;

    // This is set ...
    public DatasetGraph dsg           = null;
    // ... or this.
    public String serverConfig        = null;

    /** No registered datasets without it being an error. */
    public boolean empty              = false;
    /** General query processor servlet */
    public String addGeneral          = null;

    public boolean validators         = false;
    /** An informative label */
    public String datasetDescription;
    public String contentDirectory    = null;

    // Server authentication
    public AuthScheme authScheme      = null;
    public String passwdFile          = null;
    public String realm               = null;

    // https
    public int httpsPort              = -1;
    public String httpsKeystore       = null;
    public String httpsKeystorePasswd = null;
}
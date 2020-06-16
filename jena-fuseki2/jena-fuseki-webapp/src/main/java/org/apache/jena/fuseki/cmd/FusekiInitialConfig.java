/**
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

package org.apache.jena.fuseki.cmd;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.sparql.core.DatasetGraph;

/** Dataset setup (command line, config file) for a dataset (or several if config file) */
public class FusekiInitialConfig {
    public boolean quiet = false;
    public boolean verbose = Fuseki.verboseLogging;

    // Priority order : --conf, templated
    // through the command line processing should not allow --conf and a templated /dataset.

    // Label for dataset setup (command line).
    public String datasetDescription  = null;
    // Either this ... command line ...
    public String    argTemplateFile  = null;              // Command list args --mem, --loc, --memtdb
    public String    datasetPath      = null;              // Dataset name on the command line.
    public boolean   allowUpdate      = false;             // Command line --update.
    // Special case - prebuilt dataset.  Uses datasetPath.
    public DatasetGraph dsg           = null;             // Embedded or command line --file)

    // Or configuration file from command line
    public String    fusekiCmdLineConfigFile = null;       // Command line --conf.
    // Or configuration from run area (lowest priority)
    public String    fusekiServerConfigFile = null;        // "run" area

    // Additional information.
    public Map<String,String> params  = new HashMap<>();

    public FusekiInitialConfig() {}

    public void reset() {
        argTemplateFile  = null;
        datasetPath = null;
        allowUpdate = false;
        dsg = null;
        fusekiCmdLineConfigFile = null;       // Command line --conf.
        fusekiServerConfigFile = null;
    }
}

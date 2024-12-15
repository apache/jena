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

package org.apache.jena.fuseki.mgt;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.ctl.ActionCtl;
import org.apache.jena.fuseki.main.FusekiLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.web.HttpNames;

/**
 * Administration action to reload the server's dataset configuration.
 * <p>
 * This is done by reading the configuration file which may have changed since server startup.
 * <p>
 * If the server does not have a configuration file (e.g. command line or programmatic configuration)
 */
public class ActionReload extends ActionCtl {

    @Override
    public void validate(HttpAction action) {
        if ( action.getRequestMethod() != HttpNames.METHOD_POST ) {
            ServletOps.errorMethodNotAllowed(action.getRequestMethod());
        }
    }

    @Override
    public void execute(HttpAction action) {
        FusekiServer server = FusekiServer.get(action.getRequest().getServletContext());
        if ( server == null ) {
            ServletOps.errorOccurred("Failed to find the server for this action");
            return;
        }

        String configFilename = server.getConfigFilename();
        if ( configFilename == null ) {
            FmtLog.warn(Fuseki.serverLog, "[%d] Server does not have an associated configuration file", action.id);
            ServletOps.errorBadRequest("Server does not have an associated configuration file");
            return;
        }
        Model model = RDFParser.source(configFilename).toModel();
        FmtLog.info(Fuseki.serverLog, "[%d] Reload configuration", action.id);
        FusekiLib.reload(server, model);
    }
}

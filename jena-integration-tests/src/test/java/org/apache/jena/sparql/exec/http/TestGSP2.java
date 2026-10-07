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

package org.apache.jena.sparql.exec.http;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.FusekiTestLib;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;

/** GSP tests with specific server setup */
public class TestGSP2 {
    @Test public void gsp_config_general_dataset() {
        // Issue GH-4292
        // DatasetGraphMapLink
        DatasetGraph dsg = DatasetGraphFactory.createGeneral();
        FusekiServer server = FusekiServer.create().port(0).add("/ds", dsg).build();
        server.start();
        String URL = server.datasetURL("/ds");
        Node gn = NodeFactory.createURI("urn:ns:missing");
        try {
            // Expect 404
            FusekiTestLib.expect404(()->
                GSP.service(URL+"/data").graphName(gn).DELETE()
                );
            // GET 400
            //GSP.service(URL+"/data").graphName(gn).GET();

            QueryExec qExec = QueryExecHTTP.service(URL).query("SELECT * { GRAPH ?g { } }").build();
            RowSet rowSet = qExec.select();
            // No named graphs expected
            assertFalse(rowSet.hasNext(), "No named graphs expected");
        } finally {
            server.stop();
        }
    }
}

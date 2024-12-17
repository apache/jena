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

package org.apache.jena.fuseki.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

/**
 * Common setup for running a server with services and an initially empty database.
 */
public class AbstractFusekiTest {
    private FusekiServer server;

    protected String datasetName()    { return "database"; }
    protected String datasetPath()    { return "/"+datasetName(); }
    protected String databaseURL()    { return server.datasetURL(datasetPath()); }
    protected String serverURL()      { return server.serverURL(); }

    protected String serviceUpdate()  { return databaseURL()+"/update"; }
    protected String serviceQuery()   { return databaseURL()+"/query"; }
    protected String serviceGSP_R()   { return databaseURL()+"/get"; }
    protected String serviceGSP()     { return databaseURL()+"/data"; }

    @BeforeEach public void startServer() {
        DatasetGraph dsgTesting = DatasetGraphFactory.createTxnMem();
        server = FusekiServer.create()
                .port(0)
                //.verbose(true)
                .add(datasetPath(), dsgTesting)
                .enablePing(true)
                .enableMetrics(true)
                .start();
    }

    @AfterEach public void stopServer() {
        if ( server != null )
            server.stop();
    }
}

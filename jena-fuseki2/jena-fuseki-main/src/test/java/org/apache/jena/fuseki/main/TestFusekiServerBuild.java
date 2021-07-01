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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.Test;

public class TestFusekiServerBuild {
    // Most testing happens by use in the test suite.

    @Test public void fuseki_build_1() {
        FusekiServer server = FusekiServer.create().port(3456).build();
        // Not started. Port not assigned.
        assertTrue(server.getHttpPort() == 3456 );
        assertTrue(server.getHttpsPort() == -1 );
    }

    @Test public void fuseki_build_2() {
        FusekiServer server = FusekiServer.create().port(0).build();
        // Not started. Port not assigned.
        assertTrue(server.getHttpPort() == 0 );
        assertTrue(server.getHttpsPort() == -1 );
        server.start();
        try {
            assertFalse(server.getHttpPort() == 0 );
            assertTrue(server.getHttpsPort() == -1 );
        } finally { server.stop(); }
    }

    // The port in "testing/jetty.xml" is 1077

    @Test public void fuseki_ext_jetty_xml_1() {
        FusekiServer server = FusekiServer.create()
                .jettyServerConfig("testing/jetty.xml")
                .add("/ds", DatasetGraphFactory.createTxnMem())
                .build();
        server.start();
        try {
            assertEquals(1077, server.getHttpPort());
            assertEquals(1077, server.getPort());
            String URL = "http://localhost:1077/ds";
            assertEquals(URL, server.datasetURL("ds"));
            try ( RDFConnection conn = RDFConnectionFactory.connect(URL) ) {
                boolean b = conn.queryAsk("ASK{}");
            }

        } finally { server.stop(); }
    }
}

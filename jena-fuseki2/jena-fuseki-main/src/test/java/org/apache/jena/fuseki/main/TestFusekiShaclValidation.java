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
import static org.junit.Assert.assertNotNull;

import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.graph.Graph;
import org.apache.jena.http.HttpRDF;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ValidationReport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFusekiShaclValidation {
    // Fuseki Main server
    private static FusekiServer server = null;
    private static String serverURL = null;
    private static final String DIR = "testing/ShaclValidation/";

    @BeforeClass
    public static void beforeClass() {
        int port = WebLib.choosePort();

        FusekiServer server = FusekiServer.create()
            .port(port)
            .parseConfigFile(DIR+"config-validation.ttl")
            .build();
        server.start();
        serverURL = "http://localhost:"+port;
    }

    @AfterClass
    public static void afterClass() {
        if ( server != null )
            server.stop();
    }

    @Test
    public void shacl_empty_shapes() {
        try ( RDFConnection conn = RDFConnection.connect(serverURL+"/ds")) {
            conn.put(DIR+"data1.ttl");
            ValidationReport report = validateReport(serverURL+"/ds/shacl?graph=default", DIR+"shapes-empty.ttl");
            assertNotNull(report);
            assertEquals(0, report.getEntries().size());
            conn.update("CLEAR ALL");
        }
    }

    @Test
    public void shacl_default_graph() {
        try ( RDFConnection conn = RDFConnection.connect(serverURL+"/ds")) {
            conn.put(DIR+"data1.ttl");
            ValidationReport report = validateReport(serverURL+"/ds/shacl?graph=default", DIR+"shapes1.ttl");
            assertNotNull(report);
            assertEquals(3, report.getEntries().size());
            conn.update("CLEAR ALL");
        }
    }

    @Test
    public void shacl_no_data_graph() {
        try ( RDFConnection conn = RDFConnection.connect(serverURL+"/ds")) {
            conn.put(DIR+"data1.ttl");
            try {
                FusekiTestLib.expect404(()->{
                    ValidationReport report = validateReport(serverURL+"/ds/shacl?graph=urn:abc:noGraph", DIR+"shapes1.ttl");
                });
            } finally {
                conn.update("CLEAR ALL");
            }
        }
    }

    @Test
    public void shacl_union_1() {
        try ( RDFConnection conn = RDFConnection.connect(serverURL+"/ds")) {
            conn.put(DIR+"data1.ttl");
            ValidationReport report = validateReport(serverURL+"/ds/shacl?graph=union", DIR+"shapes1.ttl");
            assertNotNull(report);
            // Union does not include the storage default graph
            assertEquals(0, report.getEntries().size());
            conn.update("CLEAR ALL");
        }
    }

    @Test
    public void shacl_union_2() {
        try ( RDFConnection conn = RDFConnection.connect(serverURL+"/ds")) {
            conn.put("urn:abc:graph", DIR+"data1.ttl");
            ValidationReport report = validateReport(serverURL+"/ds/shacl?graph=union", DIR+"shapes1.ttl");
            assertNotNull(report);
            assertEquals(3, report.getEntries().size());
            conn.update("CLEAR ALL");
        }
    }

    @Test
    public void shacl_named_graph() {
        try ( RDFConnection conn = RDFConnection.connect(serverURL+"/ds")) {
            conn.put("urn:abc:graph", DIR+"data1.ttl");
            ValidationReport report = validateReport(serverURL+"/ds/shacl?graph=urn:abc:graph", DIR+"shapes1.ttl");
            assertNotNull(report);
            assertEquals(3, report.getEntries().size());
            conn.update("CLEAR ALL");
        }
    }

    @Test
    public void shacl_targetNode_1() {
        try ( RDFConnection conn = RDFConnection.connect(serverURL+"/ds")) {
            conn.put("urn:abc:graph", DIR+"data1.ttl");
            ValidationReport report = validateReport(serverURL+"/ds/shacl?graph=urn:abc:graph&target=:s1", DIR+"shapes1.ttl");
            assertNotNull(report);
            assertEquals(2, report.getEntries().size());
            conn.update("CLEAR ALL");
        }
    }

    @Test
    public void shacl_targetNode_2() {
        try ( RDFConnection conn = RDFConnection.connect(serverURL+"/ds")) {
            conn.put("urn:abc:graph", DIR+"data1.ttl");
            ValidationReport report = validateReport(serverURL+"/ds/shacl?graph=urn:abc:graph&target=:s3", DIR+"shapes1.ttl");
            assertNotNull(report);
            assertEquals(0, report.getEntries().size());
            conn.update("CLEAR ALL");
        }
    }

    @Test
    public void shacl_targetNode_3() {
        try ( RDFConnection conn = RDFConnection.connect(serverURL+"/ds")) {
            conn.put("urn:abc:graph", DIR+"data1.ttl");
            ValidationReport report = validateReport(serverURL+"/ds/shacl?graph=urn:abc:graph&target=http://nosuch/node/", DIR+"shapes1.ttl");
            assertNotNull(report);
            assertEquals(0, report.getEntries().size());
            conn.update("CLEAR ALL");
        }
    }

    private static ValidationReport validateReport(String url, String shapesFile) {
        Graph shapesGraph = RDFDataMgr.loadGraph(shapesFile);
        Graph responseGraph = HttpRDF.httpPostGraphRtn(url, shapesGraph);
        return ValidationReport.fromGraph(responseGraph);
    }
}

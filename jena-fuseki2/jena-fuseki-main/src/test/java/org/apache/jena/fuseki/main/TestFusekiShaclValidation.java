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

package org.apache.jena.fuseki.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.graph.Graph;
import org.apache.jena.http.HttpRDF;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ValidationReport;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestFusekiShaclValidation {
    private static final String DIR = "testing/ShaclValidation/";

    @FunctionalInterface
    interface Action { void run(String datasetURL); }

    private void withServer(Action action) {
        FusekiServer server = createServer().start();
        try {
            action.run(server.datasetURL("/ds"));
        } finally {
            server.stop();
        }
    }

    private static FusekiServer createServer() {
        return FusekiServer.create()
                .verbose(ConfigureTests.VerboseServer)
                // With SHACL service.
                .parseConfigFile(DIR+"config-validation.ttl")
                .port(0)
                .build();
    }

    private static void clearAll(RDFConnection conn) {
        try { conn.update("CLEAR ALL"); } catch (Throwable th) {}
    }

    // ====

    @Test
    public void shacl_empty_shapes() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnection.connect(datasetURL)) {
                conn.put(DIR+"data1.ttl");
                ValidationReport report = validateReport(datasetURL+"/shacl?graph=default", DIR+"shapes-empty.ttl");
                assertNotNull(report);
                assertEquals(0, report.getEntries().size());
                clearAll(conn);
            }
        });
    }

    @Test
    public void shacl_default_graph() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnection.connect(datasetURL)) {
                conn.put(DIR+"data1.ttl");
                ValidationReport report = validateReport(datasetURL+"/shacl?graph=default", DIR+"shapes1.ttl");
                assertNotNull(report);
                assertEquals(3, report.getEntries().size());
                clearAll(conn);
            }
        });
    }

    @Test
    public void shacl_union_1() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnection.connect(datasetURL)) {
                conn.put(DIR+"data1.ttl");
                ValidationReport report = validateReport(datasetURL+"/shacl?graph=union", DIR+"shapes1.ttl");
                assertNotNull(report);
                // Union does not include the storage default graph
                assertEquals(0, report.getEntries().size());
                clearAll(conn);
            }
        });
    }

    @Test
    public void shacl_union_2() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnection.connect(datasetURL)) {
                conn.put("urn:abc:graph", DIR+"data1.ttl");
                ValidationReport report = validateReport(datasetURL+"/shacl?graph=union", DIR+"shapes1.ttl");
                assertNotNull(report);
                assertEquals(3, report.getEntries().size());
                conn.update("CLEAR ALL");
            }
        });
    }

    @Test
    public void shacl_named_graph() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnection.connect(datasetURL)) {
                conn.put("urn:abc:graph", DIR+"data1.ttl");
                ValidationReport report = validateReport(datasetURL+"/shacl?graph=urn:abc:graph", DIR+"shapes1.ttl");
                assertNotNull(report);
                assertEquals(3, report.getEntries().size());
                clearAll(conn);
            }
        });
    }

    @Test
    public void shacl_targetNode_1() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnection.connect(datasetURL)) {
                conn.put("urn:abc:graph", DIR+"data1.ttl");
                ValidationReport report = validateReport(datasetURL+"/shacl?graph=urn:abc:graph&target=:s1", DIR+"shapes1.ttl");
                assertNotNull(report);
                assertEquals(2, report.getEntries().size());
                clearAll(conn);
            }
        });
    }

    @Test
    public void shacl_targetNode_2() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnection.connect(datasetURL)) {
                conn.put("urn:abc:graph", DIR+"data1.ttl");
                ValidationReport report = validateReport(datasetURL+"/shacl?graph=urn:abc:graph&target=:s3", DIR+"shapes1.ttl");
                assertNotNull(report);
                assertEquals(0, report.getEntries().size());
                clearAll(conn);
            }
        });
    }

    @Test
    public void shacl_targetNode_3() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnection.connect(datasetURL)) {
                conn.put("urn:abc:graph", DIR+"data1.ttl");
                ValidationReport report = validateReport(datasetURL+"/shacl?graph=urn:abc:graph&target=http://nosuch/node/", DIR+"shapes1.ttl");
                assertNotNull(report);
                assertEquals(0, report.getEntries().size());
                clearAll(conn);
            }
        });
    }

    @Test
    public void shacl_no_data_graph() {
        withServer((datasetURL)->{
            try ( RDFConnection conn = RDFConnection.connect(datasetURL)) {
                conn.put(DIR+"data1.ttl");
                try {
                    FusekiTestLib.expect404(()->{
                        ValidationReport report = validateReport(datasetURL+"/shacl?graph=urn:abc:noGraph", DIR+"shapes1.ttl");
                    });
                } finally {
                    conn.update("CLEAR ALL");
                }
            }
        });
    }

    @Test
    public void shacl_imports() {
        LogCtl.withLevel(Fuseki.actionLog, "FATAL", ()->{
            withServer((datasetURL)->{
                FusekiTestLib.expect400(()->{
                    validateReport(datasetURL+"/shacl?default", DIR+"shapes-imports.ttl");
                });
            });
        });
    }

    private static ValidationReport validateReport(String url, String shapesFile) {
        Graph shapesGraph = RDFDataMgr.loadGraph(shapesFile);
        Graph responseGraph = HttpRDF.httpPostGraphRtn(url, shapesGraph);
        return ValidationReport.fromGraph(responseGraph);
    }
}

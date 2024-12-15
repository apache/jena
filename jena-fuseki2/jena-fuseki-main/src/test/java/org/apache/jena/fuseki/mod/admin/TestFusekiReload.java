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

package org.apache.jena.fuseki.mod.admin;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.FusekiTestLib;
import org.apache.jena.fuseki.mgt.ActionReload;
import org.apache.jena.http.HttpOp;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;

public class TestFusekiReload {

    private static Path fConfigServer  = Path.of("target/config-reload.ttl");
    private static Path DIR            = Path.of("testing/Config/");
    private static Path fConfig1       = DIR.resolve("reload-config1.ttl");
    private static Path fConfig2       = DIR.resolve("reload-config2.ttl");

    @Before public void before() {
        // Initial state
        copyFile(fConfig1, fConfigServer);
    }

    @AfterClass public static void after() {
        try {
            Files.delete(fConfigServer);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }


    @Test public void serverReload_1() {
        FusekiServer server = server(fConfigServer);
        try {
            server.start();

            serverBeforeReload(server);

            // Change configuration file.
            copyFile(fConfig2, fConfigServer);
            Model newConfig = RDFParser.source(fConfigServer).toModel();

            // Reload operation on the server
            HttpOp.httpPost("http://localhost:"+server.getPort()+"/$/reload");

            serverAfterReload(server);
        }
        finally { server.stop(); }
    }

    @Test public void serverReload_2() {
        FusekiServer server = serverNoConfigFile();
        try {
            server.start();

            serverBeforeReload(server);

            // Change configuration file.
            // Error!
            copyFile(fConfig2, fConfigServer);
            Model newConfig = RDFParser.source(fConfigServer).toModel();

            LogCtl.withLevel(Fuseki.serverLog, "ERROR", ()->
                // Poke server - Operation denied - no configuration file.
                FusekiTestLib.expect400(()->HttpOp.httpPost("http://localhost:"+server.getPort()+"/$/reload"))
            );

            // No change
            serverBeforeReload(server);
        }
        finally { server.stop(); }
    }

    private static void copyFile(Path pathSrc, Path pathDest) {
        try {
            Files.copy(pathSrc, pathDest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) { throw IOX.exception(ex); }
    }

    private static void serverBeforeReload(FusekiServer server) {
        String URL = "http://localhost:"+server.getPort()+"/$/ping";
        String x = HttpOp.httpGetString(URL);
        query(server, "/ds", "SELECT * { }", 200);
        query(server, "/dataset2", "SELECT * { ?s ?p ?o }", 404);
        query(server, "/zero", "SELECT * { }", 404);
        query(server, "/codedsg", "SELECT * { }", 200);
    }

    private static void serverAfterReload(FusekiServer server) {
        String URL = "http://localhost:"+server.getPort()+"/$/ping";
        String x = HttpOp.httpGetString(URL);
        query(server, "/ds", "SELECT * { }", 404);
        query(server, "/dataset2", "SELECT * { ?s ?p ?o }", 200);
        query(server, "/zero", "SELECT * { }", 404);
        // Replaced.
        query(server, "/codedsg", "SELECT * { }", 404);
    }

    private static void query(FusekiServer server, String datasetName, String queryString, int expectedStatusCode) {
        QueryExec qExec = QueryExecHTTP.service(server.datasetURL(datasetName)).query(queryString).build();
        try {
            RowSetOps.consume(qExec.select());
            assertEquals(datasetName, expectedStatusCode, 200);
        } catch (QueryExceptionHTTP ex) {
            assertEquals(datasetName, expectedStatusCode, ex.getStatusCode());
        }
    }

    private static FusekiServer serverNoConfigFile() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = FusekiServer.create().port(0)
                                          // .verbose(true)
                                          .addServlet("/$/reload", new ActionReload())
                                          .add("/ds", dsg)
                                          .add("/codedsg", dsg)
                                          .build();
        return server;
    }


    private static FusekiServer server(Path fConfig) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = FusekiServer.create().port(0)
                                          // .verbose(true)
                                          .addServlet("/$/reload", new ActionReload())
                                          .parseConfigFile(fConfig)
                                          .add("/codedsg", dsg)
                                          .build();
        return server;
    }
}

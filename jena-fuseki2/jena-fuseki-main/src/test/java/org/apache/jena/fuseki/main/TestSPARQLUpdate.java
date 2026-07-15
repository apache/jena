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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;

public class TestSPARQLUpdate {
    private static String PREFIXES = """
            PREFIX : <http://example/>
            """;
    private static String DS = "/updateTest";

    private FusekiServer server() {
        DatasetGraph dsgTesting = DatasetGraphFactory.createTxnMem();
        return server(dsgTesting);
    }

    private FusekiServer server(DatasetGraph dsg) {
        FusekiServer server = FusekiServer.create()
                .port(0)
                //.verbose(true)
                .add(DS, dsg)
                .enablePing(true)
                .enableMetrics(true)
                .start();
        return server;
    }

    @Test public void update1() {
        FusekiServer server = server();
        String serviceURL = server.datasetURL(DS);
        UpdateExec.service(serviceURL).update(PREFIXES+"INSERT DATA { :s :p :o }").execute();
    }

    @Test public void update2() {
        FusekiServer server = server();
        String serviceURL = server.datasetURL(DS);
        try ( RDFLink link = RDFLink.connect(serviceURL) ) {
            link.update(PREFIXES+"INSERT DATA { :s :p :o }");
        }
    }

    @Test public void updateError1() {
        FusekiServer server = server();
        String serviceURL = server.datasetURL(DS);
        String URL = serviceURL+"/?using-named-graph-uri=http://example/ng1";
        FusekiTestLib.expect400(()->
            UpdateExec.service(URL).update(PREFIXES+" WITH  <http://example/ng2> INSERT { :s :p :o } WHERE {}").execute()
        );
    }

    @Test public void updateLoadFile_1() {
        FusekiServer server = server();
        String serviceURL = server.datasetURL(DS);

        // This will resolve to the same place in the test server.
        String FN = "testing/Files/data.ttl";
        String loadFileName = Path.of(FN).toAbsolutePath().toString();
        assertTrue(FileOps.exists(loadFileName), "No test file");
        String loadFileIRI = IRILib.filenameToIRI(loadFileName);

        FusekiTestLib.expect400(()-> {
            UpdateExec.service(serviceURL).update("LOAD <file:"+loadFileIRI+">").execute(); });
        boolean hasTriples= QueryExec.service(serviceURL).query("ASK { ?s ?p ?o }").ask();
        assertFalse(hasTriples, "Dataset not empty");
    }

    @Test public void updateLoadFile_2() {
        // No abort.
        DatasetGraph dsgTesting = DatasetGraphFactory.createGeneral();
        FusekiServer server = server(dsgTesting);
        String serviceURL = server.datasetURL(DS);

        // This will resolve to the same place in the test server.
        String FN = "testing/Files/data.ttl";
        String loadFileName = Path.of(FN).toAbsolutePath().toString();
        assertTrue(FileOps.exists(loadFileName), "No test file");
        String loadFileIRI = IRILib.filenameToIRI(loadFileName);

        FusekiTestLib.expect400(()-> {
            UpdateExec.service(serviceURL).update("LOAD <file:"+loadFileIRI+">").execute(); });
        boolean hasTriples= QueryExec.service(serviceURL).query("ASK { ?s ?p ?o }").ask();
        assertFalse(hasTriples, "Dataset not empty");
    }
}

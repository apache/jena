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

package org.apache.jena.sparql.exec.http;

import static org.apache.jena.sparql.sse.SSE.parseQuad;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestUpdateExecHTTP {

    private static FusekiServer server = null;
    private static String URL;
    private static String dsName = "/ds";
    private static String dsURL;
    private static Quad q0 = parseQuad("(_ :s :p :o)");
    private static Quad q1 = parseQuad("(:g1 :s :p 1)");
    private static Quad q2 = parseQuad("(:g2 :s :p 2)");

    static {
        if ( false )
            LogCtl.enable(Fuseki.actionLog);
    }

    @BeforeClass public static void beforeClass() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        server = FusekiServer.create()
            .port(0)
            .verbose(true)
            .add(dsName, dsg)
            .build();
        server.start();
        int port = server.getPort();
        URL = "http://localhost:"+port+"/";
        dsURL = "http://localhost:"+port+dsName;
    }

    @AfterClass public static void afterClass() {
        if ( server != null ) {
            try { server.stop(); } finally { server = null; }
        }
    }

    private static void clear() {
        UpdateExecHTTP.service(service())
            .updateString("CLEAR ALL")
            .build()
            .execute();
    }

    private static String service() { return dsURL; }
    private static String serviceQuery() { return dsURL+"/query"; }


    @Test public void update_1() {
        UpdateExecHTTP uExec = UpdateExecHTTP.service(service())
            .sendMode(UpdateSendMode.systemDefault)
            .updateString("INSERT DATA { <x:s> <x:p> 234 } ")
            .build();
        uExec.execute();
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                .endpoint(serviceQuery())
                .queryString("ASK { ?s ?p 234 }")
                .build()) {
            boolean b = qExec.ask();
            assertTrue(b);
        }
    }

    @Test public void update_form_2() {
        UpdateRequest req = UpdateFactory.create("INSERT DATA { <x:s> <x:p> 567 } ");
        UpdateExecHTTP uExec = UpdateExecHTTP.newBuilder()
            .endpoint(service())
            .sendMode(UpdateSendMode.asPostForm)
            .update(req)
            .build();
        uExec.execute();
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                .endpoint(serviceQuery())
                .queryString("ASK { ?s ?p 567 }")
                .build()) {
            boolean b = qExec.ask();
            assertTrue(b);
        }
    }

    // ?user-graph-uri= and ?using-named-graph-uri only apply to the WHERE clause of
    // an update.

    @Test public void update_using_1() {
        try {
            update_using_1_test();
        } finally {
            clear();
        }
    }

    private void update_using_1_test() {
        {
            UpdateRequest req1 = UpdateFactory.create("INSERT DATA { GRAPH <http://example/gg> { <x:s> <x:p> 567 } }");
            UpdateExecHTTP uExec1 = UpdateExecHTTP.newBuilder()
                .endpoint(service()).update(req1)
                .build();
            uExec1.execute();
        }
        {
            // Should apply the change because USING = http://example/gg
            UpdateRequest req2 = UpdateFactory.create("INSERT { <x:s1> <x:p1> ?o } WHERE { ?s ?p ?o }");
            UpdateExecHTTP uExec2 = UpdateExecHTTP.newBuilder()
                .endpoint(service()).update(req2)
                .addUsingGraphURI("http://example/gg")
                .build();
            uExec2.execute();
        }

        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                .endpoint(serviceQuery())
                .queryString("ASK { ?s ?p 567 }")
                .build()) {
            boolean b = qExec.ask();
            assertTrue(b);
        }
    }
}

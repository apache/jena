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

import static org.apache.jena.atlas.lib.StrUtils.strjoinNL;
import static org.apache.jena.sparql.sse.SSE.parseQuad;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link QueryExecHTTP} with no authentication.
 * See  {@link TestQueryExecHTTP} for most of the tests.
 */
public class TestQueryExecCleanServer {
    // Unlike TestQueryExecutionHTTP these tests run a clean server each time.
    // Test can control logging and cause broken connections which clean up async to the test suite.
    private static String URL;
    private static String dsName = "/ds";
    private static String dsURL;
    private static Quad q0 = parseQuad("(_ :s :p :o)");
    private static Quad q1 = parseQuad("(:g1 :s :p 1)");
    private static Quad q2 = parseQuad("(:g2 :s :p 2)");

    private FusekiServer server = null;

    private String serverFusekiLogLevel = null;

    @Before public void before() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        dsg.add(q0);
        dsg.add(q1);
        dsg.add(q2);
        server = FusekiServer.create()
            .port(0)
            .verbose(true)
            .add(dsName, dsg)
            .build();
        server.start();
        int port = server.getPort();
        URL = "http://localhost:"+port+"/";
        dsURL = "http://localhost:"+port+dsName;

        serverFusekiLogLevel = LogCtl.getLevel(Fuseki.actionLog);
        LogCtl.setLevel(Fuseki.actionLog, "Error");
    }

    @After public void after() {
        server.stop();
        if ( serverFusekiLogLevel != null )
            LogCtl.setLevel(Fuseki.actionLog, serverFusekiLogLevel);
    }

    private static String serviceQuery() { return dsURL; }

    // This test means the server will see "broken connection".
    // The test suite will restore logging on exit.
    // It can't be done here because the server does not see the broken connection immediately.
    @Test(expected=HttpException.class)
    public void query_timeout_1() {
        LogCtl.set(Fuseki.actionLog, "error");
        String queryString = strjoinNL
            ("PREFIX afn:     <http://jena.apache.org/ARQ/function#>"
                ,"SELECT * {"
                ,"  BIND (afn:wait(100) AS ?X)"
                ,"}");

        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                                            .service(dsURL)
                                            .queryString(queryString)
                                            // Short!
                                            .timeout(10, TimeUnit.MILLISECONDS)
                                            .build() ) {
            long x = Iter.count(qExec.select());
            assertEquals(2, x);
        }
    }
}

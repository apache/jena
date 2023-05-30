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
import static org.junit.Assert.assertTrue;

import java.util.function.Consumer;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.junit.Test;

public class TestMultipleEmbedded {

    static { JenaSystem.init(); }

    static Quad q1 = SSE.parseQuad("(_ :s :p 1)");
    static Quad q2 = SSE.parseQuad("(_ :s :p 2)");

    // Two servers, same port -> bad.
    @Test(expected=FusekiException.class)
    public void multiple_01() {
        DatasetGraph dsg = dataset();

        int port = WebLib.choosePort();
        FusekiServer server1 = FusekiServer.create().port(port).add("/ds1", dsg).build();
        // Same port - Bad.
        FusekiServer server2 = FusekiServer.create().port(port).add("/ds2", dsg).build();

        server1.start();

        try {
            server2.start();
        } catch (FusekiException ex) {
            // Jetty 9.4.12 throws BindException
            // Jetty 9.4.26 throws IOException cause BindException
            Throwable cause = ex.getCause();
            if ( cause instanceof java.io.IOException )
                cause = cause.getCause();
            assertTrue(cause instanceof java.net.BindException);
            throw ex;
        } finally {
            try { server1.stop(); } catch (Exception ex) {}
            try { server2.stop(); } catch (Exception ex) {}
        }
    }

    // Two servers, different ports -> good.
    @Test
    public void multiple_02() {
        DatasetGraph dsg = dataset();
        int port1 = WebLib.choosePort();
        FusekiServer server1 = FusekiServer.create().port(port1).add("/ds1", dsg).build();

        // Different port - good
        int port2 = WebLib.choosePort();
        FusekiServer server2 = FusekiServer.create().port(port2).add("/ds2", dsg).build();

        try {
            server1.start();
            server2.start();
        } finally {
            try { server1.stop(); } catch (Exception ex) {}
            try { server2.stop(); } catch (Exception ex) {}
        }
    }

    // Two servers, two datasets.
    @Test
    public void multiple_03() {
        DatasetGraph dsg1 = dataset();
        DatasetGraph dsg2 = dataset();
        // Same name.
        int port1 = WebLib.choosePort();
        FusekiServer server1 = FusekiServer.create().port(port1).add("/ds", dsg1).build().start();
        Txn.executeWrite(dsg1, ()->dsg1.add(q1));

        int port2 = WebLib.choosePort();
        FusekiServer server2 = FusekiServer.create().port(port2).add("/ds", dsg2).build().start();
        Txn.executeWrite(dsg2, ()->dsg2.add(q2));

        query("http://localhost:"+port1+"/ds/", "SELECT * {?s ?p 1}", qExec->{
            RowSet rs = qExec.select();
            long x = Iter.count(rs);
            assertEquals(1, x);
        });
        query("http://localhost:"+port2+"/ds/", "SELECT * {?s ?p 1}", qExec->{
            RowSet rs = qExec.select();
            long x = Iter.count(rs);
            assertEquals(0, x);
        });
        server1.stop();
        // server2 still running
        query("http://localhost:"+port2+"/ds/", "SELECT * {?s ?p 2}", qExec->{
            RowSet rs = qExec.select();
            long x = Iter.count(rs);
            assertEquals(1, x);
        });
        server2.stop();
    }

    // Two servers, one dataset under two names.
    @Test
    public void multiple_04() {
        DatasetGraph dsg = dataset();

        int port1 = WebLib.choosePort();
        FusekiServer server1 = FusekiServer.create().port(port1).add("/ds1", dsg).build().start();
        Txn.executeWrite(dsg, ()->dsg.add(q1));

        int port2 = WebLib.choosePort();
        FusekiServer server2 = FusekiServer.create().port(port2).add("/ds2", dsg).build().start();
        Txn.executeWrite(dsg, ()->dsg.add(q2));

        query("http://localhost:"+port1+"/ds1", "SELECT * {?s ?p ?o}", qExec->{
            RowSet rs = qExec.select();
            long x = Iter.count(rs);
            assertEquals(2, x);
        });
        query("http://localhost:"+port2+"/ds2", "SELECT * {?s ?p ?o}", qExec->{
            RowSet rs = qExec.select();
            long x = Iter.count(rs);
            assertEquals(2, x);
        });

        server1.stop();
        server2.stop();
    }

    /*package*/ static DatasetGraph dataset() {
        return DatasetGraphFactory.createTxnMem();
    }

    /*package*/ static void query(String URL, String query, Consumer<QueryExec> body) {
        try (QueryExec qExec = QueryExecHTTP.newBuilder().endpoint(URL).queryString(query).build() ) {
            body.accept(qExec);
        }
    }
}


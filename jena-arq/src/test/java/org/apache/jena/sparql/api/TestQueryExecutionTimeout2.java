/**
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

package org.apache.jena.sparql.api;

import static org.apache.jena.atlas.lib.Lib.sleep ;
import static org.junit.Assume.assumeFalse;

import java.util.concurrent.TimeUnit;

import org.apache.jena.base.Sys ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.query.* ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.junit.Assert ;
import org.junit.Before;
import org.junit.Test ;

public class TestQueryExecutionTimeout2
{
    // Testing related to JENA-440

    static private String prefix =
            "PREFIX f:       <http://example/ns#>\n"+
            "PREFIX afn:     <http://jena.apache.org/ARQ/function#>\n" ;
    static Graph                g   = TestQueryExecutionTimeout1.makeGraph(10);
    static DatasetGraph         dsg = DatasetGraphFactory.wrap(g) ;
    static Dataset              ds  = DatasetFactory.wrap(dsg) ;

    // Loaded CI.
    private static boolean mayBeErratic = Sys.isWindows || Sys.isMacOS;

    @Before public void beforeTest() {
        // These timeout test do not work reliably on MacOS in GH actions so for now
        // they are skipped. It looks like the server machines are heavily loaded and
        // as a result very long (10's seconds) pauses happen.
        // These tests need rewriting to cope with such an environment.
        assumeFalse(Sys.isMacOS);
    }

    @Test public void timeout_30()  { test2(200, 20, timeout(50, 250), true) ; }
    @Test public void timeout_31()  { test2(200, 100, 20, false) ; }

    // Make sure it isn't timeout1 - delay longer than timeout1
    @Test public void timeout_32()  { test2(100, 500, 200, false) ; }
    @Test public void timeout_33()  { test2(150, -1,  200, false) ; }

    @Test public void timeout_34()  { test2(10, 40, timeout(100, 250), true) ; }

    @Test public void timeout_35()  { test2(-1, 20, timeout(50, 250), true) ; }
    @Test public void timeout_36()  { test2(-1, 200, 20, false) ; }

    @Test public void timeout_37()  { test2(200, 200, 50, false) ; }
    @Test public void timeout_38()  { test2(200, -1, 50, false) ; }

    private static void test2(long timeout1, long timeout2, int delay, boolean exceptionExpected)
    {
        String qs = prefix+"SELECT * { ?s ?p ?o }";
        // Enough rows to keep the iterator pipeline full.
        try (QueryExecution qExec = QueryExecution.create().query(qs).dataset(ds)
                .initialTimeout(timeout1, TimeUnit.MILLISECONDS)
                .overallTimeout(timeout2, TimeUnit.MILLISECONDS)
                .build()) {
            ResultSet rs = qExec.execSelect() ;
            // ... wait for first binding.
            try {
                Binding b1 = rs.nextBinding() ;
            } catch (QueryCancelledException ex) {
                Assert.fail("QueryCancelledException not expected at start");
                return;
            }

            // ... then a possible timeout.
            sleep(delay) ;
            if ( exceptionExpected )
                exceptionExpected(rs) ;
            else
                noException(rs) ;
        }
    }

    private static void noException(ResultSet rs) {
        ResultSetFormatter.consume(rs);
    }

    private static void exceptionExpected(ResultSet rs) {
        try {
            ResultSetFormatter.consume(rs);
            Assert.fail("QueryCancelledException expected");
        } catch (QueryCancelledException ex) {}
    }

    private int timeout(int time1, int time2) {
        return mayBeErratic ? time2 : time1 ;
    }
}


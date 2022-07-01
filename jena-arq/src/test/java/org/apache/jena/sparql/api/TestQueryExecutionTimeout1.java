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

package org.apache.jena.sparql.api ;

import static org.apache.jena.atlas.lib.Lib.sleep ;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit ;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.base.Sys ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.* ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.function.FunctionRegistry ;
import org.apache.jena.sparql.function.library.wait ;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestQueryExecutionTimeout1
{
    static Graph                g   = makeGraph(1000) ;
    static DatasetGraph         dsg = DatasetGraphFactory.wrap(g) ;
    static Dataset              ds  = DatasetFactory.wrap(dsg) ;

    private static final String ns  = "http://example/ns#" ;

    @BeforeClass
    public static void beforeClass()
    {
        FunctionRegistry.get().put(ns + "wait", wait.class) ;
    }

    /*package*/ static Graph makeGraph(int size) {
        Graph g = GraphFactory.createDefaultGraph();
        Node s = SSE.parseNode("<s>");
        Node p = SSE.parseNode("<p>");
        for ( int i = 1 ; i < size ; i++ ) {
            Node o = SSE.parseNode("<o-"+i+">");
            g.add(Triple.create(s, p, o));
        }
        return g;
    }

    @AfterClass
    public static void afterClass()
    {
        FunctionRegistry.get().remove(ns + "wait") ;
    }

    // Loaded CI.
    private static boolean mayBeErratic = Sys.isWindows ;

    private int timeout(int time1, int time2) {
        return mayBeErratic ? time2 : time1 ;
    }

    static private String prefix =
        "PREFIX f:       <http://example/ns#>\n"+
        "PREFIX afn:     <http://jena.apache.org/ARQ/function#>\n" ;

    // Numbers all a bit iffy and can result in test failures
    // on a heavily loaded CI system and some OS'es seem to occasionally
    // suspend a process for many seconds. That means timeouts can go off when they
    // shouldn't. It's not the test framework ; the query execution semantics have
    // been changed.

    @Test
    public void timeout_01() {
        // Test unstable on loaded Jenkins CI
        String qs = prefix + "SELECT * { ?s ?p ?o }";
        QueryExecution qExec = QueryExecution.dataset(ds).query(qs)
                .timeout(50, TimeUnit.MILLISECONDS)
                .build();
        ResultSet rs = qExec.execSelect();
        exceptionExpected(rs, 10, 100, 1000); // 10 then every 100 upto 1000
    }

    @Test
    public void timeout_02() {
        String qs = prefix + "SELECT * { ?s ?p ?o }";
        // No timeout
        try ( QueryExecution qExec = QueryExecution.dataset(ds).query(qs)
                .timeout(100, TimeUnit.MILLISECONDS)
                .build()) {
            ResultSet rs = qExec.execSelect();
            ResultSetFormatter.consume(rs);
        }
    }

    @Test
    public void timeout_03() {
        String qs = prefix + "SELECT * { ?s ?p ?o }";
        try ( QueryExecution qExec = QueryExecution.dataset(ds).query(qs)
                .timeout(50, TimeUnit.MILLISECONDS)
                .build()) {
            ResultSet rs = qExec.execSelect();
            ResultSetFormatter.consume(rs);
            sleep(100);
            rs.hasNext();         // Query ended - calling rs.hasNext() is safe.
        }
    }

    @Test
    public void timeout_04()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o FILTER f:wait(1) }" ; // Sleep in execution to kick timer thread.
        try ( QueryExecution qExec = QueryExecution.dataset(ds).query(qs)
                .timeout(100, TimeUnit.MILLISECONDS)
                .build()) {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.consume(rs) ;
        }
    }

    @Test
    public void timeout_05()
    {
        // No timeout.
        String qs = prefix + "SELECT * { ?s ?p ?o FILTER f:wait(1) }" ;
        try(QueryExecution qExec = QueryExecutionFactory.create(qs, ds)) {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.consume(rs) ;
        }
    }

    @Test
    public void timeout_06()
    {
        // No timeout.
        String qs = prefix + "SELECT * { ?s ?p ?o FILTER f:wait(1) }" ;
        try ( QueryExecution qExec = QueryExecution.dataset(ds).query(qs)
                .timeout(-1, TimeUnit.MILLISECONDS)
                .build()) {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.consume(rs) ;
        }
    }

    @Test
    public void timeout_10()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        try ( QueryExecution qExec = QueryExecution.dataset(ds).query(qs)
                .initialTimeout(500, TimeUnit.MILLISECONDS)
                .overallTimeout(-1, TimeUnit.MILLISECONDS)
                .build()) {
            //was qExec.setTimeout(500, TimeUnit.MILLISECONDS, -1, TimeUnit.MILLISECONDS) ;
            ResultSet rs = qExec.execSelect() ;
            rs.next() ; // First timeout does not go off. Resets timers.
            rs.next() ; // Second timeout never goes off
            assertTrue(rs.hasNext()) ;
            ResultSetFormatter.consume(rs) ; // Second timeout does not go off.
        }
    }

    @Test
    public void timeout_11()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        try ( QueryExecution qExec = QueryExecution.dataset(ds).query(qs)
                .initialTimeout(100, TimeUnit.MILLISECONDS)
                .overallTimeout(100, TimeUnit.MILLISECONDS)
                .build()) {
            ResultSet rs = qExec.execSelect() ;
            rs.next() ; // First timeout does not go off. Resets timers.
            rs.next() ; // Second timeout never goes off
            assertTrue(rs.hasNext()) ;
            exceptionExpected(rs, 200, 100, 500) ;
        }
    }

    @Test
    public void timeout_12()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        try ( QueryExecution qExec = QueryExecution.dataset(ds).query(qs)
                .initialTimeout(-1, TimeUnit.MILLISECONDS)
                .overallTimeout(100, TimeUnit.MILLISECONDS)
                .build()) {
            ResultSet rs = qExec.execSelect() ;
            rs.next() ; // First timeout does not go off. Resets timer.
            rs.next() ; // Second timeout does not go off
            exceptionExpected(rs, 200, 100, 500) ;
        }
    }

    // Set timeouts via context.

    @Test
    public void timeout_20()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        ARQ.getContext().set(ARQ.queryTimeout, "10") ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        ResultSet rs = qExec.execSelect() ;
        exceptionExpected(rs, 50, 50, 250) ;
    }

    @Test
    public void timeout_21()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        ARQ.getContext().set(ARQ.queryTimeout, "20,10") ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        ResultSet rs = qExec.execSelect() ;
        exceptionExpected(rs, 50, 50, 250) ;
    }

    @Test
    public void timeout_22()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        ARQ.getContext().set(ARQ.queryTimeout, "-1") ;
        try(QueryExecution qExec = QueryExecutionFactory.create(qs, ds)) {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.consume(rs) ;
        }
    }

    // Expect QueryCancelledException. Slowly poll the results.
    private static void exceptionExpected(ResultSet rs, int initialWait, int pollInterval, int maxWaitMillis) {
        final int intervals = (maxWaitMillis-initialWait+1)/pollInterval;

        if ( initialWait > 0 )
            Lib.sleep(initialWait);

        long start = System.currentTimeMillis();
        long endTime = start + maxWaitMillis;
        // +1 for rounding error
        long now = start;
        for (int i = 0 ; i < intervals ; i++ ) {
            // May have waited (much) longer than the pollInterval : heavily loaded build systems.
            if ( now-start > maxWaitMillis )
                break;

            try {
                if ( ! rs.hasNext() )
                    break;
                rs.next();
            } catch (QueryCancelledException ex) { return; }

            Lib.sleep(pollInterval);
            now = System.currentTimeMillis();
        }
        fail("QueryCancelledException expected");
    }
}

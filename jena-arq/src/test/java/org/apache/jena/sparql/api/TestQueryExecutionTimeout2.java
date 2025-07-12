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

package org.apache.jena.sparql.api;

import static org.apache.jena.atlas.lib.Lib.sleep;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import org.apache.jena.base.Sys;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.query.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.AbstractIterHashJoin;
import org.apache.jena.sparql.engine.main.solver.StageMatchTriple;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecDataset;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.WrappedIterator;

public class TestQueryExecutionTimeout2
{
    // Testing related to JENA-440

    static private String prefix =
            "PREFIX f:       <http://example/ns#>\n"+
            "PREFIX afn:     <http://jena.apache.org/ARQ/function#>\n";
    static Graph                g   = TestQueryExecutionTimeout1.makeGraph(10);
    static DatasetGraph         dsg = DatasetGraphFactory.wrap(g);
    static Dataset              ds  = DatasetFactory.wrap(dsg);

    // Loaded CI.
    private static boolean mayBeErratic = Sys.isWindows || Sys.isMacOS;

    @BeforeEach public void beforeTest() {
        // These timeout test do not work reliably on MacOS in GH actions so for now
        // they are skipped. It looks like the server machines are heavily loaded and
        // as a result very long (10's seconds) pauses happen.
        // These tests need rewriting to cope with such an environment.
        assumeFalse(Sys.isMacOS);
    }

    @Test public void timeout_30()  { test2(200, 20, timeout(50, 250), true); }
    @Test public void timeout_31()  { test2(200, 100, 20, false); }

    // Make sure it isn't timeout1 - delay longer than timeout1
    @Test public void timeout_32()  { test2(100, 500, 200, false); }
    @Test public void timeout_33()  { test2(150, -1,  200, false); }

    @Test public void timeout_34()  { test2(10, 40, timeout(100, 250), true); }

    @Test public void timeout_35()  { test2(-1, 20, timeout(50, 250), true); }
    @Test public void timeout_36()  { test2(-1, 200, 20, false); }

    @Test public void timeout_37()  { test2(200, 200, 50, false); }
    @Test public void timeout_38()  { test2(200, -1, 50, false); }

    private static void test2(long timeout1, long timeout2, int delay, boolean exceptionExpected)
    {
        String qs = prefix+"SELECT * { ?s ?p ?o }";
        // Enough rows to keep the iterator pipeline full.
        try (QueryExecution qExec = QueryExecution.create().query(qs).dataset(ds)
                .initialTimeout(timeout1, TimeUnit.MILLISECONDS)
                .overallTimeout(timeout2, TimeUnit.MILLISECONDS)
                .build()) {
            ResultSet rs = qExec.execSelect();
            // ... wait for first binding.
            try {
                Binding b1 = rs.nextBinding();
            } catch (QueryCancelledException ex) {
                fail("QueryCancelledException not expected at start");
                return;
            }

            // ... then a possible timeout.
            sleep(delay);
            if ( exceptionExpected )
                exceptionExpected(rs);
            else
                noException(rs);
        }
    }

    private static void noException(ResultSet rs) {
        ResultSetFormatter.consume(rs);
    }

    private static void exceptionExpected(ResultSet rs) {
        try {
            ResultSetFormatter.consume(rs);
            fail("QueryCancelledException expected");
        } catch (QueryCancelledException ex) {}
    }

    private int timeout(int time1, int time2) {
        return mayBeErratic ? time2 : time1;
    }

    /**
     * Test case for GH-3044.
     * {@link AbstractIterHashJoin} used to eagerly populate a hash probe table on iterator construction,
     * while {@link QueryExecDataset} held a lock that prevented async abort while the iterator was being constructed.
     */
    @Test
    @Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
    public void test_timeout_hashJoin() {
        // A very large virtual graph.
        Graph graph = new GraphBase() {
            @Override
            protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
                return WrappedIterator.createNoRemove(LongStream.range(0, Long.MAX_VALUE)
                    .mapToObj(i -> NodeFactory.createURI("http://www.example.org/r" + i))
                    .peek(x -> {
                        // Throttle binding generation to prevent going out-ouf-memory.
                        // Bindings are likely to be stored in an in-memory hash probe table.
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(i -> Triple.create(i, i, i))
                    .filter(triplePattern::matches)
                    .iterator());
            }
        };

        try (QueryExec qe = QueryExec
            .graph(graph)
            .timeout(100, TimeUnit.MILLISECONDS)
            .query("""
            SELECT * {
                ?a ?b ?c .
                  # When this test case was written, a hash probe table was created for the rhs.
                  {
                    ?c ?d ?e .
                  }
                UNION
                  { BIND('x' AS ?x) }
            }
            """).build()) {
            assertThrows(QueryCancelledException.class, ()-> RowSetOps.count(qe.select()));
        }
    }

    /** Test to ensure timeouts are considered in {@link StageMatchTriple} when producing only empty joins from a large set of triples. */
    @Test
    public void test_timeout_stageMatchTriple() {
        // A very large virtual graph that never matches a concrete subject.
        Graph graph = new GraphBase() {
            @Override
            protected ExtendedIterator<Triple> graphBaseFind(Triple t) {
                if (t.getMatchSubject() != null && t.getMatchSubject().isConcrete()) {
                    // Don't match any concrete subject
                    return NiceIterator.emptyIterator();
                } else {
                    // Generate :sI :p :oI triples
                    return WrappedIterator.createNoRemove(LongStream.range(0, Long.MAX_VALUE)
                        .mapToObj(i -> Triple.create(NodeFactory.createURI("http://www.example.org/s" + i), t.getPredicate(), NodeFactory.createURI("http://www.example.org/o" + i)))
                        .iterator());
                }
            }
        };

        try (QueryExec qe = QueryExec
            .graph(graph)
            .timeout(100, TimeUnit.MILLISECONDS)
            .query("""
            SELECT * {
                ?a <urn:p> ?c .
                ?c <urn:p> ?e .
            }
            """).build()) {
            assertThrows(QueryCancelledException.class, ()-> RowSetOps.count(qe.select()));
        }
    }
}


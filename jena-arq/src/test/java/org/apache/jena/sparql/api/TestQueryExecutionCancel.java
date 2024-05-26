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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.function.FunctionRegistry ;
import org.apache.jena.sparql.function.library.wait ;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.apache.jena.sparql.sse.SSE;
import org.junit.AfterClass ;
import org.junit.Assert;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestQueryExecutionCancel {

    private static final String ns = "http://example/ns#" ;

    static Model m = GraphFactory.makeJenaDefaultModel() ;
    static Resource r1 = m.createResource() ;
    static Property p1 = m.createProperty(ns+"p1") ;
    static Property p2 = m.createProperty(ns+"p2") ;
    static Property p3 = m.createProperty(ns+"p3") ;
    static  {
        m.add(r1, p1, "x1") ;
        m.add(r1, p2, "X2") ; // NB Capital
        m.add(r1, p3, "y1") ;
    }

    @BeforeClass public static void beforeClass() { FunctionRegistry.get().put(ns + "wait", wait.class) ; }
    @AfterClass  public static void afterClass() { FunctionRegistry.get().remove(ns + "wait") ; }

    @Test(expected=QueryCancelledException.class)
    public void test_Cancel_API_1()
    {
        try(QueryExecution qExec = makeQExec("SELECT * {?s ?p ?o}")) {
            ResultSet rs = qExec.execSelect() ;
            assertTrue(rs.hasNext()) ;
            qExec.abort();
            assertTrue(rs.hasNext()) ;
            rs.nextSolution();
            assertFalse("Results not expected after cancel.", rs.hasNext()) ;
        }
    }

    @Test(expected=QueryCancelledException.class)
    public void test_Cancel_API_2()
    {
        try(QueryExecution qExec = makeQExec("PREFIX ex: <" + ns + "> SELECT * {?s ?p ?o . FILTER ex:wait(100) }")) {
            ResultSet rs = qExec.execSelect() ;
            assertTrue(rs.hasNext()) ;
            qExec.abort();
            assertTrue(rs.hasNext()) ;
            rs.nextSolution();
            assertFalse("Results not expected after cancel.", rs.hasNext()) ;
        }
    }

    @Test public void test_Cancel_API_3() throws InterruptedException
    {
        // Don't qExec.close on this thread.
        QueryExecution qExec = makeQExec("PREFIX ex: <" + ns + "> SELECT * { ?s ?p ?o . FILTER ex:wait(100) }") ;
        CancelThreadRunner thread = new CancelThreadRunner(qExec);
        thread.start();
        synchronized (qExec) { qExec.wait() ; }
        synchronized (qExec) { qExec.abort() ;}
        synchronized (qExec) { qExec.notify() ; }
        assertEquals (1, thread.getCount()) ;
    }

    @Test public void test_Cancel_API_4() throws InterruptedException
    {
        // Don't qExec.close on this thread.
        QueryExecution qExec = makeQExec("PREFIX ex: <" + ns + "> SELECT * { ?s ?p ?o } ORDER BY ex:wait(100)") ;
        CancelThreadRunner thread = new CancelThreadRunner(qExec);
        thread.start();
        synchronized (qExec) { qExec.wait() ; }
        synchronized (qExec) { qExec.abort(); }
        synchronized (qExec) { qExec.notify() ; }
        assertEquals (1, thread.getCount()) ;
    }

    @Test(expected = QueryCancelledException.class)
    public void test_Cancel_API_5() {
        try (QueryExecution qe = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", m)) {
            qe.abort();
            ResultSetFormatter.consume(qe.execSelect());
        }
    }

    private QueryExecution makeQExec(String queryString)
    {
        Query q = QueryFactory.create(queryString) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, m) ;
        return qExec ;
    }

    class CancelThreadRunner extends Thread
    {
        private QueryExecution qExec = null ;
        private int count = 0 ;

        public CancelThreadRunner(QueryExecution qExec)
        {
            this.qExec = qExec ;
        }

        @Override
        public void run()
        {
            try
            {
                ResultSet rs = qExec.execSelect() ;
                while ( rs.hasNext() )
                {
                    rs.nextSolution() ;
                    count++ ;
                    synchronized (qExec) { qExec.notify() ; }
                    synchronized (qExec) { qExec.wait() ; }
                }
            }
            catch (QueryCancelledException e) {}
            catch (InterruptedException e) {
                e.printStackTrace();
            } finally { qExec.close() ; }
        }

        public int getCount()
        {
            return count ;
        }
    }

    @Test
    public void test_cancel_select_1() {
        cancellationTest("SELECT * {}", QueryExec::select);
    }

    @Test
    public void test_cancel_select_2() {
        cancellationTest("SELECT * {}", QueryExec::select, Iterator::hasNext);
    }

    @Test
    public void test_cancel_ask() {
        cancellationTest("ASK {}", QueryExec::ask);
    }

    @Test
    public void test_cancel_construct() {
        cancellationTest("CONSTRUCT WHERE {}", QueryExec::construct);
    }

    @Test
    public void test_cancel_describe() {
        cancellationTest("DESCRIBE * {}", QueryExec::describe);
    }

    @Test
    public void test_cancel_construct_dataset() {
        cancellationTest("CONSTRUCT{} WHERE{}", QueryExec::constructDataset);
    }

    @Test
    public void test_cancel_construct_triples_1() {
        cancellationTest("CONSTRUCT{} WHERE{}", QueryExec::constructTriples, Iterator::hasNext);
    }

    @Test
    public void test_cancel_construct_triples_2() {
        cancellationTest("CONSTRUCT{} WHERE{}", QueryExec::constructTriples);
    }

    @Test
    public void test_cancel_construct_quads_1() {
        cancellationTest("CONSTRUCT{} WHERE{}", QueryExec::constructQuads, Iterator::hasNext);
    }

    @Test
    public void test_cancel_construct_quads_2() {
        cancellationTest("CONSTRUCT{} WHERE{}", QueryExec::constructQuads);
    }

    @Test
    public void test_cancel_json() {
        cancellationTest("JSON {\":a\": \"b\"} WHERE {}", exec->exec.execJson().get(0));
    }

    static <T> void cancellationTest(String queryString, Function<QueryExec, Iterator<T>> itFactory, Consumer<Iterator<T>> itConsumer) {
        cancellationTest(queryString, itFactory::apply);
        cancellationTestForIterator(queryString, itFactory, itConsumer);
    }

    /** Abort the query exec and expect all execution methods to fail */
    static void cancellationTest(String queryString, Consumer<QueryExec> execAction) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        dsg.add(SSE.parseQuad("(_ :s :p :o)"));
        try(QueryExec aExec = QueryExec.dataset(dsg).query(queryString).build()) {
            aExec.abort();
            assertThrows(QueryCancelledException.class, ()-> execAction.accept(aExec));
        }
    }

    /** Obtain an iterator and only afterwards abort the query exec.
     * Operations on the iterator are now expected to fail. */
    static <T> void cancellationTestForIterator(String queryString, Function<QueryExec, Iterator<T>> itFactory, Consumer<Iterator<T>> itConsumer) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        dsg.add(SSE.parseQuad("(_ :s :p :o)"));
        try(QueryExec aExec = QueryExec.dataset(dsg).query(queryString).build()) {
            Iterator<T> it = itFactory.apply(aExec);
            aExec.abort();
            assertThrows(QueryCancelledException.class, ()-> itConsumer.accept(it));
        }
    }

    /**
     * Test that creates iterators over a billion result rows and attempts to cancel them.
     * If this test hangs then it is likely that something went wrong in the cancellation machinery.
     */
    @Test(timeout = 10000)
    public void test_cancel_concurrent_1() {
        int maxCancelDelayInMillis = 100;

        int cpuCount = Runtime.getRuntime().availableProcessors();
        // Spend at most roughly 1 second per cpu (10 tasks a max 100ms)
        int taskCount = cpuCount * 10;

        // Create a model with 1000 triples
        Graph graph = GraphFactory.createDefaultGraph();
        IntStream.range(0, 1000)
            .mapToObj(i -> NodeFactory.createURI("http://www.example.org/r" + i))
            .forEach(node -> graph.add(node, node, node));
        Model model = ModelFactory.createModelForGraph(graph);

        // Create a query that creates 3 cross joins - resulting in one billion result rows
        Query query = QueryFactory.create("SELECT * { ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . }");
        Callable<QueryExecution> qeFactory = () -> QueryExecutionFactory.create(query, model);

        runConcurrentAbort(taskCount, maxCancelDelayInMillis, qeFactory, TestQueryExecutionCancel::doCount);
    }

    private static final int doCount(QueryExecution qe) {
        try (QueryExecution qe2 = qe) {
            ResultSet rs = qe2.execSelect();
            int size = ResultSetFormatter.consume(rs);
            return size;
        }
    }

    /**
     * Reusable method that creates a parallel stream that starts query executions
     * and schedules cancel tasks on a separate thread pool.
     */
    public static void runConcurrentAbort(int taskCount, int maxCancelDelay, Callable<QueryExecution> qeFactory, Function<QueryExecution, ?> processor) {
        Random cancelDelayRandom = new Random();
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            List<Integer> list = IntStream.range(0, taskCount).boxed().collect(Collectors.toList());
            list
                .parallelStream()
                .forEach(i -> {
                    QueryExecution qe;
                    try {
                        qe = qeFactory.call();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to build a query execution", e);
                    }
                    Future<?> future = executorService.submit(() -> processor.apply(qe));
                    int delayToAbort = cancelDelayRandom.nextInt(maxCancelDelay);
                    try {
                        Thread.sleep(delayToAbort);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    // System.out.println("Abort: " + qe);
                    qe.abort();
                    try {
                        // System.out.println("Waiting for: " + qe);
                        future.get();
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        if (!(cause instanceof QueryCancelledException)) {
                            // Unexpected exception - print out the stack trace
                            e.printStackTrace();
                        }
                        Assert.assertEquals(QueryCancelledException.class, cause.getClass());
                    } catch (InterruptedException e) {
                        // Ignored
                    } finally {
                        // System.out.println("Completed: " + qe);
                    }
                });
        } finally {
            executorService.shutdownNow();
        }
    }
}

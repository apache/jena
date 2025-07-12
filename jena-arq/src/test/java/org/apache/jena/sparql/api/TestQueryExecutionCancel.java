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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCheck;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCheck.OpenIteratorException;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase0;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.function.library.wait;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class TestQueryExecutionCancel {

    private static final String ns = "http://example/ns#";

    static Model m = GraphFactory.makeJenaDefaultModel();
    static Resource r1 = m.createResource();
    static Property p1 = m.createProperty(ns+"p1");
    static Property p2 = m.createProperty(ns+"p2");
    static Property p3 = m.createProperty(ns+"p3");
    static  {
        m.add(r1, p1, "x1");
        m.add(r1, p2, "X2"); // NB Capital
        m.add(r1, p3, "y1");
    }

    @BeforeAll public static void beforeClass() { FunctionRegistry.get().put(ns + "wait", wait.class); }
    //@AfterAll  public static void afterClass() { FunctionRegistry.get().remove(ns + "wait"); }

    @Test
    public void test_Cancel_API_1()
    {
		try(QueryExecution qExec = makeQExec("SELECT * {?s ?p ?o}")) {
            ResultSet rs = qExec.execSelect();
            assertTrue(rs.hasNext());
            qExec.abort();
			assertThrows(QueryCancelledException.class,
						 ()-> rs.nextSolution(),
						 ()->"Results not expected after cancel.");
        }
    }

    @Test
    public void test_Cancel_API_2()
    {
        try(QueryExecution qExec = makeQExec("PREFIX ex: <" + ns + "> SELECT * {?s ?p ?o . FILTER ex:wait(100) }")) {
            ResultSet rs = qExec.execSelect();
            assertTrue(rs.hasNext());
            qExec.abort();
            assertThrows(QueryCancelledException.class,
						 ()-> rs.hasNext(),
						 ()->"Results not expected after cancel.");
        }
    }

    @Test public void test_Cancel_API_3() throws InterruptedException
    {
        // Don't qExec.close on this thread.
        QueryExecution qExec = makeQExec("PREFIX ex: <" + ns + "> SELECT * { ?s ?p ?o . FILTER ex:wait(100) }");
        CancelThreadRunner thread = new CancelThreadRunner(qExec);
        thread.start();
        synchronized (qExec) { qExec.wait(); }
        synchronized (qExec) { qExec.abort();}
        synchronized (qExec) { qExec.notify(); }
        assertEquals (1, thread.getCount());
    }

    @Test public void test_Cancel_API_4() throws InterruptedException
    {
        // Don't qExec.close on this thread.
        QueryExecution qExec = makeQExec("PREFIX ex: <" + ns + "> SELECT * { ?s ?p ?o } ORDER BY ex:wait(100)");
        CancelThreadRunner thread = new CancelThreadRunner(qExec);
        thread.start();
        synchronized (qExec) { qExec.wait(); }
        synchronized (qExec) { qExec.abort(); }
        synchronized (qExec) { qExec.notify(); }
        assertEquals (1, thread.getCount());
    }

    @Test
    public void test_Cancel_API_5() {
        try (QueryExecution qe = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", m)) {
            qe.abort();
			assertThrows(QueryCancelledException.class, ()-> ResultSetFormatter.consume(qe.execSelect()));
        }
    }

    private QueryExecution makeQExec(String queryString)
    {
        Query q = QueryFactory.create(queryString);
        QueryExecution qExec = QueryExecutionFactory.create(q, m);
        return qExec;
    }

    class CancelThreadRunner extends Thread
    {
        private QueryExecution qExec = null;
        private int count = 0;

        public CancelThreadRunner(QueryExecution qExec)
        {
            this.qExec = qExec;
        }

        @Override
        public void run()
        {
            try
            {
                ResultSet rs = qExec.execSelect();
                while ( rs.hasNext() )
                {
                    rs.nextSolution();
                    count++;
                    synchronized (qExec) { qExec.notify(); }
                    synchronized (qExec) { qExec.wait(); }
                }
            }
            catch (QueryCancelledException e) {}
            catch (InterruptedException e) {
                e.printStackTrace();
            } finally { qExec.close(); }
        }

        public int getCount()
        {
            return count;
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

    /** Set cancel signal function via {@link QueryExecBuilder#set(Symbol, Object)}. */
    @Test
    public void test_cancel_signal_1() {
        DatasetGraph dsg = DatasetGraphFactory.create();
        FunctionRegistry fnReg = registerCancelSignalFunction(new FunctionRegistry());
        try (QueryExec qe = QueryExec.dataset(dsg).query("SELECT (<urn:cancelSignal>() AS ?foobar) { }")
                .set(ARQConstants.registryFunctions, fnReg)
                .build()) {
            assertEquals(1, ResultSetFormatter.consume(ResultSet.adapt(qe.select())));
        }
    }

    /** Set cancel signal function via {@link QueryExecBuilder#context(Context)}. */
    @Test
    public void test_cancel_signal_2() {
        DatasetGraph dsg = DatasetGraphFactory.create();
        Context cxt = ARQ.getContext().copy();
        FunctionRegistry fnReg = registerCancelSignalFunction(new FunctionRegistry());
        FunctionRegistry.set(cxt, fnReg);
        try (QueryExec qe = QueryExec.dataset(dsg).query("SELECT (<urn:cancelSignal>() AS ?foobar) { }").context(cxt).build()) {
            assertEquals(1, ResultSetFormatter.consume(ResultSet.adapt(qe.select())));
        }
    }

    /** Set cancel signal function via {@link QueryExec#getContext()}. */
    @Test
    public void test_cancel_signal_3() {
        DatasetGraph dsg = DatasetGraphFactory.create();
        try (QueryExec qe = QueryExec.dataset(dsg).query("SELECT (<urn:cancelSignal>() AS ?foobar) { }").build()) {
            FunctionRegistry fnReg = registerCancelSignalFunction(new FunctionRegistry());
            FunctionRegistry.set(qe.getContext(), fnReg);
            ResultSetFormatter.consume(ResultSet.adapt(qe.select()));
        }
    }

    /** Registers the function <urn:cancelSignal> which returns its value if present.
     *  A RuntimeException is raised if there is no cancel signal in the execution context. */
    static FunctionRegistry registerCancelSignalFunction(FunctionRegistry fnReg) {
        fnReg.put("urn:cancelSignal", iri -> new FunctionBase0() {
            @Override
            protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
                ExecutionContext execCxt = (ExecutionContext)env;
                AtomicBoolean cancelSignal = execCxt.getCancelSignal();
                if (cancelSignal == null) {
                    throw new RuntimeException("No cancel signal in execution context.");
                }
                return NodeValue.makeBoolean(cancelSignal.get());
            }

            @Override
            public NodeValue exec() {
                throw new IllegalStateException("Should never be called");
            }
        });

        return fnReg;
    }

    /** Create a model with 1000 triples. */
    static Graph createTestGraph() {
        Graph graph = GraphFactory.createDefaultGraph();
        IntStream.range(0, 1000)
            .mapToObj(i -> NodeFactory.createURI("http://www.example.org/r" + i))
            .forEach(node -> graph.add(node, node, node));
        return graph;
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
     *  Operations on the iterator are now expected to fail. */
    static <T> void cancellationTestForIterator(String queryString, Function<QueryExec, Iterator<T>> itFactory, Consumer<Iterator<T>> itConsumer) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        dsg.add(SSE.parseQuad("(_ :s :p :o)"));
        try(QueryExec aExec = QueryExec.dataset(dsg).query(queryString).build()) {
            Iterator<T> it = itFactory.apply(aExec);
            aExec.abort();
            assertThrows(QueryCancelledException.class, ()-> itConsumer.accept(it));
        }
    }

    /** Test that creates iterators over a billion result rows and attempts to cancel them.
     *  If this test hangs then it is likely that something went wrong in the cancellation machinery. */
    @Test
    @Timeout(value = 10000, unit=TimeUnit.MILLISECONDS)
    public void test_cancel_concurrent_1() {
        // Create a query that creates 3 cross joins - resulting in one billion result rows.
        test_cancel_concurrent("SELECT * { ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . }");
    }

    @Test
    @Timeout(value = 10000, unit=TimeUnit.MILLISECONDS)
    public void test_cancel_concurrent_2() {
        // Create a query that creates 3 cross joins - resulting in one billion result rows.
        // Tests against additional operators, namely UNION and BIND.
        test_cancel_concurrent("SELECT * { { ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . } UNION { BIND('x' AS ?x) } }");
    }

    private static void test_cancel_concurrent(String queryString) {
        int maxCancelDelayInMillis = 100;

        int cpuCount = Runtime.getRuntime().availableProcessors();
        // Spend at most roughly 1 second per cpu (10 tasks a max 100ms)
        int taskCount = cpuCount * 10;

        // Create a model with 1000 triples
        Model model = ModelFactory.createModelForGraph(createTestGraph());

        Query query = QueryFactory.create(queryString);
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

    /** Reusable method that creates a parallel stream that starts query executions
     *  and schedules cancel tasks on a separate thread pool. */
    public static void runConcurrentAbort(int taskCount, int maxCancelDelay, Callable<QueryExecution> qeFactory, Function<QueryExecution, ?> processor) {
        Random cancelDelayRandom = new Random();
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            List<Integer> list = IntStream.range(0, taskCount).boxed().toList();
            list
                .parallelStream()
                .forEach(i -> {
                    QueryExecution qe;
                    try {
                        qe = qeFactory.call();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to build a query execution", e);
                    }

                    // Fail if any iterators are not properly closed
                    qe.getContext().set(QueryIteratorCheck.failOnOpenIterator, true);

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
                        assertEquals(QueryCancelledException.class, cause.getClass());

                        boolean hasOpenIterators = Arrays.stream(cause.getSuppressed())
                                .anyMatch(x -> x instanceof OpenIteratorException);
                        if (hasOpenIterators) {
                            throw new RuntimeException("Encountered open iterators.", e);
                        }

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

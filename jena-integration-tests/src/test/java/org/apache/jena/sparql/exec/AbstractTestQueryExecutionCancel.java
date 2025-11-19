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

package org.apache.jena.sparql.exec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCheck;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCheck.OpenIteratorException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase0;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.function.library.wait;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public abstract class AbstractTestQueryExecutionCancel {

    public abstract Dataset createDataset();

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
        cancellationTest("SELECT * {}", QueryExecution::execSelect);
    }

    @Test
    public void test_cancel_select_2() {
        cancellationTest("SELECT * {}", QueryExecution::execSelect, Iterator::hasNext);
    }

    @Test
    public void test_cancel_ask() {
        cancellationTest("ASK {}", QueryExecution::execAsk);
    }

    @Test
    public void test_cancel_construct() {
        cancellationTest("CONSTRUCT WHERE {}", QueryExecution::execConstruct);
    }

    @Test
    public void test_cancel_describe() {
        cancellationTest("DESCRIBE * {}", QueryExecution::execDescribe);
    }

    @Test
    public void test_cancel_construct_dataset() {
        cancellationTest("CONSTRUCT{} WHERE{}", QueryExecution::execConstructDataset);
    }

    @Test
    public void test_cancel_construct_triples_1() {
        cancellationTest("CONSTRUCT{} WHERE{}", QueryExecution::execConstructTriples, Iterator::hasNext);
    }

    @Test
    public void test_cancel_construct_triples_2() {
        cancellationTest("CONSTRUCT{} WHERE{}", QueryExecution::execConstructTriples);
    }

    @Test
    public void test_cancel_construct_quads_1() {
        cancellationTest("CONSTRUCT{} WHERE{}", QueryExecution::execConstructQuads, Iterator::hasNext);
    }

    @Test
    public void test_cancel_construct_quads_2() {
        cancellationTest("CONSTRUCT{} WHERE{}", QueryExecution::execConstructQuads);
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

    static void generateTestData(Graph graph, int size) {
        IntStream.range(0, size)
            .mapToObj(i -> NodeFactory.createURI("http://www.example.org/r" + i))
            .forEach(node -> graph.add(node, node, node));
    }

    public <T> void cancellationTest(String queryString, Function<QueryExecution, Iterator<T>> itFactory, Consumer<Iterator<T>> itConsumer) {
        cancellationTest(queryString, itFactory::apply);
        cancellationTestForIterator(queryString, itFactory, itConsumer);
    }

    /** Abort the query exec and expect all execution methods to fail */
    public void cancellationTest(String queryString, Consumer<QueryExecution> execAction) {
        Dataset ds = createDataset();
        try (AutoTxn txn = Txn.autoTxn(ds, ReadWrite.WRITE)) {
            ds.asDatasetGraph().add(SSE.parseQuad("(_ :s :p :o)"));
            txn.commit();
        }
        try (AutoTxn txn = Txn.autoTxn(ds, ReadWrite.READ);
             QueryExecution exec = QueryExecution.dataset(ds).query(queryString).build()) {
            exec.abort();
            assertThrows(QueryCancelledException.class, ()-> execAction.accept(exec));
        }
    }

    /** Obtain an iterator and only afterwards abort the query exec.
     *  Operations on the iterator are now expected to fail. */
    public <T> void cancellationTestForIterator(String queryString, Function<QueryExecution, Iterator<T>> itFactory, Consumer<Iterator<T>> itConsumer) {
        Dataset ds = createDataset();
        try (AutoTxn txn = Txn.autoTxn(ds, ReadWrite.WRITE)) {
            ds.asDatasetGraph().add(SSE.parseQuad("(_ :s :p :o)"));
            txn.commit();
        }
        try (AutoTxn txn = Txn.autoTxn(ds, ReadWrite.READ);
             QueryExecution exec = QueryExecution.dataset(ds).query(queryString).build()) {
            Iterator<T> it = itFactory.apply(exec);
            exec.abort();
            assertThrows(QueryCancelledException.class, ()-> itConsumer.accept(it));
        }
    }

    /** Test that creates iterators over a billion result rows and attempts to cancel them.
     *  If this test hangs then it is likely that something went wrong in the cancellation machinery. */
    @Test
    @Timeout(value = 10000, unit=TimeUnit.MILLISECONDS)
    public void test_cancel_concurrent_1() {
        // Create a query that creates 3 cross joins - resulting in one billion result rows.
        test_cancel_concurrent(1000, "SELECT * { ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . }");
    }

    @Test
    @Timeout(value = 10000, unit=TimeUnit.MILLISECONDS)
    public void test_cancel_concurrent_2() {
        // Create a query that creates 3 cross joins - resulting in one billion result rows.
        // Tests against additional operators, namely UNION and BIND.
        test_cancel_concurrent(1000, "SELECT * { { ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . } UNION { BIND('x' AS ?x) } }");
    }

    @Test
    @Timeout(value = 10000, unit=TimeUnit.MILLISECONDS)
    public void test_cancel_concurrent_3() {
        test_cancel_concurrent(
            1000,
            """
                SELECT * {
                  ?s ?p ?o
                  {
                    SELECT * {
                      ?x ?y ?z
                    }
                    LIMIT 1000000
                  }
                  ?s ?p ?o
                }
                LIMIT 1000000
            """);
    }

    private void test_cancel_concurrent(int testDataSize, String queryString) {
        int maxCancelDelayInMillis = 100;

        int cpuCount = Runtime.getRuntime().availableProcessors();
        // Spend at most roughly 1 second per cpu (10 tasks a max 100ms)
        int taskCount = cpuCount * 10;

        // Create a model with 1000 triples
        Dataset ds = createDataset();
        try (AutoTxn txn = Txn.autoTxn(ds, ReadWrite.WRITE)) {
            generateTestData(ds.asDatasetGraph().getDefaultGraph(), testDataSize);
            ds.commit();
        }

        Query query = QueryFactory.create(queryString);
        Callable<QueryExecution> qeFactory = () -> QueryExecutionFactory.create(query, ds);
        runConcurrentAbort(taskCount, maxCancelDelayInMillis, ds, qeFactory, AbstractTestQueryExecutionCancel::doCount);
    }

    private static final int doCount(QueryExecution qe) {
        ResultSet rs = qe.execSelect();
        int size = ResultSetFormatter.consume(rs);
        return size;
    }

    /** Reusable method that creates a parallel stream that starts query executions
     *  and schedules cancel tasks on a separate thread pool. */
    public static void runConcurrentAbort(int taskCount, int maxCancelDelay, Transactional ds, Callable<QueryExecution> qeFactory, Function<QueryExecution, ?> processor) {
        Random cancelDelayRandom = new Random();
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            List<Integer> list = IntStream.range(0, taskCount).boxed().toList();
            list
                .parallelStream()
                .forEach(i -> {
                    try (AutoTxn txn = Txn.autoTxn(ds, ReadWrite.READ);
                         QueryExecution qe = qeFactory.call()) {
                        // Fail if any iterators are not properly closed
                        qe.getContext().set(QueryIteratorCheck.failOnOpenIterator, true);

                        // Schedule the concurrent abort action with random delay.
                        int delayToAbort = cancelDelayRandom.nextInt(maxCancelDelay);
                        boolean[] abortDone = {false};
                        Future<?> future = executorService.submit(() -> {
                            try {
                                Thread.sleep(delayToAbort);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            // System.out.println("Abort: " + qe);
                            abortDone[0] = true;
                            qe.abort();
                        });

                        // Meanwhile start the query execution.
                        try {
                            processor.apply(qe);
                        } catch (Throwable e) {
                            if (!(e instanceof QueryCancelledException)) {
                                // Unexpected exception - print out the stack trace
                                e.printStackTrace();
                            }
                            assertEquals(QueryCancelledException.class, e.getClass());

                            boolean hasOpenIterators = Arrays.stream(e.getSuppressed())
                                    .anyMatch(x -> x instanceof OpenIteratorException);
                            if (hasOpenIterators) {
                                throw new RuntimeException("Encountered open iterators.", e);
                            }
                        }

                        // The query has completed. Cancel the abort thread if it hasn't done so yet.
                        if (!abortDone[0]) {
                            future.cancel(true);
                        }
                        try {
                            future.get();
                        } catch (CancellationException e) {
                            // In this test setup, it is an error for a query to complete before abort gets called.
                            throw new RuntimeException("Query completed too early", e);
                        } catch (Exception e) {
                            // Should not happen.
                            throw new RuntimeException(e);
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        } finally {
            executorService.shutdownNow();
        }
    }
}

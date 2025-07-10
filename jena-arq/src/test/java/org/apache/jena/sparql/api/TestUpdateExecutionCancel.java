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

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class TestUpdateExecutionCancel {

    /** Set cancel signal function via {@link UpdateExecBuilder#set(Symbol, Object)}. */
    @Test
    public void test_cancel_signal_1() {
        DatasetGraph dsg = DatasetGraphFactory.create();
        FunctionRegistry fnReg = TestQueryExecutionCancel.registerCancelSignalFunction(new FunctionRegistry());
        UpdateExec ue = UpdateExec.dataset(dsg)
                .update("INSERT { <s> <p> ?o } WHERE { BIND(<urn:cancelSignal>() AS ?o) }")
                .set(ARQConstants.registryFunctions, fnReg)
                .build();
        ue.execute();
        assertEquals(1, Iter.count(dsg.find()));
    }

    /** Set cancel signal function via {@link UpdateExecBuilder#context(Context)}. */
    @Test
    public void test_cancel_signal_2() {
        DatasetGraph dsg = DatasetGraphFactory.create();
        Context cxt = ARQ.getContext().copy();
        FunctionRegistry fnReg = TestQueryExecutionCancel.registerCancelSignalFunction(new FunctionRegistry());
        FunctionRegistry.set(cxt, fnReg);
        UpdateExec ue = UpdateExec.dataset(dsg)
                .update("INSERT { <s> <p> ?o } WHERE { BIND(<urn:cancelSignal>() AS ?o) }")
                .context(cxt)
                .build();
        ue.execute();
        assertEquals(1, Iter.count(dsg.find()));
    }

    /** Set cancel signal function via {@link UpdateExec#getContext()}. */
    @Test
    public void test_cancel_signal_3() {
        DatasetGraph dsg = DatasetGraphFactory.create();
        UpdateExec ue = UpdateExec.dataset(dsg)
                .update("INSERT { <s> <p> ?o } WHERE { BIND(<urn:cancelSignal>() AS ?o) }")
                .build();
        Context cxt = ue.getContext();
        FunctionRegistry fnReg = TestQueryExecutionCancel.registerCancelSignalFunction(new FunctionRegistry());
        FunctionRegistry.set(cxt, fnReg);
        ue.execute();
        assertEquals(1, Iter.count(dsg.find()));
    }

    @Test
    @Timeout(value=5000, unit=TimeUnit.MILLISECONDS)
    public void test_update_cancel_1() {
        Graph graph = TestQueryExecutionCancel.createTestGraph();
        // Create an insert whose WHERE clause creates 3 cross joins a 1000 triples/bindings.
        // This would result in one billion result rows.

        assertThrows(QueryCancelledException.class,()->
            UpdateExec
                .dataset(graph)
                // No-op delete followed by insert indirectly tests that timeout is applied to overall update request.
                .update("DELETE { <s> <p> <o> } WHERE { ?a ?b ?c }; INSERT { <s> <p> <o> } WHERE { ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . }")
                .timeout(50, TimeUnit.MILLISECONDS)
                .build()
                .execute()
                );
    }

    /** Test that creates iterators over a billion result rows and attempts to cancel them.
     *  If this test hangs then it is likely that something went wrong in the cancellation machinery. */
    @Test
    @Timeout(value=10000, unit=TimeUnit.MILLISECONDS)
    public void test_cancel_concurrent_1() {
        int maxCancelDelayInMillis = 100;

        int cpuCount = Runtime.getRuntime().availableProcessors();
        // Spend at most roughly 1 second per cpu (10 tasks a max 100ms)
        int taskCount = cpuCount * 10;

        // Create a model with 1000 triples
        Dataset dataset = DatasetFactory.wrap(DatasetGraphFactory.wrap(TestQueryExecutionCancel.createTestGraph()));

        // Create a query that creates 3 cross joins - resulting in one billion result rows
        UpdateRequest updateRequest = UpdateFactory.create("INSERT { <s> <p> <o> } WHERE { ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . }");
        Callable<UpdateProcessor> qeFactory = () -> UpdateExecutionFactory.create(updateRequest, dataset);

        runConcurrentAbort(taskCount, maxCancelDelayInMillis, qeFactory);
    }

    /** Reusable method that creates a parallel stream that starts query executions
     *  and schedules cancel tasks on a separate thread pool. */
    public static void runConcurrentAbort(int taskCount, int maxCancelDelay, Callable<UpdateProcessor> upFactory) {
        Random cancelDelayRandom = new Random();
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            List<Integer> list = IntStream.range(0, taskCount).boxed().collect(Collectors.toList());
            list
                .parallelStream()
                .forEach(i -> {
                    UpdateProcessor up;
                    try {
                        up = upFactory.call();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to build a query execution", e);
                    }
                    Future<?> future = executorService.submit(() -> up.execute());
                    int delayToAbort = cancelDelayRandom.nextInt(maxCancelDelay);
                    try {
                        Thread.sleep(delayToAbort);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    // System.out.println("Abort: " + qe);
                    up.abort();
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

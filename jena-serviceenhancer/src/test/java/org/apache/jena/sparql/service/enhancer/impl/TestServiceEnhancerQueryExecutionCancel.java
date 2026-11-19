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

package org.apache.jena.sparql.service.enhancer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;


public class TestServiceEnhancerQueryExecutionCancel {

    /** Test cancellation of caching a large result set. */
    @Test
    public void test_01() {
        // LogCtl.setLogging();

        int maxCancelDelayInMillis = 100;

        int cpuCount = Runtime.getRuntime().availableProcessors();
        // Spend at most roughly 1 second per cpu (10 tasks a max 100ms)
        int taskCount = cpuCount * 10;

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(1000);

        // Produce a sufficiently large result set so that abort will surely hit in mid-execution
        // Query query = QueryFactory.create("SELECT * { SERVICE <cache:> { ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . ?j ?k ?l } }");
        Query query = QueryFactory.create("SELECT * { SERVICE <bulk+5:> { ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . ?j ?k ?l } }");

        // The query without the cache block:
        // Query query = QueryFactory.create("SELECT * { ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . ?j ?k ?l }");

        Callable<QueryExecution> qeFactory = () -> QueryExecutionFactory.create(query, model);

        runConcurrentAbort(taskCount, maxCancelDelayInMillis, qeFactory, TestServiceEnhancerQueryExecutionCancel::doCount);
    }

    /**
     * Copy of TestQueryExecutionCancel.runConcurrentAbort.
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

    /**
     * Copy of TestQueryExecutionCancel.doCount.
     */
    private static final int doCount(QueryExecution qe) {
        try (QueryExecution qe2 = qe) {
            ResultSet rs = qe2.execSelect();
            int size = ResultSetFormatter.consume(rs);
            return size;
        }
    }
}

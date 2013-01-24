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

package org.apache.jena.riot.lang;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.apache.jena.atlas.lib.Tuple;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.NodeFactory;

/**
 * Tests for the {@link StreamedRDFIterator} implementations
 * 
 */
public class TestStreamedRDFIterators {
    private static ExecutorService executor;

    /**
     * Create our thread pool
     */
    @BeforeClass
    public static void setup() {
        // We use far more than the required 2 threads to avoid intermittent
        // deadlock issues
        // that can otherwise occur
        executor = Executors.newFixedThreadPool(10);
    }

    /**
     * Destroy our thread pool
     * 
     * @throws InterruptedException
     */
    @AfterClass
    public static void teardown() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    private void test_streamed_triples(int bufferSize, final int generateSize, boolean fair) throws InterruptedException,
            ExecutionException, TimeoutException {

        // Create the StreamedTriplesIterator with a small buffer
        final StreamedTriplesIterator stream = new StreamedTriplesIterator(bufferSize, fair);

        // Create a runnable that will generate triples
        Runnable genTriples = new Runnable() {

            @Override
            public void run() {
                stream.start();
                // Generate triples
                for (int i = 1; i <= generateSize; i++) {
                    Triple t = new Triple(Node.createAnon(), Node.createURI("http://predicate"), NodeFactory.intToNode(i));
                    stream.triple(t);
                }
                stream.finish();
                return;
            }
        };

        // Create a runnable that will consume triples
        Callable<Integer> consumeTriples = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                int count = 0;
                while (stream.hasNext()) {
                    stream.next();
                    count++;
                }
                return count;
            }
        };

        // Run the threads
        Future<?> genResult = executor.submit(genTriples);
        Future<Integer> result = executor.submit(consumeTriples);
        Integer count = 0;
        try {
            count = result.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            //Check that it wasn't the producer thread erroring that caused us to time out
            genResult.get();
            //It wasn't so throw the original error
            throw e;
        }
        Assert.assertEquals(generateSize, (int) count);
    }

    /**
     * Test that blocking and waiting work nicely
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_triples_iterator_01() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is tiny
        this.test_streamed_triples(1, 100, true);
    }

    /**
     * Test that blocking and waiting work nicely
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_triples_iterator_02() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is much smaller than generated triples
        this.test_streamed_triples(10, 1000, false);
    }

    /**
     * Test that blocking and waiting work nicely
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_triples_iterator_03() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is smaller than generated triples
        this.test_streamed_triples(100, 1000, false);
    }

    /**
     * Test where blocking should rarely occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_triples_iterator_04() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is same as generated triples
        this.test_streamed_triples(1000, 1000, false);
    }

    /**
     * Test where blocking should rarely occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_triples_iterator_05() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is much larger than generated triples
        this.test_streamed_triples(10000, 1000, false);
    }

    /**
     * Test where blocking may occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_triples_iterator_06() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is small relative to generated triples
        this.test_streamed_triples(1000, 100000, false);
    }

    /**
     * Test where blocking may occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_triples_iterator_07() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is small relative to generated triples
        this.test_streamed_triples(10000, 100000, false);
    }

    private void test_streamed_quads(int bufferSize, final int generateSize, boolean fair) throws InterruptedException,
            ExecutionException, TimeoutException {
        // Create the StreamedQuadsIterator
        final StreamedQuadsIterator stream = new StreamedQuadsIterator(bufferSize, fair);

        // Create a runnable that will generate quads
        Runnable genQuads = new Runnable() {

            @Override
            public void run() {
                stream.start();
                // Generate quads
                for (int i = 1; i <= generateSize; i++) {
                    Quad q = new Quad(Node.createURI("http://graph"), Node.createAnon(), Node.createURI("http://predicate"),
                            NodeFactory.intToNode(i));
                    stream.quad(q);
                }
                stream.finish();
                return;
            }
        };

        // Create a runnable that will consume quads
        Callable<Integer> consumeQuads = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                int count = 0;
                while (stream.hasNext()) {
                    stream.next();
                    count++;
                }
                return count;
            }
        };

        // Run the threads
        Future<?> genResult = executor.submit(genQuads);
        Future<Integer> result = executor.submit(consumeQuads);
        Integer count = 0;
        try {
            count = result.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            //Check that it wasn't the producer thread erroring that caused us to time out
            genResult.get();
            //It wasn't so throw the original error
            throw e;
        }
        Assert.assertEquals(generateSize, (int) count);
    }

    /**
     * Test that blocking and waiting work nicely
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_quads_iterator_01() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is tiny
        this.test_streamed_quads(1, 100, true);
    }

    /**
     * Test that blocking and waiting work nicely
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_quads_iterator_02() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is much smaller than generated quads
        this.test_streamed_quads(10, 1000, false);
    }

    /**
     * Test that blocking and waiting work nicely
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_quads_iterator_03() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is smaller than generated quads
        this.test_streamed_quads(100, 1000, false);
    }

    /**
     * Test where blocking should rarely occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_quads_iterator_04() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is same as generated quads
        this.test_streamed_quads(1000, 1000, false);
    }

    /**
     * Test where blocking should rarely occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_quads_iterator_05() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is much larger than generated quads
        this.test_streamed_quads(10000, 1000, false);
    }

    /**
     * Test where blocking may occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_quads_iterator_06() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is small relative to generated quads
        this.test_streamed_quads(1000, 100000, false);
    }

    /**
     * Test where blocking may occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_quads_iterator_07() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is small relative to generated quads
        this.test_streamed_quads(10000, 100000, false);
    }

    private void test_streamed_tuples(int bufferSize, final int generateSize, boolean fair) throws InterruptedException,
            ExecutionException, TimeoutException {
        // Create the StreamedTuplesIterator
        final StreamedTuplesIterator stream = new StreamedTuplesIterator(bufferSize, fair);

        // Create a runnable that will generate tuples
        Runnable genQuads = new Runnable() {

            @Override
            public void run() {
                stream.start();
                // Generate tuples
                for (int i = 1; i <= generateSize; i++) {
                    Tuple<Node> t = Tuple.create(Node.createURI("http://graph"), Node.createAnon(),
                            Node.createURI("http://predicate"), NodeFactory.intToNode(i));
                    stream.tuple(t);
                }
                stream.finish();
                return;
            }
        };

        // Create a runnable that will consume tuples
        Callable<Integer> consumeQuads = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                int count = 0;
                while (stream.hasNext()) {
                    stream.next();
                    count++;
                }
                return count;
            }
        };

        // Run the threads
        Future<?> genResult = executor.submit(genQuads);
        Future<Integer> result = executor.submit(consumeQuads);
        Integer count = 0;
        try {
            count = result.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            //Check that it wasn't the producer thread erroring that caused us to time out
            genResult.get();
            //It wasn't so throw the original error
            throw e;
        }
        Assert.assertEquals(generateSize, (int) count);
    }

    /**
     * Test that blocking and waiting work nicely
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_tuples_iterator_01() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is tiny
        this.test_streamed_tuples(1, 100, true);
    }

    /**
     * Test that blocking and waiting work nicely
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_tuples_iterator_02() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is much smaller than generated tuples
        this.test_streamed_tuples(10, 1000, false);
    }

    /**
     * Test that blocking and waiting work nicely
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_tuples_iterator_03() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is smaller than generated tuples
        this.test_streamed_tuples(100, 1000, false);
    }

    /**
     * Test where blocking should rarely occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_tuples_iterator_04() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is same as generated tuples
        this.test_streamed_tuples(1000, 1000, false);
    }

    /**
     * Test where blocking should rarely occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_tuples_iterator_05() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is much larger than generated tuples
        this.test_streamed_tuples(10000, 1000, false);
    }

    /**
     * Test where blocking may occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_tuples_iterator_06() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is small relative to generated tuples
        this.test_streamed_tuples(1000, 100000, false);
    }

    /**
     * Test where blocking may occur
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void streamed_tuples_iterator_07() throws InterruptedException, ExecutionException, TimeoutException {
        // Buffer size is small relative to generated tuples
        this.test_streamed_tuples(10000, 100000, false);
    }
    
    /**
     * Test for bad buffer size
     */
    @Test(expected=IllegalArgumentException.class)
    public void streamed_instantiation_bad_01() {
        new StreamedTriplesIterator(0);
    }
    
    /**
     * Test for bad buffer size
     */
    @Test(expected=IllegalArgumentException.class)
    public void streamed_instantiation_bad_02() {
        new StreamedTriplesIterator(-1);
    }
}

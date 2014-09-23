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

import java.io.ByteArrayInputStream ;
import java.nio.charset.Charset ;
import java.util.concurrent.* ;

import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotException ;
import org.junit.AfterClass ;
import org.junit.Assert ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

/**
 * Tests for the {@link PipedRDFIterator} implementation
 * 
 */
public class TestPipedRDFIterators {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestPipedRDFIterators.class);

    private static ExecutorService executor;

    /**
     * Create our thread pool
     */
    @BeforeClass
    public static void setup() {
        // We use far more than the required 2 threads to avoid intermittent
        // deadlock issues that can otherwise occur
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

        final PipedRDFIterator<Triple> it = new PipedRDFIterator<>(bufferSize, fair);
        final PipedTriplesStream out = new PipedTriplesStream(it);

        // Create a runnable that will generate triples
        Runnable genTriples = new Runnable() {

            @Override
            public void run() {
                out.start();
                // Generate triples
                for (int i = 1; i <= generateSize; i++) {
                    Triple t = new Triple(com.hp.hpl.jena.graph.NodeFactory.createAnon(),
                            com.hp.hpl.jena.graph.NodeFactory.createURI("http://predicate"), NodeFactoryExtra.intToNode(i));
                    out.triple(t);
                }
                out.finish();
                return;
            }
        };

        // Create a runnable that will consume triples
        Callable<Integer> consumeTriples = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                int count = 0;
                while (it.hasNext()) {
                    it.next();
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
            count = result.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // Check that it wasn't the producer thread erroring that caused us
            // to time out
            genResult.get();
            // It wasn't so throw the original error
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

        final PipedRDFIterator<Quad> it = new PipedRDFIterator<>(bufferSize, fair);
        final PipedQuadsStream out = new PipedQuadsStream(it);

        // Create a runnable that will generate quads
        Runnable genQuads = new Runnable() {

            @Override
            public void run() {
                out.start();
                // Generate quads
                for (int i = 1; i <= generateSize; i++) {
                    Quad q = new Quad(com.hp.hpl.jena.graph.NodeFactory.createURI("http://graph"),
                            com.hp.hpl.jena.graph.NodeFactory.createAnon(),
                            com.hp.hpl.jena.graph.NodeFactory.createURI("http://predicate"), NodeFactoryExtra.intToNode(i));
                    out.quad(q);
                }
                out.finish();
                return;
            }
        };

        // Create a runnable that will consume quads
        Callable<Integer> consumeQuads = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                int count = 0;
                while (it.hasNext()) {
                    it.next();
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
            count = result.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // Check that it wasn't the producer thread erroring that caused us
            // to time out
            genResult.get();
            // It wasn't so throw the original error
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

    /**
     * Test for bad buffer size
     */
    @Test(expected = IllegalArgumentException.class)
    public void streamed_instantiation_bad_01() {
        new PipedRDFIterator<Triple>(0);
    }

    /**
     * Test for bad buffer size
     */
    @Test(expected = IllegalArgumentException.class)
    public void streamed_instantiation_bad_02() {
        new PipedRDFIterator<Triple>(-1);
    }

    /**
     * Tests that the iterate copes correctly in the case of hitting a parser
     * error
     * 
     * @param data
     *            Data string (Turtle format) which should be malformed
     * @param expected
     *            Number of valid triples expected to be generated before the
     *            error is hit
     * @throws TimeoutException
     * @throws InterruptedException
     */
    private void test_streamed_triples_bad(final String data, int expected) throws TimeoutException, InterruptedException {

        final PipedRDFIterator<Triple> it = new PipedRDFIterator<>();
        final PipedTriplesStream out = new PipedTriplesStream(it);

        // Create a runnable that will try to parse the bad data
        Runnable runParser = new Runnable() {

            @Override
            public void run() {
                Charset utf8 = Charset.forName("utf8");
                ByteArrayInputStream input = new ByteArrayInputStream(data.getBytes(utf8));
                try {
                    RDFDataMgr.parse(out, input, null, RDFLanguages.TURTLE, null);
                } catch (Throwable t) {
                    // Ignore the error
                }
                return;
            }
        };

        // Create a runnable that will consume triples
        Callable<Integer> consumeTriples = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                int count = 0;
                while (it.hasNext()) {
                    it.next();
                    count++;
                }
                return count;
            }
        };

        // Run the threads
        Future<?> genResult = executor.submit(runParser);
        Future<Integer> result = executor.submit(consumeTriples);
        Integer count = 0;
        try {
            count = result.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // We expect the producer thread to have errored
            try {
                genResult.get();
            } catch (ExecutionException ex) {
                // This is as expected, ignore
                LOGGER.warn("Errored as expected", ex);
            }
            // However we expect the consumer to still have been notified of
            // failure
            throw e;
        } catch (ExecutionException e) {
            // This was not expected
            Assert.fail(e.getMessage());
        }

        // Since the produce thread failed the consumer thread should give the
        // expected count
        Assert.assertEquals(expected, (int) count);
    }

    /**
     * Test failure of the iterator
     * 
     * @throws TimeoutException
     * @throws InterruptedException
     */
    @Test
    public void streamed_triples_bad_01() throws TimeoutException, InterruptedException {
        test_streamed_triples_bad("@prefix : <http://unterminated", 0);
    }

    /**
     * Test failure of the iterator
     * 
     * @throws TimeoutException
     * @throws InterruptedException
     */
    @Test
    public void streamed_triples_bad_02() throws TimeoutException, InterruptedException {
        test_streamed_triples_bad("@prefix : <http://example> . :s :p :o . :x :y", 1);
    }

    /**
     * Tests attempting to access the iterator before the stream has been
     * connected
     */
    @Test(expected = IllegalStateException.class)
    public void streamed_state_bad_01() {
        PipedRDFIterator<Triple> it = new PipedRDFIterator<>();
        it.hasNext();
    }

    /**
     * Tests attempting to access the iterator after the producer dies
     */
    @Test(expected = RiotException.class)
    public void streamed_state_bad_02() {

        final PipedRDFIterator<Triple> it = new PipedRDFIterator<>();
        final PipedTriplesStream out = new PipedTriplesStream(it);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                out.start();
                out.triple(Triple.create(com.hp.hpl.jena.graph.NodeFactory.createURI("urn:s"),
                        com.hp.hpl.jena.graph.NodeFactory.createURI("urn:p"),
                        com.hp.hpl.jena.graph.NodeFactory.createURI("urn:o")));
                throw new RuntimeException("die!");
            }
        });

        // Because this is a unit test, set an exception handler to suppress the
        // normal printing of the stacktrace to stderr
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                // Do nothing
            }
        });

        t.start();

        Assert.assertTrue(it.hasNext());
        it.next();

        // Should throw a RiotException
        it.hasNext();
    }

    /**
     * Tests a possible deadlock scenario where the producer dies and the
     * consumer is scheduled onto the same thread preventing the consumer from
     * ever noticing the dead producer
     */
    @Test
    public void streamed_state_bad_03() {

        final PipedRDFIterator<Triple> it = new PipedRDFIterator<>();
        final PipedTriplesStream out = new PipedTriplesStream(it);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Runnable producer = new Runnable() {
            @Override
            public void run() {
                out.start();
                out.triple(Triple.create(com.hp.hpl.jena.graph.NodeFactory.createURI("urn:s"),
                        com.hp.hpl.jena.graph.NodeFactory.createURI("urn:p"),
                        com.hp.hpl.jena.graph.NodeFactory.createURI("urn:o")));
                throw new RuntimeException("die!");
            }
        };
        executor.submit(producer);

        Runnable consumer = new Runnable() {
            @Override
            public void run() {
                it.hasNext();
                it.next();
                // Should fail since producer should have failed
                it.next();
            }
        };

        Future<?> f = executor.submit(consumer);
        try {
            f.get(3, TimeUnit.SECONDS);
            Assert.fail("Expected an error");
        } catch (ExecutionException e) {
            // Ignore - this is as expected
            Assert.assertTrue(e.getCause() != null);
            Assert.assertTrue(e.getCause() instanceof RiotException);
        } catch (TimeoutException e) {
            Assert.fail("Expected an error but a timeout occurred indicating the consumer deadlocked");
        } catch (InterruptedException e) {
            Assert.fail("Expected an execution error but an interrupt occurred");
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Tests a scenario where the producer never sends any data (for whatever
     * reason) but does remember to clean up after itself by calling finish()
     */
    @Test
    public void streamed_state_bad_04() {
        final PipedRDFIterator<Triple> iter = new PipedRDFIterator<>();
        final PipedTriplesStream stream = new PipedTriplesStream(iter);

        Runnable producer = new Runnable() {
            @Override
            public void run() {
                try {
                    stream.start();
                    throw new RuntimeException("die");
                } finally {
                    stream.finish();
                }
            }
        };

        Future<?> f = executor.submit(producer);

        // Verify that the producer did error as expected
        try {
            f.get(3, TimeUnit.SECONDS);
            Assert.fail("Expected an error");
        } catch (ExecutionException e) {
            // Ignore - this is as expected
            Assert.assertTrue(e.getCause() != null);
            Assert.assertTrue(e.getCause() instanceof RuntimeException);
        } catch (TimeoutException e) {
            Assert.fail("Unexpected timeout");
        } catch (InterruptedException e) {
            Assert.fail("Unexpected interrupt");
        }

        Runnable consumer = new Runnable() {
            @Override
            public void run() {
                iter.hasNext();
            }
        };

        // Consumer should finish successfully because producer will tell us it
        // finished even though it errored
        f = executor.submit(consumer);
        try {
            Object result = f.get(3, TimeUnit.SECONDS);
            Assert.assertNull(result);
        } catch (ExecutionException e) {
            Assert.fail("An error was not expected");
        } catch (TimeoutException e) {
            Assert.fail("A timeout occurred indicating the consumer deadlocked");
        } catch (InterruptedException e) {
            Assert.fail("An interrupt occurred");
        }
    }

    /**
     * Tests a scenario where the producer never ever calls start()/finish()
     */
    @Test
    public void streamed_state_bad_05() {
        final PipedRDFIterator<Triple> iter = new PipedRDFIterator<>(1, false, PipedRDFIterator.DEFAULT_POLL_TIMEOUT, 3);
        final PipedTriplesStream stream = new PipedTriplesStream(iter);

        Runnable producer = new Runnable() {
            @Override
            public void run() {
                // Simply die without ever calling start() or finish() on the
                // stream
                throw new RuntimeException("die");
            }
        };

        Future<?> f = executor.submit(producer);

        // Verify that the producer did error as expected
        try {
            f.get(3, TimeUnit.SECONDS);
            Assert.fail("Expected an error");
        } catch (ExecutionException e) {
            // Ignore - this is as expected
            Assert.assertTrue(e.getCause() != null);
            Assert.assertTrue(e.getCause() instanceof RuntimeException);
        } catch (TimeoutException e) {
            Assert.fail("Unexpected timeout");
        } catch (InterruptedException e) {
            Assert.fail("Unexpected interrupt");
        }

        Runnable consumer = new Runnable() {
            @Override
            public void run() {
                iter.hasNext();
            }
        };

        // Consumer should finish successfully because producer will tell us it
        // finished even though it errored
        f = executor.submit(consumer);
        try {
            Object result = f.get(10, TimeUnit.SECONDS);
            Assert.fail("An error was expected");
        } catch (ExecutionException e) {
            // Ignore - this is as expected and indicates we successfully
            // detected the bad state
            Assert.assertTrue(e.getCause() != null);
            Assert.assertTrue(e.getCause() instanceof RiotException);
        } catch (TimeoutException e) {
            Assert.fail("A timeout occurred indicating the consumer deadlocked");
        } catch (InterruptedException e) {
            Assert.fail("An interrupt occurred");
        }
    }

    /**
     * Check we can safely call hasNext() multiple times after the stream is
     * exhausted
     */
    @Test
    public void streamed_iterator_usage_01() {
        PipedRDFIterator<Triple> iter = new PipedRDFIterator<>();
        PipedTriplesStream stream = new PipedTriplesStream(iter);
        stream.start();
        stream.finish();
        Assert.assertFalse(iter.hasNext());
        Assert.assertFalse(iter.hasNext());
    }

    /**
     * Check that calling hasNext() after a close() is an error
     */
    @Test(expected = RiotException.class)
    public void streamed_iterator_usage_02() {
        PipedRDFIterator<Triple> iter = new PipedRDFIterator<>();
        PipedTriplesStream stream = new PipedTriplesStream(iter);
        stream.start();
        stream.finish();
        Assert.assertFalse(iter.hasNext());
        iter.close();
        // Should throw an error after the iterator is closed
        iter.hasNext();
    }
}

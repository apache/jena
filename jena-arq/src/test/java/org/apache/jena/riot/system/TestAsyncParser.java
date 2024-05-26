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

package org.apache.jena.riot.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.IsoMatcher;
import org.junit.Assert;
import org.junit.Test;

public class TestAsyncParser {

    private static String DIR = "testing/RIOT/Parser/";

    @Test public void async_parse_1() { test(DIR+"empty.ttl"); }
    @Test public void async_parse_2() { test(DIR+"data.ttl"); }

    @Test(expected = RiotException.class)
    public void async_parse_3() {
        test(DIR + "bad-data.ttl");
    }

    @Test(expected = RiotNotFoundException.class)
    public void async_parse_4() {
        test(DIR + "no-suchfile.ttl");
    }

    @Test
    public void async_iterator1() {
        Iterator<Triple> iter = AsyncParser.asyncParseTriples(DIR+"empty.ttl");
        assertFalse(iter.hasNext());
    }

    @Test
    public void async_iterator2() {
        Iterator<Triple> iter = AsyncParser.asyncParseTriples(DIR+"data.ttl");
        assertTrue(iter.hasNext());
    }

    @Test
    public void sources_1() {
        RDFParserBuilder b1 = RDFParser.fromString("_:a <p> <o>.", Lang.TTL);
        RDFParserBuilder b2 = RDFParser.fromString("_:a <p> <o>.", Lang.TTL);
        Graph graph = GraphFactory.createDefaultGraph();
        AsyncParser.asyncParseSources(List.of(b1,b2), StreamRDFLib.graph(graph));
        assertEquals(2, graph.size());
    }

    @Test
    public void sources_2() {
        Graph graph = GraphFactory.createDefaultGraph();
        AsyncParser.asyncParseSources(List.of(), StreamRDFLib.graph(graph));
        assertEquals(0, graph.size());
    }

    private static void test(String filename) {
        Graph graph1 = GraphFactory.createDefaultGraph();
        Graph graph2 = GraphFactory.createDefaultGraph();

        AsyncParser.asyncParse(filename, StreamRDFLib.graph(graph2));

        // Parsed, so check output.
        RDFParser.source(filename).parse(graph1);
        assertEquals(graph1.size(), graph2.size());
        assertTrue( IsoMatcher.isomorphic(graph1, graph2));
    }

    /** Repeatedly tests the parser on an infinite amount of data with a failing sink.
     * This should terminate the parser thread. */
    @Test
    public void failingSink() {
        int numThreadsToCreate = 20;
        int beforeThreadCount = ManagementFactory.getThreadMXBean().getThreadCount();
        for (int i = 0; i < numThreadsToCreate; ++i) {
            try (InputStream in = openInfiniteNtStream();
                    OutputStream out = new FailingOutputStream()) {
                StreamRDF sink = StreamRDFWriter.getWriterStream(out, RDFFormat.NT);
                sink.start();
                // Use small read ahead amount due to the infinite input and always failing sink
                AsyncParser.of(in, Lang.NT, null).setChunkSize(10).setQueueSize(3).asyncParseSources(sink);
                sink.finish();
            } catch (Exception e) {
                // Expected to fail with FailingOutputStream's error message
                Assert.assertNotNull("Unexpected exception: " + e, e.getCause());
                Assert.assertEquals(FailingOutputStream.ERROR_MSG, e.getCause().getMessage());
                continue;
            }
            throw new RuntimeException("Parsing unexpectedly succeeded");
        }
        int afterThreadCount = ManagementFactory.getThreadMXBean().getThreadCount();
        int threadCountDifference = Math.abs(afterThreadCount - beforeThreadCount);
        int maxAllowedThreadCountDifference = 5;

        Assert.assertTrue("Cancelling RDF parsing resulted in too many dangling threads ("
                + threadCountDifference + ")",
                threadCountDifference <= maxAllowedThreadCountDifference);
    }

    /** Tests to ensure threads are not piling up when repeatedly canceling parsers */
    @Test
    public void repeatedParsingCancellation_1() throws Exception {
        int numThreadsToCreate = 20;
        int beforeThreadCount = ManagementFactory.getThreadMXBean().getThreadCount();
        for (int i = 0; i < numThreadsToCreate; ++i) {
            IteratorCloseable<Triple> iter = null;
            try {
                iter = AsyncParser.of(DIR+"data.ttl").setDaemonMode(false).asyncParseTriples();

                // Ensure parsing is triggered
                iter.hasNext();
            } finally {
                if (iter != null) {
                    iter.close();
                }
            }
        }
        int afterThreadCount = ManagementFactory.getThreadMXBean().getThreadCount();
        int threadCountDifference = Math.abs(afterThreadCount - beforeThreadCount);

        // Give some room in how much the number of threads may differ
        // The main point is that the acceptable margin is significantly lower than
        // the number of created parser threads
        int maxAllowedThreadCountDifference = 5;

        Assert.assertTrue("Cancelling RDF parsing resulted in too many dangling threads ("
                + threadCountDifference + ")",
                threadCountDifference <= maxAllowedThreadCountDifference);
    }

    /** A test that checks that only a limited number of bytes is read when using a small chunk size*/
    @Test
    public void lowLatencyParse() {
        long expectedLimit = 10;
        RepeatingReadableByteChannel channel = openInfiniteNtChannel();
        try (Stream<Triple> s = AsyncParser.of(Channels.newInputStream(channel), Lang.TURTLE, null)
                .setChunkSize(100).streamTriples().limit(expectedLimit)) {
            // s.forEach(t -> System.out.println("Triple: " + t));
            long actualLimit = s.count();
            Assert.assertEquals(expectedLimit, actualLimit);
        }

        long pos = channel.position();
        // This test should consume up to 60.000 bytes:
        // - 50 bytes/triple and 100 triples/chunk results in 5K bytes per chunk.
        // - A queue size of 10 means that a full queue amounts to 50K bytes.
        // - One chunk is removed from the queue when reading which gets filled up -> 55K.
        // - The close action clears the queue and aborts on the next chunk -> 60K.
        // In order to give room for implementation changes and to account for possible buffering in the parser
        // the value tested against here is about twice as large
        Assert.assertTrue("Too many bytes consumed from input stream (" + pos + ")", pos < 120_000);
    }

    /** This test first creates some 'good' data for reference and then appends some 'bad' data in order
     * to provoke a parse error.
     * Parsing the bad data should then yield all the good data followed by an error event.
     * Conversely, errors must not cause loss of any events. */
    @Test
    public void testNoEventsAreLost() {
        // Generate some "good" sample data - good means: no parse errors
        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
        for (int i = 0; i < 4; ++i) {
            sb.append("<urn:example:s> rdfs:label \"item # " + i + "\" .\n");
        }
        String goodInputStr = sb.toString();

        List<EltStreamRDF> expected;
        try (Stream<EltStreamRDF> s = AsyncParser.of(new ByteArrayInputStream(goodInputStr.getBytes()), Lang.TURTLE, null).streamElements()) {
            expected = s.collect(Collectors.toList());
        }

        // Validate parser on the good data
        int expectedGoodEventCount = 5;
        Assert.assertEquals(expected.size(), expectedGoodEventCount);

        // Append some bad data
        String badInputStr = sb.append("parse error").toString();

        // Now test that we get back all the good data plus the error event
        for (int chunkSize = 1; chunkSize < 10; ++chunkSize) {
            try (Stream<EltStreamRDF> s = AsyncParser.of(new ByteArrayInputStream(badInputStr.getBytes()), Lang.TURTLE, null)
                    .setChunkSize(chunkSize)
                    .streamElements()) {
                List<EltStreamRDF> actual = s.collect(Collectors.toList());
                Assert.assertEquals(expectedGoodEventCount + 1, actual.size());
                Assert.assertEquals(expected, actual.subList(0, expectedGoodEventCount));
                Assert.assertTrue(actual.get(actual.size() - 1).isException());
            }
        }
    }

    /** Test an AsyncParser setup with a custom dispatch (flush) policy */
    @Test
    public void testPrematureDispatch() {
        final int expectedSize = 10;

        AtomicInteger counter = new AtomicInteger(1);
        String expectedErrorMsg = "Forced abort";
        List<EltStreamRDF> actual;
        try (Stream<EltStreamRDF> stream = AsyncParser.of(openInfiniteNtStream(), Lang.TURTLE, null)
            .setPrematureDispatch(elt -> {
                if (counter.getAndIncrement() < expectedSize) {
                    return true;
                }
                throw new RuntimeException(expectedErrorMsg);
            })
            .streamElements()
            .limit(1000)) { // The limit is just a safety net to prevent infinite parsing in case of malfunction
            actual = stream.collect(Collectors.toList());
        }

        // Check that only the expected number of elements were dispatched
        Assert.assertEquals(expectedSize, actual.size());

        // The payload of the last element must be an exception, all others must be triples
        for (int i = 0; i < expectedSize; ++i) {
            boolean isLastItem = i + 1 == expectedSize;
            EltStreamRDF elt = actual.get(i);
            if (isLastItem) {
                Assert.assertTrue("Last element expected to be an exception", elt.isException());
                Assert.assertEquals(expectedErrorMsg, elt.exception().getMessage());
            } else {
                Assert.assertTrue("Non-last element expected to be a triple", elt.isTriple());
            }
        }

    }

    /** An infinite stream of ntriple data */
    private static InputStream openInfiniteNtStream() {
        return Channels.newInputStream(openInfiniteNtChannel());
    }

    private static RepeatingReadableByteChannel openInfiniteNtChannel() {
        return new RepeatingReadableByteChannel(
                "<urn:example:s> <urn:example:p> <urn:example:o> .\n".getBytes(StandardCharsets.UTF_8));
    }

    /** An always failing output stream */
    private static class FailingOutputStream
        extends OutputStream
    {
        public static final String ERROR_MSG = "Mocked IO Error";
        @Override
        public void write(int b) throws IOException {
            throw new IOException(ERROR_MSG);
        }
    }

    /** A byte channel that infinitely repeats a given chunk of data */
    private static class RepeatingReadableByteChannel
        implements ReadableByteChannel
    {
        protected byte[] data;
        protected boolean isOpen;
        protected long pos;

        public RepeatingReadableByteChannel(byte[] data) {
            this(data, 0l);
        }

        public RepeatingReadableByteChannel(byte[] data, long pos) {
            super();
            Objects.requireNonNull(data);
            if ( data.length == 0 )
                throw new RuntimeException("Provided data array must have at least 1 item");
            this.data = data;
            this.pos = pos;
            this.isOpen = true;
        }
        @Override public boolean isOpen() { return isOpen; }
        @Override public void close() throws IOException { isOpen = false; }
        @Override
        public int read(ByteBuffer dst) throws IOException {
            int l = data.length;
            int arrOffset = (int)(pos % l);
            int n = Math.min(dst.remaining(), l - arrOffset);
            dst.put(data, arrOffset, n);
            pos += n;
            return n;
        }

        public long position() {
            return pos;
        }

        public void position(long pos) {
            this.pos = pos;
        }
    }
}

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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;

public class AsyncParserBuilder {
    /* These following two attributes only exist for javadoc references */
    private static final int dftChunkSize = AsyncParser.dftChunkSize;
    private static final int dftQueueSize = AsyncParser.dftQueueSize;

    private int chunkSize;
    private int queueSize;
    private boolean daemonMode;
    private Predicate<EltStreamRDF> prematureDispatch;
    private List<RDFParserBuilder> sources;

    public AsyncParserBuilder() {
        this.chunkSize = dftChunkSize;
        this.queueSize = dftQueueSize;
        this.daemonMode = true;
        this.prematureDispatch = null;
        this.sources = Collections.emptyList();
    }

    public AsyncParserBuilder(List<RDFParserBuilder> sources) {
        this();
        this.sources = List.copyOf(sources);
    }

    public int getChunkSize() {
        return chunkSize;
    }

    /** The chunk size controls the maxmimum number of elements the parser thread transfers at once
     * to the client iterator's queue. The default value ({@value #dftChunkSize})
     * is optimized for throughput however it introduces a delay until the first item arrives.
     * Data can be dispatched prematurely by setting {@link #setPrematureDispatch(Predicate)}.
     */
    public AsyncParserBuilder setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    public int getQueueSize() {
        return queueSize;
    }

    /**
     * The queue size controls the number of chunks the parser thread can read ahead.
     * A full queue blocks that thread.
     * The default value is {@value #dftQueueSize}.
     */
    public AsyncParserBuilder setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    public boolean isDaemonMode() {
        return daemonMode;
    }

    /** Whether the parser thread should run as a daemon thread.
     * A JVM will terminate as soon as there are no more non-daemon threads. */
    public AsyncParserBuilder setDaemonMode(boolean daemonMode) {
        this.daemonMode = daemonMode;
        return this;
    }

    /** Set a custom dispatch (flush) policy: When the predicate returns true for an element then that element and
     * any gathered data is dispatched immediately. Before dispatch a check is made whether parsing has been
     * (concurrently) aborted. */
    public AsyncParserBuilder setPrematureDispatch(Predicate<EltStreamRDF> prematureDispatch) {
        this.prematureDispatch = prematureDispatch;
        return this;
    }

    public Predicate<EltStreamRDF> getPrematureDispatch() {
        return prematureDispatch;
    }

    public List<RDFParserBuilder> getSources() {
        return sources;
    }

    public AsyncParserBuilder setSources(List<RDFParserBuilder> sources) {
        this.sources = List.copyOf(sources);
        return this;
    }

    /** Run an operation on all RDFParserBuilder sources in this builder.
     * Allows for e.g. configuring prefixes, the error handler or the label-to-node strategy */
    public AsyncParserBuilder mutateSources(Consumer<RDFParserBuilder> mutator) {
        for (RDFParserBuilder source : sources) {
            mutator.accept(source);
        }
        return this;
    }

    private IteratorCloseable<EltStreamRDF> asyncParseElements() {
        Objects.requireNonNull(sources);

        BlockingQueue<List<EltStreamRDF>> queue = new ArrayBlockingQueue<>(queueSize);
        Runnable closeAction = AsyncParser.startParserThread(AsyncParser.LOG, sources, queue, chunkSize, prematureDispatch, daemonMode);
        IteratorCloseable<List<EltStreamRDF>> blocks = AsyncParser.blockingIterator(closeAction, queue, x -> x == AsyncParser.END);

        IteratorCloseable<EltStreamRDF> elements = (IteratorCloseable<EltStreamRDF>)Iter.flatMap(blocks, x->x.iterator());
        return elements;
    }

    public IteratorCloseable<Triple> asyncParseTriples() {
        return Iter.iter(asyncParseElements()).map(AsyncParser.elt2Triple).removeNulls();
    }

    public IteratorCloseable<Quad> asyncParseQuads() {
        return Iter.iter(asyncParseElements()).map(AsyncParser.elt2Quad).removeNulls();
    }

    public Stream<EltStreamRDF> streamElements() {
        return Iter.asStream(asyncParseElements());
    }

    public Stream<Triple> streamTriples() {
        return Iter.asStream(asyncParseTriples());
    }

    public Stream<Quad> streamQuads() {
        return Iter.asStream(asyncParseQuads());
    }

    /** Calling the returned runnable stops parsing.
     * The sink will see no further data. */
    public Runnable asyncParseSources(StreamRDF output) {
        // Async thread
        Logger LOG1 = AsyncParser.LOG;
        // Receiver
        Logger LOG2 = AsyncParser.LOG;

        BlockingQueue<List<EltStreamRDF>> queue = new ArrayBlockingQueue<>(queueSize);
        Runnable closeAction = AsyncParser.startParserThread(LOG1, sources, queue, chunkSize, prematureDispatch, daemonMode);
        AsyncParser.receiver(closeAction, LOG2, queue, output);

        return closeAction;
    }
}

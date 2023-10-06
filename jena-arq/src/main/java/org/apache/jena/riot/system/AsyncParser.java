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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.atlas.iterator.IteratorSlotted;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.IteratorParsers;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ClosableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Async parsing - parses a list of files or URLs on a separate thread.
 * <p>
 * The parser runs on a separate thread. It's output is sent to a StreamRDF on the
 * callers thread. The parser output is batched into blocks and placed on a
 * finite-sized concurrent {@link BlockingQueue}.
 * <p>
 * There are overheads, so this is only beneficial in some situations. Delivery to
 * the StreamRDF has an initial latency while the first batch of work is accumulated.
 * Using the {@link AsyncParserBuilder} gives control over the chunk size such that initial latency
 * can be reduced at the cost of possibly decreasing the overall throughput.
 * <p>
 * Closing the returned {@link ClosableIterator}s terminates the parsing
 * process and closes the involved resources.
 */
public class AsyncParser {

    static final Logger LOG = LoggerFactory.getLogger(AsyncParser.class);
    static final ErrorHandler dftErrorHandler = createDefaultErrorhandler(LOG);

    static final int dftChunkSize = 100_000;
    // There is no point letting the parser get a long way ahead.
    static final int dftQueueSize = 10;
    static final List<EltStreamRDF> END = List.of();

    private static final StreamRDF alwaysFailingStreamRdf = new StreamToElements(elt -> { throw new RuntimeException(new InterruptedException()); });

    /* Destination states for handling concurrent abort */
    /** Normal operation: Accept incoming data */
    private static final int RUNNING  = 0;
    /** Abort requested: The next chunk will trigger placing the poison on the queue and transition to STOPPED state */
    private static final int ABORTING = 1;
    /** Stopped (regardless whether due to normal or exceptional cause): Any further incoming data raises an exception */
    private static final int STOPPED  = 2;

    private AsyncParser() {}

    /**
     * Function that reads a file or GETs a URL, parses the content on a separate
     * thread and sends the output to a StreamRDF on the callers thread.
     */
    public static void asyncParse(String fileOrURL, StreamRDF output) {
        AsyncParser.of(fileOrURL).asyncParseSources(output);
    }

    /**
     * Function that parses content from a list of files or URLs on a separate thread
     * and sends the output to a StreamRDF on the callers thread.
     */
    public static void asyncParse(List<String> filesOrURLs, StreamRDF output) {
        AsyncParser.ofLocations(filesOrURLs).asyncParseSources(output);
    }

    /**
     * Function to that parses an {@link InputStream} on a separate thread
     * and sends the output to a StreamRDF on the callers thread.
     */
    public static void asyncParse(InputStream input, Lang lang, String baseURI, StreamRDF output) {
        AsyncParser.of(input, lang, baseURI).asyncParseSources(output);
    }

    /**
     * Parse a number of sources ({@link RDFParserBuilder RDFParserBuilders}) on a separate thread and
     * send the output to a StreamRDF on the callers thread.
     */
    public static void asyncParseSources(List<RDFParserBuilder> sources, StreamRDF output) {
        AsyncParser.ofSources(sources).asyncParseSources(output);
    }

    /** Pull parser - triples */
    public static IteratorCloseable<Triple> asyncParseTriples(String fileOrURL) {
        return AsyncParser.of(fileOrURL).asyncParseTriples();
    }

    /** Pull parser - triples */
    public static IteratorCloseable<Triple> asyncParseTriples(List<String> filesOrURLs) {
        return AsyncParser.ofLocations(filesOrURLs).asyncParseTriples();
    }

    /**
     * Pull parser - triples.
     * <p>
     * See also {@link IteratorParsers#createIteratorNTriples}.
     */
    public static IteratorCloseable<Triple> asyncParseTriples(InputStream input, Lang lang, String baseURI) {
        return AsyncParser.of(input, lang, baseURI).asyncParseTriples();
    }

    /** Pull parser - quads */
    public static IteratorCloseable<Quad> asyncParseQuads(String fileOrURL) {
        return AsyncParser.of(fileOrURL).asyncParseQuads();
    }

    /** Pull parser - quads */
    public static IteratorCloseable<Quad> asyncParseQuads(List<String> filesOrURLs) {
        return AsyncParser.ofLocations(filesOrURLs).asyncParseQuads();
    }

    /**
     * Pull parser - quads.
     * <p>
     * See also {@link IteratorParsers#createIteratorNQuads}.
     */
    public static IteratorCloseable<Quad> asyncParseQuads(InputStream input, Lang lang, String baseURI) {
        return AsyncParser.of(input, lang, baseURI).asyncParseQuads();
    }

    /** Create an {@link AsyncParserBuilder} from a file or URL. */
    public static AsyncParserBuilder of(String fileOrURL) {
        Objects.requireNonNull(fileOrURL);
        return ofLocations(List.of(fileOrURL));
    }

    /** Create an {@link AsyncParserBuilder} from a set of files and/or URLs. */
    public static AsyncParserBuilder ofLocations(List<String> filesOrURLs) {
        Objects.requireNonNull(filesOrURLs);
        LOG.debug("Parse: "+filesOrURLs);
        return new AsyncParserBuilder(urlsToSource(filesOrURLs));
    }

    /** Create an {@link AsyncParserBuilder} from an input stream. */
    public static AsyncParserBuilder of(InputStream input, Lang lang, String baseURI) {
        Objects.requireNonNull(input);
        Objects.requireNonNull(lang);
        return ofSources(inputStreamToSource(input, lang, baseURI));
    }

    /** Create an {@link AsyncParserBuilder} from an {@link RDFParserBuilder}. */
    public static AsyncParserBuilder of(RDFParserBuilder source) {
        Objects.requireNonNull(source);
        return ofSources(Arrays.asList(source));
    }

    /** Create an {@link AsyncParserBuilder} from a set of {@link RDFParserBuilder} instances. */
    public static AsyncParserBuilder ofSources(List<RDFParserBuilder> sources) {
        Objects.requireNonNull(sources);
        return new AsyncParserBuilder(sources);
    }

    /*
     * Internals
     */

    private static List<RDFParserBuilder> urlsToSource(List<String> filesOrURLs) {
        return filesOrURLs.stream().map(
                    uriOrFile -> RDFParser.source(uriOrFile).errorHandler(dftErrorHandler))
            .collect(Collectors.toList());
    }

    /** Create a source object from the given arguments that is suitable for use with
     * {@link #asyncParseIterator(List)} which return*/
    private static List<RDFParserBuilder> inputStreamToSource(InputStream input, Lang lang, String baseURI) {
        return List.of(RDFParser.source(input).lang(lang).base(baseURI).errorHandler(dftErrorHandler));
    }

    /**
     * This method raises a caller-thread-side exception for a throwable returned by the parser thread.
     * If that throwable is a RuntimeException then the caller side's stacktrace is added
     * as a suppressed exception in order to retain the original exception type.
     * Any other type of throwable raises a generic RuntimeException with it as the cause.
     */
    private static void raiseException(Throwable throwable) {
        if (throwable instanceof RuntimeException e) {
            e.addSuppressed(new RuntimeException("Encountered error element from parse thread"));
            throw e;
        }

        throw new RuntimeException("Encountered error element from parse thread", throwable);
    }

    static Function<EltStreamRDF, Triple> elt2Triple = x-> {
        if ( x.isException() ) {
            raiseException(x.exception());
        }
        if ( x.isQuad() ) {
            Quad quad = x.quad();
            Node g = quad.getGraph();
            if ( g == Quad.tripleInQuad || Quad.isDefaultGraph(g) )
                return quad.asTriple();
            return null;
        }
        return x.triple();
    };

    static Function<EltStreamRDF, Quad> elt2Quad = x-> {
        if ( x.isException() ) {
            raiseException(x.exception());
        }
        if ( x.isTriple() )
            return Quad.create(Quad.defaultGraphIRI, x.triple());
        return x.quad();
    };

    // Iterator that stops when an end marker is seen.
    static <X> IteratorCloseable<X> blockingIterator(Runnable closeAction, BlockingQueue<X> queue, Predicate<X> endTest) {
        return new IteratorSlotted<>() {
            boolean ended = false;

            @Override
            protected X moveToNext() {
                try {
                    X x = null;
                    if (!ended) {
                        x = queue.take();
                    }
                    if ( endTest.test(x) ) {
                        ended = true;
                        return null;
                    }
                    return x;
                } catch (InterruptedException e) {
                    ended = true;
                    return null;
                }
            }

            @Override
            protected boolean hasMore() {
                return !ended;
            }

            @Override
            protected void closeIterator() {
                closeAction.run();
            }
        };
    }

    static ErrorHandler createDefaultErrorhandler(Logger LOG1) {
        return new ErrorHandler() {
            @Override
            public void warning(String message, long line, long col)
            { LOG1.warn(SysRIOT.fmtMessage(message, line, col)); }

            @Override
            public void error(String message, long line, long col) {
                throw new RiotException(SysRIOT.fmtMessage(message, line, col)) ;
            }

            @Override
            public void fatal(String message, long line, long col) {
                throw new RiotException(SysRIOT.fmtMessage(message, line, col)) ;
            }
        };
    }

    /** This class's purpose is to run a set of RDF parsers and place their generated elements on a queue.
     * Thereby errors and abort requests are handled. */
    private static class Task implements Runnable {
        private Logger logger;
        private List<RDFParserBuilder> sources;
        private BlockingQueue<List<EltStreamRDF>> queue;
        private int chunkSize;
        private Predicate<EltStreamRDF> prematureDispatch;

        // Destination resources are initialized upon calling run();
        private EltStreamBatcher<EltStreamRDF> batcher;
        private StreamRDF generatorStreamRdf;
        private AtomicInteger destinationState = new AtomicInteger(RUNNING);
        private boolean errorEncountered = false;

        public Task(List<RDFParserBuilder> sources, BlockingQueue<List<EltStreamRDF>> queue, int chunkSize, Predicate<EltStreamRDF> prematureDispatch, Logger logger) {
            this.sources = sources;
            this.queue = queue;
            this.chunkSize = chunkSize;
            this.prematureDispatch = prematureDispatch;
            this.logger = logger;
        }

        @Override
        public void run() {
            initDestination();
            try {
                start();
                int n = sources.size();
                for (int i = 0; i < n; ++i) {
                    RDFParserBuilder parser = sources.get(i);
                    parse(parser);
                }
            } finally {
                finish();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Finish parsing");
            }
        }

        /** Sets the destination into ABORTING state (if it was running).
         * This triggers transition into ABORT state on the next chunk. */
        public void abort() {
            destinationState.compareAndSet(RUNNING, ABORTING);
        }

        private void initDestination() {
            // -- Parser thread setup
            Consumer<List<EltStreamRDF>> destination = batch -> {
                if (destinationState.get() == RUNNING) {
                    try {
                        queue.put(batch);
                    } catch (InterruptedException ex) {
                        // After interrupt we may be in ABORTING state - therefore
                        // check whether to transition into ABORT state
                        handleAbortingState();
                        // FmtLog.error(LOG, ex, "Error: %s", ex.getMessage());
                        throw new RuntimeException(ex);
                    }
                } else {
                    handleAbortingState();
                    throw new RuntimeException(new InterruptedException());
                }
            };

            this.batcher = new EltStreamBatcher<>(destination, END, chunkSize);
            Consumer<EltStreamRDF> eltSink = batcher;

            // Take care of a custom dispatch policy
            if (prematureDispatch != null) {
                eltSink = elt -> {
                    boolean dispatchImmediately = prematureDispatch.test(elt);
                    batcher.accept(elt);
                    if (dispatchImmediately) {
                        batcher.flush();
                    }
                };
            }

            this.generatorStreamRdf = new StreamToElements(eltSink);
        }

        private void start() {
            batcher.startBatching();
            if (logger.isDebugEnabled()) {
                logger.debug("Start parsing");
            }
        }

        private void parse(RDFParserBuilder parser) {
            try {
                // If an error occured then all parser are invoked anyway because any
                // resources they own need yet to be closed.
                // At this point, however, the parser's sink will always fail and
                // any further errors will be suppressed.
                StreamRDF sink = errorEncountered
                        ? alwaysFailingStreamRdf
                        : generatorStreamRdf;
                parser.parse(sink);
            } catch (RuntimeException ex) {
                Throwable cause = ex.getCause();
                if (errorEncountered) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Suppressed exception", ex);
                    }
                } else {
                    if (cause instanceof InterruptedException) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Parsing was interrupted");
                        }
                    } else {
                        // Parse error.
                        EltStreamRDF elt = EltStreamRDF.exception(ex);
                        batcher.accept(elt);
                    }
                    errorEncountered = true;
                }
            } catch (Throwable throwable) {
                if (errorEncountered) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Suppressed exception", throwable);
                    }
                } else {
                    // Very bad! Wrapping with runtime exception to track this location
                    EltStreamRDF elt = EltStreamRDF.exception(new RuntimeException(throwable));
                    batcher.accept(elt);
                    errorEncountered = true;
                }
            }
        }

        private void finish() {
            try {
                // If we are already in ABORTED state then finishBatching will do nothing but raise
                // an exception
                // If we are in ABORTING state we still need to dispatch another batch in order to transition into ABORT state
                if (destinationState.get() != STOPPED) {
                    batcher.finishBatching();
                }
            } catch(Throwable ex) {
                if (errorEncountered) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Suppressed exception", ex);
                    }
                } else {
                    Throwable cause = ex.getCause();
                    if (cause instanceof InterruptedException) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Parsing was interrupted");
                        }
                    } else {
                        // Rethrow if something else went wrong
                        throw new RuntimeException(ex);
                    }
                }
            } finally {
                // We are done; dispatching any further chunks is now an error and raises an exception
                destinationState.set(STOPPED);
            }
        }

        /** Check */
        private void handleAbortingState() {
            if (destinationState.compareAndSet(ABORTING, STOPPED)) {
                // If we are transitioning into ABORT state then force putting
                // the poison on the queue - regardless of further interruptions!
                while (true) {
                    try {
                        queue.clear();
                        queue.put(END);
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }
        }
    }

    /** Returns a runnable for stopping the parse process */
    static Runnable startParserThread(
            Logger logger, List<RDFParserBuilder> sources,
            BlockingQueue<List<EltStreamRDF>> queue,
            int chunkSize,
            Predicate<EltStreamRDF> prematureDispatch,
            boolean daemonMode) {

        Task task = new Task(sources, queue, chunkSize, prematureDispatch, logger);
        // Ensures runnable has started.
        // ThreadLib.async(task) does not ensure runnable has started.
        Thread parserThread = new Thread(task, "AsyncParser");
        parserThread.setDaemon(daemonMode);
        parserThread.start();
        return () -> {
            task.abort();

            // Interrupt the parsing thread
            // Note that InputStreams and ByteChannels may close themselves
            // when interrupted while reading
            parserThread.interrupt();

            try {
                // Wait for the thread to terminate so that all is clean when we return from close()
                // Note: Some AsyncParser unit tests assert that the number of threads
                //  before and after parsing match up (within tolerance) - so joining is essential.
                parserThread.join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static void dispatch(EltStreamRDF elt, StreamRDF stream) {
        switch (elt.getType()) {
        case TRIPLE:    stream.triple(elt.triple()); break;
        case QUAD:      stream.quad(elt.quad()); break;
        case PREFIX:    stream.prefix(elt.prefix(), elt.iri()); break;
        case BASE:      stream.base(elt.iri()); break;
        case EXCEPTION: raiseException(elt.exception()); break;
        default: throw new InternalErrorException("Bad EltStreamRDF");
        }
    }

    /** Receiver. Take chunks off the queue and send to the output StreamRDF. */
    static void receiver(Runnable closeAction, Logger LOG2, BlockingQueue<List<EltStreamRDF>> queue, StreamRDF output) {
        // -- Receiver thread
        int count = 0;
        // Receive.
        try {
            for(;;) {
                List<EltStreamRDF> batch = queue.take();
                if ( batch == END ) {
                    FmtLog.debug(LOG2, "Receive: END (%,d)", count);
                    break;
                }
                count += batch.size();
                if ( LOG.isDebugEnabled() )
                    FmtLog.debug(LOG2, "Receive: Batch : %,d (%,d)", batch.size(), count);
                dispatch(batch, output);
            }
        } catch (InterruptedException e) {
            FmtLog.error(LOG2, e, "Interrupted");
        } finally {
            // The close action not only stops the parser but also waits
            // for its thread to finish
            closeAction.run();
        }
    }

    private static void dispatch(List<EltStreamRDF> batch, StreamRDF stream) {
        for ( EltStreamRDF elt : batch )
            dispatch(elt, stream);
    }

    /** Convert a Stream into EltStreamRDF */
    private static class StreamToElements implements StreamRDF {

        private final Consumer<EltStreamRDF> destination;

        public StreamToElements(Consumer<EltStreamRDF> destination) {
            this.destination = destination;
        }

        @Override
        public void start() { /* nothing to do */ }

        @Override
        public void finish() { /* nothing to do */ }

        @Override
        public void triple(Triple triple) {
            EltStreamRDF elt = EltStreamRDF.triple(triple);
            deliver(elt);
        }

        @Override
        public void quad(Quad quad) {
            EltStreamRDF elt = EltStreamRDF.quad(quad);
            deliver(elt);
        }

        @Override
        public void base(String base) {
            EltStreamRDF elt = EltStreamRDF.base(base);
            deliver(elt);
        }

        @Override
        public void prefix(String prefix, String iri) {
            EltStreamRDF elt = EltStreamRDF.prefix(prefix, iri);
            deliver(elt);
        }

        private void deliver(EltStreamRDF elt) {
            destination.accept(elt);
        }
    }

    /** Batch items from a StreamRDF */
    private static class EltStreamBatcher<T> implements Consumer<T> {

        private final int batchSize;
        private List<T> elements = null;
        private final Consumer<List<T>> batchDestination;
        private final List<T> endMarker;

        public EltStreamBatcher(Consumer<List<T>> batchDestination, List<T> endMarker, int batchSize) {
            this.batchDestination = batchDestination;
            this.batchSize = batchSize;
            this.endMarker = endMarker;
        }

        public void startBatching() { /* nothing to do */ }

        /** Immediately dispatch any available data. */
        public void flush() {
            if (elements != null) {
                dispatch(elements);
                elements = null;
            }
        }

        public void finishBatching() {
            try {
                flush();
            } finally {
                dispatch(endMarker);
            }
        }

        private <X> boolean isEmpty(List<X> list) {
            return list == null || list.isEmpty();
        }

        /** The added element is included in the dispatched batch when triggered */
        @Override
        public void accept(T elt) {
            if ( elements == null )
                elements = allocChunk();
            elements.add(elt);
            maybeDispatch();
        }

        private void maybeDispatch() {
            long x = elements.size();
            if ( x < batchSize )
                return;
            dispatch(elements);
            elements = null;
        }

        private int count = 0 ;
        private void dispatch(List<T> batch) {
            count += batch.size();
            batchDestination.accept(batch);
        }

        private List<T> allocChunk() {
            return new ArrayList<>(batchSize);
        }
    }
}

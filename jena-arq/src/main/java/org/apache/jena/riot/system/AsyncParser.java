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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorSlotted;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.*;
import org.apache.jena.sparql.core.Quad;
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
 */
public class AsyncParser {

    private static Logger LOG = LoggerFactory.getLogger(AsyncParser.class);
    private static int chunkSize = 100_000;
    // There is no point letting the parser get a long way ahead.
    private static int queueSize = 10;
    private static List<EltStreamRDF> END = List.of();

    private AsyncParser() {}

    /**
     * Function that reads a file or GETs a URL, parses the content on a separate
     * thread and sends the output to a StreamRDF on the callers thread.
     */
    public static void asyncParse(String filesOrURL, StreamRDF output) {
        asyncParse(List.of(filesOrURL), output);
    }

    /**
     * Function that parses content from a list of files or URLs on a separate thread
     * and sends the output to a StreamRDF on the callers thread.
     */
    public static void asyncParse(List<String> filesOrURLs, StreamRDF output) {
        Objects.requireNonNull(filesOrURLs);
        Objects.requireNonNull(output);
        LOG.debug("Parse: "+filesOrURLs);
        asyncParseSources(urlsToSource(filesOrURLs), output);
    }

    /**
     * Function to that parses an {@link InputStream} on a separate thread
     * and sends the output to a StreamRDF on the callers thread.
     */
    public static void asyncParse(InputStream input, Lang lang, String baseURI, StreamRDF output) {
        Objects.requireNonNull(input);
        Objects.requireNonNull(lang);
        Objects.requireNonNull(output);
        asyncParseSources(inputStreamToSource(input, lang, baseURI), output);
    }

    /**
     * Parse a number of sources ({@link RDFParserBuilder RDFParserBuilders}) on a separate thread and
     * send the output to a StreamRDF on the callers thread.
     */
    public static void asyncParseSources(List<RDFParserBuilder> sources, StreamRDF output) {
        // Async thread
        Logger LOG1 = LOG;
        // Receiver
        Logger LOG2 = LOG;
        BlockingQueue<List<EltStreamRDF>> queue = new ArrayBlockingQueue<>(queueSize);
        startParserThread(LOG1, sources, queue);
        receiver(LOG2, queue, output);
    }

    private static List<RDFParserBuilder> urlsToSource(List<String> filesOrURLs) {
        return filesOrURLs.stream().map(uriOrFile-> RDFParser.source(uriOrFile)).collect(Collectors.toList());
    }

    private static List<RDFParserBuilder> inputStreamToSource(InputStream input, Lang lang, String baseURI) {
        return List.of(RDFParser.source(input).lang(lang));
    }

    private static Function<EltStreamRDF, Triple> elt2Triple = x-> {
        if ( x.exception != null )
            throw x.exception;
        if ( x.quad != null ) {
            Node g = x.quad.getGraph();
            if ( g == Quad.tripleInQuad || Quad.isDefaultGraph(g) )
                return x.quad.asTriple();
            return null;
        }
        return x.triple;
    };

    private static Function<EltStreamRDF, Quad> elt2Quad = x-> {
        if ( x.exception != null )
            throw x.exception;
        if ( x.triple != null )
            return Quad.create(Quad.defaultGraphIRI, x.triple);
        return x.quad;
    };

    /** Pull parser - triples */
    public static Iterator<Triple> asyncParseTriples(String fileOrURL) {
        return asyncParseTriples(List.of(fileOrURL));
    }

    /** Pull parser - triples */
    public static Iterator<Triple> asyncParseTriples(List<String> filesOrURLs) {
        Iterator<EltStreamRDF> source = asyncParseIterator(urlsToSource(filesOrURLs));
        return Iter.iter(source).map(elt2Triple).removeNulls();
    }

    /** Pull parser - triples */
    public static Iterator<Triple> asyncParseTriples(InputStream input, Lang lang, String baseURI) {
        Iterator<EltStreamRDF> source = asyncParseIterator(inputStreamToSource(input, lang, baseURI));
        return Iter.iter(source).map(elt2Triple).removeNulls();
    }

    /** Pull parser - quads */
    public static Iterator<Quad> asyncParseQuads(String fileOrURL) {
        return asyncParseQuads(List.of(fileOrURL));
    }

    /** Pull parser - quads */
    public static Iterator<Quad> asyncParseQuads(List<String> filesOrURLs) {
        Iterator<EltStreamRDF> source = asyncParseIterator(urlsToSource(filesOrURLs));
        return Iter.iter(source).map(elt2Quad).removeNulls();
    }

    /** Pull parser - quads */
    public static Iterator<Quad> asyncParseQuads(InputStream input, Lang lang, String baseURI) {
        Iterator<EltStreamRDF> source = asyncParseIterator(inputStreamToSource(input, lang, baseURI));
        return Iter.iter(source).map(elt2Quad).removeNulls();
    }

    /** Pull parser */
    private static Iterator<EltStreamRDF> asyncParseIterator(List<RDFParserBuilder> sources) {
        BlockingQueue<List<EltStreamRDF>> queue = new ArrayBlockingQueue<>(queueSize);
        startParserThread(LOG, sources, queue);
        Iterator<List<EltStreamRDF>> blocks = blockingIterator(queue, x->x==END);
        Iterator<EltStreamRDF> elements = Iter.flatMap(blocks, x->x.iterator());
        return elements;
    }

    // Iterator that stops when an end marker is seen.
    private static  <X> Iterator<X> blockingIterator(BlockingQueue<X> queue, Predicate<X> endTest) {
        return new IteratorSlotted<X>() {
            boolean ended = false;

            @Override
            protected X moveToNext() {
                try {
                    X x = queue.take();
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
        };
    }

    private static void startParserThread(Logger LOG1, List<RDFParserBuilder> parserBuilders, BlockingQueue<List<EltStreamRDF>> queue) {
        // -- Parser thread setup
        Consumer<List<EltStreamRDF>> destination = batch->{
            try {
                queue.put(batch);
            } catch (InterruptedException ex) {
                FmtLog.error(LOG, "Error: %s", ex.getMessage(), ex);
            }
        };
        EltStreamBatcher batcher = new EltStreamBatcher(destination, chunkSize);
        StreamRDF generatorStream = new StreamToElements(batcher);

        ErrorHandler errhandler = new ErrorHandler() {
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

        // Parser thread
        Runnable task = ()->{
            batcher.startBatching();
            if ( LOG1.isDebugEnabled() )
                LOG1.debug("Start parsing");
            try {
                for ( RDFParserBuilder parser : parserBuilders ) {
                    parser.errorHandler(errhandler).parse(generatorStream);
                }
            } catch (RuntimeException ex) {
                // Parse error.
                EltStreamRDF elt = new EltStreamRDF();
                elt.exception = ex;
                batcher.accept(elt);
            } catch (Throwable cause) {
                // Very bad!
                EltStreamRDF elt = new EltStreamRDF();
                elt.exception = new RuntimeException(cause);
                batcher.accept(elt);
            }
            batcher.finishBatching();
            if ( LOG1.isDebugEnabled() )
                LOG1.debug("Finish parsing");
        };
        // Ensures runnable has started.
        //ThreadLib.async(task);
        // Does not ensure runnable has started.
        Thread th = new Thread(task, "AsyncParser");
        th.setDaemon(true);
        th.start();
    }

    private static void dispatch(EltStreamRDF elt, StreamRDF stream) {
        if ( elt.triple != null )
            stream.triple(elt.triple);
        else if ( elt.quad != null )
            stream.quad(elt.quad);
        else if ( elt.prefix != null )
            stream.prefix( elt.prefix, elt.iri);
        else if ( elt.iri != null )
            stream.base(elt.iri);
        else if ( elt.exception != null )
            throw elt.exception;
        else
            throw new InternalErrorException("Bad EltStreamRDF");
    }

    /** Receiver. Take chunks off the queue and send to the output StreamRDF. */
    private static void receiver(Logger LOG2, BlockingQueue<List<EltStreamRDF>> queue, StreamRDF output) {
        // -- Receiver thread
        int count = 0;
        // Receive.
        for(;;) {
            try {
                List<EltStreamRDF> batch = queue.take();
                if ( batch == END ) {
                    FmtLog.debug(LOG2, "Receive: END (%,d)", count);
                    break;
                }
                //List<EltStreamRDF> batch = queue.poll(1, TimeUnit.SECONDS);
                count += batch.size();
                if ( LOG.isDebugEnabled() )
                    FmtLog.debug(LOG2, "Receive: Batch : %,d (%,d)", batch.size(), count);
                dispatch(batch, output);
            } catch (InterruptedException e) {
                FmtLog.error(LOG2, "Interrupted", e);
            }
        }
    }

    private static void dispatch(List<EltStreamRDF> batch, StreamRDF stream) {
        for ( EltStreamRDF elt : batch )
            dispatch(elt, stream);
    }

    /** An item of a StreamRDF, including exceptions. */
    private static class EltStreamRDF {
        Triple triple = null;
        Quad quad = null;
        String prefix = null; // Null implies "base".
        String iri = null;
        RuntimeException exception = null;
    }

    /** Convert a Stream into EltStreamRDF */
    private static  class StreamToElements implements StreamRDF {

        private final Consumer<EltStreamRDF> destination;

        public StreamToElements(Consumer<EltStreamRDF> destination) {
            this.destination = destination;
        }

        @Override
        public void start() {}

        @Override
        public void finish() {}

        @Override
        public void triple(Triple triple) {
            EltStreamRDF elt = new EltStreamRDF();
            elt.triple = triple;
            deliver(elt);
        }

        @Override
        public void quad(Quad quad) {
            EltStreamRDF elt = new EltStreamRDF();
            elt.quad = quad;
            deliver(elt);
        }

        @Override
        public void base(String base) {
            EltStreamRDF elt = new EltStreamRDF();
            //elt.prefix = null;
            elt.iri = base;
            deliver(elt);
        }

        @Override
        public void prefix(String prefix, String iri) {
            EltStreamRDF elt = new EltStreamRDF();
            elt.prefix = prefix;
            elt.iri = iri;
            deliver(elt);
        }

        private void deliver(EltStreamRDF elt) {
            destination.accept(elt);
        }
    }

    /** Batch items from a StreamRDF */
    private static class EltStreamBatcher implements Consumer<EltStreamRDF> {

        private final int batchSize;
        private List<EltStreamRDF> elements = null;
        private final Consumer<List<EltStreamRDF>> batchDestination;

        public EltStreamBatcher(Consumer<List<EltStreamRDF>> batchDestination, int batchSize) {
            this.batchDestination = batchDestination;
            this.batchSize = batchSize;
        }

        public void startBatching() {}

        public void finishBatching() {
            // Flush.
            if ( elements != null ) {
                dispatch(elements);
                elements = null;
            }
            dispatch(END);
        }

        private <X> boolean isEmpty(List<X> list) {
            return list == null || list.isEmpty();
        }

        @Override
        public void accept(EltStreamRDF elt) {
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
        private void dispatch(List<EltStreamRDF> batch) {
            count += batch.size();
            batchDestination.accept(batch);
        }

        private List<EltStreamRDF> allocChunk() {
            return new ArrayList<>(batchSize);
        }
    }
}


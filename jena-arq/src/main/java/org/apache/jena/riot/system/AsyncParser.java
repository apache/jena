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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Async parsing - parses a list of files on a separate thread.
 * <p>
 * The parser runs on a separate thread and sends its output to a StreamRDF on the
 * callers thread. The parser output is batched into blocks and placed on a
 * finite-sized concurrent BlockingQueue.
 * <p>
 * There are overheads, so this is only beneficial in some situations. Delivery to
 * the StreamRDF has an initial latency while the first batch of work is accumulated.
 */
public class AsyncParser {

    private static Logger LOG = LoggerFactory.getLogger(AsyncParser.class);
    private static int chunkSize = 100_000;
    // There is no point letting the parser get a long way ahead.
    private static int queueSize = 10;

    private AsyncParser() {}

    /**
     * Function to that parses a list of files on a separate thread
     * and sends the output to a StreamRDF on the callers thread.
     */
    public static void asyncParse(List<String> files, StreamRDF output) {
        LOG.debug("Parse: "+files);
        BlockingQueue<List<EltStreamRDF>> queue = new ArrayBlockingQueue<>(queueSize);

        // Async thread
        Logger LOG1 = LOG;
        // Receiver
        Logger LOG2 = LOG;

        // Parser thread setup
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
                files.forEach(fn-> {
                    if ( LOG1.isDebugEnabled() )
                        LOG1.debug("Parse "+fn);
                    RDFParser.source(fn).errorHandler(errhandler).parse(generatorStream);
                } );
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
        th.start();

        // Receiver thread
        ElementsToStream e2s = new ElementsToStream(output);
        int count = 0;
        // Receive.
        for(;;) {
            try {
                List<EltStreamRDF> batch = queue.take();
                //List<EltStreamRDF> batch = queue.poll(1, TimeUnit.SECONDS);
                if ( batch == null ) {
                    if ( LOG2.isDebugEnabled() )
                        FmtLog.debug(LOG2, "Receive: Null batch");
                    continue;
                }
                count += batch.size();
                if ( LOG.isDebugEnabled() )
                    FmtLog.debug(LOG2, "Receive: Batch : %,d (%,d)", batch.size(), count);
                e2s.receive(batch);
                if ( e2s.finished() )
                    break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** Capture an item of an StreamRDF */
    private static class EltStreamRDF {
        // test with ==
        final static EltStreamRDF END = new EltStreamRDF();

        Triple triple = null;
        Quad quad = null;
        String prefix = null; // Null implies "base".
        String iri = null;
        RuntimeException exception = null;
    }

    /** Convert an Stream into EltStreamRDF */
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
            List<EltStreamRDF> x = List.of(EltStreamRDF.END);
            dispatch(x);
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

    /** Convert a batch of items and play them on an StreamRDF. */
    private static class ElementsToStream {

        boolean finished = false;
        private final StreamRDF stream;

        public ElementsToStream(StreamRDF stream) {
            this.stream = stream;
        }

        public void receive(List<EltStreamRDF> elements) {
            if ( elements == null )
                return;
            for ( EltStreamRDF elt : elements ) {
                if ( elt.triple != null )
                    stream.triple(elt.triple);
                else if ( elt.quad != null )
                    stream.quad(elt.quad);
                else if ( elt.prefix != null )
                    stream.prefix( elt.prefix, elt.iri);
                else if ( elt.iri != null )
                    stream.base(elt.iri);
                else if ( elt.exception != null ) {
                    finished = true;
                    throw elt.exception;
                } else if ( elt == EltStreamRDF.END ) {
                    finished = true;
                    break;
                } else {
                    throw new InternalErrorException("Bad EltStreamRDF");
                }
            }
        }

        public boolean finished() {
            return finished;
        }
    }
}


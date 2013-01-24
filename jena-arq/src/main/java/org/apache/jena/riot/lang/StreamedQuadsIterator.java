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

import org.apache.jena.atlas.lib.Tuple;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * An RDF stream which also provides a quad iterator
 * <p>
 * Behind the scenes this is implemented as a blocking queue, the RDF stream
 * will accept quads while there is capacity and the iterator will block while
 * waiting for further quads
 * </p>
 * <p>
 * This stream silently discards any triples or tuples
 * </p>
 * <p>
 * In order for this to work correctly the producer thread which is passing
 * information to the {@link StreamRDF} interface typically needs to be on a
 * separate thread. However this depends on how the producer and the consumer
 * are started, if the start of the producer does not block the main thread
 * the consumer can happily be on the main thread.  Generally speaking though
 * it is good practice to ensure that one of the actors on this class is
 * running on a different thread.
 * </p>
 */
public class StreamedQuadsIterator extends StreamedRDFIterator<Quad> {

    /**
     * Creates a new streamed quads iterator using the default buffer size of
     * {@link StreamedRDFIterator#DEFAULT_BUFFER_SIZE}
     * <p>
     * See {@link #StreamedQuadsIterator(int, boolean)} for more discussion of
     * the parameters
     * </p>
     */
    public StreamedQuadsIterator() {
        this(StreamedRDFIterator.DEFAULT_BUFFER_SIZE, false);
    }

    /**
     * Creates a new streamed quads iterator
     * <p>
     * See {@link #StreamedQuadsIterator(int, boolean)} for more discussion of
     * the parameters
     * </p>
     * 
     * @param bufferSize
     *            Buffer size
     */
    public StreamedQuadsIterator(int bufferSize) {
        this(bufferSize, false);
    }

    /**
     * Creates a new streamed quads iterator
     * <p>
     * Buffer size must be chosen carefully in order to avoid performance
     * problems, if you set the buffer size too low you will experience a lot of
     * blocked calls so it will take longer to consume the data from the
     * iterator. For best performance the buffer size should be at least 10% of
     * the expected input size though you may need to tune this depending on how
     * fast your consumer thread is.
     * </p>
     * <p>
     * The fair parameter controls whether the locking policy used for the
     * buffer is fair. When enabled this reduces throughput but also reduces the
     * chance of thread starvation.
     * </p>
     * 
     * @param bufferSize
     *            Buffer size
     * @param fair
     *            Whether the buffer should use a fair locking policy
     */
    public StreamedQuadsIterator(int bufferSize, boolean fair) {
        super(bufferSize, fair);
    }

    @Override
    public void triple(Triple triple) {
        // Triples are discarded
    }

    @Override
    public void quad(Quad quad) {
        if (quad == null)
            return;
        while (true) {
            try {
                this.buffer.put(quad);
                break;
            } catch (InterruptedException e) {
                // Ignore and retry
            }
        }
    }

    @Override
    public void tuple(Tuple<Node> tuple) {
        // Tuples are discarded
    }

}

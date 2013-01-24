/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package org.apache.jena.riot.lang;

import org.apache.jena.atlas.lib.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * An RDF stream which also provides a tuple iterator
 * <p>
 * Behind the scenes this is implemented as a blocking queue, the RDF stream
 * will accept tuples while there is capacity and the iterator will block while
 * waiting for further tuples
 * </p>
 * <p>
 * This stream silently discards any triples or quads
 * </p>
 * 
 */
public class StreamedTuplesIterator extends StreamedRDFIterator<Tuple<Node>> {

    /**
     * Creates a new streamed tuples iterator using the default buffer size of
     * {@link StreamedRDFIterator#DEFAULT_BUFFER_SIZE}
     * <p>
     * See {@link #StreamedTuplesIterator(int, boolean)} for more discussion of
     * the parameters
     * </p>
     */
    public StreamedTuplesIterator() {
        this(StreamedRDFIterator.DEFAULT_BUFFER_SIZE, false);
    }

    /**
     * Creates a new streamed tuples iterator
     * <p>
     * See {@link #StreamedTuplesIterator(int, boolean)} for more discussion of
     * the parameters
     * </p>
     * 
     * @param bufferSize
     *            Buffer size
     */
    public StreamedTuplesIterator(int bufferSize) {
        this(bufferSize, false);
    }

    /**
     * Creates a new streamed tuples iterator
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
    public StreamedTuplesIterator(int bufferSize, boolean fair) {
        super(bufferSize, fair);
    }

    @Override
    public void triple(Triple triple) {
        // Triples are discarded
    }

    @Override
    public void quad(Quad quad) {
        // Quads are discarded
    }

    @Override
    public void tuple(Tuple<Node> tuple) {
        if (tuple == null)
            return;
        while (true) {
            try {
                this.buffer.put(tuple);
                break;
            } catch (InterruptedException e) {
                // Ignore and retry
            }
        }
    }

}

/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.util;

import org.apache.jena.atlas.lib.Tuple;
import org.apache.jena.riot.lang.PipedRDFIterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * A tracked piped triples stream
 * 
 * @author rvesse
 * 
 */
public class TrackedPipedTriplesStream extends TrackedPipedRDFStream<Triple> {

    /**
     * Creates a tracked triples stream
     * 
     * @param sink
     *            Sink
     * @param input
     *            Input stream
     */
    public TrackedPipedTriplesStream(PipedRDFIterator<Triple> sink, TrackableInputStream input) {
        super(sink, input);
    }

    @Override
    public void triple(Triple triple) {
        receive(triple);
    }

    @Override
    public void quad(Quad quad) {
        // Quads are discarded
    }

    @Override
    public void tuple(Tuple<Node> tuple) {
        // Tuples are discarded
    }

}

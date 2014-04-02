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
 * A tracked piped quads stream
 * 
 * @author rvesse
 * 
 */
public class TrackedPipedQuadsStream extends TrackedPipedRDFStream<Quad> {

    /**
     * Creates a new stream
     * 
     * @param sink
     *            Sink
     * @param input
     *            Input stream
     */
    public TrackedPipedQuadsStream(PipedRDFIterator<Quad> sink, TrackableInputStream input) {
        super(sink, input);
    }

    @Override
    public void triple(Triple triple) {
        // Triples are discarded
    }

    @Override
    public void quad(Quad quad) {
        this.receive(quad);
    }

    @Override
    public void tuple(Tuple<Node> tuple) {
        // Tuples are discarded
    }

}

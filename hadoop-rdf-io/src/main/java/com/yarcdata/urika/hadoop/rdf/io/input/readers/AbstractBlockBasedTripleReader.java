/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import org.apache.jena.riot.lang.PipedRDFIterator;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.io.input.util.TrackableInputStream;
import com.yarcdata.urika.hadoop.rdf.io.input.util.TrackedPipedRDFStream;
import com.yarcdata.urika.hadoop.rdf.io.input.util.TrackedPipedTriplesStream;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * An abstract record reader for whole file triple formats
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractBlockBasedTripleReader extends AbstractBlockBasedNodeTupleReader<Triple, TripleWritable> {

    @Override
    protected PipedRDFIterator<Triple> getPipedIterator() {
        return new PipedRDFIterator<Triple>();
    }

    @Override
    protected TrackedPipedRDFStream<Triple> getPipedStream(PipedRDFIterator<Triple> iterator, TrackableInputStream input) {
        return new TrackedPipedTriplesStream(iterator, input);
    }

    @Override
    protected TripleWritable createInstance(Triple tuple) {
        return new TripleWritable(tuple);
    }
}

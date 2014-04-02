/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import org.apache.jena.riot.lang.PipedRDFIterator;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.io.input.util.TrackableInputStream;
import com.yarcdata.urika.hadoop.rdf.io.input.util.TrackedPipedQuadsStream;
import com.yarcdata.urika.hadoop.rdf.io.input.util.TrackedPipedRDFStream;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * An abstract record reader for whole file triple formats
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractWholeFileQuadReader extends AbstractWholeFileNodeTupleReader<Quad, QuadWritable> {

    @Override
    protected PipedRDFIterator<Quad> getPipedIterator() {
        return new PipedRDFIterator<Quad>();
    }

    @Override
    protected TrackedPipedRDFStream<Quad> getPipedStream(PipedRDFIterator<Quad> iterator, TrackableInputStream input) {
        return new TrackedPipedQuadsStream(iterator, input);
    }

    @Override
    protected QuadWritable createInstance(Quad tuple) {
        return new QuadWritable(tuple);
    }
}

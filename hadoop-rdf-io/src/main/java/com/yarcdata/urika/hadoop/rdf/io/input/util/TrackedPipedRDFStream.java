/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.util;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;

/**
 * A tracked piped RDF stream
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Type corresponding to a supported RDF primitive
 */
public abstract class TrackedPipedRDFStream<T> extends PipedRDFStream<T> {

    private TrackableInputStream input;
    private Queue<Long> positions = new LinkedList<Long>();

    protected TrackedPipedRDFStream(PipedRDFIterator<T> sink, TrackableInputStream input) {
        super(sink);
        this.input = input;
    }

    @Override
    protected void receive(T t) {
        // Track positions the input stream is at as we receive inputs
        synchronized (this.positions) {
            this.positions.add(this.input.getBytesRead());
        }
        super.receive(t);
    }

    /**
     * Gets the next position
     * 
     * @return Position
     */
    public Long getPosition() {
        synchronized (this.positions) {
            return this.positions.poll();
        }
    }
}

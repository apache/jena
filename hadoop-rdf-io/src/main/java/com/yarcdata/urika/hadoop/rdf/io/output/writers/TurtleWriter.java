/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.riot.Lang;

/**
 * A record writer for Turtle
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public class TurtleWriter<TKey> extends AbstractBatchedTripleWriter<TKey> {

    /**
     * Creates a new record writer
     * 
     * @param writer
     *            Writer
     * @param batchSize
     *            Batch size
     */
    public TurtleWriter(Writer writer, long batchSize) {
        super(writer, batchSize);
    }

    @Override
    protected Lang getRdfLanguage() {
        return Lang.TURTLE;
    }

}

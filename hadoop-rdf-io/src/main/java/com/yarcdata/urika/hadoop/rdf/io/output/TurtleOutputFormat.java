/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.mapreduce.RecordWriter;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.io.output.writers.TurtleWriter;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Turtle output format
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class TurtleOutputFormat<TKey> extends AbstractBatchedNodeTupleOutputFormat<TKey, Triple, TripleWritable> {

    @Override
    protected RecordWriter<TKey, TripleWritable> getRecordWriter(Writer writer, long batchSize) {
        return new TurtleWriter<TKey>(writer, batchSize);
    }

    @Override
    protected String getFileExtension() {
        return ".ttl";
    }

}

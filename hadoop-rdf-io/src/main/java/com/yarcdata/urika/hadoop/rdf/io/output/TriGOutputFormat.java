/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.mapreduce.RecordWriter;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.io.output.writers.TriGWriter;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Output format for TriG
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class TriGOutputFormat<TKey> extends AbstractBatchedNodeTupleOutputFormat<TKey, Quad, QuadWritable> {

    @Override
    protected RecordWriter<TKey, QuadWritable> getRecordWriter(Writer writer, long batchSize) {
        return new TriGWriter<TKey>(writer, batchSize);
    }

    @Override
    protected String getFileExtension() {
        return ".trig";
    }

}

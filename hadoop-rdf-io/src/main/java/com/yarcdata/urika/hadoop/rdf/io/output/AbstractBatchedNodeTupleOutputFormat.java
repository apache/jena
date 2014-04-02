/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;

import com.yarcdata.urika.hadoop.rdf.io.RdfIOConstants;
import com.yarcdata.urika.hadoop.rdf.io.output.writers.AbstractBatchedNodeTupleWriter;
import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;

/**
 * Abstract output format for formats that use a
 * {@link AbstractBatchedNodeTupleWriter} as their writer
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable tuple type
 */
public abstract class AbstractBatchedNodeTupleOutputFormat<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        AbstractNodeTupleOutputFormat<TKey, TValue, T> {

    @Override
    protected RecordWriter<TKey, T> getRecordWriter(Writer writer, Configuration config) {
        long batchSize = config.getLong(RdfIOConstants.OUTPUT_BATCH_SIZE, RdfIOConstants.DEFAULT_OUTPUT_BATCH_SIZE);
        return this.getRecordWriter(writer, batchSize);
    }
    
    protected abstract RecordWriter<TKey, T> getRecordWriter(Writer writer, long batchSize);

}

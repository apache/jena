/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;

import com.yarcdata.urika.hadoop.rdf.io.output.writers.NTriplesNodeWriter;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;

/**
 * NTriples based node output format
 * 
 * @author rvesse
 * 
 * @param <TValue>
 *            Value type
 */
public class NTriplesNodeOutputFormat<TValue> extends AbstractNodeOutputFormat<TValue> {

    @Override
    protected RecordWriter<NodeWritable, TValue> getRecordWriter(Writer writer, Configuration config) {
        return new NTriplesNodeWriter<TValue>(writer);
    }

}

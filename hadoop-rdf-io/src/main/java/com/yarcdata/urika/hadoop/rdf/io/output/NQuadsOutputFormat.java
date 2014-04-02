/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.io.output.writers.NQuadsWriter;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * NQuads output format
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class NQuadsOutputFormat<TKey> extends AbstractNodeTupleOutputFormat<TKey, Quad, QuadWritable> {

    @Override
    protected RecordWriter<TKey, QuadWritable> getRecordWriter(Writer writer, Configuration config) {
        return new NQuadsWriter<TKey>(writer);
    }

    @Override
    protected String getFileExtension() {
        return ".nq";
    }

}

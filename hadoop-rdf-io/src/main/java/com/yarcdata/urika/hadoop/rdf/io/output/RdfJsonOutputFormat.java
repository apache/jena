/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.io.output.writers.RdfJsonWriter;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * RDF/JSON output format
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class RdfJsonOutputFormat<TKey> extends AbstractNodeTupleOutputFormat<TKey, Triple, TripleWritable> {

    @Override
    protected String getFileExtension() {
        return ".rj";
    }

    @Override
    protected RecordWriter<TKey, TripleWritable> getRecordWriter(Writer writer, Configuration config) {
        return new RdfJsonWriter<TKey>(writer);
    }

}

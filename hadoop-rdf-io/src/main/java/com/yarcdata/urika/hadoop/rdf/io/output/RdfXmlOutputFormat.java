/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.io.output.writers.RdfXmlWriter;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * RDF/XML output format
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class RdfXmlOutputFormat<TKey> extends AbstractNodeTupleOutputFormat<TKey, Triple, TripleWritable> {

    @Override
    protected String getFileExtension() {
        return ".rdf";
    }

    @Override
    protected RecordWriter<TKey, TripleWritable> getRecordWriter(Writer writer, Configuration config) {
        return new RdfXmlWriter<TKey>(writer);
    }

}

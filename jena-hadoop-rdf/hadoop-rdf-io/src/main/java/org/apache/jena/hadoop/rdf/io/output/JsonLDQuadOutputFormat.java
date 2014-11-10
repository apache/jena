package org.apache.jena.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.jena.hadoop.rdf.io.output.writers.JsonLDQuadWriter;
import org.apache.jena.hadoop.rdf.types.QuadWritable;

import com.hp.hpl.jena.sparql.core.Quad;

public class JsonLDQuadOutputFormat<TKey> extends AbstractNodeTupleOutputFormat<TKey, Quad, QuadWritable> {

    @Override
    protected String getFileExtension() {
        return ".jsonld";
    }

    @Override
    protected RecordWriter<TKey, QuadWritable> getRecordWriter(Writer writer, Configuration config) {
        return new JsonLDQuadWriter<TKey>(writer);
    }

}

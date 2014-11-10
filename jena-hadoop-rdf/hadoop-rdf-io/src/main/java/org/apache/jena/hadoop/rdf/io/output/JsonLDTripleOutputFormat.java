package org.apache.jena.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.jena.hadoop.rdf.io.output.writers.JsonLDTripleWriter;
import org.apache.jena.hadoop.rdf.types.TripleWritable;

import com.hp.hpl.jena.graph.Triple;

public class JsonLDTripleOutputFormat<TKey> extends AbstractNodeTupleOutputFormat<TKey, Triple, TripleWritable> {

    @Override
    protected String getFileExtension() {
        return ".jsonld";
    }

    @Override
    protected RecordWriter<TKey, TripleWritable> getRecordWriter(Writer writer, Configuration config) {
        return new JsonLDTripleWriter<TKey>(writer);
    }

}

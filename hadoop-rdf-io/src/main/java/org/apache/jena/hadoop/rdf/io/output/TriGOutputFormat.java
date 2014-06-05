package org.apache.jena.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.jena.hadoop.rdf.io.output.writers.StreamRdfQuadWriter;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.writer.WriterStreamRDFBlocks;

import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Output format for TriG
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 */
public class TriGOutputFormat<TKey> extends
		AbstractStreamRdfNodeTupleOutputFormat<TKey, Quad, QuadWritable> {

	@Override
	protected RecordWriter<TKey, QuadWritable> getRecordWriter(
			StreamRDF stream, Writer writer, Configuration config) {
		return new StreamRdfQuadWriter<TKey>(stream, writer);
	}

	@Override
	protected StreamRDF getStream(Writer writer, Configuration config) {
		return new WriterStreamRDFBlocks(writer);
	}

	@Override
	protected String getFileExtension() {
		return ".trig";
	}

}

package org.apache.jena.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.sparql.core.Quad;

/**
 * A writer for {@link StreamRDF} based quad writers
 * 
 * @param <TKey>
 *            Key type
 */
public class StreamRdfQuadWriter<TKey> extends
		AbstractStreamRdfNodeTupleWriter<TKey, Quad, QuadWritable> {

	public StreamRdfQuadWriter(StreamRDF stream, Writer writer) {
		super(stream, writer);
	}

	@Override
	protected void sendOutput(TKey key, QuadWritable value, StreamRDF stream) {
		stream.quad(value.get());
	}
}

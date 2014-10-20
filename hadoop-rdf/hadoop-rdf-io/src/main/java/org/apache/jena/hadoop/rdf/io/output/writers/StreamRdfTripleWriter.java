package org.apache.jena.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Triple;

/**
 * A writer for {@link StreamRDF} based triple writers
 * 
 * @param <TKey>
 *            Key type
 */
public class StreamRdfTripleWriter<TKey> extends AbstractStreamRdfNodeTupleWriter<TKey, Triple, TripleWritable> {

	public StreamRdfTripleWriter(StreamRDF stream, Writer writer) {
		super(stream, writer);
	}

	@Override
	protected void sendOutput(TKey key, TripleWritable value, StreamRDF stream) {
		stream.triple(value.get());
	}
}

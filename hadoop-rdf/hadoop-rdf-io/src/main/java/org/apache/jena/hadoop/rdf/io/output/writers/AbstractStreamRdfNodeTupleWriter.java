package org.apache.jena.hadoop.rdf.io.output.writers;

import java.io.IOException;
import java.io.Writer;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.riot.system.StreamRDF;

public abstract class AbstractStreamRdfNodeTupleWriter<TKey, TTuple, TValue extends AbstractNodeTupleWritable<TTuple>>
		extends RecordWriter<TKey, TValue> {

	private StreamRDF stream;
	private Writer writer;

	public AbstractStreamRdfNodeTupleWriter(StreamRDF stream, Writer writer) {
		if (stream == null)
			throw new NullPointerException("stream cannot be null");
		if (writer == null)
			throw new NullPointerException("writer cannot be null");
		this.stream = stream;
		this.stream.start();
		this.writer = writer;
	}

	@Override
	public void close(TaskAttemptContext context) throws IOException,
			InterruptedException {
		this.stream.finish();
		this.writer.close();
	}

	@Override
	public void write(TKey key, TValue value) throws IOException,
			InterruptedException {
		this.sendOutput(key, value, this.stream);
	}

	/**
	 * Method that handles an actual key value pair passing it to the
	 * {@link StreamRDF} instance as appropriate
	 * 
	 * @param key
	 *            Key
	 * @param value
	 *            Value
	 * @param stream
	 *            RDF Stream
	 */
	protected abstract void sendOutput(TKey key, TValue value, StreamRDF stream);

}

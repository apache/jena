package org.apache.jena.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.riot.system.StreamRDF;

/**
 * Abstract output format for formats that use the RIOT {@link StreamRDF} API to
 * stream the writes
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 * @param <TTuple>
 *            Tuple type
 * @param <TValue>
 *            Writable tuple type i.e. the value type
 */
public abstract class AbstractStreamRdfNodeTupleOutputFormat<TKey, TTuple, TValue extends AbstractNodeTupleWritable<TTuple>>
		extends AbstractNodeTupleOutputFormat<TKey, TTuple, TValue> {

	@Override
	protected RecordWriter<TKey, TValue> getRecordWriter(Writer writer,
			Configuration config) {
		return getRecordWriter(getStream(writer, config), writer, config);
	}

	/**
	 * Gets a writer which provides a bridge between the {@link RecordWriter}
	 * and {@link StreamRDF} APIs
	 * 
	 * @param stream
	 *            RDF Stream
	 * @param writer
	 *            Writer
	 * @param config
	 *            Configuration
	 * @return Record Writer
	 */
	protected abstract RecordWriter<TKey, TValue> getRecordWriter(
			StreamRDF stream, Writer writer, Configuration config);

	/**
	 * Gets a {@link StreamRDF} to which the tuples to be output should be
	 * passed
	 * 
	 * @param writer
	 *            Writer
	 * @param config
	 *            Configuration
	 * @return RDF Stream
	 */
	protected abstract StreamRDF getStream(Writer writer, Configuration config);
}

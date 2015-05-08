/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	public void close(TaskAttemptContext context) throws IOException {
		this.stream.finish();
		this.writer.close();
	}

	@Override
	public void write(TKey key, TValue value) {
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

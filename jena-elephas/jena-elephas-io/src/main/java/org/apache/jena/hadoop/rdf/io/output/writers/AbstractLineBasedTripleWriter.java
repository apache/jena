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

import java.io.Writer;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;

/**
 * An abstract writer for line based triple formats
 * 
 * @param <TKey>
 *            Key type
 * 
 */
public abstract class AbstractLineBasedTripleWriter<TKey> extends
		AbstractLineBasedNodeTupleWriter<TKey, Triple, TripleWritable> {

	/**
	 * Creates a new writer using the default NTriples node formatter
	 * 
	 * @param writer
	 *            Writer
	 */
	public AbstractLineBasedTripleWriter(Writer writer) {
		this(writer, new NodeFormatterNT());
	}

	/**
	 * Creates a new writer using the specified node formatter
	 * 
	 * @param writer
	 *            Writer
	 * @param formatter
	 *            Node formatter
	 */
	public AbstractLineBasedTripleWriter(Writer writer, NodeFormatter formatter) {
		super(writer, formatter);
	}

	@Override
	protected Node[] getNodes(TripleWritable tuple) {
		Triple t = tuple.get();
		return new Node[] { t.getSubject(), t.getPredicate(), t.getObject() };
	}

}

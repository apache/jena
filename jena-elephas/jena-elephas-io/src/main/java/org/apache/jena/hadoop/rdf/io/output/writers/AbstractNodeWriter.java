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

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.Writer2;
import org.apache.jena.atlas.lib.Tuple;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.types.NodeTupleWritable;
import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.apache.jena.sparql.core.Quad ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a record writer which writes pairs of nodes and
 * arbitrary values to text based files
 * 
 * 
 * 
 * @param <TValue>
 */
public abstract class AbstractNodeWriter<TValue> extends RecordWriter<NodeWritable, TValue> {

    /**
     * Default separator written between nodes and their associated values
     */
    public static final String DEFAULT_SEPARATOR = "\t";

    private static final Logger log = LoggerFactory.getLogger(AbstractNodeWriter.class);

    protected AWriter writer;
    private NodeFormatter formatter;

    /**
     * Creates a new tuple writer using the default NTriples node formatter
     * 
     * @param writer
     *            Writer
     */
    public AbstractNodeWriter(Writer writer) {
        this(writer, new NodeFormatterNT());
    }

    /**
     * Creates a new tuple writer
     * 
     * @param writer
     *            Writer
     * @param formatter
     *            Node formatter
     */
    public AbstractNodeWriter(Writer writer, NodeFormatter formatter) {
        if (writer == null)
            throw new NullPointerException("writer cannot be null");
        if (formatter == null)
            throw new NullPointerException("formatter cannot be null");
        this.formatter = formatter;
        this.writer = Writer2.wrap(writer);
    }

    @Override
    public final void write(NodeWritable key, TValue value) {
        this.writeKey(key);
        this.writer.write(this.getSeparator());
        this.writeValue(value);
        this.writer.write('\n');
    }

    /**
     * Writes the given key
     * 
     * @param key
     *            Key
     */
    protected void writeKey(NodeWritable key) {
        writeNode(key.get());
    }

    /**
     * Writes a Node
     * 
     * @param n
     *            Node
     */
    protected void writeNode(Node n) {
        this.getNodeFormatter().format(this.writer, n);
    }

    /**
     * Writes a sequence of nodes
     * 
     * @param ns
     *            Nodes
     */
    protected void writeNodes(Node... ns) {
        String sep = this.getSeparator();
        for (int i = 0; i < ns.length; i++) {
            writeNode(ns[i]);
            if (i < ns.length - 1)
                this.writer.write(sep);
        }
    }

    /**
     * Writes the given value
     * <p>
     * If the value is one of the RDF primitives - {@link NodeWritable},
     * {@link TripleWritable}, {@link QuadWritable} and
     * {@link NodeTupleWritable} - then it is formatted as a series of nodes
     * separated by the separator. Otherwise it is formatted by simply calling
     * {@code toString()} on it.
     * </p>
     * 
     * @param value
     *            Values
     */
    protected void writeValue(TValue value) {
        // Handle null specially
        if (value instanceof NullWritable || value == null)
            return;

        // Handle RDF primitives specially and format them as proper nodes
        if (value instanceof NodeWritable) {
            this.writeKey((NodeWritable) value);
        } else if (value instanceof TripleWritable) {
            Triple t = ((TripleWritable) value).get();
            this.writeNodes(t.getSubject(), t.getPredicate(), t.getObject());
        } else if (value instanceof QuadWritable) {
            Quad q = ((QuadWritable) value).get();
            this.writeNodes(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
        } else if (value instanceof NodeTupleWritable) {
            Tuple<Node> tuple = ((NodeTupleWritable) value).get();
            this.writeNodes(tuple.tuple());
        } else {
            // For arbitrary values just toString() them
            this.writer.write(value.toString());
        }
    }

    @Override
    public void close(TaskAttemptContext context) {
        log.debug("close({})", context);
        writer.close();
    }

    /**
     * Gets the node formatter to use for formatting nodes
     * 
     * @return Node formatter
     */
    protected NodeFormatter getNodeFormatter() {
        return this.formatter;
    }

    /**
     * Gets the separator that is written between nodes
     * 
     * @return Separator
     */
    protected String getSeparator() {
        return DEFAULT_SEPARATOR;
    }
}

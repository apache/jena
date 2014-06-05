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

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.Writer2;
import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;

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
    public final void write(NodeWritable key, TValue value) throws IOException, InterruptedException {
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
        Node n = key.get();
        this.getNodeFormatter().format(this.writer, n);
    }

    /**
     * Writes the given value
     * 
     * @param value
     */
    protected void writeValue(TValue value) {
        if (value instanceof NullWritable)
            return;
        this.writer.write(value.toString());
    }

    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException {
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

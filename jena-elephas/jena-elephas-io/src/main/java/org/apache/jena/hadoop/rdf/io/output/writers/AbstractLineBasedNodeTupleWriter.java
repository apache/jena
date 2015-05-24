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

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.Writer2;
import org.apache.jena.graph.Node ;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of a record writer that writes records to a line
 * based tuple formats.
 * <p>
 * The implementation only writes the value portion of the key value pair since
 * it is the value portion that is used to convey the node tuples
 * </p>
 * 
 * 
 * @param <TKey>
 *            Key type
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable node tuple type
 * 
 */
public abstract class AbstractLineBasedNodeTupleWriter<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        RecordWriter<TKey, T> {
    /**
     * Default separator written between nodes
     */
    public static final String DEFAULT_SEPARATOR = " ";
    /**
     * Default terminator written at the end of each line
     */
    public static final String DEFAULT_TERMINATOR = ".";

    private static final Logger log = LoggerFactory.getLogger(AbstractLineBasedNodeTupleWriter.class);

    private AWriter writer;
    private NodeFormatter formatter;

    /**
     * Creates a new tuple writer using the default NTriples node formatter
     * 
     * @param writer
     *            Writer
     */
    public AbstractLineBasedNodeTupleWriter(Writer writer) {
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
    public AbstractLineBasedNodeTupleWriter(Writer writer, NodeFormatter formatter) {
        if (writer == null)
            throw new NullPointerException("writer cannot be null");
        if (formatter == null)
            throw new NullPointerException("formatter cannot be null");
        this.formatter = formatter;
        this.writer = Writer2.wrap(writer);
    }

    @Override
    public void write(TKey key, T value) {
        log.debug("write({}={})", key, value);

        Node[] ns = this.getNodes(value);
        String sep = this.getSeparator();
        NodeFormatter formatter = this.getNodeFormatter();
        for (int i = 0; i < ns.length; i++) {
            formatter.format(this.writer, ns[i]);
            this.writer.print(sep);
        }
        this.writer.println(this.getTerminator());
        this.writer.flush();
    }

    /**
     * Gets the nodes of the tuple in the order they should be written
     * 
     * @param tuple
     *            Tuple
     * @return Nodes
     */
    protected abstract Node[] getNodes(T tuple);

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

    /**
     * Gets the terminator that is written at the end of each tuple
     * 
     * @return Terminator
     */
    protected String getTerminator() {
        return DEFAULT_TERMINATOR;
    }

    @Override
    public void close(TaskAttemptContext context) {
        log.debug("close({})", context);
        writer.close();
    }
}
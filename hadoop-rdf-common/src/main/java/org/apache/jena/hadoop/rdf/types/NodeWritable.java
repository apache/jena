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

package org.apache.jena.hadoop.rdf.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.jena.hadoop.rdf.types.compators.NodeComparator;
import org.apache.jena.hadoop.rdf.types.converters.ThriftConverter;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.ThriftConvert;
import org.apache.jena.riot.thrift.wire.RDF_Term;
import org.apache.thrift.TException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.util.NodeUtils;

/**
 * A writable for {@link Node} instances
 */
public class NodeWritable implements WritableComparable<NodeWritable> {

    static {
        WritableComparator.define(NodeWritable.class, new NodeComparator());
    }

    private Node node;
    private RDF_Term term = new RDF_Term();

    /**
     * Creates an empty writable
     */
    public NodeWritable() {
        this(null);
    }

    /**
     * Creates a new instance from the given input
     * 
     * @param input
     *            Input
     * @return New instance
     * @throws IOException
     */
    public static NodeWritable read(DataInput input) throws IOException {
        NodeWritable nw = new NodeWritable();
        nw.readFields(input);
        return nw;
    }

    /**
     * Creates a new writable with the given value
     * 
     * @param n
     *            Node
     */
    public NodeWritable(Node n) {
        this.set(n);
    }

    /**
     * Gets the node
     * 
     * @return Node
     */
    public Node get() {
        return this.node;
    }

    /**
     * Sets the node
     * 
     * @param n
     *            Node
     */
    public void set(Node n) {
        this.node = n;
        // Clear the term for now
        // Only convert the Node to it as and when we want to write it out
        this.term.clear();
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        this.term.clear();
        int termLength = input.readInt();
        byte[] buffer = new byte[termLength];
        input.readFully(buffer);
        try {
            ThriftConverter.fromBytes(buffer, this.term);
        } catch (TException e) {
            throw new IOException(e);
        }
        this.node = ThriftConvert.convert(this.term);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        // May not yet have prepared the Thrift term
        if (!this.term.isSet()) {
            if (this.node == null) {
                this.term.setUndefined(TRDF.UNDEF);
            } else {
                ThriftConvert.toThrift(this.node, null, this.term, false);
            }
        }

        byte[] buffer;
        try {
            buffer = ThriftConverter.toBytes(this.term);
        } catch (TException e) {
            throw new IOException(e);
        }
        output.writeInt(buffer.length);
        output.write(buffer);
    }

    @Override
    public int compareTo(NodeWritable other) {
        return NodeUtils.compareRDFTerms(this.node, other.node);
    }

    @Override
    public String toString() {
        if (this.node == null)
            return "";
        return this.node.toString();
    }

    @Override
    public int hashCode() {
        return this.node.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NodeWritable))
            return false;
        return this.compareTo((NodeWritable) other) == 0;
    }
}

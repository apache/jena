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
import org.apache.jena.graph.Node ;
import org.apache.jena.hadoop.rdf.types.comparators.SimpleBinaryComparator;
import org.apache.jena.hadoop.rdf.types.converters.ThriftConverter;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.ThriftConvert;
import org.apache.jena.riot.thrift.wire.RDF_Term;
import org.apache.jena.sparql.util.NodeUtils ;
import org.apache.thrift.TException;

/**
 * A writable for {@link Node} instances
 * <p>
 * This uses <a
 * href="http://afs.github.io/rdf-thrift/rdf-binary-thrift.html">RDF Thrift</a>
 * for the binary encoding of terms. The in-memory storage for this type is both
 * a {@link Node} and a {@link RDF_Term} with lazy conversion between the two
 * forms as necessary.
 * </p>
 */
public class NodeWritable implements WritableComparable<NodeWritable> {

    static {
        WritableComparator.define(NodeWritable.class, new SimpleBinaryComparator());
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
        // We may not have yet loaded the node
        if (this.node == null) {
            // If term is set to undefined then node is supposed to be null
            if (this.term.isSet() && !this.term.isSetUndefined()) {
                this.node = ThriftConvert.convert(this.term);
            }
        }
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
        // We only convert the Node to a term as and when we want to write it
        // out in order to not waste effort if the value is never written out
        this.term.clear();
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        // Clear previous value
        this.node = null;
        this.term.clear();

        // Read in the new value
        int termLength = input.readInt();
        byte[] buffer = new byte[termLength];
        input.readFully(buffer);
        try {
            ThriftConverter.fromBytes(buffer, this.term);
        } catch (TException e) {
            throw new IOException(e);
        }

        // Note that we don't convert it back into a Node at this time
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

        // Write out the Thrift term
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
        // Use get() rather than accessing the field directly because the node
        // field is lazily instantiated from the Thrift term
        return NodeUtils.compareRDFTerms(this.get(), other.get());
    }

    @Override
    public String toString() {
        // Use get() rather than accessing the field directly because the node
        // field is lazily instantiated from the Thrift term
        Node n = this.get();
        if (n == null)
            return "";
        return n.toString();
    }

    @Override
    public int hashCode() {
        // Use get() rather than accessing the field directly because the node
        // field is lazily instantiated from the Thrift term
        Node n = this.get();
        return n != null ? this.get().hashCode() : 0;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NodeWritable))
            return false;
        return this.compareTo((NodeWritable) other) == 0;
    }
}

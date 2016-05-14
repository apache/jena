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
import org.apache.hadoop.io.WritableUtils;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.util.NodeUtils ;

/**
 * A abstract general purpose writable where the actual class represented is
 * composed of a number of {@link Node} instances
 * <p>
 * The binary encoding of this base implementation is just a variable integer
 * indicating the number of nodes present followed by the binary encodings of
 * the {@link NodeWritable} instances. Derived implementations may wish to
 * override the {@link #readFields(DataInput)} and {@link #write(DataOutput)}
 * methods in order to use more specialised encodings.
 * </p>
 * 
 * @param <T>
 *            Tuple type
 */
public abstract class AbstractNodeTupleWritable<T> implements WritableComparable<AbstractNodeTupleWritable<T>> {

    private T tuple;

    /**
     * Creates a new empty instance
     */
    protected AbstractNodeTupleWritable() {
        this(null);
    }

    /**
     * Creates a new instance with the given value
     * 
     * @param tuple
     *            Tuple value
     */
    protected AbstractNodeTupleWritable(T tuple) {
        this.tuple = tuple;
    }

    /**
     * Gets the tuple
     * 
     * @return Tuple
     */
    public T get() {
        return this.tuple;
    }

    /**
     * Sets the tuple
     * 
     * @param tuple
     *            Tuple
     */
    public void set(T tuple) {
        this.tuple = tuple;
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        // Determine how many nodes
        int size = WritableUtils.readVInt(input);
        Node[] ns = new Node[size];

        NodeWritable nw = new NodeWritable();
        for (int i = 0; i < ns.length; i++) {
            nw.readFields(input);
            ns[i] = nw.get();
        }

        // Load the tuple
        this.tuple = this.createTuple(ns);
    }

    /**
     * Creates the actual tuple type from an array of nodes
     * 
     * @param ns
     *            Nodes
     * @return Tuple
     */
    protected abstract T createTuple(Node[] ns);

    @Override
    public void write(DataOutput output) throws IOException {
        // Determine how many nodes
        Node[] ns = this.createNodes(this.tuple);
        WritableUtils.writeVInt(output, ns.length);

        // Write out nodes
        NodeWritable nw = new NodeWritable();
        for (int i = 0; i < ns.length; i++) {
            nw.set(ns[i]);
            nw.write(output);
        }
    }

    /**
     * Sets the tuple value
     * <p>
     * Intended only for internal use i.e. when a derived implementation
     * overrides {@link #readFields(DataInput)} and needs to set the tuple value
     * directly i.e. when a derived implementation is using a custom encoding
     * scheme
     * </p>
     * 
     * @param tuple
     *            Tuple
     */
    protected final void setInternal(T tuple) {
        this.tuple = tuple;
    }

    /**
     * Converts the actual tuple type into an array of nodes
     * 
     * @param tuple
     *            Tuples
     * @return Nodes
     */
    protected abstract Node[] createNodes(T tuple);

    /**
     * Compares instances node by node
     * <p>
     * Derived implementations may wish to override this and substitute native
     * tuple based comparisons
     * </p>
     * 
     * @param other
     *            Instance to compare with
     */
    @Override
    public int compareTo(AbstractNodeTupleWritable<T> other) {
        Node[] ns = this.createNodes(this.tuple);
        Node[] otherNs = this.createNodes(other.tuple);

        if (ns.length < otherNs.length) {
            return -1;
        } else if (ns.length > otherNs.length) {
            return 1;
        }
        // Compare node by node
        for (int i = 0; i < ns.length; i++) {
            int c = NodeUtils.compareRDFTerms(ns[i], otherNs[i]);
            if (c != 0)
                return c;
        }
        return 0;
    }

    @Override
    public String toString() {
        return this.get().toString();
    }

    @Override
    public int hashCode() {
        return this.get().hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AbstractNodeTupleWritable))
            return false;
        return this.compareTo((AbstractNodeTupleWritable<T>) other) == 0;
    }
}

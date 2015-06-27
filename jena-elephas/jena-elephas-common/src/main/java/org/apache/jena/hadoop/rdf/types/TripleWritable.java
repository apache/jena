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

import org.apache.hadoop.io.WritableComparator;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.types.comparators.SimpleBinaryComparator;
import org.apache.jena.hadoop.rdf.types.converters.ThriftConverter;
import org.apache.jena.riot.thrift.ThriftConvert;
import org.apache.jena.riot.thrift.wire.RDF_Triple;
import org.apache.thrift.TException;

/**
 * A writable triple
 * 
 * 
 * 
 */
public class TripleWritable extends AbstractNodeTupleWritable<Triple> {
    
    static {
        WritableComparator.define(TripleWritable.class, new SimpleBinaryComparator());
    }

    private RDF_Triple triple = new RDF_Triple();

    /**
     * Creates a new instance using the default NTriples node formatter
     */
    public TripleWritable() {
        this(null);
    }

    /**
     * Creates a new instance with a given value that uses a specific node
     * formatter
     * 
     * @param t
     *            Triple
     */
    public TripleWritable(Triple t) {
        super(t);
    }

    /**
     * Creates a new instance from the given input
     * 
     * @param input
     *            Input
     * @return New instance
     * @throws IOException
     */
    public static TripleWritable read(DataInput input) throws IOException {
        TripleWritable t = new TripleWritable();
        t.readFields(input);
        return t;
    }

    @Override
    public void set(Triple tuple) {
        super.set(tuple);
        this.triple.clear();
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        this.triple.clear();
        int tripleLength = input.readInt();
        byte[] buffer = new byte[tripleLength];
        input.readFully(buffer);
        try {
            ThriftConverter.fromBytes(buffer, this.triple);
        } catch (TException e) {
            throw new IOException(e);
        }
        this.setInternal(new Triple(ThriftConvert.convert(this.triple.getS()),
                ThriftConvert.convert(this.triple.getP()), ThriftConvert.convert(this.triple.getO())));
    }

    @Override
    public void write(DataOutput output) throws IOException {
        if (this.get() == null)
            throw new IOException(
                    "Null triples cannot be written using this class, consider using NodeTupleWritable instead");
        
        // May not have yet prepared the Thrift triple
        if (!this.triple.isSetS()) {
            Triple tuple = this.get();
            this.triple.setS(ThriftConvert.convert(tuple.getSubject(), false));
            this.triple.setP(ThriftConvert.convert(tuple.getPredicate(), false));
            this.triple.setO(ThriftConvert.convert(tuple.getObject(), false));
        }

        byte[] buffer;
        try {
            buffer = ThriftConverter.toBytes(this.triple);
        } catch (TException e) {
            throw new IOException(e);
        }
        output.writeInt(buffer.length);
        output.write(buffer);
    }

    @Override
    protected Triple createTuple(Node[] ns) {
        if (ns.length != 3)
            throw new IllegalArgumentException(String.format(
                    "Incorrect number of nodes to form a triple - got %d but expected 3", ns.length));
        return new Triple(ns[0], ns[1], ns[2]);
    }

    @Override
    protected Node[] createNodes(Triple tuple) {
        return new Node[] { tuple.getSubject(), tuple.getPredicate(), tuple.getObject() };
    }
}

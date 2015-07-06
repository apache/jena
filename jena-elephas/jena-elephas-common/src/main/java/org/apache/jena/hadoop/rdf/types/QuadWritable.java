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
import org.apache.jena.hadoop.rdf.types.comparators.SimpleBinaryComparator;
import org.apache.jena.hadoop.rdf.types.converters.ThriftConverter;
import org.apache.jena.riot.thrift.ThriftConvert;
import org.apache.jena.riot.thrift.wire.RDF_Quad;
import org.apache.jena.sparql.core.Quad ;
import org.apache.thrift.TException;

/**
 * A writable quad
 */
public class QuadWritable extends AbstractNodeTupleWritable<Quad> {

    static {
        WritableComparator.define(QuadWritable.class, new SimpleBinaryComparator());
    }

    private RDF_Quad quad = new RDF_Quad();

    /**
     * Creates a new empty instance
     */
    public QuadWritable() {
        this(null);
    }

    /**
     * Creates a new instance with the given value
     * 
     * @param q
     *            Quad
     */
    public QuadWritable(Quad q) {
        super(q);
    }

    /**
     * Creates a new instance from the given input
     * 
     * @param input
     *            Input
     * @return New instance
     * @throws IOException
     */
    public static QuadWritable read(DataInput input) throws IOException {
        QuadWritable q = new QuadWritable();
        q.readFields(input);
        return q;
    }

    @Override
    public void set(Quad tuple) {
        super.set(tuple);
        this.quad.clear();
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        this.quad.clear();
        int tripleLength = input.readInt();
        byte[] buffer = new byte[tripleLength];
        input.readFully(buffer);
        try {
            ThriftConverter.fromBytes(buffer, this.quad);
        } catch (TException e) {
            throw new IOException(e);
        }
        this.setInternal(new Quad(ThriftConvert.convert(this.quad.getG()), ThriftConvert.convert(this.quad.getS()),
                ThriftConvert.convert(this.quad.getP()), ThriftConvert.convert(this.quad.getO())));
    }

    @Override
    public void write(DataOutput output) throws IOException {
        if (this.get() == null)
            throw new IOException(
                    "Null quads cannot be written using this class, consider using NodeTupleWritable instead");

        // May not have yet prepared the Thrift triple
        if (!this.quad.isSetS()) {
            Quad tuple = this.get();
            this.quad.setG(ThriftConvert.convert(tuple.getGraph(), false));
            this.quad.setS(ThriftConvert.convert(tuple.getSubject(), false));
            this.quad.setP(ThriftConvert.convert(tuple.getPredicate(), false));
            this.quad.setO(ThriftConvert.convert(tuple.getObject(), false));
        }

        byte[] buffer;
        try {
            buffer = ThriftConverter.toBytes(this.quad);
        } catch (TException e) {
            throw new IOException(e);
        }
        output.writeInt(buffer.length);
        output.write(buffer);
    }

    @Override
    protected Quad createTuple(Node[] ns) {
        if (ns.length != 4)
            throw new IllegalArgumentException(String.format(
                    "Incorrect number of nodes to form a quad - got %d but expected 4", ns.length));
        return new Quad(ns[0], ns[1], ns[2], ns[3]);
    }

    @Override
    protected Node[] createNodes(Quad tuple) {
        return new Node[] { tuple.getGraph(), tuple.getSubject(), tuple.getPredicate(), tuple.getObject() };
    }

}

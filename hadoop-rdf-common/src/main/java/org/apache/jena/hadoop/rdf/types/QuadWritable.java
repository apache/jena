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
import java.io.IOException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * A writable quad
 * 
 * 
 * 
 */
public class QuadWritable extends AbstractNodeTupleWritable<Quad> {

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
    protected Quad createTuple(Node[] ns) {
        if (ns.length != 4)
            throw new IllegalArgumentException(String.format("Incorrect number of nodes to form a quad - got %d but expected 4",
                    ns.length));
        return new Quad(ns[0], ns[1], ns[2], ns[3]);
    }

    @Override
    protected Node[] createNodes(Quad tuple) {
        Quad q = this.get();
        return new Node[] { q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject() };
    }

}

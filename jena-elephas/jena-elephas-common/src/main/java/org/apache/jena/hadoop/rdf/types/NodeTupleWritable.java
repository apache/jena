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

import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.graph.Node ;

/**
 * A writable RDF tuple
 * <p>
 * Unlike the more specific {@link TripleWritable} and {@link QuadWritable} this
 * class allows for arbitrary length tuples and does not restrict tuples to
 * being of uniform size.
 * </p>
 * 
 * 
 * 
 */
public class NodeTupleWritable extends AbstractNodeTupleWritable<Tuple<Node>> {

    /**
     * Creates a new empty instance
     */
    public NodeTupleWritable() {
        this(null);
    }

    /**
     * Creates a new instance with the given value
     * 
     * @param tuple
     *            Tuple
     */
    public NodeTupleWritable(Tuple<Node> tuple) {
        super(tuple);
    }

    /**
     * Creates a new instance from the given input
     * 
     * @param input
     *            Input
     * @return New instance
     * @throws IOException
     */
    public static NodeTupleWritable read(DataInput input) throws IOException {
        NodeTupleWritable t = new NodeTupleWritable();
        t.readFields(input);
        return t;
    }

    @Override
    protected Tuple<Node> createTuple(Node[] ns) {
        return Tuple.create(ns);
    }

    @Override
    protected Node[] createNodes(Tuple<Node> tuple) {
        return tuple.tuple();
    }
}

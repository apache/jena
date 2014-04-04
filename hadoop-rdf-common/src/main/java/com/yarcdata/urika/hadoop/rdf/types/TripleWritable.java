/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.yarcdata.urika.hadoop.rdf.types;

import java.io.DataInput;
import java.io.IOException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * A writable triple
 * 
 * @author rvesse
 * 
 */
public class TripleWritable extends AbstractNodeTupleWritable<Triple> {

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
    protected Triple createTuple(Node[] ns) {
        if (ns.length != 3)
            throw new IllegalArgumentException(String.format(
                    "Incorrect number of nodes to form a triple - got %d but expected 3", ns.length));
        return new Triple(ns[0], ns[1], ns[2]);
    }

    @Override
    protected Node[] createNodes(Triple tuple) {
        Triple t = this.get();
        return new Node[] { t.getSubject(), t.getPredicate(), t.getObject() };
    }
}

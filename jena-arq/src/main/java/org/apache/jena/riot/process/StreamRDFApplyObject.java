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

package org.apache.jena.riot.process;

import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.Quad;

/**
 * Apply a function to the object of triple/quads.
 */
public class StreamRDFApplyObject extends StreamRDFWrapper {
    private final Function<Node, Node> function;

    public StreamRDFApplyObject(StreamRDF other, Function<Node, Node> function) {
        super(other);
        this.function = function;
    }

    @Override
    public void triple(Triple triple) {
        Node obj = triple.getObject();
        Node obj2 = function.apply(obj);
        if ( obj != obj2 )
            triple = Triple.create(triple.getSubject(), triple.getPredicate(), obj2);
        super.triple(triple);
    }

    @Override
    public void quad(Quad quad) {
        Node obj = quad.getObject();
        Node obj2 = function.apply(obj);
        if ( obj != obj2 )
            quad = Quad.create(quad.getGraph(), quad.getSubject(), quad.getPredicate(), obj2);
        super.quad(quad);
    }
}

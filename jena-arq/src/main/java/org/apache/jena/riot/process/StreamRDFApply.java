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
public class StreamRDFApply extends StreamRDFWrapper {
    private final Function<Node, Node> gFunction;
    private final Function<Node, Node> sFunction;
    private final Function<Node, Node> pFunction;
    private final Function<Node, Node> oFunction;

    public StreamRDFApply(StreamRDF other,
                          Function<Node, Node> sFunction,
                          Function<Node, Node> pFunction,
                          Function<Node, Node> oFunction) {
        this(other, null, sFunction, pFunction, oFunction);
    }

    public StreamRDFApply(StreamRDF other,
                          Function<Node, Node> gFunction,
                          Function<Node, Node> sFunction,
                          Function<Node, Node> pFunction,
                          Function<Node, Node> oFunction) {
        super(other);
        this.gFunction = gFunction;
        this.sFunction = sFunction;
        this.pFunction = pFunction;
        this.oFunction = oFunction;
    }

    private static Node applyFunction(Node node, Function<Node, Node> function) {
        if ( function == null )
            return node;
        Node node2 = function.apply(node);
        if ( node2 == null )
            return node;
        return node2;
    }

    @Override
    public void triple(Triple triple) {
        Node subj = triple.getSubject();
        Node subj2 = applyFunction(subj, sFunction);
        Node pred = triple.getPredicate();
        Node pred2 = applyFunction(pred, pFunction);
        Node obj = triple.getObject();
        Node obj2 = applyFunction(obj, oFunction);
        if ( subj != subj2 || pred != pred2 || obj != obj2 )
            triple = Triple.create(subj2, pred2, obj2);
        super.triple(triple);
    }

    @Override
    public void quad(Quad quad) {
        Node gName = quad.getGraph();
        Node gName2 = applyFunction(gName,  gFunction);
        Node subj = quad.getSubject();
        Node subj2 = applyFunction(subj, sFunction);
        Node pred = quad.getPredicate();
        Node pred2 = applyFunction(subj, pFunction);
        Node obj = quad.getObject();
        Node obj2 = applyFunction(obj, oFunction);
        if ( subj != subj2 || pred != pred2 || obj != obj2 || gName != gName2 )
            quad = Quad.create(gName2, subj2, pred2, obj2);
        super.quad(quad);
    }
}


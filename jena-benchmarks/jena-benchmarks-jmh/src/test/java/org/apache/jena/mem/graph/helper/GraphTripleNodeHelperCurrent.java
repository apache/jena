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
package org.apache.jena.mem.graph.helper;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.GraphMem2Fast;
import org.apache.jena.mem2.GraphMem2Legacy;
import org.apache.jena.mem2.GraphMem2Roaring;
import org.apache.jena.riot.RDFDataMgr;

import java.util.ArrayList;
import java.util.List;

public class GraphTripleNodeHelperCurrent implements GraphTripleNodeHelper<Graph, Triple, Node> {

    @SuppressWarnings("deprecation")
    @Override
    public Graph createGraph(Context.GraphClass graphClass) {
        switch (graphClass) {
            case GraphMem:
                return new org.apache.jena.mem.GraphMem();
            case GraphMem2Fast:
                return new GraphMem2Fast();
            case GraphMem2Legacy:
                return new GraphMem2Legacy();
            case GraphMem2Roaring:
                return new GraphMem2Roaring();
            default:
                throw new IllegalArgumentException("Unknown graph class: " + graphClass);
        }
    }

    @Override
    public List<Triple> readTriples(String graphUri) {
        var list = new ArrayList<Triple>();
        @SuppressWarnings("deprecation")
        var g1 = new org.apache.jena.mem.GraphMem() {
            @Override
            public void add(Triple t) {
                list.add(t);
            }
        };
        RDFDataMgr.read(g1, graphUri);
        return list;
    }

    @Override
    public List<Triple> cloneTriples(List<Triple> triples) {
        var list = new java.util.ArrayList<Triple>(triples.size());
        triples.forEach(triple -> list.add(cloneTriple(triple)));
        return list;
    }

    @Override
    public Triple cloneTriple(Triple triple) {
        return Triple.create(cloneNode(triple.getSubject()), cloneNode(triple.getPredicate()), cloneNode(triple.getObject()));
    }


    @Override
    public Node cloneNode(Node node) {
        if (node.isLiteral()) {
            return NodeFactory.createLiteral(node.getLiteralLexicalForm(), node.getLiteralLanguage(), node.getLiteralDatatype());
        }
        if (node.isURI()) {
            return NodeFactory.createURI(node.getURI());
        }
        if (node.isBlank()) {
            return NodeFactory.createBlankNode(node.getBlankNodeLabel());
        }
        throw new IllegalArgumentException("Only literals, URIs and blank nodes are supported");
    }
}

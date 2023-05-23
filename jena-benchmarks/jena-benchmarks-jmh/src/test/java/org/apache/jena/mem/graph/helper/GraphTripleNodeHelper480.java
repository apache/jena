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

import org.apache.shadedJena480.graph.Graph;
import org.apache.shadedJena480.graph.Node;
import org.apache.shadedJena480.graph.NodeFactory;
import org.apache.shadedJena480.graph.Triple;
import org.apache.shadedJena480.mem.GraphMem;
import org.apache.shadedJena480.riot.RDFDataMgr;

import java.util.ArrayList;
import java.util.List;

public class GraphTripleNodeHelper480 implements GraphTripleNodeHelper<Graph, Triple, Node> {

    @Override
    public Graph createGraph(Context.GraphClass graphClass) {
        switch (graphClass) {
            case GraphMem:
                return new GraphMem();
            default:
                throw new IllegalArgumentException("Unknown graph class: " + graphClass);
        }
    }

    @Override
    public List<Triple> readTriples(String graphUri) {
        var list = new ArrayList<Triple>();
        var g1 = new GraphMem() {
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
        var list = new ArrayList<Triple>(triples.size());
        triples.forEach(triple -> list.add(cloneTriple(triple)));
        return list;
    }

    @Override
    public Triple cloneTriple(Triple triple) {
        return new Triple(cloneNode(triple.getSubject()), cloneNode(triple.getPredicate()), cloneNode(triple.getObject()));
    }


    @Override
    public Node cloneNode(Node node) {
        if(node.isLiteral()) {
            return NodeFactory.createLiteralByValue(node.getLiteralLexicalForm(), node.getLiteralLanguage(), node.getLiteralDatatype());
        }
        if(node.isURI()) {
            return NodeFactory.createURI(node.getURI());
        }
        if(node.isBlank()) {
            return NodeFactory.createBlankNode(node.getBlankNodeLabel());
        }
        throw new IllegalArgumentException("Only literals, URIs and blank nodes are supported");
    }
}

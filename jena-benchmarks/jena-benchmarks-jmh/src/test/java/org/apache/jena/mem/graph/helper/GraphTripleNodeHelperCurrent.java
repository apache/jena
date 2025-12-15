/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */
package org.apache.jena.mem.graph.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMemFast;
import org.apache.jena.mem.GraphMemLegacy;
import org.apache.jena.mem.GraphMemRoaring;
import org.apache.jena.mem.IndexingStrategy;
import org.apache.jena.riot.RDFDataMgr;

public class GraphTripleNodeHelperCurrent implements GraphTripleNodeHelper<Graph, Triple, Node> {

    @SuppressWarnings("deprecation")
    @Override
    public Graph createGraph(Context.GraphClass graphClass) {
        return switch (graphClass) {
            case GraphMemValue -> new org.apache.jena.memvalue.GraphMemValue();
            case GraphMemFast -> new GraphMemFast();
            case GraphMemLegacy -> new GraphMemLegacy();
            case GraphMemRoaringEager -> new GraphMemRoaring(IndexingStrategy.EAGER);
            case GraphMemRoaringLazy -> new GraphMemRoaring(IndexingStrategy.LAZY);
            case GraphMemRoaringLazyParallel -> new GraphMemRoaring(IndexingStrategy.LAZY_PARALLEL);
            case GraphMemRoaringMinimal -> new GraphMemRoaring(IndexingStrategy.MINIMAL);
            case GraphMemRoaringManual -> new GraphMemRoaring(IndexingStrategy.MANUAL);
        };
    }

    @Override
    public List<Triple> readTriples(String graphUri) {
        var list = new ArrayList<Triple>();
        @SuppressWarnings("deprecation")
        var g1 = new org.apache.jena.memvalue.GraphMemValue() {
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

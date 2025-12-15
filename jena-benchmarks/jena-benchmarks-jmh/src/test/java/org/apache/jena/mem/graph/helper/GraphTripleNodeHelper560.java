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

import org.apache.shadedJena560.mem2.GraphMem2Fast;
import org.apache.shadedJena560.mem2.GraphMem2Legacy;
import org.apache.shadedJena560.mem2.GraphMem2Roaring;
import org.apache.shadedJena560.mem2.IndexingStrategy;
import org.apache.shadedJena560.graph.Graph;
import org.apache.shadedJena560.graph.Node;
import org.apache.shadedJena560.graph.NodeFactory;
import org.apache.shadedJena560.graph.Triple;
import org.apache.shadedJena560.mem.GraphMem;
import org.apache.shadedJena560.riot.RDFDataMgr;

public class GraphTripleNodeHelper560 implements GraphTripleNodeHelper<Graph, Triple, Node> {

    @SuppressWarnings("deprecation")
    @Override
    public Graph createGraph(Context.GraphClass graphClass) {
        return switch (graphClass) {
            case GraphMemValue -> new GraphMem();
            case GraphMemFast -> new GraphMem2Fast();
            case GraphMemLegacy -> new GraphMem2Legacy();
            case GraphMemRoaringEager -> new GraphMem2Roaring(IndexingStrategy.EAGER);
            case GraphMemRoaringLazy -> new GraphMem2Roaring(IndexingStrategy.LAZY);
            case GraphMemRoaringLazyParallel -> new GraphMem2Roaring(IndexingStrategy.LAZY_PARALLEL);
            case GraphMemRoaringMinimal -> new GraphMem2Roaring(IndexingStrategy.MINIMAL);
            case GraphMemRoaringManual -> new GraphMem2Roaring(IndexingStrategy.MANUAL);
        };
    }

    @SuppressWarnings("deprecation")
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

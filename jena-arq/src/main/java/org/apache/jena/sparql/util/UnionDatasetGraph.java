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

package org.apache.jena.sparql.util;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.*;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class UnionDatasetGraph extends ViewDatasetGraph {

    public UnionDatasetGraph(DatasetGraph left, DatasetGraph right, Context c) {
        super(left, right, c);
    }

    private Graph union(Function<DatasetGraph, Graph> op) {
        return join(UnionView::new, op);
    }

    <T> Iter<T> fromEach(Function<DatasetGraph, Iterator<T>> op) {
        return join(Iter::concat, op).distinct();
    }

    @Override
    public Graph getDefaultGraph() {
        return union(DatasetGraph::getDefaultGraph);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return union(dsg -> dsg.getGraph(graphNode));
    }

    @Override
    public Graph getUnionGraph() {
        return union(DatasetGraph::getUnionGraph);
    }

    @Override
    public boolean containsGraph(Node graphNode) {
        return either(dsg -> dsg.containsGraph(graphNode));
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return fromEach(DatasetGraph::listGraphNodes);
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        return fromEach(dsg -> dsg.find(g, s, p, o));
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        return fromEach(dsg -> dsg.findNG(g, s, p, o));
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        return either(dsg -> dsg.contains(g, s, p, o));
    }

    @Override
    public boolean isEmpty() {
        return both(DatasetGraph::isEmpty);
    }
    
    static class UnionView extends Union {

        public UnionView(Graph L, Graph R) {
            super(L, R);
        }

        @Override
        public void performAdd(Triple t) {
            throwNoMutationAllowed();
        }

        @Override
        public void performDelete(Triple t) {
            throwNoMutationAllowed();
        }
        
        @Override
        public void clear() {
            throwNoMutationAllowed();
        }
    }
}

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

import static org.apache.jena.atlas.iterator.Iter.iter;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.graph.*;
import org.apache.jena.graph.compose.Intersection;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;

public class IntersectionDatasetGraph extends ViewDatasetGraph {
  
    public IntersectionDatasetGraph(DatasetGraph left, DatasetGraph right, Context c) {
        super(left, right, c);
    }

    Graph intersect(Function<DatasetGraph, Graph> op) {
        return join(IntersectionView::new, op);
    }

    @Override
    public Graph getDefaultGraph() {
        return intersect(DatasetGraph::getDefaultGraph);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return intersect(dsg -> dsg.getGraph(graphNode));
    }

    @Override
    public boolean containsGraph(Node graphNode) {
        return both(dsg -> dsg.containsGraph(graphNode) && !dsg.getGraph(graphNode).isEmpty());
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return iter(getLeft().listGraphNodes())
                .filter(graphName -> !getGraph(graphName).isEmpty())
                .filter(getRight()::containsGraph)
                .filter(graphName -> !getRight().getGraph(graphName).isEmpty());
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        return both(dsg -> dsg.contains(g, s, p, o));
    }

    static class IntersectionView extends Intersection {

        public IntersectionView(Graph L, Graph R) {
            super(L, R);
        }

        @Override
        public void performAdd(Triple t) {
            IntersectionDatasetGraph.throwNoMutationAllowed();
        }

        @Override
        public void performDelete(Triple t) {
            IntersectionDatasetGraph.throwNoMutationAllowed();
        }
        
        @Override
        public void clear() {
            IntersectionDatasetGraph.throwNoMutationAllowed();
        }

        @Override
        protected ExtendedIterator<Triple> _graphBaseFind(Triple s) {
            return L.isEmpty() || R.isEmpty() ? NullIterator.instance() : super._graphBaseFind(s);
        }
    }
}

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

import static org.apache.jena.sparql.core.Quad.ANY;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.graph.*;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;

public class DifferenceDatasetGraph extends ViewDatasetGraph {

	public DifferenceDatasetGraph(DatasetGraph left, DatasetGraph right, Context c) {
		super(left, right, c);
	}
	
	private Graph difference(Function<DatasetGraph, Graph> op) {
	    return join(DifferenceView::new, op);
	}

	@Override
	public Graph getDefaultGraph() {
		return difference(DatasetGraph::getDefaultGraph);
	}

	@Override
	public Graph getGraph(Node graphNode) {
		return difference(dsg -> dsg.getGraph(graphNode));
	}

	@Override
	public boolean containsGraph(Node graphNode) {
		return getLeft().containsGraph(graphNode);
	}

	@Override
	public Iterator<Node> listGraphNodes() {
		return getLeft().listGraphNodes();
	}

	@Override
	public boolean contains(Node g, Node s, Node p, Node o) {
	    return both(dsg -> dsg.contains(g, s, p, o));
	}

	@Override
	public boolean isEmpty() {
		return getLeft().isEmpty() || getLeft() == getRight() || !contains(ANY);
	}

	@Override
	public long size() {
		return getLeft().size();
	}
	
    static class DifferenceView extends Difference {

        public DifferenceView(Graph L, Graph R) {
            super(L, R);
        }

        @Override
        public void performAdd(Triple t) {
            DifferenceDatasetGraph.throwNoMutationAllowed();
        }

        @Override
        public void performDelete(Triple t) {
            DifferenceDatasetGraph.throwNoMutationAllowed();
        }
        
        @Override
        public void clear() {
            DifferenceDatasetGraph.throwNoMutationAllowed();
        }

        @Override
        public ExtendedIterator<Triple> _graphBaseFind(Triple s) {
            return L.isEmpty() ? NullIterator.instance() : super._graphBaseFind(s);
        }
    }
}

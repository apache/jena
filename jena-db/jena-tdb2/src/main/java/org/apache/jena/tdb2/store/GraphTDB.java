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

package org.apache.jena.tdb2.store ;

import java.util.Iterator ;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;
import org.apache.jena.graph.GraphEvents;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.other.GLib ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.core.GraphView ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;

/**
 * General operations for TDB graphs (free-standing graph, default graph and
 * named graphs)
 */
public class GraphTDB extends GraphView implements Closeable, Sync {
    private final DatasetGraphTDB    dataset ;

    public GraphTDB(DatasetGraphTDB dataset, Node graphName) {
        super(dataset, graphName) ;
        this.dataset = dataset ;
    }

    /** get the current TDB dataset graph - changes for transactions */
    public DatasetGraphTDB getDSG() {
        return dataset ;
    }

    /** The NodeTupleTable for this graph */
    public NodeTupleTable getNodeTupleTable() {
        return getDSG().chooseNodeTupleTable(getGraphName()) ;
    }

    @Override
    protected PrefixMapping createPrefixMapping() {
        if ( isDefaultGraph() )
            return getDSG().getPrefixes().getPrefixMapping() ;
        if ( isUnionGraph() )
            return getDSG().getPrefixes().getPrefixMapping() ;
        return getDSG().getPrefixes().getPrefixMapping(getGraphName().getURI()) ;
    }

    @Override
    public final void sync() {
        dataset.sync() ;
    }

    @Override
    final public void close() {
        sync() ;
        // Don't close the GraphBase.
        //super.close() ;
    }

    @Override
    protected ExtendedIterator<Triple> graphUnionFind(Node s, Node p, Node o) {
        Node g = Quad.unionGraph ;
        Iterator<Quad> iterQuads = getDSG().find(g, s, p, o) ;
        Iterator<Triple> iter = GLib.quads2triples(iterQuads) ;
        // Suppress duplicates after projecting to triples.
        // TDB guarantees that duplicates are adjacent.
        // See SolverLib.
        iter = Iter.distinctAdjacent(iter) ;
        return WrappedIterator.createNoRemove(iter) ;
    }

    @Override
    protected final int graphBaseSize() {
        if ( isDefaultGraph() )
            return (int)getNodeTupleTable().size() ;

        Node gn = getGraphName() ;
        boolean unionGraph = isUnionGraph(gn) ;
        gn = unionGraph ? Node.ANY : gn ;
        Iterator<Tuple<NodeId>> iter = getDSG().getQuadTable().getNodeTupleTable().findAsNodeIds(gn, null, null, null) ;
        if ( unionGraph ) {
            iter = Iter.map(iter, project4TupleTo3Tuple) ;
            iter = Iter.distinctAdjacent(iter) ;
        }
        return (int)Iter.count(iter) ;
    }

	private static Function<Tuple<NodeId>, Tuple<NodeId>> project4TupleTo3Tuple = item -> {
		if (item.len() != 4)
			throw new TDBException("Expected a Tuple of 4, got: " + item);
		return TupleFactory.tuple(item.get(1), item.get(2), item.get(3));
	};
	
    @Override
    public void clear() {
        dataset.deleteAny(getGraphName(), Node.ANY, Node.ANY, Node.ANY) ;
        getEventManager().notifyEvent(this, GraphEvents.removeAll) ;
    }

    @Override
    public void remove(Node s, Node p, Node o) {
        if ( getEventManager().listening() ) {
            // Have to do it the hard way so that triple events happen.
            super.remove(s, p, o) ;
            return ;
        }

        dataset.deleteAny(getGraphName(), s, p, o) ;
        // We know no one is listening ...
        // getEventManager().notifyEvent(this, GraphEvents.remove(s, p, o) ) ;
    }
}

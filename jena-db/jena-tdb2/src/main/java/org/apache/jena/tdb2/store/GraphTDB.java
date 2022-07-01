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

package org.apache.jena.tdb2.store;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.dboe.DBOpEnvException;
import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.system.GraphViewStorage;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.other.G;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * General operations for TDB graphs based on GraphViewStorage (TDB2).
 * This is only valid within a transaction.
 * {@link GraphViewSwitchable} is the implementation seen by the application
 * and works across transaction boundaries.
 *
 * @see GraphViewSwitchable
 */
public class GraphTDB extends GraphViewStorage {

    public /*package*/ static GraphTDB tdb_createDefaultGraph(DatasetGraphTDB dsg, StoragePrefixes prefixes)
    { return new GraphTDB(dsg, Quad.defaultGraphNodeGenerated, prefixes); }

    public /*package*/ static GraphTDB tdb_createNamedGraph(DatasetGraphTDB dsg, Node graphIRI, StoragePrefixes prefixes)
    { return new GraphTDB(dsg, graphIRI, prefixes); }

    public /*package*/ static GraphTDB tdb_createUnionGraph(DatasetGraphTDB dsg, StoragePrefixes prefixes)
    { return new GraphTDB(dsg, Quad.unionGraph, prefixes); }

    private final DatasetGraphTDB datasetTDB;

    protected GraphTDB(DatasetGraphTDB dataset, Node graphName, StoragePrefixes prefixes) {
        super(dataset, graphName, prefixes);
        this.datasetTDB = dataset;
    }

    public DatasetGraphTDB getDSG() {
        return datasetTDB;
    }

    /** The NodeTupleTable for this graph */
    public NodeTupleTable getNodeTupleTable() {
        return getDSG().chooseNodeTupleTable(getGraphName());
    }

    // Better ways to execute.

    @Override
    protected final int graphBaseSize() {
        if ( isDefaultGraph() )
            return (int)getNodeTupleTable().size();
        Node gn = getGraphName();
        boolean unionGraph = isUnionGraph(gn);
        gn = unionGraph ? Node.ANY : gn;
        Iterator<Tuple<NodeId>> iter = getDSG().getQuadTable().getNodeTupleTable().findAsNodeIds(gn, null, null, null);
        if ( unionGraph ) {
            iter = Iter.map(iter, project4TupleTo3Tuple);
            iter = Iter.distinctAdjacent(iter);
        }
        return (int)Iter.count(iter);
    }

    private static Function<Tuple<NodeId>, Tuple<NodeId>> project4TupleTo3Tuple = item -> {
        if (item.len() != 4)
            throw new TDBException("Expected a Tuple of 4, got: " + item);
        return TupleFactory.tuple(item.get(1), item.get(2), item.get(3));
    };

    private static Iterator<Triple> projectQuadsToTriples(Node graphNode, Iterator<Quad> iter) {
        // Checking.
        Function<Quad, Triple> f = (q) -> {
            if ( graphNode != null && !q.getGraph().equals(graphNode) )
                throw new DBOpEnvException("projectQuadsToTriples: Quads from unexpected graph (expected=" + graphNode + ", got=" + q.getGraph() + ")");
            return q.asTriple();
        };
        // Without checking
        //Function<Quad, Triple> f = (q) -> q.asTriple();
        return Iter.map(iter, f);
    }

    @Override
    protected ExtendedIterator<Triple> graphUnionFind(Node s, Node p, Node o) {
        Node g = Quad.unionGraph;
        Iterator<Quad> iterQuads = getDSG().find(g, s, p, o);
        Iterator<Triple> iter = G.quads2triples(iterQuads);
        // Suppress duplicates after projecting to triples.
        // TDB guarantees that duplicates are adjacent.
        // See SolverLib.
        iter = Iter.distinctAdjacent(iter);
        return WrappedIterator.createNoRemove(iter);
    }

    @Override
    public final void sync() {}

    @Override
    final public void close() {
        sync();
        // Don't close the GraphBase.
        //super.close();
    }
}

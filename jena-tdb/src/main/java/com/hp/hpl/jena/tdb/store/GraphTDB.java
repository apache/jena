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

package com.hp.hpl.jena.tdb.store ;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.riot.other.GLib ;

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.GraphView ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.graph.BulkUpdateHandlerTDB ;
import com.hp.hpl.jena.tdb.graph.TransactionHandlerTDB ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTable ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

/**
 * General operations for TDB graphs (free-standing graph, default graph and
 * named graphs)
 */
public class GraphTDB extends GraphView implements Closeable, Sync {
    private final BulkUpdateHandler  bulkUpdateHandler  = new BulkUpdateHandlerTDB(this) ;
    private final TransactionHandler transactionHandler = new TransactionHandlerTDB(this) ;

    // Switch this to DatasetGraphTransaction
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

    /**
     * @deprecated Use DatasetGraphTDB.chooseNodeTupleTable
     */
    @Deprecated
    public static NodeTupleTable chooseNodeTupleTable(DatasetGraphTDB dsg, Node graphNode) {
        return dsg.chooseNodeTupleTable(graphNode) ;
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
    public final void performAdd(Triple triple) {
        startUpdate() ;
        super.performAdd(triple) ;
        finishUpdate() ;
    }

    @Override
    public final void performDelete(Triple triple) {
        startUpdate() ;
        super.performDelete(triple) ;
        finishUpdate() ;
    }

    @Override
    public final void sync() {
        dataset.sync() ;
    }

    @Override
    final public void close() {
        sync() ;
        super.close() ;
    }

    protected static ExtendedIterator<Triple> graphBaseFindDft(DatasetGraphTDB dataset, TripleMatch m) {
        Iterator<Quad> iterQuads = dataset.find(Quad.defaultGraphIRI, m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) ;
        if ( iterQuads == null )
            return com.hp.hpl.jena.util.iterator.NullIterator.instance() ;
        // Can't be duplicates - fixed graph node..
        Iterator<Triple> iterTriples = new ProjectQuadsToTriples(Quad.defaultGraphIRI, iterQuads) ;
        return WrappedIterator.createNoRemove(iterTriples) ;
    }

    protected static ExtendedIterator<Triple> graphBaseFindNG(DatasetGraphTDB dataset, Node graphNode, TripleMatch m) {
        Node gn = graphNode ;
        // Explicitly named union graph.
        if ( isUnionGraph(gn) )
            gn = Node.ANY ;

        Iterator<Quad> iter = dataset.getQuadTable().find(gn, m.getMatchSubject(), m.getMatchPredicate(),
                                                          m.getMatchObject()) ;
        if ( iter == null )
            return com.hp.hpl.jena.util.iterator.NullIterator.instance() ;

        Iterator<Triple> iterTriples = new ProjectQuadsToTriples((gn == Node.ANY ? null : gn), iter) ;

        if ( gn == Node.ANY )
            iterTriples = Iter.distinct(iterTriples) ;
        return WrappedIterator.createNoRemove(iterTriples) ;
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

    public void startRead() {
        getDSG().startRead() ;
    }

    public void finishRead() {
        getDSG().finishRead() ;
    }

    public final void startUpdate() {
        getDSG().startUpdate() ;
    }

    public final void finishUpdate() {
        getDSG().finishUpdate() ;
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

    private static Transform<Tuple<NodeId>, Tuple<NodeId>> project4TupleTo3Tuple = new Transform<Tuple<NodeId>, Tuple<NodeId>>() {
                                                                                     @Override
                                                                                     public Tuple<NodeId> convert(Tuple<NodeId> item) {
                                                                                         if ( item.size() != 4 )
                                                                                             throw new TDBException(
                                                                                                                    "Expected a Tuple of 4, got: "
                                                                                                                        + item) ;
                                                                                         return Tuple.createTuple(item.get(1),
                                                                                                                  item.get(2),
                                                                                                                  item.get(3)) ;
                                                                                     }
                                                                                 } ;

    // Convert from Iterator<Quad> to Iterator<Triple>
    static class ProjectQuadsToTriples implements Iterator<Triple> {
        private final Iterator<Quad> iter ;
        private final Node           graphNode ;

        /**
         * Project quads to triples - check the graphNode is as expected if not
         * null
         */
        ProjectQuadsToTriples(Node graphNode, Iterator<Quad> iter) {
            this.graphNode = graphNode ;
            this.iter = iter ;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext() ;
        }

        @Override
        public Triple next() {
            Quad q = iter.next() ;
            if ( graphNode != null && !q.getGraph().equals(graphNode) )
                throw new InternalError("ProjectQuadsToTriples: Quads from unexpected graph (expected=" + graphNode
                                        + ", got=" + q.getGraph() + ")") ;
            return q.asTriple() ;
        }

        @Override
        public void remove() {
            iter.remove() ;
        }
    }

    @Override
    public Capabilities getCapabilities() {
        if ( capabilities == null )
            capabilities = new Capabilities() {
                @Override
                public boolean sizeAccurate() {
                    return true ;
                }

                @Override
                public boolean addAllowed() {
                    return true ;
                }

                @Override
                public boolean addAllowed(boolean every) {
                    return true ;
                }

                @Override
                public boolean deleteAllowed() {
                    return true ;
                }

                @Override
                public boolean deleteAllowed(boolean every) {
                    return true ;
                }

                @Override
                public boolean canBeEmpty() {
                    return true ;
                }

                @Override
                public boolean iteratorRemoveAllowed() {
                    return false ;
                } /* ** */

                @Override
                public boolean findContractSafe() {
                    return true ;
                }

                @Override
                public boolean handlesLiteralTyping() {
                    return false ;
                } /* ** */
            } ;

        return super.getCapabilities() ;
    }

    @Deprecated
    @Override
    public BulkUpdateHandler getBulkUpdateHandler() {
        return bulkUpdateHandler ;
    }

    @Override
    public TransactionHandler getTransactionHandler() {
        return transactionHandler ;
    }

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

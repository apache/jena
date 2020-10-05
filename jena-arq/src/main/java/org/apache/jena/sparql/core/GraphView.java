/**
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

package org.apache.jena.sparql.core;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.graph.Capabilities;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase ;
import org.apache.jena.riot.other.G;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.graph.GraphUnionRead ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;

/** Implement a Graph as a view of the DatasetGraph.
 * 
 *  It maps graph operations to quad operations. 
 *  
 *  {@link GraphUnionRead} provides a union graph that does not assume quads, but loops on graphs.
 *  
 *  @see GraphUnionRead
 */ 

public class GraphView extends GraphBase implements NamedGraph, Sync
{
    // Beware this implements union graph - implementations may wish
    // to do better so see protected method below.
    
    static class GraphViewException extends JenaException
    {
        public GraphViewException()                                  { super(); }
        public GraphViewException(String message)                    { super(message); }
        public GraphViewException(Throwable cause)                   { super(cause) ; }
        public GraphViewException(String message, Throwable cause)   { super(message, cause) ; }
    }
    
    private final DatasetGraph dsg ;
    // null for default graph.
    private final Node gn ;                 
    private final TransactionHandlerView transactionHandler;

    // Factory style.
    public static GraphView createDefaultGraph(DatasetGraph dsg)
    { return new GraphView(dsg, Quad.defaultGraphNodeGenerated) ; }
    
    public static GraphView createNamedGraph(DatasetGraph dsg, Node graphIRI)
    { return new GraphView(dsg, graphIRI) ; }
    
    public static GraphView createUnionGraph(DatasetGraph dsg)
    { return new GraphView(dsg, Quad.unionGraph) ; }

    protected GraphView(DatasetGraph dsg, Node gn) {
        this.dsg = dsg ;
        this.gn = gn ;
        this.transactionHandler = new TransactionHandlerView(dsg);
    }

    /**
     * Return the graph name for this graph in the dataset it is a view of.
     * Returns {@code null} for the default graph.
     */
    @Override
    public Node getGraphName() {
        return (gn == Quad.defaultGraphNodeGenerated) ? null : gn ;
    }

    /** Return the {@link DatasetGraph} we are viewing. */
    public DatasetGraph getDataset() {
        return dsg ;
    }
    
    protected final boolean isDefaultGraph() { return isDefaultGraph(gn) ; }
    protected final boolean isUnionGraph()   { return isUnionGraph(gn) ; }

    protected static final boolean isDefaultGraph(Node gn) { return gn == null || Quad.isDefaultGraph(gn) ; }
    protected static final boolean isUnionGraph(Node gn)   { return Quad.isUnionGraph(gn) ; }
    
    @Override
    protected PrefixMapping createPrefixMapping() {
        // Subclasses should override this but in the absence of anything better ...
        return new PrefixMappingImpl() ;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple m) {
        if ( m == null ) m = Triple.ANY ;
        Node s = m.getMatchSubject() ;
        Node p = m.getMatchPredicate() ;
        Node o = m.getMatchObject() ;
        return graphBaseFind(s, p, o) ;
    }
    
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Node s, Node p, Node o) {
        if ( Quad.isUnionGraph(gn) )
            return graphUnionFind(s, p, o) ;
        Node g = graphNode(gn) ;
        Iterator<Triple> iter = G.quads2triples(dsg.find(g, s, p, o)) ;
        return WrappedIterator.createNoRemove(iter) ;
    }

    private static Node graphNode(Node gn) {
        return ( gn == null ) ? Quad.defaultGraphNodeGenerated : gn ;
    }

    protected ExtendedIterator<Triple> graphUnionFind(Node s, Node p, Node o) {
        Node g = graphNode(gn) ;
        // Implementations may wish to do better so this is separated out.
        // For example, Iter.distinctAdjacent is a lot cheaper than Iter.distinct
        // but assumes things come back in a particular order
        Iterator<Quad> iterQuads = getDataset().find(g, s, p, o) ;
        Iterator<Triple> iter = G.quads2triples(iterQuads) ;
        // Suppress duplicates after projecting to triples.
        iter = Iter.distinct(iter) ;
        return WrappedIterator.createNoRemove(iter) ;
    }
    
    @Override
    public void performAdd( Triple t ) { 
        Node g = graphNode(gn) ;
        if ( Quad.isUnionGraph(g) )
            throw new AddDeniedException("Can't update the union graph of a dataset") ; 
        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;
        dsg.add(g, s, p, o) ;
    }

    @Override
    public void performDelete( Triple t ) {
        Node g = graphNode(gn) ;
        if ( Quad.isUnionGraph(g) )
            throw new DeleteDeniedException("Can't update the union graph of a dataset") ; 
        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;
        dsg.delete(g, s, p, o) ;
    }
    
    @Override
    public void remove(Node s, Node p, Node o) {
        if ( getEventManager().listening() ) {
            // Have to do it the hard way so that triple events happen.
            super.remove(s, p, o);
            return;
        }

        dsg.deleteAny(getGraphName(), s, p, o);
        // We know no one is listening ...
        // getEventManager().notifyEvent(this, GraphEvents.remove(s, p, o) );
    }
    
    /** 
     * Subclasses may wish to provide {@code graphBaseSize} otherwise {@link GraphBase} uses {@code find()}.  
     */
    @Override protected int graphBaseSize() { return super.graphBaseSize(); }

    @Override
    public void sync() {
        SystemARQ.sync(dsg);
    }
    
    @Override
    public TransactionHandler getTransactionHandler() {
        return new TransactionHandlerView(dsg);
    }
    
    @Override
    public Capabilities getCapabilities() { 
        if (capabilities == null) 
            capabilities = new GraphViewCapabilities();
        return capabilities;
    }
    
    protected static class GraphViewCapabilities implements Capabilities {
        @Override
        public boolean sizeAccurate() {
            return true;
        }

        @Override
        public boolean addAllowed() {
            return addAllowed(false);
        }

        @Override
        public boolean addAllowed(boolean every) {
            return true;
        }

        @Override
        public boolean deleteAllowed() {
            return deleteAllowed(false);
        }

        @Override
        public boolean deleteAllowed(boolean every) {
            return true;
        }

        @Override
        public boolean canBeEmpty() {
            return true;
        }

        @Override
        public boolean iteratorRemoveAllowed() {
            //Default for GraphViews is that iterators do not provide remove. 
            return false;
        }

        @Override
        public boolean findContractSafe() {
            return true;
        }

        @Override
        public boolean handlesLiteralTyping() {
            return false;
        }
    }
}

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

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.riot.other.GLib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.graph.impl.GraphBase ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

/** Implement a Graph as a view of the DatasetGraph.
 * 
 *  It maps graph operations to quad operations. 
 */ 

public class GraphView extends GraphBase implements Sync
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
    private final Node gn ;                 // null for default graph.

    // Factory style.
    public static GraphView createDefaultGraph(DatasetGraph dsg)
    { return new GraphView(dsg, Quad.defaultGraphNodeGenerated) ; }
    
    public static GraphView createNamedGraph(DatasetGraph dsg, Node graphIRI)
    { return new GraphView(dsg, graphIRI) ; }
    
    public static GraphView createUnionGraph(DatasetGraph dsg)
    { return new GraphView(dsg, Quad.unionGraph) ; }

    // If inherited.
    protected GraphView(DatasetGraph dsg, Node gn) {
        this.dsg = dsg ;
        this.gn = gn ;
    }

    /**
     * Return the graph name for this graph in the dataset it is a view of.
     * Returns {@code null} for the default graph.
     */
    public Node getGraphName() {
        return (gn == Quad.defaultGraphNodeGenerated) ? null : gn ;
    }

    /** Return the DatasetGraph we are viewing. */
    public DatasetGraph getDataset() {
        return dsg ;
    }
    
    protected final boolean isDefaultGraph() { return isDefaultGraph(gn) ; }
    protected final boolean isUnionGraph()   { return isUnionGraph(gn) ; }

    protected static final boolean isDefaultGraph(Node gn) { return gn == null || Quad.isDefaultGraph(gn) ; }
    protected static final boolean isUnionGraph(Node gn)   { return Quad.isUnionGraph(gn) ; }
    
    @Override
    protected PrefixMapping createPrefixMapping() {
        // TODO Unsatisfactory - need PrefixMap support by DSGs then PrefixMap -> PrefixMapping
        return new PrefixMappingImpl() ;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
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
        Iterator<Triple> iter = GLib.quads2triples(dsg.find(g, s, p, o)) ;
        return WrappedIterator.createNoRemove(iter) ;
    }

    private static Node graphNode(Node gn) {
        return ( gn == null ) ? Quad.defaultGraphNodeGenerated : gn ;
    }

    protected ExtendedIterator<Triple> graphUnionFind(Node s, Node p, Node o) {
        Node g = graphNode(gn) ;
        // Implementations may wish to do better so this is separated out.
        // For example, Iter.distinctAdjacent is a lot cheaper than Iter.distinct
        // but assumes thing come back in a particular order
        Iterator<Quad> iterQuads = getDataset().find(g, s, p, o) ;
        Iterator<Triple> iter = GLib.quads2triples(iterQuads) ;
        // Suppress duplicates after projecting to triples.
        iter = Iter.distinct(iter) ;
        return WrappedIterator.createNoRemove(iter) ;
    }
    
    @Override
    public void performAdd( Triple t ) { 
        Node g = graphNode(gn) ;
        if ( Quad.isUnionGraph(g) )
            throw new GraphViewException("Can't update the default union graph of a dataset") ; 
        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;
        dsg.add(g, s, p, o) ;
    }

    @Override
    public void performDelete( Triple t ) {
        Node g = graphNode(gn) ;
        if ( Quad.isUnionGraph(g) )
            throw new GraphViewException("Can't update the default union graph of a dataset") ; 
        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;
        dsg.delete(g, s, p, o) ;
    }

    @Override
    public void sync() {
        SystemARQ.sync(dsg);
    }
    
    // Need to call GraphBase.close() or sent the protected closed flag.
    //@Override public void close() { super.close() ; }
}

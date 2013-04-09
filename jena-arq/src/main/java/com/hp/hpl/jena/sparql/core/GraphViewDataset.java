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
import org.apache.jena.riot.other.GLib ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.graph.impl.GraphBase ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

/** Implement a Graph as a view of the DatasetGraph.
 * 
 *  - maps graph operations to quad operations. */ 

public class GraphViewDataset extends GraphBase
{
    // Beware this implements union graph - implementation may wish
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

    protected GraphViewDataset(DatasetGraph dsg, Node gn)
    { 
        this.dsg = dsg ; 
        if ( gn == null )
            gn = Quad.defaultGraphNodeGenerated ;
        this.gn = gn ;
    }
    
    public static Graph createDefaultGraph(DatasetGraph dsg)
    { return new GraphViewDataset(dsg, Quad.defaultGraphNodeGenerated) ; }
    
    public static Graph createNamedGraph(DatasetGraph dsg, Node graphIRI)
    { return new GraphViewDataset(dsg, graphIRI) ; }
    
    private final boolean isDefaultGraph() { return Quad.isDefaultGraph(gn) ; }

    @Override
    protected PrefixMapping createPrefixMapping()
    {
        // TODO Unsatisfactory - need PrefixMap support by DSGs then POeefixMap -> PrefixMapping
        return new PrefixMappingImpl() ; 
    }

//    private Graph baseGraph()
//    {
//        // TODO Be able to by pass already wrapped DSGs.
//        if ( isDefaultGraph() ) 
//            return dsg.getBase().getDefaultGraph() ;
//        else
//            return dsg.getBase().getGraph(gn) ;
//    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
    {
        if ( m == null ) m = Triple.ANY ;
        Node s = m.getMatchSubject() ;
        Node p = m.getMatchPredicate() ;
        Node o = m.getMatchObject() ;
        return graphBaseFind(s, p, o) ;
    }
    
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Node s, Node p, Node o)
    {
        Iterator<Triple> iter = GLib.quads2triples(dsg.find(gn, s, p, o)) ;
        if ( Quad.isUnionGraph(gn) )
            return graphUnionFind(s, p, o) ;
        return WrappedIterator.createNoRemove(iter) ;
    }

    protected ExtendedIterator<Triple> graphUnionFind(Node s, Node p, Node o)
    {
        // Implementation may wish to do better so this is separated out.
        Iterator<Triple> iter = GLib.quads2triples(dsg.find(gn, s, p, o)) ;
        // Suppress duplicates after projecting to triples.
        iter = Iter.distinct(iter) ;
        return WrappedIterator.createNoRemove(iter) ;
    }
    
    
    @Override
    public void performAdd( Triple t )
    { 
        if ( Quad.isUnionGraph(gn) )
            throw new GraphViewException("Can't update the default union graph of a dataset") ; 
        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;
        dsg.add(gn, s, p, o) ;
    }

    @Override
    public void performDelete( Triple t ) 
    {
        if ( Quad.isUnionGraph(gn) )
            throw new GraphViewException("Can't update the default union graph of a dataset") ; 
        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;
        dsg.delete(gn, s, p, o) ;
    }
}


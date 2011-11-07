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

package com.hp.hpl.jena.graph.impl;

import java.util.List;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.RDF;

public class ReificationWrapper implements Reifier
    {
    protected final ReificationStyle style;
    protected final ReificationWrapperGraph graph;
    protected final Graph base;
    
    public ReificationWrapper( Graph graph, ReificationStyle style )
        { 
        this.style = style; 
        this.graph = (ReificationWrapperGraph) graph; 
        this.base = this.graph.getBase(); 
        }
    
    @Override
    public ExtendedIterator<Node> allNodes()
        { // TODO needs constraining for :subject :object etc
        return base.find( Node.ANY, RDF.Nodes.type, RDF.Nodes.Statement ).mapWith( Triple.getSubject );
        }
    
    @Override
    public ExtendedIterator<Node> allNodes( Triple t )
        { throw new BrokenException( "this reifier operation" ); }
    
    @Override
    public void close()
        { /* nothing to do */ }
    
    @Override
    public ExtendedIterator<Triple>find( TripleMatch m )
        { return base.find( m ).filterKeep( isReificationTriple ); }
    
    protected static final Filter<Triple> isReificationTriple = new Filter<Triple>()
        {
        @Override public boolean accept( Triple o )
            { return isReificationTriple( o ); }  
        };
    
    @Override
    public ExtendedIterator<Triple> findEither( TripleMatch m, boolean showHidden )
        { return showHidden == style.conceals() ? find( m ) : Triple.None; }
    
    @Override
    public ExtendedIterator<Triple> findExposed( TripleMatch m )
        { return find( m ); }
    
    @Override
    public Graph getParentGraph()
        { return graph; }
    
    @Override
    public ReificationStyle getStyle()
        { return style; }
    
    @Override
    public boolean handledAdd( Triple t )
        {
        base.add( t );
        return isReificationTriple( t );
        }
    
    @Override
    public boolean handledRemove( Triple t )
        { throw new BrokenException( "this reifier operation" ); }
    
    @Override
    public boolean hasTriple( Node n )
        { return getTriple( n ) != null; }
    
    @Override
    public Node reifyAs( Node n, Triple t )
        {
        Triple already = getTriple( n );
        if (already == null)
            {
            checkQuadElementFree( n, RDF.Nodes.subject, t.getSubject() );
            checkQuadElementFree( n, RDF.Nodes.predicate, t.getPredicate() );
            checkQuadElementFree( n, RDF.Nodes.object, t.getObject() );
            SimpleReifier.graphAddQuad( graph, n, t );
            }
        else if (!t.equals( already ))
            throw new AlreadyReifiedException( n );
        return n;
        }
    
    private void checkQuadElementFree( Node n, Node predicate, Node object )
        {
        List<Node> L = base.find( n, predicate, Node.ANY ).mapWith( Triple.getObject ).toList();
        if (L.size() == 0) return;
        if (L.size() == 1 && L.get( 0 ).equals( object )) return;
        throw new CannotReifyException( n );
        }
    
    @Override
    public void remove( Node n, Triple t )
        { // TODO fix to ensure only works on complete reifications
        base.delete(  Triple.create( n, RDF.Nodes.subject, t.getSubject() ) );
        base.delete(  Triple.create( n, RDF.Nodes.predicate, t.getPredicate() ) );
        base.delete(  Triple.create( n, RDF.Nodes.object, t.getObject() ) );
        base.delete(  Triple.create( n, RDF.Nodes.type, RDF.Nodes.Statement ) );
        }
    
    @Override
    public void remove( Triple t )
        { throw new BrokenException( "this reifier operation" ); }
    
    @Override
    public int size()
        { return style.conceals() ? 0: countQuadlets(); }
    
    int count( ExtendedIterator<?> find )
        { 
        int result = 0;
        while (find.hasNext()) { result += 1; find.next(); }
        return result;
        }
    
    int countConcealed()
        { return style.conceals() ? countQuadlets() : 0; }

    private int countQuadlets()
        {
        return 
            count(  base.find( Node.ANY, RDF.Nodes.subject, Node.ANY ) )
            + count( base.find( Node.ANY, RDF.Nodes.predicate, Node.ANY ) )
            + count( base.find( Node.ANY, RDF.Nodes.object, Node.ANY ) )
            + count( base.find( Node.ANY, RDF.Nodes.type, RDF.Nodes.Statement ) )
            ;
        }
    
    @Override
    public boolean hasTriple( Triple t )
        { // CHECK: there's one match AND it matches the triple t.
        Node X = Query.X,  S = Query.S, P = Query.P, O = Query.O;
        Query q = quadsQuery( Query.X );
        List<Domain> bindings = base.queryHandler().prepareBindings( q, new Node[] {X, S, P, O} ).executeBindings().toList();
        return bindings.size() == 1 && t.equals( tripleFromRSPO( bindings.get( 0 ) ) );
        }
    
    private Triple tripleFromRSPO( Domain domain )
        { 
        return Triple.create
            ( domain.getElement(1), domain.getElement(2), domain.getElement(3) );
        }
    
    @Override
    public Triple getTriple( Node n )
        {
        Node S = Query.S, P = Query.P, O = Query.O;
        Query q = quadsQuery( n );
        List<Domain> bindings = base.queryHandler().prepareBindings( q, new Node[] {S, P, O} ).executeBindings().toList();
        return bindings.size() == 1 ? tripleFromSPO( bindings.get(0) ) : null;
        }
    
    private static Query quadsQuery( Node subject )
        {
        return new Query()
            .addMatch( subject, RDF.Nodes.subject, Query.S )
            .addMatch( subject, RDF.Nodes.predicate, Query.P )
            .addMatch( subject, RDF.Nodes.object, Query.O )
            .addMatch( subject, RDF.Nodes.type, RDF.Nodes.Statement )
            ;
        }
    
    private Triple tripleFromSPO( Domain d )
        { return Triple.create( d.getElement(0), d.getElement(1), d.getElement(2) ); }
    
    private static boolean isReificationTriple( Triple t )
        {
        return 
            Reifier.Util.isReificationPredicate( t.getPredicate() ) 
            || Reifier.Util.isReificationType( t.getPredicate(), t.getObject() )
            ;
        }
    }

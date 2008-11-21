/*
 	(c) Copyright 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: ReificationWrapper.java,v 1.1 2008-11-21 15:28:18 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import java.util.List;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.TestReificationWrapper;
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
    
    static final Map1 getSubject = new Map1() 
        {
        public Object map1( Object t ) { return ((Triple) t).getSubject(); }
        };
    
    static final Map1 getObject = new Map1() 
        {
        public Object map1( Object t ) { return ((Triple) t).getObject(); }
        };
    
    public ExtendedIterator allNodes()
        { // TODO needs constraining for :subject :object etc
        return base.find( Node.ANY, RDF.Nodes.type, RDF.Nodes.Statement ).mapWith( getSubject );
        }
    
    public ExtendedIterator allNodes( Triple t )
        { throw new BrokenException( "this reifier operation" ); }
    
    public void close()
        { /* nothing to do */ }
    
    public ExtendedIterator find( TripleMatch m )
        { return base.find( m ).filterKeep( isReificationTriple ); }
    
    protected static final Filter isReificationTriple = new Filter()
        {
        public boolean accept( Object o )
            { return isReificationTriple( (Triple) o ); }  
        };
    
    public ExtendedIterator findEither( TripleMatch m, boolean showHidden )
        { return showHidden == style.conceals() ? find( m ) : NullIterator.instance; }
    
    public ExtendedIterator findExposed( TripleMatch m )
        {
        return find( m );
        }
    
    public Graph getParentGraph()
        { return graph; }
    
    public ReificationStyle getStyle()
        { return style; }
    
    public boolean handledAdd( Triple t )
        {
        base.add( t );
        return isReificationTriple( t );
        }
    
    public boolean handledRemove( Triple t )
        { throw new BrokenException( "this reifier operation" ); }
    
    public boolean hasTriple( Node n )
        { return getTriple( n ) != null; }
    
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
        List L = base.find( n, predicate, Node.ANY ).mapWith( getObject ).toList();
        if (L.size() == 0) return;
        if (L.size() == 1 && L.get( 0 ).equals( object )) return;
        throw new CannotReifyException( n );
        }
    
    public void remove( Node n, Triple t )
        { // TODO fix to ensure only works on complete reifications
        base.delete(  Triple.create( n, RDF.Nodes.subject, t.getSubject() ) );
        base.delete(  Triple.create( n, RDF.Nodes.predicate, t.getPredicate() ) );
        base.delete(  Triple.create( n, RDF.Nodes.object, t.getObject() ) );
        base.delete(  Triple.create( n, RDF.Nodes.type, RDF.Nodes.Statement ) );
        }
    
    public void remove( Triple t )
        { throw new BrokenException( "this reifier operation" ); }
    
    public int size()
        { return style.conceals() ? 0: count( findQuadlets() ); }
    
    int count( ExtendedIterator find )
        { 
        int result = 0;
        while (find.hasNext()) { result += 1; find.next(); }
        return result;
        }
    
    int countConcealed()
        { return style.conceals() ? count( findQuadlets() ) : 0; }
    
    ExtendedIterator findQuadlets()
        { 
        return
            base.find( Node.ANY, RDF.Nodes.subject, Node.ANY )
            .andThen( base.find( Node.ANY, RDF.Nodes.predicate, Node.ANY ) )
            .andThen( base.find( Node.ANY, RDF.Nodes.object, Node.ANY ) )
            .andThen( base.find( Node.ANY, RDF.Nodes.type, RDF.Nodes.Statement ) )
            ;
        }
    
    public boolean hasTriple( Triple t )
        { // CHECK: there's one match AND it matches the triple t.
        Node R = TestReificationWrapper.node( "?r" ),  S = TestReificationWrapper.node( "?s" ), P = TestReificationWrapper.node( "?p" ), O = TestReificationWrapper.node( "?o" );
        Query q = new Query()
            .addMatch( R, RDF.Nodes.subject, S )
            .addMatch( R, RDF.Nodes.predicate, P )
            .addMatch( R, RDF.Nodes.object, O );
        List bindings = base.queryHandler().prepareBindings( q, new Node[] {R, S, P, O} ).executeBindings().toList();
        return bindings.size() == 1 && t.equals( tripleFrom( (Domain) bindings.get( 0 ) ) );
        }
    
    private Triple tripleFrom( Domain domain )
        { 
        return Triple.create
            ( (Node) domain.get(1), (Node) domain.get(2), (Node) domain.get(3) );
        }
    
    public Triple getTriple( Node n )
        {
        Node S = TestReificationWrapper.node( "?s" ), P = TestReificationWrapper.node( "?p" ), O = TestReificationWrapper.node( "?o" );
        Query q = new Query()
            .addMatch( n, RDF.Nodes.subject, S )
            .addMatch( n, RDF.Nodes.predicate, P )
            .addMatch( n, RDF.Nodes.object, O )
            .addMatch( n, RDF.Nodes.type, RDF.Nodes.Statement );
        List bindings = base.queryHandler().prepareBindings( q, new Node[] {S, P, O} ).executeBindings().toList();
        return bindings.size() == 1 ? triple( (Domain) bindings.get(0) ) : null;
        }
    
    private Triple triple( Domain d )
        { return Triple.create( d.getElement( 0 ), d.getElement( 1 ), d.getElement( 2 ) ); }
    
    private static boolean isReificationTriple( Triple t )
        {
        return 
            Reifier.Util.isReificationPredicate( t.getPredicate() ) 
            || Reifier.Util.isReificationType( t.getPredicate(), t.getObject() )
            ;
        }
    }

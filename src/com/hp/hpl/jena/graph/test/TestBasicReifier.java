/*
 	(c) Copyright 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestBasicReifier.java,v 1.1 2008-11-06 10:57:30 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestBasicReifier extends AbstractTestReifier
    {
    protected final Class graphClass;
    protected final ReificationStyle style;
    
    public TestBasicReifier( Class graphClass, String name, ReificationStyle style ) 
        {
        super( name );
        this.graphClass = graphClass;
        this.style = style;
        }
        
    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite();
        result.addTest( MetaTestGraph.suite( TestBasicReifier.class, BasicReifierGraph.class ) );
        return result; 
        }   

    public Graph getGraph()
        { return getGraph( style );  }

    public Graph getGraph( ReificationStyle style )
        { return new BasicReifierGraph( new GraphMem( Standard ), style );  }
    
    public static final class BasicReifierGraph extends WrappedGraph
        {
        protected final ReificationStyle style;
        
        public BasicReifierGraph( Graph base, ReificationStyle style )
            {
            super( base );
            this.style = style;  
            this.reifier = new BasicReifier( this, style );
            }
        
        public Graph getBase()
            { return base; }
        
        public void forceAddTriple( Triple t )
            { base.add( t ); }
        
        public void forceDeleteTriple( Triple t )
            { base.delete( t ); }
        
        public int size()  
            { return base.size() - reifier.size(); }
        }

    public static class BasicReifier implements Reifier
        {
        protected final ReificationStyle style;
        protected final BasicReifierGraph graph;
        
        public BasicReifier( Graph graph, ReificationStyle style )
            { this.style = style; this.graph = (BasicReifierGraph) graph; }

        public ExtendedIterator allNodes()
            { throw new NotImplementedException(); }

        public ExtendedIterator allNodes( Triple t )
            { throw new NotImplementedException(); }

        public void close()
            { /* nothing to do */ }

        public ExtendedIterator find( TripleMatch m )
            {
            return style.conceals() 
                ? NullIterator.instance
                : graph.getBase().find( m ).filterKeep( isReificationTriple )
                ;
            }
        
        private static final Filter isReificationTriple = new Filter()
            {
            public boolean accept( Object o )
                { return isReificationTriple( (Triple) o ); }  
            };

        public ExtendedIterator findEither( TripleMatch m, boolean showHidden )
            { throw new NotImplementedException(); }

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
            graph.forceAddTriple( t );
            return isReificationTriple( t );
            }

        public boolean handledRemove( Triple t )
            { throw new NotImplementedException(); }

        public boolean hasTriple( Node n )
            { return getTriple( n ) != null; }

        public Node reifyAs( Node n, Triple t )
            {
            SimpleReifier.graphAddQuad( graph, n, t );
            return n;
            }

        public void remove( Node n, Triple t )
            { throw new NotImplementedException(); }

        public void remove( Triple t )
            { throw new NotImplementedException(); }

        public int size()
            { return style.conceals() ? count( find() ) : 0; }

        private int count( ExtendedIterator find )
            { 
            int result = 0;
            while (find.hasNext()) { result += 1; find.next(); }
            return result;
            }

        private ExtendedIterator find()
            { 
            return
                graph.find( Node.ANY, RDF.Nodes.subject, Node.ANY )
                .andThen( graph.find( Node.ANY, RDF.Nodes.predicate, Node.ANY ) )
                .andThen( graph.find( Node.ANY, RDF.Nodes.object, Node.ANY ) )
                .andThen( graph.find( Node.ANY, RDF.Nodes.type, RDF.Nodes.Statement ) )
                ;
            }

        public boolean hasTriple( Triple t )
            { // CHECK: there's one match AND it matches the triple t.
            Node R = node( "?r" ),  S = node( "?s" ), P = node( "?p" ), O = node( "?o" );
            Query q = new Query()
                .addMatch( R, RDF.Nodes.subject, S )
                .addMatch( R, RDF.Nodes.predicate, P )
                .addMatch( R, RDF.Nodes.object, O );
            List bindings = graph.queryHandler().prepareBindings( q, new Node[] {R, S, P, O} ).executeBindings().toList();
            return bindings.size() == 1 && t.equals( tripleFrom( (Domain) bindings.get( 0 ) ) );
            }

        private Triple tripleFrom( Domain domain )
            { 
            return Triple.create
                ( (Node) domain.get(1), (Node) domain.get(2), (Node) domain.get(3) );
            }

        public Triple getTriple( Node n )
            {
            Node S = node( "?s" ), P = node( "?p" ), O = node( "?o" );
            Query q = new Query()
                .addMatch( n, RDF.Nodes.subject, S )
                .addMatch( n, RDF.Nodes.predicate, P )
                .addMatch( n, RDF.Nodes.object, O )
                .addMatch( n, RDF.Nodes.type, RDF.Nodes.Statement );
            List bindings = graph.queryHandler().prepareBindings( q, new Node[] {S, P, O} ).executeBindings().toList();
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
    }


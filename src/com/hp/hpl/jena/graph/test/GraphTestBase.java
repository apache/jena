/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]ispo
  $Id: GraphTestBase.java,v 1.16 2004-06-25 06:13:41 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

/**
    An extension of JenaTestBase (which see) with Graph-specific methods.
	@author kers
*/

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.test.*;

import java.lang.reflect.Constructor;
import java.util.*;

public class GraphTestBase extends JenaTestBase
    {
    public GraphTestBase( String name )
    	{ super( name ); }
    	
    public static Node node( String x )
        { return Node.create( x ); }
        
    public static Model modelFor( Graph g )
        { return ModelFactory.createModelForGraph( g ); }
        
    public static Set iteratorToSet( Iterator L )
        { return GraphUtil.iteratorToSet( L ); }

    public static List iteratorToList( Iterator it )
        { return GraphUtil.iteratorToList( it ); }
                
    public Set nodeSet( String nodes )
        {
        HashSet result = new HashSet();
        StringTokenizer st = new StringTokenizer( nodes );
        while (st.hasMoreTokens()) result.add( node( st.nextToken() ) );
        return result;
        }
        
    public Set arrayToSet( Object [] elements )
        { return new HashSet( Arrays.asList( elements ) ); }
                
    public static Triple triple( String fact )
        { return Triple.create( fact ); }
    
    public static Triple triple( PrefixMapping pm, String fact )
        { return Triple.create( pm, fact ); }
        
    public static Triple [] tripleArray( String facts )
        {
        ArrayList al = new ArrayList();
        StringTokenizer semis = new StringTokenizer( facts, ";" );
        while (semis.hasMoreTokens()) al.add( triple( semis.nextToken() ) );   
        return (Triple []) al.toArray( new Triple [al.size()] );
        }
        
    public static Node [] nodes( String items )
        {
        ArrayList nl = new ArrayList();
        StringTokenizer nodes = new StringTokenizer( items );
        while (nodes.hasMoreTokens()) nl.add( node( nodes.nextToken() ) );   
        return (Node []) nl.toArray( new Node [nl.size()] );
        }
        
    public static Graph graphAdd( Graph g, String s )
        {
        StringTokenizer semis = new StringTokenizer( s, ";" );
        while (semis.hasMoreTokens()) g.add( triple( PrefixMapping.Extended, semis.nextToken() ) );     
        return g;
        }
        
    public static Graph newGraph()
        {
        Graph result = new GraphMem();
        result.getPrefixMapping().setNsPrefixes( PrefixMapping.Extended );
        return result;
        }
    
    public static Graph graphWith( String s )
        {
        return graphAdd( newGraph(), s );
        }
        
    public static void assertEqualsTemplate( String title, Graph g, String template )
        {
        // assertTrue( title, g.isIsomorphicWith( graphWith( template ) ) );
        assertIsomorphic( title, graphWith( template ), g );
        }
                
    public static void assertIsomorphic( String title, Graph expected, Graph got )
        {
        if (!expected.isIsomorphicWith( got ))
            {
            Map map = new HashMap();
            fail( title + ": wanted " + nice( expected, map ) + "\nbut got " + nice( got, map ) );
            }
        }
    
    protected static String nice( Graph g, Map bnodes )
        {
        StringBuffer b = new StringBuffer( g.size() * 100 );
        ExtendedIterator it = GraphUtil.findAll( g );
        while (it.hasNext()) niceTriple( b, bnodes, (Triple) it.next() );
        return b.toString();
        }
    
    protected static void niceTriple( StringBuffer b, Map bnodes, Triple t )
        {
        b.append( "\n    " );
        appendNode( b, bnodes, t.getSubject() );
        appendNode( b, bnodes, t.getPredicate() );
        appendNode( b, bnodes, t.getObject() );
        }

    static int bnc = 1000;
    
    protected static void appendNode( StringBuffer b, Map bnodes, Node n )
        {
        b.append( ' ' );
        if (n.isBlank())
            {
            Object already = bnodes.get( n );
            if (already == null) bnodes.put( n, already = "_b" + bnc++ );
            b.append( already );
            }
        else
            b.append( n.toString( PrefixMapping.Extended ) );
        }
            
    public static void assertIsomorphic( Graph expected, Graph got )
        { assertIsomorphic( "graphs must be isomorphic", expected, got ); }

    public static void assertContains( String name, String s, Graph g )
        {
        assertTrue( name + " must contain " + s, g.contains( triple( s ) ) );
        }
        
    public static void assertContainsAll( String name, Graph g, String s )
        {
        StringTokenizer semis = new StringTokenizer( s, ";" );
        while (semis.hasMoreTokens()) assertContains( name, semis.nextToken(), g );       
        }
        
    public void assertOmits( String name, Graph g, String s )
        {
        assertFalse( name + " must not contain " + s, g.contains( triple( s ) ) );
        }
        
    public void assertOmitsAll( String name, Graph g, String s )
        {
        StringTokenizer semis = new StringTokenizer( s, ";" );
        while (semis.hasMoreTokens()) assertOmits( name, g, semis.nextToken() );     
        }
        
    public static boolean contains( Graph g, String fact )
        {
        return g.contains( triple( fact ) ); 
        }
      
    public void testContains( Graph g, Triple [] triples )
        { 
        for (int i = 0; i < triples.length; i += 1) 
            assertTrue( "contains " + triples[i], g.contains( triples[i] ) ); 
        }

    public void testContains( Graph g, List triples )
        {
        for (int i = 0; i < triples.size(); i += 1)
             assertTrue( g.contains( (Triple) triples.get(i) ) );
        }

    public void testContains( Graph g, Iterator it )
        { while (it.hasNext()) assertTrue( g.contains( (Triple) it.next() ) ); }

    public void testContains( Graph g, Graph other )
        { testContains( g, GraphUtil.findAll( other ) ); }
    
    public void testOmits( Graph g, Triple [] triples )
        { for (int i = 0; i < triples.length; i += 1) assertFalse( "", g.contains( triples[i] ) ); }

    public void testOmits( Graph g, List triples )
        {
        for (int i = 0; i < triples.size(); i += 1)
             assertFalse( "", g.contains( (Triple) triples.get(i) ) );
        }

    public void testOmits( Graph g, Iterator it )
        { while (it.hasNext()) assertFalse( "", g.contains( (Triple) it.next() ) ); }

    public void testOmits( Graph g, Graph other )
        { testOmits( g, GraphUtil.findAll( other ) ); }
    
    public static void show( String title, Graph g )
        {
        ClosableIterator it = GraphUtil.findAll( g );
        System.out.println( title );
        while (it.hasNext()) 
            {
            Triple t = (Triple) it.next();
            System.out.println( "  " + t.getSubject() + " @" + t.getPredicate() + " " + t.getObject() );
            }
        }

    /**
        Answer an instance of <code>graphClass</code>. If <code>graphClass</code> has
        a constructor that takes a <code>ReificationStyle</code> argument, then that
        constructor is run on <code>style</code> to get the instance. Otherwise, if it has a #
        constructor that takes an argument of <code>wrap</code>'s class before the
        <code>ReificationStyle</code>, that constructor is used; this allows non-static inner
        classes to be used for <code>graphClass</code>, with <code>wrap</code> being
        the outer class instance. If no suitable constructor exists, a JenaException is thrown.
        
        @param wrap the outer class instance if graphClass is an inner class
        @param graphClass a class implementing Graph
        @param style the reification style to use
        @return an instance of graphClass with the given style
        @throws RuntimeException or JenaException if construction fails
     */
    public static Graph getGraph( Object wrap, Class graphClass, ReificationStyle style ) 
        {
        try
            {
            Constructor cons = getConstructor( graphClass, new Class[] {ReificationStyle.class} );
            if (cons != null) return (Graph) cons.newInstance( new Object[] { style } );
            Constructor cons2 = getConstructor( graphClass, new Class [] {wrap.getClass(), ReificationStyle.class} );
            if (cons2 != null) return (Graph) cons2.newInstance( new Object[] { wrap, style } );
            throw new JenaException( "no suitable graph constructor found for " + graphClass );
            }
        catch (RuntimeException e)
            { throw e; }
        catch (Exception e)
            { throw new JenaException( e ); }
        }

        
    }

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

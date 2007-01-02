/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
  [See end of file]ispo
  $Id: GraphTestBase.java,v 1.33 2007-01-02 11:50:07 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.test;

/**
    An extension of JenaTestBase (which see) with Graph-specific methods.
	@author kers
*/

import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.test.*;

import java.lang.reflect.Constructor;
import java.util.*;

public class GraphTestBase extends JenaTestBase
    {
    public GraphTestBase( String name )
    	{ super( name ); }
    	
    /**
        Answer a Node as described by <code>x</code>; a shorthand for
        <code>Node.create(x)</code>, which see.
    */
    public static Node node( String x )
        { return Node.create( x ); }
        
    /**
        Answer a set containing the elements from the iterator <code>it</code>;
        a shorthand for <code>IteratorCollection.iteratorToSet(it)</code>,
        which see.
    */
    public static Set iteratorToSet( Iterator it )
        { return IteratorCollection.iteratorToSet( it ); }
    
    /**
        Answer a list containing the elements from the iterator <code>it</code>,
        in order; a shorthand for <code>IteratorCollection.iteratorToList(it)</code>,
        which see.
    */
    public static List iteratorToList( Iterator it )
        { return IteratorCollection.iteratorToList( it ); }
                
    /**
        Answer a set of the nodes described (as per <code>node()</code>) by
        the space-separated substrings of <code>nodes</code>.
    */
    public Set nodeSet( String nodes )
        {
        Set result = CollectionFactory.createHashedSet();
        StringTokenizer st = new StringTokenizer( nodes );
        while (st.hasMoreTokens()) result.add( node( st.nextToken() ) );
        return result;
        }
    
    /**
        Answer a set of the elements of <code>A</code>.
    */
    public Set arrayToSet( Object [] A )
        { return CollectionFactory.createHashedSet( Arrays.asList( A ) ); }
    
    /**
        Answer a triple described by the three space-separated node descriptions
        in <code>fact</code>; a shorthand for <code>Triple.create(fact)</code>,
        which see.
    */
    public static Triple triple( String fact )
        { return Triple.create( fact ); }
    
    /**
        Answer a triple described by the three space-separated node descriptions
        in <code>fact</code>, using prefix-mappings from <code>pm</code>; a 
        shorthand for <code>Triple.create(pm, fact)</code>, which see.
    */
    public static Triple triple( PrefixMapping pm, String fact )
        { return Triple.create( pm, fact ); }
        
    /**
        Answer an array of triples; each triple is described by one of the
        semi-separated substrings of <code>facts</code>, as per 
        <code>triple</code> with prefix-mapping <code>Extended</code>.
    */
    public static Triple [] tripleArray( String facts )
        {
        ArrayList al = new ArrayList();
        StringTokenizer semis = new StringTokenizer( facts, ";" );
        while (semis.hasMoreTokens()) al.add( triple( PrefixMapping.Extended, semis.nextToken() ) );   
        return (Triple []) al.toArray( new Triple [al.size()] );
        }
    
    /**
        Answer a set of triples where the elements are described by the
        semi-separated substrings of <code>facts</code>, as per 
        <code>triple</code>.
    */
    public static Set tripleSet( String facts )
        {
        Set result = new HashSet();
        StringTokenizer semis = new StringTokenizer( facts, ";" );
        while (semis.hasMoreTokens()) result.add( triple( semis.nextToken() ) );   
        return result;
        }
       
    /**
        Answer a list of nodes, where the nodes are described by the 
        space-separated substrings of <code>items</code> as per
        <code>node()</code>.
    */
    public static List nodeList( String items )
        {
        ArrayList nl = new ArrayList();
        StringTokenizer nodes = new StringTokenizer( items );
        while (nodes.hasMoreTokens()) nl.add( node( nodes.nextToken() ) );   
        return nl;
        }   
    
    /**
        Answer an array of nodes, where the nodes are described by the 
        space-separated substrings of <code>items</code> as per
    */
    public static Node [] nodeArray( String items )
        {
        List nl = nodeList( items );  
        return (Node []) nl.toArray( new Node [nl.size()] );
        } 
    
    /**
        Answer the graph <code>g</code> after adding to it every triple
        encoded in <code>s</code> in the fashion of <code>tripleArray</code>,
        a semi-separated sequence of space-separated node descriptions.
    */
    public static Graph graphAdd( Graph g, String s )
        {
        StringTokenizer semis = new StringTokenizer( s, ";" );
        while (semis.hasMoreTokens()) g.add( triple( PrefixMapping.Extended, semis.nextToken() ) );     
        return g;
        }
    
    /**
        Answer a new memory-based graph with Extended prefixes.
    */
    public static Graph newGraph()
        {
        Graph result = Factory.createGraphMem();
        result.getPrefixMapping().setNsPrefixes( PrefixMapping.Extended );
        return result;
        }
    
    /**
        Answer a new memory-based graph with initial contents as described
        by <code>s</code> in the fashion of <code>graphAdd()</code>.
        Not over-ridable; do not use for abstraction.
    */
    public static Graph graphWith( String s )
        { return graphAdd( newGraph(), s ); }
    
    /**
        Assert that the graph <code>g</code> is isomorphic to the graph 
        described by <code>template</code> in the fashion of 
        <code>graphWith</code>.
    */
    public static void assertEqualsTemplate( String title, Graph g, String template )
        { assertIsomorphic( title, graphWith( template ), g ); }
                
    /**
        Assert that the supplied graph <code>got</code> is isomorphic with the
        the desired graph <code>expected</code>; if not, display a readable
        description of both graphs.
    */
    public static void assertIsomorphic( String title, Graph expected, Graph got )
        {
        if (!expected.isIsomorphicWith( got ))
            {
            Map map = CollectionFactory.createHashedMap();
            fail( title + ": wanted " + nice( expected, map ) + "\nbut got " + nice( got, map ) );
            }
        }
    
    /**
        Answer a string which is a newline-separated list of triples (as
        produced by niceTriple) in the graph <code>g</code>. The map 
        <code>bnodes</code> maps already-seen bnodes to their "nice" strings. 
    */
    public static String nice( Graph g, Map bnodes )
        {
        StringBuffer b = new StringBuffer( g.size() * 100 );
        ExtendedIterator it = GraphUtil.findAll( g );
        while (it.hasNext()) niceTriple( b, bnodes, (Triple) it.next() );
        return b.toString();
        }
    
    /**
        Append to the string buffer <code>b</code> a "nice" representation 
        of the triple <code>t</code> on a new line, using (and updating)
        <code>bnodes</code> to supply "nice" strings for any blank nodes.
    */
    protected static void niceTriple( StringBuffer b, Map bnodes, Triple t )
        {
        b.append( "\n    " );
        appendNode( b, bnodes, t.getSubject() );
        appendNode( b, bnodes, t.getPredicate() );
        appendNode( b, bnodes, t.getObject() );
        }

    /**
        A counter for new bnode strings; it starts at 1000 so as to make
        the bnode strings more uniform (at least for the first 9000 bnodes).
    */
    protected static int bnc = 1000;
    
    /**
        Append to the string buffer <code>b</code> a space followed by the
        "nice" representation of the node <code>n</code>. If <code>n</code>
        is a bnode, re-use any existing string for it from <code>bnodes</code>
        or make a new one of the form <i>_bNNNN</i> with NNNN a new integer.
    */
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
            b.append( nice( n ) );
        }
    
    /**
        Answer the "nice" representation of this node, the string returned by
        <code>n.toString(PrefixMapping.Extended,true)</code>.
    */
    protected static String nice( Node n )
        { return n.toString( PrefixMapping.Extended, true ); }
            
    /**
        Assert that the computed graph <code>got</code> is isomorphic with the
        desired graph <code>expected</code>; if not, fail with a default
        message (and pretty output of the graphs).
    */
    public static void assertIsomorphic( Graph expected, Graph got )
        { assertIsomorphic( "graphs must be isomorphic", expected, got ); }

    /**
        Assert that the graph <code>g</code> must contain the triple described
        by <code>s</code>; if not, fail with pretty output of both graphs
        and a message containing <code>name</code>.
    */
    public static void assertContains( String name, String s, Graph g )
        {
        assertTrue( name + " must contain " + s, g.contains( triple( s ) ) );
        }
    
    /**
        Assert that the graph <code>g</code> contains all the triples described
        by the string <code>s</code; if not, fail with a message containing
        <code>name</code>.
    */
    public static void assertContainsAll( String name, Graph g, String s )
        {
        StringTokenizer semis = new StringTokenizer( s, ";" );
        while (semis.hasMoreTokens()) assertContains( name, semis.nextToken(), g );       
        }
    
    /**
        Assert that the graph <code>g</code> does not contain the triple
        described by <code>s<code>; if it does, fail with a message containing
        <code>name</code>.
    */
    public static void assertOmits( String name, Graph g, String s )
        {
        assertFalse( name + " must not contain " + s, g.contains( triple( s ) ) );
        }
    
    /**
        Assert that the graph <code>g</code> contains none of the triples
        described by <code>s</code> in the usual way; otherwise, fail with
        a message containing <code>name</code>.
    */
    public static void assertOmitsAll( String name, Graph g, String s )
        {
        StringTokenizer semis = new StringTokenizer( s, ";" );
        while (semis.hasMoreTokens()) assertOmits( name, g, semis.nextToken() );     
        }
        
    /**
        Assert that <code>g</code> contains the triple described by 
        <code>fact</code> in the usual way.
    */
    public static boolean contains( Graph g, String fact )
        { return g.contains( triple( fact ) ); }
    
    /**
        Assert that <code>g</code> contains every triple in <code>triples</code>.
    */
    public void testContains( Graph g, Triple [] triples )
        { 
        for (int i = 0; i < triples.length; i += 1) 
            assertTrue( "contains " + triples[i], g.contains( triples[i] ) ); 
        }

    /**
        Assert that <code>g</code> contains every triple in <code>triples</code>.
    */
    public void testContains( Graph g, List triples )
        {
        for (int i = 0; i < triples.size(); i += 1)
             assertTrue( g.contains( (Triple) triples.get(i) ) );
        }

    /**
        Assert that <code>g</code> contains every triple in <code>it</code>.
    */
    public void testContains( Graph g, Iterator it )
        { while (it.hasNext()) assertTrue( g.contains( (Triple) it.next() ) ); }

    /**
        Assert that <code>g</code> contains every triple in <code>other</code>.
    */
    public void testContains( Graph g, Graph other )
        { testContains( g, GraphUtil.findAll( other ) ); }
    
    /**
        Assert that <code>g</code> contains none of the triples in 
        <code>triples</code>.
    */
    public void testOmits( Graph g, Triple [] triples )
        { for (int i = 0; i < triples.length; i += 1) assertFalse( "", g.contains( triples[i] ) ); }
    
    /**
        Assert that <code>g</code> contains none of the triples in 
        <code>triples</code>.
    */
    public void testOmits( Graph g, List triples )
        {
        for (int i = 0; i < triples.size(); i += 1)
             assertFalse( "", g.contains( (Triple) triples.get(i) ) );
        }
    
    /**
        Assert that <code>g</code> contains none of the triples in 
        <code>it</code>.
    */
    public void testOmits( Graph g, Iterator it )
        { while (it.hasNext()) assertFalse( "", g.contains( (Triple) it.next() ) ); }
    
    /**
        Assert that <code>g</code> contains none of the triples in 
        <code>other</code>.
    */
    public void testOmits( Graph g, Graph other )
        { testOmits( g, GraphUtil.findAll( other ) ); }

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

    protected static Graph getReificationTriples( final Reifier r )
        {
        return new GraphBase()
            {
            public ExtendedIterator graphBaseFind( TripleMatch m ) { return r.find( m ); }
            };
        }

        
    }

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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

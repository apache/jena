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

package com.hp.hpl.jena.graph.test;

/**
    An extension of JenaTestBase (which see) with Graph-specific methods.
*/

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor ;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.impl.TestListSyntaxCategories;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.test.JenaTestBase ;
import com.hp.hpl.jena.util.CollectionFactory ;
import com.hp.hpl.jena.util.IteratorCollection ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

public class GraphTestBase extends JenaTestBase
    {
	protected static String getFileName( String fn )
   	{
   		URL u = TestListSyntaxCategories.class.getClassLoader().getResource( fn );
   		if (u == null)
   		{
   			throw new RuntimeException( new FileNotFoundException( fn ));
   		}
   		try {
   			return u.toURI().toString();
   		} catch (URISyntaxException e) {
   			throw new RuntimeException( e );
   		}
   	}
    public GraphTestBase( String name )
    	{ super( name ); }
    	
    /**
        Answer a Node as described by <code>x</code>; a shorthand for
        <code>Node.create(x)</code>, which see.
    */
    public static Node node( String x )
        { return NodeCreateUtils.create( x ); }
        
    /**
        Answer a set containing the elements from the iterator <code>it</code>;
        a shorthand for <code>IteratorCollection.iteratorToSet(it)</code>,
        which see.
    */
    public static <T> Set<T> iteratorToSet( Iterator<? extends T> it )
        { return IteratorCollection.iteratorToSet( it ); }
    
    /**
        Answer a list containing the elements from the iterator <code>it</code>,
        in order; a shorthand for <code>IteratorCollection.iteratorToList(it)</code>,
        which see.
    */
    public static <T> List<T> iteratorToList( Iterator<? extends T> it )
        { return IteratorCollection.iteratorToList( it ); }
                
    /**
        Answer a set of the nodes described (as per <code>node()</code>) by
        the space-separated substrings of <code>nodes</code>.
    */
    public Set<Node> nodeSet( String nodes )
        {
        Set<Node> result = CollectionFactory.createHashedSet();
        StringTokenizer st = new StringTokenizer( nodes );
        while (st.hasMoreTokens()) result.add( node( st.nextToken() ) );
        return result;
        }
    
    /**
        Answer a set of the elements of <code>A</code>.
    */
    public <T> Set<T> arrayToSet( T [] A )
        { return CollectionFactory.createHashedSet( Arrays.asList( A ) ); }
    
    /**
        Answer a triple described by the three space-separated node descriptions
        in <code>fact</code>; a shorthand for <code>Triple.create(fact)</code>,
        which see.
    */
    public static Triple triple( String fact )
        { return NodeCreateUtils.createTriple( fact ); }
    
    /**
        Answer a triple described by the three space-separated node descriptions
        in <code>fact</code>, using prefix-mappings from <code>pm</code>; a 
        shorthand for <code>Triple.create(pm, fact)</code>, which see.
    */
    public static Triple triple( PrefixMapping pm, String fact )
        { return NodeCreateUtils.createTriple( pm, fact ); }
        
    /**
        Answer an array of triples; each triple is described by one of the
        semi-separated substrings of <code>facts</code>, as per 
        <code>triple</code> with prefix-mapping <code>Extended</code>.
    */
    public static Triple [] tripleArray( String facts )
        {
        ArrayList<Triple> al = new ArrayList<>();
        StringTokenizer semis = new StringTokenizer( facts, ";" );
        while (semis.hasMoreTokens()) al.add( triple( PrefixMapping.Extended, semis.nextToken() ) );   
        return al.toArray( new Triple [al.size()] );
        }
    
    /**
        Answer a set of triples where the elements are described by the
        semi-separated substrings of <code>facts</code>, as per 
        <code>triple</code>.
    */
    public static Set<Triple> tripleSet( String facts )
        {
        Set<Triple> result = new HashSet<>();
        StringTokenizer semis = new StringTokenizer( facts, ";" );
        while (semis.hasMoreTokens()) result.add( triple( semis.nextToken() ) );   
        return result;
        }
       
    /**
        Answer a list of nodes, where the nodes are described by the 
        space-separated substrings of <code>items</code> as per
        <code>node()</code>.
    */
    public static List<Node> nodeList( String items )
        {
        ArrayList<Node> nl = new ArrayList<>();
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
        List<Node> nl = nodeList( items );  
        return nl.toArray( new Node [nl.size()] );
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
            Map<Node, Object> map = CollectionFactory.createHashedMap();
            fail( title + ": wanted " + nice( expected, map ) + "\nbut got " + nice( got, map ) );
            }
        }
    
    /**
        Answer a string which is a newline-separated list of triples (as
        produced by niceTriple) in the graph <code>g</code>. The map 
        <code>bnodes</code> maps already-seen bnodes to their "nice" strings. 
    */
    public static String nice( Graph g, Map<Node, Object> bnodes )
        {
        StringBuffer b = new StringBuffer( g.size() * 100 );
        ExtendedIterator<Triple> it = GraphUtil.findAll( g );
        while (it.hasNext()) niceTriple( b, bnodes, it.next() );
        return b.toString();
        }
    
    /**
        Append to the string buffer <code>b</code> a "nice" representation 
        of the triple <code>t</code> on a new line, using (and updating)
        <code>bnodes</code> to supply "nice" strings for any blank nodes.
    */
    protected static void niceTriple( StringBuffer b, Map<Node, Object> bnodes, Triple t )
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
    protected static void appendNode( StringBuffer b, Map<Node, Object> bnodes, Node n )
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
            for ( Triple triple : triples )
            {
                assertTrue( "contains " + triple, g.contains( triple ) );
            }
        }

    /**
        Assert that <code>g</code> contains every triple in <code>triples</code>.
    */
    public void testContains( Graph g, List<Triple> triples )
        {
            for ( Triple triple : triples )
            {
                assertTrue( g.contains( triple ) );
            }
        }

    /**
        Assert that <code>g</code> contains every triple in <code>it</code>.
    */
    public void testContains( Graph g, Iterator<Triple> it )
        { while (it.hasNext()) assertTrue( g.contains( it.next() ) ); }

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
        {
            for ( Triple triple : triples )
            {
                assertFalse( "", g.contains( triple ) );
            }
        }
    
    /**
        Assert that <code>g</code> contains none of the triples in 
        <code>triples</code>.
    */
    public void testOmits( Graph g, List<Triple> triples )
        {
            for ( Triple triple : triples )
            {
                assertFalse( "", g.contains( triple ) );
            }
        }
    
    /**
        Assert that <code>g</code> contains none of the triples in 
        <code>it</code>.
    */
    public void testOmits( Graph g, Iterator<Triple> it )
        { while (it.hasNext()) assertFalse( "", g.contains( it.next() ) ); }
    
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
        @return an instance of graphClass with the given style
        @throws RuntimeException or JenaException if construction fails
     */
    public static Graph getGraph( Object wrap, Class<? extends Graph> graphClass) 
        {
        try
            {
            Constructor<?> cons = getConstructor( graphClass, new Class[] {} );
            if (cons != null) return (Graph) cons.newInstance( new Object[] { } );
            Constructor<?> cons2 = getConstructor( graphClass, new Class [] {wrap.getClass()} );
            if (cons2 != null) return (Graph) cons2.newInstance( new Object[] { wrap} );
            throw new JenaException( "no suitable graph constructor found for " + graphClass );
            }
        catch (RuntimeException e)
            { throw e; }
        catch (Exception e)
            { throw new JenaException( e ); }
        }
    }

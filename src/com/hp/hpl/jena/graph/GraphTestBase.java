/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: GraphTestBase.java,v 1.1.1.1 2002-12-19 19:13:27 bwm Exp $
*/

package com.hp.hpl.jena.graph;

/**
	@author kers
<br>
    A version of TestCase with assorted extra goodies.
*/

import java.util.*;
import java.io.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.mem.*;
import junit.framework.*;
import com.hp.hpl.jena.rdf.model.*;

public class GraphTestBase extends TestCase
    {
    public GraphTestBase( String name )
    	{ super( name ); }
    	
    public static Test suite()
        { return new TestSuite( GraphTestBase.class ); }   

    public void testNodeSet()
        {
        String [] words = {"when", "home", "come", "the", "cows"};
        Set s = nodeSet( "when the cows come home" );
        assertEquals( "set size correct", words.length, s.size() );
        for (int i = 0; i < words.length; i += 1)
            assertTrue( "", s.contains( node( words[i] ) ) );
        }
        
    public static Node node( String x )
        {
        if (x.length() > 0)
            {
            if (x.charAt( 0 ) == '\'')
                return Node.makeLiteral( new LiteralLabel(  x, "en-UK", false ) );
            if (Character.isDigit( x.charAt( 0 ) )) 
                return Node.makeLiteral( new LiteralLabel( x, "nn-NN", false ) );
            if (x.charAt( 0 ) == '_')
                return Node.makeAnon( new AnonId( x ) );
            if (x.charAt( 0 ) == '?')
                return Node.makeVariable( x.substring( 1 ) );
            if (x.charAt( 0 ) == '&')
                return Node.makeURI( "q:" + x.substring( 1 ) );
            }
        return Node.makeURI( x.indexOf( ':' ) < 0 ? "eh/" + x : x );
        }
        
    public Set iteratorToSet( ClosableIterator L )
        {
        HashSet result = new HashSet();
        while (L.hasNext()) result.add( L.next() );        
        return result;
        }
        
    public Set nodeSet( String nodes )
        {
        HashSet result = new HashSet();
        StringTokenizer st = new StringTokenizer( nodes );
        while (st.hasMoreTokens()) result.add( node( st.nextToken() ) );
        return result;
        }
        
    public static void testNodeHelp()
        {
        assertTrue( "node() making URIs", node( "hello" ).isURI() );
        assertTrue( "node() making literals", node( "123" ).isLiteral() );
        assertTrue( "node() making literals", node( "'hello'" ).isLiteral() );
        assertTrue( "node() making blanks", node( "_x" ).isBlank() );
        assertTrue( "node() making variables", node( "?x" ).isVariable() );
        }
        
    public static Triple triple( String fact )
        {
        StringTokenizer st = new StringTokenizer( fact );
        Node sub = node( st.nextToken() );
        Node pred = node( st.nextToken() );
        Node obj = node( st.nextToken() );
        return new Triple( sub, pred, obj );
        }
        
    public static Graph graphAdd( Graph g, String s )
        {
        StringTokenizer semis = new StringTokenizer( s, ";" );
        while (semis.hasMoreTokens()) g.add( triple( semis.nextToken() ) );     
        return g;
        }
        
    public static Graph graphWith( String s )
        {
        return graphAdd( new GraphMem(), s );
        }
        
    public static void assertFalse( String title, boolean b )
        {
        assertTrue( title, !b );
        }
        
    public static void assertDiffer( String title, Object x, Object y )
    	{ assertFalse( title, x.equals( y ) ); }
    	
    public static void printGraph( PrintStream sink, Graph g )
    	{
    	ClosableIterator it = g.find( null, null, null );
    	while (it.hasNext()) sink.println( it.next() );
    	}
        
    public static void assertEqualsTemplate( String title, Graph g, String template )
        {
        // one was: assertTrue( title, new ModelMem( g ) .equals( new ModelMem( graphWith( template ) ) ) );
        assertTrue( title, g.isIsomorphicWith( graphWith( template ) ) );
        }
        
    public static void assertContains( String name, Graph g, String s )
        {
        assertTrue( name + " must contain " + s, g.contains( triple( s ) ) );
        }
        
    public static void assertContainsAll( String name, Graph g, String s )
        {
        StringTokenizer semis = new StringTokenizer( s, ";" );
        while (semis.hasMoreTokens()) assertContains( name, g, semis.nextToken() );       
        }
        
    public static void assertOmits( String name, Graph g, String s )
        {
        assertFalse( name + " must not contain " + s, g.contains( triple( s ) ) );
        }
        
    public static void assertOmitsAll( String name, Graph g, String s )
        {
        StringTokenizer semis = new StringTokenizer( s, ";" );
        while (semis.hasMoreTokens()) assertOmits( name, g, semis.nextToken() );     
        }
        
    public static boolean contains( Graph g, String fact )
        {
        return g.contains( triple( fact ) ); 
        }

    public static void show( String title, Graph g )
        {
        ClosableIterator it = g.find( null, null, null );
        System.out.println( title );
        while (it.hasNext()) 
            {
            Triple t = (Triple) it.next();
            System.out.println( "  " + t.getSubject() + " @" + t.getPredicate() + " " + t.getObject() );
            }
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2002
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

/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: GraphTestBase.java,v 1.13 2003-05-05 11:09:03 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

/**
	@author kers
<br>
    A version of TestCase with assorted extra goodies.
*/

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.test.*;

import java.util.*;
import java.io.*;

import junit.framework.*;

public class GraphTestBase extends JenaTestBase
    {
    public GraphTestBase( String name )
    	{ super( name ); }
    	
    public static TestSuite suite()
        { return new TestSuite( GraphTestBase.class ); }   

    static final String RDFprefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
    public static Node node( String x )
        {
        if (x.equals( "" ))
            throw new RuntimeException( "GraphTestBase::node does not accept an empty string as argument" );
        char first = x.charAt( 0 );
        if (first == '\'')
            return Node.createLiteral( new LiteralLabel(  x, "en-UK", false ) );
        if (Character.isDigit( first )) 
            return Node.createLiteral( new LiteralLabel( x, "nn-NN", false ) );
        if (first == '_')
            return Node.createAnon( new AnonId( x ) );
        if (first == '?')
            return Node.createVariable( x.substring( 1 ) );
        if (first == '&')
            return Node.createURI( "q:" + x.substring( 1 ) );        
        int colon = x.indexOf( ':' );
        if (colon < 0)
            return Node.createURI( "eh:" + x );
        String prefix = x.substring( 0, colon );
        if (prefix.equals( "rdf") )
            return Node.createURI( RDFprefix + x.substring( colon + 1 ) );
        return Node.createURI( x );
        }
        
    public static Model modelFor( Graph g )
        { return ModelFactory.createModelForGraph( g ); }
        
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
        
    public void testNodeHelp()
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
        
    public static Triple [] tripleArray( String facts )
        {
        ArrayList al = new ArrayList();
        StringTokenizer semis = new StringTokenizer( facts, ";" );
        while (semis.hasMoreTokens()) al.add( triple( semis.nextToken() ) );   
        return (Triple []) al.toArray( new Triple [al.size()] );
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
        
    public static void printGraph( PrintStream sink, Graph g )
    	{
    	ClosableIterator it = g.find( null, null, null );
    	while (it.hasNext()) sink.println( it.next() );
    	}
        
    public static void assertEqualsTemplate( String title, Graph g, String template )
        {
        assertTrue( title, g.isIsomorphicWith( graphWith( template ) ) );
        }
        
    public void assertEquals( String name, Graph wanted, Graph obtained )
        {
        Model mWanted = modelFor( wanted ), mObtained = modelFor( obtained );
        assertTrue( name + ": wanted " + wanted + " got " + obtained, mWanted.isIsomorphicWith( mObtained ) );
        }
    
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
        { for (int i = 0; i < triples.length; i += 1) assertTrue( g.contains( triples[i] ) ); }

    public void testContains( Graph g, List triples )
        {
        for (int i = 0; i < triples.size(); i += 1)
             assertTrue( g.contains( (Triple) triples.get(i) ) );
        }

    public void testContains( Graph g, Iterator it )
        { while (it.hasNext()) assertTrue( g.contains( (Triple) it.next() ) ); }

    public void testContains( Graph g, Graph other )
        { testContains( g, other.find( null, null, null ) ); }
    
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
        { testOmits( g, other.find( null, null, null ) ); }
    
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

    /**
        create a temporary file that will be deleted on exit, and do something
        sensible with any IO exceptions - namely, throw them up wrapped in
        a JenaException.
    
        @param prefix the prefix for File.createTempFile
        @param suffix the suffix for File.createTempFile
        @return the temporary File
    */
    public static  File tempFileName( String prefix, String suffix )
        {
        File result = new File( getTempDirectory(), prefix + randomNumber() + suffix );
        if (result.exists()) return tempFileName( prefix, suffix );
        result.deleteOnExit();
        return result;
        }  

    private static int counter = 0;

    private static int randomNumber()
        {
        return ++counter;
        }
 
    public static String getTempDirectory()
        { return temp; }
    
    private static String temp = constructTempDirectory();

    private static String constructTempDirectory()
        {
        try 
            { 
            File x = File.createTempFile( "xxx", ".none" );
            x.delete();
            return x.getParent(); 
            }
        catch (IOException e) 
            { throw new JenaException( e ); }
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

/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]ispo
  $Id: GraphTestBase.java,v 1.9 2003-09-17 12:14:05 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

/**
	@author kers
<br>
    A version of TestCase with assorted extra goodies.
*/

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.test.*;

import java.util.*;
import java.io.*;

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
        
    public static Triple triple( String fact )
        { return Triple.create( fact ); }
        
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
        while (semis.hasMoreTokens()) g.add( triple( semis.nextToken() ) );     
        return g;
        }
        
    public static Graph graphWith( String s )
        {
        return graphAdd( new GraphMem(), s );
        }
        
    public static void assertEqualsTemplate( String title, Graph g, String template )
        {
        assertTrue( title, g.isIsomorphicWith( graphWith( template ) ) );
        }
                
    public static void assertIsomorphic( Graph expected, Graph got )
        {
        if (!expected.isIsomorphicWith( got ))
            fail( "wanted " + expected + " but got " + got );
        }
        
    public static void assertIsomorphic( String title, Graph expected, Graph got )
        {
        if (!expected.isIsomorphicWith( got ))
            fail( title + ": wanted " + expected + " but got " + got );
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
 
    /**
        Answer a File naming a freshly-created directory in the temporary directory. This
        directory should be deleted on exit.
        TODO handle threading issues, mkdir failure, and better cleanup
        
     	@param prefix the prefix for the directory name
     	@return a File naming the new directory
     */
    public static File getScratchDirectory( String prefix )
        {
        File result = new File( getTempDirectory(), prefix + randomNumber() );
        if (result.exists()) return getScratchDirectory( prefix );
        assertTrue( "make temp directory", result.mkdir() );
        result.deleteOnExit();
        return result;   
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

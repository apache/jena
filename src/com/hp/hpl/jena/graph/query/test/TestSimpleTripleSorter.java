/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestSimpleTripleSorter.java,v 1.3 2003-08-12 12:53:05 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.*;

import java.util.*;
import junit.framework.*;

/**
 	@author kers
*/
public class TestSimpleTripleSorter extends GraphTestBase
    {
    public TestSimpleTripleSorter(String name)
        {super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestSimpleTripleSorter.class ); }
        
    private TripleSorter sorter = new SimpleTripleSorter();
        
    /**
        Test that the empty triple array sorts into an empty triple array
    */
    public void testEmpty()
        {
        Triple [] triples = new Triple [] {};
        assertEquals( 0, sorter.sort( triples ).length ); 
        }
       
    /**
        Test that a singleton triple array sorts into that same singleton array
        for various different search-styles of triples
    */ 
    public void testSingle()
        {
        testSingle( "S P O" );
        testSingle( "S ?P O" );
        testSingle( "S P ?O" );
        testSingle( "?S ?P O" );
        testSingle( "?S P ?O" );
        testSingle( "S ?P ?O" );
        testSingle( "?S ?P ?O" );
        testSingle( "?? P O" );
        testSingle( "S ?? O" );
        testSingle( "S P ??O" );
        testSingle( "?? ?? O" );
        testSingle( "?? P ??" );
        testSingle( "S ?? ??" );
        testSingle( "?? ?? ??" );
        }
        
    public void testSingle(String ts )
        {
        Triple t = Triple.create( ts );
        assertEquals( Arrays.asList( new Triple[] {t} ), Arrays.asList( sorter.sort( new Triple[] {t} ) ) );
        }
        
    public void testConcreteFirst()
        {
        testReordersTo( "S P O; ?s ?p ?o", "S P O; ?s ?p ?o" );    
        testReordersTo( "S P O; ?s ?p ?o", "?s ?p ?o; S P O" );    
        testReordersTo( "S P O; ?s ?p ?o; ?a ?b ?c", "?s ?p ?o; ?a ?b ?c; S P O" );
        testReordersTo( "S P O; ?s ?p ?o; ?a ?b ?c", "?s ?p ?o; S P O; ?a ?b ?c" );
        }
        
    public void testBoundFirst()
        {
        testReordersTo( "?s R a; ?s ?p ?o", "?s ?p ?o; ?s R a" );    
        testReordersTo( "?s R a; ?s ?p b;", "?s ?p b; ?s R a" );
        testReordersTo( "?a P b; ?c Q d; ?a P ?c", "?a P b; ?a P ?c; ?c Q d" );
        }
        
    public void testInteraction()
        {
        testReordersTo( "?a P b; ?a Q ?b; ?b R ?c", "?b R ?c; ?a Q ?b; ?a P b" );    
        }
        
    public void testReordersTo( String desired, String original )
        {
        Triple [] o = tripleArray( original ), d = tripleArray( desired );    
        assertEquals( Arrays.asList( d ), Arrays.asList( sorter.sort( o ) ) );
        }
    }


/*
    (c) Copyright Hewlett-Packard Company 2003
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
/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestQuery.java,v 1.1 2003-05-23 15:03:02 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

import junit.framework.*;

/**
 	@author kers
    Test the query API. The query API is a doorway to the graph query SPI,
    so rather than testing that query works in terms of getting results, we
    test that the Model API translates into graph queries. This is a test of
    ModelCom really. Dunno how to make it more general without having to
    make it duplicate all the graph query tests.
*/
public class TestQuery extends ModelTestBase
    {
    public TestQuery( String name )
        { super(name); }
        
    public static TestSuite suite()
        { return new TestSuite( TestQuery.class ); }

    public void testAAA()
        {
        testQueryTranslates( "", "" );
        testQueryTranslates( "x R y", "x R y" );
        testQueryTranslates( "x R _y; p S 99", "x R _y; p S 99" );
        testQueryTranslates( "x R ?y", "x R ?y" );
        testQueryTranslates( "jqv:x jqv:H y", "?x ?H y" );
        }
        
    public void testBBB()
        {
        Model m = ModelFactory.createDefaultModel();
        String [][] tests = 
            {
                {"x", "x"},
                {"jqv:x", "?x"}
            };
        for (int i = 0; i < tests.length; i += 1)
            testVariablesTranslate( resources( m, tests[i][0] ), nodes( tests[i][1] ) );
        }
        
    private void testQueryTranslates( String model, String graph )
        {
        String title = "must translate <" + model + "> to <" + graph + ">";
        Model m = modelWithStatements( model );
        Graph g = GraphTestBase.graphWith( graph );
        QueryMapper qm = new QueryMapper( m, new Resource[0] );
        GraphTestBase.assertEquals( title, g,  qm.getGraph() );
        }
    
    public void testVariablesTranslate( Resource [] vIn, Node [] vOut )
        {
        assertEquals( "broken test", vIn.length, vOut.length );
        QueryMapper qm = new QueryMapper( modelWithStatements( "" ), vIn );
        Node [] result = qm.getVariables();
        assertEquals( vOut.length, result.length );
        for (int i = 0; i < result.length; i += 1)
            assertEquals( "variable did not convert", vOut[i], result[i] );
        }
        
    public void testXXX()
        { 
        Model m = modelWithStatements( "a R b; b S c; a R p; p T d" );
        Model q = modelWithStatements( "jqv:x R jqv:y; jqv:y S jqv:z" );
        ExtendedIterator it = m.queryBindingsWith( q, resources( q, "jqv:x jqv:z") );
        assertTrue( it.hasNext() );
        assertEquals( Arrays.asList( resources( m, "a c" ) ), it.next() );
        assertFalse( "", it.hasNext() );
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
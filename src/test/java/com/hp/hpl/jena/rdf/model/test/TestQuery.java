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

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;

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
            testVariablesTranslate( resources( m, tests[i][0] ), nodeArray( tests[i][1] ) );
        }
        
    private void testQueryTranslates( String model, String graph )
        {
        String title = "must translate <" + model + "> to <" + graph + ">";
        Model m = modelWithStatements( model );
        Graph g = GraphTestBase.graphWith( graph );
        QueryMapper qm = new QueryMapper( m, new Resource[0] );
        GraphTestBase.assertIsomorphic( title, g,  qm.getGraph() );
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
        
    public void testModelQuery()
        { 
        Model m = modelWithStatements( "a R b; b S c; a R p; p T d" );
        Model q = modelWithStatements( "jqv:x R jqv:y; jqv:y S jqv:z" );
        ExtendedIterator<List<? extends RDFNode>> it = ModelQueryUtil.queryBindingsWith( m, q, resources( q, "jqv:x jqv:z") );
        assertTrue( it.hasNext() );
        assertEquals( Arrays.asList( resources( m, "a c b" ) ), it.next() );
        assertFalse( it.hasNext() );
        }
    }

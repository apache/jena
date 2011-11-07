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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

import junit.framework.*;

/**
 	@author kers
*/
public class TestContains extends ModelTestBase
    {
    public TestContains( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestContains.class ); }          
        
    public void testContains( boolean yes, String facts, String resource )
        {
        Model m = modelWithStatements( facts );
        RDFNode r = rdfNode( m, resource );
        if (modelWithStatements( facts ).containsResource( r ) != yes)
            fail( "[" + facts + "] should" + (yes ? "" : " not") + " contain " + resource );
        }
        
    public void testContains()
        {
        testContains( false, "", "x" );
        testContains( false, "a R b", "x" );
        testContains( false, "a R b; c P d", "x" );
    /* */
        testContains( false, "a R b", "z" );
    /* */
        testContains( true, "x R y", "x" );
        testContains( true, "a P b", "P" );
        testContains( true, "i  Q  j", "j" );
        testContains( true, "x R y; a P b; i Q j", "y" );
    /* */
        testContains( true, "x R y; a P b; i Q j", "y" );
        testContains( true, "x R y; a P b; i Q j", "R" );
        testContains( true, "x R y; a P b; i Q j", "a" );
        }
    
    private Resource res( String uri )
        { return ResourceFactory.createResource( "eh:/" + uri ); }
    
    private Property prop( String uri )
        { return ResourceFactory.createProperty( "eh:/" + uri ); }
        
    public void testContainsWithNull()
        {
        testCWN( false, "", null, null, null );
        testCWN( true, "x R y", null, null, null );
        testCWN( false, "x R y", null, null, res( "z" ) );
        testCWN( true, "x RR y", res( "x" ), prop( "RR" ), null );
        testCWN( true, "a BB c", null, prop( "BB" ), res( "c" ) );
        testCWN( false, "a BB c", null, prop( "ZZ" ), res( "c" ) );
        }
    
    public void testCWN( boolean yes, String facts, Resource S, Property P, RDFNode O )
        { assertEquals( yes, modelWithStatements( facts ).contains( S, P, O ) ); }
    
    public void testModelComContainsSPcallsContainsSPO()
        {
        Graph g = Factory.createDefaultGraph();
        final boolean [] wasCalled = {false};
        Model m = new ModelCom( g )
            {
            @Override
            public boolean contains( Resource s, Property p, RDFNode o )
                {
                wasCalled[0] = true;
                return super.contains( s, p, o );
                }
            };
        assertFalse( m.contains( resource( "r" ), property( "p" ) ) );
        assertTrue( "contains(S,P) should call contains(S,P,O)", wasCalled[0] );
        }
    }

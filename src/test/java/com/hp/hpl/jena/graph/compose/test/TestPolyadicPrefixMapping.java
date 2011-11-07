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

package com.hp.hpl.jena.graph.compose.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.*;
import com.hp.hpl.jena.shared.AbstractTestPrefixMapping;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestPolyadicPrefixMapping extends AbstractTestPrefixMapping
    {
    public TestPolyadicPrefixMapping( String name )
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestPolyadicPrefixMapping.class ); }   
    
    Graph gBase;
    Graph g1, g2;
    
    /**
        Will be a polyadic graph with base gBase and subs g1, g2.
    */
    Polyadic poly;
    
    protected static final String alpha = "something:alpha#";
    protected static final String beta = "something:beta#";
    
    @Override
    public void setUp()
        {
        gBase = Factory.createDefaultGraph();
        g1 = Factory.createDefaultGraph();
        g2 = Factory.createDefaultGraph();
        poly = new MultiUnion( new Graph[] {gBase, g1, g2} );
        poly.setBaseGraph( gBase );
        }
    
    @Override
    protected PrefixMapping getMapping()
        {
        Graph gBase = Factory.createDefaultGraph();
        Graph g1 = Factory.createDefaultGraph();
        Graph g2 = Factory.createDefaultGraph();
        Polyadic poly = new MultiUnion( new Graph[] {gBase, g1, g2} );
        return new PolyadicPrefixMappingImpl( poly ); 
        }        
    
    /*
        tests for polyadic prefix mappings
        (a) base mapping is the mutable one
        (b) base mapping over-rides all others
        (c) non-overridden mappings in other maps are visible
    */
    
    public void testOnlyBaseMutated()
        {
        poly.getPrefixMapping().setNsPrefix( "a", alpha );
        assertEquals( null, g1.getPrefixMapping().getNsPrefixURI( "a" ) );
        assertEquals( null, g2.getPrefixMapping().getNsPrefixURI( "a" ) );
        assertEquals( alpha, gBase.getPrefixMapping().getNsPrefixURI( "a" ) );
        }
    
    public void testUpdatesVisible()
        {
        g1.getPrefixMapping().setNsPrefix( "a", alpha );
        g2.getPrefixMapping().setNsPrefix( "b", beta );
        assertEquals( alpha, poly.getPrefixMapping().getNsPrefixURI( "a" ) );
        assertEquals( beta, poly.getPrefixMapping().getNsPrefixURI( "b" ) );
        }
    
    public void testUpdatesOverridden()
        {
        g1.getPrefixMapping().setNsPrefix( "x", alpha );
        poly.getPrefixMapping().setNsPrefix( "x", beta );
        assertEquals( beta, poly.getPrefixMapping().getNsPrefixURI( "x" ) );
        }
    
    public void testQNameComponents()
        {
        g1.getPrefixMapping().setNsPrefix( "x", alpha );
        g2.getPrefixMapping().setNsPrefix( "y", beta );
        assertEquals( "x:hoop", poly.getPrefixMapping().qnameFor( alpha + "hoop" ) );
        assertEquals( "y:lens", poly.getPrefixMapping().qnameFor( beta + "lens" ) );
        }
    
    /**
        Test that the default namespace of a sub-graph doesn't appear as a
        default namespace of the polyadic graph.
     */
    public void testSubgraphsDontPolluteDefaultPrefix() 
        {
        String imported = "http://imported#", local = "http://local#";
        g1.getPrefixMapping().setNsPrefix( "", imported );
        poly.getPrefixMapping().setNsPrefix( "", local );
        assertEquals( null, poly.getPrefixMapping().getNsURIPrefix( imported ) );
        }
    
    public void testPolyDoesntSeeImportedDefaultPrefix()
        {
        String imported = "http://imported#";
        g1.getPrefixMapping().setNsPrefix( "", imported );
        assertEquals( null, poly.getPrefixMapping().getNsPrefixURI( "" ) );
        }
    
    public void testPolyMapOverridesFromTheLeft()
        {
        g1.getPrefixMapping().setNsPrefix( "a", "eh:/U1" );
        g2.getPrefixMapping().setNsPrefix( "a", "eh:/U2" );
        String a = poly.getPrefixMapping().getNsPrefixMap().get( "a" );
        assertEquals( "eh:/U1", a );
        }
    
    public void testPolyMapHandlesBase()
        {
        g1.getPrefixMapping().setNsPrefix( "", "eh:/U1" );
        g2.getPrefixMapping().setNsPrefix( "", "eh:/U2" );
        String a = poly.getPrefixMapping().getNsPrefixMap().get( "" );
        assertEquals( poly.getPrefixMapping().getNsPrefixURI( "" ), a );
        }
    }

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

package com.hp.hpl.jena.mem.test;

import java.util.*;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;


/**
 @author hedgehog
 */
public class TestMixedGraphMem extends TestGraphMem
    {
    public TestMixedGraphMem( String name ) 
        { super( name );}
    
    public static TestSuite suite()
        { return new TestSuite( TestMixedGraphMem.class ); }
        
    @Override public Graph getGraph()
        { return new MixedGraphMem(); }
    
    public void testRepeatedAddSuppressesPredicateAndObject()
        {
        final List<Node> history = new ArrayList<Node>();
        MixedGraphMemStore t = new MixedGraphMemStore( getGraph() )
            {
            @Override
            protected boolean add( Node key, Triple t )
                {
                history.add( key );
                return super.add( key, t );
                }
            };
        t.add( triple( "s P o" ) );
        assertEquals( nodeList( "s P o" ), history );
        t.add( triple( "s P o" ) );
        assertEquals( nodeList( "s P o s" ), history );
        }
    
    @Override public void testUnnecessaryMatches() { 
        /* test not appropriate for subclass */ 
        }
    
    public void testRemoveAbsentSuppressesPredicateAndObject()
        {
        final List<Node> history = new ArrayList<Node>();
        MixedGraphMemStore t = new MixedGraphMemStore( getGraph() )
            {
            @Override
            protected boolean remove( Node key, Triple t )
                {
                history.add( key );
                return super.remove( key, t );
                }
            };
        t.remove( triple( "s P o" ) );
        assertEquals( nodeList( "s" ), history );
        }
    }

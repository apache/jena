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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.ReifierTripleMap;

/**
     TestReifierTripleMap
     @author kers
*/
public abstract class AbstractTestReifierTripleMap extends GraphTestBase
    {
    
    public AbstractTestReifierTripleMap( String name )
        { super( name ); }

    protected abstract ReifierTripleMap getTripleMap();
        
    protected ReifierTripleMap tripleMap = getTripleMap();

    protected static final Triple triple_xRy = triple( "x R y" );
    protected static final Triple triple_aRb = triple( "a R b" );
    
    protected static final Node nodeA = node( "a" );
    protected static final Node nodeB = node( "b" );
    protected static final Node nodeC = node( "c" );
    
    public void testEmptyMap()
        { 
        assertEquals( null, tripleMap.getTriple( nodeA ) );
        assertEquals( null, tripleMap.getTriple( nodeB ) );
        assertEquals( false, tripleMap.hasTriple( triple_xRy ) );
        assertEquals( false, tripleMap.tagIterator().hasNext() );
        assertEquals( false, tripleMap.tagIterator( triple_aRb ).hasNext() );
        assertFalse( tripleMap.find( Triple.ANY ).hasNext() );
        }
    
    public void testPutTriple_hasTriple()
        {
        tripleMap.putTriple( nodeA, triple_xRy );
        assertEquals( true, tripleMap.hasTriple( triple_xRy ) );
        assertEquals( false, tripleMap.hasTriple( triple( "x R z" ) ) );
        }    
    
    public void testPutTriple_getTriple()
        {
        tripleMap.putTriple( nodeA, triple_xRy );
        assertEquals( triple_xRy, tripleMap.getTriple( nodeA ) );
        assertEquals( null, tripleMap.getTriple( nodeB ) );
        }
    
    public void testPutTriple_tagIterator()
        {
        tripleMap.putTriple( nodeA, triple_xRy );
        assertEquals( nodeSet( "a" ), iteratorToSet( tripleMap.tagIterator() ) );
        }
    
    public void testPutTriple_tagIteratorT()
        {
        tripleMap.putTriple( nodeA, triple_xRy );
        assertEquals( nodeSet( "a" ), iteratorToSet( tripleMap.tagIterator( triple_xRy ) ) );
        assertEquals( nodeSet( "" ), iteratorToSet( tripleMap.tagIterator( triple( "x S y" ) ) ) );
        }
    
    public void testPutTriples_hasTriple()
        {
        put_xRy_and_aRb();
        assertEquals( true, tripleMap.hasTriple( triple_xRy ) );
        assertEquals( true, tripleMap.hasTriple( triple_aRb ) );
        }
    
    public void testPutTriples_getTriple()
        {
        put_xRy_and_aRb();
        assertEquals( triple_xRy, tripleMap.getTriple( nodeA ) );
        assertEquals( triple_aRb, tripleMap.getTriple( nodeB ) );
        }
    
    public void testPutTriples_tagIterator()
        {
        put_xRy_and_aRb();
        assertEquals( nodeSet( "a b" ), iteratorToSet( tripleMap.tagIterator() ) );
        }
    
    public void testPutTriples_tagIteratorT()
        {
        put_xRy_and_aRb();
        assertEquals( nodeSet( "a" ), iteratorToSet( tripleMap.tagIterator( triple_xRy ) ) );
        assertEquals( nodeSet( "b" ), iteratorToSet( tripleMap.tagIterator( triple_aRb ) ) );
        }
    
    public void testMultipleTagging()
        {
        tripleMap.putTriple( nodeA, triple_xRy );
        tripleMap.putTriple( nodeB, triple_xRy );
        assertEquals( nodeSet( "a b" ), iteratorToSet( tripleMap.tagIterator( triple_xRy ) ) );
        }
    
    public void testRemoveTriplesByTag()
        {
        put_xRy_and_aRb();
        tripleMap.removeTriple( nodeA );
        assertEquals( nodeSet( "b" ), iteratorToSet( tripleMap.tagIterator() ) );
        assertEquals( nodeSet( "b" ), iteratorToSet( tripleMap.tagIterator( triple_aRb ) ) );
        assertEquals( nodeSet( "" ), iteratorToSet( tripleMap.tagIterator( triple_xRy ) ) );
        }
    
    public void testRemoveTaggedTriple()
        {
        put_xRy_and_aRb();
        tripleMap.removeTriple( nodeA, triple_xRy );
        assertEquals( null, tripleMap.getTriple( nodeA ) );
        assertEquals( triple_aRb, tripleMap.getTriple( nodeB ) );
        assertEquals( nodeSet( "b" ), iteratorToSet( tripleMap.tagIterator( triple_aRb ) ) );
        assertEquals( nodeSet( "" ), iteratorToSet( tripleMap.tagIterator( triple_xRy ) ) );
        }
    
    public void testRemoveTripleDirectly()
        {
        put_xRy_and_aRb();
        tripleMap.putTriple( nodeC, triple_xRy );
        tripleMap.removeTriple( triple_xRy );
        assertEquals( null, tripleMap.getTriple( nodeA ) );
        assertEquals( null, tripleMap.getTriple( nodeC ) );
        assertEquals( triple_aRb, tripleMap.getTriple( nodeB ) );
        assertEquals( nodeSet( "b" ), iteratorToSet( tripleMap.tagIterator( triple_aRb ) ) );
        assertEquals( nodeSet( "" ), iteratorToSet( tripleMap.tagIterator( triple_xRy ) ) );
        }
    
    protected void put_xRy_and_aRb()
        {
        tripleMap.putTriple( nodeA, triple_xRy );
        tripleMap.putTriple( nodeB, triple_aRb );
        }

    }

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

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.*;
import com.hp.hpl.jena.mem.NodeToTriplesMap;

import junit.framework.TestSuite;

/**
 	TestNodeToTriplesMap: added, post-hoc, by kers once NTM got
 	rather complicated. So these tests may be (are, at the moment)
 	incomplete.
*/
public class TestNodeToTriplesMap extends GraphTestBase
    {
    public TestNodeToTriplesMap( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestNodeToTriplesMap.class ); }
    
    protected NodeToTriplesMap ntS = new NodeToTriplesMap( Field.fieldSubject, Field.fieldPredicate, Field.fieldObject );
    	
    protected NodeToTriplesMap ntP = new NodeToTriplesMap( Field.fieldPredicate, Field.fieldObject, Field.fieldSubject );
    	
    protected NodeToTriplesMap ntO = new NodeToTriplesMap( Field.fieldObject, Field.fieldPredicate, Field.fieldSubject );

    protected static final Node x = node( "x" );
    
    protected static final Node y = node( "y" );
    
    public void testZeroSize()
        {
        testZeroSize( "fresh NTM", ntS );
        }
    
    protected void testZeroSize( String title, NodeToTriplesMap nt )
        {
        assertEquals( title + " should have size 0", 0, nt.size() );
        assertEquals( title + " should be isEmpty()", true, nt.isEmpty() );
        assertEquals( title + " should have empty domain", false, nt.domain().hasNext() );
        }
    
    public void testAddOne()
        {
        ntS.add( triple( "x P y" ) );
        testJustOne( x, ntS );
        }
    
    public void testAddOneTwice()
        {
        addTriples( ntS, "x P y; x P y" );
        testJustOne( x, ntS );
        }
    
    protected void testJustOne( Node x, NodeToTriplesMap nt )
        {
        assertEquals( 1, nt.size() );
        assertEquals( false, nt.isEmpty() );
        assertEquals( just( x ), iteratorToSet( nt.domain() ) );
        }
    
    public void testAddTwoUnshared()
        {
        addTriples( ntS, "x P a; y Q b" );
        assertEquals( 2, ntS.size() );
        assertEquals( false, ntS.isEmpty() );
        assertEquals( both( x, y ), iteratorToSet( ntS.domain() ) );
        }
    
    public void testAddTwoShared()
        {
        addTriples( ntS, "x P a; x Q b" );
        assertEquals( 2, ntS.size() );
        assertEquals( false, ntS.isEmpty() );
        assertEquals( just( x ), iteratorToSet( ntS.domain() ) );
        }
    
    public void testClear()
        {
        addTriples( ntS, "x P a; x Q b; y R z" );
        ntS.clear();
        testZeroSize( "cleared NTM", ntS );
        }
    
    public void testAllIterator()
        {
        String triples = "x P b; y P d; y P f";
        addTriples( ntS, triples );
        assertEquals( tripleSet( triples ), iteratorToSet( ntS.iterateAll() ) );
        }
    
    public void testOneIterator()
        {
        addTriples( ntS, "x P b; y P d; y P f" );
        assertEquals( tripleSet( "x P b" ), ntS.iterator( x, null ).toSet() );
        assertEquals( tripleSet( "y P d; y P f" ), ntS.iterator( y, null ).toSet() );
        }
    
    public void testRemove()
        {
        addTriples( ntS, "x P b; y P d; y R f" );
        ntS.remove( triple( "y P d" ) );
        assertEquals( 2, ntS.size() );
        assertEquals( tripleSet( "x P b; y R f" ), ntS.iterateAll().toSet() );
        }
    
    public void testRemoveByIterator()
        {
        addTriples( ntS, "x nice a; a nasty b; x nice c" );
        addTriples( ntS, "y nice d; y nasty e; y nice f" );
        Iterator<Triple> it = ntS.iterateAll();
        while (it.hasNext())
            {
            Triple t = it.next();
            if (t.getPredicate().equals( node( "nasty") )) it.remove();
            }
        assertEquals( tripleSet( "x nice a; x nice c; y nice d; y nice f" ), ntS.iterateAll().toSet() );
        }
    
    public void testIteratorWIthPatternOnEmpty()
        {
        assertEquals( tripleSet( "" ), ntS.iterateAll( triple( "a P b" ) ).toSet() );
        }

    public void testIteratorWIthPatternOnSomething()
        {
        addTriples( ntS, "x P a; y P b; y R c" );
        assertEquals( tripleSet( "x P a" ), ntS.iterateAll( triple( "x P ??" ) ).toSet() );
        assertEquals( tripleSet( "y P b; y R c" ), ntS.iterateAll( triple( "y ?? ??" ) ).toSet() );
        assertEquals( tripleSet( "x P a; y P b" ), ntS.iterateAll( triple( "?? P ??" ) ).toSet() );
        assertEquals( tripleSet( "y R c" ), ntS.iterateAll( triple( "?? ?? c" ) ).toSet() );
        }

    public void testUnspecificRemoveS()
        {
        addTriples( ntS, "x P a; y Q b; z R c" );
        ntS.remove( triple( "x P a" ) );
        assertEquals( tripleSet( "y Q b; z R c" ), ntS.iterateAll().toSet() );
        }
    
    public void testUnspecificRemoveP()
        {
        addTriples( ntP, "x P a; y Q b; z R c" );
        ntP.remove( triple( "y Q b" ) );
        assertEquals( tripleSet( "x P a; z R c" ), ntP.iterateAll().toSet() );
        }
    
    public void testUnspecificRemoveO()
        {
        addTriples( ntO, "x P a; y Q b; z R c" );
        ntO.remove( triple( "z R c" ) );
        assertEquals( tripleSet( "x P a; y Q b" ), ntO.iterateAll().toSet() );
        }
    
    public void testAddBooleanResult()
        {
        assertEquals( true, ntS.add( triple( "x P y" ) ) );
        assertEquals( false, ntS.add( triple( "x P y" ) ) );
    /* */
        assertEquals( true, ntS.add( triple( "y Q z" ) ) );
        assertEquals( false, ntS.add( triple( "y Q z" ) ) );
    /* */
        assertEquals( true, ntS.add( triple( "y R s" ) ) );
        assertEquals( false, ntS.add( triple( "y R s" ) ) );
        }
    
    public void testRemoveBooleanResult()
        {
        assertEquals( false, ntS.remove( triple( "x P y" ) ) );
        ntS.add( triple( "x P y" ) );
        assertEquals( false, ntS.remove( triple( "x Q y" ) ) );
        assertEquals( true, ntS.remove( triple( "x P y" ) ) );
        assertEquals( false, ntS.remove( triple( "x P y" ) ) );
        }
    
    public void testContains()
        {
        addTriples( ntS, "x P y; a P b" );
        assertTrue( ntS.contains( triple( "x P y" ) ) );
        assertTrue( ntS.contains( triple( "a P b" ) ) );
        assertFalse( ntS.contains( triple( "x P z" ) ) );
        assertFalse( ntS.contains( triple( "y P y" ) ) );
        assertFalse( ntS.contains( triple( "x R y" ) ) );
        assertFalse( ntS.contains( triple( "e T f" ) ) );
        assertFalse( ntS.contains( triple( "_x F 17" ) ) );
        }
    
    // TODO more here
    
    protected void addTriples( NodeToTriplesMap nt, String facts )
        {
        Triple [] t = tripleArray( facts );
            for ( Triple aT : t )
            {
                nt.add( aT );
            }
        }
    
    protected static <T> Set<T> just( T x )
        {
        Set<T> result = new HashSet<>();
        result.add( x );
        return result;
        }

    protected static Set<Object> both( Object x, Object y )
        {
        Set<Object> result = just( x );
        result.add( y );
        return result;
        }
    
    }

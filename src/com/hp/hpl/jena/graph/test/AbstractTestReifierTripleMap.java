/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: AbstractTestReifierTripleMap.java,v 1.4 2005-02-21 11:52:37 andy_seaborne Exp $
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

/*
    (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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
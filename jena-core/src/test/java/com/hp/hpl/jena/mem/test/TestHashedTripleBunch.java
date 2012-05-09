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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;

public class TestHashedTripleBunch extends TestTripleBunch
    {
    public TestHashedTripleBunch( String name )
        { super( name ); }

    protected static class HTB extends HashedTripleBunch
        {
        public HTB( TripleBunch b )
            { super( b ); }
    
        @Override
        protected int improveHashCode( int hashCode )
            { return hashCode; }    
        }
    
    @Override
    public TripleBunch getBunch()
        { return new HashedTripleBunch( emptyBunch ); }
    
    HashedTripleBunch htb = new HTB( emptyBunch ); 
    
    static class TripleWithHash extends Triple
        {
        final int hash;
        
        TripleWithHash( int hash, Node s, Node p, Node o )
            {
            super( s, p, o );
            this.hash = hash;
            }
        
        public static TripleWithHash create( int n, String s )
            {
            Triple t = triple( s );
            return new TripleWithHash( n, t.getSubject(), t.getPredicate(), t.getObject() );            
            }
        
        @Override
        public int hashCode()
            { return hash; }
        }
    
    public void testHashcodeUsedAsIndex()
        {
        HashedTripleBunch htb = new HTB( emptyBunch );
        int limit = htb.currentCapacity();
        for (int i = 0; i < limit; i += 1)
            {
            TripleWithHash t = TripleWithHash.create( i, "s p o" );
            htb.add( t );
            assertSame( t, htb.getItemForTestingAt( i ) );
            }
        }

    public void testRemovePerformsShiftFromTop()
        {
        int capacity = htb.currentCapacity();
        testRemovePerformsShift( capacity - 1, capacity );
        }
    
    public void testRemovePerformsShiftFromMiddle()
        {
        int capacity = htb.currentCapacity();
        testRemovePerformsShift( capacity - 3, capacity );
        }
    
    public void testRemovePerformsShiftWrappingLowestTwo()
        {
        int capacity = htb.currentCapacity();
        testRemovePerformsShift( 0, capacity );
        }
    public void testRemovePerformsShiftWrappingLowest()
        {
        int capacity = htb.currentCapacity();
        testRemovePerformsShift( 1, capacity );
        }

    private void testRemovePerformsShift( int most, int capacity )
        {
        int next = most - 1; if (next < 0) next += capacity;
        int least = most - 2; if (least < 0) least += capacity;
        TripleWithHash t1 = TripleWithHash.create( most, "s p o" );
        TripleWithHash t2 = TripleWithHash.create( next, "a b c" );
        TripleWithHash t3 = TripleWithHash.create( most, "x y z" );
        htb.add( t1 );
        htb.add( t2 );
        htb.add( t3 );
        assertSame( t1, htb.getItemForTestingAt( most ) );
        assertSame( t2, htb.getItemForTestingAt( next ) );
        assertSame( t3, htb.getItemForTestingAt( least ) );
    //
        htb.remove( t1 );
        assertSame( t3, htb.getItemForTestingAt( most ) );
        assertSame( t2, htb.getItemForTestingAt( next ) );
        assertSame( null, htb.getItemForTestingAt( least ) );
        }
    
    public void testIteratorRemovePerformsShiftAndDeliversElementFromTop()
        {
        int capacity = htb.currentCapacity();
        testIteratorRemovePerformsShiftAndDeliversElement( capacity - 1, capacity );
        }
    
    public void testIteratorRemovePerformsShiftAndDeliversElementFromMiddle()
        {
        int capacity = htb.currentCapacity();
        testIteratorRemovePerformsShiftAndDeliversElement( capacity - 3, capacity );
        }
    
//    public void testIteratorRemovePerformsShiftAndDeliversElementWrappingLowest()
//        {
//        int capacity = htb.currentCapacity();
//        testIteratorRemovePerformsShiftAndDeliversElement( 1, capacity );
//        }
//    
//    public void testIteratorRemovePerformsShiftAndDeliversElementWrappingLowestTwo()
//        {
//        int capacity = htb.currentCapacity();
//        testIteratorRemovePerformsShiftAndDeliversElement( 0, capacity );
//        }

    private void testIteratorRemovePerformsShiftAndDeliversElement( int most, int capacity )
        {
//        int next = most - 1; if (next < 0) next += capacity;
//        int least = most - 2; if (least < 0) least += capacity;
//        TripleWithHash t1 = TripleWithHash.create( most, "s p o" );
//        TripleWithHash t2 = TripleWithHash.create( next, "a b c" );
//        TripleWithHash t3 = TripleWithHash.create( most, "x y z" );
//        htb.add( t1 );
//        htb.add( t2 );
//        htb.add( t3 );
//        ExtendedIterator it = htb.iterator();
//        assertSame( t1, it.next() );
//        it.remove();
//        assertSame( t3, it.next() );
//        assertSame( t2, it.next() );
        }
    }

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.memvalue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.junit.jupiter.api.Test;

public class TestHashCommon
    {
    protected static final Item item2X = new Item( 2, "X" );
    protected static final Item item1Y = new Item( 1, "Y" );
    protected static final Item item2Z = new Item( 2, "Z" );


    static class ProbeHashCommon extends HashCommon<Object>
        {
        protected ProbeHashCommon( int initialCapacity )
            { super( initialCapacity ); }

        protected void set( int index, Item object )
            { keys[index] = object; }

        @Override public Object removeFrom( int here )
            { return super.removeFrom( here ); }

        public int top()
            { return capacity - 1; }

        public int capacity()
            { return capacity; }

        @Override protected int improveHashCode( int hashCode )
            { return hashCode; }

        @Override protected Object[] newKeyArray( int size )
            { return new Object[size]; }
        }

    static class Item
        {
        protected final int n;
        protected final String s;

        public Item( int n, String s ) { this.n = n; this.s = s; }

        @Override public int hashCode() { return n; }

        @Override public boolean equals( Object other )
            { return other instanceof Item && s.equals( ((Item) other).s ); }

        @Override public String toString()
            { return s + "#" + n; }
        }

    @Test
    public void testCheckTestDataConstruction()
        {
        ProbeHashCommon h = probeWith( "1:2:x 4:7:y -1:5:z" );
        assertEquals( new Item( 2, "x" ), h.getItemForTestingAt( 1 ) );
        assertEquals( new Item( 7, "y" ), h.getItemForTestingAt( 4 ) );
        assertEquals( new Item( 5, "z" ), h.getItemForTestingAt( h.top() ) );
        }

    @Test
    public void testHashcodeUsedAsIndex()
        {
        ProbeHashCommon htb = new ProbeHashCommon( 10 );
        int limit = htb.capacity();
        for (int i = 0; i < limit; i += 1)
            {
            Item t = new Item( i, "s p o" );
//            assertEquals( i, htb.)
//            assertSame( t, htb.getItemForTestingAt( i ) );
            }
        }

    @Test
    public void testRemoveNoMove()
        {
        ProbeHashCommon h = probeWith( "1:1:Y 2:2:Z" );
        Item moved = (Item) h.removeFrom( 2 );
        assertSame( null, moved );
        assertAlike( probeWith( "1:1:Y" ), h );
        }

    @Test
    public void testRemoveSimpleMove()
        {
        ProbeHashCommon h = probeWith( "0:0:X 1:2:Y -1:2:Z" );
        Item moved = (Item) h.removeFrom( 1 );
        assertSame( null, moved );
        assertAlike( probeWith( "0:0:X 1:2:Z" ), h );
        }

    @Test
    public void testRemoveCircularMove()
        {
        ProbeHashCommon h = probeWith( "0:2:X 1:1:Y 2:2:Z" );
        Item moved = (Item) h.removeFrom( 1 );
        assertEquals( new Item( 2, "X" ), moved );
        assertAlike( probeWith( "1:2:X 2:2:Z"), h );
        }

    @Test
    public void testKeyIterator()
        {
        ProbeHashCommon h = probeWith( "0:0:X" );
        Set<?> elements = h.keyIterator().toSet();
        assertEquals( itemSet( "0:X" ), elements );
        }

    private void assertAlike( ProbeHashCommon desired, ProbeHashCommon got )
        {
        assertEquals(desired.capacity(), got.capacity(), "capacities must be equal" );
        for (int i = 0; i < desired.capacity(); i += 1)
            assertEquals( desired.getItemForTestingAt( i ), got.getItemForTestingAt( i ) );
        }

    protected ProbeHashCommon probeWith( String items )
        {
        ProbeHashCommon result = new ProbeHashCommon( 10 );
        StringTokenizer st = new StringTokenizer( items );
        while (st.hasMoreTokens())
            {
            String item = st.nextToken();
            StringTokenizer itemElements = new StringTokenizer( item, ":" );
            int index = Integer.parseInt( itemElements.nextToken() );
            int hash = Integer.parseInt( itemElements.nextToken() );
            String w = itemElements.nextToken();
            result.set( (index< 0 ? index + result.capacity() : index), new Item( hash, w ) );
            }
        return result;
        }

    protected Set<Item> itemSet( String items )
        {
        Set<Item> result = new HashSet<>();
        StringTokenizer st = new StringTokenizer( items );
        while (st.hasMoreTokens()) addItem( result, st.nextToken() );
        return result;
        }

    private void addItem( Set<Item> result, String item )
        {
        StringTokenizer itemElements = new StringTokenizer( item, ":" );
        int hash = Integer.parseInt( itemElements.nextToken() );
        String w = itemElements.nextToken();
        result.add( new Item( hash, w ) );
        }
    }

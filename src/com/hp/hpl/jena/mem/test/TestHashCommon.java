/*
 	(c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestHashCommon.java,v 1.5 2007-01-02 11:51:11 andy_seaborne Exp $
*/

package com.hp.hpl.jena.mem.test;

import java.util.*;

import com.hp.hpl.jena.mem.HashCommon;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestHashCommon extends ModelTestBase
    {
    protected static final Item item2X = new Item( 2, "X" );
    protected static final Item item1Y = new Item( 1, "Y" );
    protected static final Item item2Z = new Item( 2, "Z" );

    public TestHashCommon( String name )
        { super( name ); }
    
    static class ProbeHashCommon extends HashCommon
        {
        protected ProbeHashCommon( int initialCapacity )
            { super( initialCapacity ); }
        
        protected void set( int index, Item object )
            { keys[index] = object; }
        
        public Object removeFrom( int here )
            { return super.removeFrom( here ); }
        
        public int top()
            { return capacity - 1; }

        public int capacity()
            { return capacity; }
        }
    
    static class Item
        {
        protected final int n;
        protected final String s;
        
        public Item( int n, String s ) { this.n = n; this.s = s; }
        
        public int hashCode() { return n; }
        
        public boolean equals( Object other )
            { return other instanceof Item && s.equals( ((Item) other).s ); }
        
        public String toString()
            { return s + "#" + n; }
        }    

    public void testSanityCheckTestDataConstruction()
        {
        ProbeHashCommon h = probeWith( "1:2:x 4:7:y -1:5:z" );
        assertEquals( new Item( 2, "x" ), h.getItemForTestingAt( 1 ) );
        assertEquals( new Item( 7, "y" ), h.getItemForTestingAt( 4 ) );
        assertEquals( new Item( 5, "z" ), h.getItemForTestingAt( h.top() ) );
        }

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
    
    public void testRemoveNoMove()
        {
        ProbeHashCommon h = probeWith( "1:1:Y 2:2:Z" );
        Item moved = (Item) h.removeFrom( 2 );
        assertSame( null, moved );
        assertAlike( probeWith( "1:1:Y" ), h );
        }

    public void testRemoveSimpleMove()
        {
        ProbeHashCommon h = probeWith( "0:2:X 1:1:Y 2:2:Z" );
        assertSame( null, (Item) h.removeFrom( 1 ) );
        assertAlike( probeWith( "1:2:X 2:2:Z"), h );
        }
    
    public void testRemoveCircularMove()
        {
        ProbeHashCommon h = probeWith( "0:0:X 1:2:Y -1:2:Z" );
        Item moved = (Item) h.removeFrom( 1 );
        assertAlike( probeWith( "0:0:X 1:2:Z" ), h );
        assertEquals( new Item( 2, "Z" ), moved );
        }
    
    public void testKeyIterator()
        {
        ProbeHashCommon h = probeWith( "0:0:X" );
        Set elements = h.keyIterator().toSet();
        assertEquals( itemSet( "0:X" ), elements );
        }

    /**
        Assert that the two probe HashCommon's are "alike", that is, that they
        have key arrays of equal size and are element-by-element equal. 
        Otherwise, fail (preferably with an appropriate message).
    */
    private void assertAlike( ProbeHashCommon desired, ProbeHashCommon got )
        {
        assertEquals( "capacities must be equal", desired.capacity(), got.capacity() );
        for (int i = 0; i < desired.capacity(); i += 1)
            assertEquals( desired.getItemForTestingAt( i ), got.getItemForTestingAt( i ) );
        }
    
    /**
        Answer a probe with the specified items. <code>items</code> is a
        space-separated string of item descriptions. Each description is a
        colon-separated sequence <code>index:hash:label</code>: the
        item <code>(hash, label)</code> will be placed at <code>index</code>.
        Negative index values are interpreted as indexs from the <i>end</code>
        of the key array, by adding the probe's capacity to them. 
    */
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
    
    protected Set itemSet( String items )
        {
        Set result = new HashSet();
        StringTokenizer st = new StringTokenizer( items );
        while (st.hasMoreTokens()) addItem( result, st.nextToken() );
        return result;
        }

    private void addItem( Set result, String item )
        {
        StringTokenizer itemElements = new StringTokenizer( item, ":" );
        int hash = Integer.parseInt( itemElements.nextToken() );
        String w = itemElements.nextToken();
        result.add( new Item( hash, w ) );
        }
    }

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
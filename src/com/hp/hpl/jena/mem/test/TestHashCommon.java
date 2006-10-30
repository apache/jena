/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestHashCommon.java,v 1.2 2006-10-30 16:18:58 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.test;

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
    
    public void testRemoveNoMove()
        {
        ProbeHashCommon h = new ProbeHashCommon( 10 );
        h.set( 1, item1Y );
        h.set( 2, item2Z );
        Item moved = (Item) h.removeFrom( 2 );
        assertSame( null, moved );
        assertSame( null, h.getItemForTestingAt( 0 ) );
        assertSame( item1Y, h.getItemForTestingAt( 1 ) );
        assertSame( null, h.getItemForTestingAt( 2 ) );
        }
    
    public void testRemoveSimpleMove()
        {
        ProbeHashCommon h = new ProbeHashCommon( 10 );
        h.set( 0, item2X );
        h.set( 1, item1Y );
        h.set( 2, item2Z );
        Item moved = (Item) h.removeFrom( 1 );
        assertSame( null, moved );
        assertSame( null, h.getItemForTestingAt( 0 ) );
        assertSame( item2X, h.getItemForTestingAt( 1 ) );
        assertSame( item2Z, h.getItemForTestingAt( 2 ) );
        }
    
    public void testRemoveCircularMove()
        {
        ProbeHashCommon h = new ProbeHashCommon( 10 );
        Item item0X = new Item( 0, "X" );
        h.set( 0, item0X );
        h.set( 1, new Item( 2, "Y" ) );
        h.set( h.top(), item2Z );
        Item moved = (Item) h.removeFrom( 1 );
        assertSame( item0X, h.getItemForTestingAt( 0 ) );
        assertSame( item2Z, h.getItemForTestingAt( 1 ) );
        // TODO assertSame( item2Z, moved );
        }
    }

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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
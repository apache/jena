/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestHashCommon.java,v 1.1 2006-10-30 15:57:15 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.test;

import com.hp.hpl.jena.mem.HashCommon;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestHashCommon extends ModelTestBase
    {
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
        }
    
    static class Item
        {
        protected final int n;
        protected final String s;
        
        public Item( int n, String s ) { this.n = n; this.s = s; }
        
        public int hashCode() { return n; }
        
        public boolean equals( Object other )
            { return other instanceof Item && s.equals( ((Item) other).s ); }
        }
    
    public void testY()
        {
        ProbeHashCommon h = new ProbeHashCommon( 10 );
        h.set( 0, new Item( 2, "X" ) );
        h.set( 1, new Item( 1, "Y" ) );
        h.set( 2, new Item( 2, "Z" ) );
        Item moved = (Item) h.removeFrom( 1 );
        assertSame( null, moved );
        }
    }


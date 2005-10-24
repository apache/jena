/*
    (c) Copyright 2005 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: TestTripleBunch.java,v 1.2 2005-10-24 09:13:41 chris-dollin Exp $
*/
package com.hp.hpl.jena.mem.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.StageElement;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.mem.MatchOrBind;
import com.hp.hpl.jena.mem.TripleBunch;
import com.hp.hpl.jena.util.iterator.*;

public class TestTripleBunch extends GraphTestBase
    {
    protected static final Triple tripleSPO = triple( "s P o" );
    protected static final Triple tripleXQY = triple( "x Q y" );

    public class HashedTripleBunch extends TripleBunch
        {
        protected Triple [] contents = new Triple[3];
        
        protected int size = 0;
        protected int threshold = 2;
        
        protected final Triple REMOVED = triple( "-s- --P-- -o-" );
        
        public boolean contains( Triple t )
            { return findSlot( t ) < 0; }
            
        protected int findSlot( Triple t )
            {
            int index = t.hashCode() % contents.length;
            while (true)
                {
                if (t.equals( contents[index] )) return ~index;
                if (contents[index] == null) return index;
                index = (index == 0 ? contents.length - 1 : index - 1);
                }
            }         
        
        protected int findSlotBySameValueAs( Triple t )
            { fail( "not implemented" ); return 0;
            }
        
        public boolean containsBySameValueAs( Triple t )
            { return findSlotBySameValueAs( t ) < 0; }

        public int size()
            { return size; }

        public void add( Triple t )
            {
            assertFalse( "precondition", contains( t ) );
            int where = findSlot( t );
            assertFalse( where < 0 );
            contents[where] = t;
            size += 1;
            if (size > threshold) grow();
            }

        protected void grow()
            {
            int newCapacity = computeNewCapacity();
            Triple [] oldContents = contents;
            contents = new Triple[newCapacity];
            for (int i = 0; i < oldContents.length; i += 1)
                {
                Triple t = oldContents[i];
                if (t != null && t != REMOVED) contents[findSlot( t )] = t;
                }
            }

        protected int computeNewCapacity()
            {
            threshold = (int) (contents.length * 2 * 1.75);
            return contents.length * 2;
            }

        public void remove( Triple t )
            {
            assertTrue( "precondition", contains( t ) );
            int where = findSlot( t );
            assertTrue( where < 0 );
            contents[~where] = REMOVED;
            size -= 1;
            }

        public ExtendedIterator iterator()
            {
            return new NiceIterator()
                {
                int index = 0;
                int lastIndex = -1;
                
                public boolean hasNext()
                    {
                    while (index < contents.length && (contents[index] == null || contents[index] == REMOVED))
                        index += 1;
                    return index < contents.length;
                    }
                
                public Object next()
                    {
                    assertTrue( hasNext() );
                    Object answer = contents[index];
                    lastIndex = index;
                    index += 1;
                    return answer;
                    }
                
                public void remove()
                    {
                    contents[lastIndex] = REMOVED;
                    }
                };
            }

        public void app( Domain d, StageElement next, MatchOrBind s )
            {
            }
        }
    
    public TestTripleBunch(String name)
        { super( name ); }

    private TripleBunch getBunch()
        {
        return new HashedTripleBunch();
        }
    
    public void testEmptyBunch()
        {
        TripleBunch b = getBunch();
        assertEquals( 0, b.size() );
        assertFalse( b.contains( tripleSPO ) );
        assertFalse( b.contains( tripleXQY ) );
        assertFalse( b.iterator().hasNext() );
        }

    public void testAddElement()
        {
        TripleBunch b = getBunch();
        b.add( tripleSPO );
        assertEquals( 1, b.size() );
        assertTrue( b.contains( tripleSPO ) );
        assertEquals( listOf( tripleSPO ), iteratorToList( b.iterator() ) );
        }

    public void testAddElements()
        {
        TripleBunch b = getBunch();
        b.add( tripleSPO );
        b.add( tripleXQY );
        assertEquals( 2, b.size() );
        assertTrue( b.contains( tripleSPO ) );
        assertTrue( b.contains( tripleXQY ) );
        assertEquals( setOf( tripleSPO, tripleXQY ), iteratorToSet( b.iterator() ) );
        }
    
    public void testRemoveOnlyElement()
        {
        TripleBunch b = getBunch();
        b.add( tripleSPO );
        b.remove( tripleSPO );
        assertEquals( 0, b.size() );
        assertFalse( b.contains( tripleSPO ) );
        assertFalse( b.iterator().hasNext() );
        }
    
    public void testRemoveFirstOfTwo()
        {
        TripleBunch b = getBunch();
        b.add( tripleSPO );
        b.add( tripleXQY );
        b.remove( tripleSPO );
        assertEquals( 1, b.size() );
        assertFalse( b.contains( tripleSPO ) );
        assertTrue( b.contains( tripleXQY ) );
        assertEquals( listOf( tripleXQY ), iteratorToList( b.iterator() ) );
        }

    public void testTableGrows()
        {
        TripleBunch b = getBunch();
        b.add( tripleSPO );
        b.add( tripleXQY );
        b.add( triple( "a I b" ) );
        b.add( triple( "c J d" ) );
        }
        
    protected List listOf( Triple x )
        {
        List result = new ArrayList();
        result.add( x );
        return result;
        }
    
    protected Set setOf( Triple x, Triple y )
        {
        Set result = setOf( x );
        result.add( y );
        return result;
        }
    
    protected Set setOf( Triple x )
        {
        Set result = new HashSet();
        result.add( x );
        return result;
        }
    }

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
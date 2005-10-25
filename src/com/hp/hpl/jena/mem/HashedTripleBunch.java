/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: HashedTripleBunch.java,v 1.3 2005-10-25 14:34:35 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;

public class HashedTripleBunch extends TripleBunch
    {
    protected Triple [] contents = new Triple[18];
    
    protected int capacity = contents.length;
    protected int size = 0;
    protected int threshold = 14;
    
    public HashedTripleBunch( TripleBunch b )
        {
        // System.err.println( ">> HashedTripleBunch" );
        for (Iterator it = b.iterator(); it.hasNext();) add( (Triple) it.next() );        
        }
    
    public boolean contains( Triple t )
        { return findSlot( t ) < 0; }

    protected final int initialIndexFor( Triple t )
        { return Math.abs( t.hashCode() ) % capacity; }
    
    protected int findSlot( Triple t )
        {
        int index = initialIndexFor( t );
        // System.err.println( "]] probe index for " + t + " = " + index );
        while (true)
            {
            Triple current = contents[index];
            if (current == null) return index;
            if (t.equals( current )) return ~index;
            index = (index == 0 ? capacity - 1 : index - 1);
            // System.err.println( "]]  nope; moving to " + index );
            }
        }         
    
    protected int findSlotBySameValueAs( Triple t )
        {
        int index = initialIndexFor( t );
        while (true)
            {
            Triple current = contents[index];
            if (current == null) return index;
            if (t.matches( current )) return ~index;
            index = (index == 0 ? capacity - 1 : index - 1);
            }
        }
    
    public boolean containsBySameValueAs( Triple t )
        { return findSlotBySameValueAs( t ) < 0; }
    
    public int size()
        { return size; }
    
    public void add( Triple t )
        {
        dumpState( "pre-add" );
        // System.err.println( ">> adding " + t + " [current size " + size + "]" );
        if (contains( t )) throw new RuntimeException( "precondition violation" );
        int where = findSlot( t );
        if (where < 0) throw new RuntimeException( "internal error" );
        // System.err.println( ">> " + where + " := " + t );
        contents[where] = t;
        size += 1;
        // System.err.println( ">> size := " + size );
        if (size > threshold) grow();
        dumpState( "post-add" );
        // System.err.println( ">> added" );
        }
    
    protected void grow()
        {
        // System.err.println( ">> GROW" );
        Triple [] oldContents = contents;
        final int oldCapacity = capacity;
        contents = new Triple[capacity = computeNewCapacity()];
        for (int i = 0; i < oldCapacity; i += 1)
            {
            Triple t = oldContents[i];
            if (t != null) contents[findSlot( t )] = t;
            }
        // System.err.println( ">> capacity := " + contents.length );
        }
    
    protected int computeNewCapacity()
        {
        threshold = (int) (capacity * 2 * 0.75);
        return capacity * 2;
        }
    
    public void remove( Triple t )
        {
        dumpState( "pre-remove" );
        // System.err.println( ">> removing " + t + " [current size " + size + "]" );
        if (!contains( t )) throw new RuntimeException( "precondition violation" );
        int where = findSlot( t );
        if (where >= 0 ) throw new RuntimeException( "internal error" );
        remove( ~where );
        // System.err.println( ">> " + ~where + " REMOVED" );
        size -= 1;
        // System.err.println( ">> size := " + size );
        dumpState( "post-remove" ); 
        }
    
    /**
        Remove the triple at element <code>i</code> of <code>contents</code>.
        This is an implementation of Knuth's Algorithm R from tAoCP vol3, p 527.
        It relies on linear probing but doesn't require a distinguished REMOVED
        value. Since we resize the table when it gets fullish, we don't worry [much]
        about the overhead of the linear probing.
    */
    protected void remove( int i )
        {
        while (true)
            {
            contents[i] = null;
            int j = i;
            while (true)
                {
                i = (i == 0 ? capacity - 1 : i - 1);
                Triple t = contents[i];
                if (t == null) return;
                int r = initialIndexFor( t );
                if (!((i <= r && r < j) || (r < j && j < i) || (j < i && i <= r) )) break;
                }
            contents[j] = contents[i];
            }
        }
    
    protected void dumpState( String title )
        {
        if (false)
            {
            ArrayList list = new ArrayList();
            System.err.println( "** " + title );
            for (int i = 0; i < capacity; i += 1)
                {
                Triple t = contents[i];
                System.err.print( "  contents[" + i + "]" );
                if (t == null) 
                    System.err.println( " FREE" );
                else
                    {
                    list.add( t.getPredicate() );
                    System.err.println( " " + t )
                ;   }
                }
            Set set = new HashSet( list );
            if (set.size() != list.size()) throw new RuntimeException( "broken" );
            }
        }
    
    public ExtendedIterator iterator()
        {
        return new NiceIterator()
            {
            int index = 0;
            int lastIndex = -1;
            Triple toRemove = null;
            
            public boolean hasNext()
                {
                while (index < capacity && (contents[index] == null))
                    index += 1;
                return index < capacity;
                }
            
            public Object next()
                {
                if (hasNext() == false) noElements( "" );
                Object answer = contents[index];
                lastIndex = index;
                toRemove = contents[index];
                index += 1;
                return answer;
                }
            
            public void remove()
                {
                if (contents[lastIndex] != toRemove) throw new RuntimeException( "CME" );
                HashedTripleBunch.this.remove( lastIndex );
                dumpState( "iterator-remove" );
                }
            };
        }
    
    public void app( Domain d, StageElement next, MatchOrBind s )
        {
        for (int i = 0; i < capacity; i += 1)
            {
            Triple t = contents[i];
            if (t != null  && s.matches( t )) next.run( d );
            }
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
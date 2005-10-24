/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: HashedTripleBunch.java,v 1.1 2005-10-24 15:35:42 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;

public class HashedTripleBunch extends TripleBunch
    {
    protected Triple [] contents = new Triple[18];
    
    protected int size = 0;
    protected int threshold = 14;
    
    protected final Triple REMOVED = Triple.create( "-s- --P-- -o-" );
    
    public HashedTripleBunch( TripleBunch b )
        {
        // System.err.println( ">> HashedTripleBunch" );
        for (Iterator it = b.iterator(); it.hasNext();) add( (Triple) it.next() );        
        }
    
    public boolean contains( Triple t )
        { return findSlot( t ) < 0; }
        
    protected int findSlot( Triple t )
        {
        int index = Math.abs( t.hashCode() ) % contents.length;
        System.err.println( "]] probe index = " + index );
        while (true)
            {
            Triple current = contents[index];
            if (current == null || current == REMOVED) return index;
            if (t.equals( current )) return ~index;
            index = (index == 0 ? contents.length - 1 : index - 1);
            }
        }         
    
    protected int findSlotBySameValueAs( Triple t )
        {
        int index = Math.abs( t.hashCode() ) % contents.length;
        while (true)
            {
            Triple current = contents[index];
            if (current == null || current == REMOVED) return index;
            if (t.matches( current )) return ~index;
            index = (index == 0 ? contents.length - 1 : index - 1);
            }
        }
    
    public boolean containsBySameValueAs( Triple t )
        { return findSlotBySameValueAs( t ) < 0; }
    
    public int size()
        { return size; }
    
    public void add( Triple t )
        {
        System.err.println( ">> adding " + t + " [current size " + size + "]" );
        if (contains( t )) throw new RuntimeException( "precondition violation" );
        int where = findSlot( t );
        if (where < 0) throw new RuntimeException( "internal error" );
        System.err.println( ">> " + where + " := " + t );
        contents[where] = t;
        size += 1;
        System.err.println( ">> size := " + size );
        if (size > threshold) grow();
        dumpState( "add" );
        // System.err.println( ">> added" );
        }
    
    protected void grow()
        {
        System.err.println( ">> GROW" );
        int newCapacity = computeNewCapacity();
        Triple [] oldContents = contents;
        contents = new Triple[newCapacity];
        for (int i = 0; i < oldContents.length; i += 1)
            {
            Triple t = oldContents[i];
            if (t != null && t != REMOVED) 
                {
                int slot = findSlot( t );
                if (slot < 0) 
                    {
                    System.err.println( "broken in grow" );
                    throw new RuntimeException( "broken in grow" );
                    }
                contents[slot] = t;
                }
            }
        System.err.println( ">> capacity := " + contents.length );
        }
    
    protected int computeNewCapacity()
        {
        threshold = (int) (contents.length * 2 * 0.75);
        return contents.length * 2;
        }
    
    public void remove( Triple t )
        {
        System.err.println( ">> removing " + t + " [current size " + size + "]" );
        if (!contains( t )) throw new RuntimeException( "precondition violation" );
        int where = findSlot( t );
        if (where >= 0 ) throw new RuntimeException( "internal error" );
        contents[~where] = REMOVED;
        System.err.println( ">> " + ~where + " REMOVED" );
        size -= 1;
        System.err.println( ">> size := " + size );
        dumpState( "remove" ); 
        }
    
    protected void dumpState( String title )
        {
        ArrayList list = new ArrayList();
        System.err.println( "** post-" + title );
        for (int i = 0; i < contents.length; i += 1)
            {
            Triple t = contents[i];
            System.err.print( "  contents[" + i + "]" );
            if (t == null) System.err.println( " FREE" );
            else if (t == REMOVED) System.err.println( " REMOVED" );
            else
                {
                list.add( t.getPredicate() );
                System.err.println( " " + t )
            ;   }
            }
        Set set = new HashSet( list );
        if (set.size() != list.size()) throw new RuntimeException( "broken" );
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
                if (hasNext() == false) noElements( "" );
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
        for (int i = 0; i < contents.length; i += 1)
            {
            Triple t = contents[i];
            if (t != null && t != REMOVED && s.matches( t )) next.run( d );
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
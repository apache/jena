/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: HashedTripleBunch.java,v 1.1 2009-06-29 08:55:55 castagna Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;

public class HashedTripleBunch extends HashCommon<Triple> implements TripleBunch
    {    
    public HashedTripleBunch( TripleBunch b )
        {
        super( nextSize( (int) (b.size() / loadFactor) ) );
        for (Iterator<Triple> it = b.iterator(); it.hasNext();) add( it.next() );        
        changes = 0;
        }

    @Override protected Triple[] newKeyArray( int size )
        { return new Triple[size]; }

    @Override
    public boolean contains( Triple t )
        { return findSlot( t ) < 0; }    
    
    protected int findSlotBySameValueAs( Triple key )
        {
        int index = initialIndexFor( key );
        while (true)
            {
            Object current = keys[index];
            if (current == null) return index;
            if (key.matches( (Triple) current )) return ~index;
            if (--index < 0) index += capacity;
            }
        }
    
    @Override
    public boolean containsBySameValueAs( Triple t )
        { return findSlotBySameValueAs( t ) < 0; }
    
    /**
        Answer the number of items currently in this TripleBunch. 
        @see com.hp.hpl.jena.mem.TripleBunch#size()
    */
    @Override
    public int size()
        { return size; }
    
    /**
        Answer the current capacity of this HashedTripleBunch; for testing purposes
        only. [Note that the bunch is resized when it is more than half-occupied.] 
    */
    public int currentCapacity()
        { return capacity; }
    
    @Override
    public void add( Triple t )
        {
        keys[findSlot( t )] = t;
        changes += 1;
        if (++size > threshold) grow();
        }
    
    protected void grow()
        {
        Object [] oldContents = keys;
        final int oldCapacity = capacity;
        growCapacityAndThreshold();
        Object [] newKeys = keys = new Triple[capacity];
        for (int i = 0; i < oldCapacity; i += 1)
            {
            Triple t = (Triple) oldContents[i];
            if (t != null) newKeys[findSlot( t )] = t;
            }
        }
    
    @Override public void remove( Triple t )
        {
        super.remove( t );
        changes += 1;
        }
    
    @Override
    public ExtendedIterator<Triple> iterator()
        { return iterator( NotifyEmpty.ignore ); }
    
    @Override
    public ExtendedIterator<Triple> iterator( final NotifyEmpty container )
        { return keyIterator( container ); }
    
    @Override
    public void app( Domain d, StageElement next, MatchOrBind s )
        {
        int i = capacity, initialChanges = changes;
        while (i > 0)
            {
            if (changes > initialChanges) throw new ConcurrentModificationException();
            Object t = keys[--i];
            if (t != null  && s.matches( (Triple) t )) next.run( d );
            }
        }
    }

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
/*
    (c) Copyright 2005 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: HashedBunchMap.java,v 1.1 2005-10-26 07:48:27 chris-dollin Exp $
*/
package com.hp.hpl.jena.mem;

import java.util.Iterator;

import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
    An implementation of BunchMap that does open-addressed hashing.
    @author kers
*/
public class HashedBunchMap extends BunchMap
    {
    protected Object [] keys = new Object[10];
    protected Object [] values = new Object[10];
    protected int capacity = keys.length;
    protected int size = 0;
    protected int threshold = 7;
    
    public void clear()
        { for (int i = 0; i < capacity; i += 1) keys[i] = null; }

    protected final int initialIndexFor( Object key )
        { return (key.hashCode() & 0x7fffffff) % capacity; }
    
    protected int findSlot( Object key )
        {
        int index = initialIndexFor( key );
        while (true)
            {
            Object current = keys[index];
            if (current == null) return index;
            if (key.equals( current )) return ~index;
            index = (index == 0 ? capacity - 1 : index - 1);
            }
        }       
    
    public Object get( Object key )
        {
        int slot = findSlot( key );
        return slot < 0 ? values[~slot] : null;
        }

    public void put( Object key, Object value )
        {
        int slot = findSlot( key );
        if (slot < 0)
            {
            values[~slot] = value;
            }
        else
            {
            keys[slot] = key;
            values[slot] = value;
            size += 1;
            if (size == threshold) grow();
            }
        }

    protected void grow()
        {
        Object [] oldContents = keys, oldValues = values;
        final int oldCapacity = capacity;
        keys = new Object[capacity = computeNewCapacity()];
        values = new Object[capacity];
        for (int i = 0; i < oldCapacity; i += 1)
            {
            Object key = oldContents[i];
            if (key != null) 
                {
                int j = findSlot( key );
                keys[j] = key;
                values[j] = oldValues[i];
                }
            }
        }
    
    protected int computeNewCapacity()
        {
        threshold = (int) (capacity * 2 * 0.75);
        return capacity * 2;
        }
    
    public void remove( Object key )
        {
        int slot = findSlot( key );
        if (slot < 0) remove( ~slot );
        }
    
    protected void remove( int i )
        {
        while (true)
            {
            keys[i] = null;
            int j = i;
            while (true)
                {
                i = (i == 0 ? capacity - 1 : i - 1);
                Object key = keys[i];
                if (key == null) return;
                int r = initialIndexFor( key );
                if (!((i <= r && r < j) || (r < j && j < i) || (j < i && i <= r) )) break;
                }
            keys[j] = keys[i];
            values[j] = values[i];
            }
        }

    public Iterator keyIterator()
        {
        return new NiceIterator()
            {
            int index = 0;
            
            public boolean hasNext()
                {
                while (index < capacity && keys[index] == null) index += 1;
                return index < capacity;
                }
            
            public Object next()
                {
                if (hasNext() == false) throw new RuntimeException( "oops" );
                Object answer = keys[index];
                index += 1;
                return answer;
                }
            };
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
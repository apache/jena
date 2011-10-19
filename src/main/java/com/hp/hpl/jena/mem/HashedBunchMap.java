/*
    (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: HashedBunchMap.java,v 1.1 2009-06-29 08:55:55 castagna Exp $
*/
package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.shared.*;

/**
    An implementation of BunchMap that does open-addressed hashing.
    @author kers
*/
public class HashedBunchMap extends HashCommon<Object> implements BunchMap
    {
    protected TripleBunch [] values;
    
    public HashedBunchMap()
        {
        super( 10 );
        values = new TripleBunch[capacity];
        }

    @Override protected Object[] newKeyArray( int size )
        { return new Object[size]; }
    
    /**
        Clear this map: all entries are removed. The keys <i>and value</i> array 
        elements are set to null (so the values may be garbage-collected).
    */
    @Override
    public void clear()
        {
        size = 0;
        for (int i = 0; i < capacity; i += 1) keys[i] = values[i] = null; 
        }  
    
    @Override
    public long size()
        { return size; }
        
    @Override
    public TripleBunch get( Object key )
        {
        int slot = findSlot( key );
        return slot < 0 ? values[~slot] : null;
        }

    @Override
    public void put( Object key, TripleBunch value )
        {
        int slot = findSlot( key );
        if (slot < 0)
            values[~slot] = value;
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
        Object [] oldContents = keys;
        TripleBunch [] oldValues = values;
        final int oldCapacity = capacity;
        growCapacityAndThreshold();
        keys = newKeyArray( capacity );
        values = new TripleBunch[capacity];
        for (int i = 0; i < oldCapacity; i += 1)
            {
            Object key = oldContents[i];
            if (key != null) 
                {
                int j = findSlot( key );
                if (j < 0) 
                    {
                    throw new BrokenException( "oh dear, already have a slot for " + key  + ", viz " + ~j );
                    }
                keys[j] = key;
                values[j] = oldValues[i];
                }
            }
        }

    /**
        Called by HashCommon when a key is removed: remove
        associated element of the <code>values</code> array.
    */
    @Override protected void removeAssociatedValues( int here )
        { values[here] = null; }
    
    /**
        Called by HashCommon when a key is moved: move the
        associated element of the <code>values</code> array.
    */
    @Override protected void moveAssociatedValues( int here, int scan )
        { values[here] = values[scan]; }
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
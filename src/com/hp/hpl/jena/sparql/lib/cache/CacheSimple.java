/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lib.cache;

import java.util.Arrays;
import java.util.Iterator;

import com.hp.hpl.jena.sparql.lib.ActionKeyValue;
import com.hp.hpl.jena.sparql.lib.Cache;
import com.hp.hpl.jena.sparql.lib.iterator.Iter;
import com.hp.hpl.jena.sparql.lib.iterator.IteratorArray;

/**
 * A simple fixed size cache thatuses the hash code to address a slot.
 * Clash policy is to overwrite.
 * No object cretion during lookup or insert.
 */

public class CacheSimple<K,V> implements Cache<K,V>
{
    private final V[] values ; 
    private final K[] keys ;
    private final int size ;
    private int currentSize = 0 ;
    private ActionKeyValue<K,V> dropHandler = null ;
    
    public CacheSimple(int size)
    { 
        @SuppressWarnings("unchecked")
        V[] x =  (V[])new Object[size] ;
        values = x ;
        
        @SuppressWarnings("unchecked")
        K[]  z =  (K[])new Object[size] ;
        keys = z ;
        
        this.size = size ;
    }
    

    //@Override
    public void clear()
    { 
        Arrays.fill(values, null) ;
        Arrays.fill(keys, null) ;
        // drop handler
        currentSize = 0 ;
    }

    //@Override
    public boolean containsKey(K key)
    {
        return get(key) != null ;
    }

    // Return key index : -(index+1) if the key does not match
    private final int index(K key)
    { 
        int x = (key.hashCode()&0x7fffffff) % size ;
        if ( keys[x] != null && keys[x].equals(key) )
            return x ; 
        return -x-1 ;
    }
    
    private final int decode(int x)
    { 
        if ( x >= 0 ) return x ;
        
        // y = -x-1 ==> x = -y-1 
        return -x-1 ;
    }
    
    //@Override
    //public V getObject(K key, boolean exclusive)
    public V get(K key)
    {
        int x = index(key) ;
        if ( x < 0 )
            return null ; 
        return values[x] ;
    }

    //@Override
    public V put(K key, V thing)
    {
        int x = index(key) ;
        V old = null ;
        if ( x < 0 )
            // New.
            x = decode(x) ;
        else
        {
            old = values[x] ;
            if ( dropHandler != null )
                dropHandler.apply(keys[x], old) ;
            currentSize-- ;
        }
        
        values[x] = thing ;
        if ( thing == null )
            //put(,null) is a remove.
            keys[x] = null ;
        else
            keys[x] = key ;
        currentSize++ ;
        return old ;
    }

    //@Override
    public boolean remove(K key)
    {
        V old = put(key, null) ;
        return old != null ;
    }

    //@Override
    public long size()
    {
        return currentSize ;
    }

    //@Override
    public Iterator<K> keys()
    {
        Iterator<K> iter = IteratorArray.create(keys) ;
        return Iter.removeNulls(iter) ;
    }

    //@Override
    public boolean isEmpty()
    {
        return currentSize == 0 ;
    }

    /** Callback for entries when dropped from the cache */
    public void setDropHandler(ActionKeyValue<K,V> dropHandler)
    {
        this.dropHandler = dropHandler ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
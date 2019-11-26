/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.lib.cache;

import static java.util.Arrays.asList;

import java.util.Arrays ;
import java.util.Iterator ;
import java.util.concurrent.Callable ;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Cache ;


/**
 * A simple fixed size cache that uses the hash code to address a slot.
 * Clash policy is to overwrite.
 * No object creation during lookup or insert.
 */

public class CacheSimple<K,V> implements Cache<K,V>
{
    private final V[] values ; 
    private final K[] keys ;
    private final int size ;
    private int currentSize = 0 ;
    private BiConsumer<K,V> dropHandler = null ;
    
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
    

    @Override
    public void clear()
    { 
        Arrays.fill(values, null) ;
        Arrays.fill(keys, null) ;
        // drop handler
        currentSize = 0 ;
    }

    @Override
    public boolean containsKey(K key)
    {
        return getIfPresent(key) != null ;
    }

    // Return key index : -(index+1) if the key slot is empty.
    private final int index(K key)
    { 
        int x = (key.hashCode()&0x7fffffff) % size ;
        if (key.equals(keys[x]))
            return x ; 
        return -x-1 ;
    }
    
    private final int decode(int x)
    { 
        if ( x >= 0 ) return x ;
        return -x-1 ;
    }
    
    @Override
    public V getIfPresent(K key)
    {
        int x = index(key) ;
        if ( x < 0 )
            return null ; 
        return values[x] ;
    }

    @Override
    public V getOrFill(K key, Callable<V> callable) {
        return CacheOps.getOrFillSync(this, key, callable) ;
    }

    @Override
    public void put(K key, V thing)
    {
        int x = index(key) ; 
        x = decode(x) ;
        V old = values[x] ;
        // Drop the old K->V
        if ( old != null ) {
            if ( old.equals(thing) )
                // Replace like-with-like.
                return ;
            if ( dropHandler != null )
                dropHandler.accept(keys[x], old) ;
            currentSize-- ;
        }
        
        // Already decremented if we are overwriting a full slot.
        values[x] = thing ;
        if ( thing == null ) {
            //put(,null) is a remove.
            keys[x] = null ;
        } else {
            currentSize++ ;
            keys[x] = key ;
        }
    }

    @Override
    public void remove(K key)
    {
        put(key, null) ;
    }

    @Override
    public long size()
    {
        return currentSize ;
//        long x = 0 ;
//        for ( K key : keys )
//            if ( key != null )
//                x++ ;
//        return x ;
    }

    @Override
    public Iterator<K> keys()
    {
        Iterator<K> iter = asList(keys).iterator() ;
        return Iter.removeNulls(iter) ;
    }

    @Override
    public boolean isEmpty()
    {
        return currentSize == 0 ;
    }

    /** Callback for entries when dropped from the cache */
    @Override
    public void setDropHandler(BiConsumer<K,V> dropHandler)
    {
        this.dropHandler = dropHandler ;
    }
}

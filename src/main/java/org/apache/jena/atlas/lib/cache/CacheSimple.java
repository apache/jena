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

import java.util.Arrays ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorArray ;
import org.apache.jena.atlas.lib.ActionKeyValue ;
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
    
    @Override
    //public V getObject(K key, boolean exclusive)
    public V get(K key)
    {
        int x = index(key) ;
        if ( x < 0 )
            return null ; 
        return values[x] ;
    }

    @Override
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

    @Override
    public boolean remove(K key)
    {
        V old = put(key, null) ;
        return old != null ;
    }

    @Override
    public long size()
    {
        return currentSize ;
    }

    @Override
    public Iterator<K> keys()
    {
        Iterator<K> iter = IteratorArray.create(keys) ;
        return Iter.removeNulls(iter) ;
    }

    @Override
    public boolean isEmpty()
    {
        return currentSize == 0 ;
    }

    /** Callback for entries when dropped from the cache */
    @Override
    public void setDropHandler(ActionKeyValue<K,V> dropHandler)
    {
        this.dropHandler = dropHandler ;
    }
}

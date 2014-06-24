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

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.SingletonIterator ;
import org.apache.jena.atlas.lib.ActionKeyValue ;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.Lib ;




/** A one-slot cache.*/
public class Cache1<K, V> implements Cache<K,V>
{
    private ActionKeyValue<K, V> dropHandler = null ;
    private K cacheKey ;
    private V cacheValue ;
    
    public Cache1() { clear() ; }
    
    @Override
    public boolean containsKey(K key)
    {
        if ( cacheKey == null )
            return false ;
        return cacheKey.equals(key) ;
    }

    @Override
    public V get(K key)
    {
        if ( cacheKey == null ) return null ;
        if ( cacheKey.equals(key) ) return cacheValue ;
        return null ;
    }

    @Override
    public void clear()
    { 
        if ( cacheKey == null )
            return ;

        K k = cacheKey ;
        V v = cacheValue ;
        cacheKey = null ;
        cacheValue = null ;
        
        notifyDrop(k, v) ;
    }

    @Override
    public boolean isEmpty()
    {
        return cacheKey == null ;
    }

    @Override
    public Iterator<K> keys()
    {
        return new SingletonIterator<>(cacheKey) ;
    }

    @Override
    public V put(K key, V thing)
    {
        if ( Lib.equal(cacheKey, key) && Lib.equal(cacheValue, thing) )
            // No change.
            return cacheValue ;

        // Change
        K k = cacheKey ;
        V v = cacheValue ;
        // Displaces any existing cached key/value pair
        cacheKey = key ;
        cacheValue = thing ;
        notifyDrop(k, v) ;
        return v ;
    }

    @Override
    public boolean remove(K key)
    {
        if ( cacheKey == null ) return false ;
        
        if ( cacheKey.equals(key) )
        {
            clear() ;   // Will notify
            return true ;
        }
        return false ;
    }

    @Override
    public void setDropHandler(ActionKeyValue<K, V> dropHandler)
    {
        this.dropHandler = dropHandler ;
    }

    private void notifyDrop(K key, V thing)
    {
        if ( dropHandler != null && key != null )
            dropHandler.apply(key, thing) ;
    }
    
    @Override
    public long size()
    {
        return (cacheKey == null) ? 0 : 1 ;
    }

}

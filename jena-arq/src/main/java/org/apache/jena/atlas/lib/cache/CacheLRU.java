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

import org.apache.jena.atlas.lib.ActionKeyValue ;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;


/** This class is not thread-safe. Add a synchronization wrapper if needed ({@linkplain CacheFactory#createSync})  */

public class CacheLRU<K,V> implements Cache<K,V>
{
    // Use an internal class so we don't expose the full LinkedHashMap interface.
    private CacheImpl<K,V> cache ;
    
    public CacheLRU(float loadFactor, int maxSize)
    {
        this.cache = new CacheImpl<>(loadFactor, maxSize) ;
    }

    @Override
    public void clear()
    { cache.clear() ; }

    @Override
    public boolean containsKey(K key)
    {
        return cache.containsKey(key) ;
    }

    @Override
    public V get(K key)
    {
        return cache.get(key) ;
    }

    @Override
    public V put(K key, V thing)
    {
        return cache.put(key, thing) ;
    }

    @Override
    public boolean remove(K key)
    {
        V old = cache.remove(key) ;
        return old != null ;
    }

    @Override
    public long size()
    {
        return cache.size() ;
    }

    @Override
    // NB Access the iterator must be thread-aware. 
    public Iterator<K> keys()
    {
        return cache.keySet().iterator() ;
    }

    @Override
    public boolean isEmpty()
    {
        return cache.isEmpty() ;
    }

    /** Callback for entries when dropped from the cache */
    @Override
    public void setDropHandler(ActionKeyValue<K,V> dropHandler)
    {
        cache.setDropHandler(dropHandler) ;
    }
}

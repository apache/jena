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

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import org.apache.jena.atlas.lib.ActionKeyValue ;
import org.apache.jena.atlas.lib.Cache ;


public class CacheUnbounded<K,V> implements Cache<K,V>
{
    final Map<K,V> cache ;
    private ActionKeyValue<K, V> dropHandler ;
    
    public CacheUnbounded()
    {
        cache = new HashMap<>() ;
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
    public boolean isEmpty()
    {
        return cache.isEmpty() ;
    }

    @Override
    public Iterator<K> keys()
    {
        return cache.keySet().iterator() ;
    }

    @Override
    public V put(K key, V thing)
    {
        return cache.put(key, thing) ;
    }

    @Override
    public boolean remove(K key)
    {
        V value = cache.get(key) ;
        if ( value == null )
            return false ;
        
        cache.remove(key) ;
        dropHandler.apply(key, value) ;
        return true ;
    }

    @Override
    public void setDropHandler(ActionKeyValue<K, V> dropHandler)
    {
        this.dropHandler = dropHandler ;
    }

    @Override
    public long size()
    {
        return cache.size() ;
    }

}

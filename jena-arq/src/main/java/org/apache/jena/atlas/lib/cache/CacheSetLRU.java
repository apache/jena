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

import org.apache.jena.atlas.iterator.Action ;
import org.apache.jena.atlas.lib.ActionKeyValue ;
import org.apache.jena.atlas.lib.CacheSet ;


/** Cache set - tracks LRU of objects */
public class CacheSetLRU<T> implements CacheSet<T>
{
    //LinkHashSet does not have LRU support.
    static Object theOnlyValue = new Object() ;
    CacheImpl<T, Object> cacheMap = null ;
    
    public CacheSetLRU(int maxSize)
    {
        this(0.75f, maxSize) ;
    }
    
    public CacheSetLRU(float loadFactor, int maxSize)
    {
        cacheMap = new CacheImpl<>(loadFactor, maxSize) ;
    }

//    /** Callback for entries when dropped from the cache */
//    public void setDropHandler(Action<T> dropHandler)
//    {
//        cacheMap.setDropHandler(new Wrapper<T>(dropHandler)) ;
//    }
    
    // From map action to set action.
    static class Wrapper<T>  implements ActionKeyValue<T, Object>
    {
        Action<T> dropHandler ;
        public Wrapper(Action<T> dropHandler)
        { this.dropHandler = dropHandler ; }

        @Override
        public void apply(T key, Object value)
        { dropHandler.apply(key) ; }

    }
    
    @Override
    synchronized
    public void add(T e)
    {
        cacheMap.put(e, theOnlyValue) ;
    }


    @Override
    synchronized
    public void clear()
    { 
        cacheMap.clear() ;
    }


    @Override
    synchronized
    public boolean contains(T obj)
    {
        return cacheMap.containsKey(obj) ;
    }


    @Override
    synchronized
    public boolean isEmpty()
    {
        return cacheMap.isEmpty() ;
    }


//    public Iterator<T> iterator()
//    {
//        return cacheMap.keySet().iterator() ;
//    }


    @Override
    synchronized
    public void remove(T obj)
    {
        cacheMap.remove(obj);
    }


    @Override
    synchronized
    public long size()
    {
        return cacheMap.size() ;
    }

}

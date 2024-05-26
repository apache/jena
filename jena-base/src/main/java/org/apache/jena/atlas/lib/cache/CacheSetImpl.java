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

package org.apache.jena.atlas.lib.cache ;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheSet ;

/** Cache set */
public class CacheSetImpl<T> implements CacheSet<T> {
    static Object    theOnlyValue = new Object() ;
    Cache<T, Object> cacheMap     = null ;

    public CacheSetImpl(Cache<T, Object> cache) {
        // Use the characteristics of the cache used
        // e.g. the cache size and the eviction policy
        cacheMap = cache ;
    }

    @Override
    public void add(T e) {
        cacheMap.put(e, theOnlyValue) ;
    }

    @Override
    public void clear() {
        cacheMap.clear() ;
    }

    @Override
    public boolean contains(T obj) {
        return cacheMap.containsKey(obj) ;
    }

    @Override
    public boolean isEmpty() {
        return cacheMap.isEmpty() ;
    }

    public Iterator<T> iterator() {
        return cacheMap.keys() ;
    }

    @Override
    public void remove(T obj) {
        cacheMap.remove(obj) ;
    }

    @Override
    public long size() {
        return cacheMap.size() ;
    }

}

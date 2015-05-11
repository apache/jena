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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    /** Callback for entries when dropped from the cache */
    @Override
    public void setDropHandler(Consumer<T> dropHandler) {
        cacheMap.setDropHandler(new Wrapper<T>(dropHandler)) ;
    }
    
    // From map action to set action.
    static class Wrapper<T> implements BiConsumer<T, Object> {
        Consumer<T> dropHandler ;

        public Wrapper(Consumer<T> dropHandler) {
            this.dropHandler = dropHandler ;
        }

        @Override
        public void accept(T key, Object value) {
            dropHandler.accept(key) ;
        }

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

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
import java.util.function.Consumer;

import org.apache.jena.atlas.lib.CacheSet ;
import org.apache.jena.ext.com.google.common.collect.EvictingQueue;

/** Cache set */
public class CacheSetImpl<T> implements CacheSet<T> {
	
	private EvictingQueue<T> internalCache;
   
	/**
     * NO OP unless set by {@link #setDropHandler(Consumer)}
     */
	private Consumer<T> dropHandler = t -> {};

    public CacheSetImpl(int size) {
        this.internalCache = EvictingQueue.create(size);
    }

    /** Callback for entries when dropped from the cache */
    @Override
    public void setDropHandler(Consumer<T> dropHandler) {
        this.dropHandler = dropHandler ;
    }
    
    @Override
    public void add(T e) {
    		internalCache.add(e) ;
    }

    @Override
    public void clear() {
    		internalCache.clear() ;
    }

    @Override
    public boolean contains(T obj) {
        return internalCache.contains(obj) ;
    }

    @Override
    public boolean isEmpty() {
        return internalCache.isEmpty() ;
    }

    public Iterator<T> iterator() {
        return internalCache.iterator() ;
    }

    @Override
    public void remove(T obj) {
    		internalCache.remove(obj) ;
    		dropHandler.accept(obj) ;
    }

    @Override
    public long size() {
        return internalCache.size() ;
    }

}

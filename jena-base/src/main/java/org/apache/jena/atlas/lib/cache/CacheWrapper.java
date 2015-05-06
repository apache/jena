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
import java.util.concurrent.Callable ;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.lib.Cache ;



public class CacheWrapper<Key,T> implements Cache<Key,T>
{
    protected Cache<Key,T> cache ;
    
    public CacheWrapper(Cache<Key,T> cache)         { this.cache = cache ; }

    @Override
    public void clear()                             { cache.clear(); }

    @Override
    public boolean containsKey(Key key)             { return cache.containsKey(key) ; }
    
    @Override
    public T getIfPresent(Key key)                  { return cache.getIfPresent(key) ; }

    @Override
    public T getOrFill(Key key, Callable<T> callable)  { return cache.getOrFill(key, callable) ; }

    @Override
    public boolean isEmpty()                        { return cache.isEmpty() ; }

    @Override
    public Iterator<Key> keys()                     { return cache.keys(); }

    @Override
    public void put(Key key, T thing)               { cache.put(key, thing) ; }

    @Override
    public void remove(Key key)                     { cache.remove(key) ; }

    @Override
    public void setDropHandler(BiConsumer<Key, T> dropHandler)
    { cache.setDropHandler(dropHandler) ; }

    @Override
    public long size()                              { return cache.size() ; }

}

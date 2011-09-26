/**
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

package org.openjena.atlas.lib.cache;

import java.util.Iterator ;

import org.openjena.atlas.lib.ActionKeyValue ;
import org.openjena.atlas.lib.Cache ;


public class CacheSync<Key, T> implements Cache<Key, T>
{
    protected Cache<Key,T> cache ;
    
    public CacheSync(Cache<Key,T> cache)         { this.cache = cache ; }

    //@Override
    synchronized
    public void clear()                             { cache.clear(); }

    //@Override
    synchronized
    public boolean containsKey(Key key)             { return cache.containsKey(key) ; }
    
    //@Override
    //public V getObject(K key, boolean exclusive)    { return cache.getObject(key, exclusive) ; }
    synchronized
    public T get(Key key)                           { return cache.get(key) ; }

    //@Override
    synchronized
    public boolean isEmpty()                        { return cache.isEmpty() ; }

    //@Override
    synchronized
    public Iterator<Key> keys()                     { return cache.keys(); }

    //@Override
    synchronized
    public T put(Key key, T thing)                  { return cache.put(key, thing) ; }

    //@Override
    synchronized
    public boolean remove(Key key)                  { return cache.remove(key) ; }

    //@Override
    synchronized
    public void setDropHandler(ActionKeyValue<Key, T> dropHandler)
    { cache.setDropHandler(dropHandler) ; }

    //@Override
    synchronized
    public long size()                              { return cache.size() ; }

}

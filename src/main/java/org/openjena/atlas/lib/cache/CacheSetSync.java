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

import org.openjena.atlas.lib.CacheSet ;

public class CacheSetSync<T> implements CacheSet<T>
{
    private CacheSet<T> cache ;

    public CacheSetSync(CacheSet<T> cache){ this.cache = cache ; }
    
    //@Overview
    synchronized public void add(T e)               { cache.add(e) ; }

    //@Overview
    synchronized public void clear()                { cache.clear() ; }

    //@Overview
    synchronized public boolean contains(T obj)     { return cache.contains(obj) ; }

    //@Overview
    synchronized
    public boolean isEmpty()                        { return cache.isEmpty() ; }

    //@Overview
    synchronized
    public void remove(T obj)                       { cache.remove(obj) ; }

    //@Overview
    synchronized
    public long size()                              { return cache.size() ; } 
}

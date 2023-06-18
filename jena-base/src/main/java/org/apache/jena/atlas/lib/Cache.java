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

package org.apache.jena.atlas.lib;

import java.util.Iterator ;
import java.util.concurrent.Callable ;
import java.util.function.Function;

import org.apache.jena.atlas.lib.cache.CacheInfo;

/**
 * An abstraction of a cache for basic use.
 * <p>
 * For more complex configuration of the
 * cache, use the cache builder of the implementation of choice.
 *
 * @see CacheFactory for create caches.
 */
public interface Cache<Key, Value>
{
    /** Does the cache contain the key? */
    public boolean containsKey(Key key) ;

    /** Get from cache - or return null. */
    public Value getIfPresent(Key key) ;

    /** Get from cache; if not present, call the {@link Callable}
     *  to try to fill the cache. This operation should be atomic.
     *  @deprecated Use {@link #get(Object, Function)}
     */
    @Deprecated
    public default Value getOrFill(Key key, Callable<Value> callable) {
        return get(key, k->{
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Get from cache; if not present, call the {@link Function}
     *  to fill the cache slot. This operation should be atomic.
     */
    public Value get(Key key, Function<Key, Value> callable) ;

    /** Insert into the cache */
    public void put(Key key, Value thing) ;

    /** Remove from cache - return true if key referenced an entry */
    public void remove(Key key) ;

    /** Iterate over all keys. Iterating over the keys requires the caller be thread-safe. */
    public Iterator<Key> keys() ;

    public boolean isEmpty() ;
    public void clear() ;

    /** Current size of cache */
    public long size() ;

    /** Cache statistics (not supported by all caches) */
    public default CacheInfo stats() { return null; }
}

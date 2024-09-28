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
 * This cache does not support null as keys or values.
 * <p>
 * For more complex configuration of the
 * cache, use the cache builder of the implementation of choice.
 *
 * @see CacheFactory for create caches.
 */
public interface Cache<Key, Value>
{
    /**
     *  Does the cache contain the key?
     * @param key The key to find. The key must not be null.
     * @return True, if the cache contains the key, otherwise false.
     */
    public boolean containsKey(Key key) ;

    /**
     * Get from cache - or return null.
     * @param key The key for which the value is requested. The key must not be null.
     * @return If the cache contains an entry for the given key, the value is returned, otherwise null.
     */
    public Value getIfPresent(Key key) ;

    /** Get from cache; if not present, call the {@link Callable}
     *  to try to fill the cache. This operation should be atomic.
     *  The 'key' and 'callcable' must not be null.
     *  @deprecated Use {@link #get(Object, Function)}
     */
    @Deprecated(forRemoval = true)
    public default Value getOrFill(Key key, Callable<Value> callable) {
        return get(key, k->{
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get from cache; if not present, call the {@link Function}
     * to fill the cache slot. This operation should be atomic.
     * @param key The key, for which the value should be returned or calculated. The key must not be null.
     * @param callable If the cache does not contain the key, the callable is called to calculate a value.
     *                 If the callable returns null, the key is not associated with hat value,
     *                 as nulls are not accepted as values.
     *                 The callable must not be null.
     * @return Returns either the existing value or the calculated value.
     *         If callable is called and returns null, then null is returned.
     */
    public Value get(Key key, Function<Key, Value> callable) ;

    /**
     * Insert into the cache
     * @param key The key for the 'thing' to store. The key must not be null.
     * @param thing If 'thing' is null, it will not be used as value,
     *              instead any existing entry with the same key will be removed.
     */
    public void put(Key key, Value thing) ;

    /**
     * Remove from cache - return true if key referenced an entry
     * @param key The key, which shall be removed along with its value. The key must not be null.
     */
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

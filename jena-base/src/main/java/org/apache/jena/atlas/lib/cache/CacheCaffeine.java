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

package org.apache.jena.atlas.lib.cache;

import java.util.Iterator ;
import java.util.concurrent.Callable ;
import java.util.function.BiConsumer ;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;

import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.logging.Log ;

/** Wrapper around a com.github.benmanes.caffeine */
final public class CacheCaffeine<K,V> implements Cache<K, V>
{
    private static boolean WITH_STATS = false;

    private BiConsumer<K, V> dropHandler = null ;

    private com.github.benmanes.caffeine.cache.Cache<K,V> cache ;

    public CacheCaffeine(int size) {
        this(size, null);
    }

    public CacheCaffeine(int size, BiConsumer<K, V> dropHandler) {
        @SuppressWarnings("unchecked")
        Caffeine<K,V> builder = (Caffeine<K,V>)Caffeine.newBuilder()
            .maximumSize(size)
            .initialCapacity(size/2)
            // Eviction immediately using the caller thread.
            .executor(c->c.run());

        if ( dropHandler != null ) {
            RemovalListener<K,V> drop = (key, value, cause)-> {
                if ( dropHandler != null )
                    dropHandler.accept(key, value);
            };
            builder = builder.removalListener(drop);
        }
        if ( WITH_STATS )
            builder = builder.recordStats();
        cache = builder.build();
    }

    public CacheCaffeine(com.github.benmanes.caffeine.cache.Cache<K,V> caffeine) {
        cache = caffeine;
    }

    @Override
    public V getOrFill(K key, Callable<V> filler) {
        return cache.get(key, k->call(filler));
    }

    // Callable to function conversion.
    private static <X> X call(Callable<X> filler) {
        try {
            return filler.call();
        } catch (Exception e) {
            Log.warn(CacheCaffeine.class, "Execution exception filling cache", e) ;
            return null ;
        }
    }

    @Override
    public V get(K key, Function<K, V> f) {
        return cache.get(key, f);
    }

    @Override
    public V getIfPresent(K key) {
        return cache.getIfPresent(key) ;
    }

    @Override
    public void put(K key, V thing) {
		if (thing == null)
			cache.invalidate(key);
		else
			cache.put(key, thing);
    }

    @Override
    public boolean containsKey(K key) {
        return cache.getIfPresent(key) != null ;
    }

    @Override
    public void remove(K key) {
        cache.invalidate(key) ;
    }

    @Override
    public Iterator<K> keys() {
        return cache.asMap().keySet().iterator() ;
    }

    @Override
    public boolean isEmpty() {
        return cache.estimatedSize() == 0 ;
    }

    @Override
    public void clear() {
        cache.invalidateAll() ;
    }

    @Override
    public long size() {
        return cache.estimatedSize() ;
    }
}


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

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Cache;

/**
 * A simple fixed size cache that uses the hash code to address a slot.
 * The clash policy is to overwrite.
 * <p>
 * The cache has very low overhead - there is no object creation during lookup or insert.
 * <p>
 * This cache is not thread safe.
 */
public class CacheSimple<K, V> implements Cache<K, V> {
    private final V[] values;
    private final K[] keys;
    private final int size;
    private int currentSize = 0;
    private BiConsumer<K, V> dropHandler = null;

    public CacheSimple(int size) {
        this(size, null);
    }

    public CacheSimple(int size, BiConsumer<K, V> dropHandler) {
        @SuppressWarnings("unchecked")
        V[] x = (V[])new Object[size];
        values = x;

        @SuppressWarnings("unchecked")
        K[] z = (K[])new Object[size];
        keys = z;
        this.dropHandler = dropHandler;
        this.size = size;
    }

    @Override
    public void clear() {
        Arrays.fill(values, null);
        Arrays.fill(keys, null);
        // drop handler
        currentSize = 0;
    }

    @Override
    public boolean containsKey(K key) {
        Objects.requireNonNull(key);
        return index(key) >= 0 ;
    }

    // Return key index (>=0): return -(index+1) if the key slot is empty.
    private final int index(K key) {
        int x = (key.hashCode() & 0x7fffffff) % size;
        if ( key.equals(keys[x]) )
            return x;
        return -x - 1;
    }

    // Convert to a slot index.
    private final int decode(int x) {
        if ( x >= 0 )
            return x;
        return -(x+1);
    }

    @Override
    public V getIfPresent(K key) {
        Objects.requireNonNull(key);
        int x = index(key);
        if ( x < 0 )
            return null;
        return values[x];
    }

    @Override
    public V getOrFill(K key, Callable<V> callable) {
        return getOrFillNoSync(this, key, callable);
    }

    @Override
    public V get(K key, Function<K, V> function) {
        return getOrFillNoSync(this, key, function);
    }

    /**
     * Implementation of getOrFill based on Cache.get and Cache.put
     * This function is not thread safe.
     */
    public static <K,V> V getOrFillNoSync(Cache<K,V> cache, K key, Function<K,V> function) {
        V value = cache.getIfPresent(key) ;
        if ( value == null ) {
            try { value = function.apply(key) ; }
            catch (RuntimeException ex) { throw ex; }
            catch (Exception e) {
                throw new AtlasException("Exception on cache fill", e) ;
            }
            if ( value != null )
                cache.put(key, value) ;
        }
        return value ;
    }

    /**
     * Implementation of getOrFill based on Cache.get and Cache.put
     * This function is not thread safe.
     */
    public static <K,V> V getOrFillNoSync(Cache<K,V> cache, K key, Callable<V> callable) {
        V value = cache.getIfPresent(key) ;
        if ( value == null ) {
            try { value = callable.call() ; }
            catch (RuntimeException ex) { throw ex; }
            catch (Exception e) {
                throw new AtlasException("Exception on cache fill", e) ;
            }
            if ( value != null )
                cache.put(key, value) ;
        }
        return value ;
    }


    @Override
    public void put(K key, V thing) {
        // thing may be null.
        int x = index(key);
        x = decode(x);
        V old = values[x];
        // Drop the old K->V
        if ( old != null ) {
            if ( old.equals(thing) )
                // Replace like-with-like.
                return;
            if ( dropHandler != null )
                dropHandler.accept(keys[x], old);
            currentSize--;
            //keys[x] = null;
            //values[x] = null;
        }

        // Already decremented if we are overwriting a full slot.
        values[x] = thing;
        if ( thing == null ) {
            // put(,null) is a remove.
            keys[x] = null;
        } else {
            currentSize++;
            keys[x] = key;
        }
    }

    @Override
    public void remove(K key) {
        put(key, null);
    }

    @Override
    public long size() {
        return currentSize;
//        long x = 0;
//        for ( int i = 0 ; i < size ; i++ ) {
//            K key = keys[i];
//            if ( key != null )
//                x++;
//        }
//        return x;
    }

    @Override
    public Iterator<K> keys() {
        Iterator<K> iter = asList(keys).iterator();
        return Iter.removeNulls(iter);
    }

    @Override
    public boolean isEmpty() {
        return currentSize == 0;
    }
}

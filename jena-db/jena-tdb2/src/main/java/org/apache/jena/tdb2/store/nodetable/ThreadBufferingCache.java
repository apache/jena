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

package org.apache.jena.tdb2.store.nodetable;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.tdb2.TDBException;

/**
 * A cache that buffers changes.
 * <p>
 * It has two modes, when active it captures updates and the underlying main cache is
 * only updated when {@link #flushBuffer} is called. When not active, it passes
 * updates straight through.
 * <p>
 * For access operations, it looks in the buffered cache and the underlying cache as
 * well ({@code contains} and {@code get} operations but not {@code keys}).
 * <p>
 * The algorithm is utilising the fact that KV entries are not deleted once
 * committed. Abort causes them never to appear.
 * <p>
 * This is one thread that is the creator of new node ids. For all other threads, just
 * call through to the main cache. For the updating thread, any cache changes are
 * buffered, then pushed into the main cache on commit or discarded on abort.
 * The buffering is then cleared.
 */
public class ThreadBufferingCache<Key,Value> implements Cache<Key,Value> {
    private final Cache<Key,Value> localCache;
    private final Cache<Key,Value> baseCache;
    private final AtomicReference<Thread> bufferingThread = new AtomicReference<>();
    private Object lock = new Object();
    private String label;
    // This turns the feature off. Development only. Do not release with this set "false".
    private static final boolean BUFFERING = true;

    public ThreadBufferingCache(String label, Cache<Key,Value> mainCache, int size) {
        this.localCache = CacheFactory.createCache(size);
        this.baseCache = mainCache;
        this.label = label;
    }

    private boolean buffering() {
        if ( ! BUFFERING )
            return false;
        // Changes are sync'ed and the only way to change this value is via a sync'ed method.
        if ( bufferingThread == null )
            return false;
        Thread currentThread = Thread.currentThread();
        return bufferingThread.get() == currentThread;
    }

    private Cache<Key, Value> localCache() {
        return localCache;
    }

    // ---- Buffer management.
    // Only one thread can be using the additional caches.

    public void enableBuffering() {
        if ( ! BUFFERING )
            return;
        Thread thread = Thread.currentThread();
        boolean b = bufferingThread.compareAndSet(null, thread);
        if ( !b ) {
            throw new TDBException(Lib.className(this)+": already buffering");
        }
    }

    /** Write the local cache to the main cache, and reset the local cache. */
    public void flushBuffer() {
        if ( ! buffering() )
            return ;
        //System.out.println(label+": Flush:1 L: "+localCache().size());
        //System.out.println(label+": Flush:1 M: "+baseCache.size());

        localCache().keys().forEachRemaining(k->{
            Value value = localCache().getIfPresent(k);
            baseCache.put(k, value);
        });
        localCache().clear();
        //System.out.println(label+": Flush:2 L: "+localCache().size());
        //System.out.println(label+": Flush:2 M: "+baseCache.size());
        bufferingThread.set(null);
    }

    /** Drop the local cache. */
    public void dropBuffer() {
        if ( ! buffering() )
            return ;
        //System.out.println(label+": Drop: L: "+localCache().size());
        //System.out.println(label+": Drop: M: "+baseCache.size());
        localCache().clear();
        bufferingThread.set(null);
    }

    public Cache<Key, Value> getBuffer() {
        return localCache();
    }

    public Cache<Key, Value> getBaseCache() {
        return baseCache;
    }

    // --- getters with call-through

    @Override
    public boolean containsKey(Key key) {
        if ( ! buffering() )
            return baseCache.containsKey(key);
        return localCache().containsKey(key) || baseCache.containsKey(key);
    }

    @Override
    public Value getIfPresent(Key key) {
        if ( ! buffering() )
            return baseCache.getIfPresent(key);
        Value item = localCache().getIfPresent(key);
        if ( item != null )
            return item;
        return baseCache.getIfPresent(key);
    }

    @Override
    public Value getOrFill(Key key, Callable<Value> callable) {
        if ( ! buffering() )
            return baseCache.getOrFill(key, callable);
        // Not thread safe but this overlay cache is for single-thread use.
        Value item = localCache().getIfPresent(key);
        if ( item != null )
            return item;
        item = baseCache.getIfPresent(key);
        if ( item != null )
            return item;
        // Add to cache so new data hence place in localCache.
        try {
            item = callable.call();
            localCache().put(key, item);
        } catch (Exception ex) {
            throw new TDBException("Exception filling cache", ex);
        }
        return item;
    }

    // ---- Flush changes, reset.



    // ---- Updates to buffering, local cache.

    /** Goes into local cache. */
    @Override
    public void put(Key key, Value value) {
        if ( ! buffering() ) {
            baseCache.put(key, value);
            return ;
        }
        localCache().put(key, value);
    }

    @Override
    public void remove(Key key) {
        if ( ! buffering() ) {
            baseCache.remove(key);
            return ;
        }
        localCache().remove(key);
    }


    @Override
    public Iterator<Key> keys() {
        if ( ! buffering() )
            return baseCache.keys();
        return Iter.concat(localCache().keys(), baseCache.keys());
    }

    @Override
    public boolean isEmpty() {
        if ( ! buffering() )
            return baseCache.isEmpty();
        return localCache().isEmpty();
    }

    /** Clear local cache. */
    @Override
    public void clear() {
        if ( ! buffering() ) {
            baseCache.clear();
            return ;
        }
        localCache().clear();
    }

    /** Size of local cache */
    @Override
    public long size() {
        if ( ! buffering() )
            return baseCache.size();
        return localCache().size();
    }

    @Override
    public void setDropHandler(BiConsumer<Key, Value> dropHandler) {
        if ( ! buffering() )
            return ;
       localCache().setDropHandler(dropHandler);
    }
}

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

// Kept for reference.
class CacheGuava<K,V> {
    private CacheGuava() {}
}

//import java.util.Iterator ;
//import java.util.concurrent.Callable ;
//import java.util.concurrent.ExecutionException ;
//import java.util.function.BiConsumer ;
//import java.util.function.Function;
//
//import com.google.common.cache.CacheBuilder ;
//import com.google.common.cache.RemovalListener ;
//
//import org.apache.jena.atlas.lib.Cache ;
//import org.apache.jena.atlas.logging.Log ;
//
///** Wrapper around a com.google.common.cache */
//final public class CacheGuava<K,V> implements Cache<K, V>
//{
//    private BiConsumer<K, V> dropHandler = null ;
//
//    private com.google.common.cache.Cache<K,V> cache ;
//
//    public CacheGuava(int size) {
//        this(size, null);
//    }
//
//    public CacheGuava(int size, BiConsumer<K, V> dropHandler) {
//        @SuppressWarnings("unchecked")
//        CacheBuilder<K,V> builder = (CacheBuilder<K,V>)CacheBuilder.newBuilder()
//            .maximumSize(size)
//            .recordStats()
//            .concurrencyLevel(8);
//        if ( dropHandler != null ) {
//            RemovalListener<K,V> drop = (notification)-> {
//                dropHandler.accept(notification.getKey(),
//                                   notification.getValue()) ;
//            };
//           builder = builder.removalListener(drop);
//        }
//        cache = builder.build();
//    }
//
//    public CacheGuava(com.google.common.cache.Cache<K,V> guavaCache) {
//        this.cache = guavaCache;
//    }
//
//    @Override
//    public V getOrFill(K key, Callable<V> filler) {
//        try {
//            return cache.get(key, filler) ;
//        }
//        catch (ExecutionException e) {
//            Log.warn(CacheGuava.class, "Execution exception filling cache", e) ;
//            return null ;
//        }
//    }
//
//    @Override
//    public V get(K key, Function<K, V> callable) {
//        return getOrFill(key, ()->callable.apply(key));
//    }
//
//    @Override
//    public V getIfPresent(K key) {
//        return cache.getIfPresent(key) ;
//    }
//
//    @Override
//    public void put(K key, V thing) {
//		if (thing == null)
//			cache.invalidate(key);
//		else
//			cache.put(key, thing);
//    }
//
//    @Override
//    public boolean containsKey(K key) {
//        return cache.getIfPresent(key) != null ;
//    }
//
//    @Override
//    public void remove(K key) {
//        cache.invalidate(key) ;
//    }
//
//    @Override
//    public Iterator<K> keys() {
//        return cache.asMap().keySet().iterator() ;
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return cache.size() == 0 ;
//    }
//
//    @Override
//    public void clear() {
//        cache.invalidateAll() ;
//    }
//
//    @Override
//    public long size() {
//        return cache.size() ;
//    }
//}

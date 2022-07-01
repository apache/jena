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

import java.util.concurrent.Callable ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.lib.Cache ;

/** Support operations for Cache functions */ 
class CacheOps {
    
    /** Implementation of getOrFill based on Cache.get and Cache.put */ 
    public static <K,V> V getOrFill(Cache<K,V> cache, K key, Callable<V> callable) {
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

    /** Thread safe implementation of getOrFill based on Cache.get and Cache.put */ 
    public static <K,V> V getOrFillSync(Cache<K,V> cache, K key, Callable<V> callable) {
        synchronized(cache) {
            return getOrFill(cache, key, callable) ;
        }
    }
}

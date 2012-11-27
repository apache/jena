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

import org.apache.jena.atlas.lib.Cache ;

/** Cache that takes a {@link Getter} to automatically fill cache misses. */

public class CacheWithGetter<K,V> extends CacheWrapper<K, V>
{
    Getter<K,V> getter ;
    
    public CacheWithGetter(Cache<K,V> cache, Getter<K,V> getter)
    {
        super(cache) ;
        this.getter = getter ;
    }
    
    @Override
    public V get(K key)
    { 
        V object = super.get(key) ;
        if ( object == null && getter != null )
        {
            object = getter.get(key) ;
            if ( object != null )
                cache.put(key, object) ;
        }
        return object ;
    }
}

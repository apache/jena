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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class Registry<K,T>
{
    protected Map<K, T> registry = new ConcurrentHashMap<>();

    public Registry() {}

    public void put(K key, T value)     { registry.put(key, value); }
    public T get(K key)                 { return registry.get(key); }
    public boolean isRegistered(K key)  { return registry.containsKey(key); }
    public void remove(K key)           { registry.remove(key); }
    public Collection<K> keys()         { return registry.keySet(); }
    public int size()                   { return registry.size(); }
    public boolean isEmpty()            { return registry.isEmpty(); }
    public void clear()                 { registry.clear(); }
    public void forEach(BiConsumer<K, T> action) {
        registry.forEach(action);
    }
}

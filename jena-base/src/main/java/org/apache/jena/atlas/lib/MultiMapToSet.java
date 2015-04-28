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

package org.apache.jena.atlas.lib;

import java.util.Collection ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Iter ;

public class MultiMapToSet<K,V> extends MultiMap<K,V> {
    public static <K, V> MultiMapToSet<K, V> create() { return new MultiMapToSet<>() ; }
    
    @Override
    protected Collection<V> createCollection()
    {
        return new HashSet<>() ;
    }
    
    @Override
    public Set<V> get(K key) { return (Set<V>)getByKey(key) ; }
    
    @Override
    public Set<V> values(K key) { return (Set<V>)valuesForKey(key); }

    @Override
    public Set<V> values() { return Iter.toSet(flatten()) ; }


}


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

package org.openjena.atlas.lib;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Set ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.IteratorConcat ;

public abstract class MultiMap<K, V>
{
    // size
    // clear
    // remove
    // putAll
    // values() 
    
    // Set vs List here.
    private Map<K, Collection<V>> map = new HashMap<K, Collection<V>>() ;

    protected abstract Collection<V> create() ;
    
    static class MultiMapToList<K,V> extends MultiMap<K,V> {
        @Override
        protected Collection<V> create()
        {
            return new ArrayList<V>() ;
        }}
    
    static class MultiMapToSet<K,V> extends MultiMap<K,V> {
        @Override
        protected Collection<V> create()
        {
            return new HashSet<V>() ;
        }}
    
    public static <K, V> MultiMap<K, V> createMapList() { return new MultiMapToList<K, V>() ; }
    public static <K, V> MultiMap<K, V> createMapSet() { return new MultiMapToSet<K, V>() ; }
    
    protected MultiMap() { }
    
    public Collection<V> get(K key) { 
        return map.get(key) ; } 
    public void put(K key, V value)
    { 
        Collection<V> x = map.get(key) ;
        if ( x == null )
        {
            x = create() ;
            map.put(key, x) ;
        }
        x.add(value) ;
    }
    
    public Collection<V> values(K key) { return map.get(key); }
    public Collection<V> values() { return Iter.toList(flatten()) ; }

    public boolean containsKey(K key) { return map.containsKey(key) ; }
    
    //public boolean containsValue(V Value) { return map.containsKey(key) ; }
    
    public Set<K> keys() { return map.keySet() ; }
    
    public boolean isEmpty() { return map.isEmpty() ; }

    /** Does not materialise the contents */
    public Iterator<V> flatten()
    {
        IteratorConcat<V> all = new IteratorConcat<V>() ;
        for ( K k : map.keySet() )        
        {
            Collection<V> x =  map.get(k) ;
            all.add(x.iterator()) ;
        }
        return all ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof MultiMap<?,?> ) ) return true ;
        @SuppressWarnings("unchecked")
        MultiMap<K,V> mmap = (MultiMap<K,V>)other ;
        return map.equals(mmap.map) ;
    }
    
    @Override
    public int hashCode()       { return map.hashCode()^ 0x01010101 ; }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder() ;
        sb.append("{ ") ;
        boolean firstKey = true ;
        for ( K key : keys() )
        {
            if ( ! firstKey )
                sb.append(", ") ;
            firstKey = false ;
            sb.append(key) ;
            sb.append(" [") ;
            for ( V value : values(key) )
            {
                sb.append(" ") ;
                sb.append(value) ;
            }
            sb.append(" ] ") ;
        }
        sb.append("}") ;
        return sb.toString() ;
    }
}

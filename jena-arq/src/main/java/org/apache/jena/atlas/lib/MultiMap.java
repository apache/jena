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

import java.util.* ;

import org.apache.jena.atlas.iterator.IteratorConcat ;

/* Map from K to collection of V */

public abstract class MultiMap<K, V>
{
    private Map<K, Collection<V>> map = new HashMap<>() ;

    protected abstract Collection<V> createCollection() ;
    
    public static <K, V> MultiMapToList<K, V> createMapList() { return new MultiMapToList<>() ; }
    public static <K, V> MultiMapToSet<K, V> createMapSet() { return new MultiMapToSet<>() ; }
    
    protected MultiMap() { }
    
    protected Collection<V> getByKey(K key) { 
        return map.get(key) ; 
    } 

    public abstract Collection<V> get(K key) ; 
    
    public V getOne(K key) { 
        Collection<V> c = map.get(key) ;
        if ( c == null || c.size() == 0 ) 
            return null ;
        return c.iterator().next() ;
    }
    
    public void putAll(K key, @SuppressWarnings("unchecked") V ... values)
    {
        for ( V v : values)
            put(key, v) ;
    }
    
    public void put(K key, V value)
    { 
        Collection<V> x = map.get(key) ;
        if ( x == null )
        {
            x = createCollection() ;
            map.put(key, x) ;
        }
        x.add(value) ;
    }
    
    public void remove(K key, V value)  { map.get(key).remove(value) ; }
    public void removeKey(K key)        { map.remove(key) ; }
    
    protected Collection<V> valuesForKey(K key) { return map.get(key); }
    public abstract Collection<V> values(K key) ;
    public abstract Collection<V> values() ;

    public boolean containsKey(K key) { return map.containsKey(key) ; }
    
    public Set<K> keys()        { return map.keySet() ; }
    
    public void clear()         { map.clear() ; }
    
    public boolean isEmpty()    { return map.isEmpty() ; }

    /** Does not materialise the contents */
    public Iterator<V> flatten()
    {
        IteratorConcat<V> all = new IteratorConcat<>() ;
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
            sb.append(" => [") ;
            boolean firstValue = true ; 
            for ( V value : values(key) )
            {
                if ( firstValue )
                    sb.append(" ") ;
                else
                    sb.append(", ") ;
                sb.append(value) ;
                firstValue = false ;
            }
            sb.append(" ] ") ;
        }
        sb.append("}") ;
        return sb.toString() ;
    }
}

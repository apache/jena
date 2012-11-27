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


import java.util.Iterator ;
import java.util.Map ;

import org.apache.jena.atlas.iterator.Iter ;


/**
 * A map with parent sharing. New entries go into a map but lookup is passed to
 * the parent map if nothing is found at this level.
 */
public class Map2<K, V> implements Iterable<K>
{
    private final Map<K, V> map1 ; 
    private final Map2<K, V> map2 ;

    public Map2(Map<K,V> map1, Map2<K,V> map2)
    {
        this.map1 = map1 ;
        this.map2 = map2 ;
    }
    
    public boolean containsKey(K key)
    {
        if ( map1.containsKey(key) )
             return true ;
        if ( map2 != null )
            return map2.containsKey(key) ;
        return false;
    }

//    public boolean containsValue(V value)
//    {
//        if ( map1.containsValue(value) )
//            return true ;
//        if ( map2 != null )
//            return map2.containsValue(value) ;
//        return false;
//    }

    public V get(K key)
    {
        V v = map1.get(key) ;
        if ( v != null ) return v ;
        if ( map2 != null )
            return map2.get(key) ;
        return null ;
    }

    public void put(K key, V value)
    {
        if ( map2 != null && map2.containsKey(key) )
            throw new IllegalArgumentException("Parent map already contains "+key) ;
        map1.put(key, value) ;
    }

    // The keys.
    @Override
    public Iterator<K> iterator()
    {
        Iter<K> iter1 = Iter.iter(map1.keySet().iterator()) ;
        if ( map2 == null )
            return iter1 ; 
        return iter1.append(map2.iterator()) ;
    }
    
    public boolean isEmpty()
    {
        boolean x = map1.isEmpty() ;
        if ( ! x ) return false ;
        if ( map2 != null )
            return map2.isEmpty() ;
        return true ;
    }
    
    public int size()
    {
        int x = map1.size() ;
        if ( map2 != null )
            x += map2.size();
        return x ;
    }
    
}

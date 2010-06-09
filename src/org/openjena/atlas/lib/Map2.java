/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;


import java.util.Iterator;
import java.util.Map;

import org.openjena.atlas.iterator.Iter ;


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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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
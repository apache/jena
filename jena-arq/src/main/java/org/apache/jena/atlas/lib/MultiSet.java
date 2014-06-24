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

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.NoSuchElementException ;

/** A MultiSet - also known as a Bag
 */

public class MultiSet<T> implements Iterable<T>
{
    private Map<T,RefLong> map   = new HashMap<>() ;
    private int multiSetSize = 0 ;
    
    private RefLong _get(T obj)
    {
        RefLong z = map.get(obj) ;
        if ( z == null )
        {
            z = new RefLong(0) ;
            map.put(obj, z) ;
        }
        return z ;
    }
 
    /** Does it contain any elements at all? */
    public boolean isEmpty()        { return map.isEmpty() ; }

    /** Does it contain the object? */
    public boolean contains(T obj)  { return map.containsKey(obj) ; }
    
    /** Yiled one object per element (i.e without counts) */
    public Iterator<T> elements()   { return map.keySet().iterator() ; }

    /** Add an object */
    public void add(T obj)          { _get(obj).inc(); multiSetSize++ ; } 

    /** Add an object, with cardinality n */
    public void add(T obj, long n)
    { 
        if ( n <= 0 ) return ;
        _get(obj).add(n) ;
        multiSetSize += n ;
    }
    
    /** Remove one occurrence of the object from the multiset */
    public void remove(T obj)
    {
        RefLong x = map.get(obj) ;
        if ( x == null ) return ;
        x.dec() ;
        multiSetSize-- ;
        if ( x.value() == 0 )
            map.remove(obj) ;
    }
    
    /** Remove N occurrences of the object from the multiset */
    public void remove(T obj, long n)
    {
        RefLong x = map.get(obj) ;
        if ( x == null ) return ;
        long z = x.value() ;
        if ( z < n )
            n = z ;
        x.subtract(n) ;
        multiSetSize -= n ;
        if ( x.value() <= 0 )
            map.remove(obj) ;
    }    
    

    /** Remove all occurrences of the object in themultiset */
    public void removeAll(T obj)
    {
        RefLong x = map.get(obj) ;
        if ( x == null )
            return ;
        multiSetSize -= x.value() ;
        map.remove(obj) ;
    }

    /* Remove everything */
    public void clear() { map.clear() ; multiSetSize = 0 ; }
    
    
    /** Get the count of the number of times the object appears in the multiset - i.e. it's cardinality.
     * Returns zero when not present.
     */
    public long count(T obj)
    {
        if ( ! map.containsKey(obj) ) return 0 ;
        return map.get(obj).value() ;
    }
    
    public int size()
    {
//        int count = 0 ;
//        for ( Map.Entry<T, RefLong> e : map.entrySet() )
//            count += e.getValue().value() ;
//        //return count ;
//        if ( count != multiSetSize )
//        {
//            Log.warn(this, "Mismatch") ;
//            return count ; 
//        }

        return multiSetSize ;
    }
    
    private Iterator<T> iterator1()
    {
        // CRUDE
        List<T> expanded = new ArrayList<>() ;
        for ( Map.Entry<T, RefLong> e : map.entrySet() )
        {
            for ( int i = 0 ; i < e.getValue().value() ; i++ )
                expanded.add(e.getKey()) ;
        }
        
        return expanded.iterator() ;
    }
    
    @Override
    public Iterator<T> iterator()
    {
        return new Iterator<T>() {
            
            Iterator<T> keys = map.keySet().iterator() ;
            T key = null ;
            long keyCount = 0 ;
            T slot = null ;
            
            @Override
            public boolean hasNext()
            {
                if ( slot != null )
                    return true ;
                
                if ( keys == null ) 
                    return false ;
                
                if ( key != null )
                {
                    if ( keyCount < count(key) )
                    {
                        keyCount++ ;
                        slot = key ;
                        return true ;
                    }
                    // End of this key.
                    key = null ;
                }
                    
                if ( keys.hasNext() )
                {
                    key = keys.next() ;
                    keyCount = 1 ;
                    slot = key ;
                    return true ;
                }
                keys = null ;
                return false ;
            }

            @Override
            public T next()
            {
                if ( ! hasNext() ) throw new NoSuchElementException() ;
                T x = slot ;
                slot = null ;
                return x ;
            }

            @Override
            public void remove()
            { throw new UnsupportedOperationException() ; }
        } ; 
    }
    
    @Override 
    public String toString()
    {
        StringBuilder sb = new StringBuilder() ;
        sb.append("{") ;
        String sep = "" ;
        for ( Map.Entry<T, RefLong> e : map.entrySet() )
        {
            sb.append(sep) ;
            sep = ", " ;
            sb.append(e.getKey().toString()) ;
            sb.append("=") ;
            sb.append(Long.toString(e.getValue().value())) ;
        }
        sb.append("}") ;
        return sb.toString() ;
    }
}

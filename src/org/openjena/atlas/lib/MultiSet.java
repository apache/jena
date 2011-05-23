/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

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
    private Map<T,RefLong> map   = new HashMap<T,RefLong>() ;
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
    
    //@Override
    private Iterator<T> iterator1()
    {
        // CRUDE
        List<T> expanded = new ArrayList<T>() ;
        for ( Map.Entry<T, RefLong> e : map.entrySet() )
        {
            for ( int i = 0 ; i < e.getValue().value() ; i++ )
                expanded.add(e.getKey()) ;
        }
        
        return expanded.iterator() ;
    }
    
    //@Override
    public Iterator<T> iterator()
    {
        return new Iterator<T>() {
            
            Iterator<T> keys = map.keySet().iterator() ;
            T key = null ;
            long keyCount = 0 ;
            T slot = null ;
            
            //@Override
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

            //@Override
            public T next()
            {
                if ( ! hasNext() ) throw new NoSuchElementException() ;
                T x = slot ;
                slot = null ;
                return x ;
            }

            //@Override
            public void remove()
            { throw new UnsupportedOperationException() ; }
        } ; 
    }
    
    @Override 
    public String toString()
    {
        StringBuilder sb = new StringBuilder() ;
        String sep = "" ;
        for ( Map.Entry<T, RefLong> e : map.entrySet() )
        {
            sb.append(sep) ;
            sep = ", " ;
            sb.append(e.getKey().toString()) ;
            sb.append("=") ;
            sb.append(Long.toString(e.getValue().value())) ;
        }
        
        return sb.toString() ;
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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
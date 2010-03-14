/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.IteratorArray ;
import org.openjena.atlas.iterator.Transform ;



/** Tuple class - tuples are immutable and must be created initialized */
public final class Tuple<T> implements Iterable<T>
{
    // Or use an ArrayList<T>
    
//    public static <X> Tuple<X> blankTuple(Class<X> clazz, int capacity)
//    {
//        @SuppressWarnings("unchecked")
//        X[] tuple = (X[])Array.newInstance(clazz,capacity);
//        return Tuple.create(tuple) ;
//    }
    
    public static <X> Tuple<X> create(X ... elements)
    { return new Tuple<X>(elements) ; }
    
    
   //TupleLib??
    public static <T> Iterator<T> project(final int slot, Iterator<Tuple<T>> iter)
    {
        Transform<Tuple<T>, T> projection = new Transform<Tuple<T>, T>(){
            //@Override
            public T convert(Tuple<T> tuple)
            {
                return tuple.get(slot) ;
            }
        } ;
        return Iter.map(iter, projection) ;
    }
    
    // May need to be Object[] (+ cast on access)
    // to support 
    final T[] tuple ;
    
    private Tuple(T...tuple)
    {
        this.tuple = tuple ;
    }
    
    public T get(int idx) { return tuple[idx] ; }
    //public void set(int idx, T elt) { tuple[idx] = elt ; }
    
    public int countNotNull()
    {
        int x = 0 ;
        for ( T item : tuple )
            if ( item != null ) x++ ;
        return x ;
    }

    public List<T> asList() { return Arrays.asList(tuple) ; }
    
    public T[] tuple() { return tuple ; }
    
    public T[] tupleCopy()
    { 
        return ArrayUtils.copy(tuple) ;
    }

    //@Override
    public Iterator<T> iterator()
    {
        return IteratorArray.create(tuple) ;
    }

    /** Return a tuple with the column mapping applied */
    public Tuple<T> map(ColumnMap colMap)
    {
        return colMap.map(this) ;
    }
    
    /** Return a tuple with the column mapping reversed */
    public Tuple<T> unmap(ColumnMap colMap)
    {
        return colMap.unmap(this) ;
    }
    
    public int size() { return tuple.length ; }
    
    @Override
    public int hashCode()
    { 
        int x = 99 ;
        for ( T n : tuple )
        {
            if ( n != null )
                x = x<<1 ^ n.hashCode() ;
        }
        return x ;  
    }
    
    /** Equality of tuples is based on equality of the elements in the tuple */
    @Override
    public boolean equals(Object other) 
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof Tuple<?> ) )
            return false ;
        Tuple<?> x = (Tuple<?>)other ;
        if ( x.size() != this.size() )
            return false ;
        for ( int i = 0 ; i < tuple.length ; i++ )
        {
            Object obj1 = tuple[i] ;
            Object obj2 = x.tuple[i] ;
            if ( ! Lib.equals(obj1, obj2) )
                return false ;
        }
        return true ; 
    }
    
    @Override
    public String toString()
    {
        return "["+Iter.asString(this, ", ")+"]" ;
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
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

package org.openjena.atlas.lib;


import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.IteratorArray ;
import org.openjena.atlas.iterator.Transform ;

/** Tuple class - tuples are immutable and must be created initialized */
public final class Tuple<T> implements Iterable<T>
{
    // Interface this.
    // Classes: TupleImpl, TupleSlice
    
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
            @Override
            public T convert(Tuple<T> tuple)
            {
                return tuple.get(slot) ;
            }
        } ;
        return Iter.map(iter, projection) ;
    }

    public static <T> Iterator<Tuple<T>> prefix(final int prefixLength, Iterator<Tuple<T>> iter)
    {
        Transform<Tuple<T>, Tuple<T>> sub = new Transform<Tuple<T>, Tuple<T>>(){
            @Override
            public Tuple<T> convert(Tuple<T> tuple)
            {
                T[] x = ArrayUtils.copy(tuple.tuple,0, prefixLength) ;
                return Tuple.create(x) ;
            }
        } ;
        return Iter.map(iter, sub) ;
    }

    // Alternative : Object[] (+ cast on access)
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

    @Override
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
            if ( ! Lib.equal(obj1, obj2) )
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

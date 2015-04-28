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

package org.apache.jena.atlas.iterator;

import java.util.Iterator ;
import java.util.NoSuchElementException ;

/** Iterator over a Java base array */
public final class IteratorArray<T> implements Iterator<T>
{
    /** Iterator over all the array elements */ 
    public static <T> IteratorArray<T> create(T[] array)
    { return new IteratorArray<>(array, 0, array.length) ; }
    
    /** Iterator over array elements from start (inclusive) to finish (exclusive) */ 
    public static <T> IteratorArray<T> create(T[] array, int start, int finish)
    { return new IteratorArray<>(array, start, finish) ; }
    
    private int idx ;
    private int finishIdx ;
    private T[] array ;
    
    private IteratorArray(T[] array, int start, int finish) 
    {
        if ( start < 0 )
            throw new IllegalArgumentException("Start: "+start) ;

        if ( start > finish )
            throw new IllegalArgumentException("Start >= finish: "+start+" >= "+finish) ;

// Instead: truncate to array length          
//        if ( finish > array.length )
//            throw new IllegalArgumentException("Finish outside array") ;
//        
// Instead: immediate end iterator                
//        if ( start >= array.length )
//            throw new IllegalArgumentException("Start outside array") ;

        this.array = array ;
        idx = start ;
        finishIdx = finish ;
        if ( idx < 0 )
            idx = 0 ;
        if ( finishIdx > array.length ) 
            finishIdx = array.length ;
    }

    @Override
    public boolean hasNext()
    {
//        if ( idx < 0 )
//            return false ;
        if ( idx >= finishIdx )
            return false ;
        return true ;
    }

    public T current()
    {
        if ( idx >= finishIdx )
            throw new NoSuchElementException() ;
        return array[idx] ;
    }
    
    @Override
    public T next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException() ; 
        return array[idx++] ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException("ArrayIterator") ; }
}

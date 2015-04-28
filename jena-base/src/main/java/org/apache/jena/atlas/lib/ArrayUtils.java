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

import java.lang.reflect.Array ;
import java.util.Arrays ;

/** Collection of array-related operations */
public class ArrayUtils
{
    private ArrayUtils() {}
    
    /** Allocate an array of generic type T (initialized to null) */
    @SuppressWarnings("unchecked")
    public static <T> T[] alloc(Class<T> cls, int n)
    {
        return (T[])Array.newInstance(cls, n) ;
    }
    
 // Compiles but fails at runtime (class cast exception if the reuls is assigned or accessed)
//        @SuppressWarnings("unchecked")
//        T[] array = (T[])new Object[n] ;
// or is T known 
//        @SuppressWarnings("unchecked")
//        Set<T> x[] = new Set[length] ;
//        return array ;
        
    /** Allocation space and copy */ 
    public static <T> T[] copy(T[] array)
    {
        return copy(array, 0, array.length) ;
    }
    
    /** Allocation space and copy */ 
    public static <T> T[] copy(T[] array, int start, int finish)
    {    
        return Arrays.copyOfRange(array, start, finish) ;

//        // Java5.
//        // Fails for arrays of length 0;
//        if ( array.length <= start )
//            throw new IllegalArgumentException("Zero length array not supported") ;
//        @SuppressWarnings("unchecked")
//        T[] array2 = (T[])Array.newInstance(array[start].getClass(), finish-start) ;
//        System.arraycopy(array, start, array2, 0, finish-start) ;
//        return array2 ;
    }
}

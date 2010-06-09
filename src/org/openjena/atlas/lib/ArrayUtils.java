/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.lang.reflect.Array ;

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
        // *** Java6.
        //return Arrays.copyOfRange(array, start, finish) ;

        // Java5.
        // Fails for arrays of length 0;
        if ( array.length <= start )
            throw new IllegalArgumentException("Zero length array not supported") ;
        @SuppressWarnings("unchecked")
        T[] array2 = (T[])Array.newInstance(array[start].getClass(), finish-start) ;
        System.arraycopy(array, start, array2, 0, finish-start) ;
        return array2 ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
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
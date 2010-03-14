/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.nio.IntBuffer;
import java.util.Comparator;
import java.util.List;

public class Alg
{
    // Linear search is really there to test binary search.
    static int linearSearch(IntBuffer buff, int key)
    { 
        return linearSearch(buff, 0, buff.limit(), key) ;
    }

    static int linearSearch(IntBuffer buff, int low, int high, int key)
    {
        int len = buff.limit(); // In int units.
        check(len, low, high) ;
        for ( int i = low ; i < high ; i++ )
        {
            int k2 = buff.get(i) ;
            if ( k2 == key )
                return i ;
            if ( k2 > key )
                return encodeIndex(i) ;
        }
        return encodeIndex(high) ;
    }
    
    // The encoded offset (insertion point) when not found.
    public static final int encodeIndex(int i) { return -(i+1) ; } 
    public static final int decodeIndex(int i) { return -(i+1) ; } 

    
    /* Both Arrays and Collections have a binary search implementation.
     * Arrays searches Object[] arrays, and Collections searches List<T>
     *
     * But sometime things are not so easy, and it isn't neat as to
     * what is being searched, like a slice of an NIO Buffer
     * 
     * http://en.wikipedia.org/wiki/Binary_search
     */
    
    public static int binarySearch(IntBuffer buff, int value)
    {
        return  binarySearch(buff, 0, buff.limit(), value) ;
    }
    
    public static int binarySearch(IntBuffer buff, int low, int high, int value)
    {
        // Low is inclusive, high is exclusive.
        check(buff.limit(), low, high) ;
        high -- ;   // Convert high to inclusive.

        // Non-tail-recursive form, because tail-recursion removal
        // is not required by java (unlike scheme).
        
        while (low <= high)
        {
            int mid = (low + high) >>> 1 ;  // int divide by 2 : better: mid = low + ((high - low) / 2)
            int k = buff.get(mid) ;
            
            // Two comparisons : see wikipedia for one comparison version.
            if (k < value)
                low = mid + 1 ;
            else if ( k > value)
                high = mid - 1 ;
            else
                return mid ;
        }
        // On exit, when not finding, low is the least value
        // above, including off the end of the array.  
        return encodeIndex(low) ;
    }
    
    // Alt form - no early termination.
//    public static int binarySearch2(IntBuffer buff, int value, int low, int high)
//    {
//        // Low is inclusive, high is exclusive
//        check(buff.limit(), low, high) ;
//        int N = high ;
//        
//        // Uses high as exclusive index
//        while (low < high)
//        {
//            int mid = (low + high) >>> 1 ;  // int divide by 2 : better: mid = low + ((high - low) / 2)
//            int k = buff.get(mid) ;
//            
//            // Two comparisons : see wikipedia for one comparison version.
//            if ( k < value)
//                low = mid + 1 ;
//            else 
//                //can't be high = mid-1: here A[mid] >= value,
//                //so high can't be < mid if A[mid] == value
//                high = mid;
//        }
//        if (low < N && buff.get(low) == value )
//            return low ;
//        else
//            return enc(low) ;
//    }
        
    private static void check(int len, int low, int high)
    {
        if ( low > high )
            throw new IllegalArgumentException("Low index ("+low+") is not less than high index ("+high+")") ; 
        if ( low < 0 )
            throw new ArrayIndexOutOfBoundsException("Low index is negative: "+low) ; 
        if ( high > len )
            throw new ArrayIndexOutOfBoundsException("High index is too large: "+high) ;
    }

    // Why isn't this in the java RT?
    public static <T> int binarySearch( List<T> array, int low, int high, T value, Comparator<T> comparator )
    {
        check(array.size(), low, high) ;
        high -- ;

        while( low <= high )
        {
            int mid = ( low + high ) >>> 1 ;

            T k = array.get(mid) ;
            
            int x = comparator.compare(k, value) ;
            if ( x < 0 )
                low = mid + 1 ;
            else if ( x > 0 )
                high = mid - 1 ;
            else
                return mid ;
        }
        return encodeIndex(low) ;
    }

    // Why isn't this in the java RT?
    public static <T extends Comparable<? super T>>
    int binarySearch(T[] array, int low, int high, T value)
    {
        check(array.length, low, high) ;
        high -- ;

        while( low <= high )
        {
            int mid = ( low + high ) >>> 1 ;

            T k = array[mid] ;
            
            int x = k.compareTo(value) ; // comparator.compare(k, value) ;
            if ( x < 0 )
                low = mid + 1 ;
            else if ( x > 0 )
                high = mid - 1 ;
            else
                return mid ;
        }
        return encodeIndex(low) ;
    }
    
    
    // Use Arrays.binarySearch functions
    
//    public static int binarySearch(int buff[], int value)
//    { return binarySearch(buff, value, 0, buff.length) ; } 
//
//    public static int binarySearch(int buff[], int low, int high, int value)
//    {
//        check(buff.length, low, high) ;
//        // Low is inclusive, high is exclusive.
//        high -- ;   // Convert high to inclusive.
//
//        // Non-tail-recursive form, because tail-recursion removal
//        // is not required by java (unlike scheme).
//        
//        while (low <= high)
//        {
//            int mid = (low + high) >>> 1 ;  // int divide by 2 
//            int k = buff[mid] ;
//
//            if (k < value)
//                low = mid + 1 ;
//            else if ( k > value)
//                high = mid - 1 ;
//            else
//                return mid ;
//        }
//        // On exit, when not finding, low is the least value
//        // above, including off the end of the array.  
//        return encodeIndex(low) ;
//    }
//
//    
//    public static int binarySearch(long buff[], int value)
//    { return binarySearch(buff, value, 0, buff.length) ; } 
//    
//    public static int binarySearch(long buff[], int low, int high, long value)
//    {
//        check(buff.length, low, high) ;
//        // Low is inclusive, high is exclusive.
//        high -- ;   // Convert high to inclusive.
//
//        // Non-tail-recursive form, because tail-recursion removal
//        // is not required by java (unlike scheme).
//        
//        while (low <= high)
//        {
//            int mid = (low + high) >>> 1 ;  // int divide by 2 
//            long k = buff[mid] ;
//
//            if (k < value)
//                low = mid + 1 ;
//            else if ( k > value)
//                high = mid - 1 ;
//            else
//                return mid ;
//        }
//        // On exit, when not finding, low is the least value
//        // above, including off the end of the array.  
//        return encodeIndex(low) ;
//    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
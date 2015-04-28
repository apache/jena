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

// NB shifting is "mod 32" -- <<32 is a no-op (not a clear).
// http://mindprod.com/jgloss/masking.html

/** Utilities for manipulating a bit pattern which are held in a 32 bit int.
 *  @see BitsLong
 */ 
public final class BitsInt
{
    private BitsInt() {}
    
    private static int IntLen = Integer.SIZE ;
    
    /** Extract the value packed into bits start (inclusive) and finish (exclusive).
     *  The value is returned in the low part of the returned int.
     *  The low bit is bit zero.
     * @param bits
     * @param start
     * @param finish
     * @return int  
     */ 
    
    public static final
    int unpack(int bits, int start, int finish)
    {
        check(start, finish) ;
        if ( finish == 0 ) return 0 ;
        // Remove top bits by moving up.  Clear bottom bits by them moving down.
        return (bits<<(IntLen-finish)) >>> ((IntLen-finish)+start) ;
    }

    /** Place the value into the bit pattern between start and finish
     *  and returns the new int. 
     * @param bits
     * @param value
     * @param start
     * @param finish
     * @return int
     */
    public static final
    int pack(int bits, int value, int start, int finish)
    {
        check(start, finish) ;
        bits = clear$(bits, start, finish) ;
        bits = bits | (value<<start) ;
        return bits ;
    }

    /** Get bits from a hex string.
     * 
     * @param str
     * @param startChar     Index of first character (counted from the left, string style). 
     * @param finishChar    Index after the last character (counted from the left, string style).
     * @return int
     */
    
    public static final
    int unpack(String str, int startChar, int finishChar)
    {
        String s = str.substring(startChar, finishChar) ;
        return Integer.parseInt(s, 16) ;
    }

    /** Set the bits specificied.
     * 
     * @param bits      Pattern
     * @param bitIndex 
     * @return          Modified pattern
     */
    public static final
    int set(int bits, int bitIndex)
    { 
        check(bitIndex) ;
        return set$(bits, bitIndex) ;
    }

    /** Set the bits from start (inc) to finish (exc) to one
     * 
     * @param bits      Pattern
     * @param start     start  (inclusive)
     * @param finish    finish (exclusive)
     * @return          Modified pattern
     */
    public static final
    int set(int bits, int start, int finish)
    { 
        check(start, finish) ;
        return set$(bits, start, finish) ;
    }

    /** Test whether a bit is the same as isSet 
     * @param bits      Pattern
     * @param isSet     Test whether is set or not. 
     * @param bitIndex  Bit index
     * @return          Boolean
     */
    public static final
    boolean test(int bits, boolean isSet, int bitIndex)
    {
        check(bitIndex) ;
        return test$(bits, isSet, bitIndex) ;
    }
    
    /** Test whether a bit is set 
     * @param bits      Pattern
     * @param bitIndex  Bit index
     * @return          Boolean
     */
    public static final
    boolean isSet(int bits, int bitIndex)
    {
        check(bitIndex) ;
        return test$(bits, true, bitIndex) ;
    }
    
    /** Test whether a range has a specific value or not   
     * @param bits      Pattern
     * @param value     Value to test for
     * @param start     start  (inclusive)
     * @param finish    finish (exclusive)
     * @return          Boolean
     */
    public static final
    boolean test(int bits, int value, int start, int finish)
    {
        check(start, finish) ;
        return test$(bits, value, start, finish) ;
    }
    
    /** Get the bits from start (inclusive) to finish (exclusive),
     *  leaving them aligned in the int.  See also unpack, returns
     *  the value found at that place.
     *  @see #unpack(int, int, int)
     *  @param bits
     *  @param start
     *  @param finish
     *  @return int
     */
    
    public static final
    int access(int bits, int start, int finish)
    {
        check(start, finish) ;
        return access$(bits, start, finish) ; 
    }
    
    /**
     * Clear the bits specified.
     *  @param bits
     *  @param start
     *  @param finish
     *  @return int
     */
    public static final
    int clear(int bits, int start, int finish)
    {
        check(start, finish) ;
        return clear$(bits, start, finish) ;
    }

    /**
     * Create a mask that has ones between bit positions start (inc) and finish (exc),
     * and zeros elsewhere.
     * @param start
     * @param finish
     * @return int
     */
    public static final
    int mask(int start, int finish)
    {
        check(start, finish) ;
        return mask$(start, finish) ;
    }
    
    /**
     * Create a mask that has zeros between bit positions start (inc) and finish (exc),
     * and ones elsewhere
     * @param start
     * @param finish
     * @return int
     */
    public static final
    int maskZero(int start, int finish)
    {
        check(start, finish) ;
        return maskZero$(start, finish) ;
    }
    
    private static final
    int clear$(int bits, int start, int finish)
    {
        int mask = maskZero$(start, finish) ;
        bits = bits & mask ;
        return bits ;
    }

    private static final
    int set$(int bits, int bitIndex)
    { 
        int mask = mask$(bitIndex) ;
        return bits | mask ;
    }

    private static final
    int set$(int bits, int start, int finish)
    { 
        int mask = mask$(start, finish) ;
        return bits | mask ;
    }

    private static
    boolean test$(int bits, boolean isSet, int bitIndex)
    {
        return isSet == access$(bits, bitIndex) ;
    }

    private static
    boolean test$(int bits, int value, int start, int finish)
    {
        int v = access$(bits, start, finish) ;
        return v == value ;
    }


    
    private static final
    boolean access$(int bits, int bitIndex)
    {
        int mask = mask$(bitIndex) ;
        return (bits & mask) != 0L ;
    }
    
    private static final
    int access$(int bits, int start, int finish)
    {
        // Two ways:
//        int mask = mask$(start, finish) ;
//        return bits & mask ;
        
        return ( (bits<<(IntLen-finish)) >>> (IntLen-finish+start) ) << start  ;
    }
    

    private static final
    int mask$(int bitIndex)
    {
        return 1 << bitIndex ;
    }

    private static final
    int mask$(int start, int finish)
    {
    //        int mask = 0 ;
    //        if ( finish == int.SIZE )
    //            // <<int.SIZE is a no-op 
    //            mask = -1 ;
    //        else
    //            mask = (1L<<finish)-1 ;
        if ( finish == 0 )
            // So start is zero and so the mask is zero.
            return 0 ;

        
        int mask = -1 ;
//        mask = mask << (IntLen-finish) >>> (intLen-finish) ;      // Clear the top bits
//        return mask >>> start << start ;                  // Clear the bottom bits
        return mask << (IntLen-finish) >>> (IntLen-finish+start) << start ; 
    }

    private static final
    int maskZero$(int start, int finish)
    {
        return ~mask$(start, finish) ;
    }
    
    private static final
    void check(int bitIndex)
    {
        if ( bitIndex < 0 || bitIndex >= IntLen ) throw new IllegalArgumentException("Illegal bit index: "+bitIndex) ;
    }

    private static final
    void check(int start, int finish)
    {
        if ( start < 0 || start >= IntLen ) throw new IllegalArgumentException("Illegal start: "+start) ;
        if ( finish < 0 || finish > IntLen ) throw new IllegalArgumentException("Illegal finish: "+finish) ;
        if ( start > finish )  throw new IllegalArgumentException("Illegal range: ("+start+", "+finish+")") ;
    }
    
}

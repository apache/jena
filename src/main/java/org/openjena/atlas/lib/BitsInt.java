/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

// NB shifting is "mod 32" -- <<32 is a no-op (not a clear).
// http://mindprod.com/jgloss/masking.html

/** Utilities for manipulating a bit pattern which held in a 32 bit int
 *  
 * @author Andy Seaborne
 * @version $Id: Bits.java,v 1.3 2007/01/02 11:51:49 andy_seaborne Exp $
 */ 
public final class BitsInt
{
    private BitsInt() {}
    
    private static int IntLen = Integer.SIZE ;
    
    /** Extract the value packed into bits start (inclusive) and finish (exclusive),
     *  the value is returned the low part of the returned int.
     *  The low bit is bit zero.
     */ 
    
    public static final
    int unpack(int bits, int start, int finish)
    {
        check(start, finish) ;
        if ( finish == 0 ) return 0 ;
        // Remove top bits by moving up.  Clear bottom bits by them moving down.
        return (bits<<(IntLen-finish)) >>> ((IntLen-finish)+start) ;
    }

    /** Place the value into the bit pattern between start and finish;
     *  leaves other bits aint.
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

    /** Set the bits from string (inc) to finish (exc) to one
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

    public static final
    boolean test(int bits, boolean isSet, int bitIndex)
    {
        check(bitIndex) ;
        return test$(bits, isSet, bitIndex) ;
    }
    
    public static final
    boolean isSet(int bits, int bitIndex)
    {
        check(bitIndex) ;
        return test$(bits, true, bitIndex) ;
    }
    
    public static final
    boolean test(int bits, int value, int start, int finish)
    {
        check(start, finish) ;
        return test$(bits, value, start, finish) ;
    }
    
    /** Get the bits from start (inclusive) to finish (exclusive),
     *  leaving them aligned in the int.  See also unpack, returns
     *  the value found at that place.
     */
    
    public static final
    int access(int bits, int start, int finish)
    {
        check(start, finish) ;
        return access$(bits, start, finish) ; 
    }
    
    public static final
    int clear(int bits, int start, int finish)
    {
        check(start, finish) ;
        return clear$(bits, start, finish) ;
    }

    /**
     * Create a mask that has ones between bit positions start (inc) and finish (exc)
     */
    public static final
    int mask(int start, int finish)
    {
        check(start, finish) ;
        return mask$(start, finish) ;
    }
    
    /**
     * Create a mask that has zeros between bit positions start (inc) and finish (exc)
     * and ones elsewhere
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

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
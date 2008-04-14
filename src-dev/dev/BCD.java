/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import lib.BitsLong;

public class BCD
{
    // BCD is (will be) an scale and a nibble array. 
    
    static final int NibblesPerLong = Long.SIZE/4 ;
    
    public static void set() {}

    public static long asBCD(long x)
    {
        long z = 0 ;
        int idx = 0 ;
        while( x > 0 )
        {
            int y = (int)(x%10) ;
            z = set(z, y, idx) ;
            x = x / 10 ;
            idx ++ ;
        }
        return z ;
    }
    
    public static long asLong(long bcd)
    {
        int z = numNibbles(bcd) ;
        long v = 0 ;
        for ( int i = z-1 ; i >=0 ; i-- )
            v = 10L*v + (long)nibble(bcd, i) ;
        return v ;
    }


    private static long set(long z, int y, int idx)
    {
        return BitsLong.pack(z, y, 4*idx, 4*(idx+1)) ;
    }


    public static String nibbleStrZeros(long bcd)
    { return nibbleStr(bcd, 0, NibblesPerLong) ; }
    
    public static String nibbleStr(long bcd)
    {
        StringBuilder b = new StringBuilder() ;
        int i = numNibbles(bcd)-1 ;
        if ( i == 0 )   // At least a "0"
            i = 1 ;
        return nibbleStr(bcd, 0, i) ;
    }
    
    // The number of nibbles (without the leading zeros). At least one
    private static int numNibbles(long bcd)
    {
        int i = Long.numberOfLeadingZeros(bcd) ;
        i = (i+3)/4 ;   // Leading nibbles that zero. 
        i = (64/4)-i ;  // Nibbles after the high zeros.
        return i+1 ;
    }
    
    // The bottom len nibbles.
    public static String nibbleStrLow(long bcd, int width)
    {
        return nibbleStr(bcd, 0, width) ;   
    }

    public static String nibbleStrHigh(long bcd, int width)
    {
        return nibbleStr(bcd, NibblesPerLong-width, NibblesPerLong) ;   
    }

    
    public static String nibbleStr(long bcd, int start, int finish)
    {
        StringBuilder b = new StringBuilder() ;
        for ( int j = finish-1 ; j >= start ; j-- )
        {
            char ch = dec(nibble(bcd, j)) ; 
            b.append(ch) ; 
        }
        return b.toString() ;
    }

    
    private static int nibble(long bcd, int j)
    {
        return (int)BitsLong.unpack(bcd, 4*j, 4*(j+1)) ;
    }


    private static long insert(long bits, int x, int idx)
    {
        long mask = BitsLong.maskZero(4*idx, 4*(idx+1)) ;
        x = enc(x) ;
        return (bits & mask ) | (x <<4*idx) ; 
    }



    private static int enc(int x)
    {
        check(x) ;
        return x ;
    }

    private static char dec(int x)
    {
        return (char)(x+(int)'0') ;
    }


    private static void check(int x)
    {}
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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
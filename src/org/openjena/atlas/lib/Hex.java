/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import org.openjena.atlas.AtlasException ;

/** Working in hex ... */
public class Hex
{
    // No checking, fixed width.
    public static int formatUnsignedLongHex(final byte[] b, final int start, final long value, final int width)
    {
        // Insert from low value end to high value end.
        int idx = start+width-1 ;
        int w = width ;
        long x = value ;
        
        while ( w > 0 )
        {
            int d = (int)(x & 0xF) ;
            x = x>>>4 ; // Unsigned shift.
            byte ch = Bytes.hexDigitsUC[d] ; 
            b[idx] = ch ;
            w-- ;
            idx-- ;

            if ( x == 0 )
                break ;
        }

        if ( x != 0 )
            throw new AtlasException("formatUnsignedLongHex: overflow") ;

        while ( w > 0 )
        {
            b[idx] = '0' ;
            idx-- ;
            w-- ;
        }
        return width ;
    }
    
    // No checking, fixed width.
    public static long getLong(byte[] arr, int idx)
    {
        long x = 0 ;
        for ( int i = 0 ; i < 16 ; i++ )
        {
            byte c = arr[idx] ;
            int v = hexByteToInt(c) ;
            x = x << 4 | v ;  
            idx++ ; 
        }
        return x ;
    }
    
    public static int hexByteToInt(int c)
    {
        if ( '0' <= c && c <= '9' )   
            return c-'0' ;
        else if ( 'A' <= c && c <= 'F' )
            return c-'A'+10 ;
        else if ( 'a' <= c && c <= 'f' )
            return c-'a'+10 ;
        else
            throw new IllegalArgumentException("Bad index char : "+c) ;
    }
    
    /** Return the value of the hex digit, or the marker value if not a hex digit.*/
    public static int hexByteToInt(int c, int marker)
    {
        if ( '0' <= c && c <= '9' )   
            return c-'0' ;
        else if ( 'A' <= c && c <= 'F' )
            return c-'A'+10 ;
        else if ( 'a' <= c && c <= 'f' )
            return c-'a'+10 ;
        else
            return marker ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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
/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class Chars
{
    private Chars() {}
    
 // So also Bytes.hexDigits to get bytes.
    final public static char[] digits10 = {
        '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' , '9'
    } ;
    
    // So also Bytes.hexDigits to get bytes.
    final public static char[] hexDigits = {
        '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' ,
        '9' , 'A' , 'B' , 'C' , 'D' , 'E' , 'F' 
//         , 'g' , 'h' ,
//        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
//        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
//        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
        };
    
    /** Java name for UTF-8 encoding */
    private static final String encodingUTF8     = "utf-8" ;
    /** Java name for ASCII encoding */
    private static final String encodingASCII    = "ascii" ;
    
    public static final Charset charsetUTF8 = Charset.forName(encodingUTF8) ;
    public static final Charset charsetASCII = Charset.forName(encodingASCII) ;
    
    // Pools for encoders/decoder.  Paolo says that creating an encopder or decoder is not that cheap.
    // Initial pool size. Any additional encoder/decoder are later
    // placed in the pool - it's an infinite, reusing, growing pool.
    
    private static final int PoolSize = 2 ;
    private static Pool<CharsetEncoder> encoders = new PoolSync<CharsetEncoder>() ;
    private static Pool<CharsetDecoder> decoders = new PoolSync<CharsetDecoder>() ;
    
    static {
        // Fill the pool.
        for ( int i = 0 ; i < PoolSize ; i++ )
        {
            putEncoder(createEncoder()) ;
            putDecoder(createDecoder()) ;
        }
    }

    /** Create a UTF-8 encoder */
    public static CharsetEncoder createEncoder() { return charsetUTF8.newEncoder() ; }
    /** Create a UTF-8 decoder */
    public static CharsetDecoder createDecoder() { return charsetUTF8.newDecoder() ; }

    /** Get a UTF-8 encoder from the pool (null if pool empty) */ 
    public static CharsetEncoder getEncoder() { return encoders.get() ; }
    /** Add/return a UTF-8 encoder to the pool */
    public static void putEncoder(CharsetEncoder encoder) { encoders.put(encoder) ; }

    /** Get a UTF-8 decoder from the pool (null if pool empty) */ 
    public static CharsetDecoder getDecoder() { return decoders.get() ; }
    /** Add/return a UTF-8 decoder to the pool */
    public static void putDecoder(CharsetDecoder decoder) { decoders.put(decoder) ; }
    
    public static void encodeAsHex(StringBuilder buff, char marker, char ch)
    {
        buff.append(marker) ;
        int lo = ch & 0xF ;
        int hi = (ch >> 4) & 0xF ;
        buff.append(Chars.hexDigits[hi]) ;                
        buff.append(Chars.hexDigits[lo]) ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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
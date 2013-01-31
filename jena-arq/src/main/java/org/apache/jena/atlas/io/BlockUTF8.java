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

package org.apache.jena.atlas.io ;

import java.io.IOException ;
import java.nio.ByteBuffer ;
import java.nio.CharBuffer ;

import org.apache.jena.atlas.lib.NotImplemented ;

/**
 * Convert between bytes and chars, UTF-8 only.
 * 
 * This code is just the UTF-8 encoding rules - it does not check for legality
 * of the Unicode data.  The standard codec do, so do not round-trip with binary
 * compatibility. (Example: a single element of a surrogate pair will
 * be encoded/decoded without lost.
 *  
 * 
 * The usual Charset encoders/decoders can be expensive to start up - they are also
 * not thread safe. Sometimes we want to convert 10's of chars and UTF-8 can be
 * done in code with no lookup tables (which, if used, are cache-unfriendly).
 * 
 * This code is thread safe.  It uses code in the hope that JITting will
 * make it fast if used heavily.
 */

public class BlockUTF8
{
    // Looking in java.lang.StringCoding (Sun RT) is illuminating.
    // The actual encode/decode code is in sun.nio.cs.UTF_8.(Decoder|Encoder)
    // which has special cases for ByteBuffer, ByteBuffer with array (needs offsets)
    // and byte[] <-> char[]

    // It seems that chars -> bytes (on <100char strings) is faster with BlockUTF8
    // but the conversion from bytes to string is faster with Java decoders (not by much though). 

    /*
     * Bits 
     * 7    U+007F      1 to 127              0xxxxxxx 
     * 11   U+07FF      128 to 2,047          110xxxxx 10xxxxxx
     * 16   U+FFFF      2,048 to 65,535       1110xxxx 10xxxxxx 10xxxxxx
     * 21   U+1FFFFF    65,536 to 1,114,111   11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
     * 26   U+3FFFFFF                         111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
     * 31   U+7FFFFFFF                        1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
     */
    
    
    /** Convert the bytes in the ByteBuffer to characters in the CharBuffer.
     * The CharBuffer must be large enough. 
     */
    public static void toChars(ByteBuffer bb, CharBuffer cb)
    {
//        if ( bb.hasArray() && cb.hasArray() )
//        {
//            toCharsArray(bb.array(), cb.array()) ;
//            return ;
//        }
        toCharsBuffer(bb, cb) ;
    }

    /** Convert characters to UTF-8 bytes in the ByteBuffer.
     * The ByteBuffer must be large enough. 
     */
    public static void fromChars(CharBuffer cb, ByteBuffer bb)
    {
//        if ( bb.hasArray() && cb.hasArray() )
//        {
//            fromCharsArray(cb.array(), bb.array()) ;
//            return ;
//        }
        fromCharsBuffer(cb, bb) ;
    }

    /** Make a string from UTF-8 bytes in a ByteBuffer */ 
    public static String toString(ByteBuffer bb)
    {
        // I think that the copy from some mutable collector to immutable string is inevitable in java.  
        int len = bb.remaining() ;
        CharBuffer cb = CharBuffer.allocate(len) ;
        toChars(bb, cb) ;
        return new String(cb.array(), 0, cb.position()) ;
    }

    // Using buffer access.
    private static void toCharsBuffer(ByteBuffer bb, CharBuffer cb)
    {
        int idx = bb.position();
        int limit = bb.limit() ;

        for ( ;  idx < limit ; )
        {
            int x = bb.get() ;
            if ( x > 0 && x <= 127 )
            {
                cb.put((char)x) ;
                idx += 1 ;
            } else if ( x == 0 )
            {
                // Pass through a null byte as the null character (illegal Unicode, Java compatible).
                cb.put((char)x) ;
                idx += 1 ;
            }
            else if ( (x & 0xE0) == 0xC0 )
            {
                // 10 => extension byte
                // 110..... => 2 bytes
                // Unroll common path
                //int ch = readMultiBytes(bb, x & 0x1F, 2) ;
                int x2 = bb.get() ;
                if ( (x2 & 0xC0) != 0x80 )
                    exception("Illegal UTF-8 processing character: 0x%04X",x2) ;
                // 6 bits of x2
                int ch = ( (x&0x1F) << 6) | (x2 & 0x3F); 
                cb.put((char)ch) ;
                idx += 2  ;
            } 
            else if ( (x & 0xF0) == 0xE0 ) 
            {
                //  1110.... => 3 bytes : 16 bits : not outside 16bit chars 
                int ch = readMultiBytes(bb, x & 0x0F, 3) ;
                cb.put((char)ch) ;
                idx += 3 ;
            }
            else if ( (x & 0xF8) == 0xF0 ) 
            {
                // Looking like 4 byte charcater.
                // 11110zzz => 4 bytes.
                int ch = readMultiBytes(bb, x & 0x08, 4) ;
                char chars[] = Character.toChars(ch) ;
                cb.put(chars) ;
                idx += 4 ;
            }
            else 
            {
                exception("Illegal UTF-8: 0x%04X",x) ;
                return ;
            }
        }
    }

    private static void toCharsArray(byte[] bytes, char[] chars)
    {
        throw new NotImplemented() ;
    }
    
    private static void fromCharsBuffer(CharBuffer cb, ByteBuffer bb)
    {
        // CharBuffers are CharSequences but charAt(i) adds a layer of work.
        //int bytesStart = bb.position() ;
        int idx = cb.position() ;
        int limit = cb.limit() ;
        for ( ; idx < limit ; idx ++ )
        {
            char ch = cb.get() ;
            if ( ch != 0 && ch <= 127 )
            {
                // 7 bits
                bb.put((byte)ch) ;
            } 
            else if ( ch == 0 )
            {
                // Java.
                bb.put((byte)0x00) ;
                // Modified UTF-8.
                //bb.put((byte)0xC0) ;
                //bb.put((byte)0x80) ;
            } 
            else if ( ch <= 0x07FF )
            {
                // 11 bits : 110yyyyy 10xxxxxx
                // int x1 = ( ((ch>>(11-5))&0x7) | 0xC0 ) ; outputBytes(out, x1, 2, ch) ; return ;
                int x1 = ( ((ch>>(11-5))&0x01F ) | 0xC0 ) ; 
                int x2 = ( (ch&0x3F)  | 0x80 ) ;
                bb.put((byte)x1) ;
                bb.put((byte)x2) ;
            } 
            else if ( ch <= 0xFFFF )
            {
                // 16 bits : 1110aaaa  10bbbbbb  10cccccc
                // int x1 = ( ((ch>>(16-4))&0x7) | 0xE0 ) ; outputBytes(out, x1, 3, ch) ; return ;
                int x1 = ( ((ch>>(16-4))&0x0F) | 0xE0 ) ;
                int x2 = ( ((ch>>6)&0x3F) | 0x80 ) ;
                int x3 = ( (ch&0x3F) | 0x80 ) ;
                bb.put((byte)x1) ;
                bb.put((byte)x2) ;
                bb.put((byte)x3) ;
            }
            //            if ( Character.isDefined(ch) )
            //                throw new AtlasException("not a character") ;
            //if ( true ) throw new InternalErrorException("Valid code point for Java but not encodable") ;
            // Not java, where chars are 16 bit.
            else if ( ch <= 0x1FFFFF )
            {
                // 21 bits : 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                int x1 = ( ((ch>>(21-3))&0x7) | 0xF0 ) ;
                outputBytes(bb, x1, 4, ch) ;
            } 
            else if ( ch <= 0x3FFFFFF )
            {
                // 26 bits : 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
                int x1 = ( ((ch>>(26-2))&0x3) | 0xF8 ) ;
                outputBytes(bb, x1, 5, ch) ;
            }
            else if ( ch <= 0x7FFFFFFF )
            {
                // 32 bits : 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
                int x1 = ( ((ch>>(32-1))&0x1) | 0xFC ) ;
                outputBytes(bb, x1, 6, ch) ;
            }
        }
        //int bytesFinish = bb.position() ;
    }

    private static void fromCharsArray(char[] array, byte[] array2)
    {
        throw new NotImplemented() ;
    }
    
    public static void fromChars(CharSequence cs, ByteBuffer bb)
    {
        fromChars(CharBuffer.wrap(cs), bb) ;
    }

    private static int readMultiBytes(ByteBuffer input, int start, int len)
    {
        // We have already read one byte.
        if ( input.remaining() < (len-1) )
            exception("Premature end to UTF-8 sequence at end of input") ;
        int x = start ;
        for ( int i = 0 ; i < len-1 ; i++ )
        {
            int x2 = input.get() ;
            if ( (x2 & 0xC0) != 0x80 )
                exception("Illegal UTF-8 processing character: 0x%04X",x2) ;
            // 6 bits of x2
            x = (x << 6) | (x2 & 0x3F); 
        }
        return x ;
    }

    /** Put bytes to the output ByteBuffer for charcater ch.
     * The first byte is in x1 and already has the needed bits set. 
     */
    private static void outputBytes(ByteBuffer bb, int x1, int byteLength, int ch)
    {
        bb.put((byte)x1) ;
        byteLength-- ; // remaining bytes
        for ( int i = 0 ; i < byteLength ; i++ )
        {
            // 6 Bits, loop from high to low  
            int shift = 6*(byteLength-i-1) ;
            int x =  (ch>>shift) & 0x3F ;
            x = x | 0x80 ;  // 10xxxxxx
            bb.put((byte)x) ;
        }
    }
    
    // Does not return 
    private static void exception(String fmt, Object ...args)
    {
        String str = String.format(fmt,args) ;
        IO.exception(new IOException(str)) ;
    }
}

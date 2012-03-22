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

package org.openjena.atlas.io ;

import java.io.IOException ;
import java.nio.ByteBuffer ;
import java.nio.CharBuffer ;

import org.openjena.atlas.AtlasException ;

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
    // TODO If src and dst have arrays, use the arrays.
    // TODO Flatten the loops - no function call in the loop.
    
    // Looking in java.lang.StringCoding (Sun RT) is illuminating.
    // The actual encode/decode code is in sun.nio.cs.UTF_8.(Decoder|Encoder)
    // which has special cases for ByteBuffer, ByteBuffer with array (needs offsets)
    // and byte[] <-> char[]

    // It seems that chars -> bytes (on <100char strings) is faster with BlockUTF8
    // but the conversion from bytes to string is faster with decoders (not by much though). 
    
    private static final Convert converter = new ConvertUTF8() ;
    private static final Convert asciiConvert = new ConvertAscii() ;

    /** Convert the bytes in the ByteBuffer to characters in the CharBuffer.
     * The CharBuffer must be large enough. 
     */
    public static void toChars(ByteBuffer bb, CharBuffer cb)
    {
        //if ( bb.hasArray() && cb.hasArray() )
            
        
        int len = bb.remaining() ;

        for (int i = 0; i < len; )
        {
            //i += converter.convertBytesToChar(bb, cb) ;
            i += _convertBytesToChar(bb, cb) ;
        }
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

    /** Convert characters to UTF-8 bytes in the ByteBuffer.
     * The ByteBuffer must be large enough. 
     */
    public static void fromChars(CharSequence cs, ByteBuffer bb)
    {
        int len = cs.length() ;
        for (int i = 0; i < len; i++)
        {
            char c = cs.charAt(i) ;
            //converter.convertCharToBytes(c, bb) ;
            _convertCharToBytes(c, bb) ;
        }
    }
    
    /** Convert charcaters to UTF-8 bytes in the ByteBuffer.
     * The ByteBuffer must be large enough. 
     */
    public static void fromChars(CharBuffer cb, ByteBuffer bb)
    {
        //if ( bb.hasArray() && cb.hasArray() )

        // CharBuffers are CharSequences but charAt(i) adds a layer of work.
        int len = cb.remaining() ;
        for (int i = 0; i < len; i++)
        {
            char c = cb.get() ;
            converter.convertCharToBytes(c, bb) ;
        }
    }

    private interface Convert
    {
        /** Return number of bytes consumed */
        int convertBytesToChar(ByteBuffer bb, CharBuffer cb) ;

        /** Return number of bytes produced */
        int convertCharToBytes(char c, ByteBuffer bb) ;
    }

    // ASCII
    
    private static final class ConvertAscii implements Convert
    {
        @Override
        public int convertBytesToChar(ByteBuffer bb, CharBuffer cb)
        {
            byte b = bb.get() ;
            // ASCII
            char c = (char)b ;
            cb.put(c) ;
            return 1 ;
        }

        @Override
        public int convertCharToBytes(char c, ByteBuffer bb)
        {
            byte b = (byte)(c | 0xFF) ;
            bb.put(b) ;
            return 1 ;
        }
    }
    
    private static final class ConvertUTF8 implements Convert
    {
        @Override
        public final int convertBytesToChar(ByteBuffer bb, CharBuffer cb)
        {
            return _convertBytesToChar(bb, cb) ;
        }
        
        @Override
        public int convertCharToBytes(char ch, ByteBuffer bb)
        {
            return _convertCharToBytes(ch, bb) ;
        }
    }

    static int _convertBytesToChar(ByteBuffer bb, CharBuffer cb)
    {
        int x = bb.get() ;
        if ( x > 0 && x <= 127 )
        {
            cb.put((char)x) ;
            return 1 ;
        }
        
        if ( x == 0 )
        {
            // Pass through a null byte as the nul character (illegal Unicode).
            cb.put((char)x) ;
            return 1 ;
        }
            

        // 10 => extension byte
        // 110..... => 2 bytes
        if ( (x & 0xE0) == 0xC0 )
        {
//          // Unwind.
//          int ch = readMultiBytes(bb, x & 0x1F, 2) ;
            int x2 = bb.get() ;
            if ( (x2 & 0xC0) != 0x80 )
                //throw new AtlasException("Illegal UTF-8 processing character "+count+": "+x2) ;
                throw new AtlasException(String.format("Illegal UTF-8 processing character: 0x%04X",x2)) ;
            // 6 bits of x2
            int ch = ( (x&0x1F) << 6) | (x2 & 0x3F); 
            cb.put((char)ch) ;
            return 2 ;

        }
        //  1110.... => 3 bytes : 16 bits : not outside 16bit chars 
        if ( (x & 0xF0) == 0xE0 ) 
        {
            int ch = readMultiBytes(bb, x & 0x0F, 3) ;
            cb.put((char)ch) ;
            return 3 ;
        }

        // Looking like 4 byte charcater.
        // 11110zzz => 4 bytes.
        if ( (x & 0xF8) == 0xF0 )
        {
            int ch = readMultiBytes(bb, x & 0x08, 4) ;
            char chars[] = Character.toChars(ch) ;
            cb.put(chars) ;
            return 4 ;
        }
        else 
        {
            IO.exception(new IOException("Illegal UTF-8: "+x)) ;
            return -1 ;
        }

//            // This test will go off.  We're processing a 4 byte sequence but Java only supports 16 bit chars. 
//            if ( ch > Character.MAX_VALUE )
//                throw new AtlasException("Out of range character (must use a surrogate pair)") ;
//            if ( ! Character.isDefined(ch) ) throw new AtlasException(String.format("Undefined codepoint: 0x%04X", ch)) ;
//            return ch ;
    }

    private static int readMultiBytes(ByteBuffer input, int start, int len) //throws IOException
    {
        int x = start ;
        for ( int i = 0 ; i < len-1 ; i++ )
        {
            int x2 = input.get() ;
            if ( x2 == -1 )
                throw new AtlasException("Premature end to UTF-8 sequence at end of input") ;
            
            if ( (x2 & 0xC0) != 0x80 )
                //throw new AtlasException("Illegal UTF-8 processing character "+count+": "+x2) ;
                throw new AtlasException(String.format("Illegal UTF-8 processing character: 0x%04X",x2)) ;
            // 6 bits of x2
            x = (x << 6) | (x2 & 0x3F); 
        }
        return x ;
    }

    public static int _convertCharToBytes(char ch, ByteBuffer bb)
    {
        if ( ch != 0 && ch <= 127 )
        {
            // 7 bits
            bb.put((byte)ch) ;
            return 1 ;
        }

        if ( ch == 0 )
        {
            // Java.
            bb.put((byte)0x00) ;
            return 1 ;
//            // Modified UTF-8.
//            bb.put((byte)0xC0) ;
//            bb.put((byte)0x80) ;
//            return 2 ;
        }

        if ( ch <= 0x07FF )
        {
            // 11 bits : 110yyyyy 10xxxxxx
            // int x1 = ( ((ch>>(11-5))&0x7) | 0xC0 ) ; outputBytes(out, x1, 2, ch) ; return ;
            int x1 = ( ((ch>>(11-5))&0x01F ) | 0xC0 ) ; 
            int x2 = ( (ch&0x3F)  | 0x80 ) ;
            bb.put((byte)x1) ;
            bb.put((byte)x2) ;
            return 2 ;
        }
        if ( ch <= 0xFFFF )
        {
            // 16 bits : 1110aaaa  10bbbbbb  10cccccc
            // int x1 = ( ((ch>>(16-4))&0x7) | 0xE0 ) ; outputBytes(out, x1, 3, ch) ; return ;
            int x1 = ( ((ch>>(16-4))&0x0F) | 0xE0 ) ;
            int x2 = ( ((ch>>6)&0x3F) | 0x80 ) ;
            int x3 = ( (ch&0x3F) | 0x80 ) ;
            bb.put((byte)x1) ;
            bb.put((byte)x2) ;
            bb.put((byte)x3) ;
            return 3 ;
        }

        //            if ( Character.isDefined(ch) )
        //                throw new AtlasException("not a character") ;

        //if ( true ) throw new InternalErrorException("Valid code point for Java but not encodable") ;

        // Not java, where chars are 16 bit.
        if ( ch <= 0x1FFFFF )
        {
            // 21 bits : 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            int x1 = ( ((ch>>(21-3))&0x7) | 0xF0 ) ;
            outputBytes(bb, x1, 4, ch) ;
            return 4 ;
        }
        if ( ch <= 0x3FFFFFF )
        {
            // 26 bits : 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
            int x1 = ( ((ch>>(26-2))&0x3) | 0xF8 ) ;
            outputBytes(bb, x1, 5, ch) ;
            return 5 ;
        }

        if ( ch <= 0x7FFFFFFF )
        {
            // 32 bits : 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
            int x1 = ( ((ch>>(32-1))&0x1) | 0xFC ) ;
            outputBytes(bb, x1, 6, ch) ;
            return 6 ;
        }

        return -1 ;
    }

    /*
     * Bits 
     * 7    U+007F      1 to 127              0xxxxxxx 
     * 11   U+07FF      128 to 2,047          110xxxxx 10xxxxxx
     * 16   U+FFFF      2,048 to 65,535       1110xxxx 10xxxxxx 10xxxxxx
     * 21   U+1FFFFF    65,536 to 1,114,111   11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
     * 26   U+3FFFFFF                         111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
     * 31   U+7FFFFFFF                        1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
     */
    
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

}

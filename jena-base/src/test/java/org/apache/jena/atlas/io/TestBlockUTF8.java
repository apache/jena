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

package org.apache.jena.atlas.io;

import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.OutputStreamWriter ;
import java.io.Writer ;
import java.nio.Buffer ;
import java.nio.ByteBuffer ;
import java.nio.CharBuffer ;
import java.nio.charset.Charset ;
import java.nio.charset.CharsetDecoder ;
import java.nio.charset.CharsetEncoder ;

import org.apache.jena.atlas.io.BlockUTF8 ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Chars ;
import org.junit.Test ;

public class TestBlockUTF8 extends BaseTest
{
    // Need array and non-array versions.
    
    static Charset utf8 = Chars.charsetUTF8 ;
    static CharsetDecoder dec = utf8.newDecoder() ;
    static CharsetEncoder enc = utf8.newEncoder() ;

    // UTF-8 encoding.
    // character '¢' = code point U+00A2 -> C2 A2
    // character '€' = code point U+20AC -> E2 82 AC

    static private final String asciiBase             = "abc" ;
    static private final String latinBase             = "Àéíÿ" ;
    static private final String latinExtraBase        = "ỹﬁﬂ" ;  // fi-ligature, fl-ligature
    static private final String greekBase             = "αβγ" ;
    static private final String hewbrewBase           = "אבג" ;
    static private final String arabicBase            = "ءآأ";
    static private final String symbolsBase           = "☺☻♪♫" ;
    static private final String chineseBase           = "孫子兵法" ; // The Art of War 
    static private final String japaneseBase          = "日本" ;    // Japanese
    static private final String binaryStr1            = "abc\uD800xyz" ;    // A single surrogate, without it's pair. 
    static private final String binaryStr2            = "\uD800" ;          // A single surrogate, without it's pair. 
    static private final String binaryStr3            = "\u0000" ;          // A zero character  

    static private final byte[] binaryBytes1 = {} ;         
    static private final byte[] binaryBytes2 = { (byte)0x00 } ;             // Java encoding of 0 codepoint is 0         
    static private final byte[] binaryBytes3 = { (byte)0xC0, (byte)0x80 } ;     // Modifed unicode zero codepoint.         

    @Test public void convert_in_00() { testIn("") ; }
    @Test public void convert_in_01() { testIn(asciiBase) ; }
    @Test public void convert_in_02() { testIn(latinBase) ; }
    @Test public void convert_in_03() { testIn(latinExtraBase) ; }
    @Test public void convert_in_04() { testIn(greekBase) ; }
    @Test public void convert_in_05() { testIn(hewbrewBase) ; }
    @Test public void convert_in_06() { testIn(arabicBase) ; }
    @Test public void convert_in_07() { testIn(symbolsBase) ; }
    @Test public void convert_in_08() { testIn(chineseBase) ; }
    @Test public void convert_in_09() { testIn(japaneseBase) ; }
    @Test public void convert_in_10() { testInOutBinary(binaryStr1) ; }  
    @Test public void convert_in_11() { testInOutBinary(binaryStr2) ; }  
    @Test public void convert_in_12() { testInOutBinary(binaryStr3) ; }  

    @Test public void convert_out_00() { testOut("") ; }
    @Test public void convert_out_01() { testOut(asciiBase) ; }
    @Test public void convert_out_02() { testOut(latinBase) ; }
    @Test public void convert_out_03() { testOut(latinExtraBase) ; }
    @Test public void convert_out_04() { testOut(greekBase) ; }
    @Test public void convert_out_05() { testOut(hewbrewBase) ; }
    @Test public void convert_out_06() { testOut(arabicBase) ; }
    @Test public void convert_out_07() { testOut(symbolsBase) ; }
    @Test public void convert_out_08() { testOut(chineseBase) ; }
    @Test public void convert_out_09() { testOut(japaneseBase) ; }
    @Test public void convert_out_10() { testOut(binaryStr1) ; }
    @Test public void convert_out_11() { testOut(binaryStr2) ; }
    @Test public void convert_out_12() { testOut(binaryStr3) ; }

    // While it is key is chars->bytes-chars, we also test bytes->bytes 
    @Test public void binary_01() { testBinary(binaryBytes1) ; }
    @Test public void binary_02() { testBinary(binaryBytes2) ; }
    @Test public void binary_03() { testBinary(binaryBytes3, binaryBytes2) ; }

    @Test public void binary_10() { testBinary(binaryBytes2, CharBuffer.wrap(binaryStr3)) ; }
    @Test public void binary_11() { testBinary(binaryBytes3, CharBuffer.wrap(binaryStr3)) ; }

    
    static void testIn(String x)
    {
        testIn(x, allocByteBufferArray, allocCharBufferArray) ;
        testIn(x, allocByteBufferDirect, allocCharBufferDirect) ;

    }
    static void testIn(String x, Alloc<ByteBuffer> allocBB, Alloc<CharBuffer> allocCB)
    {
        // Test as binary.
        testInOutBinary(x) ;

        // Now test, comparing to std Java.
        // Correct answer, in bytes
        ByteBuffer bytes = ByteBuffer.wrap(stringAsBytes(x)) ;
        // To bytes.stringAsBytes
        int N = x.length() ;
        CharBuffer cb = CharBuffer.wrap(x.toCharArray()) ;
        ByteBuffer bb = allocBB.allocate(4*N) ;
        BlockUTF8.fromChars(cb, bb) ;
        bb.flip() ;

        assertTrue("Bytes", sameBytes(bytes, bb)) ;
        // From bytes.
        CharBuffer cb2 = allocCB.allocate(N) ;
        BlockUTF8.toChars(bb, cb2) ;
        cb2.flip() ;
        String str = cb2.toString() ;
        assertEquals(x, str) ;
    }

    // Tesing, but not against what Java would do (it replaces bad chars, we want binary).
    static void testInOutBinary(String x)
    {
        int N = x.length() ;
        CharBuffer cb = CharBuffer.wrap(x.toCharArray()) ;
        ByteBuffer bb = ByteBuffer.allocate(4*N) ;
        BlockUTF8.fromChars(cb, bb) ;
        bb.flip() ;
        CharBuffer cb2 = CharBuffer.allocate(N) ;
        BlockUTF8.toChars(bb, cb2) ;
        // compare cb and cb2.
        String str = new String(cb2.array(), 0, cb2.position()) ;
        assertEquals(x, str) ;

        // And re-code as bytes.
        CharBuffer cb3 = CharBuffer.wrap(x.toCharArray()) ;
        ByteBuffer bb3 = ByteBuffer.allocate(4*N) ;
        BlockUTF8.fromChars(cb3, bb3) ;
        bb3.flip() ;
        assertArrayEquals(bb.array(), bb3.array()) ;
    }

    static void testOut(String x)
    {
        testOut(x, allocByteBufferArray, allocCharBufferArray) ;
        testOut(x, allocByteBufferDirect, allocCharBufferDirect) ;
    }
    
    static interface Alloc<T extends Buffer> { T allocate(int len) ; } 
    static Alloc<ByteBuffer> allocByteBufferArray = new Alloc<ByteBuffer>() {
        @Override public ByteBuffer allocate(int len) { return ByteBuffer.allocate(len) ; }
     } ;
     static Alloc<ByteBuffer> allocByteBufferDirect = new Alloc<ByteBuffer>() {
         @Override public ByteBuffer allocate(int len) { return ByteBuffer.allocateDirect(len) ; }
     } ;
     static Alloc<CharBuffer> allocCharBufferArray = new Alloc<CharBuffer>() {
         @Override public CharBuffer allocate(int len) { return CharBuffer.allocate(len) ; }
      } ;
      static Alloc<CharBuffer> allocCharBufferDirect = new Alloc<CharBuffer>() {
          @Override public CharBuffer allocate(int len) { return ByteBuffer.allocateDirect(2*len).asCharBuffer() ; }
    } ;
    
    static void testOut(String x, Alloc<ByteBuffer> allocBB, Alloc<CharBuffer> allocCB)
    {
        testBinary(stringAsBytes(x)) ;

        int N = x.length() ;
        // First - get bytes the Java way.
        ByteBuffer bytes = ByteBuffer.wrap(stringAsBytes(x)) ;
        CharBuffer cb = allocCB.allocate(N) ;

        BlockUTF8.toChars(bytes, cb) ;
        cb.flip() ;
        bytes.flip() ;

        String str = cb.toString() ;
        ByteBuffer bytes2 = allocBB.allocate(bytes.capacity()) ;
        BlockUTF8.fromChars(cb, bytes2) ;
        bytes2.flip() ;
        assertTrue("Chars", sameBytes(bytes, bytes2)) ;
    }

    static void testBinary(byte[] binary, CharBuffer chars)
    {
        int N = binary.length ;
        ByteBuffer bytes = ByteBuffer.wrap(binary) ;
        CharBuffer cb = CharBuffer.allocate(N) ;
        BlockUTF8.toChars(bytes, cb) ;
        cb.flip() ;
        assertTrue("Binary", sameChars(chars, cb));
    }

    static void testBinary(byte[] binary)
    {
        testBinary(binary, binary) ;
    }

    static void testBinary(byte[] binary, byte[] expected)
    {
        int N = binary.length ;
        ByteBuffer bytes = ByteBuffer.wrap(binary) ;
        CharBuffer cb = CharBuffer.allocate(N) ;
        BlockUTF8.toChars(bytes, cb) ;
        cb.flip() ;
        bytes.position(0) ;
        ByteBuffer bytes2 = ByteBuffer.allocate(2*N) ;  // Null bytes get expanded.
        BlockUTF8.fromChars(cb, bytes2) ;
        bytes2.flip() ;
        sameBytes(bytes, bytes2) ;
        assertTrue("Binary", sameBytes(ByteBuffer.wrap(expected), bytes2)) ;
    }

    // Does not move position.
    static boolean sameBytes(ByteBuffer bb1, ByteBuffer bb2)
    {
        if ( bb1.remaining() != bb2.remaining() ) return false ;
    
        for ( int i = 0 ; i < bb1.remaining() ; i++ )
            if ( bb1.get(i+bb1.position()) != bb2.get(i+bb2.position()) ) return false ;
        return true ;
    }
    // Does not move position.
    static boolean sameChars(CharBuffer cb1, CharBuffer cb2)
    {
        if ( cb1.remaining() != cb2.remaining() ) return false ;
    
        for ( int i = 0 ; i < cb1.remaining() ; i++ )
            if ( cb1.get(i+cb1.position()) != cb2.get(i+cb2.position()) ) return false ;
        return true ;
    }
    static byte[] stringAsBytes(String x)
    {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
            try(Writer out = new OutputStreamWriter(bout, utf8)) {
                out.write(x) ;
            }
            byte[] bytes = bout.toByteArray() ;
            return bytes ;
        } catch (IOException ex) { throw new RuntimeException(ex) ; } 
    }
}

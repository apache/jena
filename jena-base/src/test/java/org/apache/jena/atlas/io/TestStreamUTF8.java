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

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.OutputStreamWriter ;
import java.io.Writer ;
import java.nio.charset.Charset ;
import java.nio.charset.CharsetDecoder ;
import java.nio.charset.CharsetEncoder ;

import org.apache.jena.atlas.io.InStreamUTF8 ;
import org.apache.jena.atlas.io.OutStreamUTF8 ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Chars ;
import org.junit.Test ;

public class TestStreamUTF8 extends BaseTest
    {
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
        
        @Test public void test_in_00() { testIn("") ; }
        @Test public void test_in_01() { testIn(asciiBase) ; }
        @Test public void test_in_02() { testIn(latinBase) ; }
        @Test public void test_in_03() { testIn(latinExtraBase) ; }
        @Test public void test_in_04() { testIn(greekBase) ; }
        @Test public void test_in_05() { testIn(hewbrewBase) ; }
        @Test public void test_in_06() { testIn(arabicBase) ; }
        @Test public void test_in_07() { testIn(symbolsBase) ; }
        @Test public void test_in_08() { testIn(chineseBase) ; }
        @Test public void test_in_09() { testIn(japaneseBase) ; }
        
        @Test public void test_out_00() { testIn("") ; }
        @Test public void test_out_01() { testOut(asciiBase) ; }
        @Test public void test_out_02() { testOut(latinBase) ; }
        @Test public void test_out_03() { testOut(latinExtraBase) ; }
        @Test public void test_out_04() { testOut(greekBase) ; }
        @Test public void test_out_05() { testOut(hewbrewBase) ; }
        @Test public void test_out_06() { testOut(arabicBase) ; }
        @Test public void test_out_07() { testOut(symbolsBase) ; }
        @Test public void test_out_08() { testOut(chineseBase) ; }
        @Test public void test_out_09() { testOut(japaneseBase) ; }
        
        static void testIn(String x)
        {
            try {
                byte[] bytes = stringAsBytes(x) ;

                ByteArrayInputStream bin = new ByteArrayInputStream(bytes) ;
                // Create string from bytes
                try(InStreamUTF8 r = new InStreamUTF8(bin)) {
                    char[] cbuff = new char[x.length()*10] ;    // Way too big
                    int len = r.read(cbuff) ;
                    String str = new String(cbuff, 0 , len) ;
                    assertEquals(x, str) ;
                }
            } catch (IOException ex) { throw new RuntimeException(ex) ; } 
        }

        static void testOut(String x)
        {
            try {
                byte[] bytes = stringAsBytes(x) ;
                ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
                try(Writer out = new OutStreamUTF8(bout)) {
                    out.write(x) ;
                }
                byte[] bytes2 = bout.toByteArray() ;
                assertArrayEquals(bytes, bytes2) ;
            } catch (IOException ex) { throw new RuntimeException(ex) ; } 
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

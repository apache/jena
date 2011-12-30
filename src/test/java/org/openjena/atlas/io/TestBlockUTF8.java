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

package org.openjena.atlas.io;

import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.OutputStreamWriter ;
import java.io.Writer ;
import java.nio.ByteBuffer ;
import java.nio.CharBuffer ;
import java.nio.charset.Charset ;
import java.nio.charset.CharsetDecoder ;
import java.nio.charset.CharsetEncoder ;

import org.junit.Test ;
import org.openjena.atlas.io.BlockUTF8 ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.Chars ;

public class TestBlockUTF8 extends BaseTest
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
        
        static void testIn(String x)
        {
            // Correct answer, in bytes
            ByteBuffer bytes = ByteBuffer.wrap(stringAsBytes(x)) ;
            // To bytes.
            int N = x.length() ;
            CharBuffer cb = CharBuffer.wrap(x.toCharArray()) ;
            ByteBuffer bb = ByteBuffer.allocate(4*N) ;
            BlockUTF8.fromChars(cb, bb) ;
            bb.flip() ;
//            ByteBufferLib.print(bytes) ;
//            ByteBufferLib.print(bb) ;
            
            assertTrue("Bytes", sameBytes(bytes, bb)) ;

            // From bytes.
            CharBuffer cb2 = CharBuffer.allocate(N) ;
            BlockUTF8.toChars(bb, cb2) ;
            String str = new String(cb2.array(), 0, cb2.position()) ;
            assertEquals(x, str) ;
        }

        // Does not move position.
        public static boolean sameBytes(ByteBuffer bb1, ByteBuffer bb2)
        {
            if ( bb1.remaining() != bb2.remaining() ) return false ;
            
            for ( int i = 0 ; i < bb1.remaining() ; i++ )
                if ( bb1.get(i+bb1.position()) != bb2.get(i+bb2.position()) ) return false ;
            return true ;
        }
        
        static void testOut(String x)
        {
            int N = x.length() ;
            // First - get bytes the Java way.
            ByteBuffer bytes = ByteBuffer.wrap(stringAsBytes(x)) ;
            CharBuffer cb = CharBuffer.allocate(N) ;
            
            BlockUTF8.toChars(bytes, cb) ;
            bytes.flip() ;
            String str = new String(cb.array(), 0, cb.position()) ;
            cb.flip() ;

            ByteBuffer bytes2 = ByteBuffer.allocate(bytes.capacity()) ;
            BlockUTF8.fromChars(cb, bytes2) ;
            bytes2.flip() ;
            
            assertTrue("Chars", sameBytes(bytes, bytes2)) ;
        }

        static byte[] stringAsBytes(String x)
        {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
                Writer out = new OutputStreamWriter(bout, utf8) ;
                out.write(x) ;
                out.close() ;
                byte[] bytes = bout.toByteArray() ;
                return bytes ;
            } catch (IOException ex) { throw new RuntimeException(ex) ; } 
        }
    }

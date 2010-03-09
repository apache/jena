/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package atlas.io;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.OutputStreamWriter ;
import java.io.Writer ;
import java.nio.charset.Charset ;
import java.nio.charset.CharsetDecoder ;
import java.nio.charset.CharsetEncoder ;

import org.junit.Test ;
import atlas.io.StreamUTF8 ;
import atlas.lib.Chars ;
import atlas.test.BaseTest ;

public class TestStreamUTF8 extends BaseTest
    {
        static Charset utf8 = Chars.charsetUTF8 ;
        static CharsetDecoder dec = utf8.newDecoder() ;
        static CharsetEncoder enc = utf8.newEncoder() ;
        
        static private final String asciiBase             = "abc" ;
        static private final String latinBase             = "Àéíÿ" ;
        static private final String latinExtraBase        = "ỹﬁﬂ" ;  // fi-ligature, fl-ligature
        static private final String greekBase             = "αβγ" ;
        static private final String hewbrewBase           = "אבג" ;
        static private final String arabicBase            = "ءآأ";
        static private final String symbolsBase           = "☺☻♪♫" ;
        static private final String chineseBase           = "孫子兵法" ; // The Art of War 
        static private final String japaneseBase          = "日本" ;    // Japanese
        
        @Test public void test_01() { test(asciiBase) ; }
        @Test public void test_02() { test(latinBase) ; }
        @Test public void test_03() { test(latinExtraBase) ; }
        @Test public void test_04() { test(greekBase) ; }
        @Test public void test_05() { test(hewbrewBase) ; }
        @Test public void test_06() { test(arabicBase) ; }
        @Test public void test_07() { test(symbolsBase) ; }
        @Test public void test_08() { test(chineseBase) ; }
        @Test public void test_09() { test(japaneseBase) ; }
        
        static void test(String x)
        {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
                Writer out = new OutputStreamWriter(bout, utf8) ;
                out.write(x) ;
                out.close() ;
                byte[] bytes = bout.toByteArray() ;
                
//                System.out.print(">>") ;
//                for ( int i = 0 ; i < bytes.length ; i++ )
//                    System.out.printf(" %02X", bytes[i] & 0xFF) ;
//                System.out.println() ;

                //String str = new String(bytes, utf8) ;

                ByteArrayInputStream bin = new ByteArrayInputStream(bytes) ;

                // Create string from bytes
                StreamUTF8 r = new StreamUTF8(bin) ;
                //Get tests working.
                //Reader r = new InputStreamReader(bin, utf8) ;

                char[] cbuff = new char[x.length()*10] ;    // Way too big
                int len = r.read(cbuff) ;
                String str = new String(cbuff, 0 , len) ;
                assertEquals(x, str) ;
            } catch (IOException ex) { throw new RuntimeException(ex) ; } 
        }
        
    }
/*
 * (c) Copyright 2010 Talis Information Ltd.
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
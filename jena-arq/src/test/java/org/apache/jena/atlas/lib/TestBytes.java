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

package org.apache.jena.atlas.lib;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Bytes ;
import org.junit.Test ;

public class TestBytes extends BaseTest
{
    @Test public void packInt1()
    {
        byte[] b = new byte[4] ;
        Bytes.setInt(0x01020304,b) ;
        assertEquals(0x01, b[0]) ;
        assertEquals(0x02, b[1]) ;
        assertEquals(0x03, b[2]) ;
        assertEquals(0x04, b[3]) ;
    }
    
    @Test public void packInt2()
    {
        byte[] b = new byte[8] ;
        Bytes.setInt(0x01020304,b,0) ;
        Bytes.setInt(0x05060708,b,4) ;
        assertEquals(0x01, b[0]) ;
        assertEquals(0x02, b[1]) ;
        assertEquals(0x03, b[2]) ;
        assertEquals(0x04, b[3]) ;
        assertEquals(0x05, b[4]) ;
        assertEquals(0x06, b[5]) ;
        assertEquals(0x07, b[6]) ;
        assertEquals(0x08, b[7]) ;
    }

    @Test public void packInt3()
    {
        byte[] b = new byte[4] ;
        Bytes.setInt(0xF1F2F3F4,b) ;
        int i = Bytes.getInt(b) ;
        assertEquals(0xF1F2F3F4, i) ;
    }
    
    @Test public void packInt4()
    {
        byte[] b = new byte[8] ;
        Bytes.setInt(0x01020304,b,0) ;
        Bytes.setInt(0x05060708,b,4) ;

        int i1 = Bytes.getInt(b,0) ;
        int i2 = Bytes.getInt(b,4) ;
        assertEquals(0x01020304, i1) ;
        assertEquals(0x05060708, i2) ;
    }

    @Test public void packLong5()
    {
        byte[] b = new byte[8] ;
        Bytes.setLong(0x0102030405060708L,b) ;
        assertEquals(0x01, b[0]) ;
        assertEquals(0x02, b[1]) ;
        assertEquals(0x03, b[2]) ;
        assertEquals(0x04, b[3]) ;
        assertEquals(0x05, b[4]) ;
        assertEquals(0x06, b[5]) ;
        assertEquals(0x07, b[6]) ;
        assertEquals(0x08, b[7]) ;
   }
    
    @Test public void packLong6()
    {
        byte[] b = new byte[16] ;
        Bytes.setLong(0x0102030405060708L,b,0) ;
        Bytes.setLong(0x1112131415161718L,b,8) ;
        assertEquals(0x01, b[0]) ;
        assertEquals(0x02, b[1]) ;
        assertEquals(0x03, b[2]) ;
        assertEquals(0x04, b[3]) ;
        assertEquals(0x05, b[4]) ;
        assertEquals(0x06, b[5]) ;
        assertEquals(0x07, b[6]) ;
        assertEquals(0x08, b[7]) ;

        assertEquals(0x11, b[0+8]) ;
        assertEquals(0x12, b[1+8]) ;
        assertEquals(0x13, b[2+8]) ;
        assertEquals(0x14, b[3+8]) ;
        assertEquals(0x15, b[4+8]) ;
        assertEquals(0x16, b[5+8]) ;
        assertEquals(0x17, b[6+8]) ;
        assertEquals(0x18, b[7+8]) ;
    }

    @Test public void packLong7()
    {
        byte[] b = new byte[8] ;
        Bytes.setLong(0xF1F2F3F4F5F6F7F8L,b) ;
        long i = Bytes.getLong(b) ;
        assertEquals (0xF1F2F3F4F5F6F7F8L,i) ;
    }
    
    @Test public void packLong8()
    {
        byte[] b = new byte[16] ;
        Bytes.setLong(0xF1F2F3F4F5F6F7F8L,b,0) ;
        Bytes.setLong(0xA1A2A3A4A5A6A7A8L,b,8) ;

        long i1 = Bytes.getLong(b,0) ;
        long i2 = Bytes.getLong(b,8) ;
        assertEquals(0xF1F2F3F4F5F6F7F8L,i1) ;
        assertEquals(0xA1A2A3A4A5A6A7A8L,i2) ;
    }

    @Test public void compare1()    { compare(0, new byte[]{}, new byte[]{}) ; }
    @Test public void compare2()    { compare(+1, new byte[]{1}, new byte[]{}) ; }
    @Test public void compare3()    { compare(-1, new byte[]{1}, new byte[]{1,2}) ; }

    @Test public void compare4()    { compare(+1, new byte[]{1,3}, new byte[]{1,2}) ; }
    @Test public void compare5()    { compare(+1, new byte[]{1,3}, new byte[]{1,2,3}) ; }
    @Test public void compare6()    { compare(-1, new byte[]{1,2}, new byte[]{1,2,3}) ; }
    
    byte[] bytes = { 1, 2, 3, 4, 5, 6 } ;
    
    @Test public void slice1()
    {
        byte[] x = Bytes.copyOf(bytes) ;
        assertArrayEquals(bytes, x) ;
    }
    
    @Test public void slice3()
    {
        byte[] x = Bytes.copyOf(bytes,3) ;
        byte[] y = new byte[]{4,5,6} ;
        assertArrayEquals(y, x) ;
    }
    
    @Test public void slice2()
    {
        byte[] x = Bytes.copyOf(bytes,3,2) ;
        byte[] y = new byte[]{4,5} ;
        assertArrayEquals(y, x) ;
    }
    
    private static void compare(int expected, byte[] b1, byte[] b2)
    {
        int x = Bytes.compare(b1, b2) ;
        if ( x > 0 && expected > 0 ) return ;
        if ( x == 0 && expected == 0 ) return ;
        if ( x < 0 && expected < 0 ) return ;
        fail("Does not compare: "+Bytes.asHex(b1)+" :: "+Bytes.asHex(b2)) ;
    }
    
    private static void codec(String str)
    {
        ByteBuffer bb = ByteBuffer.allocate(16) ; 
        Bytes.toByteBuffer(str, bb) ;
        bb.flip() ;
        String str2 = Bytes.fromByteBuffer(bb) ;
        assertEquals(str, str2) ;
    }
    
    static private final String asciiBase             = "abc" ;
    static private final String latinBase             = "Àéíÿ" ;
    static private final String latinExtraBase        = "ỹﬁﬂ" ;  // fi-ligature, fl-ligature
    static private final String greekBase             = "αβγ" ;
    static private final String hewbrewBase           = "אבג" ;
    static private final String arabicBase            = "ءآأ";
    static private final String symbolsBase           = "☺☻♪♫" ;
    static private final String chineseBase           = "孫子兵法" ; // The Art of War 
    static private final String japaneseBase          = "日本" ;    // Japanese
    
    @Test public void codec1()  { codec(asciiBase) ; }
    @Test public void codec2()  { codec("") ; }
    @Test public void codec3()  { codec(greekBase) ; }
    @Test public void codec4()  { codec(hewbrewBase) ; }
    @Test public void codec5()  { codec(arabicBase) ; }
    @Test public void codec6()  { codec(symbolsBase) ; }
    @Test public void codec7()  { codec(chineseBase) ; }
    @Test public void codec8()  { codec(japaneseBase) ; }
}

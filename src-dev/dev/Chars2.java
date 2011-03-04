/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.ByteArrayInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;
import java.nio.ByteBuffer ;

import org.junit.Assert ;
import org.junit.Test ;
import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.io.StreamUTF8 ;
import org.openjena.atlas.lib.Bytes ;

public class Chars2
{
    // In Modified UTF-8,[15] the null character (U+0000) is encoded as 0xC0,0x80; this is not valid UTF-8[16]
    // Char to bytes.
    /* http://en.wikipedia.org/wiki/UTF-8
Bits    Last code point     Byte 1  Byte 2  Byte 3  Byte 4  Byte 5  Byte 6
  7   U+007F  0xxxxxxx
  11  U+07FF  110xxxxx    10xxxxxx
  16  U+FFFF  1110xxxx    10xxxxxx    10xxxxxx
  21  U+1FFFFF    11110xxx    10xxxxxx    10xxxxxx    10xxxxxx
  26  U+3FFFFFF   111110xx    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx
  31  U+7FFFFFFF  1111110x    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx     */
    
    
    /** char to int, where int is value, at the low end of the int, of the UTF-8 encoding. */
    
    static public byte[] toUTF8(char ch)
    {
        if ( ch != 0 && ch <= 127 ) return new byte[] {(byte)ch } ;
        if ( ch == 0 ) return new byte[] { (byte)0xC0, (byte)0x80 } ;
        
        if ( ch <= 0x07FF )
        {
            int x = 0 ;
            @SuppressWarnings("cast")
            final int v = (int)ch ;
            // x = low 11 bits yyyyy xxxxxx
            // x = 00000yyyyyxxxxxx
            //x1 = 110yyyyy    x2 = 10xxxxxx
            
            // Hi 5 bits
            int x1 = (v & 0x7C0) >>6 ; //BitsInt.access(ch, 21, 26) ;
            x1 = x1 | 0xC0 ; 

            int x2 = v&0x3F ; //BitsInt.access(ch, 26, 32) ;
            x2 = x2 | 0x80 ;
            return new byte[] {(byte)x1, (byte)x2} ;
        }
        if ( ch <= 0xFFFF )
        {
            int x = 0 ;
            @SuppressWarnings("cast")
            final int v = (int)ch ;
            // x =  aaaa bbbbbb cccccc
            //x1 = 1110aaaa    x2 = 10bbbbbb x3 = 10cccccc
            int x1 = (v>>12)&0x1F ;
            x1 = x1 | 0xE0 ;
            
            int x2 = (v>>6)&0x3F ;
            x2 = x2 | 0x80 ;
            
            int x3 = v&0x3F ;
            x3 = x3 | 0x80 ;

            return new byte[] {(byte)x1, (byte)x2, (byte)x3} ;
        }

        
        if ( true ) throw new AtlasException() ;
        // Not java, where chars are 16 bit.
        if ( ch <= 0x1FFFFF ) ; 
        if ( ch <= 0x3FFFFFF ) ; 
        if ( ch <= 0x7FFFFFFF ) ;
        
        return null ;
        
    }
    
    /** Encode a char as UTF-8, using Java's built-in encoders - may be slow - this is for testing */
    static public byte[] toUTF8_test(char ch)
    {
        byte[] bytes = new byte[4] ;
        ByteBuffer bb = ByteBuffer.wrap(bytes) ;
        String s = ""+ch ;
        int len = Bytes.toByteBuffer(s, bb) ;
        byte[] bytes2 = new byte[len] ;
        for ( int i = 0 ; i < len ; i++ )
            bytes2[i] = bytes[i] ; 
        return bytes2 ;
    }
    
    /** Encode a char as UTF-8, using Java's built-in encoders -         {
            e.printStackTrace(); rturn 
        }
may be slow - this is for testing */
    static public char fromUTF8_test(byte[] x)
    {
        InputStream in = new ByteArrayInputStream(x) ;
        Reader r = new StreamUTF8(in) ;
        try
        {
            return (char)r.read() ;
        } catch (IOException e) { throw new AtlasException(e) ; }
    }
    
    static public char fromUTF8(byte[] x)
    {
        // DRY: Stream UTF8.
        // Fastpath            //if ( (x & 0xE0) == 0xC0 ) 

        if ( x == null || x.length == 0 )
            return (char)0 ;
        //if ( x <= 127 )
        if ( x.length == 1 )
            return (char)x[0] ;

        //if ( x <= 0xFFFF )
        if ( x.length == 2 )
        {
            int hi = x[0] & 0x1F ;
            int lo = x[1]&0x3F ;
            return (char)((hi<<6)|lo) ;
        }
        //  1110.... => 3 bytes : 16 bits : not outside 16bit chars 
        //if ( x <= 0xFFFFFF )
        if ( x.length == 3 )
        {
//            int b0 = (x>>16) & 0x1F ;
//            int b1 = (x>>8)  & 0x3F ;
//            int b2 = x&0x3F ;
          int b0 = x[0] & 0x1F ;
          int b1 = x[1] & 0x3F ;
          int b2 = x[2] & 0x3F ;
            return (char)( (b0<<12) | (b1<<6) | b2 ) ;
        }
        
        throw new AtlasException("Out of range: "+x) ;
    }
    
    
    // UTF-8 encoding.
    // character '¢' = code point U+00A2 -> C2 A2
    // character '€' = code point U+20AC -> E2 82 AC
    
    @Test public void utf8_1() { testChar(' ') ; }
    @Test public void utf8_2() { testChar('¢') ; }
    @Test public void utf8_3() { testChar('€') ; }
    @Test public void utf8_4() { testChar('\uFFFF') ; }
    
    @Test public void utf8_b1() { testBytes((byte)20) ; }
    @Test public void utf8_b2() { testBytes((byte)0xC2, (byte)0xA2) ; }
    @Test public void utf8_b3() { testBytes((byte)0xE2, (byte)0x82, (byte)0xAC) ; }
    @Test public void utf8_b4() { testBytes((byte)0xE2, (byte)0xBF, (byte)0xBF) ; }


    private void testChar(char c)
    {
        byte[] b = toUTF8(c) ;
        char c2 = fromUTF8(b) ;
        Assert.assertEquals(c, c2) ;
    }

    private void testBytes(byte ...b)
    {
        char c = fromUTF8(b) ;
        byte[] b2 = toUTF8(c) ;
        Assert.assertArrayEquals(b, b2) ;
    }

}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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
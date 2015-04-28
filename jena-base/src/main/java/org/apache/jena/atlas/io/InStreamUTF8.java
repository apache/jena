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
import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.atlas.AtlasException ;

/** Fast and streaming UTF-8 */
public final class InStreamUTF8 extends Reader implements CharStream
{
    // TODO Add line and col counts.
    // See arq.utf8. 
    // TODO Better ready()/available() in InputStreamBuffered
    
    // The standard Java way of doing this is via charset decoders.
    // One small disadvantage is that bad UTF-8 does not get flagged as to
    // the byte position of the error.
    
    // This class collects knowledge of how UTF-8 encoding works;
    // the Java classes are usually slightly faster compared to using
    // this class with an InputStreamBuffered but the difference is small.
    // This class generated meaningful error messages (when line/col added).
    
    // The Java classes copy-convert a byte buffer into a char buffer.
    // Sometimes, for example in a parser, this isn't a convenient model
    // because the app is looking one character at a time and accumulating
    // the chars until it sees the end of a token of arbitrary length
    // or processes escape sequences.  
    //
    // The app might use a StringBuilder so the bytes get copied into
    // a char buffer and out again.  Instead, this code assumes the
    // app is in charge of that.
    
    // UTF-8 (UTF-16) is different from other character sets because 
    // the relationship with Java's internal character representation is
    // arithmetic, not a character mapping. 
    
    // Todo: chars > 16 bits -> surrogate pairs. 
    
    /*
     * http://en.wikipedia.org/wiki/UTF-8
     * http://tools.ietf.org/html/rfc3629
     * http://www.ietf.org/rfc/rfc3629.txt
     * 
     * Unicode                                  Byte1       Byte2       Byte3       Byte4
     * U+0000–U+007F    0 to 127                0xxxxxxx
     * U+0080–U+07FF    128 to 2,047            110yyyxx    10xxxxxx 
     * U+0800–U+FFFF    2,048 to 65,535         1110yyyy    10yyyyxx    10xxxxxx
     * U+10000–U+10FFFF 65,536 to 1,114,111     11110zzz    10zzyyyy    10yyyyxx    10xxxxxx
     * 
     * Restricted cases (RFC 3629)
     * 11110101-11110111    F5-F7   245-247     start of 4-byte sequence for codepoint above 10FFFF
     * 11111000-11111011    F8-FB   248-251     start of 5-byte sequence
     * 11111100-11111101    FC-FD   252-253     start of 6-byte sequence
     * 
     * Illegal:
     * 11000000-11000001    C0-C1   192-193     Overlong encoding: start of a 2-byte sequence, but code point <= 127
     * 11111110-11111111    FE-FF   254-255     Invalid: not defined by original UTF-8 specification
     */
    
    // There is some sort of stream decoder backing the Sun implementation 
    // of CharsetDecoder (sun.io.StreamDecoder) but it's not on all platforms
    // I want a known decoder specifically for UTF8
    
    private InputStreamBuffered input ;
    //private long count = 0 ;

    public InStreamUTF8(InputStream in)
    {
        if ( in instanceof InputStreamBuffered )
        {
            input = (InputStreamBuffered)in ;
            return ;
        }
        input = new InputStreamBuffered(in) ;
    }
    
    public InStreamUTF8(InputStreamBuffered in) { input = in ; }
    

    @Override
    public boolean ready() throws IOException
    {
        return input.available() > 0 ;
    }
    
    @Override
    public void close() throws IOException
    { input.close() ; }
    
    @Override
    public void closeStream()
    { IO.close(input) ; }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        // Doing this on a block of bytes may be faster.
        for ( int i = off ; i < off+len ; i++ )
        {
            int x = read() ;
            if ( x == -1 )
            {
                if ( i == off )
                    return -1 ;
                return (i-off) ;
            }
            cbuf[i] = (char)x ;
        }
        return len ; 
    }

    @Override
    public final int read() throws IOException
    { 
        int ch = advance(input) ;
        //if ( ! Character.isDefined(ch) ) throw new AtlasException(String.format("Undefined codepoint: 0x%04X", ch)) ;
        return ch ;
    }
    
    
    /** Next codepoint, given the first byte of any UTF-8 byte sequence is already known.
     *  Not necessarily a valid char (this function can be used a straight UTF8 decoder
     */
    @Override
    public final int advance()
    { return advance(input) ; }
    
    /** Next codepoint */
    public static final int advance(InputStreamBuffered input)
    {
        int x = input.advance() ;
        if ( x == -1 ) return -1 ;
        return advance(input, x) ;
    }
    
    /** Next codepoint, given the first byte of any UTF-8 byte sequence is already known.
     * Not necessarily a valid char (this function can be used a straight UTF8 decoder
     */
    
    public static final int advance(InputStreamBuffered input, int x)
    {
        //count++ ;
        // Fastpath
        if ( x == -1 || x <= 127 ) 
        {
            //count++ ;
            return x ;
        }

        // 10 => extension byte
        // 110..... => 2 bytes
        if ( (x & 0xE0) == 0xC0 )
        {
            int ch = readMultiBytes(input, x & 0x1F, 2) ;
            // count += 2 ;
            return ch ;
            
        }
        //  1110.... => 3 bytes : 16 bits : not outside 16bit chars 
        if ( (x & 0xF0) == 0xE0 ) 
        {
            int ch = readMultiBytes(input, x & 0x0F, 3) ;
            // count += 3 ;
            //if ( ! Character.isDefined(ch) ) throw new AtlasException(String.format("Undefined codepoint: 0x%04X", ch)) ;
            return ch ;
        }

        // Looking like 4 byte charcater.
        int ch = -2 ;
        // 11110zzz => 4 bytes.
        if ( (x & 0xF8) == 0xF0 )
        {
             ch = readMultiBytes(input, x & 0x08, 4) ;
             // Opsp - need two returns. Character.toChars(ch, chars, 0) ;
             // count += 4 ;
        }
             
        else 
            IO.exception(new IOException("Illegal UTF-8: "+x)) ;

        // This test will go off.  We're processing a 4 byte sequence but Java only supports 16 bit chars. 
        if ( ch > Character.MAX_VALUE )
            throw new AtlasException("Out of range character (must use a surrogate pair)") ;
        if ( ! Character.isDefined(ch) ) throw new AtlasException(String.format("Undefined codepoint: 0x%04X", ch)) ;
        return ch ;
    }
    
    private static int readMultiBytes(InputStreamBuffered input, int start, int len) //throws IOException
    {
        //System.out.print(" -("+len+")") ; p(start) ;
        
        int x = start ;
        for ( int i = 0 ; i < len-1 ; i++ )
        {
            int x2 = input.advance() ;
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

    private static void p(int ch)
    {
        System.out.printf(" %02X", ch) ;
        if ( ch == -1 )
            System.out.println();
    }
    
    public static String decode(byte[] bytes)
    {
        try
        {
            char[] chars = new char[bytes.length] ;
            InputStream in = new ByteArrayInputStream(bytes) ;
            StringBuilder buff = new StringBuilder() ;
            Reader r = new InStreamUTF8(in) ;
            int len ;
            len = r.read(chars) ;
            IO.close(r) ;
            return new String(chars, 0, len) ;
        } catch (IOException ex)
        {
            IO.exception(ex) ;
            return null ;
        }
    }
}

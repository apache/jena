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

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.Writer ;

/** Output UTF-8 encoded data.
 *  This class implements the "Modified UTF8" encoding rules (null -> C0 80)
 *  It will encode any 16 bit value.  
 *  It can be used as a pure UTf-8 encoder. 
 * 
 *  @see InStreamUTF8
 */
public final class OutStreamUTF8 extends Writer
{
    private OutputStream out ;

    public OutStreamUTF8(OutputStream out)
    {
        // Buffer?
        this.out = out ;
    }
    
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {
        for ( int i = 0 ; i < len; i++ )
            write(cbuf[off+i]) ;
    }
    
    @Override
    public void write(int ch) throws IOException
    { output(out, ch) ; }
    
    @Override
    public void write(char[] b) throws IOException
    { write(b, 0, b.length) ; }
    
    @Override
    public void write(String str) throws IOException
    { write(str,0, str.length()) ; }
    
    @Override
    public void write(String str, int idx, int len) throws IOException
    {
        for ( int i = 0 ; i < len; i++ )
            write(str.charAt(idx+i)) ;
    }

    public void output(int x)
    { 
        try { 
            output(out, x) ;
        } catch (IOException ex) { IO.exception(ex) ; }
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
    public static void output(OutputStream out, int ch) throws IOException
    {
        if ( ch != 0 && ch <= 127 )
        {
            // 7 bits
            out.write(ch) ;
            return ;
        }
        
        if ( ch == 0 )
        {
            // Modified UTF-8.
            out.write(0xC0) ;
            out.write(0x80) ;
            return ;
        }
        
        // Better? output(int HiMask, int byte length, int value) 
        
        if ( ch <= 0x07FF )
        {
            // 11 bits : 110yyyyy 10xxxxxx
            // int x1 = ( ((ch>>(11-5))&0x7) | 0xC0 ) ; outputBytes(out, x1, 2, ch) ; return ;
            int x1 = ( ((ch>>(11-5))&0x01F ) | 0xC0 ) ; 
            int x2 = ( (ch&0x3F)  | 0x80 ) ;
            out.write(x1) ;
            out.write(x2) ;
            return ;
        }
        if ( ch <= 0xFFFF )
        {
            // 16 bits : 1110aaaa  10bbbbbb  10cccccc
            // int x1 = ( ((ch>>(16-4))&0x7) | 0xE0 ) ; outputBytes(out, x1, 3, ch) ; return ;
            int x1 = ( ((ch>>(16-4))&0x0F) | 0xE0 ) ;
            int x2 = ( ((ch>>6)&0x3F) | 0x80 ) ;
            int x3 = ( (ch&0x3F) | 0x80 ) ;
            out.write(x1) ;
            out.write(x2) ;
            out.write(x3) ;
            return ;
        }
        
//        if ( Character.isDefined(ch) )
//            throw new AtlasException("not a character") ;
        
        //if ( true ) throw new InternalErrorException("Valid code point for Java but not encodable") ;
        
        // Not java, where chars are 16 bit.
        if ( ch <= 0x1FFFFF )
        {
            // 21 bits : 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            int x1 = ( ((ch>>(21-3))&0x7) | 0xF0 ) ;
            outputBytes(out, x1, 4, ch) ;
            return ;
        }
        if ( ch <= 0x3FFFFFF )
        {
            // 26 bits : 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
            int x1 = ( ((ch>>(26-2))&0x3) | 0xF8 ) ;
            outputBytes(out, x1, 5, ch) ;
            return ;
        }

        if ( ch <= 0x7FFFFFFF )
        {
            // 32 bits : 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
            int x1 = ( ((ch>>(32-1))&0x1) | 0xFC ) ;
            outputBytes(out, x1, 6, ch) ;
            return ;
        }
    }
    
    private static void outputBytes(OutputStream out, int x1, int byteLength, int ch) throws IOException
    {
        // ByteLength = 3 => 2 byteLenth => shift=6 and shift=0  
        out.write(x1) ;
        byteLength-- ; // remaining bytes
        for ( int i = 0 ; i < byteLength ; i++ )
        {
            // 6 Bits, loop from high to low  
            int shift = 6*(byteLength-i-1) ;
            int x =  (ch>>shift) & 0x3F ;
            x = x | 0x80 ;  // 10xxxxxx
            out.write(x) ;
        }
    }

    @Override
    public void flush() throws IOException
    { out.flush(); }

    @Override
    public void close() throws IOException
    { out.close() ; }

}

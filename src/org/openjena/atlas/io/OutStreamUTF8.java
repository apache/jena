/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.io;

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.Writer ;

import org.openjena.atlas.AtlasException ;

/** Output UTF chars 
 */
public class OutStreamUTF8 extends Writer
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

    public static void output(OutputStream out, int ch) throws IOException
    {
        if ( ch != 0 && ch <= 127 )
        {
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
        
        if ( ch <= 0x07FF )
        {
            //  x = low 11 bits yyyyy xxxxxx
            //  x = 00000yyyyyxxxxxx
            // x1 = 110yyyyy    x2 = 10xxxxxx
            
            // Hi 5 bits
            int x1 = ( ((ch & 0x7C0) >>6) | 0xC0 ) ; 
            int x2 = ( (ch&0x3F)  | 0x80 ) ;
            
            out.write(x1) ;
            out.write(x2) ;
            return ;
        }
        if ( ch <= 0xFFFF )
        {
            //  x =  aaaa bbbbbb cccccc
            // x1 = 1110aaaa    x2 = 10bbbbbb x3 = 10cccccc
            int x1 = ( ((ch>>12)&0x1F) | 0xE0 ) ;
            int x2 = ( ((ch>>6)&0x3F) | 0x80 ) ;
            int x3 = ( (ch&0x3F) | 0x80 ) ;
            out.write(x1) ;
            out.write(x2) ;
            out.write(x3) ;
            return ;
        }
        
        if ( true ) throw new AtlasException() ;
        // Not java, where chars are 16 bit.
        if ( ch <= 0x1FFFFF ) ; 
        if ( ch <= 0x3FFFFFF ) ; 
        if ( ch <= 0x7FFFFFFF ) ;
    }
    
    @Override
    public void flush() throws IOException
    { out.flush(); }

    @Override
    public void close() throws IOException
    { out.close() ; }

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
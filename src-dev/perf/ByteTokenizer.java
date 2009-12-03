/*
 * (c) Copyright 2009 Talis Information Ltd
 * All rights reserved.
 * [See end of file]
 */

package perf;

import java.io.BufferedInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.nio.ByteBuffer ;
import java.nio.CharBuffer ;
import java.nio.charset.Charset ;
import java.nio.charset.CharsetDecoder ;

import com.hp.hpl.jena.riot.tokens.Token ;

// Deal directly with bytes.

// Exception costs?

public class ByteTokenizer
{
    /*
     * Have a large work buffer (8K+)
     * Read into that and tokenize over that.
     * Cope with entities spanning by occassional copy over to start of buffer and refill. 
     *
     * new String(byte[] bytes, int offset, int length, Charset charset) 
     *  
     */
    
    static Charset charsetUTF8 = Charset.forName("utf-8") ;
    
    public void parse(InputStream in) throws IOException 
    {
        // Possibilities.
        ByteBuffer bb = ByteBuffer.allocate(0) ;
        CharBuffer cb = CharBuffer.allocate(0) ;
        CharsetDecoder cd = charsetUTF8.newDecoder() ;
        cd.decode(bb, cb, false) ;
        
        StringBuilder sb = new StringBuilder() ;
        sb.toString() ;
        
        PeekInputStream input = new PeekInputStream(in) ;
        
        for (;;)
        {
            next(input) ;
        }
        
    }

    InputStream input ;
    byte[] workspace = new byte[8*1024] ; 
    int tStart = 0 ;
    int tEnb = 0 ;
    
    // Keep keepStart to end bytes  
    int refill(int keepStart) throws IOException
    {
        int idx = 0 ;
        int N = workspace.length ;
        if ( keepStart >= 0 && keepStart < workspace.length )
        {
            // Copy down.
            System.arraycopy(workspace, keepStart, workspace, 0, workspace.length-keepStart) ;
            idx = keepStart ;
            N = keepStart ;
            
        }
        int len = input.read(workspace, keepStart, N) ;
        // New bytes start at keepStart
        return len ; 
    }

    private Token next(PeekInputStream in) throws IOException
    {
        skipWhitespace(in) ;
        int b = in.peek() ;
        
        int i = 0; 
        int j = 1 ;
        
        switch (b)
        {
            case '<':
            {
                // move on one.
                while( b != '>' )
                {
                    // Look for '>'
                    // If run over buffer, copy/restart.
                    // Make string.
                    
                }
                
            }
            //case '"':
            
        }
        return null ;
    }

    private void skipWhitespace(PeekInputStream in) throws IOException
    {
        for(;;)
        {
            switch (in.peek())
            {
                case ' ': case '\r' : case '\n':
                    in.next() ;
                    continue ;
                default:
                    break ;
            }
        }
    }

    static final class PeekInputStream
    {
        int peekByte = -1 ;
        private InputStream in ;
        
        public PeekInputStream(InputStream in) throws IOException
        {
            if ( ! ( in instanceof BufferedInputStream ) )
                // sync warning
                in = new BufferedInputStream(in) ;

            this.in = in ;
            peekByte = in.read() ;
        }
        
        public int peek() { return peekByte ; }
        
        public int next() throws IOException
        {
            int b = peekByte ;
            b = in.read() ;
            return b ;
        }
    }
    
}

/*
 * (c) Copyright 2009 Talis Information Ltd
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
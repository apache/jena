/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.io;

import static org.openjena.atlas.io.IO.EOF ;

import java.io.IOException ;
import java.io.InputStream ;

/** InputStream optimizing for one byte at a time operation.
 *  BufferedInputStream operations have synchronization making 
 *  reading one byte at a time expensive.
 *  
 *  @see java.io.InputStream
 *  @see java.io.BufferedInputStream
 */
public final class InputStreamBuffered extends InputStream 
{
    public static int DFT_BUFSIZE = 16*1024 ;
    private InputStream source ;
    private byte[] buffer ;
    private int buffLen = 0 ;
    private int idx = 0 ;
    private long count = 0 ;
    
    public InputStreamBuffered(InputStream input)
    {
        this(input, DFT_BUFSIZE) ;
    }
    
    public InputStreamBuffered(InputStream input, int bufsize)
    {
        super() ;
        this.source = input ;
        this.buffer = new byte[bufsize] ;
        this.idx = 0 ;
        this.buffLen = 0 ;
    }

//    @Override
//    public int read(byte b[], int off, int len) throws IOException
//    {
//    }
    
    @Override
    public int read() throws IOException
    {
        return advance() ;
    }
    
    @Override
    public void close() throws IOException
    {
        source.close() ;
    }
    
    //@Override
    public final int advance()
    {
        if ( idx >= buffLen )
            // Points outside the array.  Refill it 
            fillArray() ;
        
        // Advance one character.
        if ( buffLen >= 0 )
        {
            byte ch = buffer[idx] ;
            // Advance the lookahead character
            idx++ ;
            count++ ;
            return ch & 0xFF ;
        }  
        else
            // Buffer empty, end of stream.
            return EOF ;
    }

    private int fillArray()
    {
        try
        {
            int x = source.read(buffer) ;
            idx = 0 ;
            buffLen = x ;   // Maybe -1
            return x ;
        } 
        catch (IOException ex) { IO.exception(ex) ; return -1 ; }
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
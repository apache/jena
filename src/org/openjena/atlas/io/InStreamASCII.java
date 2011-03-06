/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.io;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;

import org.openjena.atlas.AtlasException ;

/** Fast and streaming.
 */
public final class InStreamASCII extends Reader implements CharStream
{
    private InputStreamBuffered input ;
    private long count = 0 ;

    public InStreamASCII(InputStream in)
    {
        if ( in instanceof InputStreamBuffered )
        {
            input = (InputStreamBuffered)in ;
            return ;
        }
        input = new InputStreamBuffered(in) ;
    }
    
    public InStreamASCII(InputStreamBuffered in) { input = in ; }

    @Override
    public boolean ready() throws IOException
    {
        return input.available() > 0 ;
    }
    
    @Override
    public void close() throws IOException
    { input.close() ; }

    //@Override
    public void closeStream()
    { IO.close(input) ; }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        for ( int i = off ; i < off+len ; i++ )
        {
            int x = read() ;
            if ( x == -1 )
            {
                if ( i == off )
                    return -1 ;
                return (i-off) ;
            }
            if ( x > 128 )
                throw new AtlasException("Illegal ASCII character : "+x) ;
            cbuf[i] = (char)x ;
        }
        return len ; 
    }

    @Override
    public int read() throws IOException
    { return advance() ; }
    
    public int advance()
    {
        count++ ;
        return input.advance() ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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
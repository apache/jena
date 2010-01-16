/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package atlas.io;

import java.io.IOException ;
import java.io.InputStream ;

import atlas.lib.AtlasException ;

final class PeekInputStreamSource extends PeekInputStream
{
    // See PeekReaderSource
   private static final int SIZE       = 32 * 1024 ;
    
    private final byte[] bytes ;
    private int buffLen ;
    private int idx ;

    private final Source source;
    
    /*package*/ PeekInputStreamSource(InputStream in)
    { this(in, SIZE) ; }

    /*package*/ PeekInputStreamSource(InputStream in, int buffSize)
    {
        super() ;
        source = new SourceInputStream(in) ;
        bytes = new byte[buffSize] ;
    }

    // Local adapter/encapsulation
    private interface Source
    { 
        int fill(byte[] array) ;
        void close() ; 
    }
    
    static final class SourceInputStream implements Source
    {
        final InputStream input ;
        SourceInputStream(InputStream in) { input = in ; }
        
        //@Override
        public void close()
        { 
            try { input.close() ; } catch (IOException ex) { exception(ex) ; } 
        }
        
        //@Override
        public int fill(byte[] array)
        {
            try { return input.read(array) ; } catch (IOException ex) { exception(ex) ; return -1 ; }
        }
    }
    
    @Override
    protected final int advance()
    {
        if ( idx >= buffLen )
            // Points outside the array.  Refill it 
            fillArray() ;
        
        // Advance one byte.
        if ( buffLen >= 0 )
        {
            byte b = bytes[idx] ;
            // Advance the lookahead character
            idx++ ;
            return b ;
        }  
        else
            // Buffer empty, end of stream.
            return EOF ;
    }

    private int fillArray()
    {
        int x = source.fill(bytes) ;
        idx = 0 ;
        buffLen = x ;   // Maybe -1
        return x ;
    }

    
    @Override
    protected void closeInput()
    {
        source.close() ;
    }

    private static void exception(IOException ex)
    { throw new AtlasException(ex) ; }

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
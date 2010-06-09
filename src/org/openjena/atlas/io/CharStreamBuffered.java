/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.io;

import static org.openjena.atlas.io.IO.EOF ;

import java.io.IOException ;
import java.io.Reader ;

/** Buffering reader without the (hidden) sync overhead in BufferedReader
 * 
 * @see java.io.BufferedReader
 */ 


public final class CharStreamBuffered extends CharStreamReader
{
    /*package*/ static final int CB_SIZE       = 16 * 1024 ;
    
    private final char[] chars ;            // CharBuffer?
    private int buffLen = 0 ;
    private int idx = 0 ;

    private final Source source;
    
    public CharStreamBuffered(Reader r)
    { this(r, CB_SIZE) ; }

    /**
     * @param r
     * @param buffSize
     */
    public CharStreamBuffered(Reader r, int buffSize)
    {
        super() ;
        source = new SourceReader(r) ;
        chars = new char[buffSize] ;
    }

    // Local adapter/encapsulation
    private interface Source
    { 
        int fill(char[] array) ;
        void close() ; 
    }
    
    static final class SourceReader implements Source
    {
        final Reader reader ;
        SourceReader(Reader r) { reader = r ; }
        
        //@Override
        public void close()
        { 
            try { reader.close() ; } catch (IOException ex) { IO.exception(ex) ; } 
        }
        
        //@Override
        public int fill(char[] array)
        {
            try { return reader.read(array) ; } catch (IOException ex) { IO.exception(ex) ; return -1 ; }
        }
    }
    
//    /** Faster?? for ASCII */
//    static final class SourceASCII implements Source
//    {
//        final InputStream input ;
//        SourceASCII(InputStream r) { input = r ; }
//        public void close()
//        { try { input.close() ; } catch (IOException ex) { exception(ex) ; } } 
//        
//        public final int fill(char[] array)
//        {
//            try {
//                // Recycle.
//                byte[] buff = new byte[array.length] ;
//                int len = input.read(buff) ;
//                for ( int i = 0 ; i < len ; i++ )
//                {
//                    byte b = buff[i] ;
//                    if ( b < 0 )
//                        throw new AtlasException("Illegal ASCII charcater: "+b) ;
//                   array[i] = (char)b ;
//                }
//                return len ;
//            } catch (IOException ex) { exception(ex) ; return -1 ; }
//        }
//    }
//    
//    static final class SourceChannel implements Source
//    {
//        final ReadableByteChannel channel ;
//        CharsetDecoder decoder = Chars.createDecoder() ;
//        SourceChannel(ReadableByteChannel r) { channel = r ; }
//        
//        //@Override
//        public void close()
//        { 
//            try { channel.close() ; } catch (IOException ex) { exception(ex) ; } 
//        }
//        
//        //@Override
//        public int fill(char[] array)
//        {
//            // Encoding foo.
////             Bytes
////             
////            ByteBuffer b = ByteBuffer.wrap(null) ;
////            
////            try { return channel.read(null).read(array) ; } catch (IOException ex) { exception(ex) ; return -1 ; }
//            return -1 ;
//        }
//    }

    @Override
    public final int advance()
    {
        if ( idx >= buffLen )
            // Points outside the array.  Refill it 
            fillArray() ;
        
        // Advance one character.
        if ( buffLen >= 0 )
        {
            char ch = chars[idx] ;
            // Advance the lookahead character
            idx++ ;
            return ch ;
        }  
        else
            // Buffer empty, end of stream.
            return EOF ;
    }

    private int fillArray()
    {
        int x = source.fill(chars) ;
        idx = 0 ;
        buffLen = x ;   // Maybe -1
        return x ;
    }

    
    @Override
    public void closeStream()
    {
        source.close() ;
    }
}


/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
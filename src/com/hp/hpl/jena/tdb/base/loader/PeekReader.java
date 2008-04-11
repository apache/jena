/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.loader;

import java.io.IOException;
import java.io.Reader;

/** Parsing-centric reader.  Faster than using BufferedReader, sometimes a lot fatser.
 *  1/ One character lookahead.
 *  2/ Line count
 *  3/ Not thread safe.
 * @author Andy Seaborne
 */ 


public final class PeekReader extends Reader
{
    // Does buffering here instead of using a BufferedReader help?
    // YES.  A lot (Java6).
    // Possibly because BufferedReader internally are synchronized, possibly
    // because this is so stripped down the JIT does a better job. 
    
    static final int CB_SIZE = 8*1024 ;
    static final byte CHAR0 = (char)0 ;
    static final int  EOF = -1 ;
    static final int  UNSET = -2 ;
    
    private char[] chars = new char[CB_SIZE];
    
    private int buffLen = 0 ;
    private int idx = 0 ;

    private int currChar = UNSET ;

    private Reader in;
    private int colNum = 0;
    private int lineNum = 1;

    public static PeekReader make(Reader r)
    {
        if ( r instanceof PeekReader )
            return (PeekReader)r ;
        return new PeekReader(r) ;
    }
    
    private PeekReader(Reader in)
    {
        this(in, CB_SIZE) ;
    }
    
    PeekReader(Reader in, int buffSize)
    {
        this.chars = new char[buffSize];
        this.in = in;
        oneChar() ;    // Advance always so that the peek character is valid.
        if ( currChar == UNSET )
            currChar = EOF ;
    }

    public int getLineNum()         { return lineNum; }

    public int getColNum()          { return colNum; }

    public int peekChar()           { return currChar ; }
    
    public int readChar()           { return oneChar() ; }
    
    // Reader operations
    @Override
    public void close() throws IOException
    {
        in.close() ;
    }

    @Override
    public int read() throws IOException
    {
        if ( eof() )
            return EOF ;
        int x = oneChar() ;
        return x ;
    }
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        if ( eof() )
            return EOF ;
        // Note - need to preserve line count, so single char ops are reasonably efficient.
        for ( int i = 0 ; i < len ; i++ )
        {
            int ch = oneChar() ;
            if ( eof() )
                // Must have moved at least one character
                // due to eof() check at the start.
                return i ;  
            cbuf[i+off] = (char)ch ;
        }
        return len ;
    }

    // Ensure the buffer is not empty, or boolean eof is set
    private void fill()
    {
        try {
            if ( idx >= buffLen )
            {
                int x = in.read(chars) ;
                idx = 0 ;
                if ( x <= 0 )
                    currChar = EOF ;
                buffLen = x ;
            }
        }
        catch(IOException ex)
        {
            // ??? XXX ???
        }
    }

    private int oneChar()
    {
        int ch = currChar ;
        if ( ch == EOF )
            return EOF ;
        
        
        fill() ;
        if ( !eof() )
        {
            // Advance the lookhead character
            currChar = chars[idx] ;
            idx++ ;
    
            if (ch == '\n')
            {
                lineNum++;
                colNum = 1;
            } 
            else
                colNum++;
        }
        return ch ;
        
    }

    public boolean eof()   { return currChar == EOF ; }
}


/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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
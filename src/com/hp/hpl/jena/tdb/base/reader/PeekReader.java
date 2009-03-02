/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileUtils;

import lib.Log;

/** Parsing-centric reader.  Faster than using BufferedReader, sometimes a lot fatser.
 *  1/ One character lookahead.
 *  2/ Line count
 *  3/ Not thread safe.
 */ 


public final class PeekReader extends Reader
{
    // Possible separation:
    // An object that can fill the 'chars' array.  See fillArray.
    // CharBuffer?
    
    // Does buffering here instead of using a BufferedReader help?
    // YES.  A lot (Java6).
    
    // Possibly because BufferedReader internally are synchronized, possibly
    // because this is so stripped down the JIT does a better job.
    // **** read(char[]) is a loop of single char operations.
    
    private static final int CB_SIZE       = 16 * 1024 ;
    private static final int PUSHBACK_SIZE = 10 ; 
    static final byte CHAR0 = (char)0 ;
    static final int  EOF = -1 ;
    static final int  UNSET = -2 ;
    
    private final char[] chars ;            // CharBuffer?
    
    private char[] pushbackChars ;
    private int idxPushback ;
    
    private int buffLen ;
    private int idx ;

    private int currChar = UNSET ;

    private Reader in;
    private long posn ;
    private long colNum ;
    private long lineNum ;
    
    public static PeekReader make(Reader r)
    {
        // StringReader special?
        if ( r instanceof PeekReader )
            return (PeekReader)r ;
        if ( r instanceof BufferedReader )
            Log.warn(PeekReader.class, "BufferedReader passed to PeekReader") ;
            
        return new PeekReader(r) ;
    }
    
    public static PeekReader makeUTF8(InputStream in) 
    {
        Reader r = FileUtils.asUTF8(in) ;
        return make(r) ;
    }
    
    private PeekReader(Reader in)
    {
        this(in, CB_SIZE, PUSHBACK_SIZE) ;
    }
    
    /** Testing */
    public static PeekReader make(String x)         { return make(x, CB_SIZE) ; }
    static PeekReader make(String x, int buffSize)  { return new PeekReader(new StringReader(x), buffSize, PUSHBACK_SIZE) ; }
    
    private PeekReader(Reader in, int buffSize, int pushBackSize)
    {
        this.chars = new char[buffSize];
        this.buffLen = 0 ;
        this.idx = 0 ; 
        
        this.pushbackChars = new char[pushBackSize] ; 
        this.idxPushback = -1 ;
        
        this.in = in;
        this.colNum = 0;
        this.lineNum = 1;
        this.posn = 0 ;
        
        oneChar() ;    // Advance always so that the peek character is valid.
        if ( currChar == UNSET )
            setCurrChar(EOF) ;
    }

    public long getLineNum()            { return lineNum; }

    public long getColNum()             { return colNum; }

    public long getPosition()           { return posn; }

    public int peekChar()               { return currChar ; }
    
    public int readChar()               { return oneChar() ; }
    
    /** push back a character : does not alter underlying position, line or column counts*/  
    public void pushbackChar(int ch)    { unreadChar(ch) ; }
    
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
            if ( ch == EOF )
                return (i==0)? EOF : i ;
            cbuf[i+off] = (char)ch ;
        }
        return len ;
    }

    public final boolean eof()   { return currChar == EOF ; }

    // ----------------
    // The methods below are the only ones to manipulate the character buffers.
    // Other methods may read the state of variables.
    
    private void unreadChar(int ch)
    {
        // The push back buffer is in the order where [0] is the oldest.
        // Does not alter the line number, column number or position count. 
        
        if ( idxPushback >= pushbackChars.length )
        {
            // Enlarge pushback buffer.
            char[] pushbackChars2 = new char[pushbackChars.length*2] ;
            System.arraycopy(pushbackChars2, 0, pushbackChars2, 0, pushbackChars.length) ;
            pushbackChars = pushbackChars2 ;
            //throw new JenaException("Pushback buffer overflow") ;
        }
        if ( ch == EOF || ch == UNSET )
            throw new JenaException("Illegal character to push back: "+ch) ;
        
        idxPushback++ ;
        pushbackChars[idxPushback] = (char)ch ;
        setCurrChar(ch) ;
    }
    
    // Ensure the buffer is not empty, or boolean eof is set
    private void fillAndAdvance()
    {
        if ( idx >= buffLen )
            // Points outsize the array.  Refill it 
            fillArray() ;
        
        // Advance one character.
        if ( buffLen >= 0 )
        {
            // Advance the lookahead character
            setCurrChar(chars[idx]) ;
            idx++ ;
            posn++ ;
        }  
        else
            // Buffer empty, end of stream.
            setCurrChar(EOF) ;
    }

    private int fillArray()
    {
        try {
            int x = in.read(chars) ;
            idx = 0 ;
            buffLen = x ;   // Maybe -1
            return x ;
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err) ;
            return -1 ; 
        }
    }
    
    // Invariants.
    // currChar is either chars[idx-1] or pushbackChars[idxPushback]
    private int oneChar()
    {
        int ch = currChar ;
        if ( ch == EOF )
            return EOF ;
        
        if ( idxPushback >= 0 )
        {
            replayPushback() ;
            return ch ;
        }

        fillAndAdvance() ;
        
        if (ch == '\n')
        {
            lineNum++;
            colNum = 0;
        } 
        else
            colNum++;
        return ch ;
    }

    private void replayPushback()
    {
        idxPushback-- ;
        if ( idxPushback >=0 )
        {
            char ch2 = pushbackChars[idxPushback] ;
            setCurrChar(ch2) ;
            return ;
        }
        
        // Push back buffer empty.
        // Next char is from chars[] which must have yielded a
        // characater and idx >= 1 or the stream was zero chars (idx <= 0)
        int nextCurrChar = EOF ;
        
        if ( idx-1 >= 0 )
            // Had been a read.
            nextCurrChar = chars[idx-1] ;
        setCurrChar(nextCurrChar) ;
    }

    private void setCurrChar(int ch)
    {
        currChar = ch ;
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
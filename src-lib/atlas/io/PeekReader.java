/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.io;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;

import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.util.FileUtils ;

/** Parsing-centric reader.
 *  <p>Faster than using BufferedReader, sometimes a lot faster, when
 *  tokenizing is the critical performance point.
 *  </p>
 *  <p>Supports a line and column
 *  count. Initially, line = 1, col = 1.  Columns go 1..N
 *  </p>
 *  This class is not thread safe.
 * @see BufferingWriter
 */ 


public abstract class PeekReader extends Reader
{
    // Does buffering here instead of using a BufferedReader help?
    // YES.  A lot (Java6).
    
    // Possibly because BufferedReader internally uses synchronized,
    // even on getting a singe character.  Thisis not only an unnecessary cost
    // but also possibly because it stops the JIT doing a better job.
    // **** read(char[]) is a loop of single char operations.
    
    private static final int PUSHBACK_SIZE = 10 ; 
    static final byte CHAR0 = (char)0 ;
    static final int  EOF = -1 ;
    static final int  UNSET = -2 ;
    
    private char[] pushbackChars ;
    private int idxPushback ;                   // Index into pushbackChars: points to next pushBack. -1 => none.
    
    private int currChar = UNSET ;              // Next character to return when reading forwards.
    private long posn ;
    
    public static final int INIT_LINE = 1 ;
    public static final int INIT_COL = 1 ;
    
    private long colNum ;
    private long lineNum ;
    
    // ---- static construction methods.
    
    public static PeekReader make(Reader r)
    {
        if ( r instanceof PeekReader )
            return (PeekReader)r ;
//        if ( r instanceof BufferedReader )
//            Log.warn(PeekReader.class, "BufferedReader passed to PeekReader") ;
            
        return new PeekReaderSource(r) ;
    }
    
    public static PeekReader make(Reader r, int bufferSize)
    {
        if ( r instanceof PeekReader )
            return (PeekReader)r ;
        return new PeekReaderSource(r, bufferSize) ;
    }

    public static PeekReader makeUTF8(InputStream in) 
    {
        Reader r = FileUtils.asUTF8(in) ;
        return make(r) ;
    }
    
    public static PeekReader make(String string)
    {
        return new PeekReaderCharSequence(string) ;
    }
    
    protected PeekReader()
    {
        this.pushbackChars = new char[PUSHBACK_SIZE] ; 
        this.idxPushback = -1 ;
        
        this.colNum = INIT_COL ;
        this.lineNum = INIT_LINE ;
        this.posn = 0 ;
        
        // We start at character "-1", i.e. just before the file starts.
        // Advance always so that the peek character is valid (is character 0) 
        // Returns the character before the file starts (i.e. UNSET).
    }

//    public static PeekReader test(String x)         { return new PeekReaderSource(new StringReader(x)) ; }
//    
//    // A bit slow in that it copies out of the string into the intermediate char array.
//    // But happens in one block operations when less than buffer size.
//    static PeekReader make(String x, int buffSize)
//    { return new PeekReaderSource(new StringReader(x), buffSize) ; }
    
    public long getLineNum()            { return lineNum; }

    public long getColNum()             { return colNum; }

    public long getPosition()           { return posn; }

    //---- Do not access currChar except with peekChar/setCurrChar.
    public int peekChar()
    { 
        if ( idxPushback >= 0 )
            return pushbackChars[idxPushback] ;
        
        // If not started ... delayed initialization.
        if ( currChar == UNSET )
            init() ;
        return currChar ;
    }
    
    // And the correct way to read the currChar is to call peekChar.
    private void setCurrChar(int ch)
    {
        currChar = ch ;
    }
    
    public int readChar()               { return nextChar() ; }
    
    /** push back a character : does not alter underlying position, line or column counts*/  
    public void pushbackChar(int ch)    { unreadChar(ch) ; }
    
    // Reader operations
    @Override
    public void close() throws IOException
    {
        closeInput() ;
    }

    @Override
    public int read() throws IOException
    {
        if ( eof() )
            return EOF ;
        int x = readChar() ;
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
            int ch = readChar() ;
            if ( ch == EOF )
                return (i==0)? EOF : i ;
            cbuf[i+off] = (char)ch ;
        }
        return len ;
    }

    public final boolean eof()   { return peekChar() == EOF ; }

    //protected abstract void init() ;
    protected abstract int advance() ;
    protected abstract void closeInput() ;

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
    }
    
    private void init()
    {
        advanceAndSet() ;
        if ( currChar == UNSET )
            setCurrChar(EOF) ;
    }

    private void advanceAndSet() 
    {
        int ch = advance() ;
        setCurrChar(ch) ;
    }
    
//    /** Move forward one character and return it.
//     * Ensure the buffer is not empty, or boolean eof is set
//     */
//    private int _advance()
//    {
//        if ( idx >= buffLen )
//            // Points outsize the array.  Refill it 
//            _fillArray() ;
//        
//        // Advance one character.
//        if ( buffLen >= 0 )
//        {
//            char ch = chars[idx] ;
//            // Advance the lookahead character
//            idx++ ;
//            return ch ;
//        }  
//        else
//            // Buffer empty, end of stream.
//            return EOF ;
//    }
//
//    private int _fillArray()
//    {
//        int x = source.fill(chars) ;
//        idx = 0 ;
//        buffLen = x ;   // Maybe -1
//        return x ;
//    }
    
    // Invariants.
    // currChar is either chars[idx-1] or pushbackChars[idxPushback]
    
    /** Return the next character, moving on one place and resetting the peek character */ 
    private int nextChar()
    {
        int ch = peekChar() ;
        if ( ch == EOF )
            return EOF ;
        
        if ( idxPushback >= 0 )
        {
            ch = replayPushback() ;
            return ch ;
        }

        posn++ ;
        
        if (ch == '\n')
        {
            lineNum++;
            colNum = INIT_COL ;
        } 
        else
            colNum++;
        
        advanceAndSet() ;
        return ch ;
    }

    private int replayPushback()
    {
        if ( idxPushback >=0 )
        {
            // Simple case : theer are more charcaters in the pushback buffer after this one. 
            char ch2 = pushbackChars[idxPushback] ;
            idxPushback-- ;
            return ch2 ;
        }

        throw new IllegalStateException() ;
        
//        // Push back buffer empty.
//        // Next char is from chars[] which must have yielded a
//        // character and idx >= 1 or the stream was zero chars (idx <= 0)
//        int nextCurrChar = EOF ;
//
//        if ( idx-1 >= 0 )
//            // Had been a read.
//            nextCurrChar = chars[idx-1] ;
//        setCurrChar(nextCurrChar) ;
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
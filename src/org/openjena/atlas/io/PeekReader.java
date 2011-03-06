/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.io;

import static org.openjena.atlas.io.IO.EOF ;
import static org.openjena.atlas.io.IO.UNSET ;

import java.io.FileInputStream ;
import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;

import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.lib.Chars ;

import com.hp.hpl.jena.shared.JenaException ;

/** Parsing-centric reader.
 *  This class is not thread safe.
 * @see BufferingWriter
 * @see PeekInputStream
 */ 

public final class PeekReader extends Reader
{
    // Remember to apply fixes to PeekInputStream as well.
    
    // Buffering is done by a CharStream - does i t make adifference?
    // Yes.  A lot (Java6).
    
    // Possibly because BufferedReader internally uses synchronized,
    // even on getting a single character.  This is not only an unnecessary cost
    // but also possibly because it stops the JIT doing a better job.
    // **** read(char[]) is a loop of single char operations.
    
    private final CharStream source ;
    
    private static final int PUSHBACK_SIZE = 10 ; 
    static final byte CHAR0 = (char)0 ;
    
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
        return make(r, CharStreamBuffered.CB_SIZE) ;
    }
    
    public static PeekReader make(Reader r, int bufferSize)
    {
//        if ( r instanceof BufferedReader )
//        {
//            // Already buffered - and we can't unbuffer it.
//            // Still worth our bufering because of the synchronized on one char reads 
//            return new PeekReader(new CharStreamBuffered(r, bufferSize)) ;
//        }
        return new PeekReader(new CharStreamBuffered(r, bufferSize)) ;
        // Particularly slow to start with.
        //return new PeekReader(new CharStreamBasic(new BufferedReader(r, bufferSize))) ;
    }

    /** Make PeekReader where the input is UTF8 */ 
    public static PeekReader makeUTF8(InputStream in) 
    {
        // This is the best route to make a PeekReader because it avoids
        // chances of wrong charset for a Reader say.
        PeekReader pr ;
        if ( true )
        {
            Reader r = IO.asUTF8(in) ;
            // This adds reader-level buffering
            pr = make(r) ;
        }
        else
        {
            // This is a bit slower - reason unknown.
            InputStreamBuffered in2 = new InputStreamBuffered(in) ;
            CharStream r = new InStreamUTF8(in2) ;
            pr = new PeekReader(r) ;
        }
        // Skip BOM.
        int ch = pr.peekChar() ;
        if ( ch == Chars.BOM )
            // Skip BOM
            pr.readChar() ;
        return pr ;
    }
    
    /** Make PeekReader where the input is ASCII */ 
    public static PeekReader makeASCII(InputStream in) 
    {
//      InputStreamBuffered in2 = new InputStreamBuffered(in) ;
//      CharStream r = new StreamASCII(in2) ;
        Reader r = IO.asASCII(in) ;
        return make(r) ;
    }
    
    
    public static PeekReader make(CharStream r) 
    {
        return new PeekReader(r) ;
    }
    
    public static PeekReader readString(String string)
    {
        return new PeekReader(new CharStreamSequence(string)) ;
    }
    
    public static PeekReader open(String filename) 
    {
        try {
            InputStream in = new FileInputStream(filename) ;
            return makeUTF8(in) ;
        } catch (FileNotFoundException ex){ throw new AtlasException("File not found: "+filename) ; }
    }
    
    private PeekReader(CharStream stream)
    {
        this.source = stream ;
        this.pushbackChars = new char[PUSHBACK_SIZE] ; 
        this.idxPushback = -1 ;
        
        this.colNum = INIT_COL ;
        this.lineNum = INIT_LINE ;
        this.posn = 0 ;
        
        // We start at character "-1", i.e. just before the file starts.
        // Advance always so that the peek character is valid (is character 0) 
        // Returns the character before the file starts (i.e. UNSET).
    }
    
    public long getLineNum()            { return lineNum; }

    public long getColNum()             { return colNum; }

    public long getPosition()           { return posn; }

    //---- Do not access currChar except with peekChar/setCurrChar.
    public final int peekChar()
    { 
        if ( idxPushback >= 0 )
            return pushbackChars[idxPushback] ;
        
        // If not started ... delayed initialization.
        if ( currChar == UNSET )
            init() ;
        return currChar ;
    }
    
    // And the correct way to read the currChar is to call peekChar.
    private final void setCurrChar(int ch)
    {
        currChar = ch ;
    }
    
    public final int readChar()               { return nextChar() ; }
    
    /** push back a character : does not alter underlying position, line or column counts*/  
    public final void pushbackChar(int ch)    { unreadChar(ch) ; }
    
    // Reader operations
    @Override
    public final void close() throws IOException
    {
        source.closeStream() ;
    }

    @Override
    public final int read() throws IOException
    {
        if ( eof() )
            return EOF ;
        int x = readChar() ;
        return x ;
    }
    
    @Override
    public final int read(char[] cbuf, int off, int len) throws IOException
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

    // ----------------
    // The methods below are the only ones to manipulate the character buffers.
    // Other methods may read the state of variables.
    
    private final void unreadChar(int ch)
    {
        // The push back buffer is in the order where [0] is the oldest.
        // Does not alter the line number, column number or position count. 
        
        if ( idxPushback >= pushbackChars.length )
        {
            // Enlarge pushback buffer.
            char[] pushbackChars2 = new char[pushbackChars.length*2] ;
            System.arraycopy(pushbackChars, 0, pushbackChars2, 0, pushbackChars.length) ;
            pushbackChars = pushbackChars2 ;
            //throw new JenaException("Pushback buffer overflow") ;
        }
        if ( ch == EOF || ch == UNSET )
            throw new JenaException("Illegal character to push back: "+ch) ;
        
        idxPushback++ ;
        pushbackChars[idxPushback] = (char)ch ;
    }
    
    private final void init()
    {
        advanceAndSet() ;
        if ( currChar == UNSET )
            setCurrChar(EOF) ;
    }

    private final void advanceAndSet() 
    {
        int ch = source.advance() ;
        setCurrChar(ch) ;
    }
    
    
    // Invariants.
    // currChar is either chars[idx-1] or pushbackChars[idxPushback]
    
    /** Return the next character, moving on one place and resetting the peek character */ 
    private final int nextChar()
    {
        int ch = peekChar() ;
        
        if ( ch == EOF )
            return EOF ;
        
        if ( idxPushback >= 0 )
        {
            char ch2 = pushbackChars[idxPushback] ;
            idxPushback-- ;
            return ch2 ;
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
}


/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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
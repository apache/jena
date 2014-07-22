/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.io ;

import static org.apache.jena.atlas.io.IO.EOF ;
import static org.apache.jena.atlas.io.IO.UNSET ;

import java.io.FileInputStream ;
import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.lib.Chars ;

import com.hp.hpl.jena.shared.JenaException ;

/**
 * Parsing-centric reader. This class is not thread safe.
 * @see PeekInputStream
 */

public final class PeekReader extends Reader {
    // Remember to apply fixes to PeekInputStream as well.

    // Buffering is done by a CharStream - does it make adifference?
    // Yes. A lot (Java6).

    // Using a Reader here seems to have zero cost or benefit but CharStream
    // allows fast String handling.
    private final CharStream source ;

    private static final int PUSHBACK_SIZE = 10 ;
    static final byte        CHAR0         = (char)0 ;

    private char[]           pushbackChars ;
    // Index into pushbackChars: points to next pushBack.
    // -1 => none.
    private int              idxPushback ;            

    // Next character to return when reading forwards.
    private int              currChar      = UNSET ;  
    private long             posn ;

    public static final int  INIT_LINE     = 1 ;
    public static final int  INIT_COL      = 1 ;

    private long             colNum ;
    private long             lineNum ;

    // ---- static construction methods.

    public static PeekReader make(Reader r) {
        if ( r instanceof PeekReader )
            return (PeekReader)r ;
        return make(r, CharStreamBuffered.CB_SIZE) ;
    }

    public static PeekReader make(Reader r, int bufferSize) {
        // It is worth our own buffering even if a BufferedReader
        // because of the synchronized on one char reads in BufferedReader.
        return new PeekReader(new CharStreamBuffered(r, bufferSize)) ;
    }

    /** Make PeekReader where the input is UTF8 : BOM is removed */
    public static PeekReader makeUTF8(InputStream in) {
        // This is the best route to make a PeekReader because it avoids
        // chances of wrong charset for a Reader say.
        PeekReader pr ;
        if ( true ) {
            Reader r = IO.asUTF8(in) ;
            // This adds reader-level buffering
            pr = make(r) ;
        } else {
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
    public static PeekReader makeASCII(InputStream in) {
        Reader r = IO.asASCII(in) ;
        return make(r) ;
    }

    public static PeekReader make(CharStream r) {
        return new PeekReader(r) ;
    }

    public static PeekReader readString(String string) {
        return new PeekReader(new CharStreamSequence(string)) ;
    }

    public static PeekReader open(String filename) {
        try {
            InputStream in = new FileInputStream(filename) ;
            return makeUTF8(in) ;
        } catch (FileNotFoundException ex) {
            throw new AtlasException("File not found: " + filename) ;
        }
    }

    private PeekReader(CharStream stream) {
        this.source = stream ;
        this.pushbackChars = new char[PUSHBACK_SIZE] ;
        this.idxPushback = -1 ;

        this.colNum = INIT_COL ;
        this.lineNum = INIT_LINE ;
        this.posn = 0 ;
    }

    public long getLineNum() {
        return lineNum ;
    }

    public long getColNum() {
        return colNum ;
    }

    public long getPosition() {
        return posn ;
    }

    // ---- Do not access currChar except with peekChar/setCurrChar.
    public final int peekChar() {
        if ( idxPushback >= 0 )
            return pushbackChars[idxPushback] ;

        // If not started ... delayed initialization.
        if ( currChar == UNSET )
            init() ;
        return currChar ;
    }

    // And the correct way to read the currChar is to call peekChar.
    private final void setCurrChar(int ch) {
        currChar = ch ;
    }

    public final int readChar() {
        return nextChar() ;
    }

    /**
     * push back a character : does not alter underlying position, line or
     * column counts
     */
    public final void pushbackChar(int ch) {
        unreadChar(ch) ;
    }

    // Reader operations
    @Override
    public final void close() throws IOException {
        source.closeStream() ;
    }

    @Override
    public final int read() throws IOException {
        if ( eof() )
            return EOF ;
        int x = readChar() ;
        return x ;
    }

    @Override
    public final int read(char[] cbuf, int off, int len) throws IOException {
        if ( eof() )
            return EOF ;
        // Note - we need to preserve line count
        // Single char ops are reasonably efficient.
        for (int i = 0; i < len; i++) {
            int ch = readChar() ;
            if ( ch == EOF )
                return (i == 0) ? EOF : i ;
            cbuf[i + off] = (char)ch ;
        }
        return len ;
    }

    public final boolean eof() {
        return peekChar() == EOF ;
    }

    // ----------------
    // The methods below are the only ones to manipulate the character buffers.
    // Other methods may read the state of variables.

    private final void unreadChar(int ch) {
        // The push back buffer is in the order where [0] is the oldest.
        // Does not alter the line number, column number or position count
        // not does reading a pushback charcater.

        if ( idxPushback >= pushbackChars.length ) {
            // Enlarge pushback buffer.
            char[] pushbackChars2 = new char[pushbackChars.length * 2] ;
            System.arraycopy(pushbackChars, 0, pushbackChars2, 0, pushbackChars.length) ;
            pushbackChars = pushbackChars2 ;
            // throw new JenaException("Pushback buffer overflow") ;
        }
        if ( ch == EOF || ch == UNSET )
            throw new JenaException("Illegal character to push back: " + ch) ;

        idxPushback++ ;
        pushbackChars[idxPushback] = (char)ch ;
    }

    private final void init() {
        advanceAndSet() ;
        if ( currChar == UNSET )
            setCurrChar(EOF) ;
    }

    private final void advanceAndSet() {
        int ch = source.advance() ;
        setCurrChar(ch) ;
    }

    // Invariants.
    // currChar is either chars[idx-1] or pushbackChars[idxPushback]

    /**
     * Return the next character, moving on one place and resetting the peek
     * character
     */
    private final int nextChar() {
        int ch = peekChar() ;

        if ( ch == EOF )
            return EOF ;

        if ( idxPushback >= 0 ) {
            char ch2 = pushbackChars[idxPushback] ;
            idxPushback-- ;
            return ch2 ;
        }

        posn++ ;

        if ( ch == '\n' ) {
            lineNum++ ;
            colNum = INIT_COL ;
        } else
            colNum++ ;

        advanceAndSet() ;
        return ch ;
    }
}

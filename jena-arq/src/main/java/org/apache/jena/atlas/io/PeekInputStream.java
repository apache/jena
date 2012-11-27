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

package org.apache.jena.atlas.io;

import static org.apache.jena.atlas.io.IO.EOF ;
import static org.apache.jena.atlas.io.IO.UNSET ;

import java.io.FileInputStream ;
import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.io.InputStream ;

import org.apache.jena.atlas.AtlasException ;

import com.hp.hpl.jena.shared.JenaException ;

/** Parsing-centric input stream.
 *  @see PeekReader
 */ 


public final class PeekInputStream extends InputStream
{
    // Change to looking at slices of a ByteBuffer and rework TokenizerBytes 
    
    private final InputStreamBuffered source ;
    
    private static final int PUSHBACK_SIZE = 10 ; 
    static final byte BYTE0 = (byte)0 ;
    
    private byte[] pushbackBytes ;
    private int idxPushback ;                   // Index into pushbackBytes: points to next pushBack. -1 => none.
    
    private int currByte = UNSET ;              // Next byte to return when reading forwards.
    private long posn ;
    
    public static final int INIT_LINE = 1 ;
    public static final int INIT_COL = 1 ;
    
    private long colNum ;
    private long lineNum ;
    
    // ---- static construction methods.
    
    public static PeekInputStream make(InputStream inputStream)
    {
        return make(inputStream, InputStreamBuffered.DFT_BUFSIZE) ;
    }
    
    public static PeekInputStream make(InputStream inputStream, int bufferSize)
    {
        if ( inputStream instanceof PeekInputStream )
            return (PeekInputStream)inputStream ;
        
        if ( inputStream instanceof InputStreamBuffered )
            return new PeekInputStream((InputStreamBuffered)inputStream) ;
        InputStreamBuffered in = new InputStreamBuffered(inputStream, bufferSize) ;
        return new PeekInputStream(in) ;
    }

    public static PeekInputStream open(String filename) 
    {
        try {
            InputStream in = new FileInputStream(filename) ;
            return make(in) ;
        } catch (FileNotFoundException ex){ throw new AtlasException("File not found: "+filename) ; }
    }
    
    private PeekInputStream(InputStreamBuffered input)
    {
        this.source = input ;
        this.pushbackBytes = new byte[PUSHBACK_SIZE] ; 
        this.idxPushback = -1 ;
        
        this.colNum = INIT_COL ;
        this.lineNum = INIT_LINE ;
        this.posn = 0 ;
        
        // We start at byte "-1", i.e. just before the file starts.
        // Advance always so that the peek byte is valid (is byte 0) 
        // Returns the byte before the file starts (i.e. UNSET).
    }

    public final InputStreamBuffered getInput()   { return source ; }
    
    public long getLineNum()            { return lineNum; }

    public long getColNum()             { return colNum; }

    public long getPosition()           { return posn; }

    //---- Do not access currByte except with peekByte/setCurrByte.
    public final int peekByte()
    { 
        if ( idxPushback >= 0 )
            return pushbackBytes[idxPushback] ;
        
        // If not started ... delayed initialization.
        if ( currByte == UNSET )
            init() ;
        return currByte ;
    }
    
    // And the correct way to read the currByte is to call peekByte
    private final void setCurrByte(int b)
    {
        currByte = b ;
    }
    
    public final int readByte()               { return nextByte() ; }
    
    /** push back a byte : does not alter underlying position, line or column counts*/  
    public final void pushbackByte(int b)    { unreadByte(b) ; }
    
    @Override
    public final void close() throws IOException
    {
        source.close() ;
    }

    @Override
    public final int read() throws IOException
    {
        if ( eof() )
            return EOF ;
        int x = readByte() ;
        return x ;
    }
    
    @Override
    public final int read(byte[] buf, int off, int len) throws IOException
    {
        if ( eof() )
            return EOF ;
        for ( int i = 0 ; i < len ; i++ )
        {
            int ch = readByte() ;
            if ( ch == EOF )
                return (i==0)? EOF : i ;
            buf[i+off] = (byte)ch ;
        }
        return len ;
    }

    public final boolean eof()   { return peekByte() == EOF ; }

    // ----------------
    // The methods below are the only ones to manipulate the byte buffers.
    // Other methods may read the state of variables.
    
    private final void unreadByte(int b)
    {
        // The push back buffer is in the order where [0] is the oldest.
        // Does not alter the line number, column number or position count. 
        
        if ( idxPushback >= pushbackBytes.length )
        {
            // Enlarge pushback buffer.
            byte[] pushbackBytes2 = new byte[pushbackBytes.length*2] ;
            System.arraycopy(pushbackBytes, 0, pushbackBytes2, 0, pushbackBytes.length) ;
            pushbackBytes = pushbackBytes2 ;
            //throw new JenaException("Pushback buffer overflow") ;
        }
        if ( b == EOF || b == UNSET )
            throw new JenaException("Illegal byte to push back: "+b) ;
        
        idxPushback++ ;
        pushbackBytes[idxPushback] = (byte)b ;
    }
    
    private final void init()
    {
        advanceAndSet() ;
        if ( currByte == UNSET )
            setCurrByte(EOF) ;
    }

    private final void advanceAndSet() 
    {
        try {
            int ch = source.read() ;
            setCurrByte(ch) ;
        } catch (IOException ex) { IO.exception(ex) ; }
    }
    
    
    // Invariants.
    // currByte is either bytes[idx-1] or pushbackBytes[idxPushback]
    
    /** Return the next byte, moving on one place and resetting the peek byte */ 
    private final int nextByte()
    {
        int b = peekByte() ;
        
        if ( b == EOF )
            return EOF ;
        
        if ( idxPushback >= 0 )
        {
            byte b2 = pushbackBytes[idxPushback] ;
            idxPushback-- ;
            return b2 ;
        }

        posn++ ;
        
        if (b == '\n')
        {
            lineNum++;
            colNum = INIT_COL ;
        } 
        else
            colNum++;
        
        advanceAndSet() ;
        return b ;
    }
}

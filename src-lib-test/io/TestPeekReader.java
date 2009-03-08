/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package io;

import io.PeekReader;

import org.junit.Test;
import test.BaseTest;

public class TestPeekReader extends BaseTest
{
    static int INIT_LINE = PeekReader.INIT_LINE ;
    static int INIT_COL = PeekReader.INIT_COL ;
    
    @Test public void read0()
    {
        assertEquals("Init line", 1, INIT_LINE) ;
        assertEquals("Init col", 1, INIT_COL) ;
    }
    
    @Test public void read1()
    {
        PeekReader r = make("") ;
        checkLineCol(r, INIT_LINE, INIT_COL) ;
        
        int x = r.peekChar() ;
        assertEquals(-1, x) ;
        x = r.readChar() ;
        assertEquals(-1, x) ;
        x = r.readChar() ;
        assertEquals(-1, x) ;
    }
    
    @Test public void read2()
    {
        // Assumes we start at (1,1) 
        PeekReader r = make("a") ;
        checkLineCol(r, INIT_LINE, INIT_COL) ;
        
        int x = r.peekChar() ;
        assertEquals('a', x) ;
        checkLineCol(r, INIT_LINE, INIT_COL) ;
        
        x = r.readChar() ;
        checkLineCol(r, INIT_LINE, INIT_COL+1) ;
        assertEquals('a', x) ;
        
        x = r.peekChar() ;
        assertEquals(-1, x) ;
        
        x = r.readChar() ;
        assertEquals(-1, x) ;
    }

    @Test public void read3()
    {
        String c = "abcde" ;
        PeekReader r = make(c) ;
        
        for ( int i = 0 ; i < c.length(); i++ )
        {
            checkLineCol(r, INIT_LINE, i+INIT_COL) ;
            long z = r.getPosition() ;
            assertEquals(i, r.getPosition()) ;
            assertEquals(c.charAt(i), r.readChar()) ;
        }
        assertTrue(r.eof()) ;
    }

    @Test public void read4()
    {
        position("abcde") ;
    }

    @Test public void read5()
    {
        position("abc\nde") ;
    }

    @Test public void read6()
    {
        position("abc\nde\n") ;
    }
    
    @Test public void read7()
    {
        position("") ;
    }

    @Test public void read8()
    {
        position("x") ;
    }


    @Test public void read9()
    {
        PeekReader r = make("a\nb\n") ;
        checkLineCol(r, INIT_LINE, INIT_COL) ;
        int x = r.peekChar() ;
        assertEquals('a', x) ;
        checkLineCol(r, INIT_LINE, INIT_COL) ;
        
        x = r.readChar() ;
        assertEquals('a', x) ;
        checkLineCol(r, INIT_LINE, INIT_COL+1) ;

        x = r.readChar() ;
        assertEquals('\n', x) ;
        checkLineCol(r, INIT_LINE+1, INIT_COL) ;
    }
    
    @Test public void unread1()
    {
        PeekReader r = make("abc") ;
        assertEquals('a', r.peekChar()) ;
        r.pushbackChar('Z') ;
        assertEquals('Z', r.peekChar()) ;
        contains(r, "Zabc") ;
    }

    @Test public void unread2()
    {
        PeekReader r = make("abc") ;
        checkLineCol(r, INIT_LINE, INIT_COL) ;
        int ch = r.readChar() ;
        // Pushback does not move line/col backwards.
        checkLineCol(r, INIT_LINE, INIT_COL+1) ;
        assertEquals('b', r.peekChar()) ;
        checkLineCol(r, INIT_LINE, INIT_COL+1) ;
        r.pushbackChar('a') ;
        checkLineCol(r, INIT_LINE, INIT_COL+1) ;
        contains(r, "abc") ;
    }
    
    @Test public void unread3()
    {
        PeekReader r = make("") ;
        int ch = r.readChar() ;
        assertEquals(-1, r.peekChar()) ;
        r.pushbackChar('a') ;
        contains(r, "a") ;
    }

    @Test public void unread4()
    {
        PeekReader r = make("") ;
        int ch = r.readChar() ;
        assertEquals(-1, r.peekChar()) ;
        r.pushbackChar('0') ;
        r.pushbackChar('1') ;
        r.pushbackChar('2') ;
        r.pushbackChar('3') ;
        contains(r, "3210") ;   // Backwards!
    }

    @Test public void unread5()
    {
        PeekReader r = make("") ;
        long lineNum = r.getLineNum() ;
        long colNum = r.getColNum() ;
        
        checkLineCol(r, lineNum, colNum) ;
        
        r.pushbackChar('0') ;
        checkLineCol(r, lineNum, colNum) ;  // Unmoved.
        r.pushbackChar('1') ;
        checkLineCol(r, lineNum, colNum) ;
        assertEquals('1', r.readChar()) ;
        
        checkLineCol(r, lineNum, colNum) ;  // Unmoved.
        r.pushbackChar('2') ;
        r.pushbackChar('3') ;
        checkLineCol(r, lineNum, colNum) ;  // Unmoved.
        assertEquals('3', r.peekChar()) ;
        contains(r, "320") ;
    }

    private void checkLineCol(PeekReader r, long lineNum, long colNum)
    {
        assertEquals("Line", lineNum, r.getLineNum()) ; 
        assertEquals("Column", colNum, r.getColNum()) ;
    }
    
    private void position(String contents)
    {
        PeekReader r = make(contents) ;
        
        int line = INIT_LINE ;
        int col = INIT_COL ;
        checkLineCol(r, line, col) ;
        assertEquals(0, r.getPosition()) ;
        
        for ( int i = 0 ; i < contents.length(); i++ )
        {
            int x = r.readChar() ;
            if ( x != -1 )
            {
                if ( x == '\n' )
                {
                    line++ ;
                    col = INIT_COL ;
                }
                else
                    col++ ;
            }
            assertEquals(contents.charAt(i), x) ;
            assertEquals(i+1, r.getPosition()) ;
            checkLineCol(r, line, col) ;
        }
        assertTrue(r.eof()) ;
    }
    
    private void contains(PeekReader r, String contents)
    {
        for ( int i = 0 ; i < contents.length(); i++ )
        {
            int x = r.readChar() ;
            assertEquals(contents.charAt(i),x) ;
        }
        assertTrue(r.eof()) ;
    }
    
    private PeekReader make(String contents)
    { return make(contents, 2) ; }
    
    
    private PeekReader make(String contents, int size)
    {
        return PeekReader.make(contents, size) ;
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
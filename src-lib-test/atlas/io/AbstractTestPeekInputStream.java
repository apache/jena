/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.io;

import org.junit.Test ;
import atlas.test.BaseTest ;

public abstract class AbstractTestPeekInputStream extends BaseTest
{
    static int INIT_LINE = PeekInputStream.INIT_LINE ;
    static int INIT_COL = PeekInputStream.INIT_COL ;
    
    abstract PeekInputStream make(String contents, int size) ;
    
    @Test public void read0()
    {
        assertEquals("Init line", 1, INIT_LINE) ;
        assertEquals("Init col", 1, INIT_COL) ;
    }
    
    @Test public void read1()
    {
        PeekInputStream in = make("") ;
        checkLineCol(in, INIT_LINE, INIT_COL) ;
        
        int x = in.peekByte() ;
        assertEquals(-1, x) ;
        x = in.readByte() ;
        assertEquals(-1, x) ;
        x = in.readByte() ;
        assertEquals(-1, x) ;
    }
    
    @Test public void read2()
    {
        // Assumes we start at (1,1) 
        PeekInputStream in = make("a") ;
        checkLineCol(in, INIT_LINE, INIT_COL) ;
        
        int x = in.peekByte() ;
        assertEquals('a', x) ;
        checkLineCol(in, INIT_LINE, INIT_COL) ;
        
        x = in.readByte() ;
        checkLineCol(in, INIT_LINE, INIT_COL+1) ;
        assertEquals('a', x) ;
        
        x = in.peekByte() ;
        assertEquals(-1, x) ;
        
        x = in.readByte() ;
        assertEquals(-1, x) ;
    }

    @Test public void read3()
    {
        String c = "abcde" ;
        PeekInputStream in = make(c) ;
        
        for ( int i = 0 ; i < c.length(); i++ )
        {
            checkLineCol(in, INIT_LINE, i+INIT_COL) ;
            long z = in.getPosition() ;
            assertEquals(i, in.getPosition()) ;
            assertEquals(c.charAt(i), in.readByte()) ;
        }
        assertTrue(in.eof()) ;
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
        PeekInputStream in = make("a\nb\n") ;
        checkLineCol(in, INIT_LINE, INIT_COL) ;
        int x = in.peekByte() ;
        assertEquals('a', x) ;
        checkLineCol(in, INIT_LINE, INIT_COL) ;
        
        x = in.readByte() ;
        assertEquals('a', x) ;
        checkLineCol(in, INIT_LINE, INIT_COL+1) ;

        x = in.readByte() ;
        assertEquals('\n', x) ;
        checkLineCol(in, INIT_LINE+1, INIT_COL) ;
    }
    
    @Test public void unread1()
    {
        PeekInputStream in = make("abc") ;
        assertEquals('a', in.peekByte()) ;
        in.pushbackByte('Z') ;
        assertEquals('Z', in.peekByte()) ;
        contains(in, "Zabc") ;
    }

    @Test public void unread2()
    {
        PeekInputStream in = make("abc") ;
        checkLineCol(in, INIT_LINE, INIT_COL) ;
        int ch = in.readByte() ;
        // Pushback does not move line/col backwards.
        checkLineCol(in, INIT_LINE, INIT_COL+1) ;
        assertEquals('b', in.peekByte()) ;
        checkLineCol(in, INIT_LINE, INIT_COL+1) ;
        in.pushbackByte('a') ;
        checkLineCol(in, INIT_LINE, INIT_COL+1) ;
        contains(in, "abc") ;
    }
    
    @Test public void unread3()
    {
        PeekInputStream in = make("") ;
        int ch = in.readByte() ;
        assertEquals(-1, in.peekByte()) ;
        in.pushbackByte('a') ;
        contains(in, "a") ;
    }

    @Test public void unread4()
    {
        PeekInputStream in = make("") ;
        int ch = in.readByte() ;
        assertEquals(-1, in.peekByte()) ;
        in.pushbackByte('0') ;
        in.pushbackByte('1') ;
        in.pushbackByte('2') ;
        in.pushbackByte('3') ;
        contains(in, "3210") ;   // Backwards!
    }

    @Test public void unread5()
    {
        PeekInputStream in = make("") ;
        long lineNum = in.getLineNum() ;
        long colNum = in.getColNum() ;
        
        checkLineCol(in, lineNum, colNum) ;
        
        in.pushbackByte('0') ;
        checkLineCol(in, lineNum, colNum) ;  // Unmoved.
        in.pushbackByte('1') ;
        checkLineCol(in, lineNum, colNum) ;
        assertEquals('1', in.readByte()) ;
        
        checkLineCol(in, lineNum, colNum) ;  // Unmoved.
        in.pushbackByte('2') ;
        in.pushbackByte('3') ;
        checkLineCol(in, lineNum, colNum) ;  // Unmoved.
        assertEquals('3', in.peekByte()) ;
        contains(in, "320") ;
    }

    private void checkLineCol(PeekInputStream in, long lineNum, long colNum)
    {
        assertEquals("Line", lineNum, in.getLineNum()) ; 
        assertEquals("Column", colNum, in.getColNum()) ;
    }
    
    private void position(String contents)
    {
        PeekInputStream in = make(contents) ;
        
        int line = INIT_LINE ;
        int col = INIT_COL ;
        checkLineCol(in, line, col) ;
        assertEquals(0, in.getPosition()) ;
        
        for ( int i = 0 ; i < contents.length(); i++ )
        {
            int x = in.readByte() ;
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
            assertEquals(i+1, in.getPosition()) ;
            checkLineCol(in, line, col) ;
        }
        assertTrue(in.eof()) ;
    }
    
    private void contains(PeekInputStream in, String contents)
    {
        for ( int i = 0 ; i < contents.length(); i++ )
        {
            int x = in.readByte() ;
            assertEquals("\""+contents+"\" -- Index "+i+" Expected:'"+contents.charAt(i)+"' Got: '"+(char)x+"'", contents.charAt(i), x) ;
        }
        assertTrue(in.eof()) ;
    }
    
    private PeekInputStream make(String contents)
    { return make(contents, 2) ; }
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
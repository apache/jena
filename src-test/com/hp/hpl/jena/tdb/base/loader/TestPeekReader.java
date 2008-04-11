/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.loader;

import java.io.StringReader;

import com.hp.hpl.jena.tdb.base.loader.PeekReader;


import org.junit.Test;
import test.BaseTest;

public class TestPeekReader extends BaseTest
{
    @Test public void read1()
    {
        PeekReader r = make("") ;
        int x = r.peekChar() ;
        assertEquals(-1, x) ;
        x = r.readChar() ;
        assertEquals(-1, x) ;
        x = r.readChar() ;
        assertEquals(-1, x) ;
    }
    
    @Test public void read2()
    {
        PeekReader r = make("a") ;
        
        int x = r.peekChar() ;
        assertEquals('a', x) ;
        assertEquals(1, r.getColNum()) ;
        
        x = r.readChar() ;
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
            assertEquals(i+1, r.getColNum()) ;
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

    private void position(String contents)
    {
        PeekReader r = make(contents) ;
        
        int line = 1 ;
        int col = 1 ;
        assertEquals(line, r.getLineNum()) ;
        
        for ( int i = 0 ; i < contents.length(); i++ )
        {
            assertEquals(line, r.getLineNum()) ;
            assertEquals(col, r.getColNum()) ;

            int x = r.readChar() ;
            assertEquals(contents.charAt(i),x) ;
            if ( x == '\n' )
            {
                line++ ;
                col = 1 ;
            }
            else
                col++ ;
            
        }
        assertTrue(r.eof()) ;
    }
    
    private PeekReader make(String contents)
    { return make(contents, 2) ; }
    
    
    private PeekReader make(String contents, int size)
    {
        StringReader r = new StringReader(contents) ;
        return new PeekReader(r,size) ;
    }
    
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
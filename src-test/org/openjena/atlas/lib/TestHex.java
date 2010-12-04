/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;


import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.Hex ;

public class TestHex extends BaseTest
{
    @Test public void hex_01()
    {
        byte[] b = new byte[16] ;
        test(0L, 0, b, 16) ;
    }
    
    @Test public void hex_02()
    {
        byte[] b = new byte[16] ;
        test(1L, 0, b, 16) ;
    }

    @Test public void hex_03()
    {
        byte[] b = new byte[16] ;
        test(Long.MAX_VALUE, 0, b, 16) ;
    }
    
    @Test public void hex_04()
    {
        byte[] b = new byte[16] ;
        test(Long.MIN_VALUE, 0, b, 16) ;
    }
    
    @Test public void hex_05()
    {
        byte[] b = new byte[16] ;
        // -1L
        test(0xFFFFFFFFFFFFFFFFL, 0, b, 16) ;
    }

    @Test public void hex_06()
    {
        byte[] b = new byte[16] ;
        test(-1L, 0, b, 16) ;
    }

    private static void test(long value, int idx, byte[] b, int width)
    {
        int x = Hex.formatUnsignedLongHex(b, idx, value, width) ;
        assertEquals(width, x) ;
        for ( int i = 0 ; i < width ; i++ )
        {
            int v = b[i] ;
            if ( v >= '0' && v <= '9' ) continue ;
            if ( v >= 'a' && v <= 'f' ) continue ;
            if ( v >= 'A' && v <= 'F' ) continue ;
            fail(String.format("Not a hex digit: %02X",b[i])) ;
        }
        
        long v = Hex.getLong(b, idx) ;
        assertEquals(value, v) ;
    }
}

/*
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
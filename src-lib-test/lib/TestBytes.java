/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package lib;

import lib.Bytes;
import org.junit.Test;
import test.BaseTest;

public class TestBytes extends BaseTest
{
    @Test public void packInt1()
    {
        byte[] b = new byte[4] ;
        Bytes.setInt(0x01020304,b) ;
        assertEquals(0x01, b[0]) ;
        assertEquals(0x02, b[1]) ;
        assertEquals(0x03, b[2]) ;
        assertEquals(0x04, b[3]) ;
    }
    
    @Test public void packInt2()
    {
        byte[] b = new byte[8] ;
        Bytes.setInt(0x01020304,b,0) ;
        Bytes.setInt(0x05060708,b,4) ;
        assertEquals(0x01, b[0]) ;
        assertEquals(0x02, b[1]) ;
        assertEquals(0x03, b[2]) ;
        assertEquals(0x04, b[3]) ;
        assertEquals(0x05, b[4]) ;
        assertEquals(0x06, b[5]) ;
        assertEquals(0x07, b[6]) ;
        assertEquals(0x08, b[7]) ;
    }

    @Test public void packInt3()
    {
        byte[] b = new byte[4] ;
        Bytes.setInt(0xF1F2F3F4,b) ;
        int i = Bytes.getInt(b) ;
        assertEquals(0xF1F2F3F4, i) ;
    }
    
    @Test public void packInt4()
    {
        byte[] b = new byte[8] ;
        Bytes.setInt(0x01020304,b,0) ;
        Bytes.setInt(0x05060708,b,4) ;

        int i1 = Bytes.getInt(b,0) ;
        int i2 = Bytes.getInt(b,4) ;
        assertEquals(0x01020304, i1) ;
        assertEquals(0x05060708, i2) ;
    }

    @Test public void packLong5()
    {
        byte[] b = new byte[8] ;
        Bytes.setLong(0x0102030405060708L,b) ;
        assertEquals(0x01, b[0]) ;
        assertEquals(0x02, b[1]) ;
        assertEquals(0x03, b[2]) ;
        assertEquals(0x04, b[3]) ;
        assertEquals(0x05, b[4]) ;
        assertEquals(0x06, b[5]) ;
        assertEquals(0x07, b[6]) ;
        assertEquals(0x08, b[7]) ;
   }
    
    @Test public void packLong6()
    {
        byte[] b = new byte[16] ;
        Bytes.setLong(0x0102030405060708L,b,0) ;
        Bytes.setLong(0x1112131415161718L,b,8) ;
        assertEquals(0x01, b[0]) ;
        assertEquals(0x02, b[1]) ;
        assertEquals(0x03, b[2]) ;
        assertEquals(0x04, b[3]) ;
        assertEquals(0x05, b[4]) ;
        assertEquals(0x06, b[5]) ;
        assertEquals(0x07, b[6]) ;
        assertEquals(0x08, b[7]) ;

        assertEquals(0x11, b[0+8]) ;
        assertEquals(0x12, b[1+8]) ;
        assertEquals(0x13, b[2+8]) ;
        assertEquals(0x14, b[3+8]) ;
        assertEquals(0x15, b[4+8]) ;
        assertEquals(0x16, b[5+8]) ;
        assertEquals(0x17, b[6+8]) ;
        assertEquals(0x18, b[7+8]) ;
    }

    @Test public void packLong7()
    {
        byte[] b = new byte[8] ;
        Bytes.setLong(0xF1F2F3F4F5F6F7F8L,b) ;
        long i = Bytes.getLong(b) ;
        assertEquals (0xF1F2F3F4F5F6F7F8L,i) ;
    }
    
    @Test public void packLong8()
    {
        byte[] b = new byte[16] ;
        Bytes.setLong(0xF1F2F3F4F5F6F7F8L,b,0) ;
        Bytes.setLong(0xA1A2A3A4A5A6A7A8L,b,8) ;

        long i1 = Bytes.getLong(b,0) ;
        long i2 = Bytes.getLong(b,8) ;
        assertEquals(0xF1F2F3F4F5F6F7F8L,i1) ;
        assertEquals(0xA1A2A3A4A5A6A7A8L,i2) ;

        
        
    }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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
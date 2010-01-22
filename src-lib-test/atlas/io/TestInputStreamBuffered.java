/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package atlas.io;

import java.io.ByteArrayInputStream ;
import java.io.IOException ;
import java.io.InputStream ;

import org.junit.Test ;
import atlas.lib.Bytes ;
import atlas.test.BaseTest ;

public class TestInputStreamBuffered extends BaseTest
{
    @Test public void test_01() throws IOException
    {
        InputStream in = stream("") ;
        InputStream in2 = new InputStreamBuffered(in) ;
        int x = count(in2) ;
        assertEquals(0, x) ;
    }
    
    @Test public void test_02() throws IOException
    {
        InputStream in = stream(1,2,3,4) ;
        InputStream in2 = new InputStreamBuffered(in) ;
        check(in2, 1,2,3,4) ;
    }
    
    @Test public void test_03() throws IOException
    {
        InputStream in = stream(1,2,3,4) ;
        InputStream in2 = new InputStreamBuffered(in, 2) ;
        check(in2, 1,2,3,4) ;
    }
    
    @Test public void test_04() throws IOException
    {
        InputStream in = stream(1,2,3,4) ;
        InputStream in2 = new InputStreamBuffered(in, 1) ;
        check(in2, 1,2,3,4) ;
        assertEquals(-1, in.read()) ;
    }
    
    
    private static InputStream stream(String data)
    {
        byte[] b = Bytes.string2bytes(data) ;
        return new ByteArrayInputStream(b) ;
    }
    
    private static InputStream stream(byte...bytes)
    {
        return new ByteArrayInputStream(bytes) ;
    }

    private static InputStream stream(int...bytes)
    {
        return stream(ints2bytes(bytes)) ;
    }

    // Convenience.
    private static byte[] ints2bytes(int...values)
    {
        byte b[] = new byte[values.length] ;
        for ( int i = 0 ; i < b.length ; i++ )
            b[i] = (byte)values[i] ;
        return b ;
    }

    private static int count(InputStream in) throws IOException
    {
        int count = 0 ;
        while(in.read() != -1 )
            count++ ;
        return count ;
    }

    private static void check(InputStream in, int ...bytes) throws IOException
    {
        check(in, ints2bytes(bytes)) ;
    }

    private static void check(InputStream in, byte ...bytes) throws IOException
    {
        for ( byte b : bytes )
        {
            assertEquals(b, (byte)in.read()) ;
        }
    }

}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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
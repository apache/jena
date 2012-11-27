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

import java.io.ByteArrayInputStream ;
import java.io.IOException ;
import java.io.InputStream ;

import org.apache.jena.atlas.io.InputStreamBuffered ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Bytes ;
import org.junit.Test ;

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

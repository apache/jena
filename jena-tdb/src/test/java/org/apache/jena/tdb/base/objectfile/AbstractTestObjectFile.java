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

package org.apache.jena.tdb.base.objectfile;

import static org.apache.jena.tdb.base.BufferTestLib.sameValue ;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

public abstract class AbstractTestObjectFile extends BaseTest
{
    ObjectFile file ;

    @Before public void before() { file = make() ; }
    @After public void after() { release(file); }

    // Test 02 and 04 were for alloc-write.
    
    @Test public void objectfile_01()
    {
        assertEquals(0, file.length()) ;
    }

    @Test public void objectfile_03()
    {
        ByteBuffer bb = ByteBuffer.allocate(10) ;
        fill(bb) ;
        long x1 = file.write(bb) ;
        assertEquals(0, x1) ;
    }

    @Test public void objectfile_05()
    {
        ByteBuffer bb1 = ByteBuffer.allocate(10) ;
        fill(bb1) ;
        
        ByteBuffer bb2 = ByteBuffer.allocate(20) ;
        fill(bb2) ;
        long x1 = file.write(bb1) ;
        long x2 = file.write(bb2) ;
        
        assertFalse(x1 == x2) ;
    }

    @Test public void objectfile_06()
    {
        ByteBuffer bb1 = ByteBuffer.allocate(10) ;
        fill(bb1) ;
        
        ByteBuffer bb2 = ByteBuffer.allocate(20) ;
        fill(bb2) ;

        long x1 = file.write(bb1) ;
        long x2 = file.write(bb2) ;
        
        ByteBuffer bb1a = file.read(x1) ;
        ByteBuffer bb2a = file.read(x2) ;
        assertNotSame(bb1a, bb2a) ;
        assertTrue(sameValue(bb1, bb1a)) ;
        assertTrue(sameValue(bb2, bb2a)) ;
    }
    
//    // Oversized writes.
//
//    @Test public void objectfile_07()
//    {
//        
//    }
//
//    @Test public void objectfile_08()
//    {}
//
//    @Test public void objectfile_09()
//    {}
//
//    @Test public void objectfile_10()
//    {}
    
    public static void fill(ByteBuffer byteBuffer)
    {
        int len = byteBuffer.remaining() ;
        for ( int i = 0 ; i < len ; i++ )
            byteBuffer.put((byte)(i&0xFF)) ;
        byteBuffer.rewind() ;
    }

    protected abstract ObjectFile make() ;
    protected abstract void release(ObjectFile file) ;
}

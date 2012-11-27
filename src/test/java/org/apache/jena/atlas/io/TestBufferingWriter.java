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


import java.nio.ByteBuffer ;

import org.apache.jena.atlas.io.BufferingWriter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.Sink ;
import org.junit.Test ;

public class TestBufferingWriter extends BaseTest
{
    
    ByteBuffer bb = null ;
    BufferingWriter w = null ;
    
    public void create(int size, int blobSize)
    {
        bb = ByteBuffer.allocate(2048) ;
        Sink<ByteBuffer> sink = new BufferingWriter.SinkBuffer(bb) ;
        w = new BufferingWriter(sink, size, blobSize) ;
    }
    
    public String string()
    { 
        bb.flip();
        return Bytes.fromByteBuffer(bb) ;
    }
    
    @Test public void write_01()
    {
        create(10, 5) ;
        w.output("x") ;
        w.flush() ;
        String x = string() ;
        assertEquals("x", x) ;
    }

    @Test public void write_02()
    {
        create(10, 5) ;
        w.output("foofoo") ;    // Large object
        w.flush() ;
        String x = string() ;
        assertEquals("foofoo", x) ;
    }
    
    @Test public void write_03()
    {
        create(10, 8) ;
        w.output("a") ;
        w.output("b") ;
        w.output("c") ;
        w.flush() ;
        String x = string() ;
        assertEquals("abc", x) ;
    }
    
    @Test public void write_04()
    {
        create(10, 8) ;
        w.output("abcdefghijklmnopqrstuvwxyz") ;
        w.output("XYZ") ;
        w.flush() ;
        String x = string() ;
        assertEquals("abcdefghijklmnopqrstuvwxyzXYZ", x) ;
    }

    @Test public void write_05()
    {
        create(10, 8) ;
        w.output("") ;
        w.flush() ;
        String x = string() ;
        assertEquals("", x) ;
    }
    
    @Test public void write_06()
    {
        // Test closing the stream without flushing (the flush should be done implicitly)
        create(100, 50);
        w.output("test");
        w.close();
        String x = string();
        assertEquals("test", x);
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.io;


import java.nio.ByteBuffer ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Sink ;

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

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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
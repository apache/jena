/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.objectfile;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.block.Block ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import static com.hp.hpl.jena.tdb.base.BufferTestLib.* ;

public abstract class AbstractTestObjectFile extends BaseTest
{
    @Test public void objectfile_01()
    {
        ObjectFile file = make() ;
        assertEquals(0, file.length()) ;
    }

    @Test public void objectfile_02()
    {
        ObjectFile file = make() ;
        Block block = file.allocWrite(10) ;
        fill(block.getByteBuffer()) ;
        file.completeWrite(block) ;
        long x1 = block.getId() ;
        assertEquals(0, x1) ;
        
        ByteBuffer bb = file.read(x1) ;
        
        // position
        
        assertTrue(sameValue(block.getByteBuffer(), bb)) ;
        
    }

    @Test public void objectfile_03()
    {
        ObjectFile file = make() ;
        ByteBuffer bb = ByteBuffer.allocate(10) ;
        fill(bb) ;
        long x1 = file.write(bb) ;
        assertEquals(0, x1) ;
    }

    @Test public void objectfile_04()
    {
        ObjectFile file = make() ;
        
        Block block1 = file.allocWrite(10) ;
        fill(block1.getByteBuffer()) ;
        file.completeWrite(block1) ;
        
        Block block2 = file.allocWrite(20) ;
        fill(block2.getByteBuffer()) ;
        file.completeWrite(block2) ;
        
        long x1 = block1.getId() ;
        long x2 = block2.getId() ;
        
        assertFalse(x1 == x2) ;
        
    }

    @Test public void objectfile_05()
    {
        ObjectFile file = make() ;
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
        ObjectFile file = make() ;
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
    
    // Oversized writes.

    @Test public void objectfile_07()
    {
        
    }

    @Test public void objectfile_08()
    {}

    @Test public void objectfile_09()
    {}

    @Test public void objectfile_10()
    {}
    
    public static void fill(ByteBuffer byteBuffer)
    {
        int len = byteBuffer.remaining() ;
        for ( int i = 0 ; i < len ; i++ )
            byteBuffer.put((byte)(i&0xFF)) ;
        byteBuffer.rewind() ;
    }

    protected abstract ObjectFile make() ;
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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
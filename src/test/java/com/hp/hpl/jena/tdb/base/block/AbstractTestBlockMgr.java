/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;


import java.nio.ByteBuffer ;

import static org.openjena.atlas.lib.ByteBufferLib.fill ;

import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

public abstract class AbstractTestBlockMgr extends BaseTest
{
    static final public int BlkSize = 256 ;
    
    protected BlockMgr blockMgr = null ;
    
    @Before public void before()
    { 
       blockMgr = make() ;
       blockMgr.beginUpdate() ;
    }
    @After  public void after()
    {
        if (blockMgr != null)
        {
            blockMgr.endUpdate() ;
            blockMgr.close() ;
        }
    }
    
    @Test public void file01()
    {
        Block block = blockMgr.allocate(BlkSize) ;
        ByteBuffer bb = block.getByteBuffer() ;
        fill(bb, (byte)1) ;
        blockMgr.write(block) ;
        blockMgr.release(block) ;
    }
    
    @Test public void file02()
    {
        Block block = blockMgr.allocate(BlkSize) ;
        ByteBuffer bb = block.getByteBuffer() ;
        fill(bb, (byte)1) ;
        long id = block.getId() ;
        blockMgr.write(block) ;
        blockMgr.release(block) ;
        
        Block block2 = blockMgr.getRead(id) ;
        ByteBuffer bb2 = block2.getByteBuffer() ;
        assertEquals(bb2.capacity(), BlkSize) ;
        assertEquals(bb2.get(0), (byte)1) ;
        assertEquals(bb2.get(BlkSize-1), (byte)1) ;
        blockMgr.release(block2) ;
    }
    
    @Test public void file03()
    {
        Block block = blockMgr.allocate(BlkSize) ;
        ByteBuffer bb = block.getByteBuffer() ;
        fill(bb, (byte)2) ;
        long id = block.getId() ;
        blockMgr.write(block) ;
        blockMgr.release(block) ;

        Block block2 = blockMgr.getRead(id) ;
        ByteBuffer bb2 = block2.getByteBuffer() ;
        assertEquals(bb2.capacity(), BlkSize) ;
        assertEquals(bb2.get(0), (byte)2) ;
        assertEquals(bb2.get(BlkSize-1), (byte)2) ;
        blockMgr.release(block2) ;
    }

    @Test public void multiAccess01()
    {
        Block block1 = blockMgr.allocate(BlkSize) ;
        Block block2 = blockMgr.allocate(BlkSize) ;
        long id1 = block1.getId() ;
        long id2 = block2.getId() ;
        
        ByteBuffer bb1 = block1.getByteBuffer() ;
        ByteBuffer bb2 = block2.getByteBuffer() ;
        
        fill(bb1, (byte)1) ;
        fill(bb2, (byte)2) ;
        
        blockMgr.write(block1) ;
        blockMgr.write(block2) ;
        blockMgr.release(block1) ;
        blockMgr.release(block2) ;
        
        Block block3 = blockMgr.getRead(id1) ;
        Block block4 = blockMgr.getRead(id2) ;
        
        ByteBuffer bb_1 = block3.getByteBuffer() ;
        ByteBuffer bb_2 = block4.getByteBuffer() ;

        contains(bb_1, (byte)1) ;
        contains(bb_2, (byte)2) ;
        
        blockMgr.release(block3) ;
        blockMgr.release(block4) ;
    }
    
    
    protected abstract BlockMgr make() ; 
    
    protected static void contains(ByteBuffer bb, byte fillValue)
    {
        for ( int i = 0; i < bb.limit(); i++ )
            assertEquals("Index: "+i, bb.get(i), fillValue ) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
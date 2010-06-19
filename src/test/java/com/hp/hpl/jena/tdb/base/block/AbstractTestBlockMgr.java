/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
    
    @Before public void before() { blockMgr = make() ; }
    @After  public void after()
    {
        if (blockMgr != null) blockMgr.close() ;
    }
    
    @Test public void file01()
    {
        int id = blockMgr.allocateId() ;
        ByteBuffer bb = blockMgr.allocateBuffer(id) ;
        fill(bb, (byte)1) ;
        blockMgr.put(id, bb) ;
    }
    
    @Test public void file02()
    {
        int id = blockMgr.allocateId() ;
        ByteBuffer bb = blockMgr.allocateBuffer(id) ;
        fill(bb, (byte)1) ;
        blockMgr.put(id, bb) ;
        ByteBuffer bb2 = blockMgr.get(id) ;
        assertEquals(bb2.capacity(), BlkSize) ;
        assertEquals(bb2.get(0), (byte)1) ;
        assertEquals(bb2.get(BlkSize-1), (byte)1) ;
    }
    
    @Test public void file03()
    {
        int id = blockMgr.allocateId() ;
        ByteBuffer bb = blockMgr.allocateBuffer(id) ;
        fill(bb, (byte)2) ;
        blockMgr.put(id, bb) ;
        ByteBuffer bb2 = blockMgr.get(id) ;
        assertEquals(bb2.capacity(), BlkSize) ;
        assertEquals(bb2.get(0), (byte)2) ;
        assertEquals(bb2.get(BlkSize-1), (byte)2) ;
    }

    // Move to abstract class
    @Test public void multiAccess01()
    {
        int id1 = blockMgr.allocateId() ;
        int id2 = blockMgr.allocateId() ;

        ByteBuffer bb1 = blockMgr.allocateBuffer(id1) ;
        ByteBuffer bb2 = blockMgr.allocateBuffer(id2) ;

        fill(bb1, (byte)1) ;
        fill(bb2, (byte)2) ;

        blockMgr.put(id1, bb1) ;
        blockMgr.put(id2, bb2) ;

        ByteBuffer bb_1 = blockMgr.get(id1) ;
        ByteBuffer bb_2 = blockMgr.get(id2) ;

        contains(bb_1, (byte)1) ;
        contains(bb_2, (byte)2) ;
        
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
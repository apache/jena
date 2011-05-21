/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.nio.ByteBuffer ;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockException ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrTracker ;

public class TestBlockMgrTracked extends BaseTest
{
    // Mainly testing the tracking

    static boolean b ;
    @BeforeClass static public void beforeClass()   { b = BlockMgrTracker.verbose ; BlockMgrTracker.verbose = false ; }
    @AfterClass  static public void afterClass()    { BlockMgrTracker.verbose = b ;}
    
    
    @Test public void track_01()
    {
        BlockMgr mgr = BlockMgrFactory.createMem("BPTRecord", 4) ;
        mgr = BlockMgrFactory.tracker(mgr) ;
        mgr.beginUpdate() ;
        Block block = mgr.allocate(4) ;
        ByteBuffer bb = block.getByteBuffer() ;
        bb.putInt(0,1234) ;
        mgr.write(block) ;
        mgr.release(block) ;
        // -----
        Block block2 = mgr.getRead(block.getId()) ;
        ByteBuffer bb2 = block2.getByteBuffer() ;
        assertArrayEquals(bb.array(), bb2.array()) ;
        mgr.release(block2) ;
        mgr.endUpdate() ;
    }

    // Multiple overlapping read operations.
    static BlockMgr setup()
    {
        BlockMgr mgr = BlockMgrFactory.createMem("BPTRecord", 4) ;
        mgr = BlockMgrFactory.tracker(mgr) ;
        return mgr ;
    }
    
    static void write(BlockMgr mgr, int value)
    {
        mgr.beginUpdate() ;
        Block block = mgr.allocate(4) ;
        ByteBuffer bb = block.getByteBuffer() ;
        bb.putInt(0,value) ;
        mgr.write(block) ;
        mgr.release(block) ;
        mgr.endUpdate() ;
    }
    
    @Test public void track_02()
    {
        BlockMgr mgr = setup() ;
        write(mgr, 1234) ;
        write(mgr, 5678) ;

        mgr.beginRead() ;
        mgr.beginRead() ;

        Block b0 = mgr.getRead(0) ;
        Block b1 = mgr.getRead(1) ;
        
        mgr.release(b1) ;
        mgr.release(b0) ;
        
        mgr.endRead() ;
        mgr.endRead() ;
    }
    
    @Test(expected=BlockException.class)
    public void track_03()
    {
        BlockMgr mgr = setup() ;
        write(mgr, 1234) ;
        write(mgr, 5678) ;

        mgr.beginRead() ;
        Block b0 = mgr.getWrite(0) ;
        mgr.endRead() ;
    }

    @Test(expected=BlockException.class)
    public void track_04()
    {
        BlockMgr mgr = setup() ;
        write(mgr, 1234) ;
        mgr.beginRead() ;
        Block b0 = mgr.getRead(0) ;
        mgr.promote(b0) ;
        mgr.endRead() ;
    }

    @Test(expected=BlockException.class)
    public void track_05()
    {
        BlockMgr mgr = setup() ;
        mgr.beginRead() ;
        mgr.endUpdate() ;
    }

    @Test(expected=BlockException.class)
    public void track_06()
    {
        BlockMgr mgr = setup() ;
        mgr.beginUpdate() ;
        mgr.endRead() ;
    }

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
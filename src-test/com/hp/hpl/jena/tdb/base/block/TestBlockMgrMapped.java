/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import static com.hp.hpl.jena.tdb.ConfigTest.testingDir;

import java.nio.ByteBuffer;

import arq.cmd.CmdUtils;

import lib.FileOps;
import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestBlockMgrMapped extends AbstractTestBlockMgr
{
    static boolean logging = false ;
    
    static { if ( logging ) CmdUtils.setLog4j() ; }
    
    static final String filename = testingDir+"/block-mgr" ;
    
    // Windows is iffy about deleting memory mapped files.
    
    @BeforeClass static public void remove1() { FileOps.deleteSilent(filename) ; }
    @AfterClass  static public void remove2() { FileOps.deleteSilent(filename) ; }
    
    @Override
    protected BlockMgr make()
    { return new BlockMgrMapped(filename, BlkSize) ; }
    
    // Move to abstract class
    @Test public void multiAccess01()
    {
        if ( logging )
        {
            org.apache.log4j.Logger log = org.apache.log4j.LogManager.getLogger(BlockMgrMapped.class) ;
            log.setLevel(Level.ALL) ;
        }
        
        BlockMgr bMgr = blockMgr ; 
        
        int id1 = blockMgr.allocateId() ;
        int id2 = blockMgr.allocateId() ;
        
        System.out.printf("id1 = %d\n", id1) ;
        System.out.printf("id2 = %d\n", id2) ;
        
        ByteBuffer bb1 = blockMgr.allocateBuffer(id1) ;
        ByteBuffer bb2 = blockMgr.allocateBuffer(id2) ;
        fill(bb1, (byte)1) ;
        fill(bb2, (byte)2) ;
        blockMgr.put(id1, bb1) ;
        blockMgr.put(id2, bb2) ;
        
        System.out.printf("bb1 = %s\n", bb1) ;
        System.out.printf("bb2 = %s\n", bb2) ;
        
        // How to trigger the raw segment issues??
        // Can't easily!  getSilent slices which does a duplicate.
        // So need to force unpositionable effects via 

        // This should make the first corrupt the underlying mapped BB 
        ByteBuffer bb_2 = blockMgr.get(id2) ;
        ByteBuffer bb_1 = blockMgr.get(id1) ;

        System.out.printf("bb_1 = %s\n", bb_1) ;
        System.out.printf("bb_2 = %s\n", bb_2) ;
        
        contains(bb_1, (byte)1) ;
        contains(bb_2, (byte)2) ;
        
    }
    
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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
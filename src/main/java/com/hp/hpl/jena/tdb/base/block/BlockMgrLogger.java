/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;


public class BlockMgrLogger implements BlockMgr //extends BlockMgrWrapper
{
    private final BlockMgr blockMgr ;
    protected final Logger log ;
    protected final boolean logAllOperations ;
    
    public BlockMgrLogger(String label, BlockMgr blockMgr, boolean logAllOperations )
    {
        this(LoggerFactory.getLogger(label), blockMgr, logAllOperations) ;
    }
    
    public BlockMgrLogger(Logger log, BlockMgr blockMgr, boolean logAllOperations )
    {
        this.blockMgr = blockMgr ;
        this.log = log ;
        this.logAllOperations = logAllOperations ;
    }

    @Override
    public Block allocate(BlockType blockType, int blockSize)
    {
        Block x = blockMgr.allocate(blockType, blockSize) ;
        log.info("Allocate: "+x.getId()) ;
        return x ;
    }

    @Override
    public boolean isEmpty()
    {
        log.info("isEmpty") ;
        return blockMgr.isEmpty() ;
    }

    @Override
    public Block getRead(int id)
    {
        log.info("getRead("+id+")") ;
        return blockMgr.getRead(id) ;
    }

    @Override
    public Block getWrite(int id)
    {
        log.info("getWrite("+id+")") ;
        return blockMgr.getWrite(id) ;
    }

    @Override
    public Block promote(Block block)
    {
        log.info("promote("+block.getId()+")") ;
        return blockMgr.promote(block) ;
    }

    @Override
    public void releaseRead(Block block)
    {
        log.info("releaseRead("+block.getId()+")") ;
        blockMgr.releaseRead(block) ;
    }

    @Override
    public void releaseWrite(Block block)
    {
        log.info("releaseWrite("+block.getId()+")") ;
        blockMgr.releaseWrite(block) ;
    }

    @Override
    public void put(Block block)
    {
        log.info("put("+block.getId()+")") ;
        blockMgr.put(block) ;
    }

    @Override
    public void free(Block block)
    {
        log.info("freeBlock("+block.getId()+")") ;
        blockMgr.free(block) ;
    }

    @Override
    public boolean valid(int id)
    {
        log.info("valid("+id+")") ;
        return blockMgr.valid(id) ;
    }

    @Override
    public void close()
    {
        log.info("close") ;
        blockMgr.close() ;
    }

    @Override
    public boolean isClosed()
    {
        log.info("isClosed") ;
        return blockMgr.isClosed() ;
    }

    @Override
    public void sync()
    {
        log.info("Sync") ;
        blockMgr.sync() ;
    }

    @Override
    public void startRead()
    {
        log.info("> start read") ;
        blockMgr.startRead() ;
    }

    @Override
    public void finishRead()
    {
        log.info("< finish read") ;
        blockMgr.finishRead() ;
    }

    @Override
    public void startUpdate()
    {
        log.info("> start update") ;
        blockMgr.startUpdate() ;
    }

    @Override
    public void finishUpdate()
    {
        log.info("< finish update") ;
        blockMgr.finishUpdate() ;
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
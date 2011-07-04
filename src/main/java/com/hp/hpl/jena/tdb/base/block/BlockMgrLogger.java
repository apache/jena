/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.util.Iterator ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;


public class BlockMgrLogger implements BlockMgr //extends BlockMgrWrapper
{
    private final BlockMgr blockMgr ;
    protected final Logger log ;
    protected final boolean logAllOperations ;
    private final String label ;
    
    public BlockMgrLogger(BlockMgr blockMgr, boolean logAllOperations )
    {
        this(null, blockMgr.getLabel(), blockMgr, logAllOperations) ;
    }
    
    public BlockMgrLogger(String label, BlockMgr blockMgr, boolean logAllOperations )
    {
        this(null, label, blockMgr, logAllOperations) ;
    }

    public BlockMgrLogger(Logger log, String label, BlockMgr blockMgr, boolean logAllOperations )
    {
        this.blockMgr = blockMgr ;
        if ( log == null )
            log = LoggerFactory.getLogger(BlockMgr.class) ;
        this.log = log ;
        this.logAllOperations = logAllOperations ;
        this.label = label ;
    }

    @Override
    public String getLabel() { return label ; }
    
    @Override
    public Block allocate(int blockSize)
    {
        Block x = blockMgr.allocate(blockSize) ;
        info("Allocate("+x.getId()+")") ;
        return x ;
    }

    @Override
    public boolean isEmpty()
    {
        info("isEmpty") ;
        return blockMgr.isEmpty() ;
    }

    @Override
    public Block getRead(long id)
    {
        info("getRead("+id+")") ;
        return blockMgr.getRead(id) ;
    }

    @Override
    public Block getReadIterator(long id)
    {
        info("getReadIterator("+id+")") ;
        return blockMgr.getReadIterator(id) ;
    }
    
    @Override
    public Block getWrite(long id)
    {
        info("getWrite("+id+")") ;
        return blockMgr.getWrite(id) ;
    }

    @Override
    public Block promote(Block block)
    {
        info("promote("+block.getId()+")") ;
        return blockMgr.promote(block) ;
    }

    @Override
    public void release(Block block)
    {
        info("release("+block.getId()+")") ;
        blockMgr.release(block) ;
    }

    @Override
    public void write(Block block)
    {
        info("write("+block.getId()+")") ;
        blockMgr.write(block) ;
    }

    @Override
    public void overwrite(Block block)
    {
        info("overwrite("+block.getId()+")") ;
        blockMgr.overwrite(block) ;
    }

    @Override
    public void free(Block block)
    {
        info("freeBlock("+block.getId()+")") ;
        blockMgr.free(block) ;
    }

    @Override
    public boolean valid(int id)
    {
        info("valid("+id+")") ;
        return blockMgr.valid(id) ;
    }

    @Override
    public void close()
    {
        info("close") ;
        blockMgr.close() ;
    }

    @Override
    public boolean isClosed()
    {
        info("isClosed") ;
        return blockMgr.isClosed() ;
    }

    @Override
    public void sync()
    {
        info("Sync") ;
        blockMgr.sync() ;
    }

    @Override
    public void beginIterator(Iterator<?> iter)
    {
        info("> start iterator") ;
        blockMgr.beginIterator(iter) ;
    }
    
    @Override
    public void endIterator(Iterator<?> iter)
    {
        info("< end iterator") ;
        blockMgr.endIterator(iter) ;
    }
    
    @Override
    public void beginRead()
    {
        info("> start read") ;
        blockMgr.beginRead() ;
    }

    @Override
    public void endRead()
    {
        info("< finish read") ;
        blockMgr.endRead() ;
    }

    @Override
    public void beginUpdate()
    {
        info("> start update") ;
        blockMgr.beginUpdate() ;
    }

    @Override
    public void endUpdate()
    {
        info("< finish update") ;
        blockMgr.endUpdate() ;
    }

    private void info(String string)
    {
        if ( label != null )
            string = label+": "+string ;
        log.info(string) ; 
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
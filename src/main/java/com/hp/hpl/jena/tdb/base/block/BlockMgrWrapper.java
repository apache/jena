/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.util.Iterator ;


public class BlockMgrWrapper implements BlockMgr
{
    protected BlockMgr blockMgr ;

    public BlockMgrWrapper(BlockMgr blockMgr)
    {
        setBlockMgr(blockMgr) ;
    }

    /** Set another BlockMgr as the target of the wrapper - return the old one */ 
    protected final BlockMgr setBlockMgr(BlockMgr blockMgr)
    {
        BlockMgr old = blockMgr ;
        this.blockMgr = blockMgr ;
        return old ;
    }
    
    @Override
    public Block allocate(int blockSize)
    {
        return blockMgr.allocate(blockSize) ;
    }

    @Override
    public Block getRead(long id)
    {
        return blockMgr.getRead(id) ;
    }

    @Override
    public Block getReadIterator(long id)
    {
        return blockMgr.getReadIterator(id) ;
    }


    @Override
    public Block getWrite(long id)
    {
        return blockMgr.getWrite(id) ;
    }

    @Override
    public Block promote(Block block)
    {
        return blockMgr.promote(block) ;
    }

    @Override
    public void release(Block block)
    {
        blockMgr.release(block) ;
    }

    @Override
    public void write(Block block)
    {
        blockMgr.write(block) ;
    }

    @Override
    public void free(Block block)
    {
        blockMgr.free(block) ;
    }

    @Override
    public boolean isEmpty()
    {
        return blockMgr.isEmpty() ;
    }

    @Override
    public void sync()
    {
        blockMgr.sync() ;
    }

    @Override
    public boolean valid(int id)
    {
        return blockMgr.valid(id) ;
    }

    @Override
    public boolean isClosed()
    {
        return blockMgr.isClosed() ;
    }

    @Override
    public void close()
    { blockMgr.close() ; }


    @Override
    public void beginIterator(Iterator<?> iter)
    {
        blockMgr.beginIterator(iter) ;
    }

    @Override
    public void endIterator(Iterator<?> iter)
    {
        blockMgr.endIterator(iter) ;
    }
    
    @Override
    public void beginRead()
    {
        blockMgr.beginRead() ;
    }

    @Override
    public void endRead()
    {
        blockMgr.endRead() ;
    }

    @Override
    public void beginUpdate()
    {
        blockMgr.beginUpdate() ;
    }

    @Override
    public void endUpdate()
    {
        blockMgr.endUpdate() ;
    }

    @Override
    public String getLabel()
    {
        return blockMgr.getLabel() ;
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
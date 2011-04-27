/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;


/** Add synchronized to a BlockMgr.  This is the same as BlockMgrWrapper but with 'synchronized' added */

public class BlockMgrSync implements BlockMgr
{
    protected final BlockMgr blockMgr ;

    public BlockMgrSync(BlockMgr blockMgr)
    {
        this.blockMgr = blockMgr ;
    }

    //@Override
    synchronized
    public Block allocate(BlockType blockType, int blockSize)
    {
        return blockMgr.allocate(blockType, blockSize) ;
    }

    //@Override
    synchronized
    public Block getRead(int id)
    {
        return blockMgr.getRead(id) ;
    }

    //@Override
    synchronized
    public Block getWrite(int id)
    {
        return blockMgr.getWrite(id) ;
    }

    //@Override
    synchronized
    public Block promote(Block block)
    {
        return blockMgr.promote(block) ;
    }
    
    //@Override
    synchronized
    public void releaseRead(Block block)
    {
        blockMgr.releaseRead(block) ;
    }

    //@Override
    synchronized
    public void releaseWrite(Block block)
    {
        blockMgr.releaseWrite(block) ;
    }

    //@Override
    synchronized
    public void put(Block block)
    {
        blockMgr.put(block) ;
    }

    //@Override
    synchronized
    public void freeBlock(Block block)
    {
        blockMgr.freeBlock(block) ;
    }

    //@Override
    synchronized
    public int blockSize()
    { return blockMgr.blockSize() ; }
    

    //@Override
    synchronized
    public void sync()
    {
        blockMgr.sync() ;
    }
    
    //@Override
    synchronized
    public void close()
    { blockMgr.close() ; }

    //@Override
    synchronized
    public boolean isEmpty()
    {
        return blockMgr.isEmpty() ;
    }

    //@Override
    synchronized
    public void startRead()
    {
        blockMgr.startRead() ;
    }

    //@Override
    synchronized
    public void finishRead()
    {
        blockMgr.finishRead() ;
    }

    //@Override
    synchronized
    public void startUpdate()
    {
        blockMgr.startUpdate() ;
    }

    //@Override
    synchronized
    public void finishUpdate()
    {
        blockMgr.finishUpdate() ;
    }

    //@Override
    synchronized
    public boolean valid(int id)
    {
        return blockMgr.valid(id) ;
    }

    //@Override
    synchronized
    public boolean isClosed()
    {
        return blockMgr.isClosed() ;
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
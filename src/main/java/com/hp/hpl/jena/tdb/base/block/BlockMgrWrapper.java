/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.nio.ByteBuffer;

/** Base class to support writing stacks of BlockMgrs */

public class BlockMgrWrapper implements BlockMgr
{
    protected final BlockMgr blockMgr ;

    public BlockMgrWrapper(BlockMgr blockMgr)
    {
        this.blockMgr = blockMgr ;
    }

    //@Override
    public int allocateId()
    {
        return blockMgr.allocateId() ;
    }

    //@Override
    public ByteBuffer allocateBuffer(int id)
    {
        return blockMgr.allocateBuffer(id) ;
    }

    //@Override
    public int blockSize()
    { return blockMgr.blockSize() ; }
    
    //@Override
    public ByteBuffer get(int id)
    {
        return blockMgr.get(id) ;
    }

    //@Override
    public void put(int id, ByteBuffer block)
    {
        blockMgr.put(id, block) ;
    }

    //@Override
    public void freeBlock(int id)
    {
        blockMgr.freeBlock(id) ;
    }

    //@Override
    public void sync()
    {
        blockMgr.sync() ;
    }
    
    //@Override
    public void close()
    { blockMgr.close() ; }

    //@Override
    public boolean isEmpty()
    {
        return blockMgr.isEmpty() ;
    }

    //@Override
    public void startRead()
    {
        blockMgr.startRead() ;
    }

    //@Override
    public void finishRead()
    {
        blockMgr.finishRead() ;
    }

    //@Override
    public void startUpdate()
    {
        blockMgr.startUpdate() ;
    }

    //@Override
    public void finishUpdate()
    {
        blockMgr.finishUpdate() ;
    }

    //@Override
    public boolean valid(int id)
    {
        return blockMgr.valid(id) ;
    }

    //@Override
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
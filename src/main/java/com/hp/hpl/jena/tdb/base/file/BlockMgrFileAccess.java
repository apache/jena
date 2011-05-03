/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockType ;

public class BlockMgrFileAccess implements BlockMgr
{
    private final FileAccess file ;

    // tracking.
    
    public BlockMgrFileAccess(FileAccess fileAccess)
    {
        this.file = fileAccess ;
    }

    @Override
    public Block allocate(BlockType blockType, int blockSize)
    {
        return null ;
    }

    @Override
    public boolean isEmpty()
    {
        return false ;
    }

    @Override
    public int blockSize()
    {
        return 0 ;
    }

    @Override
    public Block getRead(int id)
    {
        return null ;
    }

    @Override
    public void releaseRead(Block block)
    {}

    @Override
    public void releaseWrite(Block block)
    {}

    @Override
    public Block getWrite(int id)
    {
        return null ;
    }

    @Override
    public Block promote(Block block)
    {
        return null ;
    }

    @Override
    public void put(Block block)
    {}

    @Override
    public void freeBlock(Block block)
    {}

    @Override
    public boolean valid(int id)
    {
        return false ;
    }

    @Override
    public void close()
    {}

    @Override
    public boolean isClosed()
    {
        return false ;
    }

    @Override
    public void sync()
    {}

    @Override
    public void startUpdate()
    {}

    @Override
    public void finishUpdate()
    {}

    @Override
    public void startRead()
    {}

    @Override
    public void finishRead()
    {}
    
    
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
/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;

public class BlockMgrTracker implements BlockMgr //extends BlockMgrWrapper
{
    private final BlockMgr blockMgr ;
    
    // Not a BlockMgrWrapper to ensure we write all the operations.
    
    public BlockMgrTracker(BlockMgr blockMgr)
    {
        this.blockMgr = blockMgr ;
    }

    public int allocateId()
    {
        return 0 ;
    }

    public ByteBuffer allocateBuffer(int id)
    {
        return null ;
    }

    public boolean isEmpty()
    {
        return false ;
    }

    public int blockSize()
    {
        return 0 ;
    }

    public ByteBuffer get(int id)
    {
        return null ;
    }

    public void put(int id, ByteBuffer block)
    {}

    public void freeBlock(int id)
    {}

    public boolean valid(int id)
    {
        return false ;
    }

    public void close()
    {}

    public boolean isClosed()
    {
        return false ;
    }

    public void sync()
    {}

    public void startUpdate()
    {}

    public void finishUpdate()
    {}

    public void startRead()
    {}

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
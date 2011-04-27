/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.util.ArrayList ;
import java.util.List ;


// Recycle blocks - but only in-session.
// At the end of JVM run, the blocks are made "permanent" as no one finds them again on restart.
public class BlockMgrFreeChain extends BlockMgrWrapper
{
    // Could keep Pair<Integer, ByteBuffer>
    List<Block> freeBlocks = new ArrayList<Block>() ;
    private BlockType managedBlockType ;
    
    public BlockMgrFreeChain(BlockMgr blockMgr, BlockType blockType)
    {
        super(blockMgr) ;
        this.managedBlockType = blockType ;
    }

    @Override
    public Block allocate(BlockType blockType, int blockSize)
    {
        if ( freeBlocks.size() > 0 )
        {
            Block block = freeBlocks.remove(freeBlocks.size()-1) ;
            return block ;
        }
        return super.allocate(blockType, blockSize) ;
    }

    @Override
    public void freeBlock(Block block)
    {
        if ( block.getType().equals(managedBlockType) )
        {
            freeBlocks.add(block) ;
            return ;
        }
        super.freeBlock(block) ;
    }

    @Override
    public boolean valid(int id)
    {
        for ( Block blk : freeBlocks ) 
        {
            if ( blk.getId() == id )
                return true ; 
        }
        return super.valid(id) ;
    }

    @Override
    public void sync()
    {
        super.sync() ;
        
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
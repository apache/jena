/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import static java.lang.String.format ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.file.BlockAccess ;

/** Block manager that maps from the FileAccess layer to a BlockMgr. 
 * Add free block management (but we should layer with BlockMgrFreeChain) 
 */

final
public class BlockMgrFileAccess extends BlockMgrBase
{
    private static Logger log = LoggerFactory.getLogger(BlockMgrFileAccess.class) ;
    
    private final BlockAccess file ;
    private boolean closed = false ;
    
    // Create via the BlockMgrFactory.
    /*package*/ BlockMgrFileAccess(BlockAccess blockAccess, int blockSize)
    {
        super(blockAccess.getLabel(), blockSize) ;
        file = blockAccess ;
    }
    
    @Override
    protected Block allocate()
    {
        return file.allocate(blockSize) ;
    }

    @Override
    public Block promote(Block block)
    {
        return block ;
    }

    @Override
    public Block getReadIterator(long id)
    {
        return getBlock(id) ;
    }

    
    @Override
    public Block getRead(long id)
    {
        return getBlock(id) ;
    }

    @Override
    public Block getWrite(long id)
    {
        return getBlock(id) ;
    }

    private Block getBlock(long id)
    {
        Block block = file.read(id) ;
        return block ;
    }

    @Override
    public void release(Block block)
    { 
        //check(block) ;
    }

    @Override
    public void write(Block block)
    {
        file.write(block) ;
    }

    @Override
    public void overwrite(Block block)
    {
        file.overwrite(block) ;
    }


    @Override
    public void free(Block block)
    {
        // We do nothing about free blocks currently.
    }

    @Override
    public boolean valid(int id)
    {
        return file.valid(id) ;
    }

    @Override
    public void sync()
    { file.sync() ; }
    
    @Override
    public boolean isClosed() { return closed ; }  
    
    @Override
    public void close()
    { 
        closed = true ;
        file.close() ;
    }
    
    @Override
    public boolean isEmpty()
    {
        return file.isEmpty() ;
    }

    @Override
    public String toString() { return format("BlockMgrFileAccess[%d bytes]:%s", blockSize, file) ; }
    
    @Override
    protected Logger getLog()
    {
        return log ;
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
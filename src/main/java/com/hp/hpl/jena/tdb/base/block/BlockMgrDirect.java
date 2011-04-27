/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Block manager that is NOT memory mapped. */
public class BlockMgrDirect extends BlockMgrFile
{
    // [TxTDB:PATCH-UP]
    // BlockMgrFile/BlockMgrDirect/BlockMgrMapped need reworking
    
    
    // Consider: having one file per "segment", not one large file.
    private static Logger log = LoggerFactory.getLogger(BlockMgrDirect.class) ;
    
    BlockMgrDirect(String filename, int blockSize)
    {
        super(filename, blockSize) ;
    }
    
    @Override
    public Block allocate(BlockType blockType)
    {
//    if ( getLog().isDebugEnabled() ) 
//        getLog().debug(format("allocateBuffer(%d)", id)) ;
        int id = allocateId() ;
        ByteBuffer bb = ByteBuffer.allocate(blockSize) ;
        Block block = new Block(id, blockType, bb) ;
        return block ;
    }

    @Override
    public Block getRead(int id)
    { 
        // [TxTDB:PATCH-UP]
        return get(id) ; 
    }
        
    @Override
    public Block getWrite(int id)
    { 
        // [TxTDB:PATCH-UP]
        return get(id) ; 
    }

    private Block get(int id)
    {
        check(id) ;
        checkIfClosed() ;
        ByteBuffer bb = ByteBuffer.allocate(blockSize) ;
        readByteBuffer(id, bb) ;
        Block block = new Block(id, BlockType.UNDEF, bb) ;
        readByteBuffer(id, bb) ;
        return block ;
    }

    // [TxTDB:PATCH-UP]
    @Override
    public void releaseRead(Block block)
    {}

    @Override
    public void releaseWrite(Block block)
    {}

    @Override
    public Block promote(Block block)
    {
        return block ;
    }

    private void readByteBuffer(int id, ByteBuffer dst)
    {
        try {
            int len = channel.read(dst, filePosition(id)) ;
            if ( len != blockSize )
                throw new BlockException(format("get: short read (%d, not %d)", len, blockSize)) ;   
        } catch (IOException ex)
        { throw new BlockException("BlockMgrDirect.get", ex) ; }
    }
    
    @Override
    public void put(Block block)
    {
        check(block) ;
        checkIfClosed() ;
        ByteBuffer bb = block.getByteBuffer() ;
        bb.position(0) ;
        bb.limit(bb.capacity()) ;
        try {
            int len = channel.write(bb, filePosition(block.getId())) ;
            if ( len != blockSize )
                throw new BlockException(format("put: short write (%d, not %d)", len, blockSize)) ;   
        } catch (IOException ex)
        { throw new BlockException("BlockMgrDirect.put", ex) ; }
        putNotification(block) ;
    }
    
    
    private final long filePosition(int id)
    {
        return ((long)id)*((long)blockSize) ;
    }
    
    @Override
    public void freeBlock(Block block)
    { 
        check(block.getId()) ;
        checkIfClosed() ;
    }
    
//    @Override
//    public void finishUpdate()
//    {}
//
//    @Override
//    public void startUpdate()
//    {}
//
//    @Override
//    public void startRead()
//    {}
//
//    @Override
//    public void finishRead()
//    {}

    @Override
    protected Logger getLog()
    {
        return log ;
    }

    static long count = 0 ;
    @Override
    public void sync()
    {
        count++ ;
        force() ;
        if ( getLog().isDebugEnabled() )
            getLog().debug("Sync/BlockMgrDirect "+label+" -- "+count) ;
    }

    @Override
    protected void _close()
    {
        super.force() ;
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
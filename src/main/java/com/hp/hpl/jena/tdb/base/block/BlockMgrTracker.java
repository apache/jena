/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.util.ArrayList ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;


public class BlockMgrTracker /*extends BlockMgrWrapper*/ implements BlockMgr
{
    // Don't inherit BlockMgrWrapper to make sure this class caches everything.
    // Track the status of blocks.
    protected final Set<Integer> activeReadBlocks = new HashSet<Integer>() ;
    protected final Set<Integer> activeWriteBlocks = new HashSet<Integer>() ;
    
    // Track operations - clear on finish*  
    protected final List<Integer> getReadIds = new ArrayList<Integer>() ;
    protected final List<Integer> getWriteIds = new ArrayList<Integer>() ;
    protected final List<Integer> releasedReadIds = new ArrayList<Integer>() ;
    protected final List<Integer> releasedWriteIds = new ArrayList<Integer>() ;
    protected final List<Integer> putIds = new ArrayList<Integer>() ;
    protected final List<Integer> freedIds = new ArrayList<Integer>() ;
    
    protected final BlockMgr blockMgr ;
    
    private void clearInternal()
    {
        activeReadBlocks.clear() ;
        activeWriteBlocks.clear() ;
        getReadIds.clear() ;
        getWriteIds.clear() ;
        releasedReadIds.clear() ;
        releasedWriteIds.clear() ;
        putIds.clear() ;
        freedIds.clear() ;
    }
    
    private boolean inRead = false ;
    private boolean inUpdate = false ;
    private final Logger log ;
    
    public BlockMgrTracker(String label, BlockMgr blockMgr)
    {
        //super(label, blockMgr, logUpdatesOnly) ;
        this.blockMgr = blockMgr ;
        log = LoggerFactory.getLogger(label) ;
    }

    public Block allocate(BlockType blockType, int blockSize)
    {
        return blockMgr.allocate(blockType, blockSize) ;
    }

    //@Override
    public Block getRead(int id)
    {
        checkRead("getRead") ;
        getReadIds.add(id) ;
        activeReadBlocks.add(id) ;
        return blockMgr.getRead(id) ;
    }

    //@Override
    public Block getWrite(int id)
    {
        checkUpdate("getWrite") ;
        activeWriteBlocks.add(id) ;
        getWriteIds.add(id) ;
        return blockMgr.getWrite(id) ;
    }
    
    //@Override
    public Block promote(Block block)
    {
        log.info("promote("+block.getId()+")") ;
        return blockMgr.promote(block) ;
    }
    
    //@Override
    public void releaseRead(Block block)
    {   
        Integer id = block.getId() ;
        checkRead("releaseRead") ;
        if ( ! activeReadBlocks.contains(id) )
        {
            if ( getReadIds.contains(id) )
                log.error("Multiple releaseRead("+id+") after getRead()") ;
            else
                log.error("releaseRead("+id+") but no getRead()") ;
        }
        activeReadBlocks.remove(block.getId()) ;
        releasedReadIds.add(block.getId()) ;
        blockMgr.releaseRead(block) ;
    }

    //@Override
    public void releaseWrite(Block block)
    {   
        Integer id = block.getId() ;
        checkUpdate("releaseRead") ;
        if ( ! activeWriteBlocks.contains(id) )
        {
            if ( getWriteIds.contains(id) )
                log.error("Multiple releaseWrite("+id+") after getWrite()") ;
            else
                log.error("releaseWrite("+id+") but no getWrite()") ;
        }
        activeWriteBlocks.remove(id) ;
        releasedWriteIds.add(id) ;
        blockMgr.releaseWrite(block) ;
    }

    //@Override
    public void put(Block block)
    {
        Integer id = block.getId() ;
        checkUpdate("put") ;
        if ( ! activeWriteBlocks.contains(id) )
        {
            if ( getWriteIds.contains(id) )
                log.error("Multiple attempts to return write block "+id) ;
            else
                log.error("put("+id+") but no getWrite()") ;
        }
        activeWriteBlocks.remove(id) ;
        putIds.add(id) ;
        blockMgr.put(block) ;
    }

    //@Override
    public void freeBlock(Block block)
    {
        Integer id = block.getId() ;
        checkUpdate("freeBlock") ;
        if ( ! activeWriteBlocks.contains(id) )
        {
            if ( getWriteIds.contains(id) )
                log.error("Multiple attempts to return write block "+id) ;
            else
                log.error("freeBlock("+id+") but no getWrite()") ;
        }
        freedIds.add(block.getId()) ;
        blockMgr.freeBlock(block) ;
    }

    //@Override
    public int blockSize()
    { return blockMgr.blockSize() ; }
    

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
    public boolean valid(int id)
    {
        return blockMgr.valid(id) ;
    }

    //@Override
    public boolean isClosed()
    {
        return blockMgr.isClosed() ;
    }

    //@Override
    public void startRead()
    {
        if ( inRead )
            log.warn("startRead when already in read") ;
        if ( inUpdate )
            log.warn("startRead when already in update") ;
        
        inRead = true ;
        inUpdate = false ;
        blockMgr.startRead() ;
    }

    //@Override
    public void finishRead()
    {
        if ( ! inRead )
            log.error("finishRead but not in read") ;
        if ( inUpdate )
            log.error("finishRead when in update") ;
        if ( ! activeReadBlocks.isEmpty() )
            log.error("Outstanding read blocks at end of read operations") ; 

        inUpdate = false ;
        inRead = false ;
        clearInternal() ;
        blockMgr.finishRead() ;
    }

    //@Override
    public void startUpdate()
    {
        if ( inRead )
            log.error("startUpdate when already in read") ;
        if ( inUpdate )
            log.error("startUpdate when already in update") ;
        inUpdate = true ;
        blockMgr.startUpdate() ;
    }

    //@Override
    public void finishUpdate()
    {
        if ( ! inUpdate )
            log.warn("finishUpdate but not in update") ;
        if ( inRead )
            log.warn("finishUpdate when in read") ;
        
        if ( ! activeReadBlocks.isEmpty() )
            log.error("Outstanding read blocks at end of read operations") ; 

        inUpdate = false ;
        inRead = false ;
        clearInternal() ;
        blockMgr.finishUpdate() ;
    }

    private void checkUpdate(String method)
    {
        if ( ! inUpdate )
            log.error(method+" called outside update") ;
    }

    private void checkRead(String method)
    {
        if ( ! inUpdate && ! inRead )
            log.error(method+" called outside update and read") ;
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
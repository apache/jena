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

import org.openjena.atlas.lib.Pair ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;


public class BlockMgrTracker /*extends BlockMgrWrapper*/ implements BlockMgr
{
    // Don't inherit BlockMgrWrapper to make sure this class caches everything.

    // Track the active state of the BlockMgr
    protected final Set<Integer> activeReadBlocks = new HashSet<Integer>() ;
    protected final Set<Integer> activeWriteBlocks = new HashSet<Integer>() ;
    
    // Track the operations
    // an enum (op+id) and a single list. 
    static enum Action { Alloc, Promote, GetRead, GetWrite, ReleaseRead, ReleaseWrite, Put, Free }
    protected final List<Pair<Action, Integer>> actions = new ArrayList<Pair<Action, Integer>>() ;
    
    protected final BlockMgr blockMgr ;
    
    private void clearInternal()
    {
        activeReadBlocks.clear() ;
        activeWriteBlocks.clear() ;
        actions.clear() ;
    }
    
    private boolean inRead = false ;
    private boolean inUpdate = false ;
    private final Logger log ;
    
    public BlockMgrTracker(String label, BlockMgr blockMgr)
    {
        this(LoggerFactory.getLogger(label), blockMgr) ;
    }
    
    public BlockMgrTracker(Class<?> label, BlockMgr blockMgr)
    {
        this(LoggerFactory.getLogger(label), blockMgr) ;
    }
    
    public BlockMgrTracker(Logger logger, BlockMgr blockMgr)
    {
        this.blockMgr = blockMgr ;
        this.log = logger ;
    }

    private void add(Action action, Integer id) { actions.add(new Pair<Action, Integer>(action, id)) ; }

    @Override
    public Block allocate(BlockType blockType, int blockSize)
    {
        checkUpdate("allocate") ;
        Block block = blockMgr.allocate(blockType, blockSize) ;
        Integer id = block.getId() ;
        activeWriteBlocks.add(id) ;
        add(Action.Alloc, id) ;
        return block ;
    }

    
    @Override
    public Block getRead(int id)
    {
        checkRead("getRead") ;
        activeReadBlocks.add(id) ;
        add(Action.GetRead, id) ;
        return blockMgr.getRead(id) ;
    }

    @Override
    public Block getWrite(int id)
    {
        checkUpdate("getWrite") ;
        activeWriteBlocks.add(id) ;
        add(Action.GetWrite, id) ;
        return blockMgr.getWrite(id) ;
    }
    
    @Override
    public Block promote(Block block)
    {
        Integer id = block.getId() ;
        activeWriteBlocks.add(id) ;
        if ( activeReadBlocks.contains(id) )
            activeReadBlocks.remove(id) ;
        else
        {
            if ( activeWriteBlocks.contains(id) )
                log.info("Promoting "+id+" - already a write") ;
            else
                log.warn("Promoting "+id+" but not a read block") ;
        }
             
        add(Action.Promote, id) ;
        return blockMgr.promote(block) ;
    }
    
    @Override
    public void releaseRead(Block block)
    {   
        Integer id = block.getId() ;
        checkRead("releaseRead") ;
        
        if ( ! activeReadBlocks.contains(id) )
            log.error("releaseRead("+id+") -- not an active read block") ;

        if ( activeWriteBlocks.contains(id) )
            log.error("releaseRead("+id+") on a write block") ;
            
        activeReadBlocks.remove(block.getId()) ;
        add(Action.ReleaseRead, id) ;
        blockMgr.releaseRead(block) ;
    }

    @Override
    public void releaseWrite(Block block)
    {   
        Integer id = block.getId() ;
        checkUpdate("releaseRead") ;

        if ( activeReadBlocks.contains(id) )
            log.error("releaseWrite("+id+") on a read block") ;

        if ( ! activeWriteBlocks.contains(id) )
            log.error("releaseWrite("+id+") not an action write block") ;
        activeWriteBlocks.remove(id) ;
        add(Action.ReleaseWrite, id) ;
        blockMgr.releaseWrite(block) ;
    }

    @Override
    public void put(Block block)
    {
        Integer id = block.getId() ;
        checkUpdate("put") ;
        if ( ! activeWriteBlocks.contains(id) )
            log.error("put("+id+") but no active write block()") ;
        activeWriteBlocks.remove(id) ;
        add(Action.Put, id) ;
        blockMgr.put(block) ;
    }

    @Override
    public void freeBlock(Block block)
    {
        Integer id = block.getId() ;
        checkUpdate("freeBlock") ;
        if ( activeReadBlocks.contains(id) )
            log.error("freeBlock("+id+") on a read block") ;
        if ( ! activeWriteBlocks.contains(id) )
            log.error("freeBlock("+id+") but not a write block") ;
        add(Action.Free, id) ;
        blockMgr.freeBlock(block) ;
    }

    @Override
    public void sync()
    {
        blockMgr.sync() ;
    }
    
    @Override
    public void close()
    { blockMgr.close() ; }

    @Override
    public boolean isEmpty()
    {
        return blockMgr.isEmpty() ;
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

    @Override
    public void finishRead()
    {
        if ( ! inRead )
            log.error("finishRead but not in read") ;
        if ( inUpdate )
            log.error("finishRead when in update") ;
        
        checkEmpty("Outstanding read blocks at end of read operations",
                   activeReadBlocks) ;
        checkEmpty("Outstanding write blocks at end of read operations!",
                   activeWriteBlocks) ;
        
        inUpdate = false ;
        inRead = false ;
        clearInternal() ;
        blockMgr.finishRead() ;
    }

    @Override
    public void startUpdate()
    {
        if ( inRead )
            log.error("startUpdate when already in read") ;
        if ( inUpdate )
            log.error("startUpdate when already in update") ;
        inUpdate = true ;
        blockMgr.startUpdate() ;
    }

    @Override
    public void finishUpdate()
    {
        if ( ! inUpdate )
            log.warn("finishUpdate but not in update") ;
        if ( inRead )
            log.warn("finishUpdate when in read") ;
     
        checkEmpty("Outstanding read blocks at end of update operations",
                   activeReadBlocks) ;

        checkEmpty("Outstanding write blocks at end of update operations",
                   activeWriteBlocks) ;
        
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

    private void checkEmpty(String string, Set<Integer> blocks)
    {
        if ( ! blocks.isEmpty() )
        {
            log.error(string) ;
            for ( Integer id : blocks )
                log.info("    Block: "+id) ;
            history() ;
        }
    }
    
    private void history()
    {
        log.info("History") ;
        for ( Pair<Action, Integer> p : actions ) 
            log.info(String.format("    %-12s  %d", p.getLeft(), p.getRight())) ;
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
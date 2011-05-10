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

import com.hp.hpl.jena.tdb.TDBException ;

import org.openjena.atlas.lib.Pair ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import static com.hp.hpl.jena.tdb.base.block.BlockMgrTracker.Action.* ;

public class BlockMgrTracker /*extends BlockMgrWrapper*/ implements BlockMgr
{
    // Don't inherit BlockMgrWrapper to make sure this class caches everything.

    // Track the active state of the BlockMgr
    protected final Set<Integer> activeReadBlocks = new HashSet<Integer>() ;
    protected final Set<Integer> activeWriteBlocks = new HashSet<Integer>() ;
    
    // Track the operations
    // an enum (op+id) and a single list. 
    static enum Action { Alloc, Promote, GetRead, GetWrite, Write, Release, Free }
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
    public Block allocate(int blockSize)
    {
        checkUpdate("allocate") ;
        Block block = blockMgr.allocate(blockSize) ;
        Integer id = block.getId() ;
        activeWriteBlocks.add(id) ;
        add(Alloc, id) ;
        return block ;
    }

    
    @Override
    public Block getRead(int id)
    {
        checkRead("getRead") ;
        add(GetRead, id) ;
        activeReadBlocks.add(id) ;
        return blockMgr.getRead(id) ;
    }

    @Override
    public Block getWrite(int id)
    {
        checkUpdate("getWrite") ;
        add(GetWrite, id) ;
        activeWriteBlocks.add(id) ;
        return blockMgr.getWrite(id) ;
    }
    
    @Override
    public Block promote(Block block)
    {
        Integer id = block.getId() ;
        checkUpdate("promote") ;
        add(Promote, id) ;
        
        if ( ! activeWriteBlocks.contains(id) && ! activeReadBlocks.contains(id) )
            error(Promote, id+" is not an active block") ;
        
        activeReadBlocks.remove(id) ;
        activeWriteBlocks.add(id) ;
        return blockMgr.promote(block) ;
    }
    
    @Override
    public void release(Block block)
    {
        Integer id = block.getId() ;
        checkRead("release") ;
        add(Release, id) ;

     // [TxTDB:PATCH-UP] Iterator trip this.
//        if ( ! activeReadBlocks.contains(id) && ! activeWriteBlocks.contains(id) )
//            error(Release, id+" is not an active block") ;
            
        activeReadBlocks.remove(block.getId()) ;
        activeWriteBlocks.remove(block.getId()) ;
        blockMgr.release(block) ;
    }

    @Override
    public void write(Block block)
    {
        Integer id = block.getId() ;
        checkUpdate("write") ;
        add(Write, id) ;
        if ( ! activeWriteBlocks.contains(id) )
            error(Write, id+ " is not an active write block") ;
        blockMgr.write(block) ;
    }

    @Override
    public void free(Block block)
    {
        Integer id = block.getId() ;
        checkUpdate("freeBlock") ;
        add(Free, id) ;
        if ( activeReadBlocks.contains(id) )
            error(Free, id+" is a read block") ;
        else if ( ! activeWriteBlocks.contains(id) )
            error(Free, id+" is not a write block") ;
        activeWriteBlocks.remove(id) ;
        blockMgr.free(block) ;
    }

    private void error(Action action, String string)
    {
        log.error(action+": "+string) ;
        history() ;
        throw new TDBException() ;
        //debugPoint() ;
    }

    // Do nothing - but use a a breakpoint point.
    private void debugPoint() {}

    private void warn(Action action, String string)
    { log.warn(action+": "+string) ; }

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
        // [TxTDB:PATCH-UP] Iterator trip this.
//        if ( ! inUpdate && ! inRead )
//            log.error(method+" called outside update and read") ;
    }

    private void checkEmpty(String string, Set<Integer> blocks)
    {
        if ( ! blocks.isEmpty() )
        {
            log.error(string) ;
            for ( Integer id : blocks )
                log.info("    Block: "+id) ;
            history() ;
            debugPoint() ;
        }
    }
    
    private void history()
    {
        log.info("History") ;
        for ( Pair<Action, Integer> p : actions ) 
            log.info(String.format("    %-12s  %d", p.getLeft(), p.getRight())) ;
    }
    
    @Override
    public String toString() { return "BlockMgrTracker: "+log.getName() ; } 
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
/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import static com.hp.hpl.jena.tdb.base.block.BlockMgrTracker.Action.* ;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.lib.MultiSet ;
import org.openjena.atlas.lib.Pair ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.TDBException ;

public class BlockMgrTracker /*extends BlockMgrWrapper*/ implements BlockMgr
{
    public static boolean verbose = false ;

    static enum Action { Alloc, Promote, GetRead, GetWrite, Write, Release, Free, IterRead, BeginRead, EndRead, BeginUpdate, EndUpdate}
    static final Integer NoId = Integer.valueOf(-9) ;


    // Don't inherit BlockMgrWrapper to make sure this class caches everything.

    // XXX Issue: two block with same id but different ByteBuffers --> in someway, don't
    //   Check in getRead/getWrite/getReadIterator
    
    // ---- State for tracking
    // Track and count block references and releases
    // XXX Can a block be a read block AND a write block?
    //   No - the page is dirty.
    protected final MultiSet<Integer> activeReadBlocks   = new MultiSet<Integer>() ;
    protected final MultiSet<Integer> activeWriteBlocks  = new MultiSet<Integer>() ;
    protected final MultiSet<Integer> activeIterBlocks   = new MultiSet<Integer>() ;
    // Track the operations 
    protected final List<Pair<Action, Integer>> actions = new ArrayList<Pair<Action, Integer>>() ;
    protected final List<Iterator<?>> activeIterators = new ArrayList<Iterator<?>>() ;
    // ---- State for tracking
    
    protected final BlockMgr blockMgr ;
    
    private void clearInternalRW()
    {
        activeReadBlocks.clear() ;
        activeWriteBlocks.clear() ;
        actions.clear() ;
    }
    
    private void clearInternalIter()
    {
        clearInternalRW() ;
        activeIterators.clear() ;
        activeIterBlocks.clear() ;
    }

    private int inRead = 0 ;
    private int inIterator = 0 ;
    private boolean inUpdate = false ;
    private final Logger log ;
    
    public static BlockMgr track(String label, BlockMgr blkMgr)
    {
        return new BlockMgrTracker(label, blkMgr) ;
    }

    public BlockMgrTracker(BlockMgr blockMgr)
    {
        this(LoggerFactory.getLogger(BlockMgrTracker.class), blockMgr) ;
    }
    
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
        Block block ;
        synchronized (this)
        {
            checkUpdate(Alloc) ;
            block = blockMgr.allocate(blockSize) ;
            Integer id = block.getId() ;
            activeWriteBlocks.add(id) ;
            add(Alloc, id) ;
        }
        return block ;
    }
    
    @Override
    public Block getRead(int id)
    {
        synchronized (this)
        {
            checkRead(GetRead) ;
            add(GetRead, id) ;
            activeReadBlocks.add(id) ;
        }
        return blockMgr.getRead(id) ;
    }
    
    @Override
    public Block getReadIterator(int id)
    {
        synchronized (this)
        {
            checkReadOrIter(IterRead) ;
            add(IterRead, id) ;
            activeIterBlocks.add(id) ;
        }
        return blockMgr.getReadIterator(id) ;
    }    

    @Override
    public Block getWrite(int id)
    {
        synchronized (this)
        {
            checkUpdate(GetWrite) ;
            add(GetWrite, id) ;
            activeWriteBlocks.add(id) ;
        }
        return blockMgr.getWrite(id) ;
    }
    
    @Override
    public Block promote(Block block)
    {
        synchronized (this)
        {
            checkUpdate(Promote) ;
            Integer id = block.getId() ;
            add(Promote, id) ;

            if ( ! activeWriteBlocks.contains(id) && ! activeReadBlocks.contains(id) )
                error(Promote, id+" is not an active block") ;

            activeReadBlocks.remove(id) ;
            // Still on read blocks?
            activeWriteBlocks.add(id) ;
        }
        return blockMgr.promote(block) ;
    }
    
    @Override
    public void release(Block block)
    {
        synchronized (this)
        {
            checkReadOrIter(Release) ;
            Integer id = block.getId() ;
            add(Release, id) ;

            if ( ! activeReadBlocks.contains(id) && ! activeIterBlocks.contains(id) && ! activeWriteBlocks.contains(id) )
                error(Release, id+" is not an active block") ;

            // ???????????????????????
            activeReadBlocks.remove(block.getId()) ;
            activeIterBlocks.remove(block.getId()) ;
            activeWriteBlocks.remove(block.getId()) ;
        }
        blockMgr.release(block) ;
    }

    @Override
    public void write(Block block)
    {
        synchronized (this)
        {
            checkUpdate(Write) ;
            Integer id = block.getId() ;
            add(Write, id) ;
            if ( ! activeWriteBlocks.contains(id) )
                error(Write, id+ " is not an active write block") ;
        }
        blockMgr.write(block) ;
    }

    @Override
    public void free(Block block)
    {
        synchronized (this)
        {
            checkUpdate(Free) ;
            Integer id = block.getId() ;
            add(Free, id) ;
            if ( activeReadBlocks.contains(id) )
                error(Free, id+" is a read block") ;
            else if ( ! activeWriteBlocks.contains(id) )
                error(Free, id+" is not a write block") ;
            activeWriteBlocks.remove(id) ;
        }
        blockMgr.free(block) ;
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
    public void beginIterator(Iterator<?> iter)
    {
        synchronized (this)
        {
            if ( activeIterators.contains(iter) )
                log.warn("Iterator already active: "+iter) ;
            activeIterators.add(iter) ;
        }
        blockMgr.beginIterator(iter) ;
    }

    @Override
    public void endIterator(Iterator<?> iter)
    {
        synchronized (this)
        {
            if ( ! activeIterators.contains(iter) )
                log.warn("Iterator not active: "+iter) ;
            activeIterators.remove(iter) ;
            if ( activeIterators.size() == 0 )
                checkEmpty("Outstanding iterator read blocks", activeIterBlocks) ;
        }
        blockMgr.endIterator(iter) ;
    }
    
    @Override
    synchronized
    public void beginRead()
    {
        synchronized (this)
        {
            if ( inUpdate )
                error(BeginRead, "beginRead when already in update") ;
            inRead++ ;
            inUpdate = false ;
        }
        blockMgr.beginRead() ;
    }

    @Override
    synchronized
    public void endRead()
    {
        synchronized (this)
        {
            if ( inRead == 0 )
                error(EndRead, "endRead but not in read") ;
            if ( inUpdate )
                error(EndRead, "endRead when in update") ;

            checkEmpty("Outstanding write blocks at end of read operations!",
                       activeWriteBlocks) ;

            if ( inRead == 0 )
            {
                // Check at end of multiple reads or a write
                checkEmpty("Outstanding read blocks at end of read operations",
                           activeReadBlocks) ;
                clearInternalRW() ;
            }

            inUpdate = false ;
            inRead-- ;
        }
        blockMgr.endRead() ;
    }

    @Override
    public void beginUpdate()
    {
        synchronized (this)
        {
            if ( inRead > 0 )
                error(BeginUpdate, "beginUpdate when already in read") ;
            if ( inUpdate )
                error(BeginUpdate, "beginUpdate when already in update") ;
            inUpdate = true ;
        }
        blockMgr.beginUpdate() ;
    }

    @Override
    public void endUpdate()
    {
        synchronized (this)
        {
            if ( ! inUpdate )
                error(EndUpdate, "endUpdate but not in update") ;
            if ( inRead > 0 )
                error(EndUpdate, "endUpdate when in read") ;

            checkEmpty("Outstanding read blocks at end of update operations",
                       activeReadBlocks) ;

            checkEmpty("Outstanding write blocks at end of update operations",
                       activeWriteBlocks) ;

            inUpdate = false ;
            inRead = 0 ;
            clearInternalRW() ;
        }
        blockMgr.endUpdate() ;
    }

    private void checkUpdate(Action action)
    {
        if ( ! inUpdate )
            error(action,"called outside update") ;
    }

    private void checkRead(Action action)
    {
        if ( ! inUpdate && inRead == 0 )
            error(action, "Called outside update and read") ;
    }

    private void checkReadOrIter(Action action)
    {
        if ( ! inUpdate && inRead == 0 && activeIterators.size() == 0 )
            error(action, "Called outside update, read or an iterator") ;
    }


    private void checkEmpty(String string, MultiSet<Integer> blocks)
    {
        if ( ! blocks.isEmpty() )
        {
            log.error(string) ;
            for ( Integer id : blocks )
                log.info("    Block: "+id) ;
            history() ;
            throw new TDBException() ;
            //debugPoint() ;
        }
    }
    
    private void error(Action action, String string)
    {
        if ( verbose )
        {
            log.error(action+": "+string) ;
            history() ;
        }
        throw new BlockException(action+": "+string) ;
        //debugPoint() ;
    }

    // Do nothing - but use a a breakpoint point.
    private void debugPoint() {}

    private void warn(Action action, String string)
    { log.warn(action+": "+string) ; }

    private void history()
    {
        log.info("History") ;
        for ( Pair<Action, Integer> p : actions )
        {
            if ( p.getRight() != NoId )
                log.info(String.format("    %-12s  %d", p.getLeft(), p.getRight())) ;
            else
                log.info(String.format("    %-12s", p.getLeft())) ;
            
        }
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
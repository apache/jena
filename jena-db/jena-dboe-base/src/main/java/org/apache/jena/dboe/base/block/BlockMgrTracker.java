/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.dboe.base.block ;

import static org.apache.jena.dboe.base.block.BlockMgrTracker.Action.Alloc;
import static org.apache.jena.dboe.base.block.BlockMgrTracker.Action.BeginRead;
import static org.apache.jena.dboe.base.block.BlockMgrTracker.Action.BeginUpdate;
import static org.apache.jena.dboe.base.block.BlockMgrTracker.Action.EndRead;
import static org.apache.jena.dboe.base.block.BlockMgrTracker.Action.EndUpdate;
import static org.apache.jena.dboe.base.block.BlockMgrTracker.Action.Free;
import static org.apache.jena.dboe.base.block.BlockMgrTracker.Action.GetRead;
import static org.apache.jena.dboe.base.block.BlockMgrTracker.Action.GetWrite;
import static org.apache.jena.dboe.base.block.BlockMgrTracker.Action.Promote;
import static org.apache.jena.dboe.base.block.BlockMgrTracker.Action.Release;
import static org.apache.jena.dboe.base.block.BlockMgrTracker.Action.Write;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.dboe.DBOpEnvException;
import org.apache.jena.ext.com.google.common.collect.HashMultiset ;
import org.apache.jena.ext.com.google.common.collect.Multiset ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class BlockMgrTracker /* extends BlockMgrWrapper */ implements BlockMgr {
    private static Logger loggerDefault = LoggerFactory.getLogger(BlockMgrTracker.class) ;
    public static boolean verbose       = false ;

    static enum Action {
        Alloc, Promote, GetRead, GetWrite, Write, Release, Free, 
        BeginRead, EndRead, 
        BeginUpdate, EndUpdate
    }
    static final Long                        NoId              = (long)-9 ;

    // ---- State for tracking
    // Track and count block references and releases
    // No - the page is dirty.
    
    
    protected final Multiset<Long>           activeReadBlocks  = HashMultiset.create() ;
    protected final Multiset<Long>           activeWriteBlocks = HashMultiset.create() ;
    // Track the operations
    protected final List<Pair<Action, Long>> actions           = new ArrayList<>() ;
    // ---- State for tracking

    protected final BlockMgr                 blockMgr ;

    private void clearInternalRW() {
        activeReadBlocks.clear() ;
        activeWriteBlocks.clear() ;
        actions.clear() ;
    }

    private void clearInternalIter() {
        clearInternalRW() ;
    }

    private int          inRead     = 0 ;
    private boolean      inUpdate   = false ;
    private boolean      inBatch    = false ;
    private final Logger log ;
    private final String label ;

    public static BlockMgr track(BlockMgr blkMgr) {
        return track(blkMgr.getLabel(), blkMgr) ;
    }

    private static BlockMgr track(String label, BlockMgr blkMgr) {
        return new BlockMgrTracker(label, blkMgr) ;
    }

    private BlockMgrTracker(BlockMgr blockMgr) {
        this(LoggerFactory.getLogger(BlockMgrTracker.class), blockMgr.getLabel(), blockMgr) ;
    }

    private BlockMgrTracker(String label, BlockMgr blockMgr) {
        this(loggerDefault, label, blockMgr) ;
    }

    //
    // public BlockMgrTracker(Class<?> cls, String label, BlockMgr blockMgr)
    // {
    // this(LoggerFactory.getLogger(cls), label, blockMgr) ;
    // }

    private BlockMgrTracker(Logger logger, String label, BlockMgr blockMgr) {
        this.blockMgr = blockMgr ;
        this.log = logger ;
        this.label = blockMgr.getLabel() ;
    }

    private void add(Action action, Long id) {
        actions.add(new Pair<>(action, id)) ;
    }

    @Override
    public Block allocate(int blockSize) {
        Block block ;
        synchronized (this) {
            checkUpdate(Alloc) ;
            block = blockMgr.allocate(blockSize) ;
            Long id = block.getId() ;
            activeWriteBlocks.add(id) ;
            add(Alloc, id) ;
        }
        return block ;
    }

    @Override
    public Block getRead(long id) {
        // What if this is a write block already?
        synchronized (this) {
            checkRead(GetRead) ;
            Long x = id ;
            add(GetRead, x) ;

            if ( activeWriteBlocks.contains(x) )
                activeWriteBlocks.add(x) ;
            else
                activeReadBlocks.add(x) ;
        }
        return blockMgr.getRead(id) ;
    }

    @Override
    public Block getWrite(long id) {
        synchronized (this) {
            checkUpdate(GetWrite) ;
            Long x = id ;
            add(GetWrite, x) ;
            activeWriteBlocks.add(x) ;
        }
        return blockMgr.getWrite(id) ;
    }

    @Override
    public Block promote(Block block) {
        synchronized (this) {
            checkUpdate(Promote) ;
            Long id = block.getId() ;
            add(Promote, id) ;

            if ( !activeWriteBlocks.contains(id) && !activeReadBlocks.contains(id) )
                error(Promote, id + " is not an active block") ;

            if ( activeReadBlocks.contains(id) )
                // Remove one read count
                // Really, do this if obtained via "getRead" and not "getWrite"
                activeReadBlocks.remove(id) ;

            // Double promotion results in only one entry.
            if ( !activeWriteBlocks.contains(id) )
                activeWriteBlocks.add(id) ;
        }
        return blockMgr.promote(block) ;
    }

    @Override
    public void release(Block block) {
        synchronized (this) {
            checkRead(Release) ;
            Long id = block.getId() ;
            add(Release, id) ;

            if ( !activeReadBlocks.contains(id) && !activeWriteBlocks.contains(id) )
                error(Release, id + " is not an active block") ;

            // May have been promoted.
            if ( activeWriteBlocks.contains(id) )
                activeWriteBlocks.remove(id) ;
            else
                activeReadBlocks.remove(block.getId()) ;
        }
        blockMgr.release(block) ;
    }

    @Override
    public void write(Block block) {
        writeTracker(block) ;
        blockMgr.write(block) ;
    }

    @Override
    synchronized public void overwrite(Block block) {
        writeTracker(block) ;
        blockMgr.overwrite(block) ;
    }

    private void writeTracker(Block block) {
        synchronized (this) {
            checkUpdate(Write) ;
            Long id = block.getId() ;
            add(Write, id) ;
            if ( !activeWriteBlocks.contains(id) )
                error(Write, id + " is not an active write block") ;
        }
    }

    @Override
    public void free(Block block) {
        synchronized (this) {
            checkUpdate(Free) ;
            Long id = block.getId() ;
            add(Free, id) ;
            if ( activeReadBlocks.contains(id) )
                error(Free, id + " is a read block") ;
            else if ( !activeWriteBlocks.contains(id) )
                error(Free, id + " is not a write block") ;
            activeWriteBlocks.remove(id) ;
        }
        blockMgr.free(block) ;
    }

    @Override
    public void sync() {
        blockMgr.sync() ;
    }

    @Override
    public void syncForce() {
        blockMgr.syncForce() ;
    }

    @Override
    public void close() {
        blockMgr.close() ;
    }

    @Override
    public boolean isEmpty() {
        return blockMgr.isEmpty() ;
    }
    
    @Override
    public long allocLimit() {
        return blockMgr.allocLimit() ;
    }
    
    @Override
    public void resetAlloc(long boundary) {
        blockMgr.resetAlloc(boundary) ;
    }
    
    @Override
    public boolean valid(int id) {
        return blockMgr.valid(id) ;
    }

    @Override
    public boolean isClosed() {
        return blockMgr.isClosed() ;
    }

    @Override
    synchronized public void beginRead() {
        synchronized (this) {
            if ( inUpdate )
                error(BeginRead, "beginRead when already in update") ;
            inRead++ ;
            inUpdate = false ;
        }
        blockMgr.beginRead() ;
    }

    @Override
    synchronized public void endRead() {
        synchronized (this) {
            if ( inRead == 0 )
                error(EndRead, "endRead but not in read") ;
            if ( inUpdate )
                error(EndRead, "endRead when in update") ;

            checkEmpty("Outstanding write blocks at end of read operations!", activeWriteBlocks) ;

            if ( inRead == 0 ) {
                // Check at end of multiple reads or a write
                checkEmpty("Outstanding read blocks at end of read operations", activeReadBlocks) ;
                clearInternalRW() ;
            }

            inUpdate = false ;
            inRead-- ;
        }
        blockMgr.endRead() ;
    }

    @Override
    public void beginUpdate() {
        synchronized (this) {
            if ( inRead > 0 )
                error(BeginUpdate, "beginUpdate when already in read") ;
            if ( inUpdate )
                error(BeginUpdate, "beginUpdate when already in update") ;
            inUpdate = true ;
        }
        blockMgr.beginUpdate() ;
    }

    @Override
    public void endUpdate() {
        synchronized (this) {
            if ( !inUpdate )
                error(EndUpdate, "endUpdate but not in update") ;
            if ( inRead > 0 )
                error(EndUpdate, "endUpdate when in read") ;

            checkEmpty("Outstanding read blocks at end of update operations", activeReadBlocks) ;

            checkEmpty("Outstanding write blocks at end of update operations", activeWriteBlocks) ;

            inUpdate = false ;
            inRead = 0 ;
            clearInternalRW() ;
        }
        blockMgr.endUpdate() ;
    }

    private void checkUpdate(Action action) {
//        if ( ! inBatch )
//            warn(action, "update called outside a batch") ;
        if ( ! inUpdate )
            error(action, "called outside update") ;
    }

    private void checkRead(Action action) {
        if ( !inUpdate && inRead == 0 )
            error(action, "Called outside update and read") ;
    }

    private void checkEmpty(String string, Multiset<Long> blocks) {
        if ( !blocks.isEmpty() ) {
            error(string) ;
            for ( Long id : blocks )
                info("    Block: " + id) ;
            history() ;
            throw new DBOpEnvException() ;
            // debugPoint() ;
        }
    }

    private String msg(String string) {
        if ( label == null )
            return string ;
        return label + ": " + string ;
    }

    private void info(String string) {
        log.info(msg(string)) ;
    }

    private void warn(String string) {
        log.warn(msg(string)) ;
    }

    private void warn(Action action, String string) {
        warn(action + ": " + string) ;
    }

    private void error(String string) {
        log.error(msg(string)) ;
    }

    private void error(Action action, String string) {
        if ( verbose ) {
            error(action + ": " + string) ;
            history() ;
        }
        throw new BlockException(msg(action + ": " + string)) ;
        // debugPoint() ;
    }

    // Do nothing - but use a a breakpoint point.
    private void debugPoint() {}

    private void history() {
        info("History") ;
        for ( Pair<Action, Long> p : actions ) {
            if ( p.getRight() != NoId )
                log.info(String.format("%s:     %-12s  %d", label, p.getLeft(), p.getRight())) ;
            else
                log.info(String.format("%s:     %-12s", label, p.getLeft())) ;

        }
    }

    @Override
    public String toString() {
        return "BlockMgrTracker" + ((label == null) ? "" : (": " + label)) ;
    }

    @Override
    public String getLabel() {
        return label ;
    }
}

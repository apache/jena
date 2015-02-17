/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.base.file;

import static java.lang.String.format ;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.List ;

import org.seaborne.dboe.base.block.Block ;
import org.seaborne.dboe.sys.SystemIndex ;

/**
 * File access layer that simulates a disk in-memory - for testing, not written for efficiency.
 * There is a safe mode, whereby blocks are copied in and out to guarantee no writing to an unallocated block.
 * This is very inefficient but a better simulation of a disk.
 * 
 * @See BlockAccessByteArray
 */

public class BlockAccessMem implements BlockAccess
{
    public static boolean SafeMode = true ;
    static final boolean Checking = true ;
    boolean fileClosed = false ;
    private List<Block> blocks = new ArrayList<>() ;
    private final boolean safeModeThisMgr ;
    protected final int blockSize ;
    private final String label ;
    
    public BlockAccessMem(String label, int blockSize) {
        this(label, blockSize, SafeMode) ;
    }

    private BlockAccessMem(String label, int blockSize, boolean b) {
        this.blockSize = blockSize ;
        this.label = label ;
        safeModeThisMgr = b ;
    }

    @Override
    public Block allocate(int blkSize) {
        if ( blkSize > 0 && blkSize != this.blockSize )
            throw new FileException("Fixed blocksize only: request= " + blkSize + " / fixed size=" + this.blockSize) ;

        int x = blocks.size() ;
        ByteBuffer bb = ByteBuffer.allocate(blkSize) ;
        Block block = new Block(x, bb) ;
        blocks.add(block) ;
        return block ;
    }

    @Override
    public Block read(long id) {
        check(id) ;
        Block blk = blocks.get((int)id) ;
        if ( safeModeThisMgr ) {
            // [[Dev-RO]]
            blk = blk.replicate() ;
            blk.setModified(false) ; 
        }
        return blk ;
    }

    @Override
    public void write(Block block) {
        check(block) ;
        _write(block) ;
    }

    @Override
    public void overwrite(Block block) {
        write(block) ;
    }
    
    private void _write(Block block) {
        if ( safeModeThisMgr ) {
            block = block.replicate() ;
            // [[Dev-RO]]
            block.setModified(false) ;
        }
        // Memory isn't scaling to multi gigabytes.
        blocks.set(block.getId().intValue(), block) ;
    }

    @Override
    public boolean isEmpty() {
        return blocks.isEmpty() ;
    }

    @Override
    public long allocBoundary() {
        return blocks.size() ;
    }

    @Override
    public void resetAllocBoundary(long boundary) {
        // Clear the list from boundary onwards.
        blocks.subList((int)boundary, blocks.size()).clear() ;
    }
    
    @Override
    public boolean valid(long id) {
        return id >= 0 && id < blocks.size() ;
    }

    @Override
    public void close() {
        fileClosed = true ;
        // blocks = null ;
    }

    @Override
    public void sync() {}

    private void check(Block block) {
        check(block.getId()) ;
        check(block.getByteBuffer()) ;
    }

    private void check(long id) {
        if ( id > Integer.MAX_VALUE )
            throw new FileException("BlockAccessMem: Bounds exception (id large than an int): " + id) ;
        if ( !Checking )
            return ;
        if ( id < 0 || id >= blocks.size() )
            throw new FileException("BlockAccessMem: "+label+": Bounds exception: " + id + " in [0, "+blocks.size()+")") ;
    }

    private void check(ByteBuffer bb) {
        if ( !Checking )
            return ;
        if ( bb.capacity() != blockSize )
            throw new FileException(format("FileAccessMem: Wrong size block.  Expected=%d : actual=%d", blockSize, bb.capacity())) ;
        if ( bb.order() != SystemIndex.NetworkOrder )
            throw new FileException("BlockMgrMem: Wrong byte order") ;
    }

    @Override
    public String getLabel() {
        return label ;
    }

    @Override
    public String toString() {
        return "Mem:" + label ;
    }
}

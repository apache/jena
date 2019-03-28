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

package org.apache.jena.dboe.base.file;

import static java.lang.String.format;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.dboe.base.block.Block;
import org.apache.jena.dboe.sys.Sys;

/**
 * File access layer that simulates a disk in-memory - for testing, not written for efficiency.
 * There is a safe mode, whereby blocks are copied in and out to guarantee no writing to an unallocated block.
 * This is very inefficient but a better simulation of a disk.
 *
 * @see BlockAccessByteArray
 */

public class BlockAccessMem implements BlockAccess
{
    // "SafeMode" - duplicate the block data much like a disk.
    // Mildly expensive for large block sizes.

    public static boolean SafeMode = true;
    static final boolean Checking = true;
    boolean fileClosed = false;
    private List<Block> blocks = new ArrayList<>();
    private final boolean safeModeThisMgr;
    protected final int blockSize;
    private final String label;

    public BlockAccessMem(String label, int blockSize) {
        this(label, blockSize, SafeMode);
    }

    private BlockAccessMem(String label, int blockSize, boolean b) {
        this.blockSize = blockSize;
        this.label = label;
        safeModeThisMgr = b;
    }

    @Override
    public Block allocate(int blkSize) {
        checkNotClosed();
        if ( blkSize > 0 && blkSize != this.blockSize )
            throw new FileException("Fixed blocksize only: request= " + blkSize + " / fixed size=" + this.blockSize);

        int x = blocks.size();
        ByteBuffer bb = ByteBuffer.allocate(blkSize);
        Block block = new Block(x, bb);
        blocks.add(block);
        return block;
    }

    @Override
    public Block read(long id) {
        checkNotClosed();
        check(id);
        Block blk = blocks.get((int)id);
        blk = replicateBlock(blk);
        blk.setModified(false);
        return blk;
    }

    @Override
    public void write(Block block) {
        checkNotClosed();
        check(block);
        _write(block);
    }

    @Override
    public void overwrite(Block block) {
        checkNotClosed();
        write(block);
    }

    private void _write(Block block) {
        block = replicateBlock(block);
        block.setModified(false);
        // Memory isn't scaling to multi gigabytes.
        blocks.set(block.getId().intValue(), block);
    }

    private Block replicateBlock(Block blk) {
        if ( safeModeThisMgr )
            // Deep replicate.
            return  blk.replicate();
        // Just the block wrapper.
        return new Block(blk.getId(), blk.getByteBuffer());
    }

    @Override
    public boolean isEmpty() {
        checkNotClosed();
        return blocks.isEmpty();
    }

    @Override
    public long allocBoundary() {
        checkNotClosed();
        return blocks.size();
    }

    @Override
    public void resetAllocBoundary(long boundary) {
        checkNotClosed();
        // Clear the list from boundary onwards.
        blocks.subList((int)boundary, blocks.size()).clear();
    }

    @Override
    public boolean valid(long id) {
        checkNotClosed();
        return id >= 0 && id < blocks.size();
    }

    private void checkNotClosed() {
        if ( fileClosed )
            throw new RuntimeIOException("Already closed");
    }

    @Override
    public void close() {
        if ( fileClosed )
            return;
        fileClosed = true;
        blocks = null;
    }

    @Override
    public void sync() {
        checkNotClosed();
    }

    private void check(Block block) {
        check(block.getId());
        check(block.getByteBuffer());
    }

    private void check(long id) {
        if ( id > Integer.MAX_VALUE )
            throw new FileException("BlockAccessMem: Bounds exception (id large than an int): " + id);
        if ( !Checking )
            return;
        if ( id < 0 || id >= blocks.size() )
            throw new FileException("BlockAccessMem: "+label+": Bounds exception: " + id + " in [0, "+blocks.size()+")");
    }

    private void check(ByteBuffer bb) {
        if ( !Checking )
            return;
        if ( bb.capacity() != blockSize )
            throw new FileException(format("FileAccessMem: Wrong size block.  Expected=%d : actual=%d", blockSize, bb.capacity()));
        if ( bb.order() != Sys.NetworkOrder )
            throw new FileException("BlockMgrMem: Wrong byte order");
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "Mem:" + label;
    }
}

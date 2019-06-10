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

package org.apache.jena.dboe.base.block;

import static java.lang.String.format;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.dboe.base.file.BlockAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Block manager that maps from the FileAccess layer to a BlockMgr. Add free
 * block management (but we should layer with BlockMgrFreeChain)
 */

final public class BlockMgrFileAccess extends BlockMgrBase {
    private static Logger     log        = LoggerFactory.getLogger(BlockMgrFileAccess.class);

    private final BlockAccess file;
    private boolean           closed     = false;
    // Set on any write operations.
    private boolean           syncNeeded = false;

    // Create via the BlockMgrFactory.
    /* package */BlockMgrFileAccess(BlockAccess blockAccess, int blockSize) {
        super(blockAccess.getLabel(), blockSize);
        file = blockAccess;
    }

    @Override
    protected Block allocate() {
        syncNeeded = true;
        return file.allocate(blockSize);
    }

    @Override
    public Block promote(Block block) {
        return block;
    }

    @Override
    public Block getRead(long id) {
        return getBlock(id, true);
    }

    @Override
    public Block getWrite(long id) {
        return getBlock(id, false);
    }

    private Block getBlock(long id, boolean readOnly) {
        checkNotClosed();
        Block block = file.read(id);
        block.setReadOnly(readOnly);
        return block;
    }

    private void checkNotClosed() {
        if ( closed )
            throw new InternalErrorException(Lib.className(this)+" : already closed");
    }

    @Override
    public void release(Block block) {
        checkNotClosed();
    }

    @Override
    public void write(Block block) {
        checkNotClosed();
        if ( block.isReadOnly() )
            throw new BlockException("Attempt to write a read-only block ("+block.getId()+")" );
        syncNeeded = true;
        file.write(block);
    }

    @Override
    public void overwrite(Block block) {
        checkNotClosed();
        syncNeeded = true;
        file.overwrite(block);
    }

    @Override
    public void free(Block block) {
        checkNotClosed();
        // syncNeeded = true;
        // We do nothing about free blocks currently.
    }

    @Override
    public boolean valid(int id) {
        checkNotClosed();
        return file.valid(id);
    }

    @Override
    public void sync() {
        checkNotClosed();
        if ( syncNeeded )
            file.sync();
        else
            syncNeeded = true;
        syncNeeded = false;
    }

    @Override
    public void syncForce() {
        checkNotClosed();
        file.sync();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        closed = true;
        file.close();
    }

    @Override
    public boolean isEmpty() {
        checkNotClosed();
        return file.isEmpty();
    }

    @Override
    public long allocLimit() {
        checkNotClosed();
        return file.allocBoundary();
    }

    @Override
    public void resetAlloc(long boundary) {
        checkNotClosed();
        file.resetAllocBoundary(boundary);
    }

    @Override
    public String toString() {
        return format("BlockMgrFileAccess[%d bytes]:%s", blockSize, file);
    }

    @Override
    protected Logger log() {
        return log;
    }
}

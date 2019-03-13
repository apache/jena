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

package org.apache.jena.dboe.base.page;

import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.block.Block;
import org.apache.jena.dboe.base.block.BlockMgr;
import org.apache.jena.dboe.base.block.BlockType;

/** Engine that wraps from blocks to typed pages. */

public class PageBlockMgr<T extends Page> implements Closeable
{
    protected final BlockMgr blockMgr;
    protected BlockConverter<T> pageFactory;

    protected PageBlockMgr(BlockConverter<T> pageFactory, BlockMgr blockMgr) {
        this.pageFactory = pageFactory;
        this.blockMgr = blockMgr;
    }

    // Sometimes, the subclass must pass null to the constructor then call this.
    protected void setConverter(BlockConverter<T> pageFactory) { this.pageFactory = pageFactory; }

    public BlockMgr getBlockMgr()   { return blockMgr; }

    public long allocLimit()        { return blockMgr.allocLimit(); }

    public void resetAlloc(long boundary) {
        blockMgr.resetAlloc(boundary);
    }

    /** Allocate a new thing */
    public T create(BlockType bType) {
        Block block = blockMgr.allocate(-1);
        block.setModified(true);
        T page = pageFactory.createFromBlock(block, bType);
        return page;
    }

    /**
     * Fetch a block for reading.
     * @param id Block to fetch
     */
    public T getRead(int id) {
        return getRead$(id);
    }

    /**
     * Fetch a block for reading.
     *
     * @param id    Block to fetch
     * @param referencingId
     *            Id of block referring to this one.
     *            For example, a parent in a tree.
     *            May be negative for "none" or "meaningless".
     */
    public T getRead(int id, int referencingId) {
        return getRead$(id);
    }

    /**
     * Fetch a block for writing.
     * @param id Block to fetch
     */
    public T getWrite(int id) {
        return getWrite$(id);
    }

    /**
     * Fetch a block for writing.
     *
     * @param id  Block to fetch
     * @param referencingId
     *            Id of block referring to this one.
     *            For example, a parent in a tree.
     *            May be -1 for "none" or "meaningless".
     */
    public T getWrite(int id, int referencingId) {
        return getWrite$(id);
    }

    // ---- The read and write worker operations.

    final protected T getRead$(int id) {
        Block block = blockMgr.getRead(id);
        // Blocks from the BlockMgrCache may be write-dirty so this test
        // is wrong in that situation.  It is better to use the block as-is
        // otherwise we'd have two blocks of a given id with different flags
        // that confuse the block caching.
//        if ( block.isModified() ) {
//            System.err.println("getRead - isModified - "+blockMgr.getLabel()+"["+id+"]");
////            block = new Block(block.getId(), block.getByteBuffer());
////            block.setModified(false);
//            // Debug.
//            //blockMgr.getRead(id);
//        }
        T page = pageFactory.fromBlock(block);
        return page;
    }

    final protected T getWrite$(int id) {
        Block block = blockMgr.getWrite(id);
        block.setReadOnly(false);
        T page = pageFactory.fromBlock(block);
        return page;
    }

    // ----

    public void put(T page) {
        write(page);
        release(page);
    }

    public void write(T page) {
        Block blk = pageFactory.toBlock(page);
        blockMgr.write(blk);
    }

    public void release(Page page) {
        Block block = page.getBackingBlock();
        blockMgr.release(block);
    }

    private void warn(String string) {
        Log.warn(this, string);
    }

    public void free(Page page) {
        Block block = page.getBackingBlock();
        blockMgr.free(block);
    }

    /** Promote a page to be writable in-place (block id does not change, hence page does not change id). */
    public void promoteInPlace(Page page) {
        Block block = page.getBackingBlock();
        block.getByteBuffer().rewind();

        Block block2 = blockMgr.promote(block);
        block2.setReadOnly(false);
        if ( block2.getId() != block.getId() )
            throw new InternalErrorException("Block id changed");
        if ( block2 == block )
            return;
        // Change - reset Block in page.
        // The details should not have changed.
        // page.reset(block2);
    }

    /** Promote a page - return 'true' if the block changed (.reset()) will have been called */
    public boolean promoteDuplicate(Page page) {
        Block block = page.getBackingBlock();
        block.getByteBuffer().rewind();

        Block block2 =  blockMgr.allocate(-1);
        block2.getByteBuffer().put(block.getByteBuffer());
        block2.getByteBuffer().rewind();
        block2.setReadOnly(false);

        if ( block2 == block )
            return false;
        // Change - reset Block in page.
        page.reset(block2);
        return true;
    }

    public boolean valid(int id) {
        return blockMgr.valid(id);
    }

    public void dump() {
        for ( int idx = 0; valid(idx) ; idx++ ) {
            T page = getRead(idx, -1);
            System.out.println(page);
            release(page);
        }
    }

    /** Signal the start of an update operation */
    public void startUpdate()       { blockMgr.beginUpdate(); }

    /** Signal the completion of an update operation */
    public void finishUpdate()      { blockMgr.endUpdate(); }

    /** Signal the start of an read operation */
    public void startRead()         { blockMgr.beginRead(); }

    /** Signal the completion of an read operation */
    public void finishRead()        { blockMgr.endRead(); }

    @Override
    public void close()             { blockMgr.close(); }
}

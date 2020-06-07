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
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.dboe.base.block.Block;
import org.apache.jena.dboe.base.block.BlockException;
import org.apache.jena.dboe.sys.FileLib;
import org.apache.jena.dboe.sys.Sys;
import org.slf4j.Logger;

/** Support for a disk file backed FileAccess */
public abstract class BlockAccessBase implements BlockAccess {
    protected final int          blockSize;
    protected FileChannel        file;
    protected final String       filename;

    protected final String     label;
    // Does this need to be tread safe?
    // Only changes in a write transaction

    // Don't overload use of this!
    protected final AtomicLong seq;
    protected long             numFileBlocks = -1;

    public BlockAccessBase(String filename, int blockSize) {
        this.filename = filename;
        this.file = FileLib.openManaged(filename);
        this.blockSize = blockSize;
        this.label = label(filename);
        // This is not related to used file length in mapped mode.
        long filesize = FileLib.size(file);
        long longBlockSize = blockSize;

        numFileBlocks = filesize / longBlockSize; // This is not related to
                                                   // used file length in mapped
                                                   // mode.
        seq = new AtomicLong(numFileBlocks);

        if ( numFileBlocks > Integer.MAX_VALUE )
            getLog().warn(format("File size (%d) exceeds tested block number limits (%d)", filesize, blockSize));

        if ( filesize % longBlockSize != 0 )
            throw new BlockException(format("File size (%d) not a multiple of blocksize (%d)", filesize, blockSize));
    }

    /** Find path component, with extension */
    private static String label(String filename) {
        int j = filename.lastIndexOf('/');
        if ( j < 0 )
            j = filename.lastIndexOf('\\');
        String fn = (j >= 0) ? filename.substring(j + 1) : filename;
        return fn;
    }

    protected abstract Logger getLog();

    @Override
    final public boolean isEmpty() {
        return numFileBlocks <= 0;
    }

    final protected void writeNotification(Block block) {}

    final protected void overwriteNotification(Block block) {
        // Write at end => extend
        if ( block.getId() >= numFileBlocks ) {
            numFileBlocks = block.getId() + 1;
            seq.set(numFileBlocks);
        }
    }

    final protected int allocateId() {
        checkIfClosed();
        long z = seq.getAndIncrement();
        int id = (int)z;
        if ( id < 0 ) {
            String msg = format("%s : Block id has gone negative: %d (long = %d)", label, id, z);
            throw new InternalErrorException(msg);
        }
        // TODO Fix this when proper free block management is introduced.
        numFileBlocks++;
        return id;
    }

    @Override
    final public long allocBoundary() {
        checkIfClosed();
        return seq.get();
        // Underlying area is untouched.
    }

    @Override
    final public void resetAllocBoundary(long boundary) {
        checkIfClosed();
        seq.set(boundary);
        _resetAllocBoundary(boundary);
    }

    protected abstract void _resetAllocBoundary(long boundary);

    @Override
    final synchronized public boolean valid(long id) {
        if ( id >= numFileBlocks )
            return false;
        if ( id < 0 )
            return false;
        return true;
    }

    final protected void check(long id) {
        if ( id > Integer.MAX_VALUE )
            throw new BlockException(format("BlockAccessBase: Id (%d) too large", id));

        // Access to numFileBlocks not synchronized - it's only a check
        if ( id < 0 || id >= numFileBlocks ) {
            // Do it properly!
            synchronized (this) {
                if ( id < 0 || id >= numFileBlocks )
                    throw new BlockException(format("BlockAccessBase: Bounds exception: %s: (%d,%d)", filename, id, numFileBlocks));
            }
        }
    }

    final protected void check(Block block) {
        check(block.getId());
        ByteBuffer bb = block.getByteBuffer();
        if ( bb.capacity() != blockSize )
            throw new BlockException(format("BlockMgrFile: Wrong size block.  Expected=%d : actual=%d", blockSize, bb.capacity()));
        if ( bb.order() != Sys.NetworkOrder )
            throw new BlockException("BlockMgrFile: Wrong byte order");
    }

    protected void force() {
        FileLib.sync(file);
    }

    // @Override
    final public boolean isClosed() {
        return file == null;
    }

    protected final void checkIfClosed() {
        if ( isClosed() )
            getLog().error("File has been closed");
    }

    protected abstract void _close();

    @Override
    final public void close() {
        _close();
        FileLib.close(file);
        file = null;
    }

    @Override
    public String getLabel() {
        return label;
    }
}

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

package org.apache.jena.dboe.transaction.txn;

import java.nio.ByteBuffer;

import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.atlas.lib.Sync;
import org.apache.jena.dboe.base.file.BufferChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Helper class for the manage the persistent state of a transactional.
 * The persistent state is assumed to be a fixed size, or at least
 * of known maximum size.
 * May not be suitable for all transactional component implementations.
 */

public abstract class StateMgrBase implements Sync, Closeable {
    /* Compare to TransBlob which is a similar idea but where it is a
     * component in the transaction component group. That can lead to
     * the wrong control, or at least unclear, of the writing during
     * the transaction commit cycle.
     */

    private static Logger log = LoggerFactory.getLogger(StateMgrBase.class);

    private final BufferChannel storage;
    // One ByteBuffer that is reused where possible.
    // This is short-term usage
    // *  get disk state-> deserialize
    // *  serialize->set disk state
    // on a single thread (the transaction writer).
    private ByteBuffer bb;
    // Is the internal state out of sync with the disk state?
    private boolean dirty = false;

    protected StateMgrBase(BufferChannel storage, int sizeBytes) {
        bb = ByteBuffer.allocate(sizeBytes);
        this.storage = storage;
    }

    /**
     * After the default initial state is known, call this, for example, at the
     * end of the constructor.  If no on-disk state is found, a clean copy is written.
     */
    protected void init() {
        if ( ! storage.isEmpty() )
            readState();
        else
            writeState();
    }

    /* Serialize the necessary state into a ByteBuffer.
     * The argument ByteBuffer can be used or a new one returned
     * if it is the wrong size (e.g. too small).  The returned one will become the
     * recycled ByteBuffer. The returned ByteBuffer should have posn/limit
     * delimiting the necessary space to write.
     */
    protected abstract ByteBuffer serialize(ByteBuffer bytes);

    /*
     * Deserialize the persistent state from the ByteBuffer (delimited by posn/limit).
     * The byte buffer will be the recycled one from last time.
     * Most
     */
    protected abstract void deserialize(ByteBuffer bytes);

    /** Note that the in-memory state is not known to be the same
     * as the on-disk state.
     */
    protected void setDirtyFlag() {
        dirty = true;
    }

    /**
     * Event call for state writing. Called after successful
     * writing of the state.
     */
    protected abstract void writeStateEvent();

    /**
     * Event call for state reading. Called after successful
     * deserializing of the state.
     */
    protected abstract void readStateEvent();

    /** Note that the in-memory state is the same
     * as the on-disk state, or at least the on-disk state is
     * acceptable for restart at any time.
     */
    protected void clearDirtyFlag() {
        dirty = false;
    }

    /** Low level control - for example, used for cloning setup.
     * Use with care.
     */
    public BufferChannel getBufferChannel() {
        return storage;
    }

    /** Return the serialized state using the internal ByteBuffer
     * Typically called by "prepare" for the bytes to write to the journal.
     * Calls {@link #serialize}.
     * This method does not perform an external I/O.
     */
    public ByteBuffer getState() {
        bb.rewind();
        serialize(bb);
        return bb;
    }

    /** Set the in-memory state from a ByteBuffer, for example, from journal recovery.
     * This method does not perform an external I/O.
     * Call "writeState" to put the memory state as the disk state.
     */
    public void setState(ByteBuffer buff) {
        buff.rewind();
        deserialize(buff);
        setDirtyFlag();
    }

    //public BufferChannel getChannel() { return storage; }

    /** The write process : serialize, write, sync,
     * After this, the bytes definitely are on disk, not in some OS cache
     */
    public void writeState() {
        bb.rewind();
        ByteBuffer bb1 = serialize(bb);
        if ( bb1 != null )
            bb = bb1;
        bb.rewind();
        int len = storage.write(bb, 0);
        storage.sync();
        clearDirtyFlag();
        writeStateEvent();
    }

    /** The read process : get all bytes on disk, deserialize */
    public void readState() {
        bb.rewind();
        int len = storage.read(bb, 0);
        bb.rewind();
        deserialize(bb);
        readStateEvent();
        clearDirtyFlag();
    }

    @Override
    public void sync() {
        if ( dirty ) {
            writeState();
            clearDirtyFlag();
        }
    }

    @Override
    public void close() {
        storage.close();
    }
}

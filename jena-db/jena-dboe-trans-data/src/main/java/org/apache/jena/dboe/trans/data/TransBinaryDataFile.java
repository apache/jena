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

package org.apache.jena.dboe.trans.data;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.dboe.base.file.BinaryDataFile;
import org.apache.jena.dboe.base.file.BufferChannel;
import org.apache.jena.dboe.transaction.txn.*;
import org.apache.jena.query.ReadWrite;

/** Transactional {@link BinaryDataFile}.
 *  A binary file that is append-only and allows only one writer at a time.
 *  All readers see the file up to the last commit point at the time
 *  they started.  The sole writer sees more of the file.
 */

public class TransBinaryDataFile extends TransactionalComponentLifecycle<TransBinaryDataFile.TxnBinFile>
    implements BinaryDataFile {

    /*
     * The file is written to as we go along so abort requires some action. We
     * can't recover from just the file, without any redo or undo recovery
     * action.
     */

    private final FileState stateMgr;

    // The current committed position and the limit as seen by readers.
    // This is also the abort point.
    private final AtomicLong committedLength;

    // The per thread runtime state
    static class TxnBinFile {
        final long length;

        TxnBinFile(long length) {
            this.length = length;
        }
    }

    // Prepare record
    static class FileState extends StateMgrData {
        FileState(BufferChannel bufferChannel, long length, long position) {
            super(bufferChannel, length, position);
        }
        private static int idxLength = 0;
        long length()               { return get(idxLength); }
        void length(long len)       { set(idxLength, len); }
    }

    private final BinaryDataFile binFile;

    /** Create a transactional BinaryDataFile over a base implementation.
     *  The base file must provide thread-safe operation.
     */
    public TransBinaryDataFile(BinaryDataFile binFile, ComponentId cid, BufferChannel bufferChannel) {
        super(cid);
        stateMgr = new FileState(bufferChannel, 0L, 0L);
        this.binFile = binFile;
        if ( ! binFile.isOpen() )
            binFile.open();
        // Internal state may be updated by recovery. Start by
        // setting to the "clean start" settings.
        committedLength = new AtomicLong(binFile.length());
    }

    private boolean recoveryAction = false;

    @Override
    public void startRecovery() {
        recoveryAction = false;
    }

    @Override
    public void recover(ByteBuffer ref) {
        stateMgr.setState(ref);
        committedLength.set(stateMgr.length());
        recoveryAction = true;
    }

    @Override
    public void finishRecovery() {
        if ( recoveryAction ) {
            long length = committedLength.get();
            binFile.truncate(length);
            binFile.sync();
            committedLength.set(length);
        }
    }

    @Override
    public void cleanStart() { }

    @Override
    protected TxnBinFile _begin(ReadWrite readWrite, TxnId txnId) {
        // Atomic read across the two because it's called from within
        // TransactionCoordinator.begin$ where there is a lock.
        return createState();
    }

    private TxnBinFile createState() {
        long xLength = committedLength.get();
        return new TxnBinFile(xLength);
    }

    @Override
    protected TxnBinFile _promote(TxnId txnId, TxnBinFile state) {
        return createState();
    }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, TxnBinFile state) {
        // Force to disk but do not set the on disk state to record that.
        binFile.sync();
        stateMgr.length(binFile.length());
        return stateMgr.getState();
    }

    @Override
    protected void _commit(TxnId txnId, TxnBinFile state) {
        if ( isWriteTxn() ) {
            // Force to disk happens in _commitPrepare
            stateMgr.writeState();
            // Move visible commit point forward (not strictly necessary - transaction is ending.
            committedLength.set(binFile.length());
        }
    }

    @Override
    protected void _commitEnd(TxnId txnId, TxnBinFile state) {
    }

    @Override
    protected void _abort(TxnId txnId, TxnBinFile state) {
        if ( isWriteTxn() ) {
            binFile.truncate(committedLength.get());
            binFile.sync();
        }
    }

    @Override
    protected void _complete(TxnId txnId, TxnBinFile state) {}

    @Override
    protected void _shutdown() {}

    private void checkBoundsReader(long requestedPoint, TxnBinFile state) { }

    @Override
    public void open() {
        if ( ! binFile.isOpen() )
            binFile.open();
    }

    @Override
    public boolean isOpen() {
        return binFile.isOpen();
    }

    @Override
    public int read(long posn, byte[] b, int start, int length) {
        checkTxn();
        if ( isReadTxn() )
            checkRead(posn);
        return binFile.read(posn, b, start, length);
    }

    private void checkRead(long posn) {
        if ( posn > getDataState().length )
            IO.exception("Out of bounds: (limit "+getDataState().length+")"+posn);
    }

    @Override
    public long write(byte[] b, int start, int length) {
        checkWriteTxn();
        return binFile.write(b, start, length);
    }

    /**
     * Truncate only supported for an abort - this transactional version of
     * BinaryDataFile will not truncate to earlier than the committed length.
     */
    @Override
    public void truncate(long size) {
        checkWriteTxn();
        TxnBinFile state = getDataState();
        if ( size < state.length )
            throw new RuntimeIOException("truncate("+size+") to smaller than commited length "+state.length);
        binFile.truncate(size);
    }

    @Override
    public void sync() {
        // No-op in transactional mode.
        checkWriteTxn();
    }

    @Override
    public void close() {
        stateMgr.close();
        binFile.close();
    }

    @Override
    public long length() {
        super.checkTxn();
        if ( isReadTxn() )
            // Reader view.
            return getDataState().length;
        return binFile.length();
    }

    @Override
    public boolean isEmpty() {
        super.checkTxn();
        return binFile.isEmpty();
    }
}


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

import static java.lang.String.format;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.file.BinaryDataFile;
import org.apache.jena.dboe.base.file.BufferChannel;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.StateMgrData;
import org.apache.jena.dboe.transaction.txn.TransactionalComponentLifecycle;
import org.apache.jena.dboe.transaction.txn.TxnId;
import org.apache.jena.query.ReadWrite;

/** Transactional {@link BinaryDataFile}.
 *  A binary file that is append-only and allows only one writer at a time.
 *  All readers see the file up to the last commit point at the time
 *  they started.  The sole writer sees more of the file.
 */

public class TransBinaryDataFile extends TransactionalComponentLifecycle<TransBinaryDataFile.TxnBinFile>
    implements BinaryDataFile {

    /*
     * The file is written to as we go along but we might need to abort
     * and reset to the starting state of a transaction, which is TxnBinFile
     * or set a new prepare/commit intended state which is FileState
     * as a ByteBuffer.
     */
    private final FileState fileState;

    // The current committed position and the limit as seen by readers.
    // This is also the abort point.
    // Global.s
    private final AtomicLong committedLength;

    // The state of the file visible outside the transaction.
    static class TxnBinFile {
        final long length;

        TxnBinFile(long length) {
            this.length = length;
        }
    }

    // This is the state of the file ahead of time at prepare/commit.
    // It is set on prepare.
    // It is only valid on the transaction thread in the transaction lifecycle.
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
        fileState = new FileState(bufferChannel, 0L, 0L);
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
        fileState.setState(ref);
        committedLength.set(fileState.length());
        recoveryAction = true;
    }

    @Override
    public void finishRecovery() {
        if ( recoveryAction ) {
            long length = committedLength.get();
            binFile.truncate(length);
            binFile.sync();
            committedLength.set(binFile.length());
            recoveryAction = false;
        }
    }

    @Override
    public void cleanStart() { }

    @Override
    protected TxnBinFile _begin(ReadWrite readWrite, TxnId txnId) {
        return createState();
    }

    private TxnBinFile createState() {
        long xLength = committedLength.get();
        return new TxnBinFile(xLength);
    }

    @Override
    protected TxnBinFile _promote(TxnId txnId, TxnBinFile txnResetState) {
        // New state object (may be PROMOTE-READ-COMMITTED and not the same as passed in).
        return createState();
    }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, TxnBinFile txnResetState) {
        // Force to disk but do not set the on-disk state to record that.
        binFile.sync();
        fileState.length(binFile.length());
        return fileState.getState();
    }

    @Override
    protected void _commit(TxnId txnId, TxnBinFile txnResetState) {
        if ( isWriteTxn() ) {
            // Force data to disk happens in _commitPrepare
            fileState.writeState();
            committedLength.set(binFile.length());
        }
    }

    @Override
    protected void _commitEnd(TxnId txnId, TxnBinFile state) {
        // No clearup needed.
    }

    @Override
    protected void _abort(TxnId txnId, TxnBinFile txnResetState) {
        if ( isWriteTxn() ) {
            long x = committedLength.get();
            // Internal consistency check.
            // (Abort after commit would trigger the warning.) 
            if ( txnResetState.length != x )
                Log.warn(this, format("Mismatch: state.length = %d,  committedLength = %d", txnResetState.length != x));
            binFile.truncate(x);
            binFile.sync();
        }
    }

    @Override
    protected void _complete(TxnId txnId, TxnBinFile state) {}

    @Override
    protected void _shutdown() {}

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
            IO.exception("Out of bounds: (limit "+getDataState().length+") "+posn);
    }

    @Override
    public long write(byte[] b, int start, int length) {
        checkWriteTxn();
        return binFile.write(b, start, length);
    }

    /**
     * Truncate only supported for an abort - this transactional version of
     * {@link BinaryDataFile} will not truncate to earlier than the committed length.
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
        fileState.close();
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


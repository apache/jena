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

package org.apache.jena.dboe.transaction;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.migrate.L;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.TransactionalComponentLifecycle;
import org.apache.jena.dboe.transaction.txn.TxnId;
import org.apache.jena.query.ReadWrite;

/** A transaction component with an integer supporting MR+SW (=one writer AND many readers).
 */
public class TransInteger extends TransactionalComponentLifecycle<TransInteger.IntegerState> {

    private final AtomicLong value = new AtomicLong(-1712);
    private final String filename;

    /** Per transaction state - and per thread safe because we subclass
     * TransactionalComponentLifecycle
     */
    static class IntegerState {
        long txnValue;
        public IntegerState(long v) { this.txnValue = v; }
    }

    static int counter = 0;

    /** In-memory, non persistent, transactional integer */
    public TransInteger() { this(0L); }

    /** In-memory, non persistent, transactional integer */
    public TransInteger(long v) {
        this(v, ComponentId.allocLocal());
    }

    /** In-memory, non persistent, transactional integer */
    public TransInteger(long v, ComponentId componentId) {
        this(null, componentId);
        value.set(v);
    }

    /** Persistent, transactional integer. The persistent state is held in
     *  filename.  When first initialized, the value is 0L.
     * @param filename  Persistent state
     * @param cid        Component id
     */
    public TransInteger(String filename, ComponentId cid) {
        super(cid);
        this.filename = filename;
        // Set the value now for "fast read" transactions.
        readLocation();
    }

    private void readLocation() {
        if ( filename != null ) {
            if ( ! FileOps.exists(filename) ) {
                value.set(0L);
                writeLocation();
                return;
            }
            long x = read(filename);
            value.set(x);
        }
    }

    private void writeLocation() {
        writeLocation(value.get());
    }

    private void writeLocation(long value) {
        if ( filename != null ) {
            write(filename, value);
        }
    }

    //-- Read/write the value
    // This should really be checksum'ed or other internal check to make sure IO worked.
    private static long read(String filename) {
        try {
            String str = IO.readWholeFileAsUTF8(filename);
            if ( str.endsWith("\n") ) {
                str = str.substring(0, str.length()-1);
            }
            str = str.trim();
            return Long.parseLong(str);
        }
        catch (IOException ex) {
            Log.error(TransInteger.class, "IOException: " + ex.getMessage(), ex);
            IO.exception(ex);
        }
        catch (NumberFormatException ex) {
            Log.error(TransInteger.class, "NumberformatException: " + ex.getMessage());
            throw new InternalErrorException(ex);
        }
        // Not reached.
        return Long.MIN_VALUE;
    }

    private static void write(String filename, long value) {
        try { L.writeStringAsUTF8(filename, Long.toString(value)); }
        catch (IOException ex) {}
        catch (NumberFormatException ex) {}
    }

    private boolean recoveryAction = false;

    @Override
    public void startRecovery() {
        recoveryAction = false;
    }

    @Override
    public void recover(ByteBuffer ref) {
        long x = ref.getLong();
        value.set(x);
        recoveryAction = true;
    }

    @Override
    public void finishRecovery() {
        if ( recoveryAction )
            writeLocation();
        // Leave true as a record.
    }

    @Override
    public void cleanStart() {
        recoveryAction = false;
    }

    /** Set the value, return the old value*/
    public void inc() {
        checkWriteTxn();
        IntegerState ts = getDataState();
        ts.txnValue++;
    }

    /** Set the value, return the old value*/
    public long set(long x) {
        checkWriteTxn();
        IntegerState ts = getDataState();
        long v = ts.txnValue;
        ts.txnValue = x;
        return v;
    }

    /** Return the current value in a transaction. s*/
    public long read() {
        checkTxn();
        return getDataState().txnValue;
    }

    /** Return the current value.
     * If inside a transaction, return the transaction view of the value.
     * If not in a transaction return the state value (effectively
     * a read transaction, optimized by the fact that reading the
     * {@code TransInteger} state is atomic).
     */
    public long get() {
        if ( super.isActiveTxn() )
            return getDataState().txnValue;
        else
            return value.get();
    }

    /** Read the current global state (that is, the last committed value) outside a transaction. */
    public long value() {
        return value.get();
    }

    @Override
    protected IntegerState _begin(ReadWrite readWrite, TxnId txnId) {
        return createState();
    }

    private IntegerState createState() {
        return new IntegerState(value.get());
    }

    @Override
    protected IntegerState _promote(TxnId txnId, IntegerState state) {
        return createState();
    }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, IntegerState state) {
        if ( isReadTxn() )
            return null;
        ByteBuffer x = ByteBuffer.allocate(Long.BYTES);
        x.putLong(state.txnValue);
        return x;
    }

    @Override
    protected void _commit(TxnId txnId, IntegerState state) {
        if ( isReadTxn() )
            return;
        writeLocation(state.txnValue);
    }

    @Override
    protected void _commitEnd(TxnId txnId, IntegerState state) {
        if ( isReadTxn() )
            return;
        value.set(state.txnValue);
    }
    @Override
    protected void _abort(TxnId txnId, IntegerState state) {
        // Nothing
    }

    @Override
    protected void _complete(TxnId txnId, IntegerState state) {
        // Nothing
    }

    @Override
    protected void _shutdown() {
    }

    @Override
    public String toString() {
        return String.valueOf(super.getComponentId());
    }
}


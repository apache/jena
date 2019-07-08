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

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.atlas.lib.ThreadLib;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Details tests of the transaction lifecycle in one JVM
 * including tests beyond the TransactionalComponentLifecycle
 * Tests directly on the TransactionCoordinator.
 */
public class TestTransactionLifecycle2 {
    // org.junit.rules.ExternalResource ?
    protected TransactionCoordinator txnMgr;

    @Before public void setup() {
        Journal jrnl = Journal.create(Location.mem());
        txnMgr = new TransactionCoordinator(jrnl);
        txnMgr.start();
    }

    @After public void clearup() {
        txnMgr.shutdown();
    }

    protected void checkClear() {
        assertEquals(0, txnMgr.countActive());
        assertEquals(0, txnMgr.countBegin()-txnMgr.countFinished());
    }

    @Test public void txn_direct_01() {
        Transaction txn1 = txnMgr.begin(TxnType.READ);
        txn1.end();
        checkClear();
    }

    @Test(expected=TransactionException.class)
    public void txn_direct_02() {
        Transaction txn1 = txnMgr.begin(TxnType.WRITE);
        txn1.end();
        checkClear();
    }

    @Test
    public void txn_direct_03() {
        Transaction txn1 = txnMgr.begin(TxnType.WRITE);
        txn1.commit();
        txn1.end();
        checkClear();
    }

    @Test
    public void txn_direct_04() {
        Transaction txn1 = txnMgr.begin(TxnType.WRITE);
        // This tests the TransactionCoordinator
        // but the TransactiolComponentLifecycle doesn't support multiple
        // transactions per thread (use of ThreadLocals).
        // To do that, the transaction object would be needed in all
        // component API calls.  Doable but intrusive.
        Transaction txn2 = txnMgr.begin(TxnType.READ);
        txn1.commit();
        txn2.end();
        txn1.end();
        checkClear();
    }

    @Test
    public void txn_direct_05() {
        Transaction txn1 = txnMgr.begin(TxnType.WRITE);
        txn1.prepare();
        txn1.commit();
        txn1.end();
        checkClear();
    }

    @Test
    public void txn_direct_06() {
        Transaction txn1 = txnMgr.begin(TxnType.WRITE);
        // txn1.prepare(); Optional.
        txn1.commit();
        txn1.end();
        checkClear();
    }

    @Test
    public void txn_overlap_WW() {
        Transaction txn1 = txnMgr.begin(TxnType.WRITE, false);
        assertNotNull(txn1);

        Transaction txn2 = txnMgr.begin(TxnType.WRITE, false);
        assertNull(txn2);  // Otherwise blocking.

        txn1.commit();
        txn1.end();
        checkClear();
    }

    @Test
    public void txn_overlap_WR() {
        Transaction txn1 = txnMgr.begin(TxnType.WRITE, false);
        assertNotNull(txn1);

        Transaction txn2 = txnMgr.begin(TxnType.READ, false);
        assertNotNull(txn2);

        txn1.commit();
        txn1.end();
        txn2.end();
        checkClear();
    }

    @Test
    public void txn_overlap_RW() {
        Transaction txn1 = txnMgr.begin(TxnType.READ, false);
        assertNotNull(txn1);

        Transaction txn2 = txnMgr.begin(TxnType.WRITE, false);
        assertNotNull(txn2);
        txn1.commit();
        txn1.end();
        txn2.abort();
        txn2.end();
        checkClear();
    }

    @Test
    public void txn_overlap_RR() {
        Transaction txn1 = txnMgr.begin(TxnType.READ, false);
        assertNotNull(txn1);

        Transaction txn2 = txnMgr.begin(TxnType.READ, false);
        assertNotNull(txn2);

        txn1.commit();
        txn1.end();
        txn2.end();
        checkClear();
    }

    @Test
    public void txn_promote_1() {
        Transaction txn1 = txnMgr.begin(TxnType.READ_PROMOTE);
        assertNotNull(txn1);
        boolean b = txn1.promote();
        assertTrue(b);
        assertEquals(ReadWrite.WRITE, txn1.getMode());
        txn1.commit();
        txn1.end();
        checkClear();
    }

    @Test
    public void txn_promote_2() {
        Transaction txn1 = txnMgr.begin(TxnType.WRITE);
        boolean b = txn1.promote();
        assertTrue(b);
        b = txn1.promote();
        assertTrue(b);
        txn1.abort();
        txn1.end();
        checkClear();
    }

    @Test
    public void txn_promote_3() {
        Transaction txn1 = txnMgr.begin(TxnType.READ);
        boolean b = txn1.promote();
        assertFalse(b);
        b = txn1.promote();
        assertFalse(b);
        // Not a writer
        txn1.end();
        checkClear();
    }

    @Test(expected=TransactionException.class)
    public void txn_promote_4() {
        Transaction txn1 = txnMgr.begin(TxnType.READ);
        txn1.end();
        txn1.promote();
    }

    //Not a @Test
    public void txn_promote_deadlock() {
        Transaction txn1 = txnMgr.begin(TxnType.READ);
        Transaction txn2 = txnMgr.begin(TxnType.WRITE);
        // Deadlock.
        // Promotion waits for the writer to decide whether it is commiting or not.
        // This can't be done on one thread.
        boolean b = txn1.promote();
        assertFalse(b);
        txn1.end();
        txn2.commit();
        txn2.end();
        checkClear();
    }

    @Test
    public void txn_promote_thread_writer_1() {
        Transaction txn1 = txnMgr.begin(TxnType.READ_PROMOTE);
        ThreadLib.syncOtherThread(()->{
            Transaction txn2 = txnMgr.begin(TxnType.WRITE);
            txn2.commit();
            txn2.end();
        });

        boolean b = txn1.promote();
        assertFalse(b);
        txn1.end();
        checkClear();
    }

    @Test
    public void txn_promote_thread_writer_2() {
        Transaction txn1 = txnMgr.begin(TxnType.READ_PROMOTE);
        ThreadLib.syncOtherThread(()->{
            Transaction txn2 = txnMgr.begin(TxnType.WRITE);
            txn2.abort();
            txn2.end();
        });

        boolean b = txn1.promote();
        assertTrue(b);
        // Now a writer.
        txn1.commit();
        txn1.end();
        checkClear();
    }

    @Test
    public void txn_promote_thread_writer_3() {
        Transaction txn1 = txnMgr.begin(TxnType.READ_PROMOTE);
        boolean b = txn1.promote();
        assertTrue(b);
        AtomicReference<Transaction> ref = new AtomicReference<>(txn1);
        ThreadLib.syncOtherThread(()->{
            // Should fail.
            Transaction txn2 = txnMgr.begin(TxnType.WRITE, false);
            ref.set(txn2);
        });
        assertNull(ref.get());
        txn1.abort();
        txn1.end();
    }

    @Test
    public void txn_promote_thread_writer_4() {
        Transaction txn1 = txnMgr.begin(TxnType.READ_PROMOTE);
        boolean b = txn1.promote();
        assertTrue(b);
        AtomicReference<Transaction> ref = new AtomicReference<>(txn1);
        ThreadLib.syncOtherThread(()->{
            // Should fail.
            Transaction txn2 = txnMgr.begin(TxnType.WRITE, false);
            ref.set(txn2);
        });
        assertNull(ref.get());
        txn1.abort();
        txn1.end();

        ThreadLib.syncOtherThread(()->{
            // Should suceed
            Transaction txn2 = txnMgr.begin(TxnType.WRITE, false);
            ref.set(txn2);
            txn2.abort();
            txn2.end();
        });
        assertNotNull(ref.get());
        checkClear();
    }

}


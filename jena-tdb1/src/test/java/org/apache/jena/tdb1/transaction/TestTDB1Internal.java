/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.tdb1.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.jupiter.api.Test;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb1.TDB1Factory;
import org.apache.jena.tdb1.sys.StoreConnection;
import org.apache.jena.tdb1.sys.TDBInternal;

/** Tests for lower level details of TDB */
@SuppressWarnings("removal")
public class TestTDB1Internal {

    @Test
    public void basics_1() {
        DatasetGraph dsg = TDB1Factory.createDatasetGraph();
        StoreConnection sConn = TDBInternal.getStoreConnection(dsg);
        assertNotNull(sConn);
    }

    @Test
    public void basics_2() {
        DatasetGraph dsg = TDB1Factory.createDatasetGraph();
        TransactionManager txnmgr = TDBInternal.getTransactionManager(dsg);
        assertNotNull(txnmgr);
    }

    @Test
    public void exclusive_1() {
        DatasetGraph dsg = TDB1Factory.createDatasetGraph();
        TransactionManager txnmgr = TDBInternal.getTransactionManager(dsg);
        checkTxnMgr(txnmgr, 0, 0);

        ReentrantReadWriteLock rwx = (ReentrantReadWriteLock)txnmgr.getExclusivityLock$();
        checkLock(rwx, 0, 0);

        dsg.begin(ReadWrite.WRITE);
        checkLock(rwx, 1, 0);    // Exclusivity reader lock.
        checkTxnMgr(txnmgr, 0, 1);
        dsg.commit();

        checkLock(rwx, 0, 0);    // Exclusivity reader lock.
        checkTxnMgr(txnmgr, 0, 0);

        dsg.end();
        checkLock(rwx, 0, 0);    // Exclusivity reader lock.
        checkTxnMgr(txnmgr, 0, 0);
    }

    @Test
    public void exclusive_2() {
        DatasetGraph dsg = TDB1Factory.createDatasetGraph();
        TransactionManager txnmgr = TDBInternal.getTransactionManager(dsg);
        checkTxnMgr(txnmgr, 0, 0);

        ReentrantReadWriteLock rwx = (ReentrantReadWriteLock)txnmgr.getExclusivityLock$();
        checkLock(rwx, 0, 0);

        dsg.begin(ReadWrite.READ);
        checkLock(rwx, 1, 0);
        checkTxnMgr(txnmgr, 1, 0);
        dsg.end();
        checkLock(rwx, 0, 0);
        checkTxnMgr(txnmgr, 0, 0);
    }

    @Test
    public void exclusive_3() {
        DatasetGraph dsg = TDB1Factory.createDatasetGraph();
        TransactionManager txnmgr = TDBInternal.getTransactionManager(dsg);
        ReentrantReadWriteLock rwx = (ReentrantReadWriteLock)txnmgr.getExclusivityLock$();

        checkLock(rwx, 0, 0);
        txnmgr.startExclusiveMode();
        checkLock(rwx, 0, 1);
        txnmgr.finishExclusiveMode();
        checkLock(rwx, 0, 0);
    }

    @Test
    public void exclusive_4() {
        DatasetGraph dsg = TDB1Factory.createDatasetGraph();
        TransactionManager txnmgr = TDBInternal.getTransactionManager(dsg);
        ReentrantReadWriteLock rwx = (ReentrantReadWriteLock)txnmgr.getExclusivityLock$();

        checkLock(rwx, 0, 0);
        boolean b = txnmgr.tryExclusiveMode();
        assertTrue(b, "Exclusive 1");
        checkLock(rwx, 0, 1);
        txnmgr.finishExclusiveMode();
        checkLock(rwx, 0, 0);
        b = txnmgr.tryExclusiveMode();
        assertTrue(b, "Exclusive 2");
    }

    @Test
    public void exclusive_5() {
        DatasetGraph dsg = TDB1Factory.createDatasetGraph();
        TransactionManager txnmgr = TDBInternal.getTransactionManager(dsg);
        ReentrantReadWriteLock rwx = (ReentrantReadWriteLock)txnmgr.getExclusivityLock$();
        dsg.begin(ReadWrite.READ);
        boolean b = txnmgr.tryExclusiveMode();
        assertFalse(b);
    }

    private static void checkLock(ReentrantReadWriteLock rwx, int expectedR, int expectedW) {
        int r = rwx.getReadHoldCount();
        int w = rwx.getWriteHoldCount();
        assertEquals(expectedR, r, "R");
        assertEquals(expectedW, w, "W");
    }

    private static void checkTxnMgr(TransactionManager txnmgr, int expectedActiveReaders, int expectedActiveWriters) {
        long r = txnmgr.getCountActiveReaders();
        long w = txnmgr.getCountActiveWriters();
        assertEquals(expectedActiveReaders, r, "R-active");
        assertEquals(expectedActiveWriters, w, "W-active");

    }
}

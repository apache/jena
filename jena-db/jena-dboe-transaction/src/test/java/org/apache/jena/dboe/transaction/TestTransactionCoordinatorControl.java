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

import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.system.Txn;
import org.apache.jena.dboe.migrate.L;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.dboe.transaction.txn.TransactionalBase;
import org.apache.jena.query.TxnType;
import org.apache.jena.system.ThreadAction;
import org.apache.jena.system.ThreadTxn;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

public class TestTransactionCoordinatorControl {
    // The problem with these tests is the need for Lib.sleep as a way to ensure
    // async threads run if they can.  And we are sometimes testing for "they can't".
    
    static final long InitValue = 3 ;
    private TransactionCoordinator txnMgr ;
    protected Transactional unit ;
    
    @Before public void init() {
        txnMgr = new TransactionCoordinator(Location.mem()) ;
        unit = new TransactionalBase(txnMgr) ;
        txnMgr.start();
    }

    @After public void after() {
        txnMgr.shutdown();
    }
    
    
    @Test public void txn_coord_disable_writers_1() {
        AtomicInteger counter1 = new AtomicInteger(0) ;
        AtomicInteger counter2 = new AtomicInteger(0) ;

        txnMgr.blockWriters();
        ThreadAction threadTxn1 = ThreadTxn.threadTxnRead(unit, ()->counter1.incrementAndGet()) ;
        threadTxn1.run() ;
        assertEquals(1, counter1.get()) ;
    }

    @Test public void txn_coord_disable_writers_2() {
        txnMgr.blockWriters();
        Transaction txn = L.syncCallThread(()->txnMgr.begin(TxnType.WRITE, false)) ;
        assertNull(txn) ;
        txnMgr.enableWriters();
        Transaction txn1 = L.syncCallThread(()->txnMgr.begin(TxnType.WRITE, false)) ;
        assertNotNull(txn1) ;
    }
    
    @Test public void txn_coord_disable_writers_3() {
        txnMgr.blockWriters();
        Transaction txn = L.syncCallThread(()->txnMgr.begin(TxnType.READ, false)) ;
        assertNotNull(txn) ;
        txnMgr.enableWriters();
        Transaction txn1 = L.syncCallThread(()->txnMgr.begin(TxnType.WRITE, false)) ;
        assertNotNull(txn1) ;
        Transaction txn2 = L.syncCallThread(()->txnMgr.begin(TxnType.READ, false)) ;
        assertNotNull(txn2) ;
    }
    
    @Test(expected=TransactionException.class)
    public void txn_coord_disable_writers_4() {
        txnMgr.blockWriters();
        txnMgr.enableWriters();
        txnMgr.enableWriters();
    }

    @Test
    public void txn_coord_disable_writers_() {
        txnMgr.blockWriters();
        boolean b = txnMgr.tryBlockWriters() ;
        assertFalse(b) ;
        txnMgr.enableWriters();
    }
    
    @Test public void txn_coord_exclusive_1() {
        txnMgr.startExclusiveMode();
        L.syncOtherThread(()->{
            Transaction txn1 = txnMgr.begin(TxnType.WRITE, false) ;
            assertNull(txn1) ;
            Transaction txn2 = txnMgr.begin(TxnType.READ, false) ;
            assertNull(txn2) ;
        }) ;
        
        txnMgr.finishExclusiveMode();
        L.syncOtherThread(()->{
            Transaction txn1 = txnMgr.begin(TxnType.WRITE, false) ;
            assertNotNull(txn1) ;
            Transaction txn2 = txnMgr.begin(TxnType.READ, false) ;
            assertNotNull(txn2) ;
        }) ;
    }
    
    @Test public void txn_coord_exclusive_2() {
        AtomicInteger counter1 = new AtomicInteger(0) ;
        Semaphore finalSema = new Semaphore(0) ;
        ThreadAction ttxn = ThreadTxn.threadTxnWrite(unit, ()->{
            counter1.incrementAndGet() ;
        }) ;
        boolean b = txnMgr.tryExclusiveMode(false);
        assertFalse(b) ;
        assertEquals(0, counter1.get()) ;
        ttxn.run(); // Now run thread
        assertEquals(1, counter1.get()) ;
        Txn.executeWrite(unit, ()->{});
        b = txnMgr.tryExclusiveMode(false);
        assertTrue(b) ;
    }
    
    
}


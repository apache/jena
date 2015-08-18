/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.transaction;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertNotNull ;
import static org.junit.Assert.assertNull ;

import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.query.ReadWrite ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.migrate.L ;
import org.seaborne.dboe.transaction.txn.Transaction ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;

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
    
    
    @Test public void txn_coord_special_1() {
        AtomicInteger counter1 = new AtomicInteger(0) ;
        AtomicInteger counter2 = new AtomicInteger(0) ;

        txnMgr.disableWriters() ;
        ThreadTxn threadTxn1 = Txn.threadTxnRead(unit, ()->counter1.incrementAndGet()) ;
        threadTxn1.run() ;
        assertEquals(1, counter1.get()) ;
    }

    @Test public void txn_coord_special_2() {
        txnMgr.disableWriters() ;
        Transaction txn = L.syncCallThread(()->txnMgr.begin(ReadWrite.WRITE, false)) ;
        assertNull(txn) ;
        txnMgr.enableWriters();
        Transaction txn1 = L.syncCallThread(()->txnMgr.begin(ReadWrite.WRITE, false)) ;
        assertNotNull(txn1) ;
    }
    
    @Test public void txn_coord_special_3() {
        txnMgr.disableWriters() ;
        Transaction txn = L.syncCallThread(()->txnMgr.begin(ReadWrite.READ, false)) ;
        assertNotNull(txn) ;
        txnMgr.enableWriters();
        Transaction txn1 = L.syncCallThread(()->txnMgr.begin(ReadWrite.WRITE, false)) ;
        assertNotNull(txn1) ;
        Transaction txn2 = L.syncCallThread(()->txnMgr.begin(ReadWrite.READ, false)) ;
        assertNotNull(txn2) ;
    }
    

    @Test public void txn_coord_special_4() {
        AtomicInteger counter1 = new AtomicInteger(0) ;
        AtomicInteger counter2 = new AtomicInteger(0) ;

        txnMgr.beginExclusive();
        L.syncOtherThread(()->{
            Transaction txn1 = txnMgr.begin(ReadWrite.WRITE, false) ;
            assertNull(txn1) ;
            Transaction txn2 = txnMgr.begin(ReadWrite.READ, false) ;
            assertNull(txn2) ;
        }) ;
        
        txnMgr.endExclusive();
        L.syncOtherThread(()->{
            Transaction txn1 = txnMgr.begin(ReadWrite.WRITE, false) ;
            assertNotNull(txn1) ;
            Transaction txn2 = txnMgr.begin(ReadWrite.READ, false) ;
            assertNotNull(txn2) ;
        }) ;
    }

//        
//        
//        
//        L.async(()->{
//            
//            Txn.executeRead(unit, ()->counter1.incrementAndGet()) ;
//        }) ;
//        
//        
//        L.async(()->Txn.executeWrite(unit, ()->{
//            counter2 .incrementAndGet() ;
//        })) ;
//        
//        L.async(()->Txn.executeRead(unit, ()->{
//            counter1.incrementAndGet() ;
//        })) ;
//        
//        Lib.sleep(100);
//        assertEquals(2, counter1.get()) ;
//        assertEquals(0, counter2.get()) ;
//        
//        txnMgr.enableWriters();
//        Lib.sleep(100);
//        assertEquals(1, counter2.get()) ;
//    }
//    
//    @Test public void txn_coord_special_2() {
//        AtomicInteger counter1 = new AtomicInteger(0) ;
//        AtomicInteger counter2 = new AtomicInteger(0) ;
//        txnMgr.beginExclusive(); 
//        L.async(()->Txn.executeWrite(unit, ()->counter1.incrementAndGet())) ;
//        L.async(()->Txn.executeWrite(unit, ()->counter2.incrementAndGet())) ;
//        Lib.sleep(100);
//        assertEquals(0, counter1.get()) ;
//        assertEquals(0, counter2.get()) ;
//        txnMgr.endExclusive();
//        Lib.sleep(10);
//        assertEquals(1, counter1.get()) ;
//        assertEquals(1, counter2.get()) ;
//
//    }
}


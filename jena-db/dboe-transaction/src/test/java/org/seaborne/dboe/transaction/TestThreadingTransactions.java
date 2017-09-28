/*
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

import java.util.concurrent.Semaphore ;

import org.apache.jena.query.ReadWrite ;
import org.junit.After ;
import org.junit.Assert ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.jenax.Txn ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;

public class TestThreadingTransactions {
    static final long InitValue = 3 ;
    private TransactionalInteger transInt ; 
    
    @Before public void init() {
        TransactionCoordinator coord = new TransactionCoordinator(Location.mem()) ;
        transInt = new TransactionalInteger(coord, InitValue) ;
        coord.start();
    }

    @After public void after() {
        transInt.getTxnMgr().shutdown();
    }

    // Read synchronously in a transaction.
    void readTxn(String label, TransactionalInteger trans, long expected) {
        Txn.executeRead(trans, () -> {
            read(label, trans, expected) ;
        }) ;
    }

    void read(String label, TransactionalInteger trans, long expected) {
        long x = trans.get() ;
        Assert.assertEquals(label, expected, x); 
    }

    ThreadTxn threadRead(String label, TransactionalInteger trans, long expectedValue) {
        return ThreadTxn.threadTxnRead(trans, ()->{
            read(label, trans, expectedValue) ;
        }) ;
    }
    
    @Test public void threadTrans_01() {
        transInt.begin(ReadWrite.READ) ;
        read("[01]", transInt, InitValue) ;
        transInt.end();
    }
    
    @Test public void threadTrans_02() {
        transInt.begin(ReadWrite.READ) ;
        threadRead("[02]", transInt, InitValue).run() ;
        transInt.end();
    }

    @Test public void threadTrans_03() {
        Semaphore semaBefore = new Semaphore(0, true) ;
        Semaphore semaAfter  = new Semaphore(0, true) ;
        ThreadTxn async1 = threadRead("[03/1]", transInt, InitValue);
        ThreadTxn async2 = threadRead("[03/2]", transInt, InitValue);
        
        transInt.begin(ReadWrite.WRITE) ;
        read("[03/3]", transInt, InitValue) ;
        transInt.inc(); 
        read("[03/4]", transInt, InitValue+1) ;
        
        async1.run() ;
       
        threadRead("[03/5]", transInt, InitValue) ;
        
        transInt.commit();
        transInt.end();
        async2.run();
        readTxn("[03/6]", transInt, InitValue+1) ;
    }
    
    @Test public void threadTrans_04() {
        Semaphore semaBefore1 = new Semaphore(0, true) ;
        Semaphore semaBefore2 = new Semaphore(0, true) ;
        Semaphore semaAfter  = new Semaphore(0, true) ;
        
        ThreadTxn async1 = threadRead("[04/1]", transInt, InitValue);
        ThreadTxn async2 = threadRead("[04/2]", transInt, InitValue);
        ThreadTxn async3 = threadRead("[04/3]", transInt, InitValue);
        
        Txn.executeWrite(transInt, transInt::inc);

        ThreadTxn async4 = threadRead("[04/3]", transInt, InitValue+1);
        async1.run() ;

        Txn.executeWrite(transInt, transInt::inc);  // ++
        async2.run() ;
        async4.run() ;

        Txn.executeWrite(transInt, transInt::inc);  // ++
        async3.run() ;
        
        readTxn("[04/4]", transInt, InitValue+3) ;
    }
}


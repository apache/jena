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

import java.util.concurrent.Semaphore ;

import org.junit.Assert ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

import com.hp.hpl.jena.query.ReadWrite ;

public class TestThreadingTransactions {
    static final long InitValue = 3 ;
    
    private TransactionalInteger transInt ; 
    
    @Before public void init() {
        Journal journal = Journal.create(Location.mem()) ;
        transInt = new TransactionalInteger(journal, InitValue) ;
        assertionFailed = null ;
    }
    
    void testThread(Semaphore sema1, Semaphore sema2) {
        sema1.release();
        sema2.acquireUninterruptibly();
        checkThreadAssert() ;
    }
    
    void checkThreadAssert() {
        if ( assertionFailed != null )
            Assert.fail(assertionFailed) ;
    }
    
    static volatile String assertionFailed = null ; 
    
    // Execute that immediately read/checks the value, Wait for the thread.
    void threadRead(String label, TransactionalInteger trans, long expected) {
        Semaphore testSemaImmediate = new Semaphore(0, true) ;
        new Thread( ()-> {
            trans.begin(ReadWrite.READ) ;
            readRecord(label, trans, expected) ;
            trans.end() ;
            testSemaImmediate.release(1) ;
        }).start() ;
        testSemaImmediate.acquireUninterruptibly();
        checkThreadAssert() ;
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

    void readRecord(String label, TransactionalInteger trans, long expected) {
        if ( ! trans.isInTransaction() ) {
            assertionFailed = label+ ": Not in transaction" ;
        }
        
        long x = trans.get() ;
        if ( expected != x) {
            assertionFailed = label+ ": expected = "+expected+" :: got= "+x ;
            return ;
        }
    }

    // Execute a thread that waits on semaBefore, read/checks the value, then releases semaAfter.
    void threadReadAsync(String label, TransactionalInteger trans, long expectedValue, Semaphore semaBefore, Semaphore semaAfter) {
        new Thread( ()-> {
            trans.begin(ReadWrite.READ) ;
            // Make the test
            if ( assertionFailed == null ) {
                semaBefore.acquireUninterruptibly() ;
                readRecord(label, trans, expectedValue) ;
            }
            trans.end() ;
            semaAfter.release() ;
        }).start() ;
    }
    
    @Test public void threadTrans_01() {
        transInt.begin(ReadWrite.READ) ;
        read("[01]", transInt, InitValue) ;
        transInt.end();
    }
    
    @Test public void threadTrans_02() {
        transInt.begin(ReadWrite.READ) ;
        threadRead("[02]", transInt, InitValue) ;
        transInt.end();
    }

    @Test public void threadTrans_03() {
        Semaphore semaBefore = new Semaphore(0, true) ;
        Semaphore semaAfter  = new Semaphore(0, true) ;
        threadReadAsync("[03/1]", transInt, InitValue, semaBefore, semaAfter);
        threadReadAsync("[03/2]", transInt, InitValue, semaBefore, semaAfter);
        
        transInt.begin(ReadWrite.WRITE) ;
        read("[03/3]", transInt, InitValue) ;
        transInt.inc(); 
        read("[03/4]", transInt, InitValue+1) ;
        
        testThread(semaBefore, semaAfter);   //1
       
        threadRead("[03/5]", transInt, InitValue) ;
        
        transInt.commit();
        transInt.end();
        testThread(semaBefore, semaAfter);   //2
        readTxn("[03/6]", transInt, InitValue+1) ;
    }
    
    @Test public void threadTrans_04() {
        Semaphore semaBefore1 = new Semaphore(0, true) ;
        Semaphore semaBefore2 = new Semaphore(0, true) ;
        Semaphore semaAfter  = new Semaphore(0, true) ;
        
        threadReadAsync("[04/1]", transInt, InitValue, semaBefore1, semaAfter);
        threadReadAsync("[04/2]", transInt, InitValue, semaBefore1, semaAfter);
        threadReadAsync("[04/3]", transInt, InitValue, semaBefore1, semaAfter);
        
        Txn.executeWrite(transInt, transInt::inc);  // ++
        
        // ???
        threadReadAsync("[04/3]", transInt, InitValue+1, semaBefore2, semaAfter);
        
        
        testThread(semaBefore1, semaAfter);   //1

        Txn.executeWrite(transInt, transInt::inc);  // ++
        testThread(semaBefore1, semaAfter);   //2
        testThread(semaBefore2, semaAfter);   //4

        Txn.executeWrite(transInt, transInt::inc);  // ++
        testThread(semaBefore1, semaAfter);   //3
        
        readTxn("[04/4]", transInt, InitValue+3) ;
    }

}


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
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.transaction.txn.Transaction ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.dboe.transaction.txn.TransactionException ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

import com.hp.hpl.jena.query.ReadWrite ;

/**
 * Details tests of the transaction lifecycle in one JVM
 * including tests beyond the TransactionalComponentLifecycle 
 * 
 * Journal independent.
 */
public class TestTransactionLifecycle2 {
    // org.junit.rules.ExternalResource ?
    protected TransactionCoordinator txnMgr ;
//    protected TransInteger counter1 = new TransInteger(0) ; 
//    protected TransInteger counter2 = new TransInteger(0) ;
//    protected TransMonitor monitor  = new TransMonitor() ;
    
    @Before public void setup() {
        Journal jrnl = Journal.create(Location.mem()) ;
        txnMgr = new TransactionCoordinator(jrnl) ;
    }
    
    @After public void clearup() {
        txnMgr.shutdown(); 
    }
    
    protected void checkClear() {
        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(0, txnMgr.countBegin()-txnMgr.countFinished()) ;
    }
    
    @Test public void txn_direct_01() {
        Transaction txn1 = txnMgr.begin(ReadWrite.READ) ;
        txn1.end();
        checkClear() ;
    }
    
    @Test(expected=TransactionException.class)
    public void txn_direct_02() {
        Transaction txn1 = txnMgr.begin(ReadWrite.WRITE) ;
        txn1.end(); 
        checkClear() ;
    }

    @Test
    public void txn_direct_03() {
        Transaction txn1 = txnMgr.begin(ReadWrite.WRITE) ;
        txn1.commit() ;
        // FIXME
        // commit should be implicit end.
        txn1.end() ; 
        checkClear() ;
    }

    @Test
    public void txn_direct_04() {
        Transaction txn1 = txnMgr.begin(ReadWrite.WRITE) ;
        // This tests the TransactionCoordinator
        // but the TransactiolComponentLifecycle doesn't support multiple
        // transactions per thread (use of ThreadLocals).
        // To do that, the transaction object would be needed in all
        // component API calls.  Doable but intrusive.
        Transaction txn2 = txnMgr.begin(ReadWrite.READ) ;
        txn1.commit() ;
        txn2.end() ;
        txn1.end() ; 
        checkClear() ;
    }
    
    
    @Test
    public void txn_overlap_WW() {
        Transaction txn1 = txnMgr.begin(ReadWrite.WRITE, false) ;
        assertNotNull(txn1) ;
        
        Transaction txn2 = txnMgr.begin(ReadWrite.WRITE, false) ;
        assertNull(txn2) ;  // Otherwise blocking.
        
        txn1.commit();
        txn1.end() ;
        checkClear() ;
    }

    @Test
    public void txn_overlap_WR() {
        Transaction txn1 = txnMgr.begin(ReadWrite.WRITE, false) ;
        assertNotNull(txn1) ;
        
        Transaction txn2 = txnMgr.begin(ReadWrite.READ, false) ;
        assertNotNull(txn2) ;
        
        txn1.commit();
        txn1.end() ;
        txn2.end();
        checkClear() ;
    }

    @Test
    public void txn_overlap_RW() {
        Transaction txn1 = txnMgr.begin(ReadWrite.READ, false) ;
        assertNotNull(txn1) ;

        Transaction txn2 = txnMgr.begin(ReadWrite.WRITE, false) ;
        assertNotNull(txn2) ;
        txn1.commit();
        txn1.end() ;
        txn2.end();
        checkClear() ;
    }

    @Test
    public void txn_overlap_RR() {
        Transaction txn1 = txnMgr.begin(ReadWrite.READ, false) ;
        assertNotNull(txn1) ;

        Transaction txn2 = txnMgr.begin(ReadWrite.READ, false) ;
        assertNotNull(txn2) ;
        
        txn1.commit();
        txn1.end() ;
        txn2.end();
        checkClear() ;
    }
}


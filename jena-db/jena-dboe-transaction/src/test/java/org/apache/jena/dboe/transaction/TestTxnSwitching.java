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

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.fail ;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.sys.Txn;
import org.apache.jena.dboe.transaction.txn.*;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.sys.ThreadAction;
import org.apache.jena.sys.ThreadTxn;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

/** Tests of changing the thread state ... carefully */ 
public class TestTxnSwitching {
    TransInteger integer = new TransInteger(100) ;
    Journal jrnl = Journal.create(Location.mem()) ;
    
    //Transactional transactional = TransactionalFactory.create(jrnl, integer) ;
    TransactionalBase transactional ;
    TransactionCoordinator txnMgr = new  TransactionCoordinator(jrnl) ;
    {
        txnMgr.add(integer) ;
        transactional = new TransactionalBase(txnMgr) ;
        txnMgr.start() ;
    }

    
    @Before public void setup() {
    }
    
    @After public void clearup() {
    }
    
    @Test public void txnSwitch_01() {
        long z = integer.value() ;
        transactional.begin(TxnType.WRITE);
        integer.inc(); 
        
        assertEquals(integer.value()+1, integer.get()) ;
        assertEquals(z+1, integer.get()) ;
        
        TransactionCoordinatorState txnState = transactional.detach() ;
        
        assertEquals(integer.value(), integer.get()) ;
        assertEquals(z, integer.get()) ;
        
        transactional.attach(txnState);

        assertEquals(integer.value()+1, integer.get()) ;
        assertEquals(z+1, integer.get()) ;
        
        transactional.commit() ;
        transactional.end() ;
        assertEquals(z+1, integer.get()) ;
        assertEquals(z+1, integer.value()) ;
    }
    
    @Test public void txnSwitch_02() {
        long z = integer.value() ;
        Txn.executeWrite(transactional, ()->integer.inc());
        assertEquals(z+1, integer.value()) ;
        
        
        //Transaction txn = txnMgr.begin(ReadWrite.WRITE) ;
        transactional.begin(ReadWrite.WRITE);
        integer.inc(); 
        assertEquals(z+2, integer.get()) ;
        TransactionCoordinatorState txnState = transactional.detach() ;
        // Can't transactional read.
        try { integer.read() ; fail() ; } catch (TransactionException ex) {}
        
        long z1 = Txn.calculateRead(transactional, ()->integer.get()) ;
        assertEquals(z+1, z1) ;
        transactional.attach(txnState) ;
        integer.inc();
        assertEquals(z+3, integer.get()) ;
        
        ThreadAction threadTxn = ThreadTxn.threadTxnRead(transactional, ()->assertEquals(z+1, integer.get())) ;
        threadTxn.run() ;
        
        transactional.commit() ;
        transactional.end() ;
    }
    
    // As 02 but with Transaction txn = txnMgr.begin(ReadWrite.WRITE) ;
    // and txn calls and integer calls.  Not transactional calls but txnMgr calls.

    @Test public void txnSwitch_03() {
        long z = integer.value() ;
        Txn.executeWrite(transactional, ()->integer.inc());
        assertEquals(z+1, integer.value()) ;
        
        Transaction txn = txnMgr.begin(TxnType.WRITE) ;
        integer.inc(); 
        assertEquals(z+2, integer.get()) ;
        TransactionCoordinatorState txnState = txnMgr.detach(txn) ;
        
        Transaction txnRead = txnMgr.begin(TxnType.READ) ;
        assertEquals(z+1, integer.get()) ;
        txnRead.end() ;
        
        try { integer.read() ; fail() ; } catch (TransactionException ex) {}
        
        txnMgr.attach(txnState);
        
        integer.inc();
        assertEquals(z+3, integer.get()) ;
        
        ThreadTxn.threadTxnRead(transactional, ()->assertEquals(z+1, integer.get())).run();
        txn.commit(); 
        txn.end() ;
    }
    
    // Switch between read and write all on one thread. 
    @Test public void txnSwitch_04() {
        long z = integer.value() ;
        
        transactional.begin(ReadWrite.READ);
        TransactionCoordinatorState txnStateR1 = transactional.detach() ;
        
        ThreadAction t1 = ThreadTxn.threadTxnRead(transactional, ()->assertEquals(z, integer.get() )) ;
        ThreadAction t2 = ThreadTxn.threadTxnRead(transactional, ()->assertEquals(z, integer.get() )) ;

        transactional.begin(ReadWrite.WRITE);
        integer.inc();
        
        TransactionCoordinatorState txnStateW1 = transactional.detach() ;
        
        // Currently, thread has no transaction.
        long z1 = Txn.calculateRead(transactional, ()->integer.get() );
        assertEquals(z, z1) ;
        
        // Back to writer.
        transactional.attach(txnStateW1) ;
        integer.inc();
        TransactionCoordinatorState txnStateW2 = transactional.detach() ;
        
        try { integer.read() ; fail() ; } catch (TransactionException ex) {}
        // To reader.
        transactional.attach(txnStateR1) ;
        assertEquals(z1, integer.read()) ;
        t1.run() ;

        // And the writer again.
        TransactionCoordinatorState txnStateR2 = transactional.detach() ;
        transactional.attach(txnStateW2) ;
        integer.inc();
        transactional.commit(); 
        transactional.end() ;
        
        t2.run() ;
        transactional.attach(txnStateR2) ;
        assertEquals(z1, integer.read()) ;
        transactional.end() ;
    }
    
    // Some error cases.
    @Test(expected=TransactionException.class)
    public void txnSwitch_10() {
        transactional.begin(ReadWrite.READ);
        TransactionCoordinatorState txnState = transactional.detach() ;
        transactional.attach(txnState); 
        transactional.attach(txnState);
    }
    @Test(expected=TransactionException.class)
    public void txnSwitch_11() {
        transactional.begin(ReadWrite.READ);
        TransactionCoordinatorState txnState1 = transactional.detach() ;
        TransactionCoordinatorState txnState2 = transactional.detach() ;
    }

}


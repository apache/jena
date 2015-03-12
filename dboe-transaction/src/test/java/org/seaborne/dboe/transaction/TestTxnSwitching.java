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

import com.hp.hpl.jena.query.ReadWrite ;

import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.transaction.TransInteger.IntegerState ;
import org.seaborne.dboe.transaction.txn.Transaction ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;
import org.seaborne.dboe.transaction.txn.TransactionalComponentLifecycle.ComponentState ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

/** Tests of changing the therad state ... carefully */ 
public class TestTxnSwitching {
    TransInteger integer = new TransInteger(1) ;
    Journal jrnl = Journal.create(Location.mem()) ;
    
    //Transactional transactional = TransactionalFactory.create(jrnl, integer) ;
    Transactional transactional ;
    TransactionCoordinator txnMgr = new  TransactionCoordinator(jrnl) ;
    {
        txnMgr.add(integer) ;
        transactional = new TransactionalBase(txnMgr) ;
    }

    
    @Before public void setup() {
    }
    
    @After public void clearup() {
    }
    
    @Test public void txnSwitch_01() {
        transactional.begin(ReadWrite.WRITE);
        integer.inc(); 
        assertEquals(integer.value(), integer.get()-1) ;
        
        ComponentState<IntegerState> s = integer.getComponentState() ;
        transactional.commit() ;
        transactional.end() ;
    }
    
    @Test public void txnSwitch_02() {
        long x = integer.value() ;
        
        Transaction txn = txnMgr.begin(ReadWrite.WRITE) ;
        integer.inc(); 
        assertEquals(integer.value(), integer.get()-1) ;
        
        
        // TransactionalBase has a thread local.
        // Each TransactionalComponentLifecycle has a state object.
        // The data state is a ptr so needs cloning?
        //   No - restrict the ability to resume. 
        // Only allow resume  
        // Or only allow switch c.f. longjmp.
        
        // txn->
        
        /*
        ComponentState<IntegerState> s = integer.getComponentState() ;
        // Suspend txn.
        txnMgr.suspend(txn) ;
        txn.suspend() ;
        
        // Cast to a special.
        transactional.suspend()
        
        */
    }

}


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

package org.apache.jena.system;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.fail;

import org.junit.Test ;

public class TestTxnThread {

    TxnCounter counter = new TxnCounter(0) ; 
    
    // Tests for thread transactions.
    
    @Test public void txnThread_1() {
        ThreadAction t = ThreadTxn.threadTxnRead(counter, ()->{}) ;
        t.run();
    }
    
    @Test public void txnThread_2() {
        ThreadAction t = ThreadTxn.threadTxnWrite(counter, ()-> fail("")) ;
    }

    @Test(expected=AssertionError.class)
    public void txnThread_3() {
        ThreadAction t = ThreadTxn.threadTxnWrite(counter, ()-> fail("")) ;
        t.run() ;
    }

    @Test public void txnThread_10() {
        long x1 = counter.get() ;  
        ThreadAction t = ThreadTxn.threadTxnWrite(counter, ()->{ counter.inc() ;}) ;
        long x2 = counter.get() ;
        assertEquals("x2", x1, x2) ;
        t.run() ;
        long x3 = counter.get() ;
        assertEquals("x3", x1+1, x3) ;
    }
    
    @Test public void txnThread_11() {
        long x1 = counter.get() ;  
        Txn.executeWrite(counter, ()->{
            counter.inc();
            // Read the "before" state
            ThreadAction t = ThreadTxn.threadTxnRead(counter, ()->{ 
                long z1 = counter.get() ; 
                assertEquals("Thread read", x1, z1) ;
            }) ;
            counter.inc();
            t.run(); 
        }) ;
        long x2 = counter.get() ;
        assertEquals("after", x1+2, x2) ;
    }

    @Test public void txnThread_12() {
        long x1 = counter.get() ;  
        ThreadAction t = ThreadTxn.threadTxnRead(counter, () -> {
            long z1 = counter.get() ;
            assertEquals("Thread", x1, z1) ;
        }) ;
        Txn.executeWrite(counter, ()->counter.inc()) ;
        t.run() ;
        long x2 = counter.get() ;
        assertEquals("after", x1+1, x2) ;
    }

}

 
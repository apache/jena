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

import static org.junit.Assert.* ;

import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.Transactional ;
import org.apache.jena.sparql.core.TransactionalLock ;
import org.junit.Test ;

/** Lifecycle tests - apply tests to a provided Transactional
 *  but modify separate state.   
 */
public class TestTxnLifecycle {
    private AtomicLong counter = new AtomicLong(0) ;

    // Not MR+SW (See TestTxn for fully transactional tests)
    private Transactional trans = TransactionalLock.createMRSW() ;
 
    @Test public void txn_lifecycle_01() {
        Txn.executeRead(trans, ()->{}) ;
    }
    
    @Test public void txn_lifecycle_02() {
        Txn.executeWrite(trans, ()->{}) ;
    }

    @Test public void txn_lifecycle_03() {
        int x = Txn.calculateRead(trans, ()->4) ;
        assertEquals(4,x) ;
    }

    @Test public void txn_lifecycle_04() {
        int x = Txn.calculateWrite(trans, ()->5) ;
        assertEquals(5,x) ;
    }
    
    @Test public void txn_lifecycle_05() {
        int x = Txn.calculateWrite(trans, ()-> {
            // Continues outer transaction.
            return Txn.calculateWrite(trans, ()->56) ;
        });
        assertEquals(56,x) ;
    }
    
    @Test(expected=JenaTransactionException.class)
    public void txn_lifecycle_05a() {
        int x = Txn.calculateRead(trans, ()-> {
            // Does not continue outer transaction.
            return Txn.calculateWrite(trans, ()->56) ;
        });
        assertEquals(56,x) ;
    }
    
    @Test
    public void txn_lifecycle_05b() {
        int x = Txn.calculateWrite(trans, ()-> {
            return Txn.calculateRead(trans, ()->56) ;
        });
        assertEquals(56,x) ;
    }

    
    @Test(expected=ExceptionFromTest.class)
    public void txn_lifecycle_06() {
        int x = Txn.calculateWrite(trans, ()-> {
            Txn.calculateWrite(trans, ()-> {throw new ExceptionFromTest() ; }) ;
            return 45 ;
        });
        fail("Should not be here!") ;
    }
    
    
    @Test public void txn_lifecycle_07() {
        Txn.executeWrite(trans, ()->trans.commit()) ; 
    }
    
    @Test public void txn_lifecycle_08() {
        Txn.executeWrite(trans, ()->trans.abort()) ; 
    }
    
    @Test public void txn_lifecycle_09() {
        Txn.executeRead(trans, ()->trans.commit()) ; 
    }
    @Test public void txn_lifecycle_10() {
        Txn.executeRead(trans, ()->trans.abort()) ; 
    }

    static void async(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start(); 
    }
    
    static void debug(String message) {
        //System.out.println(message) ;
    }
    
    // Tests of isolation. Hard.
    
    // Is this a real test?
//    @Test public void txn_lifecycle_isolation_01() {
//        Semaphore semaStep = new Semaphore(0) ;
//        Semaphore sema = new Semaphore(0) ;
//        
//        counter.set(15) ;
//        long x1 = counter.get() ;
//        
//        Runnable r = ()->{
//            Txn.executeWrite(trans, ()->{
//                debug("Thread 1");
//                // Start step.
//                semaStep.release();
//                
//                debug("Thread 2");
//                //Wait for test 
//                sema.acquireUninterruptibly();
//                
//                debug("Thread 3");
//                // Make a change.
//                counter.incrementAndGet() ;
//                
//                debug("Thread 4");
//                // End step.
//                semaStep.release();
//                
//                debug("Thread 5");
//                // End change.
//                sema.release();
//                
//                debug("Thread 6");
//            });
//        } ;
//        
//        long x2 = counter.get() ;
//        assertEquals("x2", x1, x2) ;
//
//        debug("Main 1");
//
//        // Run!
//        async(r) ; 
//
//        debug("Main 2");
//
//        // Let thread step
//        semaStep.acquireUninterruptibly();
//
//        debug("Main 3");
//        // See what the state is (before change)
//        assertEquals(x1, counter.get()) ;
//        // Allow change
//        sema.release();
//
//        debug("Main 4");
//        // Wait for change.
//        semaStep.acquireUninterruptibly();
//
//        debug("Main 5");
//        // See what the state is (after change)
//        assertEquals(x1+1,counter.get()) ;
//        sema.acquireUninterruptibly();
//
//        // Final state.
//        debug("Main 6");
//        long x3 = counter.get() ;
//        assertEquals(x1+1, x3) ;
//    }
}

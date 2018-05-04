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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Transactional;
import org.junit.Test ;

public class TestTxn {

    TxnCounter counter = new TxnCounter(0) ; 

    @Test public void txn_basic_01() {
        long v1 = counter.get() ;
        assertEquals(0, v1) ;
        Txn.executeRead(counter, () -> {
            assertEquals(0, counter.get()) ;
        }) ;
    }

    @Test public void txn_basic_02() {
        long x = 
            Txn.calculateRead(counter, () -> {
                assertEquals("In R, value()", 0, counter.value()) ;
                assertEquals("In R, get()", 0, counter.get()) ;
                return counter.get() ;
            }) ;
        assertEquals("Outside R", 0, x) ;
    }

    @Test public void txn_basic_03() {
        Txn.executeWrite(counter, counter::inc) ;
        long x = 
            Txn.calculateRead(counter, () -> {
                assertEquals("In R, value()", 1, counter.value()) ;
                assertEquals("In R, get()", 1, counter.get()) ;
                return counter.get() ;
            }) ;
        assertEquals("Outside R", 1, x) ;
    }

    @Test public void txn_basic_05() {
        long x = 
            Txn.calculateWrite(counter, () -> {
                counter.inc() ;
                assertEquals("In W, value()", 0, counter.value()) ;
                assertEquals("In W, get()",1, counter.get()) ;
                return counter.get() ;
            }) ;
        assertEquals("Outside W",1, x) ;
    }

    @Test public void txn_write_01() {
        long x = 
            Txn.calculateWrite(counter, () -> {
                counter.inc() ;
                assertEquals("In W, value()", 0, counter.value()) ;
                assertEquals("In W, get()",1, counter.get()) ;
                long z = counter.get() ;
                counter.commit() ;
                return z ;
            }) ;
        assertEquals("Outside W",1, x) ;
    }

    @Test public void txn_write_02() {
        long x = 
            Txn.calculateWrite(counter, () -> {
                counter.inc() ;
                assertEquals("In W, value()", 0, counter.value()) ;
                assertEquals("In W, get()",1, counter.get()) ;
                long z = counter.get() ;
                counter.abort() ;
                return z ;
            }) ;
        assertEquals("Outside W",1, x) ;
    }

    @Test public void txn_write_03() {
        Txn.executeWrite(counter, () -> {
            counter.inc() ;
            assertEquals("In W, value()", 0, counter.value()) ;
            assertEquals("In W, get()",1, counter.get()) ;
            counter.commit() ;
        }) ;
        assertEquals("Outside W",1, counter.value()) ;
    }

    @Test public void txn_write_04() {
        Txn.executeWrite(counter, () -> {
            counter.inc() ;
            assertEquals("In W, value()", 0, counter.value()) ;
            assertEquals("In W, get()",1, counter.get()) ;
            counter.abort() ;
        }) ;
        assertEquals("Outside W", 0, counter.value()) ;
    }

    @Test public void txn_rw_1() {
        assertEquals(0, counter.get()) ;
        
        Txn.executeWrite(counter, () -> {
            counter.inc() ;
            assertEquals("In W, value()", 0, counter.value()) ;
            assertEquals("In W, get()",1, counter.get()) ;
        }) ;
        
        assertEquals("Direct value()", 1, counter.value()) ;
        assertEquals("Direct get()", 1, counter.get()) ;

        Txn.executeRead(counter, () -> {
            assertEquals("In R, value()", 1, counter.value()) ;
            assertEquals("In R, get()", 1, counter.get()) ;
        }) ;
    }

    @Test public void txn_rw_2() {
        Txn.executeRead(counter, () -> {
            assertEquals("In R, value()", 0, counter.value()) ;
            assertEquals("In R, get()", 0, counter.get()) ;
        }) ;

        Txn.executeWrite(counter, () -> {
            counter.inc() ;
            assertEquals("In W, value()", 0, counter.value()) ;
            assertEquals("In W, get()",1, counter.get()) ;
        }) ;
        
        assertEquals("Direct value()", 1, counter.get()) ;
        assertEquals("Direct get()", 1, counter.get()) ;

        Txn.executeRead(counter, () -> {
            assertEquals("In R, value()", 1, counter.value()) ;
            assertEquals("In R, get()", 1, counter.get()) ;
        }) ;
    }

    @Test public void txn_continue_1() {
        Txn.executeWrite(counter, ()->counter.set(91)) ;
        
        Txn.executeWrite(counter, ()-> {
            assertEquals("In txn, value()", 91, counter.value()) ;
            assertEquals("In txn, read()", 91, counter.read()) ;
            counter.inc(); 
            Txn.executeWrite(counter, ()->{
                assertEquals("In txn, value()", 91, counter.value()) ;
                assertEquals("In txn, get()", 92, counter.read()) ;
                }) ;
            });
        assertEquals(92,counter.value()) ;
    }

    @Test public void txn_continue_2() {
        Txn.executeWrite(counter, ()->counter.set(91)) ;
        
        Txn.executeWrite(counter, ()-> {
            assertEquals("In txn, value()", 91, counter.value()) ;
            assertEquals("In txn, read()", 91, counter.read()) ;
            counter.inc(); 
            Txn.executeWrite(counter, ()->{
                assertEquals("In txn, value()", 91, counter.value()) ;
                assertEquals("In txn, get()", 92, counter.read()) ;
                counter.inc();
                }) ;
            assertEquals("In txn, value()", 91, counter.value()) ;
            assertEquals("In txn, read()", 93, counter.read()) ;
            counter.inc();
            });
        assertEquals(94,counter.value()) ;
    }

    @Test(expected=ExceptionFromTest.class)
    public void txn_exception_01() {
        Txn.executeWrite(counter, counter::inc) ;
        
        Txn.executeWrite(counter, () -> {
            counter.inc() ;
            assertEquals("In W, value()", 1, counter.value()) ;
            assertEquals("In W, get()",2, counter.get()) ;
            throw new ExceptionFromTest() ;
        }) ;
    }

    @Test
    public void txn_exception_02() {
        Txn.executeWrite(counter, ()->counter.set(8)) ;
    
        try {
            Txn.executeWrite(counter, () -> {
                counter.inc();
                assertEquals("In W, value()", 8, counter.value());
                assertEquals("In W, get()", 9, counter.get());
                throw new ExceptionFromTest();
            });
        }
        catch (ExceptionFromTest ex) {}
        assertEquals("After W/abort, get()", 8, counter.get());
    }

    @Test
    public void txn_exception_03() {
        Txn.executeWrite(counter, ()->counter.set(9)) ;

        try {
            Txn.executeRead(counter, () -> {
                assertEquals("In W, value()", 9, counter.value());
                assertEquals("In W, get()", 9, counter.get());
                throw new ExceptionFromTest();
            });
        }
        catch (ExceptionFromTest ex) {}
        assertEquals("After W/abort, get()", 9, counter.get());
    }

    @Test
    public void txn_nested_01() {
        Txn.exec(counter, TxnType.READ, ()->{
            Txn.exec(counter, TxnType.READ, ()->{});
        });
    }

    @Test
    public void txn_nested_02() {
        Txn.exec(counter, TxnType.WRITE, ()->{
            Txn.exec(counter, TxnType.READ, ()->{});
        });
    }

    @Test
    public void txn_nested_03() {
        Txn.exec(counter, TxnType.READ_PROMOTE, ()->{
            Txn.exec(counter, TxnType.READ, ()->{});
        });
    }

    @Test
    public void txn_nested_04() {
        Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, ()->{
            Txn.exec(counter, TxnType.READ, ()->{});
        });
    }

    @Test(expected=JenaTransactionException.class)
    public void txn_nested_05() {
        Txn.exec(counter, TxnType.READ, ()->{
            Txn.exec(counter, TxnType.WRITE, ()->{});
        });
    }

    @Test
    public void txn_nested_06() {
        Txn.exec(counter, TxnType.READ_PROMOTE, ()->{
            boolean b = counter.promote();
            assertTrue(b);
            // Must the same type to nest Txn.
            Txn.exec(counter, TxnType.READ_PROMOTE, ()->{});
        });
    }

    @Test
    public void txn_nested_07() {
        Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, ()->{
            boolean b = counter.promote();
            assertTrue(b);
            // Must the same type to nest Txn.
            Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, ()->{});
        });
    }

    @Test
    public void txn_nested_08() {
        Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, ()->{
            boolean b = counter.promote();
            assertTrue(b);
            assertEquals(ReadWrite.WRITE, counter.transactionMode());
            // Must the same type to nest Txn.
            Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, ()->{});
        });
    }

    @Test
    public void txn_nested_09() {
        Txn.exec(counter, TxnType.READ_PROMOTE, ()->{
            boolean b = counter.promote();
            assertTrue(b);
            assertEquals(ReadWrite.WRITE, counter.transactionMode());
            // Must the same type to nest Txn.
            Txn.exec(counter, TxnType.READ_PROMOTE, ()->{});
        });
    }

    @Test(expected=JenaTransactionException.class)
    public void txn_nested_10() {
        Txn.exec(counter, TxnType.READ_PROMOTE, ()->{
            // Must the same type to nest Txn.
            Txn.exec(counter, TxnType.WRITE, ()->{});
        });
    }

    @Test(expected=JenaTransactionException.class)
    public void txn_nested_11() {
        Txn.exec(counter, TxnType.READ_COMMITTED_PROMOTE, ()->{
            // Must the same type to nest Txn.
            Txn.exec(counter, TxnType.WRITE, ()->{});
        });
    }

    @Test
    public void txn_threaded_01() {
        Txn.exec(counter, TxnType.READ_PROMOTE, ()->{
            ThreadAction a = ThreadTxn.threadTxnWrite(counter, ()->{});
            a.run();
            // Blocks promotion.
            boolean b = counter.promote();
            assertFalse(b);
            assertEquals(ReadWrite.READ, counter.transactionMode());
        });
    }

//    // This would lock up.
//    public void txn_threaded_Not_A_Test() {
//        Txn.exec(counter, TxnType.READ_PROMOTE, ()->{
//            ThreadAction a = ThreadTxn.threadTxnWrite(counter, ()->{});
//            // a is in a W transaction but has not committed or aborted - it's paused.
//            boolean b = counter.promote();
//            // Never reach here.
//            a.run();
//        });
//    }

    @Test
    public void txn_threaded_02() {
        //Transactional tx = DatasetGraphFactory.createTxnMem();
        Transactional tx = counter; 
        
        // Start and enter the W transaction.
        ThreadAction a = ThreadTxn.threadTxnWrite(tx, ()->{});

        // ThreadAction started ... in W transaction.
        Txn.exec(tx, TxnType.READ_PROMOTE, ()->{
            // ... have the thread action complete.
            a.run(); 
            // Blocks promotion.
            boolean b = tx.promote();
            assertFalse(b);
            assertEquals(ReadWrite.READ, tx.transactionMode());
        });
    }
    
    @Test
    public void txn_threaded_03() {
        Transactional tx = DatasetGraphFactory.createTxnMem();
        //Transactional tx = counter; 
        
        // Start and enter the W transaction.
        ThreadAction a = ThreadTxn.threadTxnWriteAbort(tx, ()->{});

        // ThreadAction started ... in W transaction.
        Txn.exec(tx, TxnType.READ_PROMOTE, ()->{
            // ... have the thread action abort..
            a.run(); 
            // Does not block promotion.
            boolean b = tx.promote();
            assertTrue(b);
            assertEquals(ReadWrite.WRITE, tx.transactionMode());
        });
    }

}

 
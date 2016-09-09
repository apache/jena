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

import org.apache.jena.system.Txn ;
import org.apache.jena.system.TxnCounter ;
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
    
    // Tests for thread transactions.


}

 
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

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.JenaTransactionException ;
import org.junit.Test ;

import static org.junit.Assert.* ;

// TestTxn also tests counters.
public class TestCounter {
    private TxnCounter counter = new TxnCounter(0) ;
    
    @Test
    public void counter_lifecycle_1() {
        counter.begin(ReadWrite.WRITE) ;
        counter.inc() ;
        counter.commit();
        counter.end() ;
    }

    @Test
    public void counter_lifecycle_2() {
        counter.begin(ReadWrite.WRITE) ;
        counter.abort() ;
        counter.end() ;
    }

    @Test
    public void counter_lifecycle_3() {
        counter.begin(ReadWrite.READ) ;
        counter.end() ;
    }

    @Test
    public void counter_lifecycle_4() {
        counter.begin(ReadWrite.READ) ;
        counter.commit() ;
        counter.end() ;
    }
    
    @Test
    public void counter_lifecycle_5() {
        counter.begin(ReadWrite.READ) ;
        counter.abort() ;
        counter.end() ;
    }
    
    @Test
    public void counter_lifecycle_6() {
        counter.end() ;
    }

    @Test
    public void counter_01() {
        assertEquals(0, counter.get());
        assertEquals(0, counter.value());
        
        counter.begin(ReadWrite.WRITE) ;
        
        assertEquals(0, counter.get());
        assertEquals(0, counter.value());
        
        counter.inc() ;
        long x = counter.get() ;
        assertEquals(1, counter.get());
        assertEquals(0, counter.value());
        
        counter.commit();
        assertEquals(1, counter.value());
        
        counter.end() ;
        assertEquals(1, counter.value());
    }

    @Test
    public void counter_02() {
        assertEquals(0, counter.get());
        assertEquals(0, counter.value());
        
        counter.begin(ReadWrite.WRITE) ;
        
        assertEquals(0, counter.get());
        assertEquals(0, counter.value());
        
        counter.inc() ;
        long x = counter.get() ;
        assertEquals(1, counter.get());
        assertEquals(0, counter.value());
        
        counter.abort();
        assertEquals(0, counter.value());
        counter.end() ;
        assertEquals(0, counter.value());
    }

    @Test(expected=JenaTransactionException.class)
    public void counter_bad_01() {
        counter.inc() ;
    }

    @Test(expected=JenaTransactionException.class)
    public void counter_bad_02() {
        counter.begin(ReadWrite.WRITE) ;
        counter.end() ;
    }

    @Test(expected=JenaTransactionException.class)
    public void counter_bad_03() {
        counter.begin(ReadWrite.READ) ;
        counter.inc() ;
    }

    @Test(expected=JenaTransactionException.class)
    public void counter_bad_04() {
        counter.begin(ReadWrite.READ) ;
        counter.end() ;
        counter.commit() ;
    }

    @Test(expected=JenaTransactionException.class)
    public void counter_bad_05() {
        counter.begin(ReadWrite.WRITE) ;
        counter.end() ;
        counter.commit() ;
    }
}

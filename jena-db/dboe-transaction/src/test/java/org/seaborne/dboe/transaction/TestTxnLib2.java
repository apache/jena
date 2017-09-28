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

import org.apache.jena.atlas.lib.Pair ;
import org.junit.After ;
import org.junit.Assert ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.jenax.Txn ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;

/** Unusual ways to do things.
 *  Rather than a TransactionalComponent,   
 *  TransactionalInteger 
 */
public class TestTxnLib2 extends Assert {
    // With setup/teardown / not from AbstractTestTxn
    private final long InitValue = 7 ;  
    TransactionalInteger integer ; 
    
    @Before public void setup() {
        TransactionCoordinator coord = new TransactionCoordinator(Location.mem()) ;
        integer = new TransactionalInteger(coord, InitValue) ;
        coord.start() ;
    }
    
    @After public void clearup() {
        integer.shutdown(); 
    }

    @Test public void libTxn_10() {
        Txn.executeWrite(integer, integer::inc) ;
        long x = Txn.calculateRead(integer, integer::get) ;
        assertEquals(InitValue+1, x) ;
    }
    
    @Test public void libTxn_11() {
        Pair<Long, Long> p = Txn.calculateWrite(integer, () -> {
            integer.inc() ;
            return Pair.create(integer.value(), integer.get()) ;
        }) ;
        assertEquals(InitValue,     p.getLeft().longValue()) ;
        assertEquals(InitValue+1,   p.getRight().longValue()) ;
        assertEquals(InitValue+1,   integer.get()) ;
        assertEquals(InitValue+1,   integer.value()) ;
    }
}


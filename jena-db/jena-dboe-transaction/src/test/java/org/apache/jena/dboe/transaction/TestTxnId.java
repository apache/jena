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
import static org.junit.Assert.assertNotEquals ;
import static org.junit.Assert.assertNotNull ;
import static org.junit.Assert.assertNotSame ;

import org.apache.jena.dboe.transaction.txn.TxnId;
import org.apache.jena.dboe.transaction.txn.TxnIdFactory;
import org.apache.jena.dboe.transaction.txn.TxnIdSimple;
import org.junit.Test ;

public class TestTxnId {
    @Test public void txnId_1() {
        TxnId id1 = TxnIdFactory.createSimple() ;
        assertNotNull(id1) ;
    }

    @Test public void txnId_2() {
        TxnId id1 = TxnIdFactory.createSimple() ;
        TxnId id2 = TxnIdFactory.createSimple() ;
        assertNotSame(id1, id2) ;
        assertNotEquals(id1, id2) ;
    }

    @Test public void txnId_3() {
        TxnId id1 = TxnIdFactory.createSimple() ;
        TxnId id2 = TxnIdSimple.create(id1.bytes()) ;
        assertNotSame(id1, id2) ;
        assertEquals(id1, id2) ;
        assertEquals(id1.name(), id2.name()) ;
    }

}


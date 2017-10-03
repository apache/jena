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

import org.apache.jena.query.ReadWrite ;
import org.junit.Test ;

public class TestTransactionCoordinator extends AbstractTestTxn {
    @Test public void txn_coord_read_1() {
        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(0, txnMgr.countFinished()) ;
        assertEquals(0, txnMgr.countBegin()) ;
        assertEquals(0, txnMgr.countBeginRead()) ;
        assertEquals(0, txnMgr.countBeginWrite()) ;
        assertEquals(0, txnMgr.countActiveReaders()) ;
        assertEquals(0, txnMgr.countActiveWriter()) ;

        unit.begin(ReadWrite.READ);

        assertEquals(1, txnMgr.countActive()) ;
        assertEquals(0, txnMgr.countFinished()) ;
        assertEquals(1, txnMgr.countBegin()) ;
        assertEquals(1, txnMgr.countBeginRead()) ;
        assertEquals(0, txnMgr.countBeginWrite()) ;
        
        assertEquals(1, txnMgr.countActiveReaders()) ;
        assertEquals(0, txnMgr.countActiveWriter()) ;

        unit.end() ;

        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(1, txnMgr.countBeginRead()) ;
        assertEquals(0, txnMgr.countBeginWrite()) ;
        assertEquals(1, txnMgr.countFinished()) ;
        assertEquals(1, txnMgr.countBegin()) ;
        assertEquals(0, txnMgr.countActiveReaders()) ;
        assertEquals(0, txnMgr.countActiveWriter()) ;
    }
    
    @Test public void txn_coord_read_2() {
        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(0, txnMgr.countFinished()) ;
        assertEquals(0, txnMgr.countBegin()) ;
        assertEquals(0, txnMgr.countBeginRead()) ;
        assertEquals(0, txnMgr.countBeginWrite()) ;

        unit.begin(ReadWrite.READ);

        assertEquals(1, txnMgr.countActive()) ;
        assertEquals(0, txnMgr.countFinished()) ;
        assertEquals(1, txnMgr.countBegin()) ;
        assertEquals(1, txnMgr.countBeginRead()) ;
        assertEquals(0, txnMgr.countBeginWrite()) ;

        unit.commit() ;
        
        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(1, txnMgr.countBeginRead()) ;
        assertEquals(0, txnMgr.countBeginWrite()) ;
        assertEquals(1, txnMgr.countFinished()) ;
        assertEquals(1, txnMgr.countBegin()) ;

        unit.end() ;

        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(1, txnMgr.countBeginRead()) ;
        assertEquals(0, txnMgr.countBeginWrite()) ;
        assertEquals(1, txnMgr.countFinished()) ;
        assertEquals(1, txnMgr.countBegin()) ;
    }

    @Test public void txn_coord_write_1() {
        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(0, txnMgr.countFinished()) ;
        assertEquals(0, txnMgr.countBegin()) ;
        assertEquals(0, txnMgr.countBeginRead()) ;
        assertEquals(0, txnMgr.countBeginWrite()) ;

        assertEquals(0, txnMgr.countActiveReaders()) ;
        assertEquals(0, txnMgr.countActiveWriter()) ;

        
        unit.begin(ReadWrite.WRITE);
        
        assertEquals(1, txnMgr.countActive()) ;
        assertEquals(0, txnMgr.countFinished()) ;
        assertEquals(1, txnMgr.countBegin()) ;
        assertEquals(0, txnMgr.countBeginRead()) ;
        assertEquals(1, txnMgr.countBeginWrite()) ;
        
        assertEquals(0, txnMgr.countActiveReaders()) ;
        assertEquals(1, txnMgr.countActiveWriter()) ;

        unit.commit() ;

        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(1, txnMgr.countFinished()) ;
        assertEquals(1, txnMgr.countBegin()) ;
        assertEquals(0, txnMgr.countBeginRead()) ;
        assertEquals(1, txnMgr.countBeginWrite()) ;

        assertEquals(0, txnMgr.countActiveReaders()) ;
        assertEquals(0, txnMgr.countActiveWriter()) ;

        unit.end() ;

        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(1, txnMgr.countFinished()) ;
        assertEquals(1, txnMgr.countBegin()) ;
        assertEquals(0, txnMgr.countBeginRead()) ;
        assertEquals(1, txnMgr.countBeginWrite()) ;

        assertEquals(0, txnMgr.countActiveReaders()) ;
        assertEquals(0, txnMgr.countActiveWriter()) ;
    }

    @Test public void txn_coord_write_2() {
        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(0, txnMgr.countFinished()) ;
        assertEquals(0, txnMgr.countBegin()) ;
        assertEquals(0, txnMgr.countBeginRead()) ;
        assertEquals(0, txnMgr.countBeginWrite()) ;
        assertEquals(0, txnMgr.countActiveReaders()) ;
        assertEquals(0, txnMgr.countActiveWriter()) ;
        
        unit.begin(ReadWrite.WRITE);
        
        assertEquals(1, txnMgr.countActive()) ;
        assertEquals(0, txnMgr.countFinished()) ;
        assertEquals(1, txnMgr.countBegin()) ;
        assertEquals(0, txnMgr.countBeginRead()) ;
        assertEquals(1, txnMgr.countBeginWrite()) ;
        
        assertEquals(0, txnMgr.countActiveReaders()) ;
        assertEquals(1, txnMgr.countActiveWriter()) ;
        
        unit.abort() ;

        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(1, txnMgr.countFinished()) ;
        assertEquals(1, txnMgr.countBegin()) ;
        assertEquals(0, txnMgr.countBeginRead()) ;
        assertEquals(1, txnMgr.countBeginWrite()) ;
        
        assertEquals(0, txnMgr.countActiveReaders()) ;
        assertEquals(0, txnMgr.countActiveWriter()) ;
        
        unit.end() ;

        assertEquals(0, txnMgr.countActive()) ;
        assertEquals(1, txnMgr.countFinished()) ;
        assertEquals(1, txnMgr.countBegin()) ;
        assertEquals(0, txnMgr.countBeginRead()) ;
        assertEquals(1, txnMgr.countBeginWrite()) ;

        assertEquals(0, txnMgr.countActiveReaders()) ;
        assertEquals(0, txnMgr.countActiveWriter()) ;
    }
}


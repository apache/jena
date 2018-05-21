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

package org.apache.jena.dboe.trans.bplustree ;

import static org.apache.jena.dboe.index.test.IndexTestLib.add;
import static org.apache.jena.dboe.test.RecordLib.intToRecord;

import java.util.List ;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.dboe.base.block.BlockMgr;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.index.test.AbstractTestRangeIndex;
import org.apache.jena.dboe.sys.SystemIndex;
import org.apache.jena.dboe.test.RecordLib;
import org.apache.jena.dboe.trans.bplustree.BPT;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeFactory;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;

/** Run the tests in default settings for a tree in "non-transactional" mode */ 
public class TestBPlusTreeNonTxn extends AbstractTestRangeIndex {
    // See TestBPTreeModes for parameterised tests for the duplication modes.
    
    // Add tracker or not (usually not)
    // The tracker checking can impose more constraints than are needed
    // giving false negatives.  Iterator aren't tracked (they may not
    // be consumed; don't release pages properly)
    static boolean addTracker = false  ;
    // Panic.
    static boolean addLogger  = false  ;

    static boolean originalNullOut ;
    @BeforeClass
    static public void beforeClass() {
        BPT.CheckingNode = true ;
        originalNullOut = SystemIndex.getNullOut() ;
        SystemIndex.setNullOut(true) ;
    }

    @AfterClass
    static public void afterClass() {
        SystemIndex.setNullOut(originalNullOut) ;
    }
    
    protected void testClearX(int N) {
        int[] keys = new int[N] ; // Slice is 1000.
        for ( int i = 0 ; i < keys.length ; i++ )
            keys[i] = i ;
        BPlusTree rIndex = makeRangeIndex(2, 2) ;
        add(rIndex, keys) ;
        rIndex.dump() ;
        if ( N > 0 )
            assertFalse(rIndex.isEmpty()) ;
        List<Record> x = intToRecord(keys, RecordLib.TestRecordLength) ;
        for ( int i = 0 ; i < keys.length ; i++ ) {
            System.out.println(i+": "+x.get(i)) ;
            rIndex.delete(x.get(i)) ;
        }
        assertTrue(rIndex.isEmpty()) ;
    }
    
    @Override
    protected BPlusTree makeRangeIndex(int order, int minRecords) {
        BPlusTree bpt = BPlusTreeFactory.makeMem(order, minRecords, RecordLib.TestRecordLength, 0) ;
        if ( addLogger ) {
            // Put it in but disable it so that it can be enabled
            // in the middle of a complex operation.
            LogCtl.disable(BlockMgr.class) ;
            bpt = BPlusTreeFactory.addLogging(bpt) ;
        }
        if ( addTracker )
            bpt = BPlusTreeFactory.addTracking(bpt) ;
        bpt.nonTransactional() ;
        return bpt ;
    }
}

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

package org.seaborne.dboe.trans.bplustree ;

import static org.seaborne.dboe.index.test.IndexTestLib.add ;
import static org.seaborne.dboe.test.RecordLib.intToRecord ;

import java.util.List ;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.index.test.AbstractTestRangeIndex ;
import org.seaborne.dboe.sys.SystemIndex ;
import org.seaborne.dboe.test.RecordLib ;

public class TestBPlusTreeNonTxn extends AbstractTestRangeIndex {
    static boolean originalNullOut ;

    @BeforeClass
    static public void beforeClass() {
        BPlusTreeParams.CheckingNode = true ;
         BPlusTreeParams.CheckingTree = false ; // Breaks with block tracking.
        originalNullOut = SystemIndex.getNullOut() ;
        SystemIndex.setNullOut(true) ;
    }

    @AfterClass
    static public void afterClass() {
        SystemIndex.setNullOut(originalNullOut) ;
    }
    
    @Test public void tree_clear_02a()
    { 
        testClearX(19) ;
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
        if ( false ) {
            // Breaks with CheckingTree = true ; 
            // because they are deep reads into the tree.
            BPlusTreeParams.CheckingNode = true ;
            BPlusTreeParams.CheckingTree = false ;
            bpt = BPlusTreeFactory.addTracking(bpt) ;
        }
        bpt.nonTransactional() ;
        return bpt ;
    }
}

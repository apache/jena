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

package org.apache.jena.dboe.trans.bplustree;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.dboe.base.block.BlockMgr;
import org.apache.jena.dboe.index.test.AbstractTestIndex;
import org.apache.jena.dboe.sys.SystemIndex;
import org.apache.jena.dboe.test.RecordLib;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/** Run the tests in default settings for a tree in "non-transactional" mode */
public class TestBPlusTreeIndexNonTxn extends AbstractTestIndex {
    static boolean addTracker = true ;
    // Panic.
    static boolean addLogger  = false ;

    static boolean originalNullOut;
    @BeforeClass
    static public void beforeClass() {
        BPT.CheckingNode = true;
        originalNullOut = SystemIndex.getNullOut();
        SystemIndex.setNullOut(true);
    }

    @AfterClass
    static public void afterClass() {
        SystemIndex.setNullOut(originalNullOut);
    }
    @Override
    protected BPlusTree makeIndex(int order, int minRecords) {
        BPlusTree bpt = BPlusTreeFactory.makeMem(order, minRecords, RecordLib.TestRecordLength, 0);
        if ( addLogger ) {
            // Put it in but disable it so that it can be enabled
            // in the middle of a complex operation.
            LogCtl.disable(BlockMgr.class);
            bpt = BPlusTreeFactory.addLogging(bpt);
        }
        if ( addTracker )
            bpt = BPlusTreeFactory.addTracking(bpt);
        bpt.nonTransactional();
        return bpt;
    }
}

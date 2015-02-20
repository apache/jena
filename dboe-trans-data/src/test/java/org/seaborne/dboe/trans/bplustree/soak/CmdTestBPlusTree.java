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

package org.seaborne.dboe.trans.bplustree.soak;

import org.seaborne.dboe.index.test.IndexTestLib ;
import org.seaborne.dboe.sys.SystemIndex ;
import org.seaborne.dboe.sys.SystemLz ;
import org.seaborne.dboe.trans.bplustree.BPT ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.trans.bplustree.BlockTracker ;

public class CmdTestBPlusTree extends BaseSoakTest
{
    static public void main(String... argv) {
        //argv = new String[] {"5", "20", "1000"} ;
        new CmdTestBPlusTree(argv).mainRun() ;
    }

    protected CmdTestBPlusTree(String[] argv) {
        super(argv) ;
    }

    @Override
    protected void before() {
        SystemIndex.setNullOut(true) ;
        BlockTracker.collectHistory = false ;
        // Forced mode
        if ( true ) {
            BPT.CheckingNode = false ;
            BPT.forcePromoteModes = true ;
            BPT.promoteDuplicateNodes = true ;
            BPT.promoteDuplicateRecords = true ;
        }
        if ( false ) {
            // Transactions.
        }
        
        System.out.printf("    BPT.CheckingNode            = %s\n", BPT.CheckingNode) ;
        System.out.printf("    BPT.forcePromoteModes       = %s\n", BPT.forcePromoteModes) ;
        System.out.printf("    BPT.promoteDuplicateRecords = %s\n", BPT.promoteDuplicateRecords) ;
        System.out.printf("    BPT.promoteDuplicateRecords = %s\n", BPT.promoteDuplicateRecords) ;
    }
    
    @Override
    protected void after() {}
    
    @Override
    protected void runOneTest(int order, int size, int KeySize, int ValueSize, boolean debug) {
        runOneTest(order, size, debug) ;
    }

    @Override
    protected void runOneTest(int order, int size, boolean debug) {
        //System.err.println("runOneTest("+order+","+size+")") ;
        // Tracking??
        BPlusTree bpt = BPlusTreeFactory.makeMem(order, SystemLz.SizeOfInt, 0) ;
        bpt = BPlusTreeFactory.addTracking(bpt) ;
        bpt.nonTransactional();
        bpt.startBatch();
        // Random values but exact size.
        // No iterator tests - quite slow and well tested by the test suite.
        IndexTestLib.randTest(bpt, 10*size, size, false);
    }
}
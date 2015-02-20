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

public class CmdTestBPlusTree extends BaseSoakTest
{
    static public void main(String... argv) {
        new CmdTestBPlusTree(argv).mainRun() ;
    }

    protected CmdTestBPlusTree(String[] argv) {
        super(argv) ;
    }

    @Override
    protected void before() {
        SystemIndex.setNullOut(true) ;
        // Forced mode
        if ( true ) {
            BPT.forcePromoteModes = true ;
            BPT.promoteDuplicateNodes = false ;
            BPT.promoteDuplicateRecords = false ;
        }
        if ( false ) {
            // Transactions.
        }
        
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
        bpt.nonTransactional();
        // Random values but exact size.
        IndexTestLib.randTest(bpt, 10*size, size);
    }
}
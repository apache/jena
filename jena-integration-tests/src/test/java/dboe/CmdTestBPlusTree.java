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

package dboe;

import org.apache.jena.dboe.base.file.BlockAccessMem;
import org.apache.jena.dboe.index.testlib.IndexTestLib;
import org.apache.jena.dboe.sys.SysDB;
import org.apache.jena.dboe.sys.SystemIndex;
import org.apache.jena.dboe.trans.bplustree.BPT;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeFactory;
import org.apache.jena.dboe.trans.bplustree.BlockTracker;

public class CmdTestBPlusTree extends BaseSoakTest
{
    static public void main(String... argv) {
        //argv = new String[] {"50", "150", "1000000"} ;
//        System.setProperty("bpt:checking", "false") ;
//        System.setProperty("bpt:duplication", "true") ;
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
            BlockAccessMem.SafeMode = true ;
            BPT.CheckingNode = trueOrFalse("bpt:checking", false) ;
            boolean duplication = trueOrFalse("bpt:duplication", false) ;
            BPT.forcePromoteModes = true ;
            BPT.promoteDuplicateNodes = duplication ;
            BPT.promoteDuplicateRecords = duplication ;
        }
        if ( false ) {
            // Transactions.
        }
        
        System.out.printf("    BPT.CheckingNode            = %s\n", BPT.CheckingNode) ;
        System.out.printf("    BPT.forcePromoteModes       = %s\n", BPT.forcePromoteModes) ;
        System.out.printf("    BPT.promoteDuplicateNodes   = %s\n", BPT.promoteDuplicateRecords) ;
        System.out.printf("    BPT.promoteDuplicateRecords = %s\n", BPT.promoteDuplicateRecords) ;
    }
    
    private boolean trueOrFalse(String property, boolean dftValue) {
        String s = System.getProperty(property) ;
        if ( s == null )
            return dftValue ;
        if ( s.equalsIgnoreCase("true") || s.equalsIgnoreCase("T") || s.equalsIgnoreCase("1") )
            return true ;
        if ( s.equalsIgnoreCase("false") || s.equalsIgnoreCase("F") || s.equalsIgnoreCase("0") )
            return false ;
        System.err.println("Not recognized: "+property+"="+s);
        return dftValue ;
    }

    @Override
    protected void after() {}
    
    @Override
    protected void runOneTest(int testCount, int order, int size, boolean debug) {
        runOneTest(testCount, order, size) ;
    }

    @Override
    protected void runOneTest(int testCount, int order, int size) {
//        //System.err.println("runOneTest("+order+","+size+")") ;
        BPlusTree bpt = BPlusTreeFactory.makeMem(order, SysDB.SizeOfInt, 0) ;
        bpt = BPlusTreeFactory.addTracking(bpt) ;
        bpt.nonTransactional() ;
        IndexTestLib.randTest(bpt, 5*size, size, true);
        bpt.close() ;
        
        // Transaction.
//        BPlusTree bpt = BPlusTreeFactory.makeMem(order, SystemLz.SizeOfInt, 0) ;
//        Journal journal = Journal.create(Location.mem()) ;
//        Transactional holder = TransactionalFactory.create(journal, bpt) ;
//        holder.begin(ReadWrite.WRITE);
//        IndexTestLib.randTest(bpt, 5*size, size, true);
//        holder.commit() ;
//        holder.end() ;
    }
}
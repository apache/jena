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

package dev;

import static java.util.stream.IntStream.range ;
import static org.seaborne.dboe.test.RecordLib.intToRecord ;
import static org.seaborne.dboe.test.RecordLib.r ;

import java.io.PrintStream ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.logging.LogCtl ;
import org.seaborne.dboe.base.block.BlockMgr ;
import org.seaborne.dboe.base.block.BlockMgrLogger ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.sys.SystemIndex ;
import org.seaborne.dboe.test.RecordLib ;
import org.seaborne.dboe.trans.bplustree.BPT ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.trans.bplustree.BlockMgrTrackerWriteLifecycle ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

public class MainIndexTest {
    // Extract and debug tests
    
    static { LogCtl.setLog4j() ; }

    public static void main(String... args) {
        
        BPT.CheckingNode = true ;
        BPT.CheckingTree = false ;
        SystemIndex.setNullOut(true) ;
        testClear(15) ;
    }    
    
    static RecordFactory recordFactory = new RecordFactory(4, 0) ;
    
    static Journal journal = Journal.create(Location.mem()) ;
    
    protected static BPlusTree makeRangeIndex(int order, int minRecords) {
        BPlusTree bpt = BPlusTreeFactory.makeMem(order, minRecords, RecordLib.TestRecordLength, 0) ;
        if ( true ) {
            BlockMgr mgr1 = bpt.getNodeManager().getBlockMgr() ;
            BlockMgr mgr2 = bpt.getRecordsMgr().getBlockMgr() ;
            //mgr1 = new BlockMgrLogger(mgr1, false) ; 
            mgr2 = new BlockMgrLogger(mgr2, false) ; 
            bpt =  BPlusTreeFactory.create(null, bpt.getParams(), mgr1, mgr2) ;
        }
        if ( true ) {
            BPT.CheckingNode = true ;
            //BPT.CheckingTree = true ;
            BlockMgr mgr1 = bpt.getNodeManager().getBlockMgr() ;
            BlockMgr mgr2 = bpt.getRecordsMgr().getBlockMgr() ;
            //mgr1 = BlockMgrTrackerWriteLifecycle.track(mgr1) ;
            mgr2 = BlockMgrTrackerWriteLifecycle.track(mgr2) ;
            bpt =  BPlusTreeFactory.create(null, bpt.getParams(), mgr1, mgr2) ;
        }
        bpt.nonTransactional() ;
        bpt.startBatch();
        return bpt ;
    }
    
    public static void testClear(int N) {
        LogCtl.disable(BlockMgrTrackerWriteLifecycle.logger.getName()) ;
        LogCtl.disable(BlockMgr.class) ;
        int[] keys = new int[N] ; // Slice is 1000.
        for ( int i = 0 ; i < keys.length ; i++ )
            keys[i] = i ;
        BPlusTree rIndex = makeRangeIndex(3, 3) ;
        TestLib.add(rIndex, keys) ;
        
        // XXX Start tracking, finish tracking.
        //rIndex.clear() ;
//        @Override 
//        public void clear() {
        int SLICE = 1000 ;
        Record[] records = new Record[SLICE] ;
        while(true) {
            Iterator<Record> iter = rIndex.iterator() ;
            int i = 0 ; 
            for ( i = 0 ; i < SLICE ; i++ ) {
                if ( ! iter.hasNext() )
                    break ;
                Record r = iter.next() ;
                records[i] = r ;
            }
            if ( i == 0 )
                break ;
            
            System.out.println("START CLEAR") ; 
            resetAnyTracking(rIndex) ;
            LogCtl.enable(BlockMgrTrackerWriteLifecycle.logger.getName()) ;
            LogCtl.enable(BlockMgr.class) ;
            for ( int j = 0 ; j < i ; j++ ) {
                rIndex.delete(records[j]) ;
                records[j] = null ;
            }
        }
        // ---- clear
    }
    
    private static void resetAnyTracking(BPlusTree bpt) {
        resetAnyTracking(bpt.getRecordsMgr().getBlockMgr()) ;
        resetAnyTracking(bpt.getNodeManager().getBlockMgr()) ;
    }
    
    private static void resetAnyTracking(BlockMgr blkMgr) {
        if ( blkMgr instanceof BlockMgrTrackerWriteLifecycle )
            ((BlockMgrTrackerWriteLifecycle)blkMgr).clearAll();
    }
    
    public static void testClearMod(int N) {
        SystemIndex.setNullOut(true);
        int[] keys = new int[N] ; // Slice is 1000.
        for ( int i = 0 ; i < keys.length ; i++ )
            keys[i] = i+0xAA990000 ;
        BPlusTree bpt = makeRangeIndex(2, 2) ;
        // Move the reords boxes to different numbers.
       
        List<Record> x = intToRecord(keys, RecordLib.TestRecordLength) ;
        for ( Record r : x )
        {
            //System.out.println("  Add: "+r) ;
            bpt.insert(r) ;
        }
        range(0, N).forEach(idx-> bpt.delete(x.get(idx))) ;

//        BPT.Logging = true ;
//        delete1(bpt, x.get(0)) ;
        
        System.out.println() ;
        //elements(bpt) ;
        bpt.dump();
    }
    
    public static void tree_del_2() {
        // tree_del_2_04
        int[] keys1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        int[] keys2 = {0, 9, 2, 7, 4, 5, 6, 3, 8, 1} ;
       
        //tree_del_2_03
//        int[] keys1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
//        int[] keys2 = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9} ;
        BPlusTree bpt = makeRangeIndex(2,2) ;
        range(1, 50).forEach(i->bpt.getRecordsMgr().create()) ;

        TestLib.testInsert(bpt, keys1) ;
        bpt.dump();
        TestLib.testDelete(bpt, keys2) ;
    }
    
    static void elements(BPlusTree bpt) {
        System.out.print("Elements: ") ;
        bpt.iterator().forEachRemaining(_1 -> { System.out.print(" "); System.out.print(_1); }  ) ;
        System.out.println() ;
    }
    
    static void delete1(BPlusTree rIndex, Record r) {
        System.out.println("delete "+r) ;
        rIndex.delete(r) ;
//        rIndex.dump() ;
//        System.out.println() ;
    }
    

    static void dump(BPlusTree bpt) {
        boolean b = BPT.Logging ;
        BPT.Logging = false ;
        System.out.println() ;
        bpt.dump() ;
        System.out.println() ;
        BPT.Logging = b ; 
    }
    
    static void add(BPlusTree bpt, List<Record> records) {
        records.forEach((x) -> { 
            bpt.insert(x) ;
//            dump(bpt) ;
//            System.out.println() ;
        } ) ;
    }
    
    static void add(BPlusTree bpt, Integer ... values) {
        List<Integer> data = Arrays.asList(values) ;
        List<Record> dataRecords = data.stream().map(x->r(x)).collect(Collectors.toList()) ;
        dataRecords.forEach((x) -> { 
            bpt.insert(x) ;
        } ) ;
    }

    static void verbose(boolean yesOrNo, Runnable r) {
        boolean b = BPT.Logging ;
        try {
            BPT.Logging = yesOrNo ;
            r.run(); 
        } finally { 
            BPT.Logging = b ;
        }
    }
    
    static void printTxnCoordState(TransactionCoordinator txnCoord) {
        printTxnCoordState(System.out, txnCoord) ;
    }
    
    static void printTxnCoordState(PrintStream ps, TransactionCoordinator txnCoord) {
        ps.println("TransactionCoordinator") ;
        ps.printf("  Started:  %4d (R: %d, W:%d)\n", txnCoord.countBegin(), txnCoord.countBeginRead(), txnCoord.countBeginWrite()) ; 
        ps.printf("  Active:   %4d\n", txnCoord.countActive()) ;
        ps.printf("  Finished: %4d\n", txnCoord.countFinished()) ;
    }
}



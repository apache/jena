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
import static org.seaborne.dboe.test.RecordLib.toIntList ;

import java.io.PrintStream ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.logging.LogCtl ;
import org.junit.Test ;
import org.seaborne.dboe.base.block.BlockMgr ;
import org.seaborne.dboe.base.block.BlockMgrLogger ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.sys.SystemIndex ;
import org.seaborne.dboe.test.RecordLib ;
import org.seaborne.dboe.trans.bplustree.BPT ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.trans.bplustree.BlockTracker ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;

public class MainIndexTest {
    // Extract and debug tests
    
    static { LogCtl.setLog4j() ; }

    public static void main(String... args) {
        SystemIndex.setNullOut(true) ;
        new MainIndexTest().tree_iter_2_02();
    }
    
    @Test
    public void tree_iter_2_02() {
        int[] keys = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9} ;
        BPlusTree rIndex = makeRangeIndex(2) ;
        TestLib.add(rIndex, keys) ;
//        rIndex.dump();
//        rIndex.check();
        List<Integer> x = toIntList(rIndex.iterator(r(4), r(7))) ;
        List<Integer> expected = toIntList(4, 5, 6) ;
        
        System.out.println("Actual   = "+x) ;
        System.out.println("Expected = "+expected) ;
        
        //assertEquals(expected, x) ;
        
    }

    
    public static void tree_keys() {
        LogCtl.disable(BlockTracker.logger.getName()) ;
        LogCtl.disable(BlockMgr.class) ;

        int[] keys1 = {} ;
        int[] keys2 = {} ;
        
        BPlusTree bpt = makeRangeIndex(2) ;
        TestLib.testInsertDelete(bpt, keys1, keys2);
        System.out.println("Finished");
        System.exit(0) ;

        if ( false ) {
            bpt.getRecordsMgr().startUpdate();
            range(1, 50).forEach(i->bpt.getRecordsMgr().create()) ;
            bpt.getRecordsMgr().finishUpdate();
        }
        TestLib.testInsert(bpt, keys1) ;
//        System.out.println("START") ; 
//        resetAnyTracking(bpt) ;
//        LogCtl.enable(BlockTracker.logger.getName()) ;
//        LogCtl.enable(BlockMgr.class) ;
        TestLib.delete(bpt, keys2) ;
        bpt.dump();
        
        //TestLib.testDelete(bpt, keys2) ;
    }
    
    private static void printordered(int[] vals) {

        List<Integer> x = new ArrayList<>() ;
        for ( int i : vals )
            x.add(i) ;
        x.stream().sorted().forEach(i -> System.out.printf("  %d", i));
        System.out.println() ;
    }
    
    
    public static void testClear(int N) {
        LogCtl.disable(BlockTracker.logger.getName()) ;
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
            LogCtl.enable(BlockTracker.logger.getName()) ;
            LogCtl.enable(BlockMgr.class) ;
            for ( int j = 0 ; j < i ; j++ ) {
                rIndex.delete(records[j]) ;
                records[j] = null ;
            }
        }
        // ---- clear
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
    
    protected static BPlusTree makeRangeIndex(int order) {
        return makeRangeIndex(order, order) ;
    }
    protected static BPlusTree makeRangeIndex(int order, int minRecords) {
        BPlusTree bpt = BPlusTreeFactory.makeMem(order, minRecords, RecordLib.TestRecordLength, 0) ;
        if ( false ) {
            BufferChannel rootState = null ; // bpt.getRootManager().getChannel() ;
            
            BlockMgr mgrNodes = bpt.getNodeManager().getBlockMgr() ;
            //mgrNodes = new BlockMgrLogger(bpt.getNodeManager().getBlockMgr(), false) ;

            BlockMgr mgrRecords = bpt.getRecordsMgr().getBlockMgr() ;
            mgrRecords = new BlockMgrLogger(mgrRecords, false) ;
            // But disable for now.
            LogCtl.disable(BlockMgr.class);
            
            bpt =  BPlusTreeFactory.create(null, bpt.getParams(), rootState, mgrNodes, mgrRecords) ;
        }
        if ( false ) {
            BPT.CheckingNode = true ;
            BufferChannel rootState = null ; // bpt.getRootManager().getChannel() ;
            //BPT.CheckingTree = true ;
            BlockMgr mgrNodes = bpt.getNodeManager().getBlockMgr() ;
            //mgrNodes = BlockMgrTrackerWriteLifecycle.track(mgr1) ;
            BlockMgr mgrRecords = bpt.getRecordsMgr().getBlockMgr() ;
            mgrRecords = BlockTracker.track(mgrRecords) ;
            
            bpt =  BPlusTreeFactory.rebuild(bpt, rootState, mgrNodes, mgrRecords) ;
        }
        bpt.nonTransactional() ;
        return bpt ;
    }
    private static void resetAnyTracking(BPlusTree bpt) {
        resetAnyTracking(bpt.getRecordsMgr().getBlockMgr()) ;
        resetAnyTracking(bpt.getNodeManager().getBlockMgr()) ;
    }
    private static void resetAnyTracking(BlockMgr blkMgr) {
        if ( blkMgr instanceof BlockTracker )
            ((BlockTracker)blkMgr).clearAll();
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



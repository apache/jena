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
import java.util.List ;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.logging.LogCtl ;
import org.seaborne.dboe.base.block.BlockMgrFactory ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.RangeIndex ;
import org.seaborne.dboe.sys.SystemIndex ;
import org.seaborne.dboe.test.RecordLib ;
import org.seaborne.dboe.trans.bplustree.BPT ;
/* ***************** TRANSACTION BPLUSTREE *********************** */
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeParams ;
import org.seaborne.dboe.transaction.Transactional ;
/* ***************** TRANSACTION BPLUSTREE *********************** */
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

import com.hp.hpl.jena.query.ReadWrite ;

public class MainIndex {
    static { LogCtl.setLog4j() ; }

    public static void main(String... args) {
        SystemIndex.setNullOut(true);
        main1() ;
    }    
    
    static RecordFactory recordFactory = new RecordFactory(4, 0) ;
    
    static Journal journal = Journal.create(Location.mem()) ;
    
    protected static BPlusTree makeRangeIndex(int order, int minRecords) {
        BPlusTree bpt = BPlusTreeFactory.makeMem(order, minRecords, RecordLib.TestRecordLength, 0) ;
        if ( true ) {
            // Breaks with CheckingTree = true ; 
            // because they are deep reads into the tree.
            BPlusTreeParams.CheckingNode = true ;
            BPlusTreeParams.CheckingTree = true ;
            //bpt = BPlusTreeFactory.addTracking(bpt) ;
        }
        bpt.nonTransactional() ;
        return bpt ;
    }
    
    public static void testClear() {
        int N = 30 ;
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
    
    public static void main1(String... args) {
        BPT.Logging = false ;
        BlockMgrFactory.AddTracker = false ;
        SystemIndex.setNullOut(true) ;
        
        BPlusTree bpt = BPlusTreeFactory.makeMem(2, 1, recordFactory.keyLength(), recordFactory.valueLength()) ;

        // Later - integrate
        Journal journal = Journal.create(Location.mem()) ;
        Transactional holder = new TransactionalBase(journal, bpt) ;
        holder.begin(ReadWrite.WRITE);
        
        RangeIndex idx = bpt ;
        
        for ( int i = 0 ; i < 10 ; i ++ )
            bpt.getRecordsMgr().getBlockMgr().allocate(-1) ;
        
        List<Integer> data1 = Arrays.asList( 1 , 3 , 5 ) ;//  7 , 9 , 8 , 6 , 4 , 2 ) ;
        List<Integer> data2 = Arrays.asList( 7 , 9 , 8 ) ;
        
        List<Record> dataRecords1 = data1.stream().map(x->r(x)).collect(Collectors.toList()) ;
        List<Record> dataRecords2 = data2.stream().map(x->r(x)).collect(Collectors.toList()) ;
        
        // Add data1 without logging 
        if ( dataRecords1 != null && !dataRecords1.isEmpty() ) {
            dataRecords1.forEach(bpt::insert) ;
        }
        
        System.out.printf("BPT root = %d\n", bpt.getRootId()) ;
        
        // Add data2 with logging 
        if ( dataRecords2 != null && !dataRecords2.isEmpty() ) {
            dump(bpt) ;
            System.out.println() ;
            BPT.Logging = true ;
            add(bpt, dataRecords2) ;
            BPT.Logging = false ;
            dump(bpt) ;
            System.out.println() ;
        }
        
        holder.commit() ;
        holder.end() ;
        
        holder.begin(ReadWrite.READ) ;
        dump(bpt);
        elements(bpt) ;
        holder.end() ;
        
        int rootIdx1 = bpt.getRootId() ;
        
        System.out.println("** New transaction") ;
        holder.begin(ReadWrite.WRITE);
        add(bpt, 0xFFAA, 0xFFBB, 0xFFCC) ;
        dump(bpt) ;
        holder.commit() ;
        holder.end() ;
        
        int rootIdx2 = bpt.getRootId() ;
        System.out.println("Root = "+bpt.getRootId()) ;
        
        holder.begin(ReadWrite.READ) ;
        dump(bpt);
        elements(bpt);
        holder.end() ;
        
        bpt.$testForce$(rootIdx1) ;
        holder.begin(ReadWrite.READ) ;
        //dump(bpt);
        System.out.println("Root = "+bpt.getRootId()) ;
        elements(bpt);
        holder.end() ;
        
        bpt.$testForce$(rootIdx2) ;
        holder.begin(ReadWrite.READ) ;
        //dump(bpt);
        System.out.println("Root = "+bpt.getRootId()) ;
        elements(bpt);
        holder.end() ;
        

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



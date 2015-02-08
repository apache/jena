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
/* ***************** TRANSACTION BPLUSTREE *********************** */
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeParams ;
import org.seaborne.dboe.transaction.Transactional ;
/* ***************** TRANSACTION BPLUSTREE *********************** */
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

import static dev.RecordLib.* ;

import com.hp.hpl.jena.query.ReadWrite ;

public class MainIndex {
    static { LogCtl.setLog4j() ; }
    
    static RecordFactory recordFactory = new RecordFactory(4, 0) ;
    
    static Journal journal = Journal.create(Location.mem()) ;
    
    @SuppressWarnings("null")
    public static void main(String[] args) {
        BPlusTreeParams.Logging = false ;
        BlockMgrFactory.AddTracker = false ;
        SystemIndex.setNullOut(true) ;
        
        BPlusTree bpt = BPlusTreeFactory.makeMem(2, 1, recordFactory.keyLength(), recordFactory.valueLength()) ;

        // Later - integrate
        Journal journal = Journal.create(Location.mem()) ;
        Transactional holder = new TransactionalBase(journal, bpt) ;
        holder.begin(ReadWrite.WRITE);
        
        RangeIndex idx = bpt ;
        
        //List<Integer> data1 = Arrays.asList( 1 , 3 , 5 , 7 , 9 , 8 , 6 , 4 , 2) ;
        
        List<Integer> data1 = Arrays.asList( 2, 3, 4 ) ; // , 7 , 8 , 9 ) ;
        
        List<Integer> data2a = Arrays.asList( 2 , 4, 3 , 7 , 8 ) ;
        List<Integer> data2b = Arrays.asList( 9 ) ; // , 7 , 8 , 9 ) ;
        
        List<Record> dataRecords1 =  null ; data1.stream().map(x->r(x)).collect(Collectors.toList()) ;
        List<Record> dataRecords2a =  data2a.stream().map(x->r(x)).collect(Collectors.toList()) ;
        List<Record> dataRecords2b =  data2b.stream().map(x->r(x)).collect(Collectors.toList()) ;
        
//        Runnable r = () -> data2.forEach((x) -> idx.add(r(x)) ) ;
        
        bpt.startBatch();
        
        if ( dataRecords1 != null ) {
            verbose(true, ()->add(bpt,dataRecords1)) ;
            dump(bpt) ;
            System.out.println() ;
        }
        if ( dataRecords2a != null && dataRecords2b != null ) {
            // Two part.
            add(bpt, dataRecords2a) ;
            System.out.println("After first records") ;
            dump(bpt);
            BPlusTreeParams.Logging = true ;
            add(bpt, dataRecords2b) ;
            System.out.println("After second records") ;    
        }
        
        holder.commit() ;
        holder.end() ;
        
        holder.begin(ReadWrite.READ);
        

        bpt.finishBatch();
        
        System.out.println() ;

        dump(bpt);
        
//        
//        TransactionCoordinator txnCoord1 = new TransactionCoordinator(Journal.create(Location.mem())) ;
//        Transactional tIdx = new TransactionalBase("Counter", txnCoord1) ;
//        txnCoord1.add(idx) ;
//        
//        Txn.executeWrite(tIdx, r) ;
//        
//        Txn.executeRead(tIdx, bpt::dump) ;

        
//        bpt.begin(ReadWrite.WRITE) ; 
//        for( int k : data2 ) {
//            idx.add( r(k) ) ;
//        }
//        bpt.commit() ;

//        bpt.begin(ReadWrite.READ) ; 
//        Iterator<Record> iter = idx.iterator(r(2), r(7)) ;
//        iter.next() ;
//        iter.next() ;
//        bpt.complete() ;
//        
//        bpt.begin(ReadWrite.READ) ;
//        bpt.dump();
//        bpt.complete() ;
    }
    
    static void dump(BPlusTree bpt) {
        boolean b = BPlusTreeParams.Logging ;
        BPlusTreeParams.Logging = false ;
        bpt.dump() ;
        BPlusTreeParams.Logging = b ; 
    }
    
    static void add(BPlusTree bpt, List<Record> records) {
        records.forEach((x) -> { 
            bpt.add(x) ;
//            dump(bpt) ;
//            System.out.println() ;
        } ) ;
    }
    
    static void verbose(boolean yesOrNo, Runnable r) {
        boolean b = BPlusTreeParams.Logging ;
        try {
            BPlusTreeParams.Logging = yesOrNo ;
            r.run(); 
        } finally { 
            BPlusTreeParams.Logging = b ;
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
    
//    static Record record(int key) {
//        return intToRecord(key) ;
//    }
//
//    // Size of a record when testing (one integer)
//    public final static int TestRecordLength = 4 ;
//    
//    public static Record intToRecord(int v) { return intToRecord(v, recordFactory) ; }
//    public static Record intToRecord(int v, int recLen) { return intToRecord(v, new RecordFactory(recLen, 0)) ; }
//    
//    public static Record intToRecord(int v, RecordFactory factory)
//    {
//        byte[] vb = Bytes.packInt(v) ;
//
//        int recLen = factory.recordLength() ;
//        byte[] bb = new byte[recLen] ;
//        int x = 0 ; // Start point in bb.
//        if ( recLen > 4 )
//            x = recLen-4 ;
//        
//        int len = Math.min(4, recLen) ;
//        int z = 4-len ; // Start point in vb
//    
//        // Furthest right bytes.
//        for ( int i = len-1 ; i >= 0 ; i-- ) 
//           bb[x+i] = vb[z+i] ; 
//        
//        return factory.create(bb) ;
//    }
//
//    public static List<Record> intToRecord(int[] v) { return intToRecord(v, recordFactory) ; }
//
//    public static List<Record> intToRecord(int[] v, int recLen)
//    { return intToRecord(v, new RecordFactory(recLen, 0)) ; }
//    
//    static List<Record> intToRecord(int[] v, RecordFactory factory)
//    {
//        List<Record> x = new ArrayList<>() ;
//        for ( int i : v )
//            x.add(intToRecord(i, factory)) ;
//        return x ;
//    }
//
//    public static int recordToInt(Record key)
//    {
//        return Bytes.getInt(key.getKey()) ;
//    }
//
//    public static List<Integer> toIntList(Iterator<Record> iter)
//    {
//        return Iter.toList(Iter.map(iter, new Transform<Record, Integer>(){
//            @Override
//            public Integer convert(Record item)
//            {
//                return recordToInt(item) ;
//            }}
//        )) ;
//    }
//    
//    public static Record r(int v)
//    {
//        return intToRecord(v, recordFactory) ; 
//    }
//
//    public static int r(Record rec)
//    {
//        return recordToInt(rec) ; 
//    }
//
//    public static List<Integer> toIntList(int... vals)
//    {
//        List<Integer> x = new ArrayList<>() ;
//        for ( int i : vals )
//            x.add(i) ;
//        return x ;
//    }
//
//    public static List<Integer> r(Iterator<Record> iter)
//    {
//        return toIntList(iter) ;
//    }
//
//    public static void setLog4j() {
//        if ( System.getProperty("log4j.configuration") == null ) {
//            String fn = "log4j.properties" ;
//            File f = new File(fn) ;
//            if ( f.exists() )
//                System.setProperty("log4j.configuration", "file:" + fn) ;
//        }
//    }
}



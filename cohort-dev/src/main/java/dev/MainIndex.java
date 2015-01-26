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

import java.io.File ;
import java.io.PrintStream ;
import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.ByteBufferLib ;
import org.apache.jena.atlas.lib.Bytes ;
import org.seaborne.dboe.base.block.BlockMgrFactory ;
import org.seaborne.dboe.base.file.FileFactory ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.objectfile.ObjectFile ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.RangeIndex ;
import org.seaborne.dboe.index.bplustree.BPlusTree ;
import org.seaborne.dboe.index.bplustree.BPlusTreeParams ;
import org.seaborne.dboe.sys.SystemIndex ;
import org.seaborne.dboe.transaction.* ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

import com.hp.hpl.jena.query.ReadWrite ;

public class MainIndex {
    static { setLog4j() ; }
    
    static RecordFactory recordFactory = new RecordFactory(4, 0) ;
    
    static Journal journal = Journal.create(Location.mem()) ;
    
//    // ?? TransactionalBase
//    static class Transactional1 extends TransactionalBase {
//        public Transactional1(Journal journal, TransactionalComponent unit) {
//            super(new TransactionCoordinator(journal)) ;
//            super.txnMgr.add(unit) ;
//            super.txnMgr.add(new TransLogger()) ;
//        }
//    }
    
    static long x = -99 ;
    
    public static void main(String[] args) {
        Journal journal = Journal.create(Location.mem()) ;
        ObjectFile of = FileFactory.createObjectFileMem("ObjectFile") ;
        TransObjectFile tObj = new TransObjectFile(of, 9) ;
        Transactional t = new TransactionalBase(journal, tObj) ;

        Txn.executeWrite(t, ()->{
            ByteBuffer bb = ByteBuffer.allocate(16) ;
            ByteBufferLib.fill(bb, (byte)0x13); 
            x = tObj.write(bb) ;
        }) ;

        
        t.begin(ReadWrite.WRITE) ;
        {
            ByteBuffer bb = ByteBuffer.allocate(16) ;
            ByteBufferLib.fill(bb, (byte)0x15); 
            //x =
            tObj.write(bb) ;
        }
        t.abort();
        t.end();
        
        System.out.println("Base.length   = "+of.length()) ;
        System.out.println("Base.position = "+of.position()) ;

        Txn.executeRead(t, ()->{
            ByteBuffer bb = tObj.read(x) ;
            ByteBufferLib.print(bb) ;
        }) ;
    }
    
    public static void main1(String[] args) {
        Journal jrnl = Journal.create(Location.mem()) ;
        TransactionCoordinator txnCoord = new TransactionCoordinator(jrnl) ;
        BPlusTreeParams.Logging = false ;
        BlockMgrFactory.AddTracker = true ;
        SystemIndex.setNullOut(true) ;
        
        BPlusTree bpt = BPlusTree.makeMem(2, 1, recordFactory.keyLength(), recordFactory.valueLength()) ;
        
        RangeIndex idx = bpt ;
        List<Integer> data1 = Arrays.asList( 1 , 3 , 5 , 7 , 9 , 8 , 6 , 4 , 2) ;
        List<Integer> data2 = Arrays.asList( 1 , 2 , 3 , 4 , 5 , 6 ) ; // , 7 , 8 , 9 } ;
        
//        Record r = r(0x99) ;
//        idx.add( r ) ;
        
        
        // One component
        
        TransInteger counter1 = new TransInteger(0) ; 
        TransInteger counter2 = new TransInteger(0) ;
        TransMonitor monitor = new TransMonitor() ;

        
        txnCoord.add(counter1).add(counter2).add(monitor) ;
        Transactional t = new TransactionalBase("Counter", txnCoord) ;
        

        long v1 = counter1.value() ;
        long v2 = counter2.value() ;
        
        Txn.executeWrite(t, () -> {
            counter1.inc() ;
            counter2.inc() ;
            if ( counter1.get() != counter2.get() ) {
                System.err.println("Components out of line") ;
            }
            if ( counter1.get() == counter1.value() ) {
                System.err.println("Components out of line") ;
            }
        }) ;
        if ( v1+1 != counter1.value() ) {
            System.err.println("Component 1 inconsistent") ;
        }
        if ( v2+1 != counter2.value() ) {
            System.err.println("Component 2 inconsistent") ;
        }
        
        monitor.print() ;
        
        t.begin(ReadWrite.WRITE);
        counter1.inc();
        t.commit();
        t.end();

        printTxnCoordState(txnCoord) ;

        
        System.out.println("DONE") ;
        System.exit(0) ;
        
        TransactionCoordinator txnCoord1 = new TransactionCoordinator(Journal.create(Location.mem())) ;
        Transactional tIdx = new TransactionalBase("Counter", txnCoord1) ;
        txnCoord1.add(idx) ;
        
        Runnable r = () -> data2.forEach((x) -> idx.add(r(x)) ) ; 
        Txn.executeWrite(tIdx, r) ;
        
        Txn.executeRead(tIdx, bpt::dump) ;

        
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
    
    static void printTxnCoordState(TransactionCoordinator txnCoord) {
        printTxnCoordState(System.out, txnCoord) ;
    }
    
    static void printTxnCoordState(PrintStream ps, TransactionCoordinator txnCoord) {
        ps.println("TransactionCoordinator") ;
        ps.printf("  Started:  %4d (R: %d, W:%d)\n", txnCoord.countBegin(), txnCoord.countBeginRead(), txnCoord.countBeginWrite()) ; 
        ps.printf("  Active:   %4d\n", txnCoord.countActive()) ;
        ps.printf("  Finished: %4d\n", txnCoord.countFinished()) ;
    }
    
    static Record record(int key) {
        return intToRecord(key) ;
    }

    // Size of a record when testing (one integer)
    public final static int TestRecordLength = 4 ;
    
    public static Record intToRecord(int v) { return intToRecord(v, recordFactory) ; }
    public static Record intToRecord(int v, int recLen) { return intToRecord(v, new RecordFactory(recLen, 0)) ; }
    
    public static Record intToRecord(int v, RecordFactory factory)
    {
        byte[] vb = Bytes.packInt(v) ;

        int recLen = factory.recordLength() ;
        byte[] bb = new byte[recLen] ;
        int x = 0 ; // Start point in bb.
        if ( recLen > 4 )
            x = recLen-4 ;
        
        int len = Math.min(4, recLen) ;
        int z = 4-len ; // Start point in vb
    
        // Furthest right bytes.
        for ( int i = len-1 ; i >= 0 ; i-- ) 
           bb[x+i] = vb[z+i] ; 
        
        return factory.create(bb) ;
    }

    public static List<Record> intToRecord(int[] v) { return intToRecord(v, recordFactory) ; }

    public static List<Record> intToRecord(int[] v, int recLen)
    { return intToRecord(v, new RecordFactory(recLen, 0)) ; }
    
    static List<Record> intToRecord(int[] v, RecordFactory factory)
    {
        List<Record> x = new ArrayList<>() ;
        for ( int i : v )
            x.add(intToRecord(i, factory)) ;
        return x ;
    }

    public static int recordToInt(Record key)
    {
        return Bytes.getInt(key.getKey()) ;
    }

    public static List<Integer> toIntList(Iterator<Record> iter)
    {
        return Iter.toList(Iter.map(iter, new Transform<Record, Integer>(){
            @Override
            public Integer convert(Record item)
            {
                return recordToInt(item) ;
            }}
        )) ;
    }
    
    public static Record r(int v)
    {
        return intToRecord(v, recordFactory) ; 
    }

    public static int r(Record rec)
    {
        return recordToInt(rec) ; 
    }

    public static List<Integer> toIntList(int... vals)
    {
        List<Integer> x = new ArrayList<>() ;
        for ( int i : vals )
            x.add(i) ;
        return x ;
    }

    public static List<Integer> r(Iterator<Record> iter)
    {
        return toIntList(iter) ;
    }

    public static void setLog4j() {
        if ( System.getProperty("log4j.configuration") == null ) {
            String fn = "log4j.properties" ;
            File f = new File(fn) ;
            if ( f.exists() )
                System.setProperty("log4j.configuration", "file:" + fn) ;
        }
    }
}



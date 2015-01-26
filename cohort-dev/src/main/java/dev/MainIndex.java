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
import java.io.IOException ;
import java.io.PrintStream ;
import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.Bytes ;
import org.seaborne.jena.tdb.base.block.BlockMgrFactory ;
import org.seaborne.jena.tdb.base.file.Location ;
import org.seaborne.jena.tdb.base.record.Record ;
import org.seaborne.jena.tdb.base.record.RecordFactory ;
import org.seaborne.jena.tdb.index.RangeIndex ;
import org.seaborne.jena.tdb.index.bplustree.BPlusTree ;
import org.seaborne.jena.tdb.index.bplustree.BPlusTreeParams ;
import org.seaborne.jena.tdb.sys.SystemIndex ;
import org.seaborne.transaction.* ;
import org.seaborne.transaction.txn.* ;
import org.seaborne.transaction.txn.journal.Journal ;
import org.seaborne.transaction.txn.journal.JournalEntry ;

import com.hp.hpl.jena.query.ReadWrite ;

public class MainIndex {
    static { setLog4j() ; }
    
    static RecordFactory recordFactory = new RecordFactory(4, 0) ;
    
    static Journal journal = Journal.create(Location.mem()) ;
    
    static private TransInteger i = new TransInteger("STATE", 4) ;
    
    static class Transactional1 extends TransactionalBase {
        public Transactional1(Journal journal) {
            super(new TransactionCoordinator(journal)) ;
            
            super.txnMgr.add(i) ;
            super.txnMgr.add(new TransLogger()) ;
        }
        
        public void inc() { i.inc(); }
        public long get() {
            //super.isInTransaction()  ;
            return i.get() ;
        }
    }
    
    public static void main(String[] args) throws IOException {
        // Fake a journal then recover.
        Journal jrnl2 = Journal.create(Location.mem()) ;
        ComponentId id = ComponentIds.idTxnCounter ;
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES) ;
        bb.putLong(345) ;
        bb.rewind() ;
        PrepareState ps = new PrepareState(id, bb) ; 
        jrnl2.write(ps) ;
        bb.rewind() ;
        bb.putLong(556) ;
        bb.rewind() ;
        jrnl2.write(ps) ;
        jrnl2.writeJournal(JournalEntry.COMMIT) ;
        jrnl2.sync(); 
        
        // Compoent registry.
        
        i.startRecovery(); 
        // Collect into commit groups 
        
        List<PrepareState> commitGroup = new ArrayList<>() ;
        
        jrnl2.entries().forEachRemaining( entry-> {
            ComponentId idx = entry.getComponentId() ; 
            switch(entry.getType()) {
                case ABORT :
                    commitGroup.clear() ;
                    break ;
                case COMMIT :
                    commitGroup.forEach(p-> {
                        ByteBuffer bbx = p.getData() ;
                        //bbx.rewind() ;
                        /*find component*/
                        TransactionalComponent comp = i ;
                        comp.recover(bbx) ;
                    }) ;
                    commitGroup.clear() ;
                    break ;
                case REDO : {
                    // Assume component.
                    ByteBuffer bbx = entry.getByteBuffer() ;
                    commitGroup.add(new PrepareState(idx, bbx)) ;
                    break ;
                }
                case UNDO :
                    break ;
                default :
                    break ;
            }
        });
        i.finishRecovery(); 
        System.out.println("value = "+i.get()) ;
        
//        TransInteger i = new TransInteger("STATE", 1) ;
//        long x = i.value() ;
//        System.out.println("value = "+x) ;
//        System.out.println("DONE") ;
//        System.exit(0) ;

        Transactional1 t1 = new Transactional1(journal) ;
        t1.begin(ReadWrite.WRITE) ;
        t1.inc() ;
        System.out.println("State (txn)  = "+t1.get()) ;
        System.out.println("State (live) = "+i.value()) ;
        System.out.println("State (direct) = "+i.get()) ;
        t1.commit(); 
        t1.end() ;
        
        String str = IO.readWholeFileAsUTF8("STATE") ;
        System.out.println("State (disk) = "+str) ;
        System.out.println("State (live) = "+i.get()) ;
        
        t1.begin(ReadWrite.READ) ;
        long v = t1.get() ;
        System.out.println("State (txn)  = "+v) ;
        t1.commit(); 
        t1.end() ;
        
        
        System.out.println("DONE") ;
        System.exit(0) ;
        
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



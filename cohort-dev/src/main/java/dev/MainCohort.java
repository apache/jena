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

import static org.seaborne.dboe.test.RecordLib.r ;

import java.nio.ByteBuffer ;
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
import org.seaborne.dboe.trans.bplustree.BPT ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.transaction.ComponentIdRegistry ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.ComponentIds ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;
import org.seaborne.dboe.transaction.txn.journal.JournalControl ;
import org.seaborne.dboe.transaction.txn.journal.JournalEntry ;
import org.seaborne.dboe.transaction.txn.journal.JournalEntryType ;

import com.hp.hpl.jena.query.ReadWrite ;

public class MainCohort {
    static { LogCtl.setLog4j() ; }

    public static void main(String... args) {
        BPT.Logging = false ;
        BlockMgrFactory.AddTracker = false ;
        SystemIndex.setNullOut(true) ;
        
//        BPlusTree bpt = BPlusTreeFactory.makeMem(2, 2, RecordLib.TestRecordLength, 0) ;
//        Transactional trans = new TransactionalBase(journal, bpt) ;
//        Txn.executeWrite(trans, ()->{
//            add(bpt, 1, 2, 3) ;
//        }) ;
//        dump(trans, bpt) ;
        
        ComponentIdRegistry reg = new ComponentIdRegistry() ;
        ComponentId cidBase =  ComponentId.allocLocal() ;
        ComponentId cid1 = reg.register(cidBase, "COMP", 1) ;
        ComponentId cid2 = reg.register(cidBase, "COMP", 2) ;
        
        System.out.println(cid1) ;
        System.out.println(cid2) ;
        
        
        ByteBuffer bb = ByteBuffer.allocate(8) ;
        bb.putLong(0xAABBCCDDEEFF1122L) ;
        long x1 = journal.write(JournalEntryType.REDO, cid1, bb) ;
        
        bb.rewind() ;
        bb.putLong(0x1122000000000000L) ;
        journal.write(JournalEntryType.REDO, cid2, bb) ;
        
        //JournalControl.print(journal); 
        journal.write(JournalEntryType.COMMIT, ComponentIds.idSystem, null) ;
        JournalControl.print(journal); 
        
        
        JournalEntry j1 = journal.readJournal(x1) ;
        System.out.println(j1) ;
        
        
        //JournalControl.replay(); ;
    }    
    
    static RecordFactory recordFactory = new RecordFactory(4, 0) ;
    
    static Journal journal = Journal.create(Location.mem()) ;
    
    static void elements(BPlusTree bpt) {
        System.out.print("Elements: ") ;
        bpt.iterator().forEachRemaining(_1 -> { System.out.print(" "); System.out.print(_1); }  ) ;
        System.out.println() ;
    }
    
    static void elements(Transactional t, BPlusTree bpt) {
        System.out.print("Elements: ") ;
        Txn.executeRead(t, () ->
            bpt.iterator().forEachRemaining(_1 -> { System.out.print(" "); System.out.print(_1); }  ) 
            );
        System.out.println() ;
    }
    static void delete1(BPlusTree rIndex, Record r) {
        System.out.println("delete "+r) ;
        rIndex.delete(r) ;
//        rIndex.dump() ;
//        System.out.println() ;
    }
    
    public static void main1(String... args) {
        
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
    
    static void dump(Transactional t, BPlusTree bpt) {
        boolean b = BPT.Logging ;
        BPT.Logging = false ;
        System.out.println() ;
        Txn.executeRead(t, bpt::dump) ;
        System.out.println() ;
        BPT.Logging = b ; 
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
}



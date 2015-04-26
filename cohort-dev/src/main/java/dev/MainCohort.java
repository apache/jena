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

import java.util.concurrent.Semaphore ;

import org.apache.jena.query.ReadWrite ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.seaborne.dboe.base.block.FileMode ;
import org.seaborne.dboe.base.file.FileSet ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.sys.SystemBase ;
import org.seaborne.dboe.sys.SystemIndex ;
import org.seaborne.dboe.test.RecordLib ;
import org.seaborne.dboe.trans.bplustree.BPT ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.transaction.* ;
import org.seaborne.dboe.transaction.txn.* ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

public class MainCohort {
    static { LogCtl.setLog4j() ; }
    
    static RecordFactory recordFactory = new RecordFactory(4, 0) ;
    
    static Journal journal = Journal.create(Location.mem()) ;

    public static void main(String... args) {
        TransInteger integer1 = new TransInteger() ;
        TransInteger integer2 = new TransInteger() ;
        Transactional trans = TransactionalFactory.create(journal, integer1, integer2) ;
        TransactionalBase transBase = (TransactionalBase)trans ; 
        System.out.println("     i="+integer1.get()) ;
        
        trans.begin(ReadWrite.WRITE) ;
        integer1.inc(); 
        System.out.println("(W)  i="+integer1.get()) ;
        
        Txn.threadTxnRead(trans, ()->{
            System.out.println("(RT) i="+integer1.get()) ;
        }).run() ;

        // Sort out.
        TransactionCoordinatorState magic = ((TransactionalBase)trans).detach() ;

        Txn.threadTxnRead(trans, ()->{
            System.out.println("(RT) i="+integer1.get()) ;
        }).run() ;
        
        
        // Not in transaction.
        System.out.println("(D)  i="+integer1.get()) ;
        
        try {
            integer2.inc();
            System.out.println("** integer2 inc") ;
        } catch (TransactionException ex) {
            System.out.println("integer2 : "+ ex.getMessage());
        }

        Txn.executeRead(trans, ()->{
            System.out.println("(R)  i="+integer1.get()) ;
        });
        
        Txn.threadTxnRead(trans, ()->{
            System.out.println("(RT) i="+integer1.get()) ;
        }).run() ;

        
//        // This will go wrong.
//        Txn.executeWrite(trans, ()->{
//            System.out.println("(W2) i="+integer1.get()) ;
//        });

        {
            Transaction t2 = transBase.getTxnMgr().begin(ReadWrite.WRITE, false) ;
            System.out.println("t2 = "+t2) ;
        }

        // restart
        ((TransactionalBase)trans).attach(magic) ;
        
        try {
            integer2.inc();
            System.out.println("integer2 inc") ;
        } catch (TransactionException ex) {
            System.out.println("** integer2 : "+ ex.getMessage());
        }
        
        System.out.println("(A)  i="+integer1.get()) ;
        trans.commit() ;
        trans.end() ;
        
        // Should fail.
        ((TransactionalBase)trans).attach(magic) ;
        
        System.out.println("     i="+integer1.get()) ;
        
    }
    
    public static void main2(String... args) {
        //Non-threaded
        
        BPlusTree bpt = BPlusTreeFactory.createBPTree(null, FileSet.mem(), recordFactory) ;
        TransactionCoordinator coord = new TransactionCoordinator(journal) ;
        coord.add(bpt) ;
        
        Transaction t1 = coord.begin(ReadWrite.WRITE) ;
        Semaphore done = new Semaphore(0) ;
        
        SystemBase.executor.execute(()->{
            Record r = r(6) ;
            bpt.insert(r) ;
            t1.commit(); 
            t1.end() ;
            done.release(1);
        }) ;

        done.acquireUninterruptibly(1);
        
//        Record r = r(6) ;
//        bpt.insert(r) ;
//        t1.commit(); 
//        t1.end() ;
        
        Transaction t2 = coord.begin(ReadWrite.READ) ;
        elements(bpt); 
        t2.end() ;
        
        
        
    }
    
    public static void main1(String... args) {
        // direct + tracking: initial: exception //  continuing: exception
        //                    but ok if run mapped first. 
        // mapped + tracking: initial: warn ; continuing: warn
        
        //SystemIndex.setFileMode(FileMode.direct);
        SystemIndex.setFileMode(FileMode.mapped) ;
        ComponentId cid = ComponentIds.idDev ;
        
        FileSet fs = FileSet.mem();
        
        
        if ( true ) {
//            System.out.println("RESET") ;
//            FileOps.clearAll("BPT");
            FileOps.ensureDir("BPT") ;
            fs = new FileSet("BPT", "tree") ;
        }
        
        BPlusTree bpt1 = BPlusTreeFactory.createBPTree(cid, fs, RecordLib.recordFactory) ;
        BPlusTree bpt = true ? BPlusTreeFactory.addTracking(bpt1) : bpt1 ;
        Journal journal = Journal.create(fs.getLocation()) ;
        Transactional holder = TransactionalFactory.create(journal, bpt) ;
        
        //dump(holder, bpt) ;
        
        int x1 = Txn.executeReadReturn(holder, ()-> {
            Record maxRecord = bpt.maxKey() ;
            if ( maxRecord == null ) return 0 ;
            return r(maxRecord) ;
        } ) ;

        System.out.println("x="+x1) ;
        Txn.executeWrite(holder, () ->{
            Record r = r(x1+1) ;
            bpt.insert(r) ;
        }) ;
        
        int x2 = Txn.executeReadReturn(holder, ()-> {
            Record maxRecord = bpt.maxKey() ;
            if ( maxRecord == null ) return 0 ;
            return r(maxRecord) ;
        } ) ;
        
        System.out.println("x="+x2) ;
        elements(holder, bpt) ;
        System.out.println("DONE") ;
        System.exit(0); 
    }
    
    static void elements(BPlusTree bpt) {
        StringBuilder sb = new StringBuilder() ;
        bpt.iterator().forEachRemaining(record -> { sb.append(" "); sb.append(record.toString()); }  ) ;
        System.out.flush() ;
        System.out.print("Elements:") ;
        System.out.println(sb.toString()) ;
        System.out.flush() ;
    }
    
    static void elements(Transactional t, BPlusTree bpt) {
        Txn.executeRead(t, () -> elements(bpt)) ;
    }

    static void dump(Transactional t, BPlusTree bpt) {
        System.out.println() ;
        Txn.executeRead(t, bpt::dump) ;
        System.out.println() ;
    }
    
    static void dump(BPlusTree bpt) {
        boolean b = BPT.Logging ;
        BPT.Logging = false ;
        System.out.println() ;
        bpt.dump() ;
        System.out.println() ;
        BPT.Logging = b ; 
    }
}



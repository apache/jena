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

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.seaborne.dboe.base.block.BlockMgrFactory ;
import org.seaborne.dboe.base.block.FileMode ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.base.file.BufferChannelMem ;
import org.seaborne.dboe.base.file.FileSet ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.sys.SystemIndex ;
import org.seaborne.dboe.test.RecordLib ;
import org.seaborne.dboe.trans.bplustree.BPT ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.trans.data.TransBlob ;
import org.seaborne.dboe.transaction.ComponentIdRegistry ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.TransactionalFactory ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.ComponentIds ;
import org.seaborne.dboe.transaction.txn.L ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;
import org.seaborne.dboe.transaction.txn.journal.JournalEntry ;
import org.seaborne.dboe.transaction.txn.journal.JournalEntryType ;

public class MainCohort {
    static { LogCtl.setLog4j() ; }
    
    static RecordFactory recordFactory = new RecordFactory(4, 0) ;
    
    static Journal journal = Journal.create(Location.mem()) ;

    public static void main(String... args) {
        // direct + tracking: initial: exception //  continuing: exception
        //                    but ok if run mapped first. 
        // mapped + tracking: initial: warn ; continuing: warn
        
        // direct, no track: initial: "getRead - isModified - tree[0]"
        //   But not memory -> caching? Yses, if cachign enabled.
        //   The block is in the cache as a read block.
        //      --> PageBlockMgr.getRead$
        
        SystemIndex.setFileMode(FileMode.direct);
        //SystemIndex.setFileMode(FileMode.mapped) ;
        ComponentId cid = ComponentIds.idDev ;
        
        FileSet fs = FileSet.mem();
        if ( false ) {
            FileOps.clearAll("BPT");
            fs = new FileSet("BPT", "tree") ;
        }
        
        BPlusTree bpt1 = BPlusTreeFactory.createBPTree(cid, fs, RecordLib.recordFactory) ;
        BPlusTree bpt = false ? BPlusTreeFactory.addTracking(bpt1) : bpt1 ;
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
    
    public static void main1(String... args) {
        BPT.Logging = false ;
        BlockMgrFactory.AddTracker = false ;
        SystemIndex.setNullOut(true) ;
        
        ComponentIdRegistry reg = new ComponentIdRegistry() ;
        ComponentId cidBase =  ComponentId.allocLocal() ;
        ComponentId cid1 = reg.register(cidBase, "Bob", 1) ;
        ComponentId cid2 = reg.register(cidBase, "Bob", 2) ;
        ComponentId cid3 = reg.register(cidBase, "Bob", 3) ;
        
        BufferChannel chan1 = BufferChannelMem.create() ;
        TransBlob bob1 =  new TransBlob(cid1, chan1) ;

        BufferChannel chan2 = BufferChannelMem.create() ;
        TransBlob bob2 =  new TransBlob(cid2, chan2) ;

        BufferChannel chan3 = BufferChannelMem.create() ;
        TransBlob bob3 =  new TransBlob(cid3, chan3) ;
        
        {
            journal.write(JournalEntryType.REDO, cid1, L.stringToByteBuffer("One")) ;
            journal.writeJournal(JournalEntry.COMMIT) ;
        }

        Transactional transactional = TransactionalFactory.create(journal, bob1, bob2, bob3) ;
        
        Txn.executeWrite(transactional, ()->{
            bob2.setBlob(L.stringToByteBuffer("Two")) ;
            bob3.setBlob(L.stringToByteBuffer("Three")) ;
        }) ;
        
        Txn.executeRead(transactional, ()->{
            String s1 = L.byteBufferToString(bob1.getBlob()) ;
            System.out.println(s1) ;
            String s2 = L.byteBufferToString(bob2.getBlob()) ;
            System.out.println(s2) ;
            String s3 = L.byteBufferToString(bob3.getBlob()) ;
            System.out.println(s3) ;
        }) ;
        
        Txn.executeWrite(transactional, ()->{
            bob1.setBlob(L.stringToByteBuffer("ONE")) ;
            bob3.setBlob(L.stringToByteBuffer("THREE")) ;
        }) ;
        
        Txn.executeRead(transactional, ()->{
            String s1 = L.byteBufferToString(bob1.getBlob()) ;
            System.out.println(s1) ;
            String s2 = L.byteBufferToString(bob2.getBlob()) ;
            System.out.println(s2) ;
            String s3 = L.byteBufferToString(bob3.getBlob()) ;
            System.out.println(s3) ;
        }) ;
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



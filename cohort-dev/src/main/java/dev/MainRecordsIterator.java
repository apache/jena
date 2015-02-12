/**

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

import static dev.RecordLib.r ;
import static dev.RecordLib.recordFactory ;

import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;
import java.util.stream.Collectors ;

import com.hp.hpl.jena.query.ReadWrite ;

import org.apache.jena.atlas.logging.LogCtl ;
import org.seaborne.dboe.base.block.BlockMgrFactory ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.record.Record ;
import org.seaborne.dboe.sys.SystemIndex ;
import org.seaborne.dboe.trans.bplustree.BPT ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;

public class MainRecordsIterator {
    static { LogCtl.setLog4j(); }
    
    public static void main(String...argv) {
        
//        RecordBuffer rb = new RecordBuffer(recordFactory, 10) ;
//        IntStream.range(1, 5).forEach(x->{
//            rb.add(r(x)) ;
//        }) ;
//        
//        Iterator<Record> iter = rb.iterator(r(2), r(99)) ;
//        iter.forEachRemaining(System.out::println) ;
        
        
        BPT.Logging = false ;
        BlockMgrFactory.AddTracker = false ;
        SystemIndex.setNullOut(true) ;
        BPlusTree bpt = BPlusTreeFactory.makeMem(2, 1, recordFactory.keyLength(), recordFactory.valueLength()) ;
        for ( int i = 0 ; i < 5 ; i ++ )
            bpt.getRecordsMgr().getBlockMgr().allocate(-1) ;
        // Later - integrate
        Journal journal = Journal.create(Location.mem()) ;
        Transactional holder = new TransactionalBase(journal, bpt) ;
        holder.begin(ReadWrite.WRITE);
        
        List<Integer> data = Arrays.asList(2, 3, 4, 1, 7, 8, 9 ) ;
        List<Record> dataRecords =  data.stream().map(x->r(x)).collect(Collectors.toList()) ;
        add(bpt, dataRecords) ;
        // Missing 2.
        bpt.dump() ;
        System.out.println() ;
        
        // Max test.
        
        BPT.Logging = true ;
        // Not moving on after first node entry done.
//        dwim(bpt, r(3), r(9));
        dwim(bpt, r(0), r(9)) ;
//        dwim(bpt, r(0), null) ;
//        dwim(bpt, null, r(3)) ;
//        dwim(bpt, null, null) ;
    }
    
    static void dwim(BPlusTree bpt, Record r0, Record r1) {
        System.out.printf("Iterator -- %s %s\n", r0, r1) ;
        Iterator<Record> iter = bpt.iterator(r0, r1) ;
        iter.forEachRemaining(r -> System.out.printf("    %s\n", r)) ;
        System.out.println() ;

    }
    
    static void add(BPlusTree bpt, List<Record> records) {
        records.forEach((x) -> { 
            bpt.add(x) ;
//            dump(bpt) ;
//            System.out.println() ;
        } ) ;
    }

}


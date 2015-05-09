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

package org.seaborne.tdb2;

import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.tdb.store.tupletable.TupleIndex ;
import org.apache.jena.tdb.store.tupletable.TupleIndexRecord ;
import org.seaborne.dboe.base.file.FileSet ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.RangeIndex ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeParams ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.tdb2.store.nodetable.NodeTable ;
import org.seaborne.tdb2.sys.SystemTDB ;

public class TDB2Builder {
    
    public static RangeIndex buildRangeIndex(TransactionCoordinator coord, ComponentId cid,
                                             RecordFactory recordFactory,
                                             Location location, String name) {
        FileSet fs = new FileSet(location, name) ;
        return BPlusTreeFactory.createBPTree(cid, fs, recordFactory) ; 
    }
    
    public static NodeTable buildNodeTable(TransactionCoordinator coord, ComponentId cid,
                                           Location location, String name) {
        // Index
        // Object file.
        return null ;
    }
    

    static TupleIndex make(TransactionCoordinator txnMgr, String index) {
        ColumnMap cmap = new ColumnMap("SPO", index) ;
        RecordFactory rf = new RecordFactory(SystemTDB.SizeOfNodeId * cmap.length(), 0) ;
        BPlusTreeParams params = new BPlusTreeParams(5, rf.keyLength(), rf.recordLength()) ;
        BPlusTree bpt = BPlusTreeFactory.makeMem(5, rf.keyLength(), rf.recordLength()) ;
        txnMgr.add(bpt) ;
        TupleIndex tIdx = new TupleIndexRecord(3, cmap, index, rf, bpt) ;
        return tIdx ;
    }

    

    
}


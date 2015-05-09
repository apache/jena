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

import org.apache.jena.atlas.logging.LogCtl ;

public class Reroute {
    static { LogCtl.setCmdLogging(); }
    public static void main(String[] args) {
//        DatasetGraphTDB dsgtdb = DatasetBuilderStd.create(Location.mem(), StoreParams.getDftStoreParams()) ;
//        TupleIndex[] indexes = dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes() ;
//        
//        Journal journal = Journal.create(org.seaborne.dboe.base.file.Location.mem()) ;
//        TransactionCoordinator txnMgr = new TransactionCoordinator(journal) ;
//        Transactional h = new TransactionalBase(txnMgr) ;
//        
//        IntStream.range(0, indexes.length).forEach(i -> {
//            TupleIndex tIdx = indexes[i] ;
//            indexes[i] = make(txnMgr, tIdx.getName()) ;
//        }) ;
//
//        Node s = SSE.parseNode("<http://example/s>") ;
//        Node p = SSE.parseNode("<http://example/p>") ;
//        Node o1 = SSE.parseNode("111") ;
//        Node o2 = SSE.parseNode("222") ;
//        
//        Graph g = dsgtdb.getDefaultGraph() ;
//        
//        Txn.executeWrite(h, ()-> {
//            g.add(Triple.create(s, p, o1));
//            g.add(Triple.create(s, p, o2));
//        }) ;
//        Txn.executeRead(h, ()-> {
//            Iterator<Triple> iter = g.find(null, p, null) ;
//            iter.forEachRemaining(System.out::println);
//        }) ;
//        
    }

//    static TupleIndex make(TransactionCoordinator txnMgr, String index) {
//        ColumnMap cmap = new ColumnMap("SPO", index) ;
//        RecordFactory rf = new RecordFactory(SystemTDB.SizeOfNodeId * cmap.length(), 0) ;
//        BPlusTreeParams params = new BPlusTreeParams(5, rf.keyLength(), rf.recordLength()) ;
//        BPlusTree bpt = BPlusTreeFactory.makeMem(5, rf.keyLength(), rf.recordLength()) ;
//        RangeIndex rangeIndex = new AdapterRangeIndex(bpt) ;
//        txnMgr.add(bpt) ;
//        TupleIndex tIdx = new TupleIndexRecord(3, cmap, index, rf, rangeIndex) ;
//        return tIdx ;
//    }
}


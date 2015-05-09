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

package tdbdev;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.txn.Transaction ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;
import org.seaborne.tdb2.setup.TDB2Builder ;

public class TDB_Dev_Main {
    static { LogCtl.setLog4j(); }
    
    
    public static void main(String[] args) {
        FileOps.ensureDir("DB"); 
        FileOps.clearDirectory("DB");
        Location location = Location.create("DB") ;
        DatasetGraphTDB dsg = (DatasetGraphTDB)TDB2Builder.build(location) ;
        
        Transactional trans = new TransactionalBase(dsg.getTxnCoord()) ;
        trans.begin(ReadWrite.WRITE); 
        //RDFDataMgr.read(dsg, "D.ttl");
        Timer timer = new Timer() ;
        timer.startTimer();
        RDFDataMgr.read(dsg, "/home/afs/Datasets/BSBM/bsbm-5m.nt.gz") ;
        long time_ms = timer.endTimer() ;
        
        trans.commit();
        trans.end(); 
        
        trans.begin(ReadWrite.READ) ;
        
        
        //RDFDataMgr.write(System.out,  dsg, Lang.TRIG) ;
        long x = Iter.count(dsg.find()) ;
        trans.end();
        System.out.println("Count = "+x) ;
        double seconds = time_ms/1000.0 ; 
        System.out.printf("Rate = %,.0f\n", x/seconds) ;
        System.out.println("DONE") ;
        System.exit(0) ;
        
//        FileSet idxFs1 = new FileSet(location, "index") ;
//        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
//        ComponentId cid = ComponentId.allocLocal() ;
//        RangeIndex rIdx = null ; //BPlusTreeFactory.makeBPlusTree(
//
//        BPlusTree x =(BPlusTree)rIdx ;
//        // XXX !!!!!
//        Log.warn(TDB_Dev_Main.class, "Ad-hoc memory journal");  
//        Journal journal = Journal.create(Location.mem()) ; 
//        Transactional trans = new TransactionalBase(journal, x) ;
//        trans.begin(ReadWrite.WRITE);
//        NodeTable nt = new NodeTableThrift(rIdx, location.getPath("data")) ;
//        Node n1 = SSE.parseNode("<http://example/>") ;
//        Node n2 = SSE.parseNode("<http://example/other>") ;
//        NodeId nid1 = nt.getAllocateNodeId(n1) ;
//        NodeId nid2 = nt.getAllocateNodeId(n2) ;
//        System.out.printf("nid1 = %s\n", nid1) ;
//        System.out.printf("nid2 = %s\n", nid2) ;
//        trans.commit();
//        System.exit(0) ;
    }

}


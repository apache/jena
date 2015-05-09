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

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.store.NodeId ;
import org.seaborne.dboe.base.file.FileSet ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.RangeIndex ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;
import org.seaborne.tdb2.store.nodetable.NodeTable ;
import org.seaborne.tdb2.store.nodetable.NodeTableThrift ;
import org.seaborne.tdb2.sys.SystemTDB ;

public class TDB_Dev_Main {

    public static void main(String[] args) {
        FileOps.ensureDir("TEST"); 
        Location location = Location.create("TEST") ;
        FileSet idxFs1 = new FileSet(location, "index") ;
        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
        ComponentId cid = ComponentId.allocLocal() ;
        RangeIndex rIdx = null ; //BPlusTreeFactory.makeBPlusTree(

        BPlusTree x =(BPlusTree)rIdx ;
        // XXX !!!!!
        Log.warn(TDB_Dev_Main.class, "Ad-hoc memory journal");  
        Journal journal = Journal.create(Location.mem()) ; 
        Transactional trans = new TransactionalBase(journal, x) ;
        trans.begin(ReadWrite.WRITE);
        NodeTable nt = new NodeTableThrift(rIdx, location.getPath("data")) ;
        Node n1 = SSE.parseNode("<http://example/>") ;
        Node n2 = SSE.parseNode("<http://example/other>") ;
        NodeId nid1 = nt.getAllocateNodeId(n1) ;
        NodeId nid2 = nt.getAllocateNodeId(n2) ;
        System.out.printf("nid1 = %s\n", nid1) ;
        System.out.printf("nid2 = %s\n", nid2) ;
        trans.commit();
        System.exit(0) ;
    }

}


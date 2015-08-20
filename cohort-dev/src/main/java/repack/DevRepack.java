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
 
package repack;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.migrate.L ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.tdb2.TDBFactory ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;

public class DevRepack {
    static { LogCtl.setLog4j(); }

    public static void main(String ... argv) {
        DatasetGraphTDB dsg = (DatasetGraphTDB)TDBFactory.createDatasetGraph() ;
        TransactionCoordinator coord = dsg.getTxnSystem().getTxnMgr() ;
        coord.disableWriters() ;
        
        L.async(()->Txn.executeRead(dsg, ()->{
            System.out.println("R11") ;
            Lib.sleep(100);
            System.out.println("R12") ;
        })) ;
        L.async(()->Txn.executeWrite(dsg, ()->{
            System.out.println("W1") ;
            Lib.sleep(100);
            System.out.println("W2") ;
        })) ;
        L.async(()->Txn.executeRead(dsg, ()->{
            System.out.println("R21") ;
            Lib.sleep(100);
            System.out.println("R22") ;
        })) ;
        
        Lib.sleep(500);
        coord.enableWriters();
        
        coord.startExclusiveMode(); 
        L.async(()->Txn.executeWrite(dsg, ()->System.out.println("W")));
        Lib.sleep(10);
        System.out.println("E") ;
        Lib.sleep(10);
        coord.finishExclusiveMode();
        Lib.sleep(10);
    }
    
    public static void main1(String ... argv) {
//        Location loc1 = Location.mem() ;
//        Location loc2 = Location.mem();
        Location loc1 = Location.create("DB1") ;
        Location loc2 = Location.create("DB2") ;

        FileOps.clearDirectory("DB1");
        FileOps.clearDirectory("DB2");

        DatasetGraphTDB dsgBase = (DatasetGraphTDB)TDBFactory.connectDatasetGraph(loc1) ;
        Quad q1 = SSE.parseQuad("(_ <s> :p :o)") ;
        Quad q2 = SSE.parseQuad("(<g> <s> :q :o)") ;

        Txn.executeWrite(dsgBase, ()->{dsgBase.add(q1); dsgBase.add(q2);} );
        Txn.executeRead(dsgBase, ()->System.out.println(dsgBase)) ;
        
        //DatasetGraphTDB dsg2 = CloneTDB.cloneDataset(dsgBase, loc2) ;
        DatasetGraphTDB dsg2 =  CloneTDB.cloneDatasetSimple(dsgBase, loc2) ;
        
        Txn.executeRead(dsg2, ()->System.out.println(dsg2)) ;
        System.exit(0) ;
    }


}


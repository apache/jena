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
import org.apache.jena.query.* ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.util.QueryExecUtils ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.TDBFactory ;
import org.seaborne.tdb2.lib.TDBTxn ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;
import org.seaborne.tdb2.store.nodetable.TReadAppendFileTransport ;
import org.seaborne.tdb2.sys.StoreConnection ;

public class TDB_Dev_Main {
    static { LogCtl.setLog4j(); }
    
    public static void main(String[] args) throws Exception {
        {
        Location location = Location.mem() ;
//        FileOps.ensureDir("DB"); 
//        FileOps.clearDirectory("DB");
        Dataset ds = TDBFactory.createDataset(location) ;
        System.exit(0) ;
        }
//        //AbstractTestStoreConnectionBasics.store_05/mem
//        
        long x = System.currentTimeMillis() ;
        
        Quad q  = SSE.parseQuad("(<g> <s> <p> '000-"+x+"') ") ;
        Quad q1 = SSE.parseQuad("(<g> <s> <p> '111-"+x+"')") ;
        Quad q2 = SSE.parseQuad("(<g> <s> <p> '222-"+x+"')") ;
        Quad q3 = SSE.parseQuad("(<g> <s> <p> '333-"+x+"')") ;
        Quad q4 = SSE.parseQuad("(<g> <s> <p> '444-"+x+"')") ;
        
        Location location = Location.mem("foobar") ; 
        StoreConnection sConn = StoreConnection.make(location) ;
        
        DatasetGraphTDB dsg = sConn.getDatasetGraphTDB() ;
        TDBTxn.executeWrite(dsg, ()->{
            dsg.add(q1) ;
        }) ;
        
        dsg.begin(ReadWrite.WRITE);
        dsg.add(q2) ;
        dsg.abort() ;
        dsg.end() ;
                
        StoreConnection.expel(location, true);
        
        System.out.println("DONE(abort)") ;
        
        //load() ;
    }

    private static void details(TReadAppendFileTransport file) {
        System.out.println("Len = "+file.getBinaryDataFile().length()) ;
        System.out.println("W   = "+file.getBinaryDataFile().length()) ;
        System.out.println("R   = "+file.readPosition()) ;
    }
    
    public static void query() {
        Location location = Location.create("DB") ;
        String x = "<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType15>" ;
        String qs = "SELECT * { VALUES ?s {"+x+"} ?s ?p ?o }" ;
        Dataset ds = TDBFactory.createDataset(location) ;
        query(ds, qs) ;
        System.out.println("DONE") ;
        System.exit(0) ;
    }        
    
    public static void load() {
        Location location = Location.create("DB") ;
        FileOps.ensureDir("DB"); 
        FileOps.clearDirectory("DB");
        Dataset ds = TDBFactory.createDataset(location) ;
        String FILE = "/home/afs/Datasets/BSBM/bsbm-50k.nt.gz" ;
        
        long time_ms = -1 ;

        System.out.println("Load "+FILE) ;

        Timer timer = new Timer() ;
        timer.startTimer();
        TDBTxn.executeWrite(ds, ()->RDFDataMgr.read(ds, FILE)) ;
        time_ms = timer.endTimer() ;
        System.out.println("Load finish: "+Timer.timeStr(time_ms)+"s") ;    
        long x = TDBTxn.executeReadReturn(ds, () -> {
            System.out.println("Read start") ;
            return Iter.count(ds.asDatasetGraph().find()) ;
        }) ;
        System.out.printf("Count = %,d\n", x) ;
        if ( time_ms > 0 ) {
            double seconds = time_ms/1000.0 ; 
            System.out.printf("Rate = %,.0f TPS\n", x/seconds) ;
        }
        
        String qs = "SELECT * { ?s ?p ?o } LIMIT 10" ;
        query(ds, qs) ;
        System.out.println("DONE") ;
        System.exit(0) ;
    }
    
    public static void query(Dataset ds, String queryString) {
        Query q = QueryFactory.create(queryString) ;
        TDBTxn.executeRead(ds, ()->{
            try ( QueryExecution qExec = QueryExecutionFactory.create(q, ds) ) {
                QueryExecUtils.executeQuery(qExec);
            }
        }); 
    }

}


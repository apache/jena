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
import org.apache.jena.atlas.logging.ProgressLogger ;
import org.apache.jena.query.* ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.sparql.util.QueryExecUtils ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.TDBFactory ;
import org.seaborne.tdb2.lib.TDBTxn ;
import org.slf4j.LoggerFactory ;

public class TDB_Dev_Main {
    static { LogCtl.setLog4j(); }
    
    public static void main(String[] args) throws Exception {
        load() ;
        query() ;
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
        String FILE = "/home/afs/Datasets/BSBM/bsbm-1m.nt.gz" ;
        
        long time_ms = -1 ;

        System.out.println("Load "+FILE) ;

        Timer timer = new Timer() ;
        timer.startTimer();
        
        StreamRDF s1 = StreamRDFLib.dataset(ds.asDatasetGraph()) ;
        ProgressLogger plog = new ProgressLogger(LoggerFactory.getLogger("LOAD"), 
                                                 "Triples", 50000, 10) ;
        StreamRDFMonitor s2 = new StreamRDFMonitor(s1, plog) ;
        // Ensure transaction overheads acccounted for
        StreamRDFMerge s3 = new StreamRDFMerge(s2) ;
        s3.start();
        
        // Unwrap a layer of start/finish.
        TDBTxn.executeWrite(ds, () -> {
            RDFDataMgr.parse(s3, FILE) ;
        }) ;
        s3.finish();
        
        
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


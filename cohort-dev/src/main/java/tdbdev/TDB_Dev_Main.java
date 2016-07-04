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

package tdbdev;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.* ;
import org.apache.jena.sparql.util.QueryExecUtils ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.tdb2.TDB2Factory ;
import org.seaborne.tdb2.loader.Loader ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;

public class TDB_Dev_Main {
    static { LogCtl.setLog4j(); }
    
    public static void main(String[] args) throws Exception {
        Location location = Location.create("DB") ;
        boolean load = true ;
        FileOps.ensureDir("DB");
        if ( load )
            FileOps.clearDirectory("DB");
        Dataset ds = TDB2Factory.connectDataset(location) ;
        String FILE = "/home/afs/Datasets/BSBM/bsbm-5m.nt.gz" ;
        
        if ( load )
            load(ds, FILE) ;
        count(ds) ;
        query(ds) ;
        System.out.println("DONE") ;
        System.exit(0) ;
    }

    public static void query(Dataset ds) {
        String x = "<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType15>" ;
        String qs = "SELECT * { VALUES ?s {"+x+"} ?s ?p ?o }" ;
        query(ds, qs) ;
    }        
    
    public static void load(Dataset ds, String FILE) {
        System.out.println("Database: "+((DatasetGraphTDB)(ds.asDatasetGraph())).getLocation().getDirectoryPath()) ;
        System.out.println("Load:     "+FILE) ;
        // Needs work: Loader.bulkLoadBatching
        Loader.bulkLoad(ds, FILE) ;
    }
    
    public static void count(Dataset ds) {
        long x = Txn.execReadRtn(ds, () -> {
            return Iter.count(ds.asDatasetGraph().find()) ;
        }) ;
        System.out.printf("Count = %,d\n", x) ;
    }
    
    public static void query(Dataset ds, String queryString) {
        Query q = QueryFactory.create(queryString) ;
        Txn.execRead(ds, ()->{
            try ( QueryExecution qExec = QueryExecutionFactory.create(q, ds) ) {
                QueryExecUtils.executeQuery(qExec);
            }
        }); 
    }

}


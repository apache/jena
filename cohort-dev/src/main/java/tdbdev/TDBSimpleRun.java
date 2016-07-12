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

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.* ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.util.QueryExecUtils ;
import org.apache.jena.system.Txn ;
import org.seaborne.tdb2.TDB2Factory ;

public class TDBSimpleRun {
    static { LogCtl.setLog4j(); }
    public static void main(String[] args) {
        String qs = "SELECT * {?s ?p ?o}" ;
        Query query = QueryFactory.create(qs) ;
        Dataset ds = TDB2Factory.createDataset() ;
        
        Txn.execWrite(ds,()->RDFDataMgr.read(ds, "D.ttl")) ;
        
        Txn.execRead(ds, ()->{
            try(QueryExecution qExec = QueryExecutionFactory.create(query, ds)){
                QueryExecUtils.executeQuery(query, qExec);
            }}) ;
    }
}


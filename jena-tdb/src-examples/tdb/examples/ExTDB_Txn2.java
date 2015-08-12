/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tdb.examples;

import org.apache.jena.atlas.lib.StrUtils ;

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.update.GraphStore ;
import org.apache.jena.update.GraphStoreFactory ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateProcessor ;
import org.apache.jena.update.UpdateRequest ;

/** Example of a WRITE transaction. */
public class ExTDB_Txn2
{
    public static void main(String... argv)
    {
        String directory = "MyDatabases/DB1" ;
        Dataset dataset = TDBFactory.createDataset(directory) ;

        // Start WRITE transaction. 
        //   It's possible to read from the datet inside the write transaction.

        //   An application can have other Datasets, in the same JVM, 
        //   tied to the same TDB database performing read
        //   transactions concurrently. If another write transaction
        //   starts, the call of dataset.begin(WRITE) blocks until
        //   existing writer finishes.
        
        dataset.begin(ReadWrite.WRITE) ;
        try
        {
            GraphStore graphStore = GraphStoreFactory.create(dataset) ;
            // Do a SPARQL Update.
            String sparqlUpdateString = StrUtils.strjoinNL(
                 "PREFIX . <http://example/>",
                 "INSERT { :s :p ?now } WHERE { BIND(now() AS ?now) }"
                 ) ;

            execUpdate(sparqlUpdateString, graphStore) ;
            dataset.commit() ;
            // Or call .abort()
            
        } finally
        {
            // Notify the end of the transaction.
            // The transaction was finished at the point .commit or .abort was called.
            // .end will force an abort() if no previous call to .commit() or .abort()
            // has occurred, so .end() help manage track the state of the transaction.
            // .end() can be called multiple times for the same .begin(WRITE)
            dataset.end() ;
        }
    }
    
    public static void execUpdate(String sparqlUpdateString, GraphStore graphStore)
    {
        UpdateRequest request = UpdateFactory.create(sparqlUpdateString) ;
        UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore) ;
        proc.execute() ;
    }
    
}


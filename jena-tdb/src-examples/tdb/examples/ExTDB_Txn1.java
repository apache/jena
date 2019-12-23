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

import org.apache.jena.query.*;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory ;

/**
 * Example of a READ transaction.
 * See {@link Txn#executeRead}.
 */
public class ExTDB_Txn1
{
    public static void main(String... argv)
    {
        String directory = "MyDatabases/DB1";
        Dataset dataset = TDBFactory.createDataset(directory);

        // Start READ transaction. 
        //   No updates or changes to the dataset are possible while this
        //   dataset is used for a read transaction.
        //   An application can have other Datasets, in the same JVM, 
        //   tied to the same TDB database performing read or write
        //   transactions concurrently.

        // A READ transaction is
        // dataset.begin(ReadWrite.READ);
        // try {
        // ...
        // ... The app can also call dataset.abort() or dataset.commit() here; commit is not necessary
        // } finally { dataset.end();}
        
        // Use Txn.executeRead when the app knows there are no updates.
        // Can use Txn.execute when a write is possible.
        Txn.executeRead(dataset, ()-> {
            // Do some queries
            String sparqlQueryString1 = "SELECT (count(*) AS ?count) { ?s ?p ?o }";
            execQuery(sparqlQueryString1, dataset);

            String sparqlQueryString2 = "SELECT * { ?s ?p ?o }";
            execQuery(sparqlQueryString2, dataset);
        });
    }
    
    public static void execQuery(String sparqlQueryString, Dataset dataset)
    {
        Query query = QueryFactory.create(sparqlQueryString);
        try ( QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ) {
            ResultSet results = qexec.execSelect();
            for ( ; results.hasNext() ; ) {
                QuerySolution soln = results.nextSolution();
                int count = soln.getLiteral("count").getInt();
                System.out.println("count = " + count);
            }
        }
    }
}

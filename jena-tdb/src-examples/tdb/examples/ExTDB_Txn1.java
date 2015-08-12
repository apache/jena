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

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.QuerySolution ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.tdb.TDBFactory ;

/** Example of a READ transaction. */
public class ExTDB_Txn1
{
    public static void main(String... argv)
    {
        String directory = "MyDatabases/DB1" ;
        Dataset dataset = TDBFactory.createDataset(directory) ;

        // Start READ transaction. 
        //   No updates or changes to the dataset are possible while this
        //   dataset is used for a read transaction.
        //   An application can have other Datasets, in the same JVM, 
        //   tied to the same TDB database performing read or write
        //   transactions concurrently.
        
        dataset.begin(ReadWrite.READ) ;
        try
        {
            // Do some queries
            String sparqlQueryString1 = "SELECT (count(*) AS ?count) { ?s ?p ?o }" ;
            execQuery(sparqlQueryString1, dataset) ;
            
            String sparqlQueryString2 = "SELECT * { ?s ?p ?o }" ;
            execQuery(sparqlQueryString2, dataset) ;
            
            // Can also call dataset.abort() or dataset.commit() here 
        } finally
        {
            // Notify the end of the READ transaction.
            // Any use of dataset.abort() or dataset.commit() or dataset.end()
            // .end() can be called multiple times for the same .begin(READ)
            dataset.end() ;
        }
    }
    
    public static void execQuery(String sparqlQueryString, Dataset dataset)
    {
        Query query = QueryFactory.create(sparqlQueryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
        try {
            ResultSet results = qexec.execSelect() ;
            for ( ; results.hasNext() ; )
            {
                QuerySolution soln = results.nextSolution() ;
                int count = soln.getLiteral("count").getInt() ;
                System.out.println("count = "+count) ;
            }
          } finally { qexec.close() ; }
    }
    
}


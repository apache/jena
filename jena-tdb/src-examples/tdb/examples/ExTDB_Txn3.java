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

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

/** Illustraion of working at the DatasetGraph level.
 *  Normally, applications work with {@link Dataset}.
 *  Occasionally, it's more convenient to work with the
 *  TDB-implemented DatasetGraph interface. 
 */
public class ExTDB_Txn3
{
    public static void main(String... argv)
    {
        DatasetGraphTransaction dsg = (DatasetGraphTransaction)TDBFactory.createDatasetGraph() ;

        // Start READ transaction. 
        dsg.begin(ReadWrite.READ) ;
        
        try
        {
            // Do some queries
            String sparqlQueryString1 = "SELECT (count(*) AS ?count) { ?s ?p ?o }" ;
            execQuery(sparqlQueryString1, dsg) ;
        } finally
        {
            dsg.end() ;
        }
    }
    
    public static void execQuery(String sparqlQueryString, DatasetGraph dsg)
    {
        // Add a datset wrapper to conform with the query interface.
        // This shoudl not be very expensive.
        Dataset dataset = DatasetFactory.create(dsg) ;
        
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


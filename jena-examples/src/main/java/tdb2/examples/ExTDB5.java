/*
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

package tdb2.examples;

import org.apache.jena.query.*;
import org.apache.jena.tdb2.TDB2Factory;

/**
 * Example of creating a TDB-backed model. The preferred way is to create a dataset
 * then get the mode required from the dataset. The dataset can be used for SPARQL
 * query and update but the Model (or Graph) can also be used. All the Jena APIs work
 * on the model. Calling TDBFactory is the only place TDB-specific code is needed.
 * See also ExTDB_Txn1 for use with transactions.
 */

public class ExTDB5 {
    public static void main(String...argv) {
        // Direct way: Make a TDB-back Jena model in the named directory.
        String directory = "MyDatabases/DB1";
        Dataset dataset = TDB2Factory.connectDataset(directory);

        // Potentially expensive query.
        String sparqlQueryString = "SELECT (count(*) AS ?count) { ?s ?p ?o }";

        Query query = QueryFactory.create(sparqlQueryString);
        dataset.executeRead(()->{
            try (QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
                ResultSet results = qexec.execSelect();
                for ( ; results.hasNext() ; ) {
                    QuerySolution soln = results.nextSolution();
                    int count = soln.getLiteral("count").getInt();
                    System.out.println("count = " + count);
                }
            }
        });
    }
}

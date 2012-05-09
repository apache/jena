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

package sdb.examples;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.DatasetStore;

/** Example of the usual way to connect store and issue a query.
 *  A description of the connection and store is read from file "sdb.ttl".
 *  Use and password come from environment variables SDB_USER and SDB_PASSWORD.
 */

public class Ex1
{
    static public void main(String...argv)
    {
        String queryString = "SELECT * { ?s ?p ?o }" ;
        Query query = QueryFactory.create(queryString) ;
        Store store = SDBFactory.connectStore("sdb.ttl") ;
        
        // Must be a DatasetStore to trigger the SDB query engine.
        // Creating a graph from the Store, and adding it to a general
        // purpose dataset will not necesarily exploit full SQL generation.
        // The right answers will be obtained but slowly.
        
        Dataset ds = DatasetStore.create(store) ;
        QueryExecution qe = QueryExecutionFactory.create(query, ds) ;
        try {
            ResultSet rs = qe.execSelect() ;
            ResultSetFormatter.out(rs) ;
        } finally { qe.close() ; }
        
        // Close the SDB conenction which also closes the underlying JDBC connection.
        store.getConnection().close() ;
        store.close() ;
    }
}

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
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.sdb.store.LayoutType;

/** Connect to a store using API calls. */ 

public class Ex2
{
    static public void main(String...argv)
    {
        String queryString = "SELECT * { ?s ?p ?o }" ;
        Query query = QueryFactory.create(queryString) ;
        
        StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.Derby) ;
        
        JDBC.loadDriverDerby() ;    // Multiple choices for Derby - load the embedded driver
        String jdbcURL = "jdbc:derby:DB/SDB2"; 
        
        // Passing null for user and password causes them to be extracted
        // from the environment variables SDB_USER and SDB_PASSWORD
        SDBConnection conn = new SDBConnection(jdbcURL, null, null) ; 

        // Make store from connection and store description. 
        Store store = SDBFactory.connectStore(conn, storeDesc) ;
        
        Dataset ds = DatasetStore.create(store) ;
        QueryExecution qe = QueryExecutionFactory.create(query, ds) ;
        try {
            ResultSet rs = qe.execSelect() ;
            ResultSetFormatter.out(rs) ;
        } finally { qe.close() ; }
        store.close() ;
    }
}

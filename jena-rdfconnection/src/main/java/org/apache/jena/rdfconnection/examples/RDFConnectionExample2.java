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

package org.apache.jena.rdfconnection.examples;

import org.apache.jena.query.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;

/* 
 * Example of a connection performng a number of transactional operations.
 */
public class RDFConnectionExample2 {
    public static void main(String ...args) {
        Query query = QueryFactory.create("SELECT * { {?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }");
        Dataset dataset = DatasetFactory.createTxnMem();
        
        try ( RDFConnection conn = RDFConnectionFactory.connect(dataset) ) {
            System.out.println("** Load a file");
            // ---- Transaction 1: load data. 
            Txn.executeWrite(conn, ()->conn.load("data.ttl"));
            
            // ---- Transaction 2: explicit styles 
            conn.begin(ReadWrite.WRITE);
            conn.load("http://example/g0", "data.ttl");
            
            System.out.println("** Inside multistep transaction - query dataset");
            conn.queryResultSet(query, ResultSetFormatter::out);
            
            conn.abort();
            conn.end();
            System.out.println("** After abort 1");
            
            // ---- Transaction 3: explicit styles
            Txn.executeWrite(conn, ()->{
                conn.load("http://example/g0", "data.ttl");
                System.out.println("** Inside multistep transaction - fetch dataset");
                Dataset ds2 = conn.fetchDataset();
                RDFDataMgr.write(System.out, ds2, Lang.TRIG);
                conn.abort();
            });
            
            System.out.println("** After abort 2");
            // Only default graph showing.
            conn.queryResultSet(query, ResultSetFormatter::out);
        }
    }
}

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

/** RDF Connection example */
public class RDFConnectionExample1 {
    public static void main(String ...args) {
        Query query = QueryFactory.create("SELECT * { {?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }");
        Dataset dataset = DatasetFactory.createTxnMem();
        RDFConnection conn = RDFConnectionFactory.connect(dataset);
        
        conn.executeWrite(() ->{
            System.out.println("Load a file");
            conn.load("data.ttl");
            conn.load("http://example/g0", "data.ttl");
            System.out.println("In write transaction");
            conn.queryResultSet(query, ResultSetFormatter::out);
        });
        // And again - implicit READ transaction.
        System.out.println("After write transaction");
        conn.queryResultSet(query, ResultSetFormatter::out);
    }
}

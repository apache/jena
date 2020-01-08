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

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;

/* 
 * Example of a building a remote connection.
 * The connection is to a Fuskei server and may use special features suich as more efficient data encoding.
 */
public class RDFConnectionExample4 {
    public static void main(String ...args) {
        
        RDFConnection conn0 = RDFConnectionRemote.create()
            .destination("http://sparql.org/")
            .queryEndpoint("sparql")
            // Set a specific accept header; here, sparql-results+json (preferred) and text/tab-separated-values
            // The default is "application/sparql-results+json, application/sparql-results+xml;q=0.9, text/tab-separated-values;q=0.7, text/csv;q=0.5, application/json;q=0.2, application/xml;q=0.2, */*;q=0.1" 
            .acceptHeaderSelectQuery("application/sparql-results+json, application/sparql-results+xml;q=0.9")
            .build();
        
        Query query = QueryFactory.create("SELECT * { BIND('Hello'as ?text) }");

        // Whether the connection can be reused depends on the details of the implementation.
        // See example 5.
        try ( RDFConnection conn = conn0 ) { 
            conn.queryResultSet(query, ResultSetFormatter::out);
        }
    }
}

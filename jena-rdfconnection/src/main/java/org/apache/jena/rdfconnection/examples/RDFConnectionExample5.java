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
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;

/* 
 * Example of a building a remote connection
 */
public class RDFConnectionExample5 {
    public static void main(String ...args) {
        
        RDFConnectionRemoteBuilder builder = RDFConnectionRemote.newBuilder()
            .destination("http://sparql.org/")
            // Query only.
            .queryEndpoint("sparql")
            .updateEndpoint(null)
            .gspEndpoint(null);
        
        Query query = QueryFactory.create("SELECT * { BIND('Hello'as ?text) }");

        // Whether the connection can be reused depends on the details of the implementation.
        // See example 5.
        try ( RDFConnection conn = builder.build() ) { 
            conn.queryResultSet(query, ResultSetFormatter::out);
        }
    }
}

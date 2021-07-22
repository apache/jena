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

package org.apache.jena.fuseki.main.examples;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.function.FunctionRegistry;

/**
 * Add a function to the global function registry.
 * Start up a fuseki server.
 * Use the function.
 */

public class ExFuseki_05_CustomFunction {

    /** Our function */
    public static class MyFunction extends FunctionBase1 {
        @Override
        public NodeValue exec(NodeValue v) {
            if ( v.isNumber() )
                return NodeValue.makeString("number");
            return NodeValue.makeString("not a number");
        }
    }

    public static void main(String...a) {
        FusekiLogging.setLogging();

        // ---- Register the function
        FunctionRegistry ref = FunctionRegistry.get();
        ref.put("http://my/num", MyFunction.class);

        // ---- Show it can be used
        // -- Start a server

        int PORT = WebLib.choosePort();

        // Some empty dataset
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = FusekiServer.create()
            .port(PORT)
            .add("/ds", dsg)
            .build();
        server.start();

        // -- Call the server
        String queryString = StrUtils.strjoinNL(
            "SELECT * { "
            , "  VALUES ?Z { 123 'abc'}"
            , "  BIND (<http://my/num>(?Z) AS ?X )"
            ,"}"
            );

        try {
            String url = "http://localhost:"+PORT+"/ds";
            // Connect to the server and execute the query.
            try ( RDFConnection conn = RDFConnectionFactory.connect(url) ) {
                conn.queryResultSet(queryString, ResultSetFormatter::out);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally { server.stop(); }
    }
}

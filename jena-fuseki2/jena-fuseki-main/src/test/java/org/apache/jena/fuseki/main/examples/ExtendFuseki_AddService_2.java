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

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.FusekiLib;
import org.apache.jena.fuseki.build.FusekiBuilder;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.web.HttpSC;

/**
 * Create custom endpoint names, dispatch by {@code Content-Type}.
 * See also {@link ExtendFuseki_AddService_1} for more details.
 */

public class ExtendFuseki_AddService_2 {
    static { LogCtl.setLog4j(); }

    // Endpoint dispatch only.
    static int PORT             = FusekiLib.choosePort();
    
    // The server
    static String SERVER_URL    = "http://localhost:"+PORT+"/";
    
    static String DATASET       = "dataset";
    
    public static void main(String ...args) {
        // Register a new operation

        Operation myOperation = Operation.register("Special", "Custom operation");
        
        // Service endpoint names.
        
        String queryEndpoint = "q";
        String customEndpoint = "x";
        
        // Make a DataService with custom named for endpoints.
        // In this example, "q" for SPARQL query and "x" for our custom extension and no others.
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        DataService dataService = new DataService(dsg);
        // This would add the usual defaults.
        //FusekiBuilder.populateStdServices(dataService, true);
        FusekiBuilder.addServiceEP(dataService, myOperation, customEndpoint);
        FusekiBuilder.addServiceEP(dataService, Operation.Query, queryEndpoint);

        // The handled for the new operation.
        ActionService customHandler = new SpecialService();
        
        FusekiServer server = 
            FusekiServer.create().port(PORT)
                .verbose(true)
                // Register the new operation, and it's handler
                .registerOperation(myOperation, customHandler)

                // The DataService.
                .add(DATASET, dataService)
                
                // And build the server.
                .build();
        
        server.start();
        
        // Try some operations on the server using the service URL. 
        String customOperationURL = SERVER_URL + DATASET + "/" + customEndpoint;
        String queryOperationURL = SERVER_URL + DATASET + "/" + queryEndpoint;
        
        Query query = QueryFactory.create("ASK{}"); 
        
        
        try {
            
            // Try custom name - OK
            try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(queryOperationURL, query) ) {
                qExec.execAsk();
            }
            
            // Try default name - 404
            try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(SERVER_URL + DATASET + "/sparql", query) ) {
                qExec.execAsk();
                throw new RuntimeException("Didn't fail");
            } catch (QueryExceptionHTTP ex) {
                if ( ex.getResponseCode() != HttpSC.NOT_FOUND_404 ) {
                    throw new RuntimeException("Not a 404", ex);
                }
            }
            
            // Service endpoint name : GET
            String s1 = HttpOp.execHttpGetString(customOperationURL);
            if ( s1 == null )
                throw new RuntimeException("Failed: "+customOperationURL);
            
        } finally {
            server.stop();
        }
    }
}

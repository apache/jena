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

package org.apache.jena.fuseki.embedded.examples;

import java.io.IOException;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.FusekiLib;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.util.FileUtils;
import org.apache.jena.web.HttpSC;

/**
 * This example show adding a custom operation to Fuseki.
 * <p>
 * There are two ways operations are routed: for a datset {@code /dataset}:
 * <ol>
 * <li>By endpoint name: {@code /dataset/endpoint}</li>
 * <li>By content type, when a POST is made on the {@code /dataset}.</li>
 * </ol>
 * The first is the usual way; the second is not a common pattern.
 * <p>
 * The second way is in addition to the endpoint; an endpoint is always required to
 * enabled routing by {@code Content-Type}.
 * <p>
 * The process for adding an operation is:
 * <ul>
 * <li>Register the operation with the server, with its implmementation.</li>
 * <li>Add the operation to a datasets.</li>
 * </ul>
 * <pre>
 *   // Register operation.
 *   Operation myOperation = Operation.register("Special", "Custom operation");
 *   // An implementation to call
 *   ActionService customHandler = new SpecialService();
 *   // Builder pattern ...
 *   FusekiServer server = 
 *       FusekiServer.create().port (1122)
 *          // Register the operation with the server, together with implementation. 
 *          .registerOperation(myOperation, customHandler)
 *          // Add a dataset
 *          .add("/dataset", DatasetGraphFactory.createTxnMem(), true)
 *          // Add operation by endpoint
 *          .addOperation("/dataset", "endpoint", myOperation)
 *          .build();
 * </pre>
 * @see SpecialService
 */

public class ExtendFuseki_AddService_1 {
    static { LogCtl.setLog4j(); }

    // Endpoint dispatch only.
    
    // Choose free port for the example
    // Normally, this is fixed and published, and fixed in URLs.
    // To make the example portable, we ask the OS for a free port.
    static int PORT             = FusekiLib.choosePort();
    
    // The server
    static String SERVER_URL    = "http://localhost:"+PORT+"/";
    
    static String DATASET       = "dataset";
    
    public static void main(String ...args) {
        // Create a new operation: operations are really just names (symbols). The code to
        // run is found by looking up the operation in a per-server table that gives the server-specific
        // implementation as an ActionService.

        Operation myOperation = Operation.register("Special", "Custom operation");
        
        // Service endpoint name.
        // This can be different for different datasets even in the same server.
        // c.f. {@code fuseki:serviceQuery}
        
        String endpointName = "special";

        // The handled for the new operation.
        
        ActionService customHandler = new SpecialService();
        
        FusekiServer server = 
            FusekiServer.create().setPort(PORT)
                .setVerbose(true)

                // Register the new operation, and it's handler, but no Content-Type
                .registerOperation(myOperation, customHandler)
                
                // Add a dataset with the normal, default naming services 
                // (/sparql, /query, /update, /upload, /data, /get)  
                .add(DATASET, DatasetGraphFactory.createTxnMem(), true)
                
                // Add the custom service, mapping from endpoint to operation for a specific dataset.
                .addOperation(DATASET, endpointName, myOperation)
                
                // And build the server.
                .build();
        
        // Start the server. This does not block this thread.
        server.start();
        
        // Try some operations on the server using the service URL. 
        String customOperationURL = SERVER_URL + DATASET + "/" + endpointName;
        
        try {

            // Service endpoint name : GET
            String s1 = HttpOp.execHttpGetString(customOperationURL);
            System.out.print(s1);
            if ( s1 == null )
                System.out.println();

            // Service endpoint name : POST
            try ( TypedInputStream stream = HttpOp.execHttpPostStream(customOperationURL, null, "text/plain") ) {
                String s2 = FileUtils.readWholeFileAsUTF8(stream);
                System.out.print(s2);
                if ( s2 == null )
                    System.out.println();
            } catch (IOException ex) { IO.exception(ex); }

            // Service endpoint name. DELETE -> fails 405
            try { 
                HttpOp.execHttpDelete(customOperationURL);
                throw new IllegalStateException("DELETE succeeded");
            } catch (HttpException ex) {
                if ( ex.getResponseCode() != HttpSC.METHOD_NOT_ALLOWED_405 )
                    System.err.println("Unexpected HTTP Response Code: "+ex.getMessage());
                else
                    System.out.println("DELETE rejected correctly: "+ex.getMessage());
            }
        } finally {
            server.stop();
        }
    }
}

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

import java.io.IOException;
import java.net.http.HttpRequest.BodyPublishers;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpOp;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.util.FileUtils;

/**
 * This example show adding a custom operation to Fuseki, with dispatch by {@code Content-Type}.
 * <p>
 * See {@link ExFuseki_01_NamedService} for a general description of the routing process.
 * @see DemoService
 */

public class ExFuseki_03_AddService_ContentType {
    static { FusekiLogging.setLogging(); }

    static int PORT             = WebLib.choosePort();

    // The server
    static String SERVER_URL    = "http://localhost:"+PORT+"/";

    static String DATASET       = "dataset";

    public static void main(String ...args) {
        // Create a new operation: operations are really just names (symbols). The code to
        // run is found by looking up the operation in a per-server table that gives the server-specific
        // implementation as an ActionService.

        Operation myOperation = Operation.alloc("http://example/special3", "special3", "Custom operation");

        // Service endpoint name.
        String endpointName = "special";
        String contentType = "application/special";

        // The handled for the new operation.

        ActionService customHandler = new DemoService();

        FusekiServer server =
            FusekiServer.create().port(PORT)
                .verbose(true)

                // Register the new operation, with content type and handler
                .registerOperation(myOperation, contentType, customHandler)

                // Add a dataset with the normal, default naming services
                // (/sparql, /query, /update, /upload, /data, /get)
                .add(DATASET, DatasetGraphFactory.createTxnMem(), true)

                // Add the custom service, mapping from endpoint to operation for a specific dataset.
                // Required when when routing via Content-Type.
                .addEndpoint(DATASET, endpointName, myOperation)

                // And build the server.
                .build();

        // Start the server. This does not block this thread.
        server.start();

        // Try some operations on the server using the service URL.
        String datasetURL = SERVER_URL + DATASET;
        //String customOperationURL = SERVER_URL + DATASET + "/" + endpointName;

        try {

            // Dataset endpoint name : POST, with Content-type.
            try ( TypedInputStream stream = HttpOp.httpPostStream(datasetURL, contentType, BodyPublishers.ofString(""), "text/plain")) {
                String s2 = FileUtils.readWholeFileAsUTF8(stream);
                System.out.print(s2);
                if ( s2 == null )
                    System.out.println();
            } catch (IOException ex) { IO.exception(ex); }
        } finally {
            server.stop();
        }
    }
}

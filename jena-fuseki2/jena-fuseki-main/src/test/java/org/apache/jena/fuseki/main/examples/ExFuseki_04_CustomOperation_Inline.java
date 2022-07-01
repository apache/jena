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

import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.build.FusekiExt;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpOp;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.web.HttpSC;

/**
 * Example of adding a new operation to a Fuseki server using the builder.
 * <p>
 * The custom operation is defined and registered in this class.
 * <p>
 * See {@link ExFuseki_04_CustomOperation_Module} for an example of how to put the
 * extension into a {@link FusekiModule}.
 */
public class ExFuseki_04_CustomOperation_Inline {

    static {
        JenaSystem.init();
        FusekiLogging.setLogging();
    }

    public static void main(String...args) {
        // Define the operation.
        Operation operation = Operation.alloc("http://example/extra-service", "extra-service", "Test");
        // Register globally.
        FusekiExt.registerOperation(operation, new MyCustomService());

        FusekiServer.create()
            .port(3230)
            .add("/ds", DatasetGraphFactory.createTxnMem())
            // if not global, register for the server being built.
            //.registerOperation(operation, new MyCustomService())
            // Add it to the dataset
            .addEndpoint("/ds", "extra", operation)
            .build()
            .start();

        callOperation("extra");
        System.exit(0);
    }

    // For convenience of the example - include the implementation of the custom operation in the same file.
    private static class MyCustomService extends ActionService {

        @Override
        public void execGet(HttpAction action) {
            executeLifecycle(action);
        }

        @Override
        public void validate(HttpAction action) { }

        @Override
        public void execute(HttpAction action) {
            action.setResponseStatus(HttpSC.OK_200);
            action.setResponseContentType(WebContent.contentTypeTextPlain);
            try {
                action.getResponseOutputStream().print("** GET (custom operation example / inline) ** "+DateTimeUtils.nowAsXSDDateTimeString());
            } catch (IOException e) {
                throw new FusekiException(e);
            }
        }
    }

    private static void callOperation(String name) {
        String x = HttpOp.httpGetString("http://localhost:3230/ds/"+name);
        if ( x == null ) {
            System.out.println("Not found : <null>");
        } else {
            System.out.print(x);
            if ( ! x.endsWith("\n") )
                System.out.println();
        }
    }
}
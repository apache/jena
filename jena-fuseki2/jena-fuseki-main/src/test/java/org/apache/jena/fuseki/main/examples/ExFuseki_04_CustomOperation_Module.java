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
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpOp;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.web.HttpSC;

/**
 * Example of adding a new operation to a Fuseki server with a {@link FusekiModule}.
 * <p>
 * Doing this, adding the jar to the classpath, including the {@link ServiceLoader}
 * setup, will automatically add it to the server.
 * <p>
 * See <a href="https://jena.apache.org/documentation/notes/jena-repack.html">Repacking Jena jars</a>.
 * <p>
 * See <a href="https://jena.apache.org/documentation/notes/system-initialization.html">System Initialization</a>
 */
public class ExFuseki_04_CustomOperation_Module {

    static {
        JenaSystem.init();
        FusekiLogging.setLogging();
    }

    // Example usage.
    public static void main(String...args) {

        FusekiModule fmodCustom = new FMod_Custom();

        FusekiModules modules = FusekiModules.create(List.of(fmodCustom));

        FusekiServer.create().port(3230)
            .add("/ds", DatasetGraphFactory.createTxnMem())
            .fusekiModules(modules)
            .build()
            .start();

        callOperation("extra");
        System.exit(0);
    }

    private static class FMod_Custom implements FusekiModule {

        private Operation myOperation = null;

        public FMod_Custom() {
            myOperation = Operation.alloc("http://example/extra-service", "extra-service", "Test");
        }

        @Override
        public String name() {
            return "Custom Operation Example";
        }

        @Override
        public void start() {
            // Only called if loaded via the ServiceLoader.
            Fuseki.configLog.info("Add custom operation into global registry.");
            System.err.println("**** Fuseki extension ****");
        }

        @Override
        public void prepare(FusekiServer.Builder builder, Set<String> datasetNames, Model configModel) {
            // Register only for the server being built.
            builder.registerOperation(myOperation, new MyCustomService());
            datasetNames.forEach(name->builder.addEndpoint(name, "extra", myOperation));
        }
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
                action.getResponseOutputStream().print("** GET (custom operation example / module) ** "+DateTimeUtils.nowAsXSDDateTimeString());
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
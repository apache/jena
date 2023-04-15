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
import java.util.ServiceLoader;

import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.fuseki.Fuseki;
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
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.web.HttpSC;

/**
 * Example of adding a new operation to a Fuseki server by registering it with the
 * global Fuseki registries and adding it to the global default services.
 * <p>
 * <i>The preferred way of doing this is now to use {@link FusekiModule FusekiModules}.</i>
 * <p>
 * The custom operation is loaded using {@link ServiceLoader} as shown in
 * {@link InitFusekiCustomOperation}.
 * <p>
 * {@link ExFuseki_04_CustomOperation_Module}
 * <p>
 * Doing this, adding the jar to the classpath, including the {@link ServiceLoader}
 * setup, will automatically add it to the server.
 * <p>
 * See <a href="https://jena.apache.org/documentation/notes/jena-repack.html">Repacking Jena jars</a>.
 * <p>
 * See <a href="https://jena.apache.org/documentation/notes/system-initialization.html">System Initialization</a>
 * <p>
 * <b>This example code predates {@link FusekiModule FusekiModules}.</b>
 */
@Deprecated
public class ExFuseki_99_CustomOperation_GlobalDefault {

    static {
        JenaSystem.init();
        FusekiLogging.setLogging();
    }

    // Example usage.
    public static void main(String...args) {

        // Imitate Service loader behaviour for Jena initialization.
        new InitFusekiCustomOperation().start();

        FusekiServer.create().port(3230).add("/ds", DatasetGraphFactory.createTxnMem()).build().start();

        callOperation("extra");
        System.exit(0);
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

    public static class InitFusekiCustomOperation implements JenaSubsystemLifecycle {

        public InitFusekiCustomOperation() {}

        @Override
        public void start() {
            // Can use Fuseki server logging.
            Fuseki.configLog.info("Add custom operation");
            System.err.println("**** Fuseki extension (global) ****");
            Operation op = Operation.alloc("http://example/extra-service", "extra-service", "Test");
            FusekiExt.registerOperation(op, new MyCustomService());
            FusekiExt.addDefaultEndpoint(op, "extra");
        }

        @Override
        public void stop() {}

        @Override
        public int level() { return 1000; }
    }

    // For convenience of the example - include the implementation of the custom operation in the same file.
    private static class MyCustomService extends ActionService {

        // Choose.
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
                action.getResponseOutputStream().print("** GET ** "+DateTimeUtils.nowAsXSDDateTimeString());
            } catch (IOException e) {
                throw new FusekiException(e);
            }
        }
    }
}

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
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.build.FusekiExt;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.apache.jena.web.HttpSC;

/**
* See https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html
*
* Example: the file has one line which is the full package, class name.
* Build: src/main/resources/META-INF/services/org.apache.jena.sys.JenaSubsystemLifecycle
* ----
* fuseki.examples.Ex_FusekiCustomOperation.InitFusekiCustomOperation
* ----
*/
public class InitFusekiCustomOperation implements JenaSubsystemLifecycle {

    public InitFusekiCustomOperation() {}

    @Override
    public void start() {
        // Can use Fuseki server logging.
        Fuseki.configLog.info("Add custom operation");
        System.err.println("**** Fuseki extension ****");
        Operation op = Operation.alloc("http://example/extra-service", "extra-service", "Test");
        FusekiExt.registerOperation(op, new MyCustomService());
        FusekiExt.addDefaultEndpoint(op, "extra");
    }

    @Override
    public void stop() {}

    @Override
    public int level() { return 1000; }

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
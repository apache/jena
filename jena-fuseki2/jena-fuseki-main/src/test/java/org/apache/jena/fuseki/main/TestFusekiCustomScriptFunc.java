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

package org.apache.jena.fuseki.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.apache.jena.sparql.function.scripting.ScriptLangSymbols;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sys.JenaSystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFusekiCustomScriptFunc {

    static { JenaSystem.init(); }

    private static String jsFunctions = StrUtils.strjoinNL
            ( "function inc(x) { return x+1 }"
            , "function dec(x) { return x-1 }"
            );
    private static Symbol symFunctions = ScriptLangSymbols.scriptFunctions("js");

    private static String systemPropertyScriptingOldValue = null;
    private static String scriptFunctionsOldValue = null;

    private static String dsName = "/ds" ;
    private static FusekiServer server = null;

    @BeforeClass public static void enableScripting() {
        systemPropertyScriptingOldValue = System.getProperty(ARQ.systemPropertyScripting);
        scriptFunctionsOldValue = ARQ.getContext().get(symFunctions);
        // Enable
        System.setProperty(ARQ.systemPropertyScripting, "true");
        ARQ.getContext().set(symFunctions,jsFunctions);

        Context context = Context.create().set(ARQ.symCustomFunctionScriptAllowList, "inc,dec");

        Endpoint ep1 = Endpoint.create().endpointName("script1").operation(Operation.Query).context(context).build();
        Endpoint ep2 = Endpoint.create().endpointName("script2").operation(Operation.Query).build();

        DataService dataService = DataService.newBuilder()
                .addEndpoint(ep1)
                .addEndpoint(ep2)
                .dataset(DatasetGraphFactory.empty()).build();

        server = FusekiServer.create()
                .port(0)
                .add(dsName, dataService)
                .build().start();
    }

    @AfterClass public static void disableScripting() {
        if ( server != null )
            server.stop();
        if ( systemPropertyScriptingOldValue != null )
            System.setProperty(ARQ.systemPropertyScripting, systemPropertyScriptingOldValue);
        else
            System.clearProperty(ARQ.systemPropertyScripting);
        ARQ.getContext().set(ScriptLangSymbols.scriptFunctions("js"), scriptFunctionsOldValue);

    }

    @Test public void directCall() {
        // Local.
        Context context1 = ARQ.getContext().copy().set(ARQ.symCustomFunctionScriptAllowList, "inc,dec");

//        Endpoint ep1 = Endpoint.create().endpointName("script1").operation(Operation.Query).context(context1).build();
//        DataService dSrv = DataService.newBuilder().addEndpoint(ep1).build();

        // Need to pass context to ScriptFunction and build engine then.

        String qs = StrUtils.strjoinNL
                ("PREFIX js: <http://jena.apache.org/ARQ/jsFunction#>"
                ,"SELECT ( js:inc(3) as ?x){ }"
                );
        QueryExec qExec = QueryExec.dataset(DatasetGraphFactory.empty()).query(qs).context(context1).build();
        RowSet rs = qExec.select();
        assertTrue(rs.hasNext());
        Binding row = rs.next();
        Node x = row.get("x");

        int i = ((Number) x.getLiteralValue()).intValue();
        assertEquals(4,i);
    }

    @Test public void httpCall_good() {
        Context context1 = ARQ.getContext().copy().set(ARQ.symCustomFunctionScriptAllowList, "inc,dec");
        Endpoint ep1 = Endpoint.create().endpointName("script1").operation(Operation.Query).context(context1).build();
        DataService dataService = DataService.newBuilder().addEndpoint(ep1).dataset(DatasetGraphFactory.empty()).build();

        // Need to pass context to ScriptFunction and build engine then.

        String qs = StrUtils.strjoinNL
                ("PREFIX js: <http://jena.apache.org/ARQ/jsFunction#>"
                 ,"SELECT ( js:inc(3) as ?x){ }"
                        );
        QueryExec qExec = QueryExecHTTP.service(server.datasetURL(dsName)+"/script1").query(qs).build();
        RowSet rs = qExec.select();
        assertTrue(rs.hasNext());
        Binding row = rs.next();
        Node x = row.get("x");

        int i = ((Number) x.getLiteralValue()).intValue();
        assertEquals(4,i);
    }

    @Test public void httpCall_no_such_function() {
        // Need to pass context to ScriptFunction and build engine then.

        String qs = StrUtils.strjoinNL
                ("PREFIX js: <http://jena.apache.org/ARQ/jsFunction#>"
                 ,"SELECT ( js:notPresent(3) as ?x){ }"
                        );
        QueryExecHTTP qExec = QueryExecHTTP.service(server.datasetURL(dsName)+"/script1").query(qs).build();

        FusekiTestLib.expectQuery400(()->{
            qExec.select();
        });
    }

    @Test public void httpCall_not_allowed() {
        // Need to pass context to ScriptFunction and build engine then.

        String qs = StrUtils.strjoinNL
                ("PREFIX js: <http://jena.apache.org/ARQ/jsFunction#>"
                 ,"SELECT ( js:inc(3) as ?x){ }"
                        );
        QueryExecHTTP qExec = QueryExecHTTP.service(server.datasetURL(dsName)+"/script2").query(qs).build();

        FusekiTestLib.expectQuery400(()->{
            qExec.select();
        });
    }

}

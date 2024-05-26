/**
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

package org.apache.jena.sparql.exec.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.sys.HttpRequestModifier;
import org.apache.jena.http.sys.RegistryRequestModifier;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.test.conn.EnvTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Most tests of SERVICE */
public class TestService2 {
    // ---- Enable service
    @BeforeClass public static void enableAllowServiceExecution() { CtlService.enableAllowServiceExecution(); }
    @AfterClass public static void resetAllowServiceExecution() { CtlService.resetAllowServiceExecution(); }
    public static Context minimalContext() { return CtlService.minimalContext(); }
    // ----

    private static String SERVICE;
    private static EnvTest env;
    // Local dataset for execution of SERVICE. Can be used to carry a context.
    private static final DatasetGraph localDataset() {return DatasetGraphZero.create(); }

    @BeforeClass public static void beforeClass() {
        // Also edit src/test/resources/log4j2.properties to get logging output.
        // FusekiLogging.setLogging();
        env = EnvTest.create("/ds");
        SERVICE = env.datasetURL();
    }

    @Before public void before() {
        env.clear();
    }

    @AfterClass public static void afterClass() {
        EnvTest.stop(env);
    }

    @Test public void service_send_mode_1() { serviceSendMode(QuerySendMode.asGetAlways); }
    @Test public void service_send_mode_2() {serviceSendMode(QuerySendMode.asGetWithLimitBody); }
    @Test public void service_send_mode_3() { serviceSendMode(QuerySendMode.asGetWithLimitForm); }
    @Test public void service_send_mode_4() { serviceSendMode(QuerySendMode.asPost); }
    @Test public void service_send_mode_5() { serviceSendMode(QuerySendMode.asPostForm); }

    @Test public void service_send_mode_str_1() { serviceSendMode("GET"); }
    @Test public void service_send_mode_str_2() { serviceSendMode("POST"); }

    @Test(expected=QueryExecException.class)
    public void service_send_mode_str_3() { serviceSendMode("JUNK"); }

    @Test(expected=QueryExecException.class)
    public void service_send_mode_str_4() { serviceSendMode(""); }

    @Test public void service_send_mode_str_10() { serviceSendMode(QuerySendMode.asGetAlways.name()); }
    @Test public void service_send_mode_str_11() { serviceSendMode(QuerySendMode.asGetWithLimitBody.name()); }
    @Test public void service_send_mode_str_12() { serviceSendMode(QuerySendMode.asGetWithLimitForm.name()); }
    @Test public void service_send_mode_str_13() { serviceSendMode(QuerySendMode.asPost.name()); }
    @Test public void service_send_mode_str_14() { serviceSendMode(QuerySendMode.asPostForm.name()); }

    @Test public void service_send_mode_withLimit_1() { serviceSendModeWithLimit(QuerySendMode.asGetAlways, 10); }
    @Test public void service_send_mode_withLimit_2() { serviceSendModeWithLimit(QuerySendMode.asGetWithLimitBody, 10); }
    @Test public void service_send_mode_withLimit_3() { serviceSendModeWithLimit(QuerySendMode.asGetWithLimitForm, 10); }
    @Test public void service_send_mode_withLimit_4() { serviceSendModeWithLimit(QuerySendMode.asPost, 10); }
    @Test public void service_send_mode_withLimit_5() { serviceSendModeWithLimit(QuerySendMode.asPostForm, 10); }

    private void serviceSendMode(QuerySendMode sendMode) {
        serviceSendModeWithLimit(sendMode, -1);
    }

    private void serviceSendMode(String sendMode) {
        Node expected = NodeFactory.createLiteral("28181", XSDDatatype.XSDinteger);
        String queryString = "SELECT * { SERVICE <"+SERVICE+"> { VALUES ?x { 28181 } } }";

        Context cxt = minimalContext();
        // Until GH-1399 is fixed, theer must be a setting for registryServiceExecutors
        cxt.set(ARQConstants.registryServiceExecutors, ARQ.getContext().get(ARQConstants.registryServiceExecutors));
        cxt.set(Service.httpServiceSendMode, sendMode);
        RowSet rs = QueryExec.dataset(localDataset()).query(queryString).context(cxt).select().materialize();
        assertTrue(rs.hasNext());
        Node n = rs.next().get("x");
        assertEquals(expected, n);
    }

    private void serviceSendModeWithLimit(QuerySendMode sendMode, int urlLimit) {
        int systemValue = HttpEnv.urlLimit;
        try {
            if ( urlLimit > 0 )
                HttpEnv.urlLimit = urlLimit;
            Context cxt = minimalContext();
            cxt.set(Service.httpServiceSendMode, sendMode);
            serviceSendMode_runTest(cxt);
        } finally {
            HttpEnv.urlLimit = systemValue;
        }
    }

    private void serviceSendMode_runTest(Context cxt) {
        Node expected = NodeFactory.createLiteral("28181", XSDDatatype.XSDinteger);
        String queryString = "SELECT * { SERVICE <"+SERVICE+"> { VALUES ?x { 28181 } } }";
        RowSet rs = QueryExec.dataset(localDataset()).query(queryString).select().materialize();
        assertTrue(rs.hasNext());
        Node n = rs.next().get("x");
        assertEquals(expected, n);
    }

    private static void runWithModifier(String key, HttpRequestModifier modifier, Runnable action) {
        RegistryRequestModifier.get().add(SERVICE, modifier);
        try {
            action.run();
        } finally {
            RegistryRequestModifier.get().remove(SERVICE);
        }
    }
}

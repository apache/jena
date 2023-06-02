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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.http.HttpClient;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.fuseki.main.FusekiTestLib;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.http.RegistryHttpClient;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.query.ARQ;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkFactory;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.op.OpService ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.test.conn.EnvTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test Service implementation code -- Service.exec */
public class TestServiceAuth {
    // ---- Enable service
    @BeforeClass public static void enableAllowServiceExecution() { CtlService.enableAllowServiceExecution(); }
    @AfterClass public static void resetAllowServiceExecution() { CtlService.resetAllowServiceExecution(); }
    public static Context minimalContext() { return CtlService.minimalContext(); }
    // ----

    private static String SERVICE;
    private static final String USER = "user13";
    private static final String PASSWORD = "pw13";
    private static EnvTest env;

    @BeforeClass public static void beforeClass() {
        env = EnvTest.createAuth("/ds", DatasetGraphFactory.createTxnMem(), USER, PASSWORD);
        SERVICE = env.datasetURL();
    }

    @Before public void before() {
        env.clear();
    }

    @AfterClass public static void afterClass() {
        EnvTest.stop(env);
    }

    private static DatasetGraph empty = DatasetGraphZero.create();

    private static void minimalContext(Context context) {
        context.set(Service.httpServiceAllowed, true);
        ServiceExecutorRegistry registry = ARQ.getContext().get(ARQConstants.registryServiceExecutors);
        context.put(ARQConstants.registryServiceExecutors, registry);
    }

    private static void runServiceQuery(String serviceURL) {
        String queryString = "SELECT * { SERVICE <"+serviceURL+"> { BIND( 'X' as ?X) } }";
        try ( QueryExec qExec = QueryExec.dataset(empty).query(queryString).build() ) {
            qExec.select().materialize();
        }
    }

    private static void runServiceQueryWithContext(String serviceURL, Context cxt) {
        String queryString = "SELECT * { SERVICE <"+serviceURL+"> { BIND( 'X' as ?X) } }";
        try ( QueryExec qExec = QueryExec.dataset(empty).query(queryString).context(cxt).build() ) {
            qExec.select().materialize();
        }
    }

    private static void runServiceQueryWithDataset(String serviceURL, DatasetGraph dsg) {
        String queryString = "SELECT * { SERVICE <"+serviceURL+"> { BIND( 'X' as ?X) } }";
        try ( QueryExec qExec = QueryExec.dataset(dsg).query(queryString).build() ) {
            qExec.select().materialize();
        }
    }


    @Test(expected=QueryExceptionHTTP.class)
    public void service_auth_none() {
        OpService op = TestService.makeOp(env);
        QueryIterator qIter = Service.exec(op, minimalContext());
    }

    @Test
    public void service_exec_auth() {
        // Service request with users/password in it. Not ideal.
        String authorityURL = "http://"+USER+":"+PASSWORD+"@localhost:"+env.server.getPort()+env.dsName();
        Node serviceNode = NodeFactory.createURI(authorityURL);
        OpService op = TestService.makeOp(env, serviceNode);
        QueryIterator qIter = Service.exec(op, minimalContext());
    }

    @Test
    public void service_auth_userinfo_1() {
        String serviceURL = "http://"+USER+":"+PASSWORD+"@localhost:"+env.server.getPort()+env.dsName();

        URI key = URI.create(serviceURL);
        assertFalse(AuthEnv.get().hasRegistation(key));

        runServiceQuery(serviceURL);

        // Check no registration left in place.
        assertFalse(AuthEnv.get().hasRegistation(key));
    }

    @Test
    public void service_auth_userinfo_2() {
        String serviceURL1 = "http://"+USER+":"+PASSWORD+"@localhost:"+env.server.getPort()+env.dsName();
        runServiceQuery(serviceURL1);

        // And again but no auth.
        String serviceURL2 = "http://localhost:"+env.server.getPort()+env.dsName();

        FusekiTestLib.expectQuery401(()->{
            runServiceQuery(serviceURL2);
        });
    }

    @Test
    public void service_auth_userinfo_3() {
        // userinfo in the service URL.
        // Registration here for that users.
        String userURL = "http://role@localhost:"+env.server.getPort()+"/";
        URI uri = URI.create(userURL);
        AuthEnv.get().registerUsernamePassword(uri, USER, PASSWORD);

        // Includes "role", not the (user,password)
        String serviceURL = userURL+"ds";
        try {
            runServiceQuery(serviceURL);
        } finally {
            AuthEnv.get().unregisterUsernamePassword(uri);
        }
    }

    @Test
    public void service_auth_userinfo_4() {
        // User, no password
        // No registration
        String serviceURL = "http://"+USER+"@localhost:"+env.server.getPort()+env.dsName();
        FusekiTestLib.expectQuery401(()->{
            runServiceQuery(serviceURL);
        });
    }

    @Test public void service_auth_good_registry_1_exact() {
        HttpClient hc = env.httpClientAuthGood();
        String serviceURL = env.datasetURL();
        RegistryHttpClient.get().add(serviceURL, hc);
        try {
            runServiceQuery(serviceURL);
        } finally {
            RegistryHttpClient.get().remove(serviceURL);
        }
    }

    @Test public void service_auth_good_registry_2_prefix() {
        HttpClient hc = env.httpClientAuthGood();
        String serviceURL = env.datasetURL();
        RegistryHttpClient.get().addPrefix(env.serverBaseURL(), hc);
        try {
            runServiceQuery(serviceURL);
        } finally {
            RegistryHttpClient.get().remove(env.serverBaseURL());
        }
    }

    @Test public void service_auth_good_registry_3_prefix() {
        HttpClient hc = env.httpClientAuthGood();
        String serviceURL = env.datasetURL();
        // Prefix of the URL.
        String serverURL = env.serverBaseURL();
        assertTrue(serviceURL.startsWith(serverURL));

        // Register under a prefix.
        RegistryHttpClient.get().addPrefix(serverURL, hc);
        try {
            runServiceQuery(serviceURL);
        } finally {
            RegistryHttpClient.get().remove(serverURL);
            //RegistryHttpClient.get().clear();
        }
    }

    @Test public void service_auth_good_cxt_1() {
        Context context = minimalContext();
        HttpClient hc = env.httpClientAuthGood();
        context.set(Service.httpQueryClient, hc);
        runServiceQueryWithContext(env.datasetURL(), context);
    }

    @Test(expected=QueryExceptionHTTP.class)
    public void service_auth_bad_cxt_2() {
        Context context = minimalContext();
        HttpClient hc = env.httpClientAuthBadRetry();    // Bad
        context.set(Service.httpQueryClient, hc);
        runServiceQueryWithContext(env.datasetURL(), context);
    }

    @Test public void service_auth_good_dsg_cxt() {
        // Context dataset graph.
        DatasetGraph local = DatasetGraphZero.create();
        Context context = local.getContext();
        minimalContext(context);
        HttpClient hc = env.httpClientAuthGood();
        context.set(Service.httpQueryClient, hc);
        runServiceQueryWithDataset(SERVICE, local);
    }

    @Test(expected=QueryExceptionHTTP.class)
    public void service_query_auth_context_bad() {
        // Context dataset graph.
        DatasetGraph local = DatasetGraphZero.create();
        Context context = local.getContext();
        minimalContext(context);
        HttpClient hc = env.httpClientAuthBad();    // Bad
        context.set(Service.httpQueryClient, hc);
        runServiceQuery(SERVICE);
    }

    public void service_query_auth_context_silent() {
        DatasetGraph dsg = env.dsg();
        dsg.executeWrite(()->dsg.add(SSE.parseQuad("(_ :s :p :o)")));

        String queryString = "SELECT * { SERVICE SILENT <"+SERVICE+"> { ?s ?p ?o }} ";

        // Context dataset graph.
        DatasetGraph local = DatasetGraphZero.create();

        FusekiTestLib.expectQuery401(() -> {
            RDFLink link = RDFLinkFactory.connect(local);
            try (link) {
                try (QueryExec qExec = link.query(queryString)) {
                    RowSet rs = qExec.select();
                    // OK here. Next line causes the SERVICE clause to execute.
                    long x = Iter.count(rs);
                    fail("Should not get this far");
                }
            }
        });
    }

}

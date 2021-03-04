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

package org.apache.jena.sparql.engine.http;

import java.net.SocketException ;
import java.net.UnknownHostException;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ConnectTimeoutException ;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpService ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.modify.UpdateProcessRemoteBase ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.junit.AfterClass ;
import org.junit.Assert ;
import org.junit.BeforeClass ;
import org.junit.Test ;

/** This test suite does external network traffic.
 *  It causes INFO level messages if the network interface is not reachable.
 *  It does not cause tests to fail. 
 */
public class TestService {
    // TODO Move to jena-integration-tests and use a Fuskei server as target.
    private static final String SERVICE = "http://example.com:40000/";

    private static Object value ;
    
    @BeforeClass public static void recordContextState() { value = ARQ.getContext().get(Service.serviceContext) ; }
    @AfterClass public static void restoreContextState() { ARQ.getContext().set(Service.serviceContext, value) ; }

    @Test
    public void testNumericTimeout() {
        BasicPattern basicPattern = new BasicPattern();
        basicPattern.add(Triple.ANY);
        Node serviceNode = NodeFactory.createURI(SERVICE);
        OpService opService = new OpService(serviceNode, new OpBGP(basicPattern), false);

        Context context = new Context();
        ARQ.setNormalMode(context);

        context.set(Service.queryTimeout, 10);

        try {
            Service.exec(opService, context);
            Assert.fail("Expected QueryExceptionHTTP");
        } catch (QueryExceptionHTTP expected) {
            Throwable thrown = expected.getCause() ;
            if ( thrown instanceof SocketException || thrown instanceof ConnectTimeoutException || thrown instanceof UnknownHostException )  {
                // expected
            } else {
                Assert.fail(String.format("Expected SocketException or ConnectTimeoutException, instead got: %s %s", 
                                          thrown.getClass().getName(), 
                                          thrown.getMessage()));
            }
        }

    }

    @Test
    public void testStringTimeout() {
        BasicPattern basicPattern = new BasicPattern();
        basicPattern.add(Triple.ANY);
        Node serviceNode = NodeFactory.createURI(SERVICE);
        OpService opService = new OpService(serviceNode, new OpBGP(basicPattern), false);

        Context context = new Context();
        ARQ.setNormalMode(context);

        context.set(Service.queryTimeout, "10");

        try {
            Service.exec(opService, context);
            Assert.fail("Expected QueryExceptionHTTP");
        } catch (QueryExceptionHTTP expected) {
            Throwable thrown = expected.getCause() ;
            if ( thrown instanceof SocketException || thrown instanceof ConnectTimeoutException || thrown instanceof UnknownHostException )  {
                // expected
            } else {
                Assert.fail(String.format("Expected SocketException or ConnectTimeoutException, instead got: %s %s", 
                                          thrown.getClass().getName(), 
                                          thrown.getMessage()));
            }
        }
    }

    @Test
    public void testStringTimeout2() {
        BasicPattern basicPattern = new BasicPattern();
        basicPattern.add(Triple.ANY);
        Node serviceNode = NodeFactory.createURI(SERVICE);
        OpService opService = new OpService(serviceNode, new OpBGP(basicPattern), false);

        Context context = new Context();
        ARQ.setNormalMode(context);

        context.set(Service.queryTimeout, "10,10000");

        try {
            Service.exec(opService, context);
            Assert.fail("Expected QueryExceptionHTTP");
        } catch (QueryExceptionHTTP expected) {
            Throwable thrown = expected.getCause() ;
            if ( thrown instanceof SocketException || thrown instanceof ConnectTimeoutException || thrown instanceof UnknownHostException )  {
                // expected
            } else {
                Assert.fail(String.format("Expected SocketException or ConnectTimeoutException, instead got: %s %s", 
                                          thrown.getClass().getName(), 
                                          thrown.getMessage()));
            }
        }
    }

    @Test
    public void query_service_context_application_01() {
        // This test requires no service context to be set
        @SuppressWarnings("unchecked")
        Map<String, Context> serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        if (serviceContextMap != null) {
            serviceContextMap.remove(SERVICE);
        }

        Query q = QueryFactory.create("ASK { }");
        QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(SERVICE, q);
        Assert.assertNotNull(engine);

        // Check that no settings were changed
        Assert.assertEquals(-1, engine.getTimeout1());
        Assert.assertEquals(-1, engine.getTimeout2());
        Assert.assertTrue(engine.getAllowCompression());
        Assert.assertNull(engine.getClient());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void query_service_context_application_02() {
        // This test requires us to set some authentication credentials for the
        // service
        Map<String, Context> serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        if (serviceContextMap == null) {
            ARQ.getContext().put(Service.serviceContext, new HashMap<String, Context>());
            serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        }
        if (serviceContextMap.get(SERVICE) == null) {
            serviceContextMap.put(SERVICE, new Context(ARQ.getContext()));
        }
        Context serviceContext = serviceContextMap.get(SERVICE);
        try {
            HttpClient testClient = HttpClients.custom().build();
            serviceContext.put(Service.queryClient, testClient);

            Query q = QueryFactory.create("ASK { }");
            QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(SERVICE, q);
            Assert.assertNotNull(engine);

            // Check that no settings were changed
            Assert.assertEquals(-1, engine.getTimeout1());
            Assert.assertEquals(-1, engine.getTimeout2());
            Assert.assertTrue(engine.getAllowCompression());
            Assert.assertEquals(testClient, engine.getClient());

        } finally {
            serviceContext.remove(Service.queryClient);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void query_service_context_application_03() {
        // This test requires us to set some timeouts for the service
        Map<String, Context> serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        if (serviceContextMap == null) {
            ARQ.getContext().put(Service.serviceContext, new HashMap<String, Context>());
            serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        }
        if (serviceContextMap.get(SERVICE) == null) {
            serviceContextMap.put(SERVICE, new Context(ARQ.getContext()));
        }
        Context serviceContext = serviceContextMap.get(SERVICE);
        try {
            serviceContext.put(Service.queryTimeout, "10");

            Query q = QueryFactory.create("ASK { }");
            QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(SERVICE, q);
            Assert.assertNotNull(engine);

            // Check that no settings were changed
            Assert.assertEquals(-1, engine.getTimeout1());
            Assert.assertEquals(10, engine.getTimeout2());
            Assert.assertTrue(engine.getAllowCompression());
            Assert.assertNull(engine.getClient());
        } finally {
            serviceContext.remove(Service.queryTimeout);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void query_service_context_application_04() {
        // This test requires us to set some timeouts for the service
        Map<String, Context> serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        if (serviceContextMap == null) {
            ARQ.getContext().put(Service.serviceContext, new HashMap<String, Context>());
            serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        }
        if (serviceContextMap.get(SERVICE) == null) {
            serviceContextMap.put(SERVICE, new Context(ARQ.getContext()));
        }
        Context serviceContext = serviceContextMap.get(SERVICE);
        try {
            serviceContext.put(Service.queryTimeout, "10,20");

            Query q = QueryFactory.create("ASK { }");
            QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(SERVICE, q);
            Assert.assertNotNull(engine);

            // Check that no settings were changed
            Assert.assertEquals(20, engine.getTimeout1());
            Assert.assertEquals(10, engine.getTimeout2());
            Assert.assertTrue(engine.getAllowCompression());
            Assert.assertNull(engine.getClient());
        } finally {
            serviceContext.remove(Service.queryTimeout);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void query_service_context_application_05() {
        // This test requires us to set that GZip and Deflate are permitted
        Map<String, Context> serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        if (serviceContextMap == null) {
            ARQ.getContext().put(Service.serviceContext, new HashMap<String, Context>());
            serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        }
        if (serviceContextMap.get(SERVICE) == null) {
            serviceContextMap.put(SERVICE, new Context(ARQ.getContext()));
        }
        Context serviceContext = serviceContextMap.get(SERVICE);
        try {
            serviceContext.put(Service.queryCompression, false);

            Query q = QueryFactory.create("ASK { }");
            QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(SERVICE, q);
            Assert.assertNotNull(engine);

            // Check that no settings were changed
            Assert.assertEquals(-1, engine.getTimeout1());
            Assert.assertEquals(-1, engine.getTimeout2());
            Assert.assertFalse(engine.getAllowCompression());
            Assert.assertNull(engine.getClient());
        } finally {
            serviceContext.remove(Service.queryCompression);
        }
    }

    @Test
    public void update_service_context_application_01() {
        // This test requires no service context to be set
        @SuppressWarnings("unchecked")
        Map<String, Context> serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        if (serviceContextMap != null) {
            serviceContextMap.remove(SERVICE);
        }

        UpdateRequest updates = UpdateFactory.create("CREATE GRAPH <http://example>");
        UpdateProcessRemoteBase engine = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, SERVICE);
        Assert.assertNotNull(engine);

        // Check that no settings were changed
        Assert.assertNull(engine.getClient());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void update_service_context_application_02() {
        // This test requires no service context to be set
        Map<String, Context> serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        if (serviceContextMap == null) {
            ARQ.getContext().put(Service.serviceContext, new HashMap<String, Context>());
            serviceContextMap = (Map<String, Context>) ARQ.getContext().get(Service.serviceContext);
        }
        if (serviceContextMap.get(SERVICE) == null) {
            serviceContextMap.put(SERVICE, new Context(ARQ.getContext()));
        }
        Context serviceContext = serviceContextMap.get(SERVICE);
        try {
            HttpClient testClient = HttpClients.custom().build();
            serviceContext.put(Service.queryClient, testClient);

            UpdateRequest updates = UpdateFactory.create("CREATE GRAPH <http://example>");
            UpdateProcessRemoteBase engine = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, SERVICE);
            Assert.assertNotNull(engine);

            // Check that client settings were changed
            Assert.assertEquals(testClient, engine.getClient());

        } finally {
            serviceContext.remove(Service.queryClient);
        }
    }
}

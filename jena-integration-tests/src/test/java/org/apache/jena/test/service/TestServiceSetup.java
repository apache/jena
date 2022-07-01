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

package org.apache.jena.test.service;

import java.net.SocketException ;
import java.net.UnknownHostException;
import java.net.http.HttpConnectTimeoutException;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpService ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.exec.http.Service;
import org.apache.jena.sparql.util.Context ;
import org.junit.AfterClass ;
import org.junit.Assert ;
import org.junit.BeforeClass ;
import org.junit.Test ;

/**
 * This test suite does external network traffic to a non-existence endpoint..
 * It causes INFO level messages if the network interface is not reachable.
 * It does not cause tests to fail.
 */
public class TestServiceSetup {
    private static final String SERVICE = "http://example.com:40000/";

    private static Object value ;

    @BeforeClass public static void recordContextState() { value = ARQ.getContext().get(Service.oldServiceContext) ; }
    @AfterClass public static void restoreContextState() { ARQ.getContext().set(Service.oldServiceContext, value) ; }

    @Test
    public void testNumericTimeout() {
        BasicPattern basicPattern = new BasicPattern();
        basicPattern.add(Triple.ANY);
        Node serviceNode = NodeFactory.createURI(SERVICE);
        OpService opService = new OpService(serviceNode, new OpBGP(basicPattern), false);

        Context context = new Context();
        ARQ.setNormalMode(context);

        context.set(Service.httpQueryTimeout, 10);

        try {
            Service.exec(opService, context);
            Assert.fail("Expected QueryExceptionHTTP");
        } catch (QueryExceptionHTTP expected) {
            Throwable thrown = expected.getCause() ;
            if ( thrown instanceof SocketException || thrown instanceof HttpConnectTimeoutException || thrown instanceof UnknownHostException )  {
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

        context.set(Service.httpQueryTimeout, "10");

        try {
            Service.exec(opService, context);
            Assert.fail("Expected QueryExceptionHTTP");
        } catch (QueryExceptionHTTP expected) {
            Throwable thrown = expected.getCause() ;
            if ( thrown instanceof SocketException || thrown instanceof HttpConnectTimeoutException || thrown instanceof UnknownHostException )  {
                // expected
            } else {
                Assert.fail(String.format("Expected SocketException or HttpConnectTimeoutException, instead got: %s %s",
                                          thrown.getClass().getName(),
                                          thrown.getMessage()));
            }
        }
    }
}

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

package org.apache.jena.jdbc.remote.connections;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.fuseki.ServerTest;
import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.remote.connections.RemoteEndpointConnection;
import org.apache.jena.jdbc.utils.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.hp.hpl.jena.query.Dataset;

/**
 * Tests for the {@link RemoteEndpointConnection} where we force the default
 * graph to be a named graph and use connection URL parameters to ensure desired
 * default graph is used
 * 
 */
public class TestRemoteEndpointConnectionWithGraphUris extends AbstractRemoteEndpointConnectionTests {

    /**
     * Constant used for default graph URI in these tests
     */
    private static final String DEFAULT_GRAPH_URI = "http://example.org/defaultGraph";

    /**
     * Setup for the tests by allocating a Fuseki instance to work with
     */
    @BeforeClass
    public static void setup() {
        ServerTest.allocServer();
    }

    /**
     * Clean up after each test by resetting the Fuseki instance
     */
    @After
    public void cleanupTest() {
        ServerTest.resetServer();
    }

    /**
     * Clean up after tests by de-allocating the Fuseki instance
     */
    @AfterClass
    public static void cleanup() {
        ServerTest.freeServer();
    }

    @Override
    protected boolean supportsTimeouts() {
        // While timeouts are supported they are unreliable for remote
        // connections
        return false;
    }

    @Override
    protected JenaConnection getConnection() throws SQLException {
        List<String> defaultGraphs = new ArrayList<String>();
        defaultGraphs.add(DEFAULT_GRAPH_URI);
        return new RemoteEndpointConnection(ServerTest.serviceQuery, ServerTest.serviceUpdate, defaultGraphs, null,
                defaultGraphs, null, null, JenaConnection.DEFAULT_HOLDABILITY, JdbcCompatibility.DEFAULT, null, null);
    }

    @Override
    protected JenaConnection getConnection(Dataset ds) throws SQLException {
        List<String> defaultGraphs = new ArrayList<String>();
        defaultGraphs.add(DEFAULT_GRAPH_URI);
        List<String> namedGraphs = new ArrayList<String>();
        Iterator<String> names = ds.listNames();
        while (names.hasNext()) {
            String name = names.next();
            if (!DEFAULT_GRAPH_URI.equals(name)) {
                namedGraphs.add(name);
            }
        }

        // Set up the dataset
        ds = TestUtils.renameGraph(ds, null, DEFAULT_GRAPH_URI);
        Assert.assertEquals(0, ds.getDefaultModel().size());
        TestUtils.copyToRemoteDataset(ds, ServerTest.serviceREST);
        return new RemoteEndpointConnection(ServerTest.serviceQuery, ServerTest.serviceUpdate, defaultGraphs, namedGraphs,
                defaultGraphs, namedGraphs, null, JenaConnection.DEFAULT_HOLDABILITY, JdbcCompatibility.DEFAULT, null, null);
    }

    @Override
    protected boolean usesNamedGraphAsDefault() {
        return true;
    }

    @Override
    protected String getDefaultGraphName() throws SQLException {
        return DEFAULT_GRAPH_URI;
    }

}

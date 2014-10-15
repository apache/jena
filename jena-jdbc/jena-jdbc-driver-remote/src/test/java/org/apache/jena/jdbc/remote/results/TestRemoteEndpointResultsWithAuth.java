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

package org.apache.jena.jdbc.remote.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.fuseki.ServerTest;
import org.apache.jena.fuseki.server.FusekiConfig;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.apache.jena.fuseki.server.ServerConfig;
import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.remote.connections.RemoteEndpointConnection;
import org.apache.jena.jdbc.utils.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.modify.request.Target;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;

/**
 * Tests result sets from a remote endpoint
 * 
 */
@Ignore
public class TestRemoteEndpointResultsWithAuth extends AbstractRemoteEndpointResultSetTests {

    private static RemoteEndpointConnection connection;

    private static String USER = "test";
    private static String PASSWORD = "letmein";
    private static File realmFile;
    private static SPARQLServer server;
    private static HttpAuthenticator authenticator;

    /**
     * Setup for the tests by allocating a Fuseki instance to work with
     * 
     * @throws SQLException
     * @throws IOException
     */
    @BeforeClass
    public static void setup() throws SQLException, IOException {
        authenticator = new SimpleAuthenticator(USER, PASSWORD.toCharArray());

        realmFile = File.createTempFile("realm", ".properties");

        FileWriter writer = new FileWriter(realmFile);
        writer.write(USER + ": " + PASSWORD + ", fuseki\n");
        writer.close();

        DatasetGraph dsg = DatasetGraphFactory.createMem();
        // This must agree with ServerTest
        ServerConfig conf = FusekiConfig.defaultConfiguration(ServerTest.datasetPath, dsg, true, false);
        conf.port = ServerTest.port;
        conf.pagesPort = ServerTest.port;
        conf.authConfigFile = realmFile.getAbsolutePath();

        server = new SPARQLServer(conf);
        server.start();

        connection = new RemoteEndpointConnection(ServerTest.serviceQuery, ServerTest.serviceUpdate, null, null, null, null,
                authenticator, JenaConnection.DEFAULT_HOLDABILITY, JdbcCompatibility.DEFAULT, null, null);
        connection.setJdbcCompatibilityLevel(JdbcCompatibility.HIGH);
    }

    /**
     * Clean up after each test by resetting the Fuseki instance
     * @throws InterruptedException 
     */
    @After
    public void cleanupTest() throws InterruptedException {
        Update clearRequest = new UpdateDrop(Target.ALL) ;
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(clearRequest, ServerTest.serviceUpdate, authenticator) ;
        proc.execute() ;
    }

    /**
     * Clean up after tests by de-allocating the Fuseki instance
     * 
     * @throws SQLException
     * @throws InterruptedException 
     */
    @AfterClass
    public static void cleanup() throws SQLException, InterruptedException {
        
        // Sleep attempts to avoid a intermittent timing issue on the build server that can result in hung builds
        
        connection.close();
        realmFile.delete();
        server.stop();
    }

    @Override
    protected ResultSet createResults(Dataset ds, String query) throws SQLException {
        return createResults(ds, query, ResultSet.TYPE_FORWARD_ONLY);
    }

    @Override
    protected ResultSet createResults(Dataset ds, String query, int resultSetType) throws SQLException {
        TestUtils.copyToRemoteDataset(ds, ServerTest.serviceREST, authenticator);
        Statement stmt = connection.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY);
        return stmt.executeQuery(query);
    }
}

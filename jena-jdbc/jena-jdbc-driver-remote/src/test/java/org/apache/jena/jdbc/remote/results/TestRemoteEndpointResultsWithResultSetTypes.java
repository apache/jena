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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.remote.FusekiJdbcTestServer;
import org.apache.jena.jdbc.remote.connections.RemoteEndpointConnection;
import org.apache.jena.jdbc.remote.utils.TestJdbcRemoteUtils;
import org.apache.jena.query.Dataset ;
import org.apache.jena.riot.WebContent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before ;
import org.junit.BeforeClass;

/**
 * Tests result sets from a remote endpoint
 *
 */
public class TestRemoteEndpointResultsWithResultSetTypes extends AbstractRemoteEndpointResultSetTests {

    //@BeforeClass public static void ctlBeforeClass() { FusekiTestServer.ctlBeforeClass(); }
    //@AfterClass  public static void ctlAfterClass()  { FusekiTestServer.ctlAfterClass(); }
    @Before      public void ctlBeforeTest()  { FusekiJdbcTestServer.ctlBeforeTest(); }
    @After       public void ctlAfterTest()   { FusekiJdbcTestServer.ctlAfterTest(); } 

    private static RemoteEndpointConnection connection;
    
    /**
     * Setup for the tests by allocating a Fuseki instance to work with
     * @throws SQLException 
     */
    @BeforeClass
    public static void setup() throws SQLException {
        FusekiJdbcTestServer.ctlBeforeClass();
        connection = new RemoteEndpointConnection(FusekiJdbcTestServer.serviceQuery(), FusekiJdbcTestServer.serviceUpdate(), null, null, null, null, null, JenaConnection.DEFAULT_HOLDABILITY, JdbcCompatibility.DEFAULT, WebContent.contentTypeTextTSV, WebContent.contentTypeRDFJSON);
        connection.setJdbcCompatibilityLevel(JdbcCompatibility.HIGH);
    }
    
    /**
     * Clean up after tests by de-allocating the Fuseki instance
     * @throws SQLException 
     */
    @AfterClass
    public static void cleanup() throws SQLException {
        connection.close();
        FusekiJdbcTestServer.ctlAfterClass();
    }

    @Override
    protected ResultSet createResults(Dataset ds, String query) throws SQLException {
        return createResults(ds, query, ResultSet.TYPE_FORWARD_ONLY);
    }
    
    @Override
    protected ResultSet createResults(Dataset ds, String query, int resultSetType) throws SQLException {
        TestJdbcRemoteUtils.copyToRemoteDataset(ds, FusekiJdbcTestServer.serviceGSP());
        Statement stmt = connection.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY);
        return stmt.executeQuery(query);
    }
}

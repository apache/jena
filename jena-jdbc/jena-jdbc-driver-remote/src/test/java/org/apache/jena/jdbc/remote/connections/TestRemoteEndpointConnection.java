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

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.remote.FusekiTestServer;
import org.apache.jena.jdbc.utils.TestUtils;
import org.apache.jena.query.Dataset ;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before ;
import org.junit.BeforeClass;

/**
 * Tests for the {@link RemoteEndpointConnection}
 *
 */
public class TestRemoteEndpointConnection extends AbstractRemoteEndpointConnectionTests {
        
    @BeforeClass public static void ctlBeforeClass() { FusekiTestServer.ctlBeforeClass(); }
    @AfterClass  public static void ctlAfterClass()  { FusekiTestServer.ctlAfterClass(); }
    @Before      public void ctlBeforeTest()         { FusekiTestServer.ctlBeforeTest(); }
    @After       public void ctlAfterTest()          { FusekiTestServer.ctlAfterTest(); } 
    
    @Override
    protected boolean supportsTimeouts() {
        // While timeouts are supported they are unreliable for remote connections
        return false;
    }
    
    @Override
    protected JenaConnection getConnection() throws SQLException {
        return new RemoteEndpointConnection(FusekiTestServer.serviceQuery(), FusekiTestServer.serviceUpdate(), JenaConnection.DEFAULT_HOLDABILITY, JdbcCompatibility.DEFAULT);
    }

    @Override
    protected JenaConnection getConnection(Dataset ds) throws SQLException {
        // Set up the dataset
        TestUtils.copyToRemoteDataset(ds, FusekiTestServer.serviceGSP());
        return new RemoteEndpointConnection(FusekiTestServer.serviceQuery(), FusekiTestServer.serviceUpdate(), JenaConnection.DEFAULT_HOLDABILITY, JdbcCompatibility.DEFAULT);
    }


}

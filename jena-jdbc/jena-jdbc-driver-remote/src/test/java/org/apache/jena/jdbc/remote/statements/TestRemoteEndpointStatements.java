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

package org.apache.jena.jdbc.remote.statements;

import java.sql.SQLException;

import org.apache.jena.fuseki.embedded.FusekiTestServer ;
import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.remote.connections.RemoteEndpointConnection;
import org.apache.jena.jdbc.statements.AbstractJenaStatementTests;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before ;
import org.junit.BeforeClass;

/**
 * Tests for the {@link RemoteEndpointStatement}
 *
 */
public class TestRemoteEndpointStatements extends AbstractJenaStatementTests {

    @BeforeClass public static void ctlBeforeClass() { FusekiTestServer.ctlBeforeClass(); }
    @AfterClass  public static void ctlAfterClass()  { FusekiTestServer.ctlAfterClass(); }
    @Before      public void ctlBeforeTest()         { FusekiTestServer.ctlBeforeTest(); }
    @After       public void ctlAfterTest()          { FusekiTestServer.ctlAfterTest(); } 

    @Override
    protected JenaConnection getConnection() throws SQLException {
        return new RemoteEndpointConnection(FusekiTestServer.serviceQuery(), FusekiTestServer.serviceUpdate(), JenaConnection.DEFAULT_HOLDABILITY, JdbcCompatibility.DEFAULT);
    }


}

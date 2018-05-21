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
package org.apache.jena.jdbc.remote;

import org.apache.jena.fuseki.embedded.FusekiTestServer ;
import org.apache.jena.jdbc.remote.connections.TestRemoteEndpointConnection;
import org.apache.jena.jdbc.remote.connections.TestRemoteEndpointConnectionWithAuth;
import org.apache.jena.jdbc.remote.connections.TestRemoteEndpointConnectionWithGraphUris;
import org.apache.jena.jdbc.remote.connections.TestRemoteEndpointConnectionWithResultSetTypes;
import org.apache.jena.jdbc.remote.metadata.TestRemoteConnectionMetadata;
import org.apache.jena.jdbc.remote.results.TestRemoteEndpointResults;
import org.apache.jena.jdbc.remote.results.TestRemoteEndpointResultsWithAuth;
import org.apache.jena.jdbc.remote.results.TestRemoteEndpointResultsWithGraphUris;
import org.apache.jena.jdbc.remote.results.TestRemoteEndpointResultsWithResultSetTypes;
import org.apache.jena.jdbc.remote.statements.TestRemoteEndpointStatements;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test Suite for the Jena JDBC Remote Endpoint driver
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestRemoteEndpointDriver.class,
    
    TestRemoteEndpointConnection.class,
    TestRemoteEndpointConnectionWithAuth.class,
    TestRemoteEndpointConnectionWithGraphUris.class,
    TestRemoteEndpointConnectionWithResultSetTypes.class
    ,
    TestRemoteConnectionMetadata.class,
    TestRemoteEndpointStatements.class
    ,
    TestRemoteEndpointResults.class,
    TestRemoteEndpointResultsWithAuth.class,
    TestRemoteEndpointResultsWithGraphUris.class,
    TestRemoteEndpointResultsWithResultSetTypes.class
})


public class TS_JdbcDriverRemote {

    @BeforeClass
    public static void beforeClassAbstract1() {
        FusekiTestServer.ctlBeforeTestSuite() ;
    }

    @AfterClass
    public static void afterClassAbstract1() {
        FusekiTestServer.ctlAfterTestSuite() ;
    }
}

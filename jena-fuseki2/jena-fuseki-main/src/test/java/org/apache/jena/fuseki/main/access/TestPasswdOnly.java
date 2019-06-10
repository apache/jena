/*
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

package org.apache.jena.fuseki.main.access;

import static org.apache.jena.fuseki.main.FusekiTestLib.expectOK;
import static org.apache.jena.fuseki.main.FusekiTestLib.expectQuery401;

import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.LibSec;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.web.AuthSetup;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test a server with a password file and no other auth policies. Should become a
 * requirement to log in by no other restrictions.
 */
public class TestPasswdOnly {
    protected static FusekiServer server; 
    protected static int port;
    
    private static AuthSetup auth1 = new AuthSetup("localhost", port, "user1", "pw1", null);
    
    @BeforeClass public static void beforeClass () {
        port = WebLib.choosePort();
        server = FusekiServer.create()
            //.verbose(true)
            .port(port)
            .add("/db", DatasetGraphFactory.createTxnMem())
            .passwordFile("testing/Access/passwd")
            // Should be default.
            //.serverAuthPolicy(Auth.ANY_USER)
            .build();
        server.start();
    }
    
    @AfterClass public static void afterClass () {
        server.stop();
    }
    
    // Bounced by Jetty.
    @Test(expected=QueryExceptionHTTP.class)
    public void passwd_no_user_A() {
        try (RDFConnection conn = RDFConnectionFactory.connect("http://localhost:" + port + "/db")) {
            try ( QueryExecution qExec = conn.query("ASK{}") ) {
                QueryExecUtils.executeQuery(qExec);
            }
        }
    }
    
    @Test
    public void passwd_no_user_B() {
        expectQuery401(() -> {
            try(RDFConnection conn = RDFConnectionFactory.connect("http://localhost:" + port + "/db")) {
                conn.queryAsk("ASK{}");
            }
        });
    }
    
    @Test public void passwd_user1() {
        // Works!
        AuthSetup auth1 = new AuthSetup("localhost", port, "user1", "pw1", null);
        expectOK(() -> {
            LibSec.withAuth("http://localhost:"+port+"/db", auth1, conn -> {
                conn.queryAsk("ASK{}");
            });
        });
    }
    
    @Test public void passwd_user_bad() {
        AuthSetup auth1 = new AuthSetup("localhost", port, "user99", "pw1", null);
        expectQuery401(
            () -> {
            LibSec.withAuth("http://localhost:"+port+"/db", auth1, conn -> {
                conn.queryAsk("ASK{}");
            });
        });
    }
}

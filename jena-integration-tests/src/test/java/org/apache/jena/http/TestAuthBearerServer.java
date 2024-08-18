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

package org.apache.jena.http;

import static org.apache.jena.http.AuthBearerTestLib.attempt;
import static org.junit.Assert.assertEquals;

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.auth.AuthBearerFilter;
import org.apache.jena.fuseki.main.auth.BearerMode;
import org.apache.jena.http.auth.AuthEnv;
import org.junit.After;
import org.junit.Test;

/**
 * Test server side functionality of bearer auth.
 */
public class TestAuthBearerServer {

    private static String databaseACL = "database_acl";
    private static String databasePlain = "database_no_acl";
    private static FusekiServer server(AuthBearerFilter authBearerFilter) {
        return FusekiServer.create()
                .port(0)
                .auth(AuthScheme.BEARER)
                .parseConfigFile("src/test/files/Fuseki/config-bearer.ttl")
                .addFilter("/*", authBearerFilter)
                .start();
    }

    @After
    public void afterTest() {
        AuthEnv.get().clearAuthEnv();
    }

    @Test public void testTokens() {
        String jwt = AuthBearerTestLib.generateTestJWT("user1");
        String u = AuthBearerTestLib.subjectFromEncodedJWT(jwt);
        assertEquals("user1",u);
    }

    @Test public void modeBearerDefault() {
        FusekiServer server = server(new AuthBearerFilter(AuthBearerTestLib::subjectFromEncodedJWT));
        testModeBearerRequired(server);
    }

    @Test public void modeBearerRequired() {
        FusekiServer server = server(new AuthBearerFilter(AuthBearerTestLib::subjectFromEncodedJWT, BearerMode.REQUIRED));
        testModeBearerRequired(server);
    }

    private static void testModeBearerRequired(FusekiServer server) {
        String baseURL = "http://localhost:"+server.getHttpPort()+"/";

        try {
            String JWT_user1 = AuthBearerTestLib.generateTestJWT("user1");
            String JWT_user2 = AuthBearerTestLib.generateTestJWT("noSuchUser");
            String URL1 = baseURL+databaseACL;
            String URL2 = baseURL+databasePlain;

            // Global setting
            AuthEnv.get().setBearerTokenProvider( (uri, challenge)-> uri.equals(URL1) ? JWT_user1 : null);

            attempt(URL1, null, AuthBearerTestLib.Expect.SUCCESS);
            AuthEnv.get().setBearerTokenProvider( null );

            AuthEnv.get().clearAuthEnv();

            attempt(URL1, JWT_user1, AuthBearerTestLib.Expect.SUCCESS);

            // User not authorized for this database
            attempt(URL1, JWT_user2, AuthBearerTestLib.Expect.REJECT);

            attempt(URL1, null,      AuthBearerTestLib.Expect.REJECT);

            // User not authorized for this database by basic auth
            AuthBearerTestLib.attemptBasic(URL1, "users1", "pw1", AuthBearerTestLib.Expect.REJECT);

            attempt(URL2, JWT_user1, AuthBearerTestLib.Expect.SUCCESS);

            // Fails - bearer required.
            attempt(URL2, null, AuthBearerTestLib.Expect.REJECT);

            AuthBearerTestLib.attemptBasic(URL2, "users1", "pw1", AuthBearerTestLib.Expect.REJECT);

        } finally { server.stop(); }
    }

    @Test public void modeBearerOptional() {
        FusekiServer server = server(new AuthBearerFilter(AuthBearerTestLib::subjectFromEncodedJWT, BearerMode.OPTIONAL));

        try {
            String baseURL = "http://localhost:"+server.getHttpPort()+"/";
            String JWT_user1 = AuthBearerTestLib.generateTestJWT("user1");
            String JWT_user2 = AuthBearerTestLib.generateTestJWT("noSuchUser");

            String URL1 = baseURL+databaseACL;
            String URL2 = baseURL+databasePlain;

            attempt(URL1, JWT_user1, AuthBearerTestLib.Expect.SUCCESS);

            attempt(URL1, JWT_user2, AuthBearerTestLib.Expect.REJECT);

            attempt(URL1, null,      AuthBearerTestLib.Expect.REJECT);

            AuthBearerTestLib.attemptBasic(URL1, "users1", "pw1", AuthBearerTestLib.Expect.REJECT);

            attempt(URL2, JWT_user1, AuthBearerTestLib.Expect.SUCCESS);

            attempt(URL2, null,      AuthBearerTestLib.Expect.SUCCESS);

            // Optional bearer auth - pass through to the non-ACL setup.
            AuthBearerTestLib.attemptBasic(URL2, "users1", "pw1", AuthBearerTestLib.Expect.SUCCESS);

        } finally { server.stop(); }
    }

    @Test public void modeBearerNone() {
        FusekiServer server = server(new AuthBearerFilter(AuthBearerTestLib::subjectFromEncodedJWT, BearerMode.NONE));

        try {
            String baseURL = "http://localhost:"+server.getHttpPort()+"/";
            String JWT_user1 = AuthBearerTestLib.generateTestJWT("user1");
            String URL1 = baseURL+databaseACL;
            String URL2 = baseURL+databasePlain;

            // No bearer allowed.
            attempt(URL1, JWT_user1, AuthBearerTestLib.Expect.REJECT);

            // No auth setup for basic.
            AuthBearerTestLib.attemptBasic(URL1, "users1", "pw1", AuthBearerTestLib.Expect.REJECT);

            // No bearer allowed.
            attempt(URL2, JWT_user1, AuthBearerTestLib.Expect.REJECT);

            // No setting
            attempt(URL2, null, AuthBearerTestLib.Expect.SUCCESS);

            AuthBearerTestLib.attemptBasic(URL2, "users1", "pw1", AuthBearerTestLib.Expect.SUCCESS);

        } finally { server.stop(); }
    }
}

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

import static org.junit.Assert.fail;

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.auth.AuthBearerFilter;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.junit.Test;

/**
 * Test server side functionality of bearer auth.
 */
public class TestAuthBearerServer {

    @Test public void modeBearerAlways() {
        FusekiServer server = FusekiServer.create()
                .port(0)
                .auth(AuthScheme.BEARER)
                .parseConfigFile("src/test/files/Fuseki/config-bearer.ttl")
                // Default is AuthBearerFilter.BearerMode.REQUIRED
                .addFilter("/*", new AuthBearerFilter(AuthBearerTestLib::subjectFromEncodedJWT))
                .start();
        String baseURL = "http://localhost:"+server.getHttpPort()+"/";

        try {
            String JWT_user1 = AuthBearerTestLib.generateTestToken("user1");
            String JWT_user2 = AuthBearerTestLib.generateTestToken("noSuchUser");
            String URL1 = baseURL+"database1";
            String URL2 = baseURL+"database2";

            // 401
            AuthEnv.get().setBearerTokenProvider( (uri, challenge)-> uri.equals(URL1) ? JWT_user1 : null);
            //System.out.println("database1, with user1 token (yes/401)");
            attempt(URL1, null, true);
            AuthEnv.get().setBearerTokenProvider( null );

            AuthEnv.get().clearAuthEnv();

            //System.out.println("database1, with user1 token (yes)");
            attempt(URL1, JWT_user1, true);
            //System.out.println("database1, with user2 token (no)");
            attempt(URL1, JWT_user2, false);
            //System.out.println("database1, without token (no)");
            attempt(URL1, null, false);

            //System.out.println("database2, with user1 token (yes)");
            attempt(URL2, JWT_user1, true);
            //System.out.println("database2, with no token (no)");
            // fails - bearer required.
            attempt(URL2, null, false);
        } finally { server.stop(); }
    }

    @Test public void modePossibleBearerTesting() {
        FusekiServer server = FusekiServer.create()
                .port(0)
                .auth(AuthScheme.BEARER)
                .parseConfigFile("src/test/files/Fuseki/config-bearer.ttl")
                .addFilter("/*", new AuthBearerFilter(AuthBearerTestLib::subjectFromEncodedJWT, AuthBearerFilter.BearerMode.OPTIONAL))
                .start();
        try {
            String baseURL = "http://localhost:"+server.getHttpPort()+"/";
            String JWT_user1 = AuthBearerTestLib.generateTestToken("user1");
            String JWT_user2 = AuthBearerTestLib.generateTestToken("noSuchUser");

            String URL1 = baseURL+"database1";
            String URL2 = baseURL+"database2";

            //System.out.println("database1, with user1 token (yes)");
            attempt(URL1, JWT_user1, true);
            //System.out.println("database1, with user2 token (no)");
            attempt(URL1, JWT_user2, false);
            //System.out.println("database1, without token (no)");
            attempt(URL1, null, false);

            //System.out.println("database2, with user1 token (yes)");
            attempt(URL2, JWT_user1, true);
            //System.out.println("database2, without token (yes)");
            attempt(URL2, null, true);

        } finally { server.stop(); }
    }

    private static void attempt(String URL, String JWT, boolean expectedToSucceed) {
        if ( JWT != null )
            AuthEnv.get().setBearerToken(URL, JWT);

        try {
            attempt(URL, expectedToSucceed);
        } finally {
            if ( JWT != null )
                AuthEnv.get().setBearerToken(URL, null);
        }
    }

    private static void attempt(String URL, boolean expectedToSucceed) {
        try {
            boolean b = QueryExecHTTP.service(URL)
                    .query("ASK{}")
                    .ask();
            if ( ! expectedToSucceed )
                fail("Expected the operation to be rejected");
        } catch (RuntimeException ex) {
            if ( expectedToSucceed )
                fail("Expected the operation to succeed");
        }
    }
}

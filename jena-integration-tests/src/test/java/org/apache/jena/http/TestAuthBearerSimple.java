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
import static org.apache.jena.http.AuthBearerTestLib.attemptBasic;

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.auth.AuthBearerFilter;
import org.apache.jena.fuseki.main.auth.SimpleBearer;
import org.apache.jena.http.AuthBearerTestLib.Expect;
import org.apache.jena.http.auth.AuthEnv;
import org.junit.After;
import org.junit.Test;

/**
 * {@link SimpleBearer} is a simple to use bear toke authentication mechanism
 * <p>
 * {@code Bearer: base64("user:NAME")}
 * </p>
 *  * It is easy to setup and use in development and testing.
 */
public class TestAuthBearerSimple {
    private static String databaseACL = "database_acl";
    private static String databasePlain = "database_no_acl";

    private static AuthBearerFilter authBearerFilter() {
        return new AuthBearerFilter(SimpleBearer::getUserFromToken64);
    }

    private static FusekiServer server() {
        return FusekiServer.create()
                .port(0)
                .auth(AuthScheme.BEARER)
                .parseConfigFile("src/test/files/Fuseki/config-bearer.ttl")
                .addFilter("/*", authBearerFilter())
                .start();
    }

    @After
    public void afterTest() {
        AuthEnv.get().clearAuthEnv();
    }

    @Test
    public void testSimpleBearer() {
        FusekiServer server = server();
        simpleBearer(server);
    }

    private static void simpleBearer(FusekiServer server) {
        String baseURL = "http://localhost:"+server.getHttpPort()+"/";

        try {
            String token_user1 = SimpleBearer.requestAuthorizationToken("user1");
            String token_user2 = SimpleBearer.requestAuthorizationToken("noSuchUser");
            String URL1 = baseURL+databaseACL;
            String URL2 = baseURL+databasePlain;

            attempt(URL1, token_user1, Expect.SUCCESS);

            // User not authorized for this database
            attempt(URL1, token_user2, Expect.REJECT);

            attempt(URL1, null,        Expect.REJECT);

            // User not authorized for this database by basic auth
            attemptBasic(URL1, "users1", "pw1", Expect.REJECT);

            attempt(URL2, token_user1, Expect.SUCCESS);

            // Fails - bearer required.
            attempt(URL2, null, Expect.REJECT);

            // Bearer required.
            attemptBasic(URL2, "users1", "pw1", Expect.REJECT);

        } finally { server.stop(); }
    }
}

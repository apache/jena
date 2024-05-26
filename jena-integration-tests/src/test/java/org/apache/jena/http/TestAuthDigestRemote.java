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

import java.net.URI;
import java.util.Objects;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.JettySecurityLib;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Digest authentication.
 * Digest auth is not provided by java.net.http,
 * so {@code Authenticator} on the {@code HttpClient} does not work.
 * Jena has to implement it itself (in AuthLib).
 */
public class TestAuthDigestRemote extends AbstractTestAuthRemote {
    private static String user = "user";
    private static String password = "password";

    private static FusekiServer server = null;
    private static String dsEndpoint;
    private static URI dsEndpointURI;

    @Override
    protected String endpoint() {
        return dsEndpoint;
    }

    @Override
    protected URI endpointURI() {
        return dsEndpointURI;
    }

    @Override
    protected String user() {
        return user;
    }

    @Override
    protected String password() {
        return password;
    }

    @BeforeClass public static void beforeClass() {
        server = server("/ds", DatasetGraphFactory.createTxnMem(), user, password);
    }

    private static FusekiServer server(String dsName, DatasetGraph dsg, String user, String password) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(password);
        FusekiServer.Builder builder = FusekiServer.create()
            .port(0)
            .enablePing(true)
            .auth(AuthScheme.DIGEST)
            .add(dsName, dsg);
        if ( user != null ) {
            UserStore userStore = JettySecurityLib.makeUserStore(user, password);
            SecurityHandler sh = JettySecurityLib.makeSecurityHandler("TripleStore",  userStore, AuthScheme.DIGEST);
            builder.securityHandler(sh)
                   .serverAuthPolicy(Auth.policyAllowSpecific(user));
        }
        FusekiServer server = builder.build();
        server.start();
        dsEndpoint = "http://localhost:"+server.getHttpPort()+"/ds";
        dsEndpointURI = URI.create(dsEndpoint);
        return server;
    }

    //@Before public void before() {}
    @After public void after() {
        AuthEnv.get().unregisterUsernamePassword(dsEndpointURI);
    }

    @AfterClass public static void afterClass() {
        dsEndpoint = null;
        dsEndpointURI = null;
        if ( server == null )
            return;
        try {
            server.stop();
            server = null;
        } catch (Throwable th) {
            Log.warn(TestAuthDigestRemote.class, "Exception in test suite shutdown", th);
        }
    }
}

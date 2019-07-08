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

package org.apache.jena.fuseki.main.old;

import static org.apache.jena.fuseki.main.FusekiTestLib.expectQuery401;
import static org.apache.jena.fuseki.main.old.FusekiTestAuth.assertAuthHttpException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.web.HttpOp;
import org.eclipse.jetty.security.SecurityHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFusekiTestAuthOld {
    // ** Old tests
    // Predates full access control support in Fuseki.
    // Superseded.

    private static String USER      = "user1234";
    private static String PASSWORD  = "password1234";

    @BeforeClass
    public static void ctlBeforeClass() {
        SecurityHandler sh = FusekiTestAuth.makeSimpleSecurityHandler("/*", USER, PASSWORD);
        FusekiTestAuth.setupServer(false, sh);
    }

    @AfterClass
    public static void ctlAfterClass() {
        FusekiTestAuth.teardownServer();
        HttpOp.setDefaultHttpClient(HttpOp.createPoolingHttpClient());
    }

    @Test(expected=HttpException.class)
    public void testServer_auth_no_auth() {
        expectQuery401(()->{
            try ( TypedInputStream in = HttpOp.execHttpGet(FusekiTestAuth.urlDataset(), "*/*") ){}
        });
    }

    @Test public void testServer_auth() {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(USER, PASSWORD);
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        try ( TypedInputStream in = HttpOp.execHttpGet(FusekiTestAuth.urlDataset(), "*/*", client, null) ) {}
    }

    @Test(expected=HttpException.class)
    public void testServer_auth_bad_user() {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials("USERUSER", PASSWORD);
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        try ( TypedInputStream in = HttpOp.execHttpGet(FusekiTestAuth.urlDataset(), "*/*", client, null) ) {}
        catch (HttpException ex) { throw assertAuthHttpException(ex); }
    }

    @Test(expected=HttpException.class)
    public void testServer_auth_bad_password() {
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(USER, "WRONG"));
        HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProv).build();

        try ( TypedInputStream in = HttpOp.execHttpGet(FusekiTestAuth.urlDataset(), "*/*", client, null) ) {}
        catch (HttpException ex) { throw assertAuthHttpException(ex); }
    }
}

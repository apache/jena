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

package org.apache.jena.fuseki;

import static org.apache.http.auth.AuthScope.ANY;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.engine.http.Service_AHC;
import org.apache.jena.sparql.modify.UpdateProcessRemote;
import org.apache.jena.sparql.modify.UpdateProcessRemoteBase;
import org.apache.jena.sparql.modify.UpdateProcessRemoteForm;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests Fuseki operation with authentication enabled.
 * This is as much a test of the Jena client libraries handling authentication.
 * These tests use Jetty-supplied authentication, not Apache Shiro.
 */
@SuppressWarnings("deprecation")
public class TestAuth_AHC {

    // Use different port etc because sometimes the previous testing servers
    // don't release ports fast enough (OS issue / Linux)
    public static final int authPort             = FusekiNetLib.choosePort();
    public static final String authUrlRoot       = "http://localhost:"+authPort+"/";
    public static final String authDatasetPath   = "/dataset";
    public static final String authServiceUpdate = "http://localhost:"+authPort+authDatasetPath+"/update";
    public static final String authServiceQuery  = "http://localhost:"+authPort+authDatasetPath+"/query";
    public static final String authServiceREST   = "http://localhost:"+authPort+authDatasetPath+"/data";
    private static File realmFile;

    /**
     * Sets up the authentication for tests
     * @throws IOException
     */
    @BeforeClass
    public static void setup() throws IOException {
        realmFile = File.createTempFile("realm", ".properties");

        try ( FileWriter writer = new FileWriter(realmFile); ) {
            writer.write("allowed: password, fuseki\n");
            writer.write("forbidden: password, other");
        }

        LogCtl.setLevel(Fuseki.serverLogName, "warn");
        LogCtl.setLevel(Fuseki.actionLogName, "warn");
        LogCtl.setLevel("org.eclipse.jetty",  "warn");

        ServerCtl.setupServer(authPort, realmFile.getAbsolutePath(), authDatasetPath, true);
    }

    /**
     * Tears down authentication test setup
     */
    @AfterClass
    public static void teardown() {
        ServerCtl.teardownServer();
        realmFile.delete();
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_01() {
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");
        // No auth credentials should result in an error
        qe.execAsk();
    }

    private static HttpClient withBasicAuth(AuthScope scope, String user, String passwd) {
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, passwd);
        provider.setCredentials(scope, credentials);
        return HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
    }

    private QueryEngineHTTP createQueryEngineHTTP(String serviceURL, String queryString) {
        return new QueryEngineHTTP(serviceURL, queryString);
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_02() {
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with bad password
        qe.setClient(withBasicAuth(ANY, "allowed", "incorrect"));
        qe.execAsk();
    }

    @Test
    public void query_with_auth_03() {
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with correct password
        qe.setClient(withBasicAuth(ANY, "allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_04() {
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with correct password BUT not in
        // correct role
        qe.setClient(withBasicAuth(ANY, "forbidden", "password"));
        qe.execAsk();
    }

    @Test
    public void query_with_auth_05() {
        // Uses auth and enables compression
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");
        qe.setAllowCompression(true);

        // Auth credentials for valid user with correct password
        qe.setClient(withBasicAuth(ANY, "allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_06() {
        // Uses auth and enables compression
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");
        qe.setAllowCompression(true);

        // Auth credentials for valid user with bad password
        qe.setClient(withBasicAuth(ANY, "allowed", "incorrect"));
        qe.execAsk();
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_07() throws URISyntaxException {
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password but scoped to
        // wrong URI
        qe.setClient(withBasicAuth(new AuthScope("example", authPort), "allowed", "password"));
        qe.execAsk();
    }

    @Test
    public void query_with_auth_08() throws URISyntaxException {
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped to
        // correct URI
        qe.setClient(withBasicAuth(new AuthScope("localhost", authPort), "allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }

    @Test
    public void query_with_auth_09() throws URISyntaxException {
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password
        qe.setClient(withBasicAuth(new AuthScope("localhost", authPort), "allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }

    @Test
    public void query_with_auth_10() {
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped
        // to correct URI
        // Provided via Service Context and its associated authenticator
        Map<String, Context> serviceContext = new HashMap<>();
        Context authContext = new Context();

        HttpClient client = withBasicAuth(ANY, "allowed", "password");
        authContext.put(Service_AHC.queryClient, client);
        serviceContext.put(authServiceQuery, authContext);
        qe.getContext().put(Service_AHC.serviceContext, serviceContext);
        Assert.assertTrue(qe.execAsk());
    }

    @Test
    public void query_with_auth_11() {
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped
        // to base URI of the actual service URL
        // Provided via Service Context and its associated authenticator
        Map<String, Context> serviceContext = new HashMap<>();
        Context authContext = new Context();

        HttpClient client = withBasicAuth(ANY, "allowed", "password");
        authContext.put(Service_AHC.queryClient, client);
        serviceContext.put(authServiceQuery, authContext);
        qe.getContext().put(Service_AHC.serviceContext, serviceContext);
        Assert.assertTrue(qe.execAsk());
    }

    @Test
    public void query_with_auth_13() throws URISyntaxException {
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped to
        // base URI of the actual service URL
        qe.setClient(withBasicAuth(new AuthScope("localhost" , authPort),"allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }

    @Test
    public void query_with_auth_14() throws URISyntaxException {
        QueryEngineHTTP qe = createQueryEngineHTTP(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped to
        // base URI of the actual service URL
        qe.setClient(withBasicAuth(new AuthScope("localhost" , authPort),"allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }

    private static UpdateProcessRemoteBase createUpdateHTTP(UpdateRequest update, String serviceURL) {
        return new UpdateProcessRemote(update, serviceURL, null);
    }

    private static UpdateProcessRemoteBase createUpdateFormHTTP(UpdateRequest update, String serviceURL) {
        return new UpdateProcessRemoteForm(update, serviceURL, null);
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_01() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = createUpdateHTTP(updates, authServiceUpdate);
        // No auth credentials should result in an error
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_02() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = createUpdateHTTP(updates, authServiceUpdate);
        // Auth credentials for valid user with bad password
        ue.setClient(withBasicAuth(ANY, "allowed", "incorrect"));
        ue.execute();
    }

    @Test
    public void update_with_auth_03() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = createUpdateHTTP(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password
        ue.setClient(withBasicAuth(ANY, "allowed", "password"));
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_04() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = createUpdateHTTP(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password BUT not in
        // correct role
        ue.setClient(withBasicAuth(ANY, "forbidden", "password"));
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_05() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = createUpdateFormHTTP(updates, authServiceUpdate);
        // No auth credentials should result in an error
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_06() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = createUpdateFormHTTP(updates, authServiceUpdate);
        // Auth credentials for valid user with bad password
        ue.setClient(withBasicAuth(ANY, "allowed", "incorrect"));
        ue.execute();
    }

    @Test
    public void update_with_auth_07() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = createUpdateFormHTTP(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password
        ue.setClient(withBasicAuth(ANY, "allowed", "password"));
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_08() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = createUpdateFormHTTP(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password BUT not in
        // correct role
        ue.setClient(withBasicAuth(ANY, "forbidden", "password"));
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_09() throws URISyntaxException {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = createUpdateHTTP(updates, authServiceUpdate);

        // Auth credentials for valid user with correct password but scoped to
        // wrong URI
        ue.setClient(withBasicAuth(new AuthScope("example" , authPort),"allowed", "password"));
        ue.execute();
    }

    @Test
    public void update_with_auth_10() throws URISyntaxException {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = createUpdateHTTP(updates, authServiceUpdate);

        // Auth credentials for valid user with correct password scoped to
        // correct URI
        ue.setClient(withBasicAuth(new AuthScope("localhost" , authPort),"allowed", "password"));
        ue.execute();
    }
}

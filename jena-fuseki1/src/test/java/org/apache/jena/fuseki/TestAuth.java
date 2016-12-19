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

import java.io.File ;
import java.io.FileWriter ;
import java.io.IOException ;
import java.net.URI ;
import java.net.URISyntaxException ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient ;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.fuseki.server.FusekiConfig ;
import org.apache.jena.fuseki.server.SPARQLServer ;
import org.apache.jena.fuseki.server.ServerConfig ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.DatasetAccessor ;
import org.apache.jena.query.DatasetAccessorFactory ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP ;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP ;
import org.apache.jena.sparql.engine.http.Service ;
import org.apache.jena.sparql.modify.UpdateProcessRemoteBase ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.junit.* ;
import org.junit.Test ;

/**
 * Tests Fuseki operation with authentication enabled
 */
public class TestAuth {
    // Use different port etc because sometimes the previous testing servers
    // don't release ports fast enough (OS issue / Linux)
    
    private static HttpClient defaultHttpClient;
    public static final int authPort             = ServerCtl.choosePort() ;
    public static final String authUrlRoot       = "http://localhost:"+authPort+"/" ;
    public static final String authDatasetPath   = "/authDataset" ;
    public static final String authServiceUpdate = "http://localhost:"+authPort+authDatasetPath+"/update" ; 
    public static final String authServiceQuery  = "http://localhost:"+authPort+authDatasetPath+"/query" ; 
    public static final String authServiceREST   = "http://localhost:"+authPort+authDatasetPath+"/data" ;
    
    private static File realmFile;
    private static SPARQLServer server;

    /**
     * Sets up the authentication for tests
     * @throws IOException
     */
    @BeforeClass
    public static void setup() throws IOException {
        // Preserve the HttpClient setup.  
        defaultHttpClient = HttpOp.getDefaultHttpClient();
        HttpOp.setDefaultHttpClient(HttpOp.createPoolingHttpClient()) ;
        
        realmFile = File.createTempFile("realm", ".properties");

        try(FileWriter writer = new FileWriter(realmFile)) {
            writer.write("allowed: password, fuseki\n");
            writer.write("forbidden: password, other");
        }

        LogCtl.setLevel(Fuseki.serverLog.getName(), "WARN");
        LogCtl.setLevel(Fuseki.requestLog.getName(), "WARN");
        LogCtl.setLevel("org.eclipse.jetty", "WARN");

        DatasetGraph dsg = DatasetGraphFactory.create();
        ServerConfig conf = FusekiConfig.defaultConfiguration(authDatasetPath, dsg, true, true);
        conf.port = authPort ;
        conf.pagesPort = authPort ;
        conf.authConfigFile = realmFile.getAbsolutePath();

        server = new SPARQLServer(conf);
        server.start();
    }

    /**
     * Tears down authentication test setup
     */
    @AfterClass
    public static void teardown() {
        server.stop();
        realmFile.delete();
        // Restore the HttpClient setup.  
        IO.close((CloseableHttpClient) HttpOp.getDefaultHttpClient()) ;
        HttpOp.setDefaultHttpClient(defaultHttpClient);
    }
    
    private static HttpClient withCreds(String uname, String password) {
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(uname, password));
        return HttpClients.custom().setDefaultCredentialsProvider(credsProv).build();
    }
    
    private static HttpClient withCreds(URI scope, String uname, String password) {
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(new AuthScope(scope.getHost(), scope.getPort()),
                new UsernamePasswordCredentials(uname, password));
        return HttpClients.custom().setDefaultCredentialsProvider(credsProv).build();
    }
    
    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_01() {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        // No auth credentials should result in an error
        qe.execAsk();   
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_02() {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with bad password
        qe.setClient(withCreds("allowed", "incorrect"));
        qe.execAsk();
    }

    @Test
    public void query_with_auth_03() {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with correct password
        qe.setClient(withCreds("allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_04() {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with correct password BUT not in correct role
        qe.setClient(withCreds("forbidden", "password"));
        qe.execAsk();
    }

    @Test
    public void query_with_auth_05() {
        // Uses auth and enables compression
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        qe.setAllowCompression(true);
        // Auth credentials for valid user with correct password
        qe.setClient(withCreds("allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_06() {
        // Uses auth and enables compression
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        qe.setAllowCompression(true);
        // Auth credentials for valid user with bad password
        qe.setClient(withCreds("allowed", "incorrect"));
        qe.execAsk();
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_07() throws URISyntaxException {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with correct password but scoped to wrong URI
        qe.setClient(withCreds(new URI("http://example"), "allowed", "password"));
        qe.execAsk();
    }

    @Test
    public void query_with_auth_08() throws URISyntaxException {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with correct password and scoped to correct URI
        qe.setClient(withCreds(new URI(authServiceQuery), "allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }

    @Test
    public void query_with_auth_09() {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with correct password using pre-emptive auth
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        URI scope = URI.create(authServiceUpdate);
        credsProv.setCredentials(new AuthScope(scope.getHost(), scope.getPort()),
                new UsernamePasswordCredentials("allowed", "password"));
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(new HttpHost(scope.getHost()), basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProv);
        context.setAuthCache(authCache);
        HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProv).build();
        qe.setClient(client);
        qe.setHttpContext(context);
        qe.setClient(withCreds(URI.create(authServiceQuery), "allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }

    @Test
    public void query_with_auth_10() {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped to correct URI
        // Provided via Service Context
        Map<String, Context> serviceContext = new HashMap<>();
        Context authContext = new Context();
        authContext.put(Service.queryClient, withCreds("allowed", "password"));
        serviceContext.put(authServiceQuery, authContext);
        qe.getContext().put(Service.serviceContext, serviceContext);
        Assert.assertTrue(qe.execAsk());
    }
    
    @Test
    public void query_with_auth_11() {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped to base URI of the actual service URL
        // Provided via Service Context
        Map<String, Context> serviceContext = new HashMap<>();
        Context authContext = new Context();
        authContext.put(Service.queryClient, withCreds(URI.create(authUrlRoot), "allowed", "password"));
        serviceContext.put(authServiceQuery, authContext);
        qe.getContext().put(Service.serviceContext, serviceContext);
        Assert.assertTrue(qe.execAsk());
    }
    
    @Test
    public void query_with_auth_12() {
        ARQ.getContext().remove(Service.serviceContext);
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with correct password
        qe.setClient(withCreds("allowed", "password"));
        Assert.assertTrue(qe.execAsk());
     }
    
    @Test
    public void query_with_auth_13() throws URISyntaxException {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped to base URI of the actual service URL
        qe.setClient(withCreds(new URI(authUrlRoot), "allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }
    
    @Test
    public void query_with_auth_14() throws URISyntaxException {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped to base URI of the actual service URL
        qe.setClient(withCreds(new URI("http://localhost:" + authPort), "allowed", "password"));
        Assert.assertTrue(qe.execAsk());
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_01() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, authServiceUpdate);
        // No auth credentials should result in an error
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_02() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, authServiceUpdate);
        // Auth credentials for valid user with bad password
        ue.setClient(withCreds("allowed", "incorrect"));
        ue.execute();
    }

    @Test
    public void update_with_auth_03() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password
        ue.setClient(withCreds("allowed", "password"));
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_04() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password BUT not in correct role
        ue.setClient(withCreds("forbidden", "password"));
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_05() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemoteForm(updates, authServiceUpdate);
        // No auth credentials should result in an error
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_06() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemoteForm(updates, authServiceUpdate);
        // Auth credentials for valid user with bad password
        ue.setClient(withCreds("allowed", "incorrect"));
        ue.execute();
    }

    @Test
    public void update_with_auth_07() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemoteForm(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password
        ue.setClient(withCreds("allowed", "password"));
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_08() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemoteForm(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password BUT not in correct role
        ue.setClient(withCreds("forbidden", "password"));
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_09() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password but scoped to wrong URI
        ue.setClient(withCreds(URI.create("http://example"), "allowed", "password"));
        ue.execute();
    }

    @Test
    public void update_with_auth_10() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, authServiceUpdate);

        // Auth credentials for valid user with correct password scoped to correct URI
        ue.setClient(withCreds(URI.create(authServiceUpdate), "allowed", "password"));
        ue.execute();
    }

    @Test
    public void update_with_auth_11() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates,
                authServiceUpdate);

        // Auth credentials for valid user with correct password scoped to correct URI
        // Also using pre-emptive auth
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        URI scope = URI.create(authServiceUpdate);
        credsProv.setCredentials(new AuthScope(scope.getHost(), scope.getPort()),
                new UsernamePasswordCredentials("allowed", "password"));
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(new HttpHost(scope.getHost()), basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProv);
        context.setAuthCache(authCache);
        HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProv).build();
        ue.setClient(client);
        ue.setHttpContext(context);
        ue.execute();
    }
    
    @Test(expected = HttpException.class)
    public void graphstore_with_auth_01() {       
        // No auth credentials
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(authServiceREST);
        accessor.getModel();
    }
    
    @Test(expected = HttpException.class)
    public void graphstore_with_auth_02() {
        // Incorrect auth credentials
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(authServiceREST, withCreds("allowed", "incorrect"));
        accessor.getModel();
    }
    
    @Test
    public void graphstore_with_auth_03() {
        // Correct auth credentials
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(authServiceREST, withCreds("allowed", "password"));
        Model m = accessor.getModel();
        Assert.assertTrue(m.isEmpty());
    }
    
    @Test(expected = HttpException.class)
    public void graphstore_with_auth_04() throws URISyntaxException {
        // Correct auth credentials scoped to wrong URI
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(authServiceREST, withCreds(new URI("http://example.org/"), "allowed", "password"));
        accessor.getModel();
    }
    
    @Test
    public void graphstore_with_auth_05() throws URISyntaxException {
        // Correct auth credentials scoped to correct URI
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(authServiceREST, withCreds(new URI(authServiceREST), "allowed", "password"));
        accessor.getModel();
    }
}

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

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.auth.PreemptiveBasicAuthenticator ;
import org.apache.jena.atlas.web.auth.ScopedAuthenticator ;
import org.apache.jena.atlas.web.auth.ServiceAuthenticator ;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.DatasetAccessor ;
import org.apache.jena.query.DatasetAccessorFactory ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP ;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP ;
import org.apache.jena.sparql.engine.http.Service ;
import org.apache.jena.sparql.modify.UpdateProcessRemoteBase ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.junit.AfterClass ;
import org.junit.Assert ;
import org.junit.BeforeClass ;
import org.junit.Test ;

/**
 * Tests Fuseki operation with authentication enabled
 */
public class TestAuth {
    
    // Use different port etc because sometimes the previous testing servers
    // don't release ports fast enough (OS issue / Linux)
    public static final int authPort             = ServerTest.port+10 ;
    public static final String authUrlRoot       = "http://localhost:"+authPort+"/" ;
    public static final String authDatasetPath   = "/dataset" ;
    public static final String authServiceUpdate = "http://localhost:"+authPort+authDatasetPath+"/update" ; 
    public static final String authServiceQuery  = "http://localhost:"+authPort+authDatasetPath+"/query" ; 
    public static final String authServiceREST   = "http://localhost:"+authPort+authDatasetPath+"/data" ;
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

        LogCtl.logLevel(Fuseki.serverLog.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING);
        LogCtl.logLevel(Fuseki.actionLog.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING);
        LogCtl.logLevel("org.eclipse.jetty", org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING);

        ServerTest.setupServer(authPort, realmFile.getAbsolutePath(), authDatasetPath);
    }

    /**
     * Tears down authentication test setup
     */
    @AfterClass
    public static void teardown() {
        ServerTest.teardownServer(); 
        realmFile.delete();
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
        qe.setBasicAuthentication("allowed", "incorrect".toCharArray());
        qe.execAsk();
    }

    @Test
    public void query_with_auth_03() {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with correct password
        qe.setBasicAuthentication("allowed", "password".toCharArray());
        Assert.assertTrue(qe.execAsk());
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_04() {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        // Auth credentials for valid user with correct password BUT not in
        // correct role
        qe.setBasicAuthentication("forbidden", "password".toCharArray());
        qe.execAsk();
    }

    @Test
    public void query_with_auth_05() {
        // Uses auth and enables compression
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        qe.setAllowDeflate(true);
        qe.setAllowGZip(true);

        // Auth credentials for valid user with correct password
        qe.setBasicAuthentication("allowed", "password".toCharArray());
        Assert.assertTrue(qe.execAsk());
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_06() {
        // Uses auth and enables compression
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");
        qe.setAllowDeflate(true);
        qe.setAllowGZip(true);

        // Auth credentials for valid user with bad password
        qe.setBasicAuthentication("allowed", "incorrect".toCharArray());
        qe.execAsk();
    }

    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_07() throws URISyntaxException {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password but scoped to
        // wrong URI
        ScopedAuthenticator authenticator = new ScopedAuthenticator(new URI("http://example"), "allowed",
                "password".toCharArray());
        qe.setAuthenticator(authenticator);
        qe.execAsk();
    }

    @Test
    public void query_with_auth_08() throws URISyntaxException {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped to
        // correct URI
        ScopedAuthenticator authenticator = new ScopedAuthenticator(new URI(authServiceQuery), "allowed", "password".toCharArray());
        qe.setAuthenticator(authenticator);
        Assert.assertTrue(qe.execAsk());
    }

    @Test
    public void query_with_auth_09() throws URISyntaxException {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password using
        // pre-emptive auth
        ScopedAuthenticator authenticator = new ScopedAuthenticator(new URI(authServiceQuery), "allowed", "password".toCharArray());
        qe.setAuthenticator(new PreemptiveBasicAuthenticator(authenticator));
        Assert.assertTrue(qe.execAsk());
    }

    @Test
    public void query_with_auth_10() {
        Context ctx = ARQ.getContext();
        try {
            QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

            // Auth credentials for valid user with correct password and scoped
            // to correct URI
            // Provided via Service Context and its associated authenticator
            Map<String, Context> serviceContext = new HashMap<String, Context>();
            Context authContext = new Context();
            authContext.put(Service.queryAuthUser, "allowed");
            authContext.put(Service.queryAuthPwd, "password");
            serviceContext.put(authServiceQuery, authContext);
            ctx.put(Service.serviceContext, serviceContext);

            qe.setAuthenticator(new ServiceAuthenticator());
            Assert.assertTrue(qe.execAsk());
        } finally {
            ctx.remove(Service.serviceContext);
        }
    }
    
    @Test
    public void query_with_auth_11() {
        Context ctx = ARQ.getContext();
        try {
            QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

            // Auth credentials for valid user with correct password and scoped
            // to base URI of the actual service URL
            // Provided via Service Context and its associated authenticator
            Map<String, Context> serviceContext = new HashMap<String, Context>();
            Context authContext = new Context();
            authContext.put(Service.queryAuthUser, "allowed");
            authContext.put(Service.queryAuthPwd, "password");
            serviceContext.put(authUrlRoot, authContext);
            ctx.put(Service.serviceContext, serviceContext);

            qe.setAuthenticator(new ServiceAuthenticator());
            Assert.assertTrue(qe.execAsk());
        } finally {
            ctx.remove(Service.serviceContext);
        }
    }
    
    @Test
    public void query_with_auth_12() {
        ARQ.getContext().remove(Service.serviceContext);

        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password
        // Use service authenticator with fallback credentials.
        qe.setAuthenticator(new ServiceAuthenticator("allowed", "password".toCharArray()));
        Assert.assertTrue(qe.execAsk());
     }
    
    @Test
    public void query_with_auth_13() throws URISyntaxException {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped to
        // base URI of the actual service URL
        ScopedAuthenticator authenticator = new ScopedAuthenticator(new URI(authUrlRoot), "allowed", "password".toCharArray());
        qe.setAuthenticator(authenticator);
        Assert.assertTrue(qe.execAsk());
    }
    
    @Test
    public void query_with_auth_14() throws URISyntaxException {
        QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(authServiceQuery, "ASK { }");

        // Auth credentials for valid user with correct password and scoped to
        // base URI of the actual service URL
        ScopedAuthenticator authenticator = new ScopedAuthenticator(new URI("http://localhost:" + authPort), "allowed", "password".toCharArray());
        qe.setAuthenticator(authenticator);
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
        ue.setAuthentication("allowed", "incorrect".toCharArray());
        ue.execute();
    }

    @Test
    public void update_with_auth_03() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password
        ue.setAuthentication("allowed", "password".toCharArray());
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_04() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password BUT not in
        // correct role
        ue.setAuthentication("forbidden", "password".toCharArray());
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
        ue.setAuthentication("allowed", "incorrect".toCharArray());
        ue.execute();
    }

    @Test
    public void update_with_auth_07() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemoteForm(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password
        ue.setAuthentication("allowed", "password".toCharArray());
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_08() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemoteForm(updates, authServiceUpdate);
        // Auth credentials for valid user with correct password BUT not in
        // correct role
        ue.setAuthentication("forbidden", "password".toCharArray());
        ue.execute();
    }

    @Test(expected = HttpException.class)
    public void update_with_auth_09() throws URISyntaxException {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, authServiceUpdate);

        // Auth credentials for valid user with correct password but scoped to
        // wrong URI
        ScopedAuthenticator authenticator = new ScopedAuthenticator(new URI("http://example"), "allowed",
                "password".toCharArray());
        ue.setAuthenticator(authenticator);
        ue.execute();
    }

    @Test
    public void update_with_auth_10() throws URISyntaxException {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, authServiceUpdate);

        // Auth credentials for valid user with correct password scoped to
        // correct URI
        ScopedAuthenticator authenticator = new ScopedAuthenticator(new URI(authServiceUpdate), "allowed", "password".toCharArray());
        ue.setAuthenticator(authenticator);
        ue.execute();
    }

    @Test
    public void update_with_auth_11() throws URISyntaxException {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updates, authServiceUpdate);

        // Auth credentials for valid user with correct password scoped to
        // correct URI
        // Also using pre-emptive auth
        ScopedAuthenticator authenticator = new ScopedAuthenticator(new URI(authServiceUpdate), "allowed", "password".toCharArray());
        ue.setAuthenticator(new PreemptiveBasicAuthenticator(authenticator));
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
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(authServiceREST, new SimpleAuthenticator("allowed", "incorrect".toCharArray()));
        accessor.getModel();
    }
    
    @Test
    public void graphstore_with_auth_03() {
        // Correct auth credentials
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(authServiceREST, new SimpleAuthenticator("allowed", "password".toCharArray()));
        Model m = accessor.getModel();
        Assert.assertTrue(m.isEmpty());
    }
    
    @Test(expected = HttpException.class)
    public void graphstore_with_auth_04() throws URISyntaxException {
        // Correct auth credentials scoped to wrong URI
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(authServiceREST, new ScopedAuthenticator(new URI("http://example.org/"), "allowed", "password".toCharArray()));
        accessor.getModel();
    }
    
    @Test
    public void graphstore_with_auth_05() throws URISyntaxException {
        // Correct auth credentials scoped to correct URI
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(authServiceREST, new ScopedAuthenticator(new URI(authServiceREST), "allowed", "password".toCharArray()));
        accessor.getModel();
    }
}

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.server.FusekiConfig;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.apache.jena.fuseki.server.ServerConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.sparql.modify.UpdateProcessRemoteBase;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Tests Fuseki operation with authentication enabled
 * @author rvesse
 *
 */
public class TestAuth extends BaseServerTest {
    
    private static File realmFile;
    private static SPARQLServer server;

    @BeforeClass
    public static void setup() throws IOException {
        realmFile = File.createTempFile("realm", ".properties");
        
        FileWriter writer = new FileWriter(realmFile);
        writer.write("allowed: password, fuseki\n");
        writer.write("forbidden: password, other");
        writer.close();
        
        Log.logLevel(Fuseki.serverLog.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;
        Log.logLevel(Fuseki.requestLog.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;
        Log.logLevel("org.eclipse.jetty", org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;

        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        // This must agree with BaseServerTest
        ServerConfig conf = FusekiConfig.defaultConfiguration(datasetPath, dsg, true) ;
        conf.port = BaseServerTest.port ;
        conf.pagesPort = BaseServerTest.port ;
        conf.authConfigFile = realmFile.getAbsolutePath() ;

        server = new SPARQLServer(conf) ;
        server.start() ;
    }
    
    @AfterClass
    public static void teardown() {
        server.stop();
        
        realmFile.delete();
    }
    
    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_01() {
        QueryEngineHTTP qe = (QueryEngineHTTP)QueryExecutionFactory.sparqlService(serviceQuery, "ASK { }");
        // No auth credentials should result in an error
        qe.execAsk();
    }
    
    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_02() {
        QueryEngineHTTP qe = (QueryEngineHTTP)QueryExecutionFactory.sparqlService(serviceQuery, "ASK { }");
        // Auth credentials for valid user with bad password
        qe.setBasicAuthentication("allowed", "incorrect".toCharArray());
        qe.execAsk();
    }
    
    @Test
    public void query_with_auth_03() {
        QueryEngineHTTP qe = (QueryEngineHTTP)QueryExecutionFactory.sparqlService(serviceQuery, "ASK { }");
        // Auth credentials for valid user with correct password
        qe.setBasicAuthentication("allowed", "password".toCharArray());
        Assert.assertTrue(qe.execAsk());
    }
    
    @Test(expected = QueryExceptionHTTP.class)
    public void query_with_auth_04() {
        QueryEngineHTTP qe = (QueryEngineHTTP)QueryExecutionFactory.sparqlService(serviceQuery, "ASK { }");
        // Auth credentials for valid user with correct password BUT not in correct role
        qe.setBasicAuthentication("forbidden", "password".toCharArray());
        qe.execAsk();
    }
    
    @Test(expected = HttpException.class)
    public void update_with_auth_01() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase)UpdateExecutionFactory.createRemote(updates, serviceUpdate);
        // No auth credentials should result in an error
        ue.execute();
    }
    
    @Test(expected = HttpException.class)
    public void update_with_auth_02() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase)UpdateExecutionFactory.createRemote(updates, serviceUpdate);
        // Auth credentials for valid user with bad password
        ue.setAuthentication("allowed", "incorrect".toCharArray());
        ue.execute();
    }
    
    @Test
    public void update_with_auth_03() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase)UpdateExecutionFactory.createRemote(updates, serviceUpdate);
        // Auth credentials for valid user with correct password
        ue.setAuthentication("allowed", "password".toCharArray());
        ue.execute();
    }
    
    @Test(expected = HttpException.class)
    public void update_with_auth_04() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase)UpdateExecutionFactory.createRemote(updates, serviceUpdate);
        // Auth credentials for valid user with correct password BUT not in correct role
        ue.setAuthentication("forbidden", "password".toCharArray());
        ue.execute();
    }
    
    @Test(expected = HttpException.class)
    public void update_with_auth_05() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase)UpdateExecutionFactory.createRemoteForm(updates, serviceUpdate);
        // No auth credentials should result in an error
        ue.execute();
    }
    
    @Test(expected = HttpException.class)
    public void update_with_auth_06() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase)UpdateExecutionFactory.createRemoteForm(updates, serviceUpdate);
        // Auth credentials for valid user with bad password
        ue.setAuthentication("allowed", "incorrect".toCharArray());
        ue.execute();
    }
    
    @Test
    public void update_with_auth_07() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase)UpdateExecutionFactory.createRemoteForm(updates, serviceUpdate);
        // Auth credentials for valid user with correct password
        ue.setAuthentication("allowed", "password".toCharArray());
        ue.execute();
    }
    
    @Test(expected = HttpException.class)
    public void update_with_auth_08() {
        UpdateRequest updates = UpdateFactory.create("CREATE SILENT GRAPH <http://graph>");
        UpdateProcessRemoteBase ue = (UpdateProcessRemoteBase)UpdateExecutionFactory.createRemoteForm(updates, serviceUpdate);
        // Auth credentials for valid user with correct password BUT not in correct role
        ue.setAuthentication("forbidden", "password".toCharArray());
        ue.execute();
    }
}

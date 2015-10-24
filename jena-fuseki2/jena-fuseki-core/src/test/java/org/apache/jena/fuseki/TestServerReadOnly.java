/**
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

import static org.apache.jena.fuseki.ServerTest.serviceGSP ;
import static org.apache.jena.fuseki.ServerTest.serviceQuery ;
import static org.apache.jena.fuseki.ServerTest.serviceUpdate ;

import java.nio.charset.StandardCharsets ;

import org.apache.http.HttpEntity ;
import org.apache.http.entity.StringEntity ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateProcessor ;
import org.apache.jena.update.UpdateRequest ;
import org.apache.jena.web.HttpSC ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

/** Tests on a read only server. */
public class TestServerReadOnly extends BaseTest
{
    // readonly server.
    @BeforeClass
    public static void allocServerForSuite() {
        ServerTest.allocServer(false) ;
    }

    @AfterClass
    public static void freeServerForSuite() {
        ServerTest.freeServer() ;
    }
    
    @Test
    public void query_readonly() {
        Query query = QueryFactory.create("ASK{}");
        QueryExecution qexec = QueryExecutionFactory.sparqlService(serviceQuery, query);
        qexec.execAsk() ;
    }
    
    @Test()
    public void update_readonly() {
        FusekiTest.exec404( () -> {
            UpdateRequest update = UpdateFactory.create("INSERT DATA {}");
            UpdateProcessor proc = UpdateExecutionFactory.createRemote(update, serviceUpdate);
            proc.execute();
        });
    }

    
    @Test
    public void gsp_w_readonly_POST() {
        // Try to write
        FusekiTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            HttpEntity e = new StringEntity("", StandardCharsets.UTF_8) ;
            HttpOp.execHttpPost(serviceGSP+"?default", e);
        }) ;
    }
    
    @Test
    public void gsp_w_readonly_PUT() {
        // Try to write
        FusekiTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            HttpEntity e = new StringEntity("", StandardCharsets.UTF_8) ;
            HttpOp.execHttpPut(serviceGSP+"?default", e);
        }) ;
    }

    @Test
    public void gsp_w_readonly_DELETE() {
        // Try to write
        FusekiTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            HttpOp.execHttpDelete(serviceGSP+"?default");
        }) ;
    }
    
    @Test
    public void dataset_w_readonly_POST() {
        // Try to write
        FusekiTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            HttpEntity e = new StringEntity("", StandardCharsets.UTF_8) ;
            HttpOp.execHttpPost(ServerTest.urlDataset, e) ;
        }) ;
    }

    @Test
    public void dataset_w_readonly_PUT() {
        // Try to write
        FusekiTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            HttpEntity e = new StringEntity("", StandardCharsets.UTF_8) ;
            HttpOp.execHttpPut(ServerTest.urlDataset, e) ;
        }) ;
    }

    @Test
    public void dataset_w_readonly_DELETE() {
        // Try to write
        FusekiTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            HttpOp.execHttpDelete(ServerTest.urlDataset) ;
        }) ;
    }

    @Test
    public void options_gsp_readonly() {
        String v = FusekiTest.execOptions(serviceGSP+"?default") ;
        FusekiTest.assertStringList(v, "GET", "OPTIONS", "HEAD") ;
    }

}


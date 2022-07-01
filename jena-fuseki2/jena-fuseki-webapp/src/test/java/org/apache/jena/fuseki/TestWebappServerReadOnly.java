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

import static org.apache.jena.fuseki.ServerCtl.serviceQuery;
import static org.apache.jena.fuseki.ServerCtl.serviceUpdate;

import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;

import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.test.FusekiTest;
import org.apache.jena.fuseki.test.HttpTest;
import org.apache.jena.http.HttpOp;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.web.HttpSC;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests on a read only server. */
public class TestWebappServerReadOnly
{
    // readonly server.
    @BeforeClass
    public static void allocServerForSuite() {
        ServerCtl.freeServer();
        //Manage ourselves.
        ServerCtl.setupServer(false);
    }

    @AfterClass
    public static void freeServerForSuite() {
        ServerCtl.freeServer();
    }

    @Test
    public void query_readonly() {
        Query query = QueryFactory.create("ASK{}");
        QueryExecutionHTTP.service(serviceQuery(), query).execAsk();
    }

    @Test()
    public void update_readonly() {
        HttpTest.expect404( () -> {
            UpdateExecution.service(serviceUpdate()).update("INSERT DATA {}").execute();
        });
    }

    @Test
    public void gsp_w_readonly_POST() {
        // Try to write
        HttpTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            BodyPublisher bodyPublisher = BodyPublishers.ofString("");
            HttpOp.httpPost(ServerCtl.serviceGSP()+"?default", null, bodyPublisher);
        });
    }

    @Test
    public void gsp_w_readonly_PUT() {
        // Try to write
        HttpTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            BodyPublisher bodyPublisher = BodyPublishers.ofString("");
            HttpOp.httpPut(ServerCtl.serviceGSP()+"?default", null, bodyPublisher);
        });
    }

    @Test
    public void gsp_w_readonly_DELETE() {
        // Try to write
        HttpTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            HttpOp.httpDelete(ServerCtl.serviceGSP()+"?default");
        });
    }

    @Test
    public void dataset_readonly_GET() {
        // Try to read
        try ( TypedInputStream in = HttpOp.httpGet(ServerCtl.urlDataset()) ) {}
    }


    @Test
    public void dataset_w_readonly_POST() {
        // Try to write
        HttpTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            BodyPublisher bodyPublisher = BodyPublishers.ofString("");
            HttpOp.httpPost(ServerCtl.urlDataset(), null, bodyPublisher);
        });
    }

    @Test
    public void dataset_w_readonly_PUT() {
        // Try to write
        HttpTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            BodyPublisher bodyPublisher = BodyPublishers.ofString("");
            HttpOp.httpPut(ServerCtl.urlDataset(), null, bodyPublisher);
        });
    }

    @Test
    public void dataset_w_readonly_DELETE() {
        // Try to write
        HttpTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, ()->{
            HttpOp.httpDelete(ServerCtl.urlDataset());
        });
    }

    @Test
    public void options_gsp_readonly() {
        String v = HttpOp.httpOptions(ServerCtl.serviceGSP()+"?default");
        FusekiTest.assertStringList(v, "GET", "OPTIONS", "HEAD");
    }

}


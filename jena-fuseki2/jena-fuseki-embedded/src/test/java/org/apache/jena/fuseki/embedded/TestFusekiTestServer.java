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

package org.apache.jena.fuseki.embedded;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.web.HttpSC;
import org.junit.*;

public class TestFusekiTestServer {
    
//    @BeforeClass static public void beforeSuiteClass() { ServerCtl.ctlBeforeTestSuite(); } 
//    @AfterClass  static public void afterSuiteClass()  { ServerCtl.ctlAfterTestSuite(); }

    // This is file is the "suite".
    
    @BeforeClass public static void ctlBeforeClass() { FusekiTestServer.ctlBeforeTestSuite(); FusekiTestServer.ctlBeforeClass(); }
    @AfterClass  public static void ctlAfterClass()  { FusekiTestServer.ctlAfterClass();      FusekiTestServer.ctlAfterTestSuite(); }
    @Before      public void ctlBeforeTest()         { FusekiTestServer.ctlBeforeTest(); }
    @After       public void ctlAfterTest()          { FusekiTestServer.ctlAfterTest(); }
    
    @Test public void testServer_1() {
        HttpOp.execHttpGetString(FusekiTestServer.urlDataset());
    }
    
    @Test public void testServer_2() {
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("USER", "PASSWORD"));
        HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProv).build();
        
        // No auth set - should work.
        try ( TypedInputStream in = HttpOp.execHttpGet(FusekiTestServer.urlDataset(), "*/*") ) {}
        catch (HttpException ex) {
            Assert.assertTrue(ex.getResponseCode() == HttpSC.FORBIDDEN_403 || ex.getResponseCode() == HttpSC.UNAUTHORIZED_401 );
            throw ex;
        }
    }
}

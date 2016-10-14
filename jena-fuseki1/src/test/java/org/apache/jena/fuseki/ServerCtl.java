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

import org.apache.http.client.HttpClient ;
import org.apache.http.impl.client.CloseableHttpClient ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.web.HttpOp ;

public class ServerCtl {
    static { Fuseki.init(); }
    
    /* Put this in each test class using the Fuseki server:
    @BeforeClass public static void ctlBeforeClass() { ServerCtl.ctlBeforeClass(); }
    @AfterClass  public static void ctlAfterClass()  { ServerCtl.ctlAfterClass(); }
    @Before      public void ctlBeforeTest()         { ServerCtl.ctlBeforeTest(); }
    @After       public void ctlAfterTest()          { ServerCtl.ctlAfterTest(); } 
    */
    
    static HttpClient defaultHttpClient = HttpOp.getDefaultHttpClient();

    // 2 choices: server over whole test suite or server over each test class.
    // Preferred "true" - stop-start server between test classes.
    // Note: it is import to cleanly close a PoolingHttpClient across server restarts
    // otherwise the pooled connections remian for the old server. 
    
    static final boolean SERVER_PER_CLASS = true ;  
    public static void ctlBeforeTestSuite() {
        if ( ! SERVER_PER_CLASS ) {
            setPoolingHttpClient() ;
            ServerTest.allocServer();
        }
    }
    
    public static void ctlAfterTestSuite()  {
        if ( ! SERVER_PER_CLASS ) {
            ServerTest.freeServer();
            resetDefaultHttpClient() ;
        }
    }
    
    /**
     * Setup for the tests by allocating a Fuseki instance to work with
     */
    public static void ctlBeforeClass() {
        if ( SERVER_PER_CLASS ) {
            setPoolingHttpClient() ;
            ServerTest.allocServer();
        }
    }
    
    /**
     * Clean up after tests by de-allocating the Fuseki instance
     */
    public static void ctlAfterClass() {
        if ( SERVER_PER_CLASS ) {
            ServerTest.freeServer();
            resetDefaultHttpClient() ;
        }
    }

    /**
     * Placeholder.
     */
    public static void ctlBeforeTest() {
    }

    /**
     * Clean up after each test by resetting the Fuseki dataset
     */
    public static void ctlAfterTest() {
        ServerTest.resetServer();
    }

    /** Set a PoolingHttpClient */
    private static void setPoolingHttpClient() {
        setHttpClient(HttpOp.createPoolingHttpClient()) ;
    }

    /** Restore the original setup */
    private static void resetDefaultHttpClient() {
        setHttpClient(defaultHttpClient);
    }
    
    /** Set the HttpClient - close the old one if appropriate */
    private static void setHttpClient(HttpClient newHttpClient) {
        HttpClient hc = HttpOp.getDefaultHttpClient() ;
        if ( hc instanceof CloseableHttpClient )
            IO.close((CloseableHttpClient)hc) ;
        HttpOp.setDefaultHttpClient(newHttpClient) ;
    }
}

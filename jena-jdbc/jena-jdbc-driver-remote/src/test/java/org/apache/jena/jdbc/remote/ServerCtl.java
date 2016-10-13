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

package org.apache.jena.jdbc.remote;

import org.apache.http.client.HttpClient ;
import org.apache.http.impl.client.CloseableHttpClient ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.ServerTest ;
import org.apache.jena.riot.web.HttpOp ;

public class ServerCtl {
    static { Fuseki.init(); }
    
    /* Put this in each test class using the Fuseki server:
    @BeforeClass public static void ctlBeforeClass() { ServerCtl.ctlBeforeClass(); }
    @AfterClass  public static void ctlAfterClass()  { ServerCtl.ctlAfterClass(); }
    @Before      public void ctlBeforeTest() { ServerCtl.ctlBeforeTest(); }
    @After       public void ctlAfterTest()  { ServerCtl.ctlAfterTest(); } 
    */
    
    // One server, all tests.
    //static { ServerTest.allocServer(); }
    
    // Use HttpOp caching of connections during testing to avoid
    // swamping kernel socket management
    static HttpClient defaultHttpClient = HttpOp.getDefaultHttpClient();
    
    // Used for all tests except auth tests.
    //static final HttpClient globalPoolingClient = HttpOp.createPoolingHttpClient();

    public static void ctlBeforeTestSuite() {
        // Does not work to have pool across server free/alloc.
        // This may be to do with timing when using localhost
        // and a high frequence connection churn.
        //setPoolingHttpClient() ;
    }
    
    public static void ctlAfterTestSuite()  {
        //resetDefaultHttpClient();
    }
    
    /**
     * Setup for the tests by allocating a Fuseki instance to work with
     */
    public static void ctlBeforeClass() {
        setPoolingHttpClient() ;
        ServerTest.allocServer();
    }
    
    /**
     * Clean up after tests by de-allocating the Fuseki instance
     */
    public static void ctlAfterClass() {
        ServerTest.freeServer();
        resetDefaultHttpClient() ;
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

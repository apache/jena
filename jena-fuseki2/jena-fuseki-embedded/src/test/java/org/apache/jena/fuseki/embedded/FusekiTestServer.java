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

package org.apache.jena.fuseki.embedded;

import static org.apache.jena.fuseki.embedded.FusekiTestServer.ServerScope.CLASS ;
import static org.apache.jena.fuseki.embedded.FusekiTestServer.ServerScope.SUITE ;
import static org.apache.jena.fuseki.embedded.FusekiTestServer.ServerScope.TEST ;

import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.http.client.HttpClient ;
import org.apache.http.impl.client.CloseableHttpClient ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.fuseki.server.FusekiEnv;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.modify.request.Target ;
import org.apache.jena.sparql.modify.request.UpdateDrop ;
import org.apache.jena.system.Txn ;
import org.apache.jena.update.Update ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateProcessor ;

// NOT FINISHED

/**
 * Manage a single server for use with tests. It supports three modes:
 * <ul>
 * <li>{@code ServerScope.SUITE} : One server for a whole test suite
 * <li>{@code ServerScope.CLASS} : One server per test class
 * <li>{@code ServerScope.TEST} :One server per individual test
 * </ul>
 * One server per individual test can be troublesome due to connections not closing down
 * fast enough and can also be slow.
 * <p> One server per test class is a good compromise. 
 * <p> The data in the server is always reset between tests in all modes.
 * <p>
 * Using a connection pooling HttpClient (see {@link HttpOp#createPoolingHttpClient()}) is important,
 * both for test performance and for reducing the TCP connection load on the operating system.  
 * <p>
 * Usage:
 * </p>
 * <p>
 * In the test suite, put:
 * 
 * <pre>
 *  {@literal @BeforeClass} static public void beforeSuiteClass() { ServerCtl.ctlBeforeTestSuite(); } 
 *  {@literal @AfterClass}  static public void afterSuiteClass()  { ServerCtl.ctlAfterTestSuite(); }
 * </pre>
 * <p>
 * In the test class, put:
 * <pre>
 * {@literal @BeforeClass} public static void ctlBeforeClass() { ServerCtl.ctlBeforeClass(); }
 * {@literal @AfterClass}  public static void ctlAfterClass()  { ServerCtl.ctlAfterClass(); }
 * {@literal @Before}      public void ctlBeforeTest()         { ServerCtl.ctlBeforeTest(); }
 * {@literal @After}       public void ctlAfterTest()          { ServerCtl.ctlAfterTest(); }
 * </pre>
 */
public class FusekiTestServer {
    /* Cut&Paste versions:

    Test suite (TS_*)
    @BeforeClass static public void beforeSuiteClass() { ServerCtl.ctlBeforeTestSuite(); } 
    @AfterClass  static public void afterSuiteClass()  { ServerCtl.ctlAfterTestSuite(); }

    Test class (Test*)
    @BeforeClass public static void ctlBeforeClass() { ServerCtl.ctlBeforeClass(); }
    @AfterClass  public static void ctlAfterClass()  { ServerCtl.ctlAfterClass(); }
    @Before      public void ctlBeforeTest()         { ServerCtl.ctlBeforeTest(); }
    @After       public void ctlAfterTest()          { ServerCtl.ctlAfterTest(); }
    */
    
    static HttpClient defaultHttpClient = HttpOp.getDefaultHttpClient();

    // Note: it is important to cleanly close a PoolingHttpClient across server restarts
    // otherwise the pooled connections remain for the old server. 
    
    /*package : for import static */ enum ServerScope { SUITE, CLASS, TEST }
    private static ServerScope serverScope = ServerScope.CLASS ;
    private static int currentPort = FusekiEnv.choosePort() ;
    
    public static int port() {
        return currentPort ;
    }

    // Whether to use a transaction on the dataset or to use SPARQL Update. 
    static boolean CLEAR_DSG_DIRECTLY = true ;
    static private DatasetGraph dsgTesting ;
    
    // reference count of start/stop server
    private static AtomicInteger countServer    = new AtomicInteger() ; 
    private static FusekiEmbeddedServer server  = null ;
    
    public static final String urlRoot()        { return "http://localhost:"+port()+"/" ; }
    public static final String datasetPath()    { return "/ds_test" ; }
    public static final String urlDataset()     { return "http://localhost:"+port()+datasetPath() ; }
    
    public static final String serviceUpdate()  { return "http://localhost:"+port()+datasetPath()+"/update" ; } 
    public static final String serviceQuery()   { return "http://localhost:"+port()+datasetPath()+"/query" ; }
    public static final String serviceGSP()     { return "http://localhost:"+port()+datasetPath()+"/data" ; }
    
    public static void ctlBeforeTestSuite() {
        if ( serverScope == SUITE  ) {
            setPoolingHttpClient() ;
            allocServer();
        }
    }
    
    public static void ctlAfterTestSuite()  {
        if ( serverScope == SUITE  ) {
            freeServer();
            resetDefaultHttpClient() ;
        }
    }
    
    /**
     * Setup for the tests by allocating a Fuseki instance to work with
     */
    public static void ctlBeforeClass() {
        if ( serverScope == CLASS  ) {
            setPoolingHttpClient() ;
            allocServer();
        }
    }
    
    /**
     * Clean up after tests by de-allocating the Fuseki instance
     */
    public static void ctlAfterClass() {
        if ( serverScope == CLASS  ) {
            freeServer();
            resetDefaultHttpClient() ;
        }
    }

    /**
     * Placeholder.
     */
    public static void ctlBeforeTest() {
        if ( serverScope == TEST  ) {
            setPoolingHttpClient() ;
            allocServer();
        }
    }

    /**
     * Clean up after each test by resetting the Fuseki dataset
     */
    public static void ctlAfterTest() {
        if ( serverScope == TEST  ) {
            freeServer();
            resetDefaultHttpClient() ;
        } else
            resetServer();
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
    /*package*/ static void setHttpClient(HttpClient newHttpClient) {
        HttpClient hc = HttpOp.getDefaultHttpClient() ;
        if ( hc instanceof CloseableHttpClient )
            IO.close((CloseableHttpClient)hc) ;
        HttpOp.setDefaultHttpClient(newHttpClient) ;
    }
    
    /*package*/ static void allocServer() {
        if ( countServer.getAndIncrement() == 0 )
            setupServer(true) ;
    }
    
    /*package*/ static void freeServer() {
        if ( countServer.decrementAndGet() == 0 )
            teardownServer() ;
    }
    
    /*package*/ static void setupServer(boolean updateable) {
        dsgTesting = DatasetGraphFactory.createTxnMem() ;
        server =
            FusekiEmbeddedServer.create()
            .setPort(port())
            .setLoopback(true)
            .add(datasetPath(), dsgTesting)
            .build();
    }
    
    /*package*/ static void teardownServer() {
        if ( server != null )
            server.stop() ;
        server = null ;
    }

    /*package*/ static void resetServer() {
        if (countServer.get() == 0)  
            throw new RuntimeException("No server started!");
        if ( CLEAR_DSG_DIRECTLY ) {
            Txn.executeWrite(dsgTesting, ()->dsgTesting.clear()) ;   
        } else {
            Update clearRequest = new UpdateDrop(Target.ALL) ;
            UpdateProcessor proc = UpdateExecutionFactory.createRemote(clearRequest, serviceUpdate()) ;
            try {proc.execute() ; }
            catch (Throwable e) {e.printStackTrace(); throw e;}
        }
    }
}

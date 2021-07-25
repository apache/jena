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

import static org.apache.jena.fuseki.ServerCtl.ServerScope.CLASS;
import static org.apache.jena.fuseki.ServerCtl.ServerScope.SUITE;
import static org.apache.jena.fuseki.ServerCtl.ServerScope.TEST;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.cmd.FusekiArgs;
import org.apache.jena.fuseki.cmd.JettyFusekiWebapp;
import org.apache.jena.fuseki.jetty.JettyServerConfig;
import org.apache.jena.fuseki.webapp.FusekiEnv;
import org.apache.jena.fuseki.webapp.FusekiServerListener;
import org.apache.jena.fuseki.webapp.FusekiWebapp;
import org.apache.jena.fuseki.webapp.SystemState;
import org.apache.jena.riot.web.HttpOp1;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.modify.request.Target;
import org.apache.jena.sparql.modify.request.UpdateDrop;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.base.file.Location;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateProcessor;

/**
 * <b>Note:</b>
 * <br/>
 * <em> There is a {@code FusekiTestServer} in the basic Fuseki server which is more
 * appropriate for testing SPARQL protocols. It does not have an on-disk footprint.</em>
 * <br/>
 * This class is
 * primarily for testing the full Fuseki server and has a full on-disk configuration.
 *
 * Manage a single server for use with tests. It supports three modes:
 * <ul>
 * <li>One server for a whole test suite
 * <li>One server per test class
 * <li>One server per individual test
 * </ul>
 * One server per individual test can be troublesome due to connections not closing down
 * fast enough (left in TCP state {@code TIME_WAIT} which is 2 minutes) and also can be
 * slow. One server per test class is a good compromise.
 * <p>
 * The data in the server is always reseet between tests.
 * <p>
 * Using a connection pooling HttpClient (see {@link HttpOp1#createPoolingHttpClient()}) is
 * important, both for test performance and for reducing the TCP connection load on the
 * operating system.
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
 *
 * <pre>
 * {@literal @BeforeClass} public static void ctlBeforeClass() { ServerCtl.ctlBeforeClass(); }
 * {@literal @AfterClass}  public static void ctlAfterClass()  { ServerCtl.ctlAfterClass(); }
 * {@literal @Before}      public void ctlBeforeTest()         { ServerCtl.ctlBeforeTest(); }
 * {@literal @After}       public void ctlAfterTest()          { ServerCtl.ctlAfterTest(); }
 * </pre>
 */
public class ServerCtl {
    static { Fuseki.init(); }

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

    // Note: it is important to cleanly close a PoolingHttpClient across server restarts
    // otherwise the pooled connections remain for the old server.

    /*package : for import static */ enum ServerScope { SUITE, CLASS, TEST }
    private static ServerScope serverScope = ServerScope.CLASS;
    private static int currentPort = WebLib.choosePort();

    public static int port() {
        return currentPort;
    }

    // Whether to use a transaction on the dataset or to use SPARQL Update.
    static boolean CLEAR_DSG_DIRECTLY = true;
    static private DatasetGraph dsgTesting;

    // Abstraction that runs a SPARQL server for tests.
    public static final String urlRoot()        { return "http://localhost:"+port()+"/"; }
    public static final String datasetName()    { return "dataset"; }
    public static final String datasetPath()    { return "/"+datasetName(); }
    public static final String urlDataset()     { return "http://localhost:"+port()+datasetPath(); }

    public static final String serviceUpdate()  { return "http://localhost:"+port()+datasetPath()+"/update"; }
    public static final String serviceQuery()   { return "http://localhost:"+port()+datasetPath()+"/query"; }
    public static final String serviceGSP()     { return "http://localhost:"+port()+datasetPath()+"/data"; }


    public static void ctlBeforeTestSuite() {
        if ( serverScope == SUITE  ) {
            setPoolingHttpClient();
            allocServer();
        }
    }

    public static void ctlAfterTestSuite()  {
        if ( serverScope == SUITE  ) {
            freeServer();
            resetDefaultHttpClient();
        }
    }

    /**
     * Setup for the tests by allocating a Fuseki instance to work with
     */
    public static void ctlBeforeClass() {
        if ( serverScope == CLASS  ) {
            setPoolingHttpClient();
            allocServer();
        }
    }

    /**
     * Clean up after tests by de-allocating the Fuseki instance
     */
    public static void ctlAfterClass() {
        if ( serverScope == CLASS  ) {
            freeServer();
            resetDefaultHttpClient();
        }
    }

    /**
     * Placeholder.
     */
    public static void ctlBeforeTest() {
        if ( serverScope == TEST  ) {
            setPoolingHttpClient();
            allocServer();
        }
    }

    /**
     * Clean up after each test by resetting the Fuseki dataset
     */
    public static void ctlAfterTest() {
        if ( serverScope == TEST  ) {
            freeServer();
            resetDefaultHttpClient();
        } else
            resetServer();
    }

    /** Set a PoolingHttpClient */
    private static void setPoolingHttpClient() {
        setHttpClient(HttpOp1.createPoolingHttpClient());
    }

    /** Restore the original setup */
    private static void resetDefaultHttpClient() {
        setHttpClient(HttpOp1.createDefaultHttpClient());
    }

    /** Set the HttpClient - close the old one if appropriate */
    /*package*/ static void setHttpClient(HttpClient newHttpClient) {
        HttpClient hc = HttpOp1.getDefaultHttpClient();
        if ( hc instanceof CloseableHttpClient )
            IO.close((CloseableHttpClient)hc);
        HttpOp1.setDefaultHttpClient(newHttpClient);
    }

    // reference count of start/stop server
    private static AtomicInteger countServer = new AtomicInteger();
    private static JettyFusekiWebapp server        = null;

    /*package*/ static void allocServer() {
        if ( countServer.getAndIncrement() == 0 )
            setupServer(true);
    }

    /*package*/ static void freeServer() {
        if ( countServer.decrementAndGet() == 0 )
            teardownServer();
    }

    protected static void setupServer(boolean updateable) {
        // Does not initial Fuseki webapp.
        FusekiEnv.FUSEKI_HOME = Paths.get(TS_FusekiWebapp.FusekiTestHome).toAbsolutePath();
        FileOps.ensureDir("target");
        FileOps.ensureDir(TS_FusekiWebapp.FusekiTestHome);
        FileOps.ensureDir(TS_FusekiWebapp.FusekiTestBase);
        FusekiEnv.FUSEKI_BASE = Paths.get(TS_FusekiWebapp.FusekiTestBase).toAbsolutePath();
        // Must have shiro.ini.
        // This fakes the state after FusekiSystem initialization
        // in the case of starting in the same location. FusekiSystem has statics.
        // Fuseki-full is designed to be the only server, not restartable.
        // Here, we want to reset for testing.
        FusekiWebapp.formatBaseArea();
        emptyDirectory(FusekiWebapp.dirSystemDatabase);
        emptyDirectory(FusekiWebapp.dirBackups);
        emptyDirectory(FusekiWebapp.dirLogs);
        emptyDirectory(FusekiWebapp.dirConfiguration);
        emptyDirectory(FusekiWebapp.dirDatabases);
        emptyDirectory(FusekiWebapp.dirSystemFileArea);

        setupServer(port(), null, datasetPath(), updateable);
    }

    private static void emptyDirectory(Path directory) {
        if ( directory == null )
            // Server will create.
            return;
        FileOps.clearAll(directory.toString());
    }

    public static void setupServer(int port, String authConfigFile, String datasetPath, boolean updateable) {
        SystemState.location = Location.mem();
        SystemState.init$();

        FusekiArgs params = new FusekiArgs();
        dsgTesting = DatasetGraphFactory.createTxnMem();
        params.dsg = dsgTesting;
        params.datasetPath = datasetPath;
        params.allowUpdate = updateable;

        FusekiServerListener.initialSetup = params;

        JettyServerConfig config = make(port, true);
        config.authConfigFile = authConfigFile;
        JettyFusekiWebapp.initializeServer(config);
        JettyFusekiWebapp.instance.start();
        server = JettyFusekiWebapp.instance;
    }

    /*package*/ static void teardownServer() {
        if ( server != null ) {
            // Clear out the registry.
            server.getDataAccessPointRegistry().clear();
            FileOps.clearAll(FusekiWebapp.dirConfiguration.toFile());
            server.stop();
        }
        server = null;
    }

    /*package*/ static JettyServerConfig make(int port, boolean allowUpdate) {
        JettyServerConfig config = new JettyServerConfig();
        // Avoid any persistent record.
        config.port = port;
        config.contextPath = "/";
        // Always execute tests with localhost only access.
        config.loopback = true;
        config.jettyConfigFile = null;
        config.enableCompression = true;
        config.verboseLogging = false;
        return config;
    }

    /*package*/ static void resetServer() {
        if (countServer.get() == 0)
            throw new RuntimeException("No server started!");
        if ( CLEAR_DSG_DIRECTLY ) {
            Txn.executeWrite(dsgTesting, ()->dsgTesting.clear());
        } else {
            Update clearRequest = new UpdateDrop(Target.ALL);
            UpdateProcessor proc = UpdateExecutionFactory.createRemote(clearRequest, serviceUpdate());
            try {proc.execute(); }
            catch (Throwable e) {e.printStackTrace(); throw e;}
        }
    }
}

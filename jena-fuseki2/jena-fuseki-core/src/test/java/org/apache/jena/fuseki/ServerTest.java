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

package org.apache.jena.fuseki ;

import java.nio.file.Paths ;
import java.util.Collection ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.fuseki.jetty.JettyServerConfig ;
import org.apache.jena.fuseki.jetty.JettyFuseki ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.modify.request.Target ;
import org.apache.jena.sparql.modify.request.UpdateDrop ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.update.Update ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateProcessor ;

/**
 * Manage a server for testing. Example for one server per test suite:
 * 
 * <pre>
 *     \@BeforeClass public static void beforeClass() { ServerTest.allocServer() ; }
 *     \@AfterClass  public static void afterClass()  { ServerTest.freeServer() ; }
 *     \@Before      public void beforeTest()         { ServerTest.resetServer() ; }
 * </pre>
 */
public class ServerTest {
    // Abstraction that runs a SPARQL server for tests.

    public static final int     port          = 3535 ;
    public static final String  urlRoot       = "http://localhost:" + port + "/" ;
    public static final String  datasetPath   = "/dataset" ;
    public static final String  urlDataset    = "http://localhost:" + port + datasetPath ;
    public static final String  serviceUpdate = urlDataset + "/update" ;
    public static final String  serviceQuery  = urlDataset + "/query" ;
    public static final String  serviceREST   = urlDataset + "/data" ;

    public static final String  gn1           = "http://graph/1" ;
    public static final String  gn2           = "http://graph/2" ;
    public static final String  gn99          = "http://graph/99" ;

    public static final Node    n1            = NodeFactory.createURI("http://graph/1") ;
    public static final Node    n2            = NodeFactory.createURI("http://graph/2") ;
    public static final Node    n99           = NodeFactory.createURI("http://graph/99") ;

    public static final Graph   graph1        = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 1)))") ;
    public static final Graph   graph2        = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 2)))") ;

    public static final Model   model1        = ModelFactory.createModelForGraph(graph1) ;
    public static final Model   model2        = ModelFactory.createModelForGraph(graph2) ;

    private static JettyFuseki server        = null ;

    // reference count of start/stop server
    private static int          countServer   = 0 ;

    // This will cause there to be one server over all tests.
    // Must be after initialization of counters
    // static { allocServer() ; }

    static public void allocServer() {
        if ( countServer == 0 )
            setupServer() ;
        countServer++ ;
    }

    static public void freeServer() {
        if ( countServer >= 0 ) {
            countServer-- ;
            if ( countServer == 0 )
                teardownServer() ;
        }
    }

    protected static void setupServer() {
        FusekiEnv.FUSEKI_HOME = Paths.get(TS_Fuseki.FusekiTestHome).toAbsolutePath() ;
        FileOps.ensureDir("target");
        FileOps.ensureDir(TS_Fuseki.FusekiTestHome);
        FileOps.ensureDir(TS_Fuseki.FusekiTestBase) ;
        FusekiEnv.FUSEKI_BASE = Paths.get(TS_Fuseki.FusekiTestBase).toAbsolutePath() ;
        setupServer(ServerTest.port, null, ServerTest.datasetPath) ;
    }
    
    protected static void setupServer(int port, String authConfigFile, String datasetPath) {
        SystemState.location = Location.mem() ;
        SystemState.init$() ;
        
        ServerInitialConfig params = new ServerInitialConfig() ;
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        params.dsg = dsg ;
        params.datasetPath = datasetPath ;
        params.allowUpdate = true ;
        FusekiServerListener.initialSetup = params ;
        
        JettyServerConfig config = make(port, true, true) ;
        config.authConfigFile = authConfigFile ;
        JettyFuseki.initializeServer(config);
        JettyFuseki.instance.start() ;
        server = JettyFuseki.instance ;
    }

    public static JettyServerConfig make(int port, boolean allowUpdate, boolean listenLocal) {
        JettyServerConfig config = new JettyServerConfig() ;
        // Avoid any persistent record.
        config.port = port ;
        config.contextPath = "/" ;
        config.loopback = listenLocal ;
        config.jettyConfigFile = null ;
        config.pages = Fuseki.PagesStatic ;
        config.enableCompression = true ;
        config.verboseLogging = false ;
        return config ;
    }

    protected static void teardownServer() {
        if ( server != null )
            server.stop() ;
        server = null ;
        // Clear out the registry.
        Collection<String> keys = Iter.toList(DataAccessPointRegistry.get().keys().iterator()) ;
        for (String k : keys)
            DataAccessPointRegistry.get().remove(k) ;
        // Clear configuration directory.
        FileOps.clearAll(FusekiServer.dirConfiguration.toFile()) ;
    }

    public static void resetServer() {
        Update clearRequest = new UpdateDrop(Target.ALL) ;
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(clearRequest, ServerTest.serviceUpdate) ;
        proc.execute() ;
    }
}

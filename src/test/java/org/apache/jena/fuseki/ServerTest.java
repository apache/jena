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

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.fuseki.server.FusekiConfig ;
import org.apache.jena.fuseki.server.SPARQLServer ;
import org.apache.jena.fuseki.server.ServerConfig ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.modify.request.Target ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateExecutionFactory ;
import com.hp.hpl.jena.update.UpdateProcessor ;

/** Manage a server for testing.
 * Example for one server per test suite: 
 * <pre>
    \@BeforeClass public static void beforeClass() { ServerTest.allocServer() ; }
    \@AfterClass  public static void afterClass()  { ServerTest.freeServer() ; }
    \@Before      public void beforeTest()         { ServerTest.resetServer() ; }
    </pre>
 */
public class ServerTest
{
    // Abstraction that runs a SPARQL server for tests.
    
    public static final int port             = 3535 ;
    public static final String urlRoot       = "http://localhost:"+port+"/" ;
    public static final String datasetPath   = "/dataset" ;
    public static final String serviceUpdate = "http://localhost:"+port+datasetPath+"/update" ; 
    public static final String serviceQuery  = "http://localhost:"+port+datasetPath+"/query" ; 
    public static final String serviceREST   = "http://localhost:"+port+datasetPath+"/data" ; // ??????
    
    public static final String gn1       = "http://graph/1" ;
    public static final String gn2       = "http://graph/2" ;
    public static final String gn99      = "http://graph/99" ;
    
    public static final Node n1          = NodeFactory.createURI("http://graph/1") ;
    public static final Node n2          = NodeFactory.createURI("http://graph/2") ;
    public static final Node n99         = NodeFactory.createURI("http://graph/99") ;
    
    public static final Graph graph1     = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 1)))") ;
    public static final Graph graph2     = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 2)))") ;
    
    public static final Model model1     = ModelFactory.createModelForGraph(graph1) ;
    public static final Model model2     = ModelFactory.createModelForGraph(graph2) ;
    
    private static SPARQLServer server = null ;
    
    // reference count of start/stop server logging
    private static int countLogging = 0 ; 

    // reference count of start/stop server
    private static int countServer = 0 ; 
    
    // This will cause there to be one server over all tests.
    // Must be after initialization of counters 
    //static { allocServer() ; }

    static public void allocServer()
    {
        if ( countLogging == 0 )
            serverStartLogging() ;
        countLogging++ ;

        if ( countServer == 0 )
            setupServer() ;
        countServer++ ;
    }
    
    static public void freeServer() 
    {
        if ( countServer >= 0 ) {
            countServer -- ;
            if ( countServer == 0 )
                teardownServer() ;
        }
        if ( countLogging >= 0 ) { 
            countLogging-- ;
            if ( countLogging == 0 )
                serverStopLogging() ;
        }
    }
    
    protected static void setupServer()
    {
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        // This must agree with ServerTest
        ServerConfig conf = FusekiConfig.defaultConfiguration(ServerTest.datasetPath, dsg, true) ;
        conf.port = ServerTest.port ;
        conf.pagesPort = ServerTest.port ;
        server = new SPARQLServer(conf) ;
        server.start() ;
    }
    
    protected static void teardownServer() {
        if ( server != null )
            server.stop() ;
        server = null ;
    }
        
    // For tests, the server sits in the background
    // Set logging to WARN only.
    
    protected static void serverStartLogging() {
        Log.logLevel(Fuseki.serverLog.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;
        Log.logLevel(Fuseki.requestLog.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;
        Log.logLevel("org.eclipse.jetty", org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;
        
    }
    
    protected static void serverStopLogging() {
        Log.logLevel(Fuseki.serverLog.getName(), org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
        Log.logLevel(Fuseki.requestLog.getName(), org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
        Log.logLevel("org.eclipse.jetty", org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
    }
    
    public static void resetServer()
    {
        Update clearRequest = new UpdateDrop(Target.ALL) ;
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(clearRequest, ServerTest.serviceUpdate) ;
        proc.execute() ;
    }
}

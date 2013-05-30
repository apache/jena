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

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.modify.request.Target ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateExecutionFactory ;
import com.hp.hpl.jena.update.UpdateProcessor ;

/** Manage a server for testing.
 * <pre>
    \@BeforeClass public static void beforeClass() { ServerTest.allocServer() ; }
    \@AfterClass public static void afterClass() { ServerTest.freeServer() ; }
    </pre>
 */
public class ServerTest extends BaseServerTest
{
    // Abstraction that runs one server.
    // Inherit from this class to add starting/stopping a server.  
    
    private static int referenceCount = 0 ;
    private static SPARQLServer server = null ; 
    
    // If not inheriting from this class, call:
    
    static public void allocServer()
    { 
        if ( referenceCount == 0 )
            serverStart() ;
        referenceCount ++ ;
    }
    
    static public void freeServer() 
    { 
        referenceCount -- ;
        if ( referenceCount == 0 )
            serverStop() ;
    }
    
    protected static void serverStart()
    {
        Log.logLevel(Fuseki.serverLog.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;
        Log.logLevel(Fuseki.requestLog.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;
        Log.logLevel("org.eclipse.jetty", org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;
        
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        // This must agree with BaseServerTest
        ServerConfig conf = FusekiConfig.defaultConfiguration(datasetPath, dsg, true) ;
        conf.port = BaseServerTest.port ;
        conf.pagesPort = BaseServerTest.port ;
//        public static final String serviceUpdate = "http://localhost:"+ServerTest.port+datasetPath+"/update" ; 
//        public static final String serviceQuery  = "http://localhost:"+ServerTest.port+datasetPath+"/query" ; 
//        public static final String serviceREST   = "http://localhost:"+ServerTest.port+datasetPath+"/data" ; // ??????
        
        server = new SPARQLServer(conf) ;
        server.start() ;
    }
    
    protected static void serverStop()
    {
        server.stop() ;
        Log.logLevel(Fuseki.serverLog.getName(), org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
        Log.logLevel(Fuseki.requestLog.getName(), org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
        Log.logLevel("org.eclipse.jetty", org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
        server = null ;
    }
    
    public static void resetServer()
    {
        Update clearRequest = new UpdateDrop(Target.ALL) ;
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(clearRequest, serviceUpdate) ;
        proc.execute() ;
    }
}

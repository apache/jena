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

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.fuseki.server.FusekiConfig ;
import org.apache.jena.fuseki.server.SPARQLServer ;
import org.apache.jena.fuseki.server.ServerConfig ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.tdb.TDBFactory ;

/** Embedded (same JVM) server.
 * <p>Example for one server per test suite: 
 * <pre>
    private static EmbeddedFusekiServer server = null ;
    \@BeforeClass public static void beforeClass() { 
        server = EmbeddedFusekiServer.createMemByPath(3030, "/test") ;
        server.start() ;
    \@AfterClass  public static void afterClass()  { 
        server.stop() ;
    }
    </pre>
 */
public class EmbeddedFusekiServer
{
    
    public static EmbeddedFusekiServer mem(int port, String datasetPath) {
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        return EmbeddedFusekiServer.create(port, dsg, datasetPath) ;
    }
    
    public static EmbeddedFusekiServer memTDB(int port, String datasetPath) {
        DatasetGraph dsg = TDBFactory.createDatasetGraph() ;
        return EmbeddedFusekiServer.create(port, dsg, datasetPath) ;
    }

    public static EmbeddedFusekiServer create(int port, DatasetGraph dsg, String datasetPath) {
        ServerConfig conf = FusekiConfig.defaultConfiguration(datasetPath, dsg, true, true) ;
        conf.port = port ;
        conf.pagesPort = port ;
        if ( ! FileOps.exists(conf.pages) )
            conf.pages = null ;
        return new EmbeddedFusekiServer(conf) ;
    }
        
    public static EmbeddedFusekiServer configure(int port, String fileConfig) {
        ServerConfig conf = FusekiConfig.configure(fileConfig) ;
        conf.port = port ;
        conf.pagesPort = port ;
        if ( ! FileOps.exists(conf.pages) )
            conf.pages = null ;
        return new EmbeddedFusekiServer(conf) ;
    }
    
    private SPARQLServer server = null ;
    
    public EmbeddedFusekiServer(ServerConfig conf) {
        server = new SPARQLServer(conf) ;
    }
    
    public void start() {
        server.start() ;
    }
    
    public void stop() {
        server.stop() ;
    }
}

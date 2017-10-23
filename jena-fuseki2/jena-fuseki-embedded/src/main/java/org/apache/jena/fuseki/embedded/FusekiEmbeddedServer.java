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

import javax.servlet.ServletContext ;

import org.apache.jena.fuseki.server.DataAccessPointRegistry ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.eclipse.jetty.server.Server ;

/**
 * Embedded Fuseki server.
 * @deprecated Use {@link FusekiServer} 
 */
@Deprecated
public class FusekiEmbeddedServer {
    /** @deprecated Use {@link FusekiServer#make} */  
    @Deprecated
    static public FusekiEmbeddedServer make(int port, String name, DatasetGraph dsg) {
        return new FusekiEmbeddedServer(FusekiServer.make(port, name, dsg));
    }
    
    /** @deprecated Use {@link FusekiServer#create} */  
    @Deprecated
    public static FusekiServer.Builder create() {
        return FusekiServer.create();
    }
    
    private final FusekiServer server ;
    
    private FusekiEmbeddedServer(FusekiServer server) {
        this.server = server ;
    }
    
    /** 
     * Return the port begin used.  
     * This will be the give port, which defaults to 3330, or
     * the one actually allocated if the port was 0 ("choose a free port").
     */
    public int getPort() {
        return server.getPort();
    }

    /** Get the underlying Jetty server which has also been set up. */ 
    public Server getJettyServer() {
        return server.getJettyServer();
    }
    
    /** Get the {@link ServletContext}.
     * Adding new servlets is possible with care.
     */ 
    public ServletContext getServletContext() {
        return server.getServletContext() ;
    }

    /** Get the {@link DataAccessPointRegistry}.
     * This method is intended for inspecting the registry.
     */ 
    public DataAccessPointRegistry getDataAccessPointRegistry() {
        return server.getDataAccessPointRegistry() ;
    }

    /** Start the server - the server continues to run after this call returns.
     *  To synchronise with the server stopping, call {@link #join}.  
     */
    public FusekiEmbeddedServer start() { 
        server.start();
        return this ;
    }

    /** Stop the server. */
    public void stop() { 
        server.stop();
    }
    
    /** Wait for the server to exit. This call is blocking. */
    public void join() {
        server.join();
    }
}

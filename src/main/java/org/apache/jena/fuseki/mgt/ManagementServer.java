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

package org.apache.jena.fuseki.mgt;

import static org.apache.jena.fuseki.Fuseki.serverLog ;

import javax.servlet.http.HttpServlet ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.server.SPARQLServer ;
import org.eclipse.jetty.server.Connector ;
import org.eclipse.jetty.server.Server ;
import org.eclipse.jetty.server.nio.BlockingChannelConnector ;
import org.eclipse.jetty.servlet.ServletContextHandler ;
import org.eclipse.jetty.servlet.ServletHolder ;

public class ManagementServer
{
    public static ServletContextHandler addManagementServer(SPARQLServer server, int port) {
        Server jettyServer = server.getServer() ;
        BlockingChannelConnector bcConnector = new BlockingChannelConnector() ;
        // bcConnector.setUseDirectBuffers(false) ;

        Connector connector = bcConnector ;
        connector.setMaxIdleTime(0) ; // Jetty outputs a lot of messages if this goes off.

        //if ( loopback )
        if ( server.getServerPort() == port )
            connector.setHost("localhost");
        connector.setPort(port) ;
        // Control - defaults OK 
//        connector.setRequestHeaderSize(64 * 1024) ;
//        connector.setRequestBufferSize(5 * 1024 * 1024) ;
//        connector.setResponseBufferSize(5 * 1024 * 1024) ;
        jettyServer.addConnector(connector);
        ServletContextHandler context = new ServletContextHandler() ;
        jettyServer.setHandler(context) ;
        return context ; 
    }
    
    public static void addServerFunctions(ServletContextHandler context, String base) {
        Fuseki.serverLog.info("Adding server information functions") ;
        if ( !base.endsWith("/" ) )
            base = base + "/" ;
        if ( !base.startsWith("/"))
            throw new FusekiException("Base URI does not start with a '/'") ; 

        addServlet(context, new DumpServlet(),         base+MgtConst.opDump) ;
        addServlet(context, new ActionDescription(),   base+MgtConst.opStatus) ;
        addServlet(context, new ActionPing(),          base+MgtConst.opPing) ;
    }
    
    public static void addAdminFunctions(ServletContextHandler context, String base) {
        Fuseki.serverLog.info("Adding administration functions") ;
        if ( !base.endsWith("/" ) )
            base = base + "/" ;
        if ( !base.startsWith("/"))
            throw new FusekiException("Base URI does not start with a '/'") ; 
        
        addServlet(context, new ActionStats(),          base+MgtConst.opStats+"/*") ;   // "/abc/*" covers ".../abc" as well.
        addServlet(context, new ActionDatasets(),       base+MgtConst.opDatasets+"/*") ;  
    }
    
    public static void addServlet(ServletContextHandler context, HttpServlet servlet, String pathSpec)
    {
        ServletHolder holder = new ServletHolder(servlet) ;
        serverLog.debug("Add servlet @ "+pathSpec) ;
        context.addServlet(holder, pathSpec) ;
    }
}


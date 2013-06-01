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

import java.util.List ;

import javax.servlet.http.HttpServlet ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.FusekiErrorHandler ;
import org.apache.jena.fuseki.servlets.DumpServlet ;
import org.eclipse.jetty.server.Connector ;
import org.eclipse.jetty.server.Server ;
import org.eclipse.jetty.server.nio.SelectChannelConnector ;
import org.eclipse.jetty.servlet.ServletContextHandler ;
import org.eclipse.jetty.servlet.ServletHolder ;

public class ManagementServer
{
    public static Server createManagementServer(int mgtPort)
    {
        Fuseki.serverLog.info("Adding management functions") ;
        
        // Separate Jetty server
        Server server = new Server() ;
        
//        BlockingChannelConnector bcConnector = new BlockingChannelConnector() ;
//        bcConnector.setUseDirectBuffers(false) ;
//        Connector connector = bcConnector ;
        
        Connector connector = new SelectChannelConnector() ;
        // Ignore idle time. 
        // If set, then if this goes off, it keeps going off and you get a lot of log messages.
        connector.setMaxIdleTime(0) ; // Jetty outputs a lot of messages if this goes off.
        connector.setPort(mgtPort);
        server.addConnector(connector) ;
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setErrorHandler(new FusekiErrorHandler()) ;
        server.setHandler(context);
        
        // Add the server control servlet
        addServlet(context, new MgtCmdServlet(),    "/mgt") ;
        addServlet(context, new DumpServlet(),      "/dump") ;
        addServlet(context, new StatsServlet(),     "/stats") ;
        
        return server ; 
        // Old plan
//      // Development : server control panel.
//      addServlet(context, new ServerServlet(), "/server") ;
//      addServlet(context, new ActionBackup(), "/backup") ;
    }

    // SHARE
    private static void addServlet(ServletContextHandler context, String datasetPath, HttpServlet servlet, List<String> pathSpecs)
    {
        for ( String pathSpec : pathSpecs )
        {
            if ( pathSpec.endsWith("/") )
                pathSpec = pathSpec.substring(0, pathSpec.length()-1) ;
            if ( pathSpec.startsWith("/") )
                pathSpec = pathSpec.substring(1, pathSpec.length()) ;
            addServlet(context, servlet, datasetPath+"/"+pathSpec) ;
        }
    }

    private static void addServlet(ServletContextHandler context, HttpServlet servlet, String pathSpec)
    {
        ServletHolder holder = new ServletHolder(servlet) ;
        addServlet(context, holder, pathSpec) ;
    }
    
    private static void addServlet(ServletContextHandler context, ServletHolder holder, String pathSpec)
    {
        serverLog.debug("Add servlet @ "+pathSpec) ;
        context.addServlet(holder, pathSpec) ;
    }

}


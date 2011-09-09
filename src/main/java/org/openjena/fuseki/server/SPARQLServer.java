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

package org.openjena.fuseki.server;

import static java.lang.String.format ;
import static org.openjena.fuseki.Fuseki.serverLog ;

import java.io.FileInputStream ;
import java.util.List ;

import javax.servlet.http.HttpServlet ;

import org.eclipse.jetty.http.MimeTypes ;
import org.eclipse.jetty.server.Connector ;
import org.eclipse.jetty.server.Server ;
import org.eclipse.jetty.server.nio.BlockingChannelConnector ;
import org.eclipse.jetty.servlet.DefaultServlet ;
import org.eclipse.jetty.servlet.ServletContextHandler ;
import org.eclipse.jetty.servlet.ServletHolder ;
import org.eclipse.jetty.xml.XmlConfiguration ;
import org.openjena.fuseki.Fuseki ;
import org.openjena.fuseki.FusekiException ;
import org.openjena.fuseki.mgt.ActionDataset ;
import org.openjena.fuseki.servlets.DumpServlet ;
import org.openjena.fuseki.servlets.SPARQL_QueryDataset ;
import org.openjena.fuseki.servlets.SPARQL_QueryGeneral ;
import org.openjena.fuseki.servlets.SPARQL_REST_R ;
import org.openjena.fuseki.servlets.SPARQL_REST_RW ;
import org.openjena.fuseki.servlets.SPARQL_Update ;
import org.openjena.fuseki.servlets.SPARQL_Upload ;
import org.openjena.fuseki.validation.DataValidator ;
import org.openjena.fuseki.validation.IRIValidator ;
import org.openjena.fuseki.validation.QueryValidator ;
import org.openjena.fuseki.validation.UpdateValidator ;
import org.openjena.riot.WebContent ;

import com.hp.hpl.jena.sparql.util.Utils ;

public class SPARQLServer
{
    private Server server = null ;
    private String datasetPath ;
    private int port ;
    private boolean verbose = false ;
    
    //private static int ThreadPoolSize = 100 ;
    
    public SPARQLServer(String jettyConfig, int port, List<DatasetRef> services)
    {
        this.port = port ; 
        ServletContextHandler context = buildServer(jettyConfig) ;
        // Build them all.
        for ( DatasetRef sDesc : services )
            configureOneDataset(context, sDesc) ;
    }
    
    public void start()
    {
        String now = Utils.nowAsString() ;
        serverLog.info(format("%s %s", Fuseki.NAME, Fuseki.VERSION)) ;
        String jettyVersion = org.eclipse.jetty.server.Server.getVersion() ;
        serverLog.info(format("Jetty %s",jettyVersion)) ;
        String host = server.getConnectors()[0].getHost();
        if (host != null)
            serverLog.info("Incoming connections limited to " + host);
        serverLog.info(format("Started %s on port %d", now, server.getConnectors()[0].getPort())) ;

        try { server.start() ; }
        catch (java.net.BindException ex)
        { serverLog.error("SPARQLServer: Failed to start server: " + ex.getMessage()) ; System.exit(1) ; }
        catch (Exception ex)
        { serverLog.error("SPARQLServer: Failed to start server: " + ex.getMessage(), ex) ; System.exit(1) ; }
        
        ServletContextHandler context = (ServletContextHandler)server.getHandler() ;
    }

    public void stop()
    {
        String now = Utils.nowAsString() ;
        serverLog.info(format("Stopped %s on port %d", now, server.getConnectors()[0].getPort())) ;
        try { server.stop() ; }
        catch (Exception ex)
        { Fuseki.serverLog.warn("SPARQLServer: Exception while stopping server: " + ex.getMessage(), ex) ; }
    }
    
    public Server getServer() { return server ; }
    
    // Later : private and in constructor.
    private ServletContextHandler buildServer(String jettyConfig)
    {
        if ( jettyConfig != null )
        {
            // --jetty-config=jetty-fuseki.xml
            // for detailed configuration of the server using Jetty features. 
            server = configServer(jettyConfig) ;
        }
        else 
            server = defaultServerConfig(port) ; 
        // Keep the server to a maximum number of threads.
        //server.setThreadPool(new QueuedThreadPool(ThreadPoolSize)) ;

        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setErrorHandler(new FusekiErrorHandler()) ;
        server.setHandler(context);
        // Constants. Add RDF types.
        MimeTypes mt = new MimeTypes() ; 
        mt.addMimeMapping("rdf",    WebContent.contentTypeRDFXML+";charset=utf-8") ;
        mt.addMimeMapping("ttl",    WebContent.contentTypeTurtle+";charset=utf-8") ;
        mt.addMimeMapping("nt",     WebContent.contentTypeNTriples+";charset=ascii") ;
        mt.addMimeMapping("nq",     WebContent.contentTypeNQuads+";charset=ascii") ;
        mt.addMimeMapping("trig",   WebContent.contentTypeTriG+";charset=utf-8") ;
        context.setMimeTypes(mt) ;
        
        
        // Fixed for now.
        String pages = true ? Fuseki.PagesAll : Fuseki.PagesPublish ; 
        serverLog.debug("Pages = "+pages) ;
        
        boolean installManager = true ;
        boolean installServices = true ;
        
        String validationRoot = "/validate" ;
        String sparqlProcessor = "/sparql" ;
        
        if ( installManager || installServices)
        {
            server.setHandler(context);

            HttpServlet jspServlet = new org.apache.jasper.servlet.JspServlet() ;
            ServletHolder jspContent = new ServletHolder(jspServlet) ;
            //?? Need separate context for admin stuff??
            context.setResourceBase(pages) ;
            addServlet(context, jspContent, "*.jsp") ;
        }
        
        
        if ( installManager )
        {
            // Action when control panel selects a dataset.
            HttpServlet datasetChooser = new ActionDataset() ;
            addServlet(context, datasetChooser, "/dataset") ;
        }
        
        if ( installServices )
        {
            // Validators
            HttpServlet validateQuery = new QueryValidator() ;
            HttpServlet validateUpdate = new UpdateValidator() ;
            HttpServlet validateData = new DataValidator() ;    
            HttpServlet validateIRI = new IRIValidator() ;
            
            HttpServlet dumpService = new DumpServlet() ;
            HttpServlet generalQueryService = new SPARQL_QueryGeneral() ;
            
            addServlet(context, validateQuery, validationRoot+"/query") ;
            addServlet(context, validateUpdate, validationRoot+"/update") ;
            addServlet(context, validateData, validationRoot+"/data") ;
            addServlet(context, validateIRI, validationRoot+"/iri") ;
            addServlet(context, dumpService, "/dump") ;
            // general query processor.
            addServlet(context, generalQueryService, sparqlProcessor) ;
        }
        
        if ( installManager || installServices )
        {
            String [] files = { "fuseki.html" } ;
            context.setWelcomeFiles(files) ;
            //  if this is /* then don't see *.jsp. Why?
            addContent(context, "/", pages) ;
        }
        
        return context ; 
        
    }
    
    private void configureOneDataset(ServletContextHandler context, DatasetRef sDesc)
    {
        String datasetPath = sDesc.name ;
        if ( datasetPath.equals("/") )
            datasetPath = "" ;
        else if ( ! datasetPath.startsWith("/") )
            datasetPath = "/"+datasetPath ;
        
        if ( datasetPath.endsWith("/") )
            datasetPath = datasetPath.substring(0, datasetPath.length()-1) ; 

        DatasetRegistry.get().put(datasetPath, sDesc) ;
        serverLog.info(format("Dataset path = %s", datasetPath)) ;
        
        HttpServlet sparqlQuery = new SPARQL_QueryDataset(verbose) ;
        HttpServlet sparqlUpdate = new SPARQL_Update(verbose) ;
        HttpServlet sparqlUpload = new SPARQL_Upload(verbose) ;
        HttpServlet sparqlHttpR = new SPARQL_REST_R(verbose) ;  
        HttpServlet sparqlHttpRW = new SPARQL_REST_RW(verbose) ;
        
        addServlet(context, datasetPath, sparqlQuery, sDesc.queryEP) ;
        addServlet(context, datasetPath, sparqlUpdate, sDesc.updateEP) ;
        addServlet(context, datasetPath, sparqlUpload, sDesc.uploadEP) ;
        addServlet(context, datasetPath, sparqlHttpR, sDesc.readGraphStoreEP) ;
        addServlet(context, datasetPath, sparqlHttpRW, sDesc.readWriteGraphStoreEP) ;
    }
    
    private static Server configServer(String jettyConfig)
    {
        try {
            serverLog.info("Jetty server config file = "+jettyConfig) ;
            Server server = new Server() ;
            XmlConfiguration configuration = new XmlConfiguration(new FileInputStream(jettyConfig)) ;
            configuration.configure(server) ;
            return server ;
        } catch (Exception ex)
        {
            serverLog.error("SPARQLServer: Failed to configure server: " + ex.getMessage(), ex) ;
            throw new FusekiException("Failed to configure a server using configuration file '"+jettyConfig+"'") ; 
        }
    }

    private static Server defaultServerConfig(int port)
    {
        // Server, with one NIO-based connector, large input buffer size (for long URLs, POSTed forms (queries, updates)).
        Server server = new Server();
        
        // Using "= new SelectChannelConnector() ;" on Darwin (OS/X) causes problems 
        // with initialization not seen (thread scheduling?) in Joseki.
        
        // BlockingChannelConnector is better for pumping large responses back
        // but there have been observed problems with DirectMemory allocation
        // (-XX:MaxDirectMemorySize=1G does not help)
        // Connector connector = new SelectChannelConnector() ;
        
        // Connector and specific settings.
        BlockingChannelConnector bcConnector = new BlockingChannelConnector() ;
        //bcConnector.setUseDirectBuffers(false) ;
        
        Connector connector = bcConnector ;
        // Ignore. If set, then if this goes off, it keeps going off 
        // and you get a lot of log messages.
        connector.setMaxIdleTime(0) ; // Jetty outputs a lot of messages if this goes off.
        connector.setPort(port);
        // Some people do try very large operations ...
        connector.setRequestHeaderSize(64*1024) ;
        connector.setRequestBufferSize(5*1024*1024) ;
        connector.setResponseBufferSize(5*1024*1024) ;
        server.addConnector(connector) ;
        return server ;
    }

    private static void addContent(ServletContextHandler context, String pathSpec, String pages)
    {
        DefaultServlet staticServlet = new DefaultServlet() ;
        ServletHolder staticContent = new ServletHolder(staticServlet) ;
        staticContent.setInitParameter("resourceBase", pages) ;
        addServlet(context, staticContent, pathSpec) ;
    }

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

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

package org.apache.jena.fuseki.server;

import static java.lang.String.format ;
import static org.apache.jena.fuseki.Fuseki.serverLog ;

import java.io.FileInputStream ;
import java.util.* ;

import javax.servlet.DispatcherType ;
import javax.servlet.http.HttpServlet ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.mgt.ActionDataset ;
import org.apache.jena.fuseki.mgt.MgtFunctions ;
import org.apache.jena.fuseki.mgt.PageNames ;
import org.apache.jena.fuseki.servlets.* ;
import org.apache.jena.fuseki.validation.DataValidator ;
import org.apache.jena.fuseki.validation.IRIValidator ;
import org.apache.jena.fuseki.validation.QueryValidator ;
import org.apache.jena.fuseki.validation.UpdateValidator ;
import org.apache.jena.riot.WebContent ;
import org.eclipse.jetty.http.MimeTypes ;
import org.eclipse.jetty.server.Connector ;
import org.eclipse.jetty.server.Server ;
import org.eclipse.jetty.server.nio.BlockingChannelConnector ;
import org.eclipse.jetty.servlet.DefaultServlet ;
import org.eclipse.jetty.servlet.ServletContextHandler ;
import org.eclipse.jetty.servlet.ServletHolder ;
import org.eclipse.jetty.xml.XmlConfiguration ;

import org.eclipse.jetty.servlets.GzipFilter;


import com.hp.hpl.jena.sparql.util.Utils ;

public class SPARQLServer
{
    static { Fuseki.init() ; }
    
    private ServerConfig serverConfig ;
    
    private Server server = null ;
    private boolean verboseLogging = false ;
    private static List<String> epDataset = Arrays.asList("*") ;
    
    //private static int ThreadPoolSize = 100 ;
    
    public SPARQLServer(ServerConfig config)
    {
        this.serverConfig = config ; 
        verboseLogging = config.verboseLogging ;
        
        // GZip compression
        // Note that regardless of this setting we'll always leave it turned off for the servlets
        // where it makes no sense to have it turned on e.g. update and upload
        
        ServletContextHandler context = buildServer(serverConfig.jettyConfigFile, config.enableCompression) ;
        // Build them all.
        for ( DatasetRef sDesc : serverConfig.services )
            configureOneDataset(context, sDesc,  config.enableCompression) ;
    }
    
    public void start()
    {
        String now = Utils.nowAsString() ;
        //serverLog.info(format("%s %s", Fuseki.NAME, Fuseki.VERSION)) ;
        serverLog.info(format("%s %s %s", Fuseki.NAME, Fuseki.VERSION, Fuseki.BUILD_DATE)) ;
        // This does not get set usefully for Jetty as we use it.
//        String jettyVersion = org.eclipse.jetty.server.Server.getVersion() ;
//        serverLog.info(format("Jetty %s",jettyVersion)) ;
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
    public List<DatasetRef> getDatasets() { return serverConfig.services ; }
    
    public ServerConfig getServerConfig()
    {
        return serverConfig ;
    }

    // Later : private and in constructor.
    private ServletContextHandler buildServer(String jettyConfig, boolean enableCompression)
    {
        if ( jettyConfig != null )
        {
            // --jetty-config=jetty-fuseki.xml
            // for detailed configuration of the server using Jetty features. 
            server = configServer(jettyConfig) ;
        }
        else 
            server = defaultServerConfig(serverConfig.port) ; 
        // Keep the server to a maximum number of threads.
        //server.setThreadPool(new QueuedThreadPool(ThreadPoolSize)) ;
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setErrorHandler(new FusekiErrorHandler()) ;
        // Increase form size.
        context.getServletContext().getContextHandler().setMaxFormContentSize(10*1000*1000) ;
        server.setHandler(context);
        
        // Constants. Add RDF types.
        MimeTypes mt = new MimeTypes() ; 
        mt.addMimeMapping("rdf",    WebContent.contentTypeRDFXML+";charset=utf-8") ;
        mt.addMimeMapping("ttl",    WebContent.contentTypeTurtle+";charset=utf-8") ;
        mt.addMimeMapping("nt",     WebContent.contentTypeNTriples+";charset=ascii") ;
        mt.addMimeMapping("nq",     WebContent.contentTypeNQuads+";charset=ascii") ;
        mt.addMimeMapping("trig",   WebContent.contentTypeTriG+";charset=utf-8") ;
        
        //mt.addMimeMapping("tpl",    "text/html;charset=utf-8") ;
        context.setMimeTypes(mt) ;
        server.setHandler(context);

        serverLog.debug("Pages = "+serverConfig.pages) ;
        
        boolean installManager = true ;
        boolean installServices = true ;
        
        String validationRoot = "/validate" ;
        String sparqlProcessor = "/sparql" ;
        
        // Should all services be /_/.... or some such?
        
        if ( installManager || installServices )
        {
            // TODO Respect port.
            if ( serverConfig.pagesPort != serverConfig.port )
                serverLog.warn("Not supported yet - pages on a different port to services") ;
            
            String base = serverConfig.pages ;
            Map<String, Object> data = new HashMap<String, Object>() ;
            data.put("mgt", new MgtFunctions()) ;
            SimpleVelocityServlet templateEngine = new SimpleVelocityServlet(base, data) ;
            addServlet(context, templateEngine, "*.tpl", false) ;
        }
        
        if ( installManager )
        {
            // Action when control panel selects a dataset.
            HttpServlet datasetChooser = new ActionDataset() ;
            addServlet(context, datasetChooser, PageNames.actionDatasetNames, false) ;
        }
        
        if ( installServices )
        {
            // Validators
            HttpServlet validateQuery = new QueryValidator() ;
            HttpServlet validateUpdate = new UpdateValidator() ;
            HttpServlet validateData = new DataValidator() ;    
            HttpServlet validateIRI = new IRIValidator() ;
            
            HttpServlet statsService = new StatsServlet() ;
            HttpServlet dumpService = new DumpServlet() ;
            HttpServlet generalQueryService = new SPARQL_QueryGeneral() ;
            // TODO Name management 
            addServlet(context, validateQuery, validationRoot+"/query", false) ;
            addServlet(context, validateUpdate, validationRoot+"/update", false) ;
            addServlet(context, validateData, validationRoot+"/data", false) ;
            addServlet(context, validateIRI, validationRoot+"/iri", false) ;
            
            // general query processor.
            addServlet(context, generalQueryService, sparqlProcessor, enableCompression) ;
        }
        
        if ( installManager || installServices )
        {
            String [] files = { "fuseki.html", "index.html" } ;
            context.setWelcomeFiles(files) ;
            addContent(context, "/", serverConfig.pages) ;
        }
        
        return context ; 
        
    }
    
    // Experimental - off by default.
    // The überservlet sits on the dataset name and handles all requests.
    // Includes direct naming and quad access to the dataset.
    public static boolean überServlet = false ;
    private static List<String> ListOfEmptyString = Arrays.asList("") ;
    
    private void configureOneDataset(ServletContextHandler context, DatasetRef sDesc, boolean enableCompression)
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
        
        HttpServlet sparqlQuery     = new SPARQL_QueryDataset(verboseLogging) ;
        HttpServlet sparqlUpdate    = new SPARQL_Update(verboseLogging) ;
        HttpServlet sparqlUpload    = new SPARQL_Upload(verboseLogging) ;
        HttpServlet sparqlHttpR     = new SPARQL_REST_R(verboseLogging) ;  
        HttpServlet sparqlHttpRW    = new SPARQL_REST_RW(verboseLogging) ;
        HttpServlet sparqlDataset   = new SPARQL_UberServlet.AccessByConfig(verboseLogging) ;

        if ( ! überServlet )
        {
            // If uberserver, these are unnecessary but can be used.
            // If just means the überservlet isn't handling these operations. 
            addServlet(context, datasetPath, sparqlQuery,   sDesc.queryEP,    enableCompression) ;
            addServlet(context, datasetPath, sparqlUpdate,  sDesc.updateEP,   false) ;
            addServlet(context, datasetPath, sparqlUpload,  sDesc.uploadEP,   false) ;    // No point - no results of any size.
            addServlet(context, datasetPath, sparqlHttpR,   sDesc.readGraphStoreEP,       enableCompression) ;
            addServlet(context, datasetPath, sparqlHttpRW,  sDesc.readWriteGraphStoreEP,  enableCompression) ;
            // This adds direct operations on the dataset itself. 
            //addServlet(context, datasetPath, sparqlDataset, ListOfEmptyString, enableCompression) ;
        }
        else
        {
            // This is the servlet that analyses requests and dispatches them to the appropriate servlet.
            //    SPARQL Query, SPARQL Update -- handles dataset?query=  dataset?update=
            //    Graph Store Protocol (direct and indirect naming) if enabled.
            //    GET/PUT/POST on the dataset itself.
            // It also checks for a request that looks like a service request and passes it
            // on to the service (this takes precedence over direct naming).
            addServlet(context, datasetPath, sparqlDataset, epDataset, enableCompression) ;
        }
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
        
        //Note we set GZip to false for static content because the Jetty DefaultServlet has
        //a built-in GZip capability that is better for static content than the mechanism the
        //GzipFilter uses for dynamic content
        addServlet(context, staticContent, pathSpec, false) ;
    }

    // SHARE
    private static void addServlet(ServletContextHandler context, String datasetPath, HttpServlet servlet, List<String> pathSpecs, boolean enableCompression)
    {
        for ( String pathSpec : pathSpecs )
        {
            if ( pathSpec.equals("") )
            {
                // "" is special -- add as "base" and "base/" 
                addServlet(context, servlet, datasetPath+"/", enableCompression) ;
                addServlet(context, servlet, datasetPath,     enableCompression) ;
                continue ;
            }
            
            if ( pathSpec.endsWith("/") )
                pathSpec = pathSpec.substring(0, pathSpec.length()-1) ;
            if ( pathSpec.startsWith("/") )
                pathSpec = pathSpec.substring(1, pathSpec.length()) ;
            addServlet(context, servlet, datasetPath+"/"+pathSpec, enableCompression) ;
        }
    }

    private static void addServlet(ServletContextHandler context, HttpServlet servlet, String pathSpec, boolean enableCompression)
    {
        ServletHolder holder = new ServletHolder(servlet) ;
        addServlet(context, holder, pathSpec, enableCompression) ;
    }
    
    private static void addServlet(ServletContextHandler context, ServletHolder holder, String pathSpec, boolean enableCompression)
    {
        if ( serverLog.isDebugEnabled() )
        {
            if ( enableCompression )
                serverLog.debug("Add servlet @ "+pathSpec+" (with gzip)") ;
            else
                serverLog.debug("Add servlet @ "+pathSpec) ;
        }
        context.addServlet(holder, pathSpec) ;

        if (enableCompression)
            context.addFilter(GzipFilter.class, pathSpec, EnumSet.allOf(DispatcherType.class));
    }

}

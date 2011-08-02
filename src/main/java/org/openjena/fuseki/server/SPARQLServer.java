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

import javax.servlet.http.HttpServlet ;

import org.eclipse.jetty.http.MimeTypes ;
import org.eclipse.jetty.server.Connector ;
import org.eclipse.jetty.server.Server ;
import org.eclipse.jetty.server.nio.BlockingChannelConnector ;
import org.eclipse.jetty.servlet.DefaultServlet ;
import org.eclipse.jetty.servlet.ServletContextHandler ;
import org.eclipse.jetty.servlet.ServletHolder ;
import org.eclipse.jetty.xml.XmlConfiguration ;
import org.openjena.atlas.logging.Log ;
import org.openjena.fuseki.Fuseki ;
import org.openjena.fuseki.FusekiException ;
import org.openjena.fuseki.HttpNames ;
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
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class SPARQLServer
{
    static { Log.setLog4j() ; }
    public static final Logger log = LoggerFactory.getLogger(SPARQLServer.class) ;
    private Server server = null ;
    private String datasetPath ;
    private int port ;
    private boolean verbose = false ;
    private boolean enableUpdate = false ;
    
    //private static int ThreadPoolSize = 100 ;
    
    public SPARQLServer(String jettyConfig, DatasetGraph dsg, String datasetPath, int port, boolean allowUpdate, boolean verbose)
    {
        this.port = port ;
        this.datasetPath = datasetPath ;
        this.enableUpdate = allowUpdate ;
        this.verbose = verbose ;
        init(jettyConfig, dsg, datasetPath, port) ;
    }
    
    public SPARQLServer(String jettyConfig, DatasetGraph dsg, String datasetPath, int port, boolean allowUpdate)
    {
        this(jettyConfig, dsg, datasetPath, port, allowUpdate, false) ;
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
        { log.error("SPARQLServer: Failed to start server: " + ex.getMessage()) ; System.exit(1) ; }
        catch (Exception ex)
        { log.error("SPARQLServer: Failed to start server: " + ex.getMessage(), ex) ; System.exit(1) ; }
        
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
    
    private void init(String jettyConfig, DatasetGraph dsg, String datasetPath, int port)
    {
        if ( datasetPath.equals("/") )
            datasetPath = "" ;
        else if ( ! datasetPath.startsWith("/") )
            datasetPath = "/"+datasetPath ;
        
        if ( datasetPath.endsWith("/") )
            datasetPath = datasetPath.substring(0, datasetPath.length()-1) ; 
        
        if ( jettyConfig != null )
        {
            // --jetty-config=jetty-fuseki.xml
            // for detailed configuration of the server using Jetty features. 
            server = configServer(jettyConfig) ;
        }
        else 
            server = defaultServerConfig(port) ; 
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setErrorHandler(new FusekiErrorHandler()) ;
        server.setHandler(context);
        // Constants. Add RDF types.
        MimeTypes mt = new MimeTypes() ; 
        mt.addMimeMapping("rdf",    WebContent.contentTypeRDFXML+";charset=utf-8") ;
        mt.addMimeMapping("ttl",    WebContent.contentTypeTurtle1+";charset=utf-8") ;
        mt.addMimeMapping("nt",     WebContent.contentTypeNTriples+";charset=ascii") ;
        mt.addMimeMapping("nq",     WebContent.contentTypeNQuads+";charset=ascii") ;
        mt.addMimeMapping("trig",   WebContent.contentTypeTriG+";charset=utf-8") ;
        context.setMimeTypes(mt) ;
        
        String[] datasets = { datasetPath } ;
        DatasetRegistry.get().put(datasetPath, dsg) ;
        
        String validationRoot = "/validate" ;
        String sparqlProcessor = "/sparql" ;    // Combine with validators in /services/?
        boolean installServices = true ;
        boolean installManager = false ;

        // Static pages to install.
        // Make very sure non-update does not leave holes. 
        String pages = enableUpdate ? Fuseki.PagesAll : Fuseki.PagesPublish ; 
        
        if ( enableUpdate )
            installManager = true ;
            
        serverLog.info(enableUpdate ? "Update enabled" : "Read-only server") ;
        
        // Set the max form size much higher.
        context.setMaxFormContentSize(1*1024*1024) ;
        
        for ( String dsPath : datasets )
        {
            serverLog.info(format("Dataset = %s", dsPath)) ;
            HttpServlet sparqlQuery = new SPARQL_QueryDataset(verbose) ;
            HttpServlet sparqlHttp = 
                enableUpdate 
                ? new SPARQL_REST_RW(verbose) 
                : new SPARQL_REST_R(verbose) ;
            
            // SPARQL services per dataset
            addServlet(context, sparqlHttp, dsPath);                                // URI: /dataset
            addServlet(context, sparqlHttp, dsPath+HttpNames.ServiceData) ;         // URI: /dataset/data
            addServlet(context, sparqlQuery, dsPath+HttpNames.ServiceQuery) ;       // URI: /dataset/query
            addServlet(context, sparqlQuery, dsPath+HttpNames.ServiceQueryAlt) ;    // URI: /dataset/sparql -- Alternative name

            if ( enableUpdate )
            {
                HttpServlet sparqlUpdate = new SPARQL_Update(verbose) ;
                addServlet(context, sparqlUpdate, dsPath+HttpNames.ServiceUpdate) ; // URI: /dataset/update
                
                HttpServlet sparqlUpload = new SPARQL_Upload(verbose) ;
                addServlet(context, sparqlUpload, dsPath+HttpNames.ServiceUpload) ; // URI: /dataset/upload
            }
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

        if ( installManager )
        {
            server.setHandler(context);

            HttpServlet jspServlet = new org.apache.jasper.servlet.JspServlet() ;
            ServletHolder jspContent = new ServletHolder(jspServlet) ;
            //?? Need separate context for admin stuff??
            context.setResourceBase(pages) ;
            addServlet(context, jspContent, "*.jsp") ;

            // Action when control panel selects a dataset.
            HttpServlet datasetChooser = new ActionDataset() ;
            addServlet(context, datasetChooser, "/dataset") ;
        }
        
        if ( installManager || installServices )
        {
            String [] files = { "fuseki.html" } ;
            context.setWelcomeFiles(files) ;
            //  if this is /* then don't see *.jsp. Why?
            addContent(context, "/", pages) ;
        }
        
        // TEST
//            // Add the webapp.
//            webAppContextJoseki = new WebAppContext(server, "webapps/joseki", "/") ;
//            server.addHandler(webAppContextJoseki) ;
            
//            WebAppContext context = new WebAppContext();
//            context.setDescriptor(webapp+"/WEB-INF/web.xml");
//            context.setResourceBase("../test-jetty-webapp/src/main/webapp");
//            context.setContextPath("/");
//            context.setParentLoaderPriority(true);Exception ex)
    }
    
    private static Server configServer(String jettyConfig)
    {
        try {
            serverLog.info("Jetty server config file = "+jettyConfig) ;
            Server server = new Server();
            XmlConfiguration configuration = new XmlConfiguration(new FileInputStream(jettyConfig));
            configuration.configure(server);
            return server ;
        } catch (Exception ex)
        {
            log.error("SPARQLServer: Failed to start server: " + ex.getMessage(), ex) ;
            throw new FusekiException("Failed to create a server using configuration file '"+jettyConfig+"'") ; 
        }
    }

    private static Server defaultServerConfig(int port)
    {
        // Server, with one NIO-based connector, large input buffer size (for long URLs, POSTed forms (queries, updates)).
        Server server = new Server();
        
        // Keep the server to a maximum number of threads.
        // Issue - the test suite seems to need a lot of threads (>50) - lack of close?
        
        //server.setThreadPool(new QueuedThreadPool(ThreadPoolSize)) ;
        
        // Using "= new SelectChannelConnector() ;" on Darwin (OS/X) causes problems 
        // with initialization not seen (thread scheduling?) in Joseki.
        
        // BlockingChannelConnector is better for pumping large responses back
        // but there have been observed problems with DiretcMemory allocation
        // (-XX:MaxDirectMemorySize= does not help)
        // Connector connector = new SelectChannelConnector() ;
        
        // Connector and specific settings.
        BlockingChannelConnector bcConnector = new BlockingChannelConnector() ;
        //bcConnector.setUseDirectBuffers(false) ;
        
        Connector connector = bcConnector ;
        // Ignore. If set, then if this goes off, it keeps going off.
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

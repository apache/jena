/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.server;

import static java.lang.String.format ;
import static org.openjena.fuseki.Fuseki.serverlog ;

import javax.servlet.http.HttpServlet ;

import org.eclipse.jetty.http.MimeTypes ;
import org.eclipse.jetty.server.Connector ;
import org.eclipse.jetty.server.Server ;
import org.eclipse.jetty.server.nio.BlockingChannelConnector ;
import org.eclipse.jetty.servlet.DefaultServlet ;
import org.eclipse.jetty.servlet.ServletContextHandler ;
import org.eclipse.jetty.servlet.ServletHolder ;
import org.openjena.atlas.logging.Log ;
import org.openjena.fuseki.Fuseki ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.mgt.ActionDataset ;
import org.openjena.fuseki.servlets.SPARQL_QueryDataset ;
import org.openjena.fuseki.servlets.SPARQL_REST_R ;
import org.openjena.fuseki.servlets.SPARQL_REST_RW ;
import org.openjena.fuseki.servlets.SPARQL_Update ;
import org.openjena.fuseki.servlets.SPARQL_Upload ;
import org.openjena.fuseki.validation.DataValidator ;
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
    
    public SPARQLServer(DatasetGraph dsg, String datasetPath, int port, boolean allowUpdate, boolean verbose)
    {
        this.port = port ;
        this.datasetPath = datasetPath ;
        this.enableUpdate = allowUpdate ;
        this.verbose = verbose ;
        init(dsg, datasetPath, port) ;
    }
    
    public SPARQLServer(DatasetGraph dsg, String datasetPath, int port, boolean allowUpdate)
    {
        this(dsg, datasetPath, port, allowUpdate, false) ;
    }
    
    public void start()
    {
        String now = Utils.nowAsString() ;
        serverlog.info(format("%s %s", Fuseki.NAME, Fuseki.VERSION)) ;
        String jettyVersion = org.eclipse.jetty.server.Server.getVersion() ;
        serverlog.info(format("Jetty %s",jettyVersion)) ;
        serverlog.info(format("Dataset = %s", datasetPath)) ;
        serverlog.info(format("Started %s on port %d", now, server.getConnectors()[0].getPort())) ;

        try { server.start() ; }
        catch (Exception ex)
        { log.error("SPARQLServer: Failed to start server: " + ex.getMessage(), ex) ; }
        
        ServletContextHandler context = (ServletContextHandler)server.getHandler() ;
    }

    public void stop()
    {
        String now = Utils.nowAsString() ;
        serverlog.info(format("Stopped %s on port %d", now, server.getConnectors()[0].getPort())) ;
        try { server.stop() ; }
        catch (Exception ex)
        { log.warn("SPARQLServer: Exception while stopping server: " + ex.getMessage(), ex) ; }
    }
    
    public Server getServer() { return server ; }
    
    private void init(DatasetGraph dsg, String datasetPath, int port)
    {
        if ( datasetPath.equals("/") )
            datasetPath = "" ;
        else if ( ! datasetPath.startsWith("/") )
            datasetPath = "/"+datasetPath ;
        
        if ( datasetPath.endsWith("/") )
            datasetPath = datasetPath.substring(0, datasetPath.length()-1) ; 
        
        // Server, with one NIO-based connector, large input buffer size (for long URLs, POSTed forms (queries, updates)).
        server = new Server();
        // Using "= new SelectChannelConnector() ;" on Darwin (OS/X) causes problems 
        // with initialization not seen (thread scheduling?) in Joseki.
        Connector connector = new BlockingChannelConnector() ;
        connector.setMaxIdleTime(1000) ; // Jetty outputs a lot of messages if this goes off.
        connector.setPort(port);
        
        // Some people do try very large operations ...
        connector.setRequestBufferSize(1*1024*1024) ;
        
        server.addConnector(connector) ;

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
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
        boolean installValidators = true ;
        boolean installManager = false ;

        // Static pages to install.
        // Make very sure non-update does not leave holes. 
        String pages = enableUpdate ? Fuseki.PagesAll : Fuseki.PagesPublish ; 
        
        if ( enableUpdate )
            installManager = true ;
            
        serverlog.info(enableUpdate ? "Update enabled" : "Read-only server") ;
        
        for ( String dsPath : datasets )
        {
            HttpServlet sparqlQuery = new SPARQL_QueryDataset(verbose) ;
            HttpServlet sparqlHttp = 
                enableUpdate 
                ? new SPARQL_REST_RW(verbose) 
                : new SPARQL_REST_R(verbose) ;
            
            // SPARQL services.
            addServlet(context, sparqlHttp, dsPath);                                // URI: /dataset
            addServlet(context, sparqlHttp, dsPath+HttpNames.ServiceData) ;         // URI: /dataset/data
            addServlet(context, sparqlQuery, dsPath+HttpNames.ServiceQuery) ;       // URI: /dataset/query
            addServlet(context, sparqlQuery, dsPath+HttpNames.ServiceQueryAlt) ;    // URI: /dataset/sparql -- Alternative name
            //add(context, new DumpServlet(),"/dump");

            if ( enableUpdate )
            {
                HttpServlet sparqlUpdate = new SPARQL_Update(verbose) ;
                addServlet(context, sparqlUpdate, dsPath+HttpNames.ServiceUpdate) ; // URI: /dataset/update
                
                HttpServlet sparqlUpload = new SPARQL_Upload(verbose) ;
                addServlet(context, sparqlUpload, dsPath+HttpNames.ServiceUpload) ; // URI: /dataset/upload
            }
        }
        
        
        if ( installValidators )
        {
            // Validators
            HttpServlet validateQuery = new QueryValidator() ;
            HttpServlet validateUpdate = new UpdateValidator() ;
            HttpServlet validateData = new DataValidator() ;    
            addServlet(context, validateQuery, validationRoot+"/query") ;
            addServlet(context, validateUpdate, validationRoot+"/update") ;
            addServlet(context, validateData, validationRoot+"/data") ;
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
        
        if ( installManager || installValidators )
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
        context.addServlet(holder, pathSpec) ;
    }

}


/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
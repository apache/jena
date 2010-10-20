/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.server;

import static java.lang.String.format ;
import static org.openjena.fuseki.Fuseki.serverlog ;
import org.eclipse.jetty.server.Connector ;
import org.eclipse.jetty.server.Server ;
import org.eclipse.jetty.server.nio.BlockingChannelConnector ;
import org.eclipse.jetty.servlet.DefaultServlet ;
import org.eclipse.jetty.servlet.ServletContextHandler ;
import org.eclipse.jetty.servlet.ServletHolder ;
import org.openjena.atlas.logging.Log ;
import org.openjena.fuseki.Fuseki ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.servlets.SPARQL_QueryDataset ;
import org.openjena.fuseki.servlets.SPARQL_REST_RW ;
import org.openjena.fuseki.servlets.SPARQL_Update ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class SPARQLServer
{
    // fuseki [--mem|--desc assembler.ttl] [--port PORT] 
    static { Log.setLog4j() ; }
    public static final Logger log = LoggerFactory.getLogger(SPARQLServer.class) ;
    private Server server = null ;
    private String datasetPath ;
    private int port ;
    private boolean verbose = false ;
    
    public Server getServer() { return server ; }
    
    public void start()
    {
        String now = Utils.nowAsString() ;
        serverlog.info(format("%s %s", Fuseki.NAME, Fuseki.VERSION, now)) ;
        String jettyVersion = org.eclipse.jetty.server.Server.getVersion() ;
        serverlog.info("Jetty " + jettyVersion) ;
        serverlog.info(format("Dataset = %s", datasetPath)) ;
        serverlog.info(format("Started %s on port %d", now, server.getConnectors()[0].getPort())) ;

        try { server.start() ; }
        catch (Exception ex)
        { log.error("SPARQLServer: Failed to start server: " + ex.getMessage(), ex) ; }
    }

    public void stop()
    {
        String now = Utils.nowAsString() ;
        serverlog.info(format("Stopped %s on port %d", now, server.getConnectors()[0].getPort())) ;
        try { server.stop() ; }
        catch (Exception ex)
        { log.warn("SPARQLServer: Exception while stopping server: " + ex.getMessage(), ex) ; }
    }
    
    public SPARQLServer(DatasetGraph dsg, String datasetPath, int port, boolean verbose)
    {
        this.port = port ;
        this.datasetPath = datasetPath ;
        this.verbose = verbose ;
        init(dsg, datasetPath, port) ;
    }
    
    public SPARQLServer(DatasetGraph dsg, String datasetPath, int port)
    {
        this(dsg, datasetPath, port, false) ;
    }
    
    private void init(DatasetGraph dsg, String datasetPath, int port)
    {
        if ( datasetPath.equals("/") )
            datasetPath = "" ;
        else if ( ! datasetPath.startsWith("/") )
            datasetPath = "/"+datasetPath ;
        
        // Server, with one NIO-based connector, large input buffer size (for long URLs).
        server = new Server();
        // Using "= new SelectChannelConnector() ;" on Darwin (OS/X) causes problems 
        // with initialization not seen (thread scheduling?) in Joseki.
        Connector connector = new BlockingChannelConnector() ;
        connector.setPort(port);
        connector.setRequestBufferSize(16*1024) ;
        server.addConnector(connector) ;

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        DatasetRegistry.get().put(datasetPath, dsg) ;
        
        String[] datasets = { datasetPath } ;
        
        for ( String dsPath : datasets )
        {
            ServletHolder sparqlQuery = new ServletHolder(new SPARQL_QueryDataset(verbose)) ;
            ServletHolder sparqlUpdate = new ServletHolder(new SPARQL_Update(verbose)) ;
            ServletHolder sparqlHttp = new ServletHolder(new SPARQL_REST_RW(verbose)) ;
            
            // SPARQL services.
            context.addServlet(sparqlHttp, dsPath);
            context.addServlet(sparqlHttp, dsPath+HttpNames.ServiceData) ;
            context.addServlet(sparqlQuery, dsPath+HttpNames.ServiceQuery) ;
            context.addServlet(sparqlQuery, dsPath+HttpNames.ServiceQueryAlt) ;      // Alternative name
            context.addServlet(sparqlUpdate, dsPath+HttpNames.ServiceUpdate) ;
            //context.addServlet(new ServletHolder(new DumpServlet()),"/dump");
            
            // Finally, static content
            ServletHolder staticContent = new ServletHolder(new DefaultServlet()) ;
            // Content location : isolate so as not to expose the current directory
            staticContent.setInitParameter("resourceBase", "pages") ;

            context.addServlet(staticContent, "/") ;
        }
        
//            // Add the webapp.
//            webAppContextJoseki = new WebAppContext(server, "webapps/joseki", "/") ;
//            server.addHandler(webAppContextJoseki) ;
            
//            WebAppContext context = new WebAppContext();
//            context.setDescriptor(webapp+"/WEB-INF/web.xml");
//            context.setResourceBase("../test-jetty-webapp/src/main/webapp");
//            context.setContextPath("/");
//            context.setParentLoaderPriority(true);Exception ex)
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
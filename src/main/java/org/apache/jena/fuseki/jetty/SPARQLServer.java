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

package org.apache.jena.fuseki.jetty ;

import static java.lang.String.format ;
import static org.apache.jena.fuseki.Fuseki.serverLog ;

import java.io.FileInputStream ;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.mgt.MgtJMX ;
import org.eclipse.jetty.security.* ;
import org.eclipse.jetty.security.authentication.BasicAuthenticator ;
import org.eclipse.jetty.server.* ;
import org.eclipse.jetty.servlet.ServletContextHandler ;
import org.eclipse.jetty.util.security.Constraint ;
import org.eclipse.jetty.webapp.WebAppContext ;
import org.eclipse.jetty.xml.XmlConfiguration ;

import com.hp.hpl.jena.sparql.util.Utils ;

/** Standalone server, not run as a WAR file.
 * Used in testing and development.
 * 
 * SPARQLServer is the Jena server instance which wraps/utilizes 
 * {@link org.eclipse.jetty.server.Server}. This class provides
 * immediate access to the {@link org.eclipse.jetty.server.Server#start()} and 
 * {@link org.eclipse.jetty.server.Server#stop()} commands as well as obtaining
 * instances of the server and server configuration. Finally we can obtain 
 * instances of {@link org.apache.jena.fuseki.jetty.JettyServerConfig}.
 */
public class SPARQLServer {
    // Jetty specific.
    // This class is becoming less important - it now sets up a Jetty server for in-process use
    // either for the command line in development  
    // and in testing but not direct webapp deployments. 
    static { Fuseki.init() ; }

    public static SPARQLServer  instance    = null ;

    private ServerConnector serverConnector = null ;
    // If a separate ...
    private ServerConnector mgtConnector    = null ;
    
    private JettyServerConfig serverConfig ;

    // The jetty server.
    private Server              server         = null ;
    
    // webapp setup - standard maven layout
    public static final String contextpath    = "/" ;
    public static final String resourceBase   = "src/main/webapp" ;
    public static final String descriptorFile = resourceBase+"/WEB-INF/web.xml" ;

    /**
     * Default setup which requires a {@link org.apache.jena.fuseki.jetty.JettyServerConfig}
     * object as input.  We use this config to pass in the command line arguments for dataset, 
     * name etc. 
     * @param config
     */
    
    public static void initializeServer(JettyServerConfig config) {
        // Currently server-wide.
        Fuseki.verboseLogging = config.verboseLogging ;
        instance = new SPARQLServer(config) ;
    }
    
    /** Build a Jetty server using the development files for the webapp
     *  No command line configuration. 
     */
    public static Server create(int port) {
        Server server = new Server(port) ;
        WebAppContext webapp = createWebApp() ;
        server.setHandler(webapp) ;
        return server ;
    }

    private SPARQLServer(JettyServerConfig config) {
        this.serverConfig = config ;
        boolean webappBuild = true ;
        
        buildServerWebapp(serverConfig.jettyConfigFile, config.enableCompression) ;
        
        if ( mgtConnector == null )
            mgtConnector = serverConnector ;
    }

    /**
     * Initialize the {@link SPARQLServer} instance.
     */
    public void start() {
        
        String version = Fuseki.VERSION ;
        String buildDate = Fuseki.BUILD_DATE ;
        
        if ( version != null && version.equals("${project.version}") )
            version = null ;
        if ( buildDate != null && buildDate.equals("${build.time.xsd}") )
            buildDate = Utils.nowAsXSDDateTimeString() ;
        
        if ( version != null && buildDate != null )
            serverLog.info(format("%s %s %s", Fuseki.NAME, version, buildDate)) ;
        // This does not get set usefully for Jetty as we use it.
        // String jettyVersion = org.eclipse.jetty.server.Server.getVersion() ;
        // serverLog.info(format("Jetty %s",jettyVersion)) ;
        
        String host = serverConnector.getHost() ;
        if ( host != null )
            serverLog.info("Incoming connections limited to " + host) ;

        try {
            server.start() ;
        } catch (java.net.BindException ex) {
            serverLog.error("SPARQLServer (port="+serverConnector.getPort()+"): Failed to start server: " + ex.getMessage()) ;
            System.exit(1) ;
        } catch (Exception ex) {
            serverLog.error("SPARQLServer: Failed to start server: " + ex.getMessage(), ex) ;
            System.exit(1) ;
        }
        String now = Utils.nowAsString() ;
        serverLog.info(format("Started %s on port %d", now, serverConnector.getPort())) ;
    }

    /**
     * Sync with the {@link SPARQLServer} instance.
     * Returns only if the server exits cleanly 
     */
    public void join() {
        try {
            server.join() ;
        } catch (InterruptedException ex) { }
    }

        /**
     * Stop the {@link SPARQLServer} instance.
     */
    public void stop() {
        String now = Utils.nowAsString() ;
        serverLog.info(format("Stopped %s on port %d", now, serverConnector.getPort())) ;
        try {
            server.stop() ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("SPARQLServer: Exception while stopping server: " + ex.getMessage(), ex) ;
        }
        MgtJMX.removeJMX() ;
    }

    public static WebAppContext createWebApp() {
      WebAppContext webapp = new WebAppContext();
      webapp.getServletContext().getContextHandler().setMaxFormContentSize(10 * 1000 * 1000) ;
      webapp.setDescriptor(resourceBase+"/WEB-INF/web.xml");
      webapp.setResourceBase(resourceBase);
      webapp.setContextPath(contextpath);
      
      //-- Jetty setup for the ServletContext logger.
      // The name of the Jetty-allocated slf4j/log4j logger is
      // the display name or, if null, the context path name.   
      // It is set, without checking for a previous call of setLogger in "doStart"
      // which happens during server startup. 
      // This the name of the ServletContext logger as well
      webapp.setDisplayName(Fuseki.servletRequestLogName);  
      LogCtl.set(Fuseki.servletRequestLogName, "WARN"); 
      
      webapp.setParentLoaderPriority(true);  // Normal Java classloader behaviour.
      webapp.setErrorHandler(new FusekiErrorHandler()) ;
      return webapp ;
    }
    
    private void buildServerWebapp(String jettyConfig, boolean enableCompression) {
        if ( jettyConfig != null )
            // --jetty-config=jetty-fuseki.xml
            // for detailed configuration of the server using Jetty features.
            configServer(jettyConfig) ;
        else
            defaultServerConfig(serverConfig.port, serverConfig.loopback) ;
        WebAppContext webapp = createWebApp() ;
        server.setHandler(webapp) ;
        // Replaced by Shiro.
        if ( jettyConfig == null && serverConfig.authConfigFile != null )
            security(webapp, serverConfig.authConfigFile) ;
    }
    
    private static void security(ServletContextHandler context, String authfile) {
        Constraint constraint = new Constraint() ;
        constraint.setName(Constraint.__BASIC_AUTH) ;
        constraint.setRoles(new String[]{"fuseki"}) ;
        constraint.setAuthenticate(true) ;

        ConstraintMapping mapping = new ConstraintMapping() ;
        mapping.setConstraint(constraint) ;
        mapping.setPathSpec("/*") ;

        IdentityService identService = new DefaultIdentityService() ;

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler() ;
        securityHandler.addConstraintMapping(mapping) ;
        securityHandler.setIdentityService(identService) ;

        HashLoginService loginService = new HashLoginService("Fuseki Authentication", authfile) ;
        loginService.setIdentityService(identService) ;

        securityHandler.setLoginService(loginService) ;
        securityHandler.setAuthenticator(new BasicAuthenticator()) ;

        context.setSecurityHandler(securityHandler) ;

        serverLog.debug("Basic Auth Configuration = " + authfile) ;
    }

    private void configServer(String jettyConfig) {
        try {
            serverLog.info("Jetty server config file = " + jettyConfig) ;
            server = new Server() ;
            XmlConfiguration configuration = new XmlConfiguration(new FileInputStream(jettyConfig)) ;
            configuration.configure(server) ;
            serverConnector = (ServerConnector)server.getConnectors()[0] ;
        } catch (Exception ex) {
            serverLog.error("SPARQLServer: Failed to configure server: " + ex.getMessage(), ex) ;
            throw new FusekiException("Failed to configure a server using configuration file '" + jettyConfig + "'") ;
        }
    }

    private void defaultServerConfig(int port, boolean loopback) {
        server = new Server() ;
        ConnectionFactory f1 = new HttpConnectionFactory() ;
        ConnectionFactory f2 = new SslConnectionFactory() ;
        
        ServerConnector connector = new ServerConnector(server, f1) ; //, f2) ;
        connector.setPort(port);
        server.addConnector(connector);
        
        if ( loopback )
            connector.setHost("localhost");
        connector.setPort(port) ;
        serverConnector = connector ;
    }
}

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

package org.apache.jena.fuseki.cmd;

import static java.lang.String.format;
import static org.apache.jena.fuseki.Fuseki.serverLog;

import jakarta.servlet.ServletContext;

import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.FusekiCoreInfo;
import org.apache.jena.fuseki.webapp.FusekiEnv;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

/** Standalone full server, not run as a WAR file.
 * Used in testing and development.
 *
 * SPARQLServer is the Jena server instance which wraps/utilizes
 * {@link org.eclipse.jetty.server.Server}. This class provides
 * immediate access to the {@link org.eclipse.jetty.server.Server#start()} and
 * {@link org.eclipse.jetty.server.Server#stop()} commands as well as obtaining
 * instances of the server and server configuration. Finally we can obtain
 * instances of {@link org.apache.jena.fuseki.cmd.JettyServerConfig}.
 */
public class JettyFusekiWebapp {
    // Jetty specific.
    // This class is becoming less important - it now sets up a Jetty server for in-process use
    // either for the command line in development
    // and in testing but not direct webapp deployments.
    static { Fuseki.init(); }

    public static JettyFusekiWebapp  instance    = null;

    private ServerConnector serverConnector = null;
    // If a separate ...
    private ServerConnector mgtConnector    = null;

    private JettyServerConfig serverConfig;

    // The jetty server.

    private Server              server         = null;
    private ServletContext      servletContext = null;

    // webapp setup - standard maven layout
    public static       String contextpath     = "/";
    // Standalone jar
    public static final String resourceBase1   = "webapp";
    // Development
    public static final String resourceBase2   = "target/webapp";

    /**
     * Default setup which requires a {@link org.apache.jena.fuseki.cmd.JettyServerConfig}
     * object as input.  We use this config to pass in the command line arguments for dataset,
     * name etc.
     * @param config
     */

    public static void initializeServer(JettyServerConfig config) {
        instance = new JettyFusekiWebapp(config);
    }

    private JettyFusekiWebapp(JettyServerConfig config) {
        this.serverConfig = config;
        buildServerWebapp(serverConfig.contextPath, serverConfig.jettyConfigFile);
        if ( mgtConnector == null )
            mgtConnector = serverConnector;

        if ( config.enableCompression ) {
            GzipHandler gzipHandler = new GzipHandler();
            gzipHandler.setHandler(server.getHandler());
            server.setHandler(gzipHandler);
        }
    }

    /**
     * Initialize the {@link JettyFusekiWebapp} instance.
     */
    public void start() {

        FusekiCoreInfo.logCode(serverLog);
        // This does not get anything usefully for Jetty as we use it.
        // String jettyVersion = org.eclipse.jetty.server.Server.getVersion();
        // serverLog.info(format("Jetty %s",jettyVersion));

        String host = serverConnector.getHost();
        if ( host != null )
            serverLog.info("Incoming connections limited to " + host);

        try {
            server.start();
        } catch (java.net.BindException ex) {
            serverLog.error("SPARQLServer (port="+serverConnector.getPort()+"): Failed to start server: " + ex.getMessage());
            throw new FusekiException("BindException: port="+serverConnector.getPort()+": Failed to start server: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            serverLog.error("SPARQLServer: Failed to start server: " + ex.getMessage(), ex);
            throw new FusekiException("Failed to start server: " + ex.getMessage(), ex);
        }
        String now = DateTimeUtils.nowAsString();
        serverLog.info(format("Started %s on port %d", now, serverConnector.getPort()));
    }

    /**
     * Sync with the {@link JettyFusekiWebapp} instance.
     * Returns only if the server exits cleanly
     */
    public void join() {
        try {
            server.join();
        } catch (InterruptedException ex) { }
    }

        /**
     * Stop the {@link JettyFusekiWebapp} instance.
     */
    public void stop() {
        String now = DateTimeUtils.nowAsString();
        serverLog.info(format("Stopped %s on port %d", now, serverConnector.getPort()));
        try {
            server.stop();
        } catch (Exception ex) {
            Fuseki.serverLog.warn("SPARQLServer: Exception while stopping server: " + ex.getMessage(), ex);
        }
    }

    public static WebAppContext createWebApp(String contextPath) {
        FusekiEnv.setEnvironment();
        WebAppContext webapp = new WebAppContext();
        webapp.getServletContext().getContextHandler().setMaxFormContentSize(20 * 1000 * 1000);

        // Hunt for the webapp for the standalone jar (or development system).
        // Note that Path FUSEKI_HOME is not initialized until the webapp starts
        // so it is not available here.

        String resourceBase3 = null;
        String resourceBase4 = null;
        if ( FusekiEnv.FUSEKI_HOME != null ) {
            String HOME = FusekiEnv.FUSEKI_HOME.toString();
            resourceBase3 = HOME+"/"+resourceBase1;
            resourceBase4 = HOME+"/"+resourceBase2;
        }

        String resourceBase = tryResourceBase(resourceBase1, null);
        resourceBase = tryResourceBase(resourceBase2, resourceBase);
        resourceBase = tryResourceBase(resourceBase3, resourceBase);
        resourceBase = tryResourceBase(resourceBase4, resourceBase);

        if ( resourceBase == null ) {
            if ( resourceBase3 == null )
                Fuseki.serverLog.error("Can't find resourceBase (tried "+resourceBase1+" and "+resourceBase2+")");
            else
                Fuseki.serverLog.error("Can't find resourceBase (tried "+resourceBase1+", "+resourceBase2+", "+resourceBase3+" and "+resourceBase4+")");
            Fuseki.serverLog.error("Failed to start");
            throw new FusekiException("Failed to start");
        }

        webapp.setDescriptor(resourceBase+"/WEB-INF/web.xml");
        webapp.setResourceBase(resourceBase);
        webapp.setContextPath(contextPath);

        //-- Jetty setup for the ServletContext logger.
        // The name of the Jetty-allocated slf4j/log4j logger is
        // the display name or, if null, the context path name.
        // It is set, without checking for a previous call of setLogger in "doStart"
        // which happens during server startup.
        // This the name of the ServletContext logger as well
        webapp.setDisplayName(Fuseki.servletRequestLogName);
        webapp.setParentLoaderPriority(true);               // Normal Java classloader behaviour.
        webapp.setErrorHandler(new FusekiErrorHandler());   // If used.
        return webapp;
    }

    public static String getenv(String name) {
        String x = System.getenv(name);
        if ( x == null )
            x = System.getProperty(name);
        return x;
    }

    public DataAccessPointRegistry getDataAccessPointRegistry() {
        return DataAccessPointRegistry.get(servletContext);
    }

    private static String tryResourceBase(String maybeResourceBase, String currentResourceBase) {
        if ( currentResourceBase != null )
            return currentResourceBase;
        if ( maybeResourceBase != null && FileOps.exists(maybeResourceBase) )
            return maybeResourceBase;
        return currentResourceBase;
    }

    private void buildServerWebapp(String contextPath, String jettyConfig) {
        if ( jettyConfig != null )
            // --jetty-config=jetty-fuseki.xml
            // for detailed configuration of the server using Jetty features.
            configServer(jettyConfig);
        else
            defaultServerConfig(serverConfig.port, serverConfig.loopback);

        WebAppContext webapp = createWebApp(contextPath);
        if ( false /*enable symbolic links */ ) {
            // See http://www.eclipse.org/jetty/documentation/current/serving-aliased-files.html
            // Record what would be needed:
            // 1 - Allow all symbolic links without checking
            webapp.addAliasCheck(new AllowedResourceAliasChecker(webapp));
            // 2 - Check links are to valid resources. But default for Unix?
            webapp.addAliasCheck(new SymlinkAllowedResourceAliasChecker(webapp));
        }
        servletContext = webapp.getServletContext();
        server.setHandler(webapp);
        // Replaced by Shiro.
        if ( jettyConfig == null && serverConfig.authConfigFile != null )
            security(webapp, serverConfig.authConfigFile);
    }

    // This is now provided by Shiro.
    private static void security(ServletContextHandler context, String authfile) {
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"fuseki"});
        constraint.setAuthenticate(true);

        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setConstraint(constraint);
        mapping.setPathSpec("/*");

        IdentityService identService = new DefaultIdentityService();

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.addConstraintMapping(mapping);
        securityHandler.setIdentityService(identService);

        HashLoginService loginService = new HashLoginService("Fuseki Authentication", authfile);
        loginService.setIdentityService(identService);

        securityHandler.setLoginService(loginService);
        securityHandler.setAuthenticator(new BasicAuthenticator());

        context.setSecurityHandler(securityHandler);

        serverLog.debug("Basic Auth Configuration = " + authfile);
    }

    private void configServer(String jettyConfig) {
        try {
            serverLog.info("Jetty server config file = " + jettyConfig);
            server = new Server();
            Resource configXml = Resource.newResource(jettyConfig);
            XmlConfiguration configuration = new XmlConfiguration(configXml);
            configuration.configure(server);
            serverConnector = (ServerConnector)server.getConnectors()[0];
        } catch (Exception ex) {
            serverLog.error("SPARQLServer: Failed to configure server: " + ex.getMessage(), ex);
            throw new FusekiException("Failed to configure a server using configuration file '" + jettyConfig + "'");
        }
    }

    private void defaultServerConfig(int port, boolean loopback) {
        server = new Server();
        HttpConnectionFactory f1 = new HttpConnectionFactory();
        // Some people do try very large operations ... really, should use POST.
        f1.getHttpConfiguration().setRequestHeaderSize(512 * 1024);
        f1.getHttpConfiguration().setOutputBufferSize(5 * 1024 * 1024);
        // Do not add "Server: Jetty(....) when not a development system.
        if ( ! Fuseki.outputJettyServerHeader )
            f1.getHttpConfiguration().setSendServerVersion(false);

        // https is better done with a Jetty configuration file
        // because there are several things to configure.
        // See "examples/fuseki-jetty-https.xml"

        ServerConnector connector = new ServerConnector(server, f1);
        connector.setPort(port);
        server.addConnector(connector);
        if ( loopback )
            connector.setHost("localhost");
        serverConnector = connector;
    }
}

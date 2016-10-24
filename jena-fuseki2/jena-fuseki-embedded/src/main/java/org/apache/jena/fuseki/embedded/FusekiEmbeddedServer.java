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

import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import javax.servlet.ServletContext ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.build.FusekiBuilder ;
import org.apache.jena.fuseki.build.FusekiConfig ;
import org.apache.jena.fuseki.jetty.FusekiErrorHandler1 ;
import org.apache.jena.fuseki.mgt.ActionStats ;
import org.apache.jena.fuseki.server.DataAccessPoint ;
import org.apache.jena.fuseki.server.DataAccessPointRegistry ;
import org.apache.jena.fuseki.server.DataService ;
import org.apache.jena.fuseki.servlets.FusekiFilter ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.eclipse.jetty.server.HttpConnectionFactory ;
import org.eclipse.jetty.server.Server ;
import org.eclipse.jetty.server.ServerConnector ;
import org.eclipse.jetty.servlet.FilterHolder ;
import org.eclipse.jetty.servlet.ServletContextHandler ;

/**
 * Embedded Fuseki server. This is a Fuseki server running with a precofigured set of
 * datasets and services. 
 * There is no admin UI.
 * <p>
 * To create a embedded sever, use {@link FusekiEmbeddedServer} ({@link #make} is a
 * packaging of a call to {@link FusekiEmbeddedServer} for the case of one dataset,
 * responding to localhost only).
 * <p>
 * The application should call {@link #start()} to actually start the server
 * (it wil run in the background : see {@link #join}).
 * <p>Example:
 * <pre>
 *      DatasetGraph dsg = ... ;
 *      FusekiEmbeddedServer server = FusekiEmbeddedServer.create()
 *          .setPort(1234)
 *          .add("/ds", dsg)
 *          .build() ;
 *       server.start() ;
 * </pre>
 * Compact form (use the builder pattern above to get more flexibility):
 * <pre>
 *    FusekiEmbeddedServer.make(1234, "/ds", dsg).start() ;
 * </pre>
 * 
 */
public class FusekiEmbeddedServer {
    static { 
        //FusekiEnv.mode = FusekiEnv.INIT.EMBEDDED ;
        // Stop anything accidently resetting Fuseki server logging. 
        //FusekiLogging.allowLoggingReset(false) ;
    }
    
    /** Construct a Fuseki server for one dataset.
     * It only responds to localhost. 
     * The returned server has not been started  */ 
    static public FusekiEmbeddedServer make(int port, String name, DatasetGraph dsg) {
        return create()
            .setPort(port)
            .setLoopback(true)
            .add(name, dsg)
            .build() ;
    }
    
    public static Builder create() {
        return new Builder() ;
    }
    
    public final Server server ;
    private int port ;
    
    private FusekiEmbeddedServer(int port, Server server) {
        this.server = server ;
        // This should be the same.
        //this.port = ((ServerConnector)server.getConnectors()[0]).getPort() ;
        this.port = port ;
    }
    
    /** 
     * Return the port begin used.  
     * This will be the give port, which defauls to 3330, or
     * the one actually allocated if the port was 0 ("choose a free port").
     */
    public int getPort() {
        return port ; 
    }

    /** Get the underlying Jetty server which has also been set up. */ 
    public Server getJettyServer() {
        return server ; 
    }
    
    /** Get the {@link ServletContext}.
     * Adding new servlets is possible with care.
     */ 
    public ServletContext getServletContext() {
        return ((ServletContextHandler)server.getHandler()).getServletContext() ;
    }

    /** Get the {@link DataAccessPointRegistry}.
     * This method is intended for inspecting the registry.
     */ 
    public DataAccessPointRegistry getDataAccessPointRegistry() {
        return DataAccessPointRegistry.get(getServletContext()) ;
    }

    /** Start the server - the server continues to run after this call returns.
     *  To synchronise with the server stopping, call {@link #join}.  
     */
    public FusekiEmbeddedServer start() { 
        try { server.start(); }
        catch (Exception e) { throw new FusekiException(e) ; }
        if ( port == 0 )
            port = ((ServerConnector)server.getConnectors()[0]).getLocalPort() ;
        Fuseki.serverLog.info("Start Fuseki (port="+port+")");
        return this ;
    }

    /** Stop the server. */
    public void stop() { 
        Fuseki.serverLog.info("Stop Fuseki (port="+port+")");
        try { server.stop(); }
        catch (Exception e) { throw new FusekiException(e) ; }
    }
    
    /** Wait for the server to exit. This call is blocking. */
    public void join() {
        try { server.join(); }
        catch (Exception e) { throw new FusekiException(e) ; }
    }
    
    /** FusekiEmbeddedServer.Builder */
    public static class Builder {
        private Map<String, DataService> map           = new HashMap<>() ;
        // Default values.
        private int                      port          = 3330 ;
        private boolean                  loopback      = false ;
        private boolean                  withStats     = false ;
        private String                   contextPath   = "/" ;

        /** Set the port to run on. */ 
        public Builder setPort(int port) {
            if ( port < 0 )
                throw new IllegalArgumentException("Illegal port="+port+" : Port must be greater than or equal to zero.") ;
            this.port = port ;
            return this ;
        }
        
        /** Context path to Fuseki.  If it's "/" then Fuseki URL look like
         * "http://host:port/dataset/query" else "http://host:port/path/dataset/query" 
         */
        public Builder setContextPath(String path) {
            this.contextPath = path ;
            return this ;
        }
        
        /** Restrict the server to only respoding to the localhost interface. */ 
        public Builder setLoopback(boolean loopback) {
            this.loopback = loopback;
            return this ;
        }

        /** Add the "/$/stats" servlet that responds with stats about the server,
         * including counts of all calls made.
         */ 
        public Builder enableStats(boolean withStats) {
            this.withStats = withStats;
            return this ;
        }

        /** Add the dataset with given name and a default set of services including update */  
        public Builder add(String name, Dataset ds) {
            return add(name, ds.asDatasetGraph()) ;
        }

        /** Add the dataset with given name and a default set of services including update */  
        public Builder add(String name, DatasetGraph dsg) {
            return add(name, dsg, true) ;
        }

        /** Add the dataset with given name and a default set of services. */  
        public Builder add(String name, Dataset ds, boolean allowUpdate) {
            return add(name, ds.asDatasetGraph(), allowUpdate) ;
        }
            
        
        /** Add the dataset with given name and a default set of services. */  
        public Builder add(String name, DatasetGraph dsg, boolean allowUpdate) {
            DataService dSrv = FusekiBuilder.buildDataService(dsg, allowUpdate) ; 
            return add(name, dSrv) ;
        }
        
        /** Add a data service that includes dataset and service names.
         * A {@link DataService} allows for choices of the various endpoint names.
         */  
        public Builder add(String name, DataService dataService) {
            return add$(name, dataService) ; 
        }
        
        private Builder add$(String name, DataService dataService) {
            name = DataAccessPoint.canonical(name) ;
            if ( map.containsKey(name) )
                throw new FusekiConfigException("Data service name already registered: "+name) ;
            map.put(name, dataService) ;
            return this ;
        }

        /** Read and parse a Fuseki services/datasets file.
         *  <p>
         *  The application is responsible for ensuring a correct classpath. For example,
         *  including a dependency on {@code jena-text} if the configuration file
         *  includes a text index.     
         */
        public Builder parseConfigFile(String filename) {
            List<DataAccessPoint> x = FusekiConfig.readConfigurationFile(filename) ;
            // Unbundle so that they accumulate.
            x.forEach(dap-> add(dap.getName(), dap.getDataService())) ;
            return this ;
        }

        /** Build a server according to the current description */ 
        public FusekiEmbeddedServer build() {
            DataAccessPointRegistry registry = new DataAccessPointRegistry() ;
            map.forEach((name, dSrv) -> {
                DataAccessPoint dap = new DataAccessPoint(name, dSrv) ;
                registry.put(name, dap) ;
            }) ;
            ServletContextHandler handler = buildServletContext(contextPath, registry) ;
            if ( withStats )
                handler.addServlet(ActionStats.class, "/$/stats") ;
            DataAccessPointRegistry.set(handler.getServletContext(), registry) ;
            Server server = jettyServer(port, loopback) ;
            server.setHandler(handler);
            return new FusekiEmbeddedServer(port, server) ;
        }

        /** Build a ServletContextHandler with the Fuseki router : {@link FusekiFilter} */
        private static ServletContextHandler buildServletContext(String contextPath, DataAccessPointRegistry registry) {
            if ( contextPath == null || contextPath.isEmpty() )
                contextPath = "/" ;
            else if ( !contextPath.startsWith("/") )
                contextPath = "/" + contextPath ;
            ServletContextHandler context = new ServletContextHandler() ;
            FusekiFilter ff = new FusekiFilter() ;
            FilterHolder h = new FilterHolder(ff) ;
            context.setContextPath(contextPath) ;
            context.addFilter(h, "/*", null) ;
            context.setDisplayName(Fuseki.servletRequestLogName) ;
            context.setErrorHandler(new FusekiErrorHandler1()) ;
            return context ;
        }
        
        /** Jetty server */
        private static Server jettyServer(int port, boolean loopback) {
            Server server = new Server() ;
            HttpConnectionFactory f1 = new HttpConnectionFactory() ;
            // Some people do try very large operations ... really, should use POST.
            f1.getHttpConfiguration().setRequestHeaderSize(512 * 1024);
            f1.getHttpConfiguration().setOutputBufferSize(5 * 1024 * 1024) ;
            // Do not add "Server: Jetty(....) when not a development system.
            if ( ! Fuseki.outputJettyServerHeader )
                f1.getHttpConfiguration().setSendServerVersion(false) ;
            ServerConnector connector = new ServerConnector(server, f1) ;
            connector.setPort(port) ;
            server.addConnector(connector);
            if ( loopback )
                connector.setHost("localhost");
            return server ;
        }
    }
}
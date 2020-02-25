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

package org.apache.jena.fuseki.main;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.access.DataAccessCtl;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.build.FusekiConfig;
import org.apache.jena.fuseki.ctl.ActionPing;
import org.apache.jena.fuseki.ctl.ActionStats;
import org.apache.jena.fuseki.jetty.FusekiErrorHandler1;
import org.apache.jena.fuseki.jetty.JettyHttps;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.metrics.MetricsProviderRegistry;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.fuseki.servlets.*;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.util.NotUniqueException;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.sys.JenaSystem;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;

/**
 * Embedded Fuseki server. This is a Fuseki server running with a pre-configured set of
 * datasets and services. There is no admin UI and no security.
 * <p>
 * To create a embedded sever, use {@link FusekiServer} ({@link #make} is a
 * packaging of a call to {@link FusekiServer} for the case of one dataset,
 * responding to localhost only).
 * <p>
 * The application should call {@link #start()} to actually start the server
 * (it will run in the background : see {@link #join}).
 * <p>Example:
 * <pre>
 *      DatasetGraph dsg = ...;
 *      FusekiServer server = FusekiServer.create()
 *          .port(1234)
 *          .add("/ds", dsg)
 *          .build();
 *       server.start();
 * </pre>
 * Compact form (use the builder pattern above to get more flexibility):
 * <pre>
 *    FusekiServer.make(1234, "/ds", dsg).start();
 * </pre>
 */

public class FusekiServer {
    static { JenaSystem.init(); }

    /** Construct a Fuseki server for one dataset.
     * It only responds to localhost.
     * The returned server has not been started.
     */
    static public FusekiServer make(int port, String name, DatasetGraph dsg) {
        return create()
            .port(port)
            .loopback(true)
            .add(name, dsg)
            .build();
    }

    /** Return a builder, with the default choices of actions available. */
    public static Builder create() {
        return new Builder();
    }

    /**
     * Return a builder, with a custom set of operation-action mappings. An endpoint must
     * still be created for the server to be able to provide the action. An endpoint
     * dispatches to an operation, and an operation maps to an implementation. This is a
     * specialised operation - normal use is the operation {@link #create()}.
     */
    public static Builder create(OperationRegistry serviceDispatchRegistry) {
        return new Builder(serviceDispatchRegistry);
    }

    public final Server server;
    private final int httpPort;
    private final int httpsPort;
    private final ServletContext servletContext;
    private final boolean accessCtlRequest;
    private final boolean accessCtlData;

//    private FusekiServer(int httpPort, Server server) {
//        this(httpPort, -1, server,
//            ((ServletContextHandler)server.getHandler()).getServletContext()
//            );
//    }

    private FusekiServer(int httpPort, int httpsPort, Server server,
//                         boolean accessCtlRequest,
//                         boolean accessCtlData,
                         ServletContext fusekiServletContext) {
        this.server = server;
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
        this.servletContext = fusekiServletContext;
//        this.accessCtlRequest = accessCtlRequest;
//        this.accessCtlData = accessCtlData;
      this.accessCtlRequest = false;
      this.accessCtlData = false;
    }

    /**
     * Return the port being used.
     * This will be the give port, which defaults to 3330, or
     * the one actually allocated if the port was 0 ("choose a free port").
     * If https in use, this is the HTTPS port.
     */
    public int getPort() {
        return httpsPort > 0 ? httpsPort : httpPort;
    }

    /** Get the underlying Jetty server which has also been set up. */
    public Server getJettyServer() {
        return server;
    }

    /** Get the {@link ServletContext} used for Fuseki processing.
     * Adding new servlets is possible with care.
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /** Get the {@link DataAccessPointRegistry}.
     * This method is intended for inspecting the registry.
     */
    public DataAccessPointRegistry getDataAccessPointRegistry() {
        return DataAccessPointRegistry.get(getServletContext());
    }

    /** Get the {@link OperationRegistry}.
     * This method is intended for inspecting the registry.
     */
    public OperationRegistry getOperationRegistry() {
        return OperationRegistry.get(getServletContext());
    }

    /** Return whether this server has any access control enabled. */
    public boolean hasUserAccessControl() {
        return accessCtlRequest || accessCtlData;
    }

    /** Start the server - the server continues to run after this call returns.
     *  To synchronise with the server stopping, call {@link #join}.
     */
    public FusekiServer start() {
        try { server.start(); }

        catch (IOException ex) {
            if ( ex.getCause() instanceof java.security.UnrecoverableKeyException )
                // Unbundle for clearer message.
                throw new FusekiException(ex.getMessage());
            throw new FusekiException(ex);
        }
        catch (IllegalStateException ex) {
            throw new FusekiException(ex.getMessage());
        }
        catch (Exception ex) {
            throw new FusekiException(ex);
        }
        if ( httpsPort > 0 )
            Fuseki.serverLog.info("Start Fuseki (port="+httpPort+"/"+httpsPort+")");
        else
            Fuseki.serverLog.info("Start Fuseki (port="+getPort()+")");
        return this;
    }

    /** Stop the server. */
    public void stop() {
        Fuseki.serverLog.info("Stop Fuseki (port="+getPort()+")");
        try { server.stop(); }
        catch (Exception e) { throw new FusekiException(e); }
    }

    /** Wait for the server to exit. This call is blocking. */
    public void join() {
        try { server.join(); }
        catch (Exception e) { throw new FusekiException(e); }
    }

    /** FusekiServer.Builder */
    public static class Builder {
        private final DataAccessPointRegistry  dataAccessPoints =
            new DataAccessPointRegistry( MetricsProviderRegistry.get().getMeterRegistry() );
        private final OperationRegistry  operationRegistry;
        // Default values.
        private int                      serverPort         = 3330;
        private int                      serverHttpsPort    = -1;
        private boolean                  networkLoopback    = false;
        private boolean                  verbose            = false;
        private boolean                  withStats          = false;
        private boolean                  withPing           = false;
        private Map<String, String>      corsInitParams     = null;

        // Server wide authorization policy.
        // Endpoints, datasets and graphs within datasets may have addition policies.
        private AuthPolicy               serverAuth         = null;

        // HTTP authentication
        private String                   passwordFile       = null;
        private String                   realm              = null;
        private AuthScheme               authScheme         = null;

        // HTTPS
        private String                   httpsKeystore          = null;
        private String                   httpsKeystorePasswd    = null;

        // Other servlets to add.
        private List<Pair<String, HttpServlet>> servlets    = new ArrayList<>();
        private List<Pair<String, Filter>> filters          = new ArrayList<>();

        private String                   contextPath        = "/";
        private String                   staticContentDir   = null;
        private SecurityHandler          securityHandler    = null;
        private Map<String, Object>      servletAttr        = new HashMap<>();

        // The default CORS settings.
        private static final Map<String, String> corsInitParamsDft = new LinkedHashMap<>();
        static {
            // This is the CrossOriginFilter default.
            corsInitParamsDft.put(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
            // Variatiosn from CrossOriginFilter defaults.
            corsInitParamsDft.put(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,DELETE,PUT,HEAD,OPTIONS,PATCH");
            corsInitParamsDft.put(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
                "X-Requested-With, Content-Type, Accept, Origin, Last-Modified, Authorization");
            // The 7 CORS default exposed headers.
            corsInitParamsDft.put(CrossOriginFilter.EXPOSED_HEADERS_PARAM,
                "Cache-Control, Content-Language, Content-Length, Content-Type, Expires, Last-Modified, Pragma");
        }

        // Builder with standard operation-action mapping.
        Builder() {
            this.operationRegistry = OperationRegistry.createStd();
        }

        // Builder with provided operation-action mapping.
        Builder(OperationRegistry  operationRegistry) {
            // Isolate.
            this.operationRegistry = OperationRegistry.createEmpty();
            OperationRegistry.copyConfig(operationRegistry, this.operationRegistry);
        }

        /** Set the port to run on.
         * @deprecated Use {@link #port}.
         */
        @Deprecated
        public Builder setPort(int port) {
            return port(port);
        }

        /** Set the port to run on. */
        public Builder port(int port) {
            if ( port < 0 )
                throw new IllegalArgumentException("Illegal port="+port+" : Port must be greater than or equal to zero.");
            this.serverPort = port;
            return this;
        }

        /** Context path to Fuseki.  If it's "/" then Fuseki URL look like
         * "http://host:port/dataset/query" else "http://host:port/path/dataset/query"
         * @deprecated Use {@link #contextPath}.
         */
        @Deprecated
        public Builder setContextPath(String path) {
            return contextPath(path);
        }

        /** Context path to Fuseki.  If it's "/" then Fuseki URL look like
         * "http://host:port/dataset/query" else "http://host:port/path/dataset/query"
         */
        public Builder contextPath(String path) {
            requireNonNull(path, "path");
            this.contextPath = path;
            return this;
        }

        /** Restrict the server to only responding to the localhost interface.
         *  @deprecated Use {@link #networkLoopback}.
         */
        @Deprecated
        public Builder setLoopback(boolean loopback) {
            return loopback(loopback);
        }

        /** Restrict the server to only responding to the localhost interface. */
        public Builder loopback(boolean loopback) {
            this.networkLoopback = loopback;
            return this;
        }

        /** Set the location (filing system directory) to serve static file from.
         *  @deprecated Use {@link #staticFileBase}.
         */
        @Deprecated
        public Builder setStaticFileBase(String directory) {
            return staticFileBase(directory);
        }

        /** Set the location (filing system directory) to serve static files from. */
        public Builder staticFileBase(String directory) {
            requireNonNull(directory, "directory");
            this.staticContentDir = directory;
            return this;
        }

        /** Set a Jetty SecurityHandler.
         * <p>
         *  By default, the server runs with no security.
         *  @deprecated Use {@link #staticFileBase}.
         */
        @Deprecated
        public Builder setSecurityHandler(SecurityHandler securityHandler) {
            return securityHandler(securityHandler);
        }

        /** Set a Jetty SecurityHandler.
         * <p>
         *  By default, the server runs with no security.
         *  This is more for using the basic server for testing.
         *  The full Fuseki server provides security with Apache Shiro
         *  and a defensive reverse proxy (e.g. Apache httpd) in front of the Jetty server
         *  can also be used, which provides a wide variety of proven security options.
         */
        public Builder securityHandler(SecurityHandler securityHandler) {
            requireNonNull(securityHandler, "securityHandler");
            this.securityHandler = securityHandler;
            return this;
        }

        /** Set verbose logging
         *  @deprecated Use {@link #verbose(boolean)}.
         */
        @Deprecated
        public Builder setVerbose(boolean verbose) {
            return verbose(verbose);
        }

        /** Set verbose logging */
        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        /** Add the Cross Origin (CORS) filter.
         * {@link CrossOriginFilter}.
         */
        public Builder enableCors(boolean withCORS) {
            corsInitParams = withCORS ? corsInitParamsDft : null ;
            return this;
        }

        /** Add the "/$/stats" servlet that responds with stats about the server,
         * including counts of all calls made.
         */
        public Builder enableStats(boolean withStats) {
            this.withStats = withStats;
            return this;
        }

        /** Add the "/$/ping" servlet that responds to HTTP very efficiently.
         * This is useful for testing whether a server is alive, for example, from a load balancer.
         */
        public Builder enablePing(boolean withPing) {
            this.withPing = withPing;
            return this;
        }

        /**
         * Add the dataset with given name and a default set of services including update.
         * This is equivalent to {@code add(name, dataset, true)}.
         */
        public Builder add(String name, Dataset dataset) {
            requireNonNull(name, "name");
            requireNonNull(dataset, "dataset");
            return add(name, dataset.asDatasetGraph());
        }

        /**
         * Add the {@link DatasetGraph} with given name and a default set of services including update.
         * This is equivalent to {@code add(name, dataset, true)}.
         */
        /** Add the dataset with given name and a default set of services including update */
        public Builder add(String name, DatasetGraph dataset) {
            requireNonNull(name, "name");
            requireNonNull(dataset, "dataset");
            return add(name, dataset, true);
        }

        /**
         * Add the dataset with given name and a default set of services and enabling
         * update if allowUpdate=true.
         */
        public Builder add(String name, Dataset dataset, boolean allowUpdate) {
            requireNonNull(name, "name");
            requireNonNull(dataset, "dataset");
            return add(name, dataset.asDatasetGraph(), allowUpdate);
        }

        /**
         * Add the dataset with given name and a default set of services and enabling
         * update if allowUpdate=true.
         */
        public Builder add(String name, DatasetGraph dataset, boolean allowUpdate) {
            requireNonNull(name, "name");
            requireNonNull(dataset, "dataset");
            DataService dSrv = FusekiConfig.buildDataServiceStd(dataset, allowUpdate);
            return add(name, dSrv);
        }

        /** Add a data service that includes dataset and service names.
         * A {@link DataService} allows for choices of the various endpoint names.
         */
        public Builder add(String name, DataService dataService) {
            requireNonNull(name, "name");
            requireNonNull(dataService, "dataService");
            return add$(name, dataService);
        }

        private Builder add$(String name, DataService dataService) {
            name = DataAccessPoint.canonical(name);
            if ( dataAccessPoints.isRegistered(name) )
                throw new FusekiConfigException("Data service name already registered: "+name);
            DataAccessPoint dap = new DataAccessPoint(name, dataService);
            addDataAccessPoint(dap);
            return this;
        }

        /** Add a {@link DataAccessPoint}. */
        private Builder addDataAccessPoint(DataAccessPoint dap) {
            if ( dataAccessPoints.isRegistered(dap.getName()) )
                throw new FusekiConfigException("Data service name already registered: "+dap.getName());
            dataAccessPoints.register(dap);
            return this;
        }

        /**
         * Configure using a Fuseki services/datasets assembler file.
         * <p>
         * The application is responsible for ensuring a correct classpath. For example,
         * including a dependency on {@code jena-text} if the configuration file includes
         * a text index.
         */
        public Builder parseConfigFile(String filename) {
            requireNonNull(filename, "filename");
            Model model = AssemblerUtils.readAssemblerFile(filename);
            parseConfig(model);
            return this;

        }

        /**
         * Configure using a Fuseki services/datasets assembler model.
         * <p>
         * The application is responsible for ensuring a correct classpath. For example,
         * including a dependency on {@code jena-text} if the configuration file includes
         * a text index.
         */
        public Builder parseConfig(Model model) {
            requireNonNull(model, "model");
            Resource server = FusekiConfig.findServer(model);
            processServerLevel(server);

            // Process server and services, whether via server ja:services or, if absent, by finding by type.
            // Side effect - sets global context.
            List<DataAccessPoint> x = FusekiConfig.processServerConfiguration(model, Fuseki.getContext());
            x.forEach(dap->addDataAccessPoint(dap));
            return this;
        }

        /**
         * Server level setting specific to Fuseki main.
         * General settings done by {@link FusekiConfig#processServerConfiguration}.
         */
        private void processServerLevel(Resource server) {
            if ( server == null )
                return;

            withPing  = argBoolean(server, FusekiVocab.pServerPing,  false);
            withStats = argBoolean(server, FusekiVocab.pServerStats, false);

            // Extract settings - the server building is done in buildSecurityHandler,
            // buildAccessControl.  Dataset and graph level happen in assemblers.
            String passwdFile = GraphUtils.getAsStringValue(server, FusekiVocab.pPasswordFile);
            if ( passwdFile != null )
                passwordFile(passwdFile);
            String realmStr = GraphUtils.getAsStringValue(server, FusekiVocab.pRealm);
            if ( realmStr != null )
                realm(realmStr);

            String authStr = GraphUtils.getAsStringValue(server, FusekiVocab.pAuth);
            if ( authStr != null )
                auth(AuthScheme.scheme(authStr));
            serverAuth = FusekiConfig.allowedUsers(server);
        }

        private static boolean argBoolean(Resource r, Property p, boolean dftValue) {
            try { GraphUtils.atmostOneProperty(r, p); }
            catch (NotUniqueException ex) {
                throw new FusekiConfigException(ex.getMessage());
            }
            Statement stmt = r.getProperty(p);
            if ( stmt == null )
                return dftValue;
            try {
                return stmt.getBoolean();
            } catch (JenaException ex) {
                throw new FusekiConfigException("Not a boolean for '"+p+"' : "+stmt.getObject());
            }
        }

        /** Process password file, auth and realm settings on the server description. **/
        private void processAuthentication(Resource server) {
            String passwdFile = GraphUtils.getAsStringValue(server, FusekiVocab.pPasswordFile);
            if ( passwdFile != null )
                passwordFile(passwdFile);
            String realmStr = GraphUtils.getAsStringValue(server, FusekiVocab.pRealm);
            if ( realmStr != null )
                realm(realmStr);
        }

        /**
         * Choose the HTTP authentication scheme.
         */
        public Builder auth(AuthScheme authScheme) {
            this.authScheme = authScheme;
            return this;
        }

        /**
         * Set the server-wide server authorization {@link AuthPolicy}.
         * Defaults to "logged in users" if a password file provided but no other policy.
         * To allow any one to access the server, use {@link Auth#ANY_ANON}.
         */
        public Builder serverAuthPolicy(AuthPolicy authPolicy) {
            this.serverAuth = authPolicy;
            return this;
        }

        /**
         * Set the realm used for HTTP digest authentication.
         */
        public Builder realm(String realm) {
            this.realm = realm;
            return this;
        }

        /**
         * Set the password file. This will be used to build a {@link #securityHandler
         * security handler} if one is not supplied. Setting null clears any previous entry.
         * The file should be in the format of
         * <a href="https://www.eclipse.org/jetty/documentation/current/configuring-security.html#hash-login-service">Eclipse jetty password file</a>.
         */
        public Builder passwordFile(String passwordFile) {
            this.passwordFile = passwordFile;
            return this;
        }

        public Builder https(int httpsPort, String certStore, String certStorePasswd) {
            requireNonNull(certStore, "certStore");
            requireNonNull(certStorePasswd, "certStorePasswd");
            this.httpsKeystore = certStore;
            this.httpsKeystorePasswd = certStorePasswd;
            this.serverHttpsPort = httpsPort;
            return this;
        }

        /**
         * Add an {@link ActionProcessor} as a servlet. {@link ActionProcessor} are
         * the implementation of servlet handling that operate within the Fuseki
         * logging and execution framework.
         */
        public Builder addProcessor(String pathSpec, ActionProcessor processor) {
            return addProcessor(pathSpec, processor, Fuseki.actionLog);
        }

        /**
         * Add an {@link ActionProcessor} as a servlet. {@link ActionProcessor} are
         * the implementation of servlet handling that operate within the Fuseki
         * logging and execution framework.
         */
        public Builder addProcessor(String pathSpec, ActionProcessor processor, Logger log) {
            requireNonNull(pathSpec, "pathSpec");
            requireNonNull(processor, "processor");

            HttpServlet servlet;
            if ( processor instanceof HttpServlet )
                servlet = (HttpServlet)processor;
            else
                servlet = new ServletAction(processor, log);
            servlets.add(Pair.create(pathSpec, servlet));
            return this;
        }

        /**
         * Add the given servlet with the {@code pathSpec}. These servlets are added so
         * that they are checked after the Fuseki filter for datasets and before the
         * static content handler (which is the last servlet) used for
         * {@link #setStaticFileBase(String)}.
         */
        public Builder addServlet(String pathSpec, HttpServlet servlet) {
            requireNonNull(pathSpec, "pathSpec");
            requireNonNull(servlet, "servlet");
            servlets.add(Pair.create(pathSpec, servlet));
            return this;
        }

        /**
         * Add a servlet attribute. Pass a value of null to remove any existing binding.
         */
        public Builder addServletAttribute(String attrName, Object value) {
            requireNonNull(attrName, "attrName");
            if ( value != null )
                servletAttr.put(attrName, value);
            else
                servletAttr.remove(attrName);
            return this;
        }

        /**
         * Add a filter with the pathSpec. Note that Fuseki dispatch uses a servlet filter
         * which is the last in the filter chain.
         */
        public Builder addFilter(String pathSpec, Filter filter) {
            requireNonNull(pathSpec, "pathSpec");
            requireNonNull(filter, "filter");
            filters.add(Pair.create(pathSpec, filter));
            return this;
        }

        /**
         * Add an operation and handler to the server. This does not enable it for any dataset.
         * <p>
         * To associate an operation with a dataset, call {@link #addEndpoint} after adding the dataset.
         *
         * @see #addEndpoint
         */
        public Builder registerOperation(Operation operation, ActionService handler) {
            registerOperation(operation, null, handler);
            return this;
        }

        /**
         * Add an operation to the server, together with its triggering Content-Type (may be null) and servlet handler.
         * <p>
         * To associate an operation with a dataset, call {@link #addEndpoint} after adding the dataset.
         *
         * @see #addEndpoint
         */
        public Builder registerOperation(Operation operation, String contentType, ActionService handler) {
            Objects.requireNonNull(operation, "operation");
            if ( handler == null )
                operationRegistry.unregister(operation);
            else
                operationRegistry.register(operation, contentType, handler);
            return this;
        }

        /**
         * Create an endpoint on the dataset.
         * The operation must already be registered with the builder.
         * @see #registerOperation(Operation, ActionService)
         */
        public Builder addEndpoint(String datasetName, String endpointName, Operation operation) {
            return addEndpoint(datasetName, endpointName, operation, null);
        }

        /**
         * Create an endpoint as a service of the dataset (i.e. {@code /dataset/endpointName}).
         * The operation must already be registered with the builder.
         * @see #registerOperation(Operation, ActionService)
         */
        public Builder addEndpoint(String datasetName, String endpointName, Operation operation, AuthPolicy authPolicy) {
            Objects.requireNonNull(datasetName, "datasetName");
            Objects.requireNonNull(endpointName, "endpointName");
            Objects.requireNonNull(operation, "operation");
            serviceEndpointOperation(datasetName, endpointName, operation, authPolicy);
            return this;
        }

        /**
         * Create an endpoint on the dataset i.e. {@code /dataset/} for an operation that has other query parameters
         * or a Content-Type that distinguishes it.
         * The operation must already be registered with the builder.
         * @see #registerOperation(Operation, ActionService)
         */
        public Builder addOperation(String datasetName, Operation operation) {
            addOperation(datasetName, operation, null);
            return this;
        }

        /**
         * Create an endpoint on the dataset i.e. {@code /dataset/} for an operation that has other query parameters
         * or a Content-Type that distinguishes it.  Use {@link #addEndpoint(String, String, Operation)} when
         * the functionality is invoked by presence of a name in the URL after the dataset name.
         *
         * The operation must already be registered with the builder.
         * @see #registerOperation(Operation, ActionService)
         */
        public Builder addOperation(String datasetName, Operation operation, AuthPolicy authPolicy) {
            Objects.requireNonNull(datasetName, "datasetName");
            Objects.requireNonNull(operation, "operation");
            serviceEndpointOperation(datasetName, null, operation, authPolicy);
            return this;
        }

        private void serviceEndpointOperation(String datasetName, String endpointName, Operation operation, AuthPolicy authPolicy) {
            String name = DataAccessPoint.canonical(datasetName);

            if ( ! operationRegistry.isRegistered(operation) )
                throw new FusekiConfigException("Operation not registered: "+operation.getName());

            if ( ! dataAccessPoints.isRegistered(name) )
                throw new FusekiConfigException("Dataset not registered: "+datasetName);
            DataAccessPoint dap = dataAccessPoints.get(name);
            Endpoint endpoint = Endpoint.create()
                .operation(operation)
                .endpointName(endpointName)
                .authPolicy(authPolicy)
                .build();
            dap.getDataService().addEndpoint(endpoint);
        }

        /**
         * Build a server according to the current description.
         */
        public FusekiServer build() {
            buildStart();
            try {
                validate();
                if ( securityHandler == null && passwordFile != null )
                    securityHandler = buildSecurityHandler();
                ServletContextHandler handler = buildFusekiContext();

                Server server;
                if ( serverHttpsPort == -1 ) {
                    // HTTP
                    server = jettyServer(handler, serverPort);
                } else {
                    // HTTPS, no http redirection.
                    server = jettyServerHttps(handler, serverPort, serverHttpsPort, httpsKeystore, httpsKeystorePasswd);
                }
                if ( networkLoopback )
                    applyLocalhost(server);
                return new FusekiServer(serverPort, serverHttpsPort, server, handler.getServletContext());
            } finally {
                buildFinish();
            }
        }

        private ConstraintSecurityHandler buildSecurityHandler() {
            if ( passwordFile == null )
                return null;
            UserStore userStore = JettyLib.makeUserStore(passwordFile);
            return JettyLib.makeSecurityHandler(realm, userStore, authScheme);
        }

        // These booleans are only for validation.
        // They do not affect the build() step.

        // Triggers some checking.
        private boolean hasAuthenticationHandler = false;

        // Whether there is any per-graph access control.
        private boolean hasDataAccessControl     = false;

        // Do we need to authenticate the user?
        // Triggers some checking.
        private boolean authenticateUser         = false;

        private List<DatasetGraph> datasets = null;

        private void buildStart() {
            // -- Server and dataset authentication
            hasAuthenticationHandler = (passwordFile != null) || (securityHandler != null);

            if ( realm == null )
                realm = Auth.dftRealm;

            // See if there are any DatasetGraphAccessControl.
            hasDataAccessControl =
                dataAccessPoints.keys().stream()
                    .map(name-> dataAccessPoints.get(name).getDataService().getDataset())
                    .anyMatch(DataAccessCtl::isAccessControlled);

            // Server level.
            authenticateUser = ( serverAuth != null );
            // Dataset level.
            if ( ! authenticateUser ) {
                // Any datasets with allowedUsers?
                authenticateUser = dataAccessPoints.keys().stream()
                        .map(name-> dataAccessPoints.get(name).getDataService())
                        .anyMatch(dSvc->dSvc.authPolicy() != null);
            }
            // Endpoint level.
            if ( ! authenticateUser ) {
                authenticateUser = dataAccessPoints.keys().stream()
                    .map(name-> dataAccessPoints.get(name).getDataService())
                    .flatMap(dSrv->dSrv.getEndpoints().stream())
                    .anyMatch(ep->ep.getAuthPolicy()!=null);
            }

            // If only a password file given, and nothing else, set the server to allowedUsers="*" (must log in).
            if ( passwordFile != null && ! authenticateUser ) {
                if ( serverAuth == null ) {
                    // Set server auth to "any logged in user" if it hasn't been set otherwise.
                    serverAuth = Auth.ANY_USER;
                    authenticateUser = true;
                }
            }
        }

        private static boolean authAny(AuthPolicy policy) {
            // Test for any AuthPolicy that accepts "no user".
            return policy == null || policy == Auth.ANY_ANON || policy.isAllowed(null);
        }

        /** Test whether some server authorization is needed. */
        private boolean hasServerWideAuth() {
            return ! authAny(serverAuth);
        }

        private void buildFinish() {
            hasAuthenticationHandler = false;
            hasDataAccessControl = false;
            datasets = null;
        }

        /** Do some checking to make sure setup is consistent. */
        private void validate() {
            if ( ! hasAuthenticationHandler ) {
                if ( authenticateUser )
                    Fuseki.configLog.warn("Authetication of users required (e.g. 'allowedUsers' is set) but there is no authentication setup (e.g. password file)");
                if ( hasDataAccessControl )
                    Fuseki.configLog.warn("Data-level access control in the configuration but there is no authentication setup (e.g. password file)");
            }
        }

        /** Build one configured Fuseki processor (ServletContext), same dispatch ContextPath */
        private ServletContextHandler buildFusekiContext() {
            ServletContextHandler handler = buildServletContext(contextPath);
            ServletContext cxt = handler.getServletContext();
            Fuseki.setVerbose(cxt, verbose);
            servletAttr.forEach((n,v)->cxt.setAttribute(n, v));

            // Clone to isolate from any future changes (reusing the builder).
            DataAccessPointRegistry dapRegistry = new DataAccessPointRegistry(dataAccessPoints);
            OperationRegistry operationReg = new OperationRegistry(operationRegistry);
            OperationRegistry.set(cxt, operationReg);
            DataAccessPointRegistry.set(cxt, dapRegistry);
            JettyLib.setMimeTypes(handler);
            servletsAndFilters(handler);
            buildAccessControl(handler);

            dapRegistry.forEach((name, dap) -> {
                // Custom processors (endpoint specific,fuseki:implementation) will have already
                // been set; all others need setting from the OperationRegistry in scope.
                dap.getDataService().setEndpointProcessors(operationReg);
                dap.getDataService().forEachEndpoint(ep->{
                    // Override for graph-level access control.
                    if ( DataAccessCtl.isAccessControlled(dap.getDataService().getDataset()) )
                        FusekiLib.modifyForAccessCtl(ep, DataAccessCtl.requestUserServlet);
                });
            });

            // Start services.
            dapRegistry.forEach((name, dap)->dap.getDataService().goActive());
            return handler;
        }

        private void buildAccessControl(ServletContextHandler cxt) {
            // -- Access control
            if ( securityHandler != null ) {
                cxt.setSecurityHandler(securityHandler);
                if ( securityHandler instanceof ConstraintSecurityHandler ) {
                    ConstraintSecurityHandler csh = (ConstraintSecurityHandler)securityHandler;
                    if ( hasServerWideAuth() ) {
                        JettyLib.addPathConstraint(csh, "/*");
                    }
                    else {
                        // Find datasets that need login.
                        // If any endpoint
                        DataAccessPointRegistry.get(cxt.getServletContext()).forEach((name, dap)-> {
                            DatasetGraph dsg = dap.getDataService().getDataset();
                            if ( ! authAny(dap.getDataService().authPolicy()) ) {
                                JettyLib.addPathConstraint(csh, DataAccessPoint.canonical(name));
                                JettyLib.addPathConstraint(csh, DataAccessPoint.canonical(name)+"/*");
                            }
                            else {
                                // Need to to a pass to find the "right" set to install.
                                dap.getDataService().forEachEndpoint(ep->{
                                    // repeats.
                                    if ( ! authAny(ep.getAuthPolicy()) ) {
                                        // Unnamed - applies to the dataset. Yuk.
                                        if ( ep.getName().isEmpty() ) {
                                            JettyLib.addPathConstraint(csh, DataAccessPoint.canonical(name));
                                            JettyLib.addPathConstraint(csh, DataAccessPoint.canonical(name)+"/*");

                                        } else {
                                            // Named.
                                            JettyLib.addPathConstraint(csh, DataAccessPoint.canonical(name)+"/"+ep.getName());
                                            if ( Fuseki.GSP_DIRECT_NAMING )
                                                JettyLib.addPathConstraint(csh, DataAccessPoint.canonical(name)+"/"+ep.getName()+"/*");
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            }
        }

        /** Build a ServletContextHandler */
        private ServletContextHandler buildServletContext(String contextPath) {
            if ( contextPath == null || contextPath.isEmpty() )
                contextPath = "/";
            else if ( !contextPath.startsWith("/") )
                contextPath = "/" + contextPath;
            ServletContextHandler context = new ServletContextHandler();
            context.setDisplayName(Fuseki.servletRequestLogName);
            context.setErrorHandler(new FusekiErrorHandler1());
            context.setContextPath(contextPath);
            // securityHandler done in buildAccessControl
            return context;
        }

        /** Add servlets and servlet filters, including the {@link FusekiFilter} */
        private void servletsAndFilters(ServletContextHandler context) {
            // First in chain. Authentication.
            if ( hasServerWideAuth() ) {
                Predicate<String> auth = serverAuth::isAllowed;
                AuthFilter authFilter = new AuthFilter(auth);
                addFilter(context, "/*", authFilter);
            }

            // CORS, maybe
            if ( corsInitParams != null ) {
                Filter corsFilter = new CrossOriginFilter();
                FilterHolder holder = new FilterHolder(corsFilter);
                holder.setInitParameters(corsInitParams);
                addFilterHolder(context, "/*", holder);
            }

            // End of chain. May dispatch and not pass on requests.
            // Looks for any URL that starts with a dataset name.
            FusekiFilter ff = new FusekiFilter();
            addFilter(context, "/*", ff);

            // and then any additional servlets and filters.
            if ( withStats )
                addServlet(context, "/$/stats/*", new ActionStats());
            if ( withPing )
                addServlet(context, "/$/ping", new ActionPing());

            servlets.forEach(p-> addServlet(context, p.getLeft(), p.getRight()));
            filters.forEach (p-> addFilter(context, p.getLeft(), p.getRight()));

            // Finally, drop to state content if configured.
            if ( staticContentDir != null ) {
                DefaultServlet staticServlet = new DefaultServlet();
                ServletHolder staticContent = new ServletHolder(staticServlet);
                staticContent.setInitParameter("resourceBase", staticContentDir);
                context.addServlet(staticContent, "/");
            }
        }

        private static void addServlet(ServletContextHandler context, String pathspec, HttpServlet httpServlet) {
            ServletHolder sh = new ServletHolder(httpServlet);
            context.addServlet(sh, pathspec);
        }

        private void addFilter(ServletContextHandler context, String pathspec, Filter filter) {
            FilterHolder holder = new FilterHolder(filter);
            addFilterHolder(context, pathspec, holder);
        }

        private void addFilterHolder(ServletContextHandler context, String pathspec, FilterHolder holder) {
            context.addFilter(holder, pathspec, null);
        }

        /** Jetty server with one connector/port. */
        private static Server jettyServer(ServletContextHandler handler, int port) {
            Server server = new Server();
            HttpConnectionFactory f1 = new HttpConnectionFactory();
            // Some people do try very large operations ... really, should use POST.
            f1.getHttpConfiguration().setRequestHeaderSize(512 * 1024);
            f1.getHttpConfiguration().setOutputBufferSize(1024 * 1024);
            // Do not add "Server: Jetty(....) when not a development system.
            if ( ! Fuseki.outputJettyServerHeader )
                f1.getHttpConfiguration().setSendServerVersion(false);
            ServerConnector connector = new ServerConnector(server, f1);
            connector.setPort(port);
            server.addConnector(connector);
            server.setHandler(handler);
            return server;
        }

        /** Jetty server with https. */
        private static Server jettyServerHttps(ServletContextHandler handler, int httpPort, int httpsPort, String keystore, String certPassword) {
            return JettyHttps.jettyServerHttps(handler, keystore, certPassword, httpPort, httpsPort);
        }

        /** Restrict connectors to localhost */
        private static void applyLocalhost(Server server) {
            Connector[] connectors = server.getConnectors();
            for ( int i = 0; i < connectors.length; i++ ) {
                if ( connectors[i] instanceof ServerConnector ) {
                    ((ServerConnector)connectors[i]).setHost("localhost");
                }
            }
        }
    }
}

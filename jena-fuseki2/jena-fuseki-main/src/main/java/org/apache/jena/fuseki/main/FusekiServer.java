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
import static org.apache.jena.fuseki.Fuseki.serverLog;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.Registry;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.access.DataAccessCtl;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.build.FusekiConfig;
import org.apache.jena.fuseki.ctl.*;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.sys.*;
import org.apache.jena.fuseki.metrics.MetricsProviderRegistry;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.fuseki.servlets.*;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.other.G;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NotUniqueException;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.web.HttpSC;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.slf4j.Logger;

/**
 * Fuseki server.
 * <p>
 * This is a Fuseki server running with a pre-configured set
 * of datasets and services.
 * <p>
 * To create a embedded sever, use {@link FusekiServer} ({@link #make} is a packaging
 * of a call to {@link FusekiServer} for the case of one dataset, responding to
 * localhost only).
 * <p>
 * The application calls {@link #start()} to run the server (it will run in the background : see {@link #join}).
 * <p>
 * Example:
 *
 * <pre>
 *      DatasetGraph dsg = ...;
 *      FusekiServer server = FusekiServer.create()
 *          .port(1234)
 *          .add("/ds", dsg)
 *          .build();
 *       server.start();
 * </pre>
 * <p>
 * Supplying a port number of 0, causes the server to allocate a free port and use
 * that. The actual port can be found with {@link #getPort()}.
 * <p>
 * The following {@link #make compact form} builds a server that only responds to localhost traffic:
 * <pre>
 *    FusekiServer.make(1234, "/ds", dsg).start();
 * </pre>
 * which may be useful for a test server.
 */

public class FusekiServer {
    static { JenaSystem.init(); }

    /**
     * Construct a Fuseki server from command line arguments.
     * The return server has not been started.
     */
    static public FusekiServer construct(String... args) {
        return FusekiMain.build(args);
    }

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

    private final Server server;
    private int httpPort;
    private int httpsPort;
    private final String staticContentDir;
    private final ServletContext servletContext;
    private final FusekiModules modules;

    private FusekiServer(int httpPort, int httpsPort, Server server,
                         String staticContentDir,
                         FusekiModules modules,
                         ServletContext fusekiServletContext) {
        this.server = Objects.requireNonNull(server);
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
        this.staticContentDir = staticContentDir;
        this.servletContext = Objects.requireNonNull(fusekiServletContext);
        this.modules = Objects.requireNonNull(modules);
    }

    /**
     * Return the port being used.
     * <p>
     * This will be the server port, which defaults to 3330 for embedded use,
     * and to 3030 for command line use,
     * or one actually allocated if the port was 0 ("choose a free port").
     * <p>
     * If https is in-use, this is the HTTPS port.
     * <p>
     * If http and https are in-use, this is the HTTPS port.
     * <p>
     * If there multiple ports of the same schema, return any one port in use.
     * <p>
     * See also {@link #getHttpPort} or Use {@link #getHttpsPort}.
     */
    public int getPort() {
        return httpsPort > 0 ? httpsPort : httpPort;
    }

    /**
     * Get the HTTP port.
     * <p>
     * Returns -1 for no HTTP port.
     * <p>
     * If there are multiple HTTP ports configured, returns one of them.
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * Get the HTTPS port.
     * <p>
     * Returns -1 for no HTTPS port.
     * <p>
     * If there are multiple HTTPS ports configured, returns one of them.
     */
    public int getHttpsPort() {
        return httpsPort;
    }

    /**
     * Calculate the server URL for "localhost".
     * <p>
     * Example: {@code http://localhost:3330/}.
     * The URL ends in "/".
     * The host name is "localhost".
     * If both HTTP and HTTPS are available, then reply with an HTTPS URL.
     * <p>
     * This operation is useful when using Fuseki as an embedded test server.
     */
    public String serverURL() {
        return schemeHostPort()+"/";
    }

    /**
     * Return the URL for a local dataset.
     * <p>
     * Example: {@code http://localhost:3330/dataset}.
     * The host name is "localhost".
     * If both HTTP and HTTPS are available, then reply with an HTTPS URL.
     * <p>
     * This operation is useful when using Fuseki as an embedded test server.
     */
    public String datasetURL(String dsName) {
        if ( ! dsName.startsWith("/") )
            dsName = "/"+dsName;
        return schemeHostPort()+dsName;
    }

    // schema://host:port, no trailing "/".
    private String schemeHostPort() {
        int port = getHttpPort();
        String scheme = "http";
        if ( getHttpsPort() > 0 ) { //&& server.getHttpPort() < 0 ) {
            scheme = "https";
            port =  getHttpsPort();
        }
        return scheme+"://localhost:"+port;
    }

    /** Get the underlying Jetty server which has also been set up. */
    public Server getJettyServer() {
        return server;
    }

    /**
     * Get the {@link ServletContext} used for Fuseki processing.
     * Adding new servlets is possible with care.
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Get the {@link DataAccessPointRegistry}.
     * This method is intended for inspecting the registry.
     */
    public DataAccessPointRegistry getDataAccessPointRegistry() {
        return DataAccessPointRegistry.get(getServletContext());
    }

    /**
     * Get the {@link OperationRegistry}.
     * This method is intended for inspecting the registry.
     */
    public OperationRegistry getOperationRegistry() {
        return OperationRegistry.get(getServletContext());
    }

    /**
     * Return the filename to the static content area.
     * Returns null if there is no such area.
     */
    public String getStaticContentDir() {
        return staticContentDir;
    }

    /**
     * Return the list of {@link FusekiModule}s for this server.
     */
    public FusekiModules getModules() {
        return modules;
    }

    /**
     * Start the server - the server continues to run after this call returns.
     * To synchronise with the server stopping, call {@link #join}.
     */
    public FusekiServer start() {
        try {
            FusekiModuleStep.serverBeforeStarting(this);
            server.start();
        }
        catch (IOException ex) {
            if ( ex.getCause() instanceof java.security.UnrecoverableKeyException )
                // Unbundle for clearer message.
                throw new FusekiException(ex.getMessage());
            throw new FusekiException(ex);
        }
        catch (IllegalStateException ex) {
            throw new FusekiException(ex.getMessage(), ex);
        }
        catch (Exception ex) {
            throw new FusekiException(ex);
        }

        // Post-start completion. Find the ports.
        Connector[] connectors = server.getServer().getConnectors();
        if ( connectors.length == 0 )
            serverLog.warn("Start Fuseki: No connectors");

        // Extract the ports from the Connectors.
        Arrays.stream(connectors).forEach(c->{
            if ( c instanceof ServerConnector connector ) {
                String protocol = connector.getDefaultConnectionFactory().getProtocol();
                String scheme = (protocol.startsWith("SSL-") || protocol.equals("SSL")) ? "https" : "http";
                int port = connector.getLocalPort();
                connector(scheme, port);
            }
        });

        FusekiModuleStep.serverAfterStarting(this);

        if ( httpsPort > 0 && httpPort > 0 )
            Fuseki.serverLog.info("Start Fuseki (http="+httpPort+" https="+httpsPort+")");
        else if ( httpsPort > 0 )
            Fuseki.serverLog.info("Start Fuseki (https="+httpsPort+")");
        else if ( httpPort > 0 )
            Fuseki.serverLog.info("Start Fuseki (http="+httpPort+")");
        else
            Fuseki.serverLog.info("Start Fuseki");

        // Any post-startup configuration here.
        // --
        // Done!
        return this;
    }

    private void connector(String scheme, int port) {
        switch (scheme) {
            case "http":
                if ( httpPort <= 0 )
                    httpPort = port;
                break;
            case "https":
                if ( httpsPort <= 0 )
                    httpsPort = port;
                break;
        }
    }

    /** Stop the server. */
    public void stop() {
        Fuseki.serverLog.info("Stop Fuseki");
        try {
            server.stop();
            FusekiModuleStep.serverStopped(this);
        } catch (Exception e) { throw new FusekiException(e); }
    }

    /** Wait for the server to exit. This call is blocking. */
    public void join() {
        try { server.join(); }
        catch (Exception e) { throw new FusekiException(e); }
    }

    /** FusekiServer.Builder */
    public static class Builder {
        private static final int DefaultServerPort  = 3330;
        private static final int PortUnset          = -2;
        private static final int PortInactive       = -3;

        // DataServices we build over multiple Builder calls.
        private Registry<String, DataService.Builder> dataServices = new Registry<>();
        // DataServices provided from the caller. These are immutable.
        private Registry<String, DataService> providedDataServices = new Registry<>();

        private final OperationRegistry  operationRegistry;
        // Default values.
        private int                      serverHttpPort     = PortUnset;
        private int                      serverHttpsPort    = PortUnset;
        private boolean                  networkLoopback    = false;
        private int                      minThreads         = -1;
        private int                      maxThreads         = -1;
        private ErrorHandler             errorHandler       = new FusekiErrorHandler();

        private boolean                  verbose            = false;
        private boolean                  withCompact        = false;
        private boolean                  withPing           = false;
        private boolean                  withMetrics        = false;
        private boolean                  withStats          = false;
        private boolean                  withTasks          = false;

        private String                   jettyServerConfig  = null;
        private Model                    configModel        = null;
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

        // Bearer authentication : verify and extract the user for a request.
        private Function<String, String> bearerVerifiedUser = null;

        // Other servlets to add. The pathspec for servlets must be unique.
        // Order does not matter, the rules of pathspec dispatch are "exact match"
        // before "prefix match".
        private Map<String, HttpServlet> servlets           = new HashMap<>();
        // whereas several filters can share a path spec and order matters.
        private List<Pair<String, Filter>> beforeFilters    = new ArrayList<>();
        private List<Pair<String, Filter>> afterFilters     = new ArrayList<>();

        // Modules to use to process the building of the server.
        // The default (fusekiModules is null) is the system-wide modules.
        private FusekiModules            fusekiModules     = null;

        private String                   contextPath        = "/";
        private String                   staticContentDir   = null;
        private SecurityHandler          securityHandler    = null;
        private Map<String, Object>      servletAttr        = new HashMap<>();

        //private Context                  context            = null;

        // The default CORS settings.
        private static final Map<String, String> corsInitParamsDft = new LinkedHashMap<>();
        static {
            // This is the CrossOriginFilter default.
            corsInitParamsDft.put(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
            // Variations from CrossOriginFilter defaults.
            corsInitParamsDft.put(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,DELETE,PUT,HEAD,OPTIONS,PATCH");
            corsInitParamsDft.put(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
                "X-Requested-With, Content-Type, Accept, Origin, Last-Modified, Authorization");
            // The 7 CORS default exposed headers.
            corsInitParamsDft.put(CrossOriginFilter.EXPOSED_HEADERS_PARAM,
                "Cache-Control, Content-Language, Content-Length, Content-Type, Expires, Last-Modified, Pragma");
            // Respond to preflight without passing OPTIONS down the filter chain.
            corsInitParamsDft.put(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, "false");
        }

        // Builder with standard operation-action mapping.
        private Builder() {
            this.operationRegistry = OperationRegistry.createStd();
        }

        // Builder with provided operation-action mapping.
        private Builder(OperationRegistry operationRegistry) {
            // Isolate.
            this.operationRegistry = OperationRegistry.createEmpty();
            OperationRegistry.copyConfig(operationRegistry, this.operationRegistry);
        }

        /**
         * Is this name already registered in this builder?
         * The name should be canonical.
         */
        private boolean isRegistered(String datasetPath) {
            datasetPath = DataAccessPoint.canonical(datasetPath);
            return dataServices.isRegistered(datasetPath) || providedDataServices.isRegistered(datasetPath);
        }

        /**
         * Set the HTTP port to run on.
         * <p>
         * If set to 0, a random free port will be used.
         */
        public Builder port(int port) {
            if ( port == -1 ) {
                this.serverHttpPort = PortInactive;
                return this;
            }
            if ( port < 0 )
                throw new IllegalArgumentException("Illegal port="+port+" : Port must be greater than or equal to zero, or -1 to unset");
            this.serverHttpPort = port;
            return this;
        }

        /**
         * Context path to Fuseki.  If it's "/" then Fuseki URL look like
         * "http://host:port/dataset/query" else "http://host:port/path/dataset/query"
         * The default is "/".
         */
        public Builder contextPath(String path) {
            this.contextPath = path;
            return this;
        }

        /** Restrict the server to only responding to the localhost interface. */
        public Builder loopback(boolean loopback) {
            this.networkLoopback = loopback;
            return this;
        }

        /** Set the location (filing system directory) to serve static files from. */
        public Builder staticFileBase(String directory) {
            requireNonNull(directory, "directory");
            if ( ! FileOps.exists(directory) )
                Fuseki.configLog.warn("File area not found: "+directory);
            this.staticContentDir = directory;
            return this;
        }

        /**
         * Get the location (if any has been set) to serve static files from.
         * Return null if unset.
         */
        public String staticFileBase() {
            return this.staticContentDir;
        }

        /**
         * Set a Jetty SecurityHandler.
         * <p>
         * This is an alternative to using the Fuseki Main built-in security
         * configuration.
         */
        public Builder securityHandler(SecurityHandler securityHandler) {
            requireNonNull(securityHandler, "securityHandler");
            this.securityHandler = securityHandler;
            return this;
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

        /** Add the "/$/ping" servlet that responds to HTTP very efficiently.
         * This is useful for testing whether a server is alive, for example, from a load balancer.
         */
        public Builder enablePing(boolean withPing) {
            this.withPing = withPing;
            return this;
        }

        /** Add the "/$/stats" servlet that responds with stats about the server,
         * including counts of all calls made.
         */
        public Builder enableStats(boolean withStats) {
            this.withStats = withStats;
            return this;
        }

        /** Add the "/$/metrics" servlet that responds with Prometheus metrics about the server. */
        public Builder enableMetrics(boolean withMetrics) {
            this.withMetrics = withMetrics;
            return this;
        }

        /**
         * Add the "/$/compact/*" servlet that triggers compaction for specified dataset.
         * Also adds the "/$/tasks/*" servlet if compact is enabled (but if compact is disabled,
         * then tasks is not automatically disabled).
         */
        public Builder enableCompact(boolean withCompact) {
            this.withCompact = withCompact;
            if (withCompact) {
                this.enableTasks(true);
            }
            return this;
        }

        /** Add the "/$/tasks" servlet that responds with info about tasks run on the server */
        public Builder enableTasks(boolean withTasks) {
            this.withTasks = withTasks;
            return this;
        }

        /**
         * Get the DataService.Builder, if any, in this builder for the given service name.
         * <p>
         * Returns the {@link org.apache.jena.fuseki.server.DataService.Builder DataService.Builder} or null.
         * <p>
         * This operation does not return the FusekiServer builder.
         */
        public DataService.Builder getDataServiceBuilder(String name) {
            requireNonNull(name, "name");
            name = DataAccessPoint.canonical(name);
            return dataServices.get(name);
        }

        /**
         * Get the DatasetGraph, if any, being built for a service in this builder.
         * <p>
         * Returns the DatasetGraph or null.
         * <p>
         * This operation does not return the FusekiServer builder.
         */
        public DatasetGraph getDataset(String name) {
            requireNonNull(name, "name");
            DataService.Builder b = getDataServiceBuilder(name);
            if ( b == null )
                return null;
            return b.dataset();
        }

        /**
         * Remove the dataset from being built.
         * <p>
         * Returns the DatasetGraph or null.
         * <p>
         * This operation does not return the builder.
         */
        public DatasetGraph remove(String name) {
            requireNonNull(name, "name");
            name = DataAccessPoint.canonical(name);
            DataService.Builder dSrvBuilder = dataServices.get(name);
            if ( dSrvBuilder != null ) {
                dataServices.remove(name);
                return dSrvBuilder.dataset();
            }
            DataService provided = providedDataServices.get(name);
            if ( provided != null ) {
                providedDataServices.remove(name);
                return provided.getDataset();
            }
            return null;
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
            name = DataAccessPoint.canonical(name);
            if ( isRegistered(name) )
                throw new FusekiConfigException("Data service name already registered: "+name);
            DataService.Builder dataServiceBuilder = DataService.newBuilder(dataset).withStdServices(allowUpdate);
            addNamedDataService$(name, dataServiceBuilder);
            return this;
        }

        /**
         * Add a dataset, do not configure it in this call. Subsequent calls of
         * {@code addEndpoint} and {@code addOperation} will be needed to give this
         * dataset some functionality.
         * <p>
         * This operation replaces any previous dataset and configuration with the same canonical name.
         * <p>
         * {@link org.apache.jena.fuseki.server.DataService.Builder DataService.Builder}.
         * for building the DataService separately.
         */
        public Builder addDataset(String name, DatasetGraph dataset) {
            requireNonNull(name, "name");
            requireNonNull(dataset, "dataset");
            DataService.Builder dataServiceBuilder = DataService.newBuilder(dataset);
            return addNamedDataService$(name, dataServiceBuilder);
        }

        public Builder add(String name, DataService.Builder dataServiceBuilder) {
            requireNonNull(name, "name");
            requireNonNull(dataServiceBuilder, "dataServiceBuilderr");
            addNamedDataService$(name, dataServiceBuilder);
            return this;
        }

        /** Add name and DataService-in-progress (the builder). */
        private Builder addNamedDataService$(String name, DataService.Builder builder) {
            name = DataAccessPoint.canonical(name);
            dataServices.put(name, builder);
            return this;
        }

        // ---- Pre-built DataServices

        /**
         * Add a data service that includes dataset and service names.
         * A {@link DataService} allows for choices of the various endpoint names.
         * A DataService added with this operation cannot be modified further
         * with other builder calls.
         */
        public Builder add(String name, DataService dataService) {
            requireNonNull(name, "name");
            requireNonNull(dataService, "dataService");
            return addDefinedDataService$(name, dataService);
        }

        private Builder addDefinedDataService$(String name, DataService dataService) {
            name = DataAccessPoint.canonical(name);
            if ( isRegistered(name) )
                throw new FusekiConfigException("Data service name already registered: "+name);
            providedDataServices.put(name, dataService);
            return this;
        }

        // ---- Configuration file.

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
            processConfigServerLevel(server);

            // Process server and services, whether via server ja:services or, if absent, by finding by type.

            // Context is only set, not deleted, in a configuration file.
            Context settings = new Context();
            List<DataAccessPoint> x = FusekiConfig.processServerConfiguration(model, settings);

            // Side effect - sets global context.
            Fuseki.getContext().putAll(settings);
            // Can further modify the services in the configuration file.
            x.forEach(dap->addDataAccessPoint(dap));
            configModel = model;
            return this;
        }

        /**
         * Configure using a Fuseki services/datasets assembler in a {@link Graph}.
         * <p>
         * The application is responsible for ensuring a correct classpath. For example,
         * including a dependency on {@code jena-text} if the configuration file includes
         * a text index.
         */
        public Builder parseConfig(Graph graph) {
            return parseConfig(ModelFactory.createModelForGraph(graph));
        }

        /** Add a {@link DataAccessPoint} as a builder. */
        private Builder addDataAccessPoint(DataAccessPoint dap) {
            if ( isRegistered(dap.getName()) )
                throw new FusekiConfigException("Data service name already registered: "+dap.getName());
            addNamedDataService$(dap.getName(), DataService.newBuilder(dap.getDataService()));
            return this;
        }

        /**
         * Build the server using a Jetty configuration file.
         * See <a href="https://wiki.eclipse.org/Jetty/Reference/jetty.xml_syntax">Jetty/Reference/jetty.xml_syntax</a>
         * This is instead of any other HTTP server settings such as port and HTTPs.
         */
        public Builder jettyServerConfig(String filename) {
            requireNonNull(filename, "filename");
            if ( ! FileOps.exists(filename) )
                throw new FusekiConfigException("File no found: "+filename);
            this.jettyServerConfig = filename;
            return this;
        }

        /**
         * Server level setting specific to Fuseki main.
         * General settings done by {@link FusekiConfig#processServerConfiguration}.
         */
        private void processConfigServerLevel(Resource server) {
            if ( server == null )
                return;

            if ( server.hasProperty(FusekiVocab.pServerContextPath) )
                contextPath(argString(server, FusekiVocab.pServerContextPath, "/"));
            enablePing(argBoolean(server, FusekiVocab.pServerPing,  false));
            enableStats(argBoolean(server, FusekiVocab.pServerStats, false));
            enableMetrics(argBoolean(server, FusekiVocab.pServerMetrics, false));
            enableCompact(argBoolean(server, FusekiVocab.pServerCompact, false));

            processConfAuthentication(server);

            serverAuth = FusekiConfig.allowedUsers(server);
        }

        /** Process password file, auth and realm settings on the server description. **/
        private void processConfAuthentication(Resource server) {
            String passwdFile = GraphUtils.getAsStringValue(server, FusekiVocab.pPasswordFile);
            if ( passwdFile != null )
                passwordFile(passwdFile);
            String realmStr = GraphUtils.getAsStringValue(server, FusekiVocab.pRealm);
            if ( realmStr != null )
                realm(realmStr);

            String authStr = GraphUtils.getAsStringValue(server, FusekiVocab.pAuth);
            if ( authStr != null ) {
                AuthScheme authScheme = AuthScheme.scheme(authStr);
                switch (authScheme) {
                    case BASIC: case DIGEST:
                        break;
                    case BEARER:
                        throw new FusekiConfigException("Authentication scheme not supported in config file: \""+authStr+"\"");
                    case UNKNOWN: default:
                        throw new FusekiConfigException("Authentication scheme not recognized: \""+authStr+"\"");
                }
                auth(authScheme);
            }
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

        private static String argString(Resource r, Property p, String dftValue) {
            try { GraphUtils.atmostOneProperty(r, p); }
            catch (NotUniqueException ex) {
                throw new FusekiConfigException(ex.getMessage());
            }
            Statement stmt = r.getProperty(p);
            if ( stmt == null )
                return dftValue;
            try {
                Node n = stmt.getObject().asLiteral().asNode();
                if ( ! G.isString(n) )
                    throw new FusekiConfigException("Not a string for '"+p+"' : "+stmt.getObject());
                return n.getLiteralLexicalForm();
            } catch (JenaException ex) {
                throw new FusekiConfigException("Not a string for '"+p+"' : "+stmt.getObject());
            }
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

//        /**
//         * Set the verifier for bearer tokens when auth scheme is {@link AuthScheme#BEARER}.
//         * The auth scheme is set to "Bearer" by this method.
//         */
//        public Builder bearerAuthVerifier(Function<String, String> verifiedUser) {
//            this.auth(AuthScheme.BEARER);
//            this.bearerVerifiedUser = verifiedUser;
//            return this;
//        }

        /**
         * Set the password file. This will be used to build a {@link #securityHandler
         * security handler} if one is not supplied. Setting null clears any previous entry.
         * The file should be in the format of
         * <a href="https://www.eclipse.org/jetty/documentation/current/configuring-security.html#hash-login-service">Eclipse jetty password file</a>.
         */
        public Builder passwordFile(String passwordFile) {
            if ( passwordFile.startsWith("file:") )
                passwordFile = IRILib.IRIToFilename(passwordFile);
            this.passwordFile = passwordFile;
            return this;
        }

        /**
         * Set the HTTPs port and provide the certificate store and password.
         * <br/>
         * Pass -1 for the httpsPort to clear the settings.
         * <br/>
         * Pass port 0 to get an allocated free port on startup.
         */
        public Builder https(int httpsPort, String certStore, String certStorePasswd) {
            requireNonNull(certStore, "certStore");
            requireNonNull(certStorePasswd, "certStorePasswd");
            if ( httpsPort <= -1 ) {
                this.serverHttpsPort = PortInactive;
                this.httpsKeystore = null;
                this.httpsKeystorePasswd = null;
                return this;
            }
            this.httpsKeystore = certStore;
            this.httpsKeystorePasswd = certStorePasswd;
            this.serverHttpsPort = httpsPort;
            return this;
        }

        /**
         * Set the HTTPs port and read the certificate store location and password from a file.
         * The file can be secured by the host OS.
         * This means the password for the certificate is not in the application code.
         * <p>
         * The file format is a JSON object:
         * <pre>
         * {
         *     "keystore" : "mykey.jks" ,
         *     "passwd"   : "certificate password"
         * }
         * </pre>
         * Pass -1 for the httpsPort to clear the settings.
         * <br/>
         * Pass port 0 to get an allocated free port on startup.
         */
        public Builder https(int httpsPort, String certificate) {
            requireNonNull(certificate, "certificate file");
            if ( httpsPort <= -1 ) {
                this.serverHttpsPort = PortInactive;
                this.httpsKeystore = null;
                this.httpsKeystorePasswd = null;
                return this;
            }
            setHttpsCert(certificate);
            this.serverHttpsPort = httpsPort;
            return this;
        }

        private void setHttpsCert(String filename) {
            try {
                JsonObject httpsConf = JSON.read(filename);
                Path path = Path.of(filename).toAbsolutePath();
                String keystore = httpsConf.get("keystore").getAsString().value();
                // Resolve relative to the https setup file.
                this.httpsKeystore = path.getParent().resolve(keystore).toString();
                this.httpsKeystorePasswd = httpsConf.get("passwd").getAsString().value();
            } catch (Exception ex) {
                this.httpsKeystore = null;
                this.httpsKeystorePasswd = null;
                throw new FusekiConfigException("Failed to read the HTTP details file: "+ex.getMessage());
            }
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
            if ( processor instanceof HttpServlet proc )
                servlet = proc;
            else
                servlet = new ServletAction(processor, log);
            addServlet(pathSpec, servlet);
            return this;
        }

        /**
         * Add the given servlet with the {@code pathSpec}. These servlets are added so
         * that they are checked after the Fuseki filter for datasets and before the
         * static content handler (which is the last servlet) used for
         * {@link #staticFileBase(String)}.
         */
        public Builder addServlet(String pathSpec, HttpServlet servlet) {
            requireNonNull(pathSpec, "pathSpec");
            requireNonNull(servlet, "servlet");
            servlets.put(pathSpec, servlet);
            return this;
        }

        /** Add a servlet attribute. Pass a value of null to remove any existing binding. */
        public Builder addServletAttribute(String attrName, Object value) {
            requireNonNull(attrName, "attrName");
            if ( value != null )
                servletAttr.put(attrName, value);
            else
                servletAttr.remove(attrName);
            return this;
        }

        /**
         * Read a servlet attribute that has been set during building this server.
         */
        public Object getServletAttribute(String attrName) {
            requireNonNull(attrName, "attrName");
            return servletAttr.get(attrName);
        }

        /**
         * Add a filter with the pathSpec. Note that Fuseki dispatch uses a servlet filter
         * which is the last in the filter chain.
         */
        public Builder addFilter(String pathSpec, Filter filter) {
            requireNonNull(pathSpec, "pathSpec");
            requireNonNull(filter, "filter");
            beforeFilters.add(Pair.create(pathSpec, filter));
            return this;
        }

        /** @deprecated Use {@link #fusekiModules(FusekiModules)}. */
        @Deprecated
        public Builder setModules(FusekiModules modules) {
            return fusekiModules(modules);
        }

        // Conflict with fusekiModules()
//        /**
//         * Set the {@link FusekiModules Fuseki Modules} for a server.
//         * If no modules are added to a builder, then the system-wide default set (found by loading FusekiModules
//         * via Java's {@link ServiceLoader} mechanism) is used.
//         * <p>Pass {@code null} to switch back the system-wide default set.
//         *
//         * @see FusekiModules
//         */
//        public Builder fusekiModules(FusekiModule ...fmods) {
//            return fusekiModules(FusekiModules.create(fmods));
//        }

        /**
         * Set the {@link FusekiModule Fuseki Module} for a server.
         * If no modules are added to a builder, then the system-wide default set (found by loading FusekiModule
         * via Java's {@link ServiceLoader} mechanism) is used.
         * <p>Pass {@code null} to switch back the system-wide default set.
         *
         * @see FusekiModules
         */
        public Builder fusekiModules(FusekiModules modules) {
            fusekiModules = modules;
            return this;
        }

        /**
         * Return the current list of Fuseki modules in the builder.
         */
        public FusekiModules fusekiModules() {
            return fusekiModules;
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
         * Add an operation to the server, together with its triggering Content-Type (which may be null) and servlet handler.
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

            if ( ! isRegistered(name) )
                throw new FusekiConfigException("Dataset not registered: "+datasetName);

            DataService.Builder dsBuilder = dataServices.get(name);
            Endpoint endpoint = Endpoint.create()
                .operation(operation)
                .endpointName(endpointName)
                .authPolicy(authPolicy)
                .build();
            dsBuilder.addEndpoint(endpoint);
        }

        /**
         * Set the number threads used by Jetty.
         * This uses a {@link org.eclipse.jetty.util.thread.QueuedThreadPool}
         * provided by Jetty.
         * <p>
         * Argument order is (minThreads, maxThreads).
         * <ul>
         * <li>Use (-1,-1) for Jetty "default". The Jetty 9.4 defaults are (min=8,max=200).
         * <li>If (min != -1, max is -1) then the default max is 20.
         * <li>If (min is -1, max != -1) then the default min is 2.
         * </ul>
         */
        public Builder numServerThreads(int minThreads, int maxThreads) {
            if ( minThreads >= 0 && maxThreads > 0 ) {
                if ( minThreads > maxThreads )
                    throw new FusekiConfigException(String.format("Bad thread setting: (min=%d, max=%d)", minThreads, maxThreads));
            }
            this.minThreads = minThreads;
            this.maxThreads = maxThreads;
            return this;
        }

        /**
         * Set the maximum number threads used by Jetty.
         * This is equivalent to {@code numServerThreads(-1, maxThreads)}
         * and overrides any previous setting of the maximum number of threads.
         * In development or in embedded use, limiting the maximum threads can be useful.
         */
        public Builder maxServerThreads(int maxThreads) {
            if ( minThreads > maxThreads )
                throw new FusekiConfigException(String.format("Bad thread setting: (min=%d, max=%d)", minThreads, maxThreads));
            numServerThreads(minThreads, maxThreads);
            return this;
        }

        // Placeholder for the future.
        // Not currently used (servlet attributes in the ServletContext may be more appropriate for many uses)
//        /** Set context value. */
//        public void context(Symbol symbol, Object value) {
//            if ( context == null )
//                context = new Context();
//            context.set(symbol, value);
//        }
//
//        /** Return the context (may be null) */
//        public Context context() {
//            return context;
//        }

        /**
         * Shortcut: build, then start the server.
         */
        public FusekiServer start() {
            return build().start();
        }

        /**
         * Build a server according to the current description.
         */
        public FusekiServer build() {
            if ( serverHttpPort < 0 && serverHttpsPort < 0 )
                serverHttpPort = DefaultServerPort;

            FusekiModules modules = (fusekiModules == null)
                    ? FusekiAutoModules.load()
                    : fusekiModules;

            // FusekiModule call - final preparations.
            Set<String> datasetNames = Set.copyOf(dataServices.keys());
            FusekiModuleStep.prepare(modules, this, datasetNames, configModel);

            // Freeze operation registry (builder may be reused).
            OperationRegistry operationReg = new OperationRegistry(operationRegistry);

            // Internally built - does not need to be copied.
            DataAccessPointRegistry dapRegistry = buildStart();

            // FusekiModule call - inspect the DataAccessPointRegistry.
            FusekiModuleStep.configured(modules, this, dapRegistry, configModel);

            // Setup Prometheus metrics. This will become a module.
            bindPrometheus(dapRegistry);

            // Process the DataAccessPointRegistry for security.
            buildSecurity(dapRegistry);

            try {
                validate();

                // Build the ServletContextHandler - the Jetty server configuration.
                ServletContextHandler handler = buildFusekiServerContext();
                boolean hasFusekiSecurityHandler = applySecurityHandler(handler);
                // Prepare the DataAccessPointRegistry.
                // Put it in the servlet context.
                // This would be the reload operation.
                applyDatabaseSetup(handler, dapRegistry, operationReg);

                // Must be after the DataAccessPointRegistry is in the servlet context.
                if ( hasFusekiSecurityHandler )
                    applyAccessControl(handler, dapRegistry);

                if ( jettyServerConfig != null ) {
                    Server server = jettyServer(handler, jettyServerConfig);
                    return new FusekiServer(-1, -1, server, staticContentDir, modules, handler.getServletContext());
                }

                Server server;
                int httpPort = Math.max(-1, serverHttpPort);
                int httpsPort = Math.max(-1, serverHttpsPort);

                if ( httpsPort <= -1 ) {
                    // HTTP only
                    server = jettyServer(handler, httpPort, minThreads, maxThreads);
                } else {
                    // HTTPS, no http redirection.
                    server = jettyServerHttps(handler, httpPort, httpsPort, minThreads, maxThreads,
                                              httpsKeystore, httpsKeystorePasswd);
                }
                // The servletContext error handler isn't called when there is a
                // dispatch to something that isn't there.
                // Jetty default error handler is broken for application/json for Jetty GH-10474
                // Jetty 12.0.1 - fixed at 12.0.next
                if ( errorHandler != null )
                    server.setErrorHandler(errorHandler);

                if ( networkLoopback )
                    applyLocalhost(server);

                FusekiServer fusekiServer = new FusekiServer(httpPort, httpsPort, server, staticContentDir, modules, handler.getServletContext());
                FusekiModuleStep.server(fusekiServer);
                return fusekiServer;
            } finally {
                buildFinish();
            }
        }

        private DataAccessPointRegistry buildStart() {
            DataAccessPointRegistry dapRegistry = new DataAccessPointRegistry();
            dataServices.forEach((name, builder)->{
                DataService dSrv = builder.build();
                DataAccessPoint dap = new DataAccessPoint(name, dSrv);
                dapRegistry.register(dap);
            });
            providedDataServices.forEach((name, dSrv)->{
                DataAccessPoint dap = new DataAccessPoint(name, dSrv);
                dapRegistry.register(dap);
            });
            return dapRegistry;
        }

        private void bindPrometheus(DataAccessPointRegistry dapRegistry) {
            if ( withMetrics ) {
                // Connect to Prometheus metrics.
                MetricsProviderRegistry.bindPrometheus(dapRegistry);
            }
        }

        /**
         * Build one configured Fuseki processor (ServletContext), same dispatch ContextPath
         */
        private ServletContextHandler buildFusekiServerContext() {
            // DataAccessPointRegistry was created by buildStart so does not need copying.
            ServletContextHandler handler = buildServletContext(contextPath);
            ServletContext cxt = handler.getServletContext();
            Fuseki.setVerbose(cxt, verbose);
            servletAttr.forEach((n,v)->cxt.setAttribute(n, v));
            JettyLib.setMimeTypes(handler);
            servletsAndFilters(handler);
            return handler;
        }

        private static void prepareDataServices(DataAccessPointRegistry dapRegistry, OperationRegistry operationReg) {
            dapRegistry.forEach((name, dap) -> {
                // Override for graph-level access control.
                if ( DataAccessCtl.isAccessControlled(dap.getDataService().getDataset()) ) {
                    dap.getDataService().forEachEndpoint(ep->
                        FusekiLib.modifyForAccessCtl(ep, DataAccessCtl.requestUserServlet));
                }
            });

            // Start services.
            dapRegistry.forEach((name, dap)-> {
                // Custom processors (endpoint specific, fuseki:implementation)
                // will have already been set. Normal defaults need setting
                // from the OperationRegistry in scope.
                dap.getDataService().setEndpointProcessors(operationReg);
                dap.getDataService().goActive();
            });
        }

        /**
         * Given a ServletContextHandler, set the servlet attributes for
         * {@link DataAccessPointRegistry} and {@link OperationRegistry}.
         */
        private static void applyDatabaseSetup(ServletContextHandler handler,
                                                          DataAccessPointRegistry dapRegistry,
                                                          OperationRegistry operationReg) {
            // Final wiring up of DataAccessPointRegistry
            prepareDataServices(dapRegistry, operationReg);

            ServletContext cxt = handler.getServletContext();
            OperationRegistry.set(cxt, operationReg);
            DataAccessPointRegistry.set(cxt, dapRegistry);
        }

        private ConstraintSecurityHandler buildSecurityHandler() {
            UserStore userStore = JettySecurityLib.makeUserStore(passwordFile);
            return JettySecurityLib.makeSecurityHandler(realm, userStore, authScheme);
        }

        // These booleans are only for validation.
        // They do not affect the build() step.

        // Triggers some checking.
        private boolean hasAuthenticationHandler = false;

        // Whether there is any per-graph access control.
        // Used for checking.
        private boolean hasDataAccessControl     = false;

        // Do we need to authenticate the user?
        // Triggers some checking.
        private boolean authenticateUser         = false;

        private void buildSecurity(DataAccessPointRegistry dataAccessPoints) {
            // -- Server and dataset authentication
            hasAuthenticationHandler = (passwordFile != null) || (securityHandler != null);

            if ( realm == null )
                realm = Auth.dftRealm;

            // See if there are any DatasetGraphAccessControl.
            hasDataAccessControl = dataAccessPoints.keys().stream()
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
        }

        /** Do some checking to make sure setup is consistent. */
        private void validate() {
            if ( ! hasAuthenticationHandler && authScheme != AuthScheme.BEARER ) {
                if ( authenticateUser )
                    Fuseki.configLog.warn("Authentication of users required (e.g. 'allowedUsers' is set) but there is no authentication setup (e.g. password file)");
                if ( hasDataAccessControl )
                    Fuseki.configLog.warn("Data-level access control in the configuration but there is no authentication setup (e.g. password file)");
            }
            if ( authScheme != null ) {
                switch(authScheme) {
                    case BASIC:
                    case DIGEST:
                        // Authentication style set but no authentication setup.
                        // Unsecured server. Don't continue.
                        if ( passwordFile == null && securityHandler == null )
                            throw new FusekiConfigException("Authentication scheme set but no password file");
                        break;
                    case BEARER:
//                        if ( bearerVerifiedUser == null )
//                            throw new FusekiConfigException("Bearer authentication set but no function to get the verified user");
                        break;
                    case UNKNOWN:
                        throw new FusekiConfigException("Unknown authentication scheme");
                }
            }
        }

        /**
         * Set up the ServletContextHandler if there is a securityHandler or password file
         * Return true if there is a security handler built for this server (not externally provided).
         */
        private boolean applySecurityHandler(ServletContextHandler cxt) {
            if ( securityHandler == null && passwordFile != null )
                securityHandler = buildSecurityHandler();

            // -- Access control
            if ( securityHandler == null )
                return false;

            cxt.setSecurityHandler(securityHandler);
            if ( ! ( securityHandler instanceof ConstraintSecurityHandler ) ) {
                // Externally provided security handler.
                return false;
            }

            ConstraintSecurityHandler csh = (ConstraintSecurityHandler)securityHandler;
            if ( hasServerWideAuth() )
                JettySecurityLib.addPathConstraint(csh, "/*");
            return true;
        }

        /** Look in a DataAccessPointRegistry for datasets and endpoints with authentication policies.*/
        private void applyAccessControl(ServletContextHandler cxt, DataAccessPointRegistry dapRegistry) {
            ConstraintSecurityHandler csh = (ConstraintSecurityHandler)(cxt.getSecurityHandler());
            if ( csh == null )
                return ;

            // Look for datasets and endpoints that need login and add a path constraint.
            dapRegistry.forEach((name, dap)-> {
                if ( ! authAny(dap.getDataService().authPolicy()) ) {
                    // Dataset wide.
                    JettySecurityLib.addPathConstraint(csh, DataAccessPoint.canonical(name));
                    JettySecurityLib.addPathConstraint(csh, DataAccessPoint.canonical(name)+"/*");
                }
                else {
                    // Check endpoints.
                    dap.getDataService().forEachEndpoint(ep->{
                        if ( ! authAny(ep.getAuthPolicy()) ) {
                            // Unnamed - unfortunately this then applies to all operations on the dataset.
                            if ( ep.getName().isEmpty() ) {
                                JettySecurityLib.addPathConstraint(csh, DataAccessPoint.canonical(name));
                                JettySecurityLib.addPathConstraint(csh, DataAccessPoint.canonical(name)+"/*");
                            } else {
                                // Named service.
                                JettySecurityLib.addPathConstraint(csh, DataAccessPoint.canonical(name)+"/"+ep.getName());
                                if ( Fuseki.GSP_DIRECT_NAMING )
                                    JettySecurityLib.addPathConstraint(csh, DataAccessPoint.canonical(name)+"/"+ep.getName()+"/*");
                            }
                        }
                    });
                }
            });
        }

        /** Build a ServletContextHandler */
        private ServletContextHandler buildServletContext(String contextPath) {
            if ( contextPath == null || contextPath.isEmpty() )
                contextPath = "/";
            else if ( !contextPath.startsWith("/") )
                contextPath = "/" + contextPath;
            ServletContextHandler context = new ServletContextHandler();
            context.setDisplayName(Fuseki.servletRequestLogName);
            // Also set on the server which handles request that don't dispatch.
            context.setErrorHandler(errorHandler);
            context.setContextPath(contextPath);
            // SPARQL Update by HTML - not the best way but.
            context.setMaxFormContentSize(1024*1024);
            // securityHandler done in buildAccessControl
            return context;
        }

        /** Add servlets and servlet filters, including the {@link FusekiFilter} */
        private void servletsAndFilters(ServletContextHandler context) {
            // First in chain. CORS.
            // Preflight to set to respond without passing on OPTIONS.
            // Otherwise passes on to the next filter.
            if ( corsInitParams != null ) {
                Filter corsFilter = new CrossOriginFilter();
                FilterHolder holder = new FilterHolder(corsFilter);
                holder.setInitParameters(corsInitParams);
                addFilterHolder(context, "/*", holder);
            }

            // Authentication.
            if ( hasServerWideAuth() ) {
                Predicate<String> auth = serverAuth::isAllowed;
                AuthFilter authFilter = new AuthFilter(auth);
                addFilter(context, "/*", authFilter);
            }

            beforeFilters.forEach(pair -> addFilter(context, pair.getLeft(), pair.getRight()));

            // End of chain though there may be custom "afterFilters".
            // This servlet filter may dispatch and not pass on requests.
            // Looks for any URL that starts with a dataset name.
            FusekiFilter ff = new FusekiFilter();
            addFilter(context, "/*", ff);

            // and then any additional servlets and filters.
            if ( withPing )
                addServlet(context, "/$/ping", new ActionPing());
            if ( withStats )
                addServlet(context, "/$/stats/*", new ActionStats());
            if ( withMetrics )
                addServlet(context, "/$/metrics", new ActionMetrics());
            if ( withCompact )
                addServlet(context, "/$/compact/*", new ActionCompact());
            if ( withTasks )
                addServlet(context, "/$/tasks/*", new ActionTasks());

            servlets.forEach((pathspecp, servlet) -> addServlet(context, pathspecp, servlet));
            afterFilters.forEach(pair -> addFilter(context, pair.getLeft(), pair.getRight()));

            // Finally, drop to state content if configured.
            if ( staticContentDir != null ) {
                DefaultServlet staticServlet = new DefaultServlet();
                ServletHolder staticContent = new ServletHolder(staticServlet);
                staticContent.setInitParameter("baseResource", staticContentDir);
                //staticContent.setInitParameter("cacheControl", "false");
                context.addServlet(staticContent, "/");
            } else {
                // Backstop servlet
                // Jetty default is 404 on GET and 405 otherwise
                HttpServlet staticServlet = new Servlet404();
                ServletHolder staticContent = new ServletHolder(staticServlet);
                context.addServlet(staticContent, "/");
            }
        }

        /** 404 for HEAD/GET/POST/PUT */
        static class Servlet404 extends HttpServlet {

            public Servlet404() {}
            // service()?
            @Override
            protected void doHead(HttpServletRequest req, HttpServletResponse resp)     { err404(req, resp); }
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp)      { err404(req, resp); }
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp)     { err404(req, resp); }
            @Override
            protected void doPut(HttpServletRequest req, HttpServletResponse resp)      { err404(req, resp); }
            //protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            //protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
            //protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            private static void err404(HttpServletRequest req, HttpServletResponse response) {
                try {
                    response.sendError(HttpSC.NOT_FOUND_404, HttpSC.getMessage(HttpSC.NOT_FOUND_404));
                } catch (IOException ex) {}
            }
        }

        private static void addServlet(ServletContextHandler context, String pathspec, HttpServlet httpServlet) {
            ServletHolder sh = new ServletHolder(httpServlet);
            context.addServlet(sh, pathspec);
        }

        private static void addFilter(ServletContextHandler context, String pathspec, Filter filter) {
            FilterHolder holder = new FilterHolder(filter);
            addFilterHolder(context, pathspec, holder);
        }

        private static void addFilterHolder(ServletContextHandler context, String pathspec, FilterHolder holder) {
            context.addFilter(holder, pathspec, null);
        }

        /** Jetty server with one connector/port. */
        private static Server jettyServer(ServletContextHandler handler, int port, int minThreads, int maxThreads) {
            Server server = JettyServer.jettyServer(minThreads, maxThreads);
            HttpConfiguration httpConfig = JettyLib.httpConfiguration();

            // Do not add "Server: Jetty(....) unless configured to do so.
            if ( Fuseki.outputJettyServerHeader )
                httpConfig.setSendServerVersion(true);

            HttpConnectionFactory f1 = new HttpConnectionFactory(httpConfig);
            ServerConnector connector = new ServerConnector(server, f1);
            connector.setPort(port);
            server.addConnector(connector);
            server.setHandler(handler);
            return server;
        }

        private Server jettyServer(ServletContextHandler handler, String jettyServerConfig) {
            serverLog.info("Jetty server config file = " + jettyServerConfig);
            Server server = JettyServer.jettyServer(jettyServerConfig);
            server.setHandler(handler);
            return server;
        }

        /** Jetty server with https */
        private static Server jettyServerHttps(ServletContextHandler handler, int httpPort, int httpsPort, int minThreads, int maxThreads, String keystore, String certPassword) {
            return JettyHttps.jettyServerHttps(handler, keystore, certPassword, httpPort, httpsPort, minThreads, maxThreads);
        }

        /** Restrict connectors to localhost */
        private static void applyLocalhost(Server server) {
            Connector[] connectors = server.getConnectors();
            for ( int i = 0; i < connectors.length; i++ ) {
                if ( connectors[i] instanceof ServerConnector serverConnector) {
                    serverConnector.setHost("localhost");
                }
            }
        }
    }
}

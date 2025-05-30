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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.main.sys.FusekiSystemConstants;
import org.apache.jena.fuseki.main.sys.JettyLib;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.OperationRegistry;
import org.apache.jena.fuseki.servlets.ActionBase;
import org.apache.jena.riot.WebContent;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jetty server for servlets, including being able to run Fuseki {@link ActionBase} derived servlets.
 * Static RDF types by file extension can be enabled.
 */
public class JettyServer {
    // Possibility: Use this for the super class of FusekiServer or within FusekiServer.jettyServer
    // as implementation inheritance.
    // Caution :
    //   there are small differences e.g. in building where order matters.
    //   Setting default (look for FusekiServerConstants)
    //

    private static Logger LOG = LoggerFactory.getLogger("HTTP");

    protected final Server server;
    protected int port;

    public static Builder create() {
        return new Builder();
    }

    protected JettyServer(int port, Server server) {
        this.server = server;
        this.port = port;
    }

    /**
     * Return the port begin used.
     * This will be the give port, which defaults to 3330, or
     * the one actually allocated if the port was 0 ("choose a free port").
     */
    public int getPort() {
        return port;
    }

    /** Get the underlying Jetty server which has also been set up. */
    public Server getJettyServer() {
        return server;
    }

    /** Get the {@link ServletContext}.
     * Adding new servlets is possible with care.
     */
    public ServletContext getServletContext() {
        return ((ServletContextHandler)server.getHandler()).getServletContext();
    }

    /** Start the server - the server continues to run after this call returns.
     *  To synchronise with the server stopping, call {@link #join}.
     */
    public JettyServer start() {
        try { server.start(); }
        catch (Exception e) { throw new RuntimeException(e); }
        if ( port == 0 )
            port = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
        logStart();
        return this;
    }

    protected void logStart() {
        LOG.info("Start (port="+port+")");
    }

    /** Stop the server. */
    public void stop() {
        logStop();
        try { server.stop(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    protected void logStop() {
        LOG.info("Stop (port="+port+")");
    }

    /** Wait for the server to exit. This call is blocking. */
    public void join() {
        try { server.join(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /** Simple error handler - always text/plain. */
    public static class PlainErrorHandler extends ErrorHandler {
        public PlainErrorHandler() {}

        @Override
        protected boolean generateAcceptableResponse(Request request, Response response, Callback callback, String contentType, List<Charset> charsets, int code, String message, Throwable cause) throws IOException {
            System.err.println(">> plain:generateAcceptableResponse");
            // Force text/plain
            // https://github.com/eclipse/jetty.project/issues/10474 (Bug in 12.0.0, 12.0.1 for applicatipon/json)
            if ( contentType != null && contentType.equals(WebContent.contentTypeJSON) )
                contentType = MimeTypes.Type.TEXT_PLAIN.asString();

            try {
                boolean b =
                        super.generateAcceptableResponse(request, response, callback,
                                                         //"text/plain", List.of(StandardCharsets.UTF_8),
                                                         contentType, charsets,
                                                         code, message, cause);
                System.err.println("<< plain:generateAcceptableResponse: "+b);
                return b;

            } catch (IllegalStateException ex) {
                System.err.println("<< plain:generateAcceptableResponse: IllegalStateException");
                ex.printStackTrace();
                return true;
            }
        }

        @Override
        protected void generateResponse(Request request, Response response, int code, String message, Throwable cause, Callback callback) throws IOException {
            generateAcceptableResponse(request, response, callback, WebContent.contentTypeTextPlain, List.of(StandardCharsets.UTF_8), code, message, cause);
        }

//        @Override
//        protected void writeErrorPlain(Request request, PrintWriter writer, int code, String message, Throwable cause, boolean showStacks) {
//            writer.write("HTTP ERROR ");
//            writer.write(Integer.toString(code));
//            writer.write(' ');
//            writer.write(StringUtil.sanitizeXmlString(message));
//            writer.write("\n");
//            writer.printf("URI: %s%n", request.getHttpURI());
//            writer.printf("STATUS: %s%n", code);
//            writer.printf("MESSAGE: %s%n", message);
//            while (cause != null) {
//                writer.printf("CAUSED BY %s%n", cause);
//                if (showStacks)
//                    cause.printStackTrace(writer);
//                cause = cause.getCause();
//            }
//        }
    }

    public static class JettyConfigException extends FusekiConfigException {
        public JettyConfigException(String msg) { super(msg); }
    }

    public static class Builder {
        private int                     port               = -1;
        private int                     minThreads         = -1;
        private int                     maxThreads         = -1;
        private boolean                 loopback           = false;
        private String                  jettyServerConfig  = null;
        protected boolean               verbose            = false;
        // Other servlets to add.
        private List<Pair<String, HttpServlet>> servlets   = new ArrayList<>();
        private List<Pair<String, Filter>> filters         = new ArrayList<>();

        private String                  contextPath        = "/";
        private String                  servletContextName = "Jetty";
        private String                  staticContentDir   = null;
        private SecurityHandler         securityHandler    = null;
        private ErrorHandler            errorHandler       = new PlainErrorHandler();
        private Map<String, Object>     servletAttr        = new HashMap<>();

        public Builder() {}

        /** Set the port to run on. */
        public Builder port(int port) {
            if ( port < 0 )
                throw new IllegalArgumentException("Illegal port="+port+" : Port must be greater than or equal to zero.");
            this.port = port;
            return this;
        }

        /**
         * Build the server using a Jetty configuration file.
         * See <a href="https://wiki.eclipse.org/Jetty/Reference/jetty.xml_syntax">Jetty/Reference/jetty.xml_syntax</a>
         * This is instead of any other server settings such as port or https.
         */
        public Builder jettyServerConfig(String filename) {
            requireNonNull(filename, "filename");
            if ( ! FileOps.exists(filename) )
                throw new JettyConfigException("File not found: "+filename);
            this.jettyServerConfig = filename;
            return this;
        }

        /**
         * Context path.  If it's "/" then Server URL will look like
         * "http://host:port/" else "http://host:port/path/"
         * (or no port if :80).
         */
        public Builder contextPath(String path) {
            requireNonNull(path, "path");
            this.contextPath = path;
            return this;
        }

        /**
         * ServletContextName.
         */
        public Builder servletContextName(String name) {
            requireNonNull(name, "name");
            this.servletContextName = name;
            return this;
        }

        /** Restrict the server to only responding to the localhost interface. */
        public Builder loopback(boolean loopback) {
            this.loopback = loopback;
            return this;
        }

        /** Set the location (filing system directory) to serve static file from. */
        public Builder staticFileBase(String directory) {
            requireNonNull(directory, "directory");
            this.staticContentDir = directory;
            return this;
        }

        /** Set a Jetty SecurityHandler.
         * <p>
         *  By default, the server runs with no security.
         *  This is more for using the basic server for testing.
         *  The full Fuseki server provides security with Apache Shiro
         *  and a defensive reverse proxy (e.g. Apache httpd) in front of the Jetty server
         *  can also be used, which provides a wide varity of proven security options.
         */
        public Builder securityHandler(SecurityHandler securityHandler) {
            requireNonNull(securityHandler, "securityHandler");
            this.securityHandler = securityHandler;
            return this;
        }

        /** Set an {@link ErrorHandler}.
         * <p>
         *  By default, the server runs with error handle that prints the code and message.
         */
        public Builder errorHandler(ErrorHandler errorHandler) {
            requireNonNull(errorHandler, "securityHandler");
            this.errorHandler = errorHandler;
            return this;
        }

        /** Set verbose logging */
        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        /**
         * Set the number threads used by Jetty. This uses a {@code org.eclipse.jetty.util.thread.QueuedThreadPool} provided by Jetty.
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
                    throw new JettyConfigException(String.format("Bad thread setting: (min=%d, max=%d)", minThreads, maxThreads));
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
                throw new JettyConfigException(String.format("Bad thread setting: (min=%d, max=%d)", minThreads, maxThreads));
            numServerThreads(minThreads, maxThreads);
            return this;
        }

        /**
         * Add the given servlet with the pathSpec. These are added so that they are
         * before the static content handler (which is the last servlet)
         * used for {@link #staticFileBase(String)}.
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
         * Add the given filter with the pathSpec.
         * It is applied to all dispatch types.
         */
        public Builder addFilter(String pathSpec, Filter filter) {
            requireNonNull(pathSpec, "pathSpec");
            requireNonNull(filter, "filter");
            filters.add(Pair.create(pathSpec, filter));
            return this;
        }

        /**
         * Build a server according to the current description.
         */
        public JettyServer build() {
            ServletContextHandler handler = buildServletContext();
            // Use HandlerCollection for several ServletContextHandlers and thus several ServletContext.
            Server server = jettyServerConfig != null
                    ? jettyServer(jettyServerConfig)
                    : jettyServer(minThreads, maxThreads);
            serverAddConnectors(server, port, loopback);
            server.setHandler(handler);
            return new JettyServer(port, server);
        }

        /** Build a ServletContextHandler : one servlet context */
        private ServletContextHandler buildServletContext() {
            ServletContextHandler handler = buildServletContext(contextPath);
            ServletContext cxt = handler.getServletContext();
            adjustForFuseki(cxt);
            servletAttr.forEach((n,v)->cxt.setAttribute(n, v));
            servletsAndFilters(handler);
            return handler;
        }

        private void adjustForFuseki(ServletContext cxt) {
            // For Fuseki servlets added directly.
            // This enables servlets inheriting from {@link ActionBase} to work in the
            // plain Jetty server, e.g. to use Fuseki logging.
            try {
                Fuseki.setVerbose(cxt, verbose);
                OperationRegistry.set(cxt, OperationRegistry.createEmpty());
                // Empty DataAccessPointRegistry, no nulls!
                DataAccessPointRegistry.set(cxt, new DataAccessPointRegistry());
            } catch (NoClassDefFoundError err) {
                LOG.info("Fuseki classes not found");
            }
        }

        /** Build a ServletContextHandler. */
        private ServletContextHandler buildServletContext(String contextPath) {
            if ( contextPath == null || contextPath.isEmpty() )
                contextPath = "/";
            else if ( !contextPath.startsWith("/") )
                contextPath = "/" + contextPath;
            ServletContextHandler context = new ServletContextHandler();
            context.setDisplayName(servletContextName);
            context.setErrorHandler(errorHandler);
            context.setContextPath(contextPath);
            // Large - for SPARQL Update by HTML forms
            context.setMaxFormContentSize(FusekiSystemConstants.jettyMaxFormContentSize);
            if ( securityHandler != null )
                context.setSecurityHandler(securityHandler);
            return context;
        }

        /** Add servlets and servlet filters */
        private void servletsAndFilters(ServletContextHandler context) {
            servlets.forEach(p-> addServlet(context, p.getLeft(), p.getRight()) );
            filters.forEach (p-> addFilter (context, p.getLeft(), p.getRight()) );

            if ( staticContentDir != null ) {
                DefaultServlet staticServlet = new DefaultServlet();
                ServletHolder staticContent = new ServletHolder(staticServlet);
                staticContent.setInitParameter("baseResource", staticContentDir);
                context.addServlet(staticContent, "/");
            }
        }

        protected static void addServlet(ServletContextHandler context, String pathspec, HttpServlet httpServlet) {
            ServletHolder sh = new ServletHolder(httpServlet);
            context.addServlet(sh, pathspec);
        }

        protected void addFilter(ServletContextHandler context, String pathspec, Filter filter) {
            FilterHolder h = new FilterHolder(filter);
            context.addFilter(h, pathspec, null);
        }
    }

    // Jetty Server

    public static Server jettyServer(String jettyConfig) {
        try {
            Server server = new Server();
            Resource configXml = JettyLib.newResource(jettyConfig);
            XmlConfiguration configuration = new XmlConfiguration(configXml);
            configuration.configure(server);
            return server;
        } catch (Exception ex) {
            serverLog.error("JettyServer: Failed to configure server: " + ex.getMessage(), ex);
            throw new JettyConfigException("Failed to configure a server using configuration file '" + jettyConfig + "'");
        }
    }

    public static Server jettyServer(int minThreads, int maxThreads) {
        ThreadPool threadPool = null;
        // Jetty 9.4 and 12.0 : the Jetty default is max=200, min=8
        if ( minThreads < 0 )
            minThreads = 2;
        if ( maxThreads < 0 )
            maxThreads = 20;
        maxThreads = Math.max(minThreads, maxThreads);
        // Args reversed: Jetty uses (max,min)
        threadPool = new QueuedThreadPool(maxThreads, minThreads);
        Server server = new Server(threadPool);
        return server;
    }

    private static void serverAddConnectors(Server server, int port,  boolean loopback) {
        HttpConfiguration httpConnectionFactory = JettyLib.httpConfiguration();
        HttpConnectionFactory f1 = new HttpConnectionFactory(httpConnectionFactory);
        ServerConnector connector = new ServerConnector(server, f1);
        connector.setPort(port);
        server.addConnector(connector);
        if ( loopback )
            connector.setHost("localhost");
    }
}

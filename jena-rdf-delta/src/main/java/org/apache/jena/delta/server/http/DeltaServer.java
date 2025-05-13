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

package org.apache.jena.delta.server.http;

import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.metrics.HealthCheck;
import org.apache.jena.delta.metrics.MetricsEndpoint;
import org.apache.jena.delta.server.PatchLogServer;
import org.apache.jena.delta.server.local.FileStore;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * HTTP server for exposing a patch log server.
 */
public class DeltaServer {
    private static final Logger LOG = LoggerFactory.getLogger(DeltaServer.class);
    
    private final Server server;
    private final PatchLogServer patchLogServer;
    private final HealthCheck healthCheck;
    private final PrometheusMeterRegistry prometheusRegistry;
    
    /**
     * Create a new DeltaServer.
     * @param port The port to listen on
     * @param storePath The path to the store directory
     */
    public DeltaServer(int port, String storePath) {
        this.patchLogServer = new FileStore(storePath);
        this.healthCheck = null;
        this.prometheusRegistry = null;
        this.server = createServer(port);
    }
    
    /**
     * Create a new DeltaServer.
     * @param port The port to listen on
     * @param patchLogServer The patch log server to expose
     */
    public DeltaServer(int port, PatchLogServer patchLogServer) {
        this.patchLogServer = patchLogServer;
        this.healthCheck = null;
        this.prometheusRegistry = null;
        this.server = createServer(port);
    }
    
    /**
     * Create a new DeltaServer with monitoring.
     * @param port The port to listen on
     * @param patchLogServer The patch log server to expose
     * @param healthCheck The health check system (may be null)
     * @param prometheusRegistry The Prometheus registry (may be null)
     */
    public DeltaServer(int port, PatchLogServer patchLogServer, 
                        HealthCheck healthCheck, PrometheusMeterRegistry prometheusRegistry) {
        this.patchLogServer = patchLogServer;
        this.healthCheck = healthCheck;
        this.prometheusRegistry = prometheusRegistry;
        this.server = createServer(port);
    }
    
    /**
     * Create the Jetty server.
     */
    private Server createServer(int port) {
        Server server = new Server(port);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        
        // Add the main delta server servlet
        ServletHolder deltaHolder = new ServletHolder(new DeltaServlet(patchLogServer));
        context.addServlet(deltaHolder, "/");
        
        // Add metrics endpoint if enabled
        if (healthCheck != null || prometheusRegistry != null) {
            // Add metrics API endpoints
            ServletHolder metricsHolder = new ServletHolder(
                new MetricsEndpoint(prometheusRegistry, healthCheck));
            context.addServlet(metricsHolder, "/monitoring/api/*");
            
            // Add monitoring dashboard
            ServletHolder dashboardHolder = new ServletHolder(
                new org.apache.jena.delta.metrics.MonitoringServlet());
            context.addServlet(dashboardHolder, "/monitoring");
            context.addServlet(dashboardHolder, "/monitoring/dashboard");
            
            LOG.info("Monitoring endpoints enabled at /monitoring/");
        }
        
        server.setHandler(context);
        
        return server;
    }
    
    /**
     * Start the server.
     */
    public void start() {
        try {
            server.start();
            LOG.info("DeltaServer started on port {}", server.getURI().getPort());
        } catch (Exception e) {
            throw new DeltaException("Failed to start DeltaServer", e);
        }
    }
    
    /**
     * Stop the server.
     */
    public void stop() {
        try {
            server.stop();
            LOG.info("DeltaServer stopped");
        } catch (Exception e) {
            LOG.error("Error stopping DeltaServer", e);
        }
    }
    
    /**
     * Get the patch log server.
     */
    public PatchLogServer getPatchLogServer() {
        return patchLogServer;
    }
    
    /**
     * Get the server URI.
     */
    public String getURI() {
        return server.getURI().toString();
    }
    
    /**
     * Main method for running the server.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: DeltaServer <port> <storePath>");
            System.exit(1);
        }
        
        int port = Integer.parseInt(args[0]);
        String storePath = args[1];
        
        DeltaServer server = new DeltaServer(port, storePath);
        server.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop()));
        
        try {
            server.server.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
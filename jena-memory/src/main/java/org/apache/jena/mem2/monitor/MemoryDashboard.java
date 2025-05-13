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

package org.apache.jena.mem2.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.mem2.monitor.MemoryMonitor.MemoryAlert;
import org.apache.jena.mem2.monitor.MemoryMonitor.MemoryAlertListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A web-based dashboard for monitoring memory usage.
 * <p>
 * This class provides a web interface for viewing memory statistics,
 * including:
 * <ul>
 * <li>Real-time memory usage charts</li>
 * <li>GC behavior visualization</li>
 * <li>Component memory usage breakdown</li>
 * <li>Memory leak detection</li>
 * <li>Historical trends</li>
 * </ul>
 */
public class MemoryDashboard implements MemoryAlertListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(MemoryDashboard.class);
    
    // Default port for the dashboard
    private static final int DEFAULT_PORT = 8088;
    
    // Memory monitor
    private final MemoryMonitor monitor;
    
    // HTTP server
    private HttpServer server;
    
    // Alert history
    private final CopyOnWriteArrayList<MemoryAlert> alerts = new CopyOnWriteArrayList<>();
    
    // Maximum number of alerts to keep
    private static final int MAX_ALERTS = 100;
    
    // State
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final int port;
    
    /**
     * Create a new memory dashboard with the default port.
     * 
     * @param monitor The memory monitor to use
     */
    public MemoryDashboard(MemoryMonitor monitor) {
        this(monitor, DEFAULT_PORT);
    }
    
    /**
     * Create a new memory dashboard with the specified port.
     * 
     * @param monitor The memory monitor to use
     * @param port The port to listen on
     */
    public MemoryDashboard(MemoryMonitor monitor, int port) {
        this.monitor = monitor;
        this.port = port;
        
        // Register as an alert listener
        monitor.addAlertListener(this);
        
        LOG.debug("Created MemoryDashboard with port: {}", port);
    }
    
    /**
     * Start the dashboard.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            try {
                // Create the HTTP server
                server = HttpServer.create(new InetSocketAddress(port), 0);
                
                // Add handlers
                server.createContext("/", new DashboardHandler());
                server.createContext("/api/summary", new SummaryHandler());
                server.createContext("/api/alerts", new AlertsHandler());
                server.createContext("/api/gc", new GcHandler());
                
                // Set executor
                server.setExecutor(Executors.newCachedThreadPool(r -> {
                    Thread t = new Thread(r, "MemoryDashboard-HTTP");
                    t.setDaemon(true);
                    return t;
                }));
                
                // Start the server
                server.start();
                
                LOG.info("Memory dashboard started on http://localhost:{}/", port);
            } catch (IOException e) {
                LOG.error("Error starting memory dashboard", e);
                running.set(false);
            }
        }
    }
    
    /**
     * Stop the dashboard.
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (server != null) {
                server.stop(0);
                LOG.info("Memory dashboard stopped");
            }
        }
    }
    
    /**
     * Check if the dashboard is running.
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * Get the dashboard port.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Get the dashboard URL.
     */
    public String getUrl() {
        return "http://localhost:" + port + "/";
    }
    
    @Override
    public void onMemoryAlert(MemoryAlert alert) {
        // Add to the alert history
        alerts.add(alert);
        
        // Limit the history length
        while (alerts.size() > MAX_ALERTS) {
            alerts.remove(0);
        }
    }
    
    /**
     * Handler for the main dashboard UI.
     */
    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.equals("/") || path.equals("/index.html")) {
                // Serve the main dashboard HTML
                serveResource(exchange, "/org/apache/jena/mem2/monitor/webapp/index.html", "text/html");
            } else if (path.equals("/style.css")) {
                // Serve the CSS
                serveResource(exchange, "/org/apache/jena/mem2/monitor/webapp/style.css", "text/css");
            } else if (path.equals("/script.js")) {
                // Serve the JavaScript
                serveResource(exchange, "/org/apache/jena/mem2/monitor/webapp/script.js", "application/javascript");
            } else {
                // Not found
                exchange.sendResponseHeaders(404, 0);
                exchange.getResponseBody().close();
            }
        }
    }
    
    /**
     * Handler for the memory summary API.
     */
    private class SummaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Get the memory summary
            JsonObject summary = monitor.getMemorySummary();
            
            // Send the response
            byte[] response = summary.toString().getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }
    
    /**
     * Handler for the alerts API.
     */
    private class AlertsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Build a JSON array of alerts
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            
            boolean first = true;
            for (MemoryAlert alert : alerts) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(alert.toJson().toString());
                first = false;
            }
            
            sb.append("]");
            
            // Send the response
            byte[] response = sb.toString().getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }
    
    /**
     * Handler for the GC API.
     */
    private class GcHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Force garbage collection
            boolean triggered = monitor.forceGarbageCollection();
            
            // Build response
            JsonObject response = JsonObject.create();
            response.put("triggered", triggered);
            response.put("message", triggered 
                ? "Garbage collection triggered" 
                : "Garbage collection not triggered (conditions not met)");
            
            // Send the response
            byte[] responseBytes = response.toString().getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
    
    /**
     * Serve a resource from the classpath.
     */
    private void serveResource(HttpExchange exchange, String resourcePath, String contentType) 
            throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                exchange.sendResponseHeaders(404, 0);
                exchange.getResponseBody().close();
                return;
            }
            
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, 0);
            
            try (OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
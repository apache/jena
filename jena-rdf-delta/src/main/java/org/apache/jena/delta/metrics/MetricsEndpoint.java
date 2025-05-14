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

package org.apache.jena.delta.metrics;

import java.io.IOException;
import java.time.Instant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.logging.FmtLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Servlet that provides monitoring and metrics endpoints.
 * <p>
 * This servlet exposes the following endpoints:
 * <ul>
 * <li>/metrics - Prometheus metrics endpoint</li>
 * <li>/health - Health check endpoint</li>
 * <li>/status - System status information</li>
 * </ul>
 */
public class MetricsEndpoint extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MetricsEndpoint.class);
    
    private final PrometheusMeterRegistry prometheusRegistry;
    private final HealthCheck healthCheck;
    
    /**
     * Create a new metrics endpoint.
     * 
     * @param prometheusRegistry The Prometheus registry (may be null if Prometheus is disabled)
     * @param healthCheck The health check system (may be null if health checks are disabled)
     */
    public MetricsEndpoint(PrometheusMeterRegistry prometheusRegistry, HealthCheck healthCheck) {
        this.prometheusRegistry = prometheusRegistry;
        this.healthCheck = healthCheck;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String path = req.getPathInfo();
        
        if (path == null || path.equals("/")) {
            handleRoot(req, resp);
        } else if (path.equals("/metrics")) {
            handleMetrics(req, resp);
        } else if (path.equals("/health")) {
            handleHealth(req, resp);
        } else if (path.equals("/status")) {
            handleStatus(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }
    
    /**
     * Handle the root endpoint.
     */
    private void handleRoot(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        JsonBuilder builder = JsonBuilder.create();
        builder.add("name", "RDF Delta Monitoring");
        builder.add("description", "RDF Delta server monitoring endpoints");
        builder.add("version", getVersion());
        builder.add("endpoints", JsonBuilder.create()
            .add("metrics", "Prometheus metrics endpoint")
            .add("health", "Health check endpoint")
            .add("status", "System status information")
            .build());
        
        sendJsonResponse(resp, builder.build());
    }
    
    /**
     * Handle the metrics endpoint.
     */
    private void handleMetrics(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        if (prometheusRegistry == null) {
            sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Prometheus metrics not enabled");
            return;
        }
        
        resp.setContentType("text/plain; version=0.0.4");
        resp.getWriter().write(prometheusRegistry.scrape());
    }
    
    /**
     * Handle the health endpoint.
     */
    private void handleHealth(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        if (healthCheck == null) {
            sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Health checks not enabled");
            return;
        }
        
        // Run health checks if requested
        String runParam = req.getParameter("run");
        if (runParam != null && runParam.equals("true")) {
            healthCheck.runAllChecks();
        }
        
        // Get health JSON
        JsonObject healthJson = healthCheck.getHealthJson();
        
        // Set the HTTP status code based on the overall health status
        HealthCheck.Status overallStatus = healthCheck.getOverallStatus();
        if (overallStatus == HealthCheck.Status.UNHEALTHY) {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } else if (overallStatus == HealthCheck.Status.DEGRADED) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        
        sendJsonResponse(resp, healthJson);
    }
    
    /**
     * Handle the status endpoint.
     */
    private void handleStatus(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        JsonBuilder builder = JsonBuilder.create();
        builder.add("name", "RDF Delta Server");
        builder.add("version", getVersion());
        builder.add("timestamp", Instant.now().toString());
        
        // Add JVM info
        builder.add("jvm", JsonBuilder.create()
            .add("version", System.getProperty("java.version"))
            .add("vendor", System.getProperty("java.vendor"))
            .add("uptime", ManagementUtils.getUptime())
            .add("memory", JsonBuilder.create()
                .add("heap", JsonBuilder.create()
                    .add("used", ManagementUtils.getHeapMemoryUsed())
                    .add("max", ManagementUtils.getHeapMemoryMax())
                    .add("committed", ManagementUtils.getHeapMemoryCommitted())
                    .build())
                .add("non_heap", JsonBuilder.create()
                    .add("used", ManagementUtils.getNonHeapMemoryUsed())
                    .add("committed", ManagementUtils.getNonHeapMemoryCommitted())
                    .build())
                .build())
            .build());
        
        // Add system info
        builder.add("system", JsonBuilder.create()
            .add("os", System.getProperty("os.name"))
            .add("os_version", System.getProperty("os.version"))
            .add("os_arch", System.getProperty("os.arch"))
            .add("processors", ManagementUtils.getAvailableProcessors())
            .add("load_average", ManagementUtils.getSystemLoadAverage())
            .build());
        
        // Add health status if available
        if (healthCheck != null) {
            builder.add("health", healthCheck.getOverallStatus().getLabel());
        }
        
        sendJsonResponse(resp, builder.build());
    }
    
    /**
     * Helper method to send a JSON response.
     */
    private void sendJsonResponse(HttpServletResponse resp, JsonObject json) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }
    
    /**
     * Helper method to send a JSON error response.
     */
    private void sendJsonError(HttpServletResponse resp, int status, String message) 
            throws IOException {
        resp.setStatus(status);
        sendJsonResponse(resp, JsonBuilder.create()
            .add("error", true)
            .add("status", status)
            .add("message", message)
            .build());
    }
    
    /**
     * Get the version of the RDF Delta server.
     */
    private String getVersion() {
        String version = MetricsEndpoint.class.getPackage().getImplementationVersion();
        return version != null ? version : "development";
    }
    
    /**
     * Utility class for accessing management information.
     */
    private static class ManagementUtils {
        /**
         * Get the system uptime in milliseconds.
         */
        public static long getUptime() {
            return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        }
        
        /**
         * Get the number of available processors.
         */
        public static int getAvailableProcessors() {
            return java.lang.management.ManagementFactory.getOperatingSystemMXBean()
                .getAvailableProcessors();
        }
        
        /**
         * Get the system load average.
         */
        public static double getSystemLoadAverage() {
            return java.lang.management.ManagementFactory.getOperatingSystemMXBean()
                .getSystemLoadAverage();
        }
        
        /**
         * Get the heap memory used in bytes.
         */
        public static long getHeapMemoryUsed() {
            return java.lang.management.ManagementFactory.getMemoryMXBean()
                .getHeapMemoryUsage().getUsed();
        }
        
        /**
         * Get the heap memory max in bytes.
         */
        public static long getHeapMemoryMax() {
            return java.lang.management.ManagementFactory.getMemoryMXBean()
                .getHeapMemoryUsage().getMax();
        }
        
        /**
         * Get the heap memory committed in bytes.
         */
        public static long getHeapMemoryCommitted() {
            return java.lang.management.ManagementFactory.getMemoryMXBean()
                .getHeapMemoryUsage().getCommitted();
        }
        
        /**
         * Get the non-heap memory used in bytes.
         */
        public static long getNonHeapMemoryUsed() {
            return java.lang.management.ManagementFactory.getMemoryMXBean()
                .getNonHeapMemoryUsage().getUsed();
        }
        
        /**
         * Get the non-heap memory committed in bytes.
         */
        public static long getNonHeapMemoryCommitted() {
            return java.lang.management.ManagementFactory.getMemoryMXBean()
                .getNonHeapMemoryUsage().getCommitted();
        }
    }
}
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

package org.apache.jena.delta.server;

import java.time.Duration;

import org.apache.jena.delta.conflict.ConflictDetector;
import org.apache.jena.delta.conflict.ConflictResolver;
import org.apache.jena.delta.conflict.ConflictType;
import org.apache.jena.delta.conflict.ResolutionStrategy;
import org.apache.jena.delta.metrics.AlertSystem;
import org.apache.jena.delta.metrics.DeltaMetrics;
import org.apache.jena.delta.metrics.HealthCheck;
import org.apache.jena.delta.metrics.PatchServerMetrics;
import org.apache.jena.delta.server.cluster.DistributedPatchLogServer;
import org.apache.jena.delta.server.conflict.ConflictAwarePatchLogServer;
import org.apache.jena.delta.server.http.DeltaServer;
import org.apache.jena.delta.server.local.FileStore;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Builder for creating a patch log server.
 */
public class ServerBuilder {
    // Server configuration
    private int port = 1066;
    private String storePath = null;
    private String zookeeperConnect = null;
    private boolean distributed = false;
    
    // Metrics configuration
    private boolean metricsEnabled = true;
    private boolean jmxEnabled = false;
    private boolean prometheusEnabled = false;
    
    // Health and monitoring configuration
    private boolean healthCheckEnabled = true;
    private boolean alertingEnabled = false;
    private Duration healthCheckInterval = Duration.ofMinutes(1);
    private Duration alertRateLimitInterval = Duration.ofMinutes(5);
    private String webhookUrl = null;
    
    // Conflict resolution configuration
    private boolean conflictDetectionEnabled = false;
    private ResolutionStrategy defaultResolutionStrategy = ResolutionStrategy.LAST_WRITE_WINS;
    private long conflictCacheExpiryMs = 60000;
    
    private final ResolutionStrategy[] conflictStrategies = new ResolutionStrategy[ConflictType.values().length];
    
    /**
     * Create a new ServerBuilder.
     */
    public ServerBuilder() {
        // Initialize default conflict resolution strategies
        for (int i = 0; i < conflictStrategies.length; i++) {
            conflictStrategies[i] = defaultResolutionStrategy;
        }
    }
    
    /**
     * Set the port for the server.
     */
    public ServerBuilder port(int port) {
        this.port = port;
        return this;
    }
    
    /**
     * Set the storage path for the server.
     */
    public ServerBuilder storePath(String storePath) {
        this.storePath = storePath;
        return this;
    }
    
    /**
     * Set the ZooKeeper connection string for distributed mode.
     */
    public ServerBuilder zookeeperConnect(String zookeeperConnect) {
        this.zookeeperConnect = zookeeperConnect;
        return this;
    }
    
    /**
     * Enable or disable distributed mode.
     */
    public ServerBuilder distributed(boolean distributed) {
        this.distributed = distributed;
        return this;
    }
    
    /**
     * Enable or disable metrics.
     */
    public ServerBuilder metricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
        return this;
    }
    
    /**
     * Enable or disable JMX metrics.
     */
    public ServerBuilder jmxEnabled(boolean jmxEnabled) {
        this.jmxEnabled = jmxEnabled;
        return this;
    }
    
    /**
     * Enable or disable Prometheus metrics.
     */
    public ServerBuilder prometheusEnabled(boolean prometheusEnabled) {
        this.prometheusEnabled = prometheusEnabled;
        return this;
    }
    
    /**
     * Enable or disable conflict detection and resolution.
     */
    public ServerBuilder conflictDetectionEnabled(boolean conflictDetectionEnabled) {
        this.conflictDetectionEnabled = conflictDetectionEnabled;
        return this;
    }
    
    /**
     * Enable or disable health checks.
     */
    public ServerBuilder healthCheckEnabled(boolean healthCheckEnabled) {
        this.healthCheckEnabled = healthCheckEnabled;
        return this;
    }
    
    /**
     * Set the health check interval.
     */
    public ServerBuilder healthCheckInterval(Duration healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
        return this;
    }
    
    /**
     * Enable or disable alerting.
     */
    public ServerBuilder alertingEnabled(boolean alertingEnabled) {
        this.alertingEnabled = alertingEnabled;
        return this;
    }
    
    /**
     * Set the alert rate limit interval.
     */
    public ServerBuilder alertRateLimitInterval(Duration alertRateLimitInterval) {
        this.alertRateLimitInterval = alertRateLimitInterval;
        return this;
    }
    
    /**
     * Set the webhook URL for alerts.
     */
    public ServerBuilder webhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        return this;
    }
    
    /**
     * Set the default conflict resolution strategy.
     */
    public ServerBuilder defaultResolutionStrategy(ResolutionStrategy defaultResolutionStrategy) {
        this.defaultResolutionStrategy = defaultResolutionStrategy;
        
        // Update all strategies that haven't been explicitly set
        for (int i = 0; i < conflictStrategies.length; i++) {
            if (conflictStrategies[i] == null) {
                conflictStrategies[i] = defaultResolutionStrategy;
            }
        }
        
        return this;
    }
    
    /**
     * Set the conflict resolution strategy for a specific conflict type.
     */
    public ServerBuilder resolutionStrategy(ConflictType type, ResolutionStrategy strategy) {
        conflictStrategies[type.ordinal()] = strategy;
        return this;
    }
    
    /**
     * Set the expiry time for the conflict detection cache.
     */
    public ServerBuilder conflictCacheExpiryMs(long conflictCacheExpiryMs) {
        this.conflictCacheExpiryMs = conflictCacheExpiryMs;
        return this;
    }
    
    /**
     * Build the server.
     */
    public DeltaServer build() {
        // Validate configuration
        if (storePath == null) {
            throw new IllegalArgumentException("Storage path must be set");
        }
        
        // Create the metrics registry
        // Initialize central metrics
        DeltaMetrics.initializeMetrics(prometheusEnabled, jmxEnabled);
        MeterRegistry registry = DeltaMetrics.getRegistry();
        
        // Create health check system if enabled
        HealthCheck healthCheck = null;
        AlertSystem alertSystem = null;
        
        if (healthCheckEnabled) {
            healthCheck = new HealthCheck();
            
            // Create alerting system if enabled
            if (alertingEnabled) {
                alertSystem = new AlertSystem(alertRateLimitInterval);
                
                // Add webhook channel if URL is provided
                if (webhookUrl != null && !webhookUrl.isEmpty()) {
                    alertSystem.addWebhookChannel("default", webhookUrl);
                }
                
                // Register alert system as a health check listener
                healthCheck.addListener(alertSystem);
            }
        }
        
        // Create the base server
        PatchLogServer server;
        if (distributed && zookeeperConnect != null) {
            String serverUrl = "http://localhost:" + port + "/";
            server = new DistributedPatchLogServer(zookeeperConnect, 30000, serverUrl, storePath, registry);
        } else {
            server = new FileStore(storePath);
        }
        
        // Add server metrics if enabled
        if (metricsEnabled && registry != null) {
            PatchServerMetrics serverMetrics = new PatchServerMetrics(registry, server, "patch-server");
            
            // Add health checks for the server
            if (healthCheck != null) {
                // Add storage health check
                healthCheck.addCheck("storage", () -> {
                    try {
                        if (server.listDatasets().isEmpty()) {
                            return new HealthCheck.Result("storage", HealthCheck.Status.HEALTHY, 
                                "Storage is healthy (no datasets yet)");
                        } else {
                            return new HealthCheck.Result("storage", HealthCheck.Status.HEALTHY, 
                                "Storage is healthy with " + server.listDatasets().size() + " datasets");
                        }
                    } catch (Exception e) {
                        return new HealthCheck.Result("storage", HealthCheck.Status.UNHEALTHY, 
                            "Storage access error: " + e.getMessage());
                    }
                }, healthCheckInterval);
            }
        }
        
        // Add conflict detection if enabled
        if (conflictDetectionEnabled) {
            ConflictDetector detector = new ConflictDetector(true, true, true, true, true, 5000, registry);
            ConflictResolver resolver = new ConflictResolver(detector, defaultResolutionStrategy, registry);
            
            // Set specific resolution strategies
            for (ConflictType type : ConflictType.values()) {
                ResolutionStrategy strategy = conflictStrategies[type.ordinal()];
                if (strategy != null) {
                    resolver.setStrategy(type, strategy);
                }
            }
            
            server = new ConflictAwarePatchLogServer(server, detector, resolver, registry, conflictCacheExpiryMs);
            
            // Add conflict health check
            if (healthCheck != null) {
                healthCheck.addCheck("conflict-resolution", () -> {
                    return new HealthCheck.Result("conflict-resolution", HealthCheck.Status.HEALTHY, 
                        "Conflict resolution system is active");
                }, healthCheckInterval);
            }
        }
        
        // Find Prometheus registry if enabled
        PrometheusMeterRegistry prometheusRegistry = null;
        if (prometheusEnabled && registry instanceof CompositeMeterRegistry) {
            CompositeMeterRegistry composite = (CompositeMeterRegistry)registry;
            for (MeterRegistry reg : composite.getRegistries()) {
                if (reg instanceof PrometheusMeterRegistry) {
                    prometheusRegistry = (PrometheusMeterRegistry)reg;
                    break;
                }
            }
        }
        
        // Create the HTTP server with monitoring
        DeltaServer deltaServer = new DeltaServer(port, server, healthCheck, prometheusRegistry);
        
        // Add system health checks
        if (healthCheck != null) {
            // Memory usage health check
            healthCheck.addCheck("system.memory", () -> {
                Runtime runtime = Runtime.getRuntime();
                long maxMemory = runtime.maxMemory() / (1024 * 1024);
                long totalMemory = runtime.totalMemory() / (1024 * 1024);
                long freeMemory = runtime.freeMemory() / (1024 * 1024);
                long usedMemory = totalMemory - freeMemory;
                double memoryUsage = (double) usedMemory / maxMemory;
                
                if (memoryUsage > 0.9) {
                    return new HealthCheck.Result("system.memory", HealthCheck.Status.DEGRADED, 
                        String.format("High memory usage: %.1f%% (%d/%d MB)", 
                            memoryUsage * 100, usedMemory, maxMemory));
                } else {
                    return new HealthCheck.Result("system.memory", HealthCheck.Status.HEALTHY, 
                        String.format("Memory usage: %.1f%% (%d/%d MB)", 
                            memoryUsage * 100, usedMemory, maxMemory));
                }
            }, healthCheckInterval);
            
            // Disk space health check
            healthCheck.addCheck("system.disk", () -> {
                java.io.File file = new java.io.File(storePath);
                long totalSpace = file.getTotalSpace() / (1024 * 1024);
                long usableSpace = file.getUsableSpace() / (1024 * 1024);
                double usagePercent = 100.0 - ((double) usableSpace / totalSpace * 100.0);
                
                if (usagePercent > 90) {
                    return new HealthCheck.Result("system.disk", HealthCheck.Status.DEGRADED, 
                        String.format("Low disk space: %.1f%% used (%d/%d MB free)", 
                            usagePercent, usableSpace, totalSpace));
                } else {
                    return new HealthCheck.Result("system.disk", HealthCheck.Status.HEALTHY, 
                        String.format("Disk space: %.1f%% used (%d/%d MB free)", 
                            usagePercent, usableSpace, totalSpace));
                }
            }, healthCheckInterval);
        }
        
        return deltaServer;
    }
    
    /**
     * Create a meter registry based on the configuration.
     * 
     * @deprecated Use DeltaMetrics.initializeMetrics instead.
     */
    @Deprecated
    private MeterRegistry createRegistry() {
        if (!metricsEnabled) {
            return null;
        }
        
        // Use the central metrics system
        return DeltaMetrics.getRegistry();
    }
}
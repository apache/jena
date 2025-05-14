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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.logging.FmtLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Health check system for RDF Delta components.
 * <p>
 * This class provides a framework for running periodic health checks and 
 * notifying listeners of status changes. It can be used to monitor:
 * <ul>
 * <li>Server availability</li>
 * <li>Dataset health</li>
 * <li>Replication status</li>
 * <li>Resource usage</li>
 * </ul>
 * <p>
 * Health checks can trigger alerts when they fail.
 */
public class HealthCheck {
    private static final Logger LOG = LoggerFactory.getLogger(HealthCheck.class);
    
    /** Health status enumeration */
    public enum Status {
        /** Component is healthy and fully operational */
        HEALTHY("healthy"),
        
        /** Component is operational but has degraded performance or issues */
        DEGRADED("degraded"),
        
        /** Component is not operational */
        UNHEALTHY("unhealthy"),
        
        /** Component's status is unknown (e.g., health check couldn't run) */
        UNKNOWN("unknown");
        
        private final String label;
        
        Status(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    /** A health check result */
    public static class Result {
        private final String component;
        private final Status status;
        private final String message;
        private final Instant timestamp;
        
        public Result(String component, Status status, String message) {
            this.component = component;
            this.status = status;
            this.message = message;
            this.timestamp = Instant.now();
        }
        
        public String getComponent() { return component; }
        public Status getStatus() { return status; }
        public String getMessage() { return message; }
        public Instant getTimestamp() { return timestamp; }
        
        public JsonObject toJson() {
            return JsonBuilder.create()
                .add("component", component)
                .add("status", status.getLabel())
                .add("message", message)
                .add("timestamp", timestamp.toString())
                .build();
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s (%s)", 
                timestamp, component, status.getLabel(), message);
        }
    }
    
    /** A health check */
    public static class Check {
        private final String name;
        private final Supplier<Result> checker;
        private final long intervalMillis;
        private volatile Result lastResult;
        private Instant lastRun;
        
        public Check(String name, Supplier<Result> checker, Duration interval) {
            this.name = name;
            this.checker = checker;
            this.intervalMillis = interval.toMillis();
            this.lastResult = new Result(name, Status.UNKNOWN, "Not yet run");
        }
        
        public String getName() { return name; }
        public Result getLastResult() { return lastResult; }
        public Instant getLastRun() { return lastRun; }
        public long getIntervalMillis() { return intervalMillis; }
        
        public Result run() {
            try {
                this.lastResult = checker.get();
                this.lastRun = Instant.now();
                return this.lastResult;
            } catch (Exception e) {
                this.lastResult = new Result(name, Status.UNKNOWN, 
                    "Check failed to execute: " + e.getMessage());
                this.lastRun = Instant.now();
                LOG.warn("Health check '{}' failed to execute", name, e);
                return this.lastResult;
            }
        }
    }
    
    /** A listener for health status changes */
    public interface StatusListener {
        /**
         * Called when a health check result changes.
         * 
         * @param oldResult The previous result (may be null)
         * @param newResult The new result
         */
        void onStatusChange(Result oldResult, Result newResult);
    }
    
    // Health checks
    private final List<Check> checks = new CopyOnWriteArrayList<>();
    private final List<StatusListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler;
    
    /**
     * Create a new health check system.
     */
    public HealthCheck() {
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "HealthCheckScheduler");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Add a health check to be run periodically.
     * 
     * @param name The check name
     * @param checker The check implementation
     * @param interval How often to run the check
     * @return This HealthCheck for chaining
     */
    public HealthCheck addCheck(String name, Supplier<Result> checker, Duration interval) {
        Check check = new Check(name, checker, interval);
        checks.add(check);
        
        // Schedule the check
        scheduler.scheduleAtFixedRate(
            () -> runCheck(check),
            0, // Run immediately
            check.getIntervalMillis(),
            TimeUnit.MILLISECONDS);
            
        return this;
    }
    
    /**
     * Add a listener for health status changes.
     * 
     * @param listener The listener to add
     * @return This HealthCheck for chaining
     */
    public HealthCheck addListener(StatusListener listener) {
        listeners.add(listener);
        return this;
    }
    
    /**
     * Remove a listener.
     * 
     * @param listener The listener to remove
     * @return true if the listener was removed
     */
    public boolean removeListener(StatusListener listener) {
        return listeners.remove(listener);
    }
    
    /**
     * Run all health checks immediately.
     * 
     * @return List of results from all checks
     */
    public List<Result> runAllChecks() {
        List<Result> results = new ArrayList<>();
        for (Check check : checks) {
            results.add(runCheck(check));
        }
        return results;
    }
    
    /**
     * Run a specific health check.
     * 
     * @param check The check to run
     * @return The check result
     */
    private Result runCheck(Check check) {
        Result oldResult = check.getLastResult();
        Result newResult = check.run();
        
        // Notify listeners if status changed
        if (oldResult.getStatus() != newResult.getStatus()) {
            for (StatusListener listener : listeners) {
                try {
                    listener.onStatusChange(oldResult, newResult);
                } catch (Exception e) {
                    LOG.warn("Status listener threw exception", e);
                }
            }
        }
        
        return newResult;
    }
    
    /**
     * Get the latest results from all health checks.
     * 
     * @return List of the latest results
     */
    public List<Result> getLatestResults() {
        List<Result> results = new ArrayList<>();
        for (Check check : checks) {
            results.add(check.getLastResult());
        }
        return Collections.unmodifiableList(results);
    }
    
    /**
     * Get the overall system health status.
     * 
     * @return HEALTHY if all checks are healthy, DEGRADED if any are degraded,
     *         UNHEALTHY if any are unhealthy, UNKNOWN if no checks or all unknown
     */
    public Status getOverallStatus() {
        if (checks.isEmpty()) {
            return Status.UNKNOWN;
        }
        
        boolean hasUnhealthy = false;
        boolean hasDegraded = false;
        boolean hasHealthy = false;
        
        for (Check check : checks) {
            Status status = check.getLastResult().getStatus();
            if (status == Status.UNHEALTHY) {
                hasUnhealthy = true;
            } else if (status == Status.DEGRADED) {
                hasDegraded = true;
            } else if (status == Status.HEALTHY) {
                hasHealthy = true;
            }
        }
        
        if (hasUnhealthy) {
            return Status.UNHEALTHY;
        } else if (hasDegraded) {
            return Status.DEGRADED;
        } else if (hasHealthy) {
            return Status.HEALTHY;
        } else {
            return Status.UNKNOWN;
        }
    }
    
    /**
     * Get a JSON object representing the current health status.
     * 
     * @return A JSON object with health check results
     */
    public JsonObject getHealthJson() {
        JsonBuilder builder = JsonBuilder.create();
        builder.add("status", getOverallStatus().getLabel());
        builder.add("timestamp", Instant.now().toString());
        
        JsonBuilder checksBuilder = JsonBuilder.create();
        for (Check check : checks) {
            Result result = check.getLastResult();
            checksBuilder.add(check.getName(), result.toJson());
        }
        
        builder.add("checks", checksBuilder.build());
        return builder.build();
    }
    
    /**
     * Shutdown the health check system.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
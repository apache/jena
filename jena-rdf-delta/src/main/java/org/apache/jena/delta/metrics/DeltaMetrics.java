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

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.*;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.FmtLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A centralized metrics management system for RDF Delta components.
 * <p>
 * This class provides:
 * <ul>
 * <li>A central registry for monitoring metrics</li>
 * <li>Standard tag definitions for consistent metric labeling</li>
 * <li>Helper methods for creating and registering metrics</li>
 * <li>Support for Prometheus and JMX metric backends</li>
 * </ul>
 * <p>
 * Typical usage:
 * <pre>
 * MeterRegistry registry = DeltaMetrics.createRegistry(enablePrometheus, enableJMX);
 * // Then use the registry to create metrics
 * Counter counter = DeltaMetrics.createCounter(registry, "delta.server.patches", "Number of patches processed");
 * counter.increment();
 * </pre>
 */
public class DeltaMetrics {
    private static final Logger LOG = LoggerFactory.getLogger(DeltaMetrics.class);
    
    // Singleton registry
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static volatile MeterRegistry globalRegistry = null;
    
    // Metric category constants
    public static final String CATEGORY_SERVER = "server";
    public static final String CATEGORY_CLIENT = "client";
    public static final String CATEGORY_DATASET = "dataset";
    public static final String CATEGORY_TRANSACTION = "transaction";
    public static final String CATEGORY_PATCH = "patch";
    public static final String CATEGORY_REPLICATION = "replication";
    public static final String CATEGORY_CONFLICT = "conflict";
    
    // Common tag keys
    public static final String TAG_SERVER = "server";
    public static final String TAG_DATASET = "dataset";
    public static final String TAG_OPERATION = "operation";
    public static final String TAG_STATUS = "status";
    public static final String TAG_COMPONENT = "component";
    
    // Metrics prefix
    public static final String METRICS_PREFIX = "delta";
    
    /**
     * Initialize the global metrics registry with specified backends.
     * 
     * @param enablePrometheus True to enable Prometheus metrics
     * @param enableJMX True to enable JMX metrics
     * @return The initialized global MeterRegistry
     */
    public static synchronized MeterRegistry initializeMetrics(boolean enablePrometheus, boolean enableJMX) {
        if (initialized.get())
            return globalRegistry;
        
        CompositeMeterRegistry registry = new CompositeMeterRegistry();
        
        // Add Prometheus registry if enabled
        if (enablePrometheus) {
            PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
            registry.add(prometheusRegistry);
            LOG.info("Prometheus metrics enabled");
        }
        
        // Add JMX registry if enabled
        if (enableJMX) {
            JmxMeterRegistry jmxRegistry = new JmxMeterRegistry(JmxConfig.DEFAULT, Clock.SYSTEM);
            registry.add(jmxRegistry);
            LOG.info("JMX metrics enabled");
        }
        
        // Register JVM metrics
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        
        // Register OS metrics
        new ProcessorMetrics().bindTo(registry);
        new FileDescriptorMetrics().bindTo(registry);
        
        globalRegistry = registry;
        initialized.set(true);
        
        LOG.info("Delta metrics system initialized");
        return registry;
    }
    
    /**
     * Get the global metrics registry, initializing it if necessary.
     * 
     * @return The global MeterRegistry
     */
    public static MeterRegistry getRegistry() {
        if (!initialized.get()) {
            // Default to both Prometheus and JMX if not explicitly initialized
            return initializeMetrics(true, true);
        }
        return globalRegistry;
    }
    
    /**
     * Create a Counter with specified name and description.
     * 
     * @param registry The meter registry
     * @param name The metric name (will be prefixed with "delta.")
     * @param description The metric description
     * @param tags Optional tags for the counter
     * @return The created Counter
     */
    public static Counter createCounter(MeterRegistry registry, String name, String description, Tag... tags) {
        return Counter.builder(prefixName(name))
               .description(description)
               .tags(tags)
               .register(registry);
    }
    
    /**
     * Create a Timer with specified name and description.
     * 
     * @param registry The meter registry
     * @param name The metric name (will be prefixed with "delta.")
     * @param description The metric description
     * @param tags Optional tags for the timer
     * @return The created Timer
     */
    public static Timer createTimer(MeterRegistry registry, String name, String description, Tag... tags) {
        return Timer.builder(prefixName(name))
               .description(description)
               .tags(tags)
               .register(registry);
    }
    
    /**
     * Create a Gauge with specified name, description, and object to measure.
     * 
     * @param registry The meter registry
     * @param name The metric name (will be prefixed with "delta.")
     * @param description The metric description
     * @param obj The object to measure
     * @param valueFunction Function to extract a numeric value from the object
     * @param tags Optional tags for the gauge
     * @param <T> The type of object being measured
     */
    public static <T> void createGauge(MeterRegistry registry, String name, String description, 
                                       T obj, ToDoubleFunction<T> valueFunction, Tag... tags) {
        Gauge.builder(prefixName(name), obj, valueFunction)
             .description(description)
             .tags(tags)
             .register(registry);
    }
    
    /**
     * Create a DistributionSummary with specified name and description.
     * 
     * @param registry The meter registry
     * @param name The metric name (will be prefixed with "delta.")
     * @param description The metric description
     * @param tags Optional tags for the distribution summary
     * @return The created DistributionSummary
     */
    public static DistributionSummary createDistributionSummary(MeterRegistry registry, String name, 
                                                               String description, Tag... tags) {
        return DistributionSummary.builder(prefixName(name))
                .description(description)
                .tags(tags)
                .register(registry);
    }
    
    /**
     * Create tag with the given key and value.
     * 
     * @param key The tag key
     * @param value The tag value
     * @return A new Tag
     */
    public static Tag tag(String key, String value) {
        return Tag.of(key, value);
    }
    
    /**
     * Add the metrics prefix to a metric name if it doesn't already have it.
     */
    private static String prefixName(String name) {
        if (name.startsWith(METRICS_PREFIX + "."))
            return name;
        return METRICS_PREFIX + "." + name;
    }
    
    /**
     * Get the scrape data for Prometheus metrics.
     * 
     * @return The Prometheus scrape data as a string, or null if Prometheus is not enabled
     */
    public static String getPrometheusData() {
        if (!initialized.get() || !(globalRegistry instanceof CompositeMeterRegistry))
            return null;
            
        CompositeMeterRegistry composite = (CompositeMeterRegistry)globalRegistry;
        for (MeterRegistry registry : composite.getRegistries()) {
            if (registry instanceof PrometheusMeterRegistry) {
                return ((PrometheusMeterRegistry)registry).scrape();
            }
        }
        return null;
    }
}
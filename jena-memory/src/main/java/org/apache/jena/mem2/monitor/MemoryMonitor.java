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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.mem2.MemoryStats;
import org.apache.jena.mem2.gc.GarbageCollectionOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors memory usage in Apache Jena applications.
 * <p>
 * This class provides:
 * <ul>
 * <li>Detailed memory usage tracking</li>
 * <li>GC behavior monitoring</li>
 * <li>Memory leak detection</li>
 * <li>Performance monitoring related to memory usage</li>
 * <li>Historical data for trend analysis</li>
 * </ul>
 */
public class MemoryMonitor {
    
    private static final Logger LOG = LoggerFactory.getLogger(MemoryMonitor.class);
    
    // Default monitoring interval
    private static final long DEFAULT_INTERVAL_MS = 5000; // 5 seconds
    
    // Maximum history length
    private static final int MAX_HISTORY_LENGTH = 120; // 10 minutes with 5s intervals
    
    // Memory usage samples
    private final List<MemorySample> samples = new CopyOnWriteArrayList<>();
    
    // Registered components
    private final Map<String, MonitoredComponent> components = new ConcurrentHashMap<>();
    
    // Memory beans
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    // GC optimizer
    private final GarbageCollectionOptimizer gcOptimizer;
    
    // Listeners
    private final List<MemoryAlertListener> alertListeners = new CopyOnWriteArrayList<>();
    
    // Executor for monitoring thread
    private final ScheduledExecutorService scheduler;
    
    // Monitoring interval
    private long intervalMs;
    
    // State
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    /**
     * Memory usage sample at a point in time.
     */
    public static class MemorySample {
        private final Instant timestamp;
        private final long heapUsed;
        private final long heapMax;
        private final long nonHeapUsed;
        private final long nonHeapMax;
        private final long youngGenUsed;
        private final long oldGenUsed;
        private final long metaspaceUsed;
        private final long youngGcCount;
        private final long oldGcCount;
        private final long youngGcTimeMs;
        private final long oldGcTimeMs;
        
        /**
         * Create a new memory sample.
         */
        public MemorySample() {
            this.timestamp = Instant.now();
            
            // Get heap and non-heap memory usage
            MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
            
            this.heapUsed = heapUsage.getUsed();
            this.heapMax = heapUsage.getMax();
            this.nonHeapUsed = nonHeapUsage.getUsed();
            this.nonHeapMax = nonHeapUsage.getMax();
            
            // Get memory pool usage
            long youngGenUsed = 0;
            long oldGenUsed = 0;
            long metaspaceUsed = 0;
            
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                String name = pool.getName().toLowerCase();
                MemoryUsage usage = pool.getUsage();
                
                if (name.contains("eden") || name.contains("survivor") || name.contains("young")) {
                    youngGenUsed += usage.getUsed();
                } else if (name.contains("old") || name.contains("tenured")) {
                    oldGenUsed += usage.getUsed();
                } else if (name.contains("metaspace") || name.contains("perm")) {
                    metaspaceUsed += usage.getUsed();
                }
            }
            
            this.youngGenUsed = youngGenUsed;
            this.oldGenUsed = oldGenUsed;
            this.metaspaceUsed = metaspaceUsed;
            
            // Get GC stats
            long youngGcCount = 0;
            long oldGcCount = 0;
            long youngGcTimeMs = 0;
            long oldGcTimeMs = 0;
            
            for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                String name = gc.getName().toLowerCase();
                
                if (name.contains("young") || name.contains("scavenge") || name.contains("copy")) {
                    youngGcCount += gc.getCollectionCount();
                    youngGcTimeMs += gc.getCollectionTime();
                } else if (name.contains("old") || name.contains("mark") || name.contains("cms")) {
                    oldGcCount += gc.getCollectionCount();
                    oldGcTimeMs += gc.getCollectionTime();
                }
            }
            
            this.youngGcCount = youngGcCount;
            this.oldGcCount = oldGcCount;
            this.youngGcTimeMs = youngGcTimeMs;
            this.oldGcTimeMs = oldGcTimeMs;
        }
        
        // Getters
        public Instant getTimestamp() { return timestamp; }
        public long getHeapUsed() { return heapUsed; }
        public long getHeapMax() { return heapMax; }
        public long getNonHeapUsed() { return nonHeapUsed; }
        public long getNonHeapMax() { return nonHeapMax; }
        public long getYoungGenUsed() { return youngGenUsed; }
        public long getOldGenUsed() { return oldGenUsed; }
        public long getMetaspaceUsed() { return metaspaceUsed; }
        public long getYoungGcCount() { return youngGcCount; }
        public long getOldGcCount() { return oldGcCount; }
        public long getYoungGcTimeMs() { return youngGcTimeMs; }
        public long getOldGcTimeMs() { return oldGcTimeMs; }
        
        /**
         * Convert this sample to a JSON object.
         */
        public JsonObject toJson() {
            return JsonBuilder.create()
                .add("timestamp", timestamp.toString())
                .add("heap", JsonBuilder.create()
                    .add("used", heapUsed)
                    .add("max", heapMax)
                    .add("usedPercentage", heapMax > 0 ? (heapUsed * 100.0 / heapMax) : 0)
                    .build())
                .add("nonHeap", JsonBuilder.create()
                    .add("used", nonHeapUsed)
                    .add("max", nonHeapMax)
                    .build())
                .add("generations", JsonBuilder.create()
                    .add("young", youngGenUsed)
                    .add("old", oldGenUsed)
                    .add("metaspace", metaspaceUsed)
                    .build())
                .add("gc", JsonBuilder.create()
                    .add("young", JsonBuilder.create()
                        .add("count", youngGcCount)
                        .add("timeMs", youngGcTimeMs)
                        .build())
                    .add("old", JsonBuilder.create()
                        .add("count", oldGcCount)
                        .add("timeMs", oldGcTimeMs)
                        .build())
                    .build())
                .build();
        }
    }
    
    /**
     * Interface for components that provide memory statistics.
     */
    public interface MonitoredComponent {
        /**
         * Get the component name.
         */
        String getName();
        
        /**
         * Get memory statistics for this component.
         */
        MemoryStats getMemoryStats();
        
        /**
         * Get component-specific metrics as a JSON object.
         */
        default JsonObject getMetrics() {
            return JsonBuilder.create().build();
        }
    }
    
    /**
     * Interface for memory alert listeners.
     */
    public interface MemoryAlertListener {
        /**
         * Called when a memory alert is triggered.
         * 
         * @param alert The alert object
         */
        void onMemoryAlert(MemoryAlert alert);
    }
    
    /**
     * Memory alert types.
     */
    public enum MemoryAlertType {
        /** High heap memory usage */
        HIGH_HEAP_USAGE,
        /** High non-heap memory usage */
        HIGH_NON_HEAP_USAGE,
        /** Frequent garbage collections */
        FREQUENT_GC,
        /** Long garbage collection pauses */
        LONG_GC_PAUSE,
        /** Memory leak suspected */
        MEMORY_LEAK_SUSPECTED,
        /** Memory usage trend (increasing, decreasing) */
        MEMORY_TREND,
        /** Component-specific alert */
        COMPONENT_ALERT
    }
    
    /**
     * Memory alert.
     */
    public static class MemoryAlert {
        private final Instant timestamp;
        private final MemoryAlertType type;
        private final String message;
        private final JsonObject data;
        
        /**
         * Create a new memory alert.
         * 
         * @param type The alert type
         * @param message The alert message
         * @param data Additional data as a JSON object
         */
        public MemoryAlert(MemoryAlertType type, String message, JsonObject data) {
            this.timestamp = Instant.now();
            this.type = type;
            this.message = message;
            this.data = data;
        }
        
        // Getters
        public Instant getTimestamp() { return timestamp; }
        public MemoryAlertType getType() { return type; }
        public String getMessage() { return message; }
        public JsonObject getData() { return data; }
        
        /**
         * Convert this alert to a JSON object.
         */
        public JsonObject toJson() {
            return JsonBuilder.create()
                .add("timestamp", timestamp.toString())
                .add("type", type.name())
                .add("message", message)
                .add("data", data)
                .build();
        }
        
        @Override
        public String toString() {
            return String.format("MemoryAlert[type=%s, message=%s]", type, message);
        }
    }
    
    /**
     * Create a new memory monitor with the default interval.
     */
    public MemoryMonitor() {
        this(DEFAULT_INTERVAL_MS);
    }
    
    /**
     * Create a new memory monitor with the specified interval.
     * 
     * @param intervalMs The monitoring interval in milliseconds
     */
    public MemoryMonitor(long intervalMs) {
        this.intervalMs = intervalMs;
        this.gcOptimizer = new GarbageCollectionOptimizer(null);
        
        // Create a daemon thread pool
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "MemoryMonitor");
            t.setDaemon(true);
            return t;
        });
        
        LOG.debug("Created MemoryMonitor with interval: {} ms", intervalMs);
    }
    
    /**
     * Start monitoring.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            // Start the monitoring thread
            scheduler.scheduleAtFixedRate(
                this::sample,
                0, // Start immediately
                intervalMs,
                TimeUnit.MILLISECONDS);
            
            // Start the GC optimizer
            gcOptimizer.start();
            
            LOG.info("MemoryMonitor started with interval: {} ms", intervalMs);
        }
    }
    
    /**
     * Stop monitoring.
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                scheduler.shutdownNow();
            }
            
            // Stop the GC optimizer
            gcOptimizer.stop();
            
            LOG.info("MemoryMonitor stopped");
        }
    }
    
    /**
     * Register a component for monitoring.
     * 
     * @param component The component to register
     */
    public void registerComponent(MonitoredComponent component) {
        components.put(component.getName(), component);
        LOG.debug("Registered component for monitoring: {}", component.getName());
    }
    
    /**
     * Unregister a component.
     * 
     * @param componentName The name of the component to unregister
     */
    public void unregisterComponent(String componentName) {
        components.remove(componentName);
        LOG.debug("Unregistered component from monitoring: {}", componentName);
    }
    
    /**
     * Add a memory alert listener.
     * 
     * @param listener The listener to add
     */
    public void addAlertListener(MemoryAlertListener listener) {
        alertListeners.add(listener);
    }
    
    /**
     * Remove a memory alert listener.
     * 
     * @param listener The listener to remove
     */
    public void removeAlertListener(MemoryAlertListener listener) {
        alertListeners.remove(listener);
    }
    
    /**
     * Take a memory usage sample.
     */
    private void sample() {
        try {
            // Create a new sample
            MemorySample sample = new MemorySample();
            
            // Add to the sample history
            samples.add(sample);
            
            // Limit the history length
            while (samples.size() > MAX_HISTORY_LENGTH) {
                samples.remove(0);
            }
            
            // Check for alerts
            checkForAlerts(sample);
            
            // Sample components
            for (MonitoredComponent component : components.values()) {
                try {
                    component.getMemoryStats();
                } catch (Exception e) {
                    LOG.error("Error sampling component: {}", component.getName(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Error taking memory sample", e);
        }
    }
    
    /**
     * Check for memory alerts based on the latest sample.
     */
    private void checkForAlerts(MemorySample sample) {
        // Check for high heap usage
        double heapUsage = sample.getHeapMax() > 0 
            ? (double) sample.getHeapUsed() / sample.getHeapMax() 
            : 0;
            
        if (heapUsage > 0.9) {
            triggerAlert(new MemoryAlert(
                MemoryAlertType.HIGH_HEAP_USAGE,
                String.format("High heap usage: %.1f%%", heapUsage * 100),
                JsonBuilder.create()
                    .add("usage", heapUsage)
                    .add("used", sample.getHeapUsed())
                    .add("max", sample.getHeapMax())
                    .build()));
        }
        
        // Check for GC frequency
        if (samples.size() >= 2) {
            MemorySample previousSample = samples.get(samples.size() - 2);
            
            long youngGcDelta = sample.getYoungGcCount() - previousSample.getYoungGcCount();
            long oldGcDelta = sample.getOldGcCount() - previousSample.getOldGcCount();
            
            // Alert on frequent old GCs
            if (oldGcDelta > 2) {
                triggerAlert(new MemoryAlert(
                    MemoryAlertType.FREQUENT_GC,
                    String.format("Frequent old generation garbage collections: %d in the last %d ms", 
                        oldGcDelta, intervalMs),
                    JsonBuilder.create()
                        .add("youngGcDelta", youngGcDelta)
                        .add("oldGcDelta", oldGcDelta)
                        .build()));
            }
            
            // Alert on long GC pauses
            long youngGcTimeDelta = sample.getYoungGcTimeMs() - previousSample.getYoungGcTimeMs();
            long oldGcTimeDelta = sample.getOldGcTimeMs() - previousSample.getOldGcTimeMs();
            
            if (oldGcTimeDelta > 1000) {
                triggerAlert(new MemoryAlert(
                    MemoryAlertType.LONG_GC_PAUSE,
                    String.format("Long old generation garbage collection pause: %d ms", 
                        oldGcTimeDelta),
                    JsonBuilder.create()
                        .add("youngGcTimeDelta", youngGcTimeDelta)
                        .add("oldGcTimeDelta", oldGcTimeDelta)
                        .build()));
            }
        }
        
        // Check for memory leaks (steady increase in old gen)
        if (samples.size() >= 10) {
            boolean increasing = true;
            for (int i = samples.size() - 10; i < samples.size() - 1; i++) {
                if (samples.get(i).getOldGenUsed() > samples.get(i + 1).getOldGenUsed()) {
                    increasing = false;
                    break;
                }
            }
            
            if (increasing) {
                MemorySample firstSample = samples.get(samples.size() - 10);
                MemorySample lastSample = samples.get(samples.size() - 1);
                
                double increase = (double) (lastSample.getOldGenUsed() - firstSample.getOldGenUsed()) 
                    / firstSample.getOldGenUsed();
                    
                if (increase > 0.1) {
                    triggerAlert(new MemoryAlert(
                        MemoryAlertType.MEMORY_LEAK_SUSPECTED,
                        String.format("Possible memory leak: Old generation increased by %.1f%% over the last %d samples", 
                            increase * 100, 10),
                        JsonBuilder.create()
                            .add("increase", increase)
                            .add("oldGenStart", firstSample.getOldGenUsed())
                            .add("oldGenEnd", lastSample.getOldGenUsed())
                            .build()));
                }
            }
        }
    }
    
    /**
     * Trigger a memory alert.
     */
    private void triggerAlert(MemoryAlert alert) {
        LOG.warn("Memory alert: {}", alert);
        
        // Notify listeners
        for (MemoryAlertListener listener : alertListeners) {
            try {
                listener.onMemoryAlert(alert);
            } catch (Exception e) {
                LOG.error("Error notifying alert listener", e);
            }
        }
    }
    
    /**
     * Get the recent memory samples.
     */
    public List<MemorySample> getSamples() {
        return Collections.unmodifiableList(new ArrayList<>(samples));
    }
    
    /**
     * Get the latest memory sample.
     */
    public MemorySample getLatestSample() {
        if (samples.isEmpty()) {
            return new MemorySample();
        }
        return samples.get(samples.size() - 1);
    }
    
    /**
     * Get a summary of the current memory state as a JSON object.
     */
    public JsonObject getMemorySummary() {
        MemorySample latest = getLatestSample();
        
        JsonBuilder builder = JsonBuilder.create()
            .add("timestamp", Instant.now().toString())
            .add("sample", latest.toJson());
        
        // Add component stats
        JsonBuilder componentsBuilder = JsonBuilder.create();
        for (MonitoredComponent component : components.values()) {
            try {
                MemoryStats stats = component.getMemoryStats();
                componentsBuilder.add(component.getName(), JsonBuilder.create()
                    .add("heapUsed", stats.getOnHeapBytesUsed())
                    .add("offHeapUsed", stats.getOffHeapBytesUsed())
                    .add("metrics", component.getMetrics())
                    .build());
            } catch (Exception e) {
                LOG.error("Error getting memory stats for component: {}", component.getName(), e);
            }
        }
        builder.add("components", componentsBuilder.build());
        
        // Add GC optimizer stats
        builder.add("gcOptimizer", JsonBuilder.create()
            .add("forcedGcCount", gcOptimizer.getForcedGcCount())
            .add("lastGcTime", gcOptimizer.getLastGcTime())
            .build());
        
        return builder.build();
    }
    
    /**
     * Force garbage collection if appropriate.
     * 
     * @return true if GC was triggered, false otherwise
     */
    public boolean forceGarbageCollection() {
        return gcOptimizer.forceGarbageCollection();
    }
    
    /**
     * Set the monitoring interval.
     * 
     * @param intervalMs The interval in milliseconds
     */
    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
        
        // Restart monitoring if already running
        if (running.get()) {
            stop();
            start();
        }
    }
    
    /**
     * Get the monitoring interval.
     */
    public long getIntervalMs() {
        return intervalMs;
    }
    
    /**
     * Check if monitoring is running.
     */
    public boolean isRunning() {
        return running.get();
    }
}
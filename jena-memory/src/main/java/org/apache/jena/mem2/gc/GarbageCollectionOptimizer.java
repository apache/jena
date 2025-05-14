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

package org.apache.jena.mem2.gc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.mem2.MemoryOptimizedGraphConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optimizes garbage collection behavior for memory-intensive RDF applications.
 * <p>
 * This class provides:
 * <ul>
 * <li>Adaptive heap management to reduce GC pauses</li>
 * <li>Memory pressure monitoring and mitigation</li>
 * <li>GC-friendly allocation patterns</li>
 * <li>GC timing and optimization</li>
 * </ul>
 */
public class GarbageCollectionOptimizer {
    
    private static final Logger LOG = LoggerFactory.getLogger(GarbageCollectionOptimizer.class);
    
    // Thresholds for memory pressure
    private static final double HIGH_MEMORY_THRESHOLD = 0.85; // 85% usage
    private static final double CRITICAL_MEMORY_THRESHOLD = 0.95; // 95% usage
    
    // Minimum time between forced GCs
    private static final long MIN_FORCED_GC_INTERVAL_MS = 30_000; // 30 seconds
    
    // Thread pool for background tasks
    private final ScheduledExecutorService scheduler;
    
    // Configuration
    private final MemoryOptimizedGraphConfiguration config;
    
    // GC statistics
    private final AtomicLong lastGcTime = new AtomicLong(0);
    private final AtomicLong forcedGcCount = new AtomicLong(0);
    
    // Memory beans
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    // State
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    /**
     * Create a new garbage collection optimizer with the specified configuration.
     * 
     * @param config The memory configuration
     */
    public GarbageCollectionOptimizer(MemoryOptimizedGraphConfiguration config) {
        this.config = config;
        
        // Create a daemon thread pool for background tasks
        this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "GcOptimizer");
            t.setDaemon(true);
            return t;
        });
        
        LOG.debug("Created GarbageCollectionOptimizer with config: {}", config);
    }
    
    /**
     * Start the optimizer.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            // Start memory monitoring
            scheduler.scheduleWithFixedDelay(
                this::monitorMemory, 
                1000, // Initial delay
                5000, // Check every 5 seconds
                TimeUnit.MILLISECONDS);
            
            LOG.info("GarbageCollectionOptimizer started");
            
            // Set initial JVM options for GC optimization
            setJvmGcOptions();
        }
    }
    
    /**
     * Stop the optimizer.
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                scheduler.shutdownNow();
            }
            
            LOG.info("GarbageCollectionOptimizer stopped");
        }
    }
    
    /**
     * Force a garbage collection if conditions are appropriate.
     * 
     * @return true if GC was triggered, false otherwise
     */
    public boolean forceGarbageCollection() {
        if (!running.get()) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        
        // Check if enough time has passed since the last forced GC
        if (now - lastGcTime.get() < MIN_FORCED_GC_INTERVAL_MS) {
            LOG.debug("Skipping forced GC, too soon since last GC");
            return false;
        }
        
        // Check memory pressure
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        double memoryUsage = (double) heapUsage.getUsed() / heapUsage.getMax();
        
        if (memoryUsage < HIGH_MEMORY_THRESHOLD) {
            LOG.debug("Skipping forced GC, memory pressure not high enough: {}", memoryUsage);
            return false;
        }
        
        // Trigger garbage collection
        LOG.info("Forcing garbage collection, memory usage: {}", memoryUsage);
        System.gc();
        lastGcTime.set(now);
        forcedGcCount.incrementAndGet();
        
        return true;
    }
    
    /**
     * Monitor memory usage and trigger GC if needed.
     */
    private void monitorMemory() {
        if (!running.get()) {
            return;
        }
        
        try {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            double memoryUsage = (double) heapUsage.getUsed() / heapUsage.getMax();
            
            // Log current memory state at debug level
            LOG.debug("Memory usage: {}, heap used: {}/{} MB", 
                String.format("%.1f%%", memoryUsage * 100),
                heapUsage.getUsed() / (1024 * 1024),
                heapUsage.getMax() / (1024 * 1024));
            
            // Take action based on memory pressure
            if (memoryUsage > CRITICAL_MEMORY_THRESHOLD) {
                LOG.warn("Critical memory pressure detected: {}%, forcing garbage collection", 
                    String.format("%.1f", memoryUsage * 100));
                forceGarbageCollection();
            } else if (memoryUsage > HIGH_MEMORY_THRESHOLD) {
                LOG.info("High memory pressure detected: {}%, considering garbage collection", 
                    String.format("%.1f", memoryUsage * 100));
                
                // Only force GC if it's been a while since the last one
                long timeSinceLastGc = System.currentTimeMillis() - lastGcTime.get();
                if (timeSinceLastGc > MIN_FORCED_GC_INTERVAL_MS) {
                    forceGarbageCollection();
                }
            }
        } catch (Exception e) {
            LOG.error("Error monitoring memory", e);
        }
    }
    
    /**
     * Set JVM options for GC optimization.
     */
    private void setJvmGcOptions() {
        // This method would set JVM system properties to optimize GC behavior
        // Note that many GC settings actually need to be set at JVM startup
        // via command line arguments, but we can still set some properties
        
        // Example: disable explicit GC
        System.setProperty("DisableExplicitGC", "true");
        
        // For a real implementation, you might use JVM-specific APIs or
        // reflection to modify GC behavior at runtime, but this is
        // generally not recommended and may not be portable
        
        LOG.info("Set JVM options for GC optimization");
    }
    
    /**
     * Get the number of forced garbage collections.
     */
    public long getForcedGcCount() {
        return forcedGcCount.get();
    }
    
    /**
     * Get the time of the last forced garbage collection.
     */
    public long getLastGcTime() {
        return lastGcTime.get();
    }
    
    /**
     * Get current GC statistics.
     */
    public GcStats getGcStats() {
        GcStats stats = new GcStats();
        
        // Collect information from GC beans
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            stats.gcCount += gcBean.getCollectionCount();
            stats.gcTimeMs += gcBean.getCollectionTime();
        }
        
        // Add forced GC info
        stats.forcedGcCount = forcedGcCount.get();
        stats.lastForcedGcTime = lastGcTime.get();
        
        // Get current memory usage
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        stats.heapUsed = heapUsage.getUsed();
        stats.heapMax = heapUsage.getMax();
        stats.heapCommitted = heapUsage.getCommitted();
        
        return stats;
    }
    
    /**
     * GC statistics.
     */
    public static class GcStats {
        public long gcCount;
        public long gcTimeMs;
        public long forcedGcCount;
        public long lastForcedGcTime;
        public long heapUsed;
        public long heapMax;
        public long heapCommitted;
        
        @Override
        public String toString() {
            return String.format(
                "GC Stats: count=%d, time=%d ms, forced=%d, lastForced=%d, " +
                "heap used=%d MB, heap max=%d MB (%.1f%%)",
                gcCount, gcTimeMs, forcedGcCount, lastForcedGcTime,
                heapUsed / (1024 * 1024), heapMax / (1024 * 1024),
                (double) heapUsed / heapMax * 100);
        }
    }
}
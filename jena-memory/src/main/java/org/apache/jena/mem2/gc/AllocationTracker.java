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

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks object allocations to help identify memory leaks.
 * <p>
 * This class monitors object allocations and keeps track of objects that
 * haven't been garbage collected. It can help identify memory leaks by
 * reporting objects that stay alive longer than expected.
 */
public class AllocationTracker {
    
    private static final Logger LOG = LoggerFactory.getLogger(AllocationTracker.class);
    
    // Singleton instance with lazy initialization
    private static AllocationTracker instance;
    
    /**
     * Get the singleton instance.
     */
    public static synchronized AllocationTracker getInstance() {
        if (instance == null) {
            instance = new AllocationTracker();
        }
        return instance;
    }
    
    // Reference queue for tracking garbage collected objects
    private final ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
    
    // Maps to track allocations
    private final Map<ObjectInfo, PhantomReference<Object>> trackedObjects = new ConcurrentHashMap<>();
    private final Map<Class<?>, AtomicLong> allocationsByType = new ConcurrentHashMap<>();
    private final Set<Object> leakSuspects = Collections.newSetFromMap(new WeakHashMap<>());
    
    // Thread pool for processing reference queue
    private final ScheduledExecutorService scheduler;
    
    // Statistics
    private final AtomicLong totalAllocations = new AtomicLong(0);
    private final AtomicLong totalCollected = new AtomicLong(0);
    
    // Configuration
    private long leakSuspectThresholdMs = 60_000; // 1 minute
    
    // State
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    /**
     * Contains information about an allocated object.
     */
    public static class ObjectInfo {
        final long id;
        final Class<?> type;
        final String allocatedAt;
        final long creationTime;
        volatile long lastAccessTime;
        
        ObjectInfo(long id, Object obj) {
            this.id = id;
            this.type = obj.getClass();
            this.allocatedAt = getStackTraceString();
            this.creationTime = System.currentTimeMillis();
            this.lastAccessTime = this.creationTime;
        }
        
        private String getStackTraceString() {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            if (stack.length <= 3) {
                return "Unknown";
            }
            
            // Skip the first 3 elements (getStackTrace, getStackTraceString, constructor)
            StringBuilder sb = new StringBuilder();
            for (int i = 3; i < Math.min(stack.length, 8); i++) {
                if (i > 3) sb.append(" <- ");
                sb.append(stack[i].getClassName())
                  .append(".")
                  .append(stack[i].getMethodName())
                  .append("(")
                  .append(stack[i].getFileName())
                  .append(":")
                  .append(stack[i].getLineNumber())
                  .append(")");
            }
            return sb.toString();
        }
        
        @Override
        public int hashCode() {
            return Long.hashCode(id);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ObjectInfo other = (ObjectInfo) obj;
            return id == other.id;
        }
        
        @Override
        public String toString() {
            return String.format("Object[id=%d, type=%s, allocated=%d ms ago, last access=%d ms ago, at=%s]",
                id, type.getName(),
                System.currentTimeMillis() - creationTime,
                System.currentTimeMillis() - lastAccessTime,
                allocatedAt);
        }
    }
    
    /**
     * Create a new allocation tracker.
     */
    private AllocationTracker() {
        // Create a daemon thread pool
        this.scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "AllocationTracker");
                t.setDaemon(true);
                return t;
            }
        });
    }
    
    /**
     * Enable or disable tracking.
     * 
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        if (enabled && !running.get()) {
            start();
        } else if (!enabled && running.get()) {
            stop();
        }
    }
    
    /**
     * Check if tracking is enabled.
     */
    public boolean isEnabled() {
        return enabled.get();
    }
    
    /**
     * Set the threshold for considering an object a potential leak.
     * 
     * @param thresholdMs The threshold in milliseconds
     */
    public void setLeakSuspectThresholdMs(long thresholdMs) {
        this.leakSuspectThresholdMs = thresholdMs;
    }
    
    /**
     * Start the tracker.
     */
    private void start() {
        if (running.compareAndSet(false, true)) {
            // Start reference processing
            scheduler.scheduleWithFixedDelay(
                this::processReferenceQueue,
                100, // Initial delay
                1000, // Check every second
                TimeUnit.MILLISECONDS);
            
            // Start leak detection
            scheduler.scheduleWithFixedDelay(
                this::detectLeaks,
                1000, // Initial delay
                10000, // Check every 10 seconds
                TimeUnit.MILLISECONDS);
            
            LOG.info("AllocationTracker started");
        }
    }
    
    /**
     * Stop the tracker.
     */
    private void stop() {
        if (running.compareAndSet(true, false)) {
            LOG.info("AllocationTracker stopped");
        }
    }
    
    /**
     * Track an object allocation.
     * 
     * @param obj The object to track
     * @return An info object for the tracked object
     */
    public ObjectInfo trackAllocation(Object obj) {
        if (!enabled.get() || obj == null) {
            return null;
        }
        
        // Create an info record for this object
        long id = totalAllocations.incrementAndGet();
        ObjectInfo info = new ObjectInfo(id, obj);
        
        // Create a phantom reference for this object
        PhantomReference<Object> ref = new PhantomReference<>(obj, refQueue);
        trackedObjects.put(info, ref);
        
        // Update allocation statistics
        allocationsByType.computeIfAbsent(obj.getClass(), k -> new AtomicLong(0))
            .incrementAndGet();
        
        return info;
    }
    
    /**
     * Record object access to update the last access time.
     * 
     * @param info The object info
     */
    public void recordAccess(ObjectInfo info) {
        if (info != null) {
            info.lastAccessTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Process the reference queue to detect garbage collected objects.
     */
    private void processReferenceQueue() {
        if (!running.get()) {
            return;
        }
        
        try {
            // Poll the reference queue for garbage collected objects
            Reference<?> ref;
            while ((ref = refQueue.poll()) != null) {
                // Find the corresponding info and remove it
                for (Map.Entry<ObjectInfo, PhantomReference<Object>> entry : trackedObjects.entrySet()) {
                    if (entry.getValue() == ref) {
                        trackedObjects.remove(entry.getKey());
                        totalCollected.incrementAndGet();
                        break;
                    }
                }
                
                // Clear the reference
                ref.clear();
            }
        } catch (Exception e) {
            LOG.error("Error processing reference queue", e);
        }
    }
    
    /**
     * Detect potential memory leaks.
     */
    private void detectLeaks() {
        if (!running.get()) {
            return;
        }
        
        try {
            long now = System.currentTimeMillis();
            
            // Clear previous leak suspects
            leakSuspects.clear();
            
            // Find objects that haven't been accessed for a long time
            Map<Class<?>, Integer> leaksByType = new HashMap<>();
            
            for (Map.Entry<ObjectInfo, PhantomReference<Object>> entry : trackedObjects.entrySet()) {
                ObjectInfo info = entry.getKey();
                
                // Check if this object is a leak suspect
                if (now - info.lastAccessTime > leakSuspectThresholdMs) {
                    // Try to get the actual object through the reference
                    PhantomReference<Object> ref = entry.getValue();
                    // Since it's a phantom reference, we can't get the original object
                    // but we can still track the info
                    
                    // Update leak counts by type
                    leaksByType.compute(info.type, (k, v) -> (v == null) ? 1 : v + 1);
                    
                    // Log detailed info about the leak
                    LOG.debug("Potential memory leak: {}", info);
                }
            }
            
            // Log summary of potential leaks
            if (!leaksByType.isEmpty()) {
                LOG.warn("Potential memory leaks detected: {}", leaksByType);
            }
        } catch (Exception e) {
            LOG.error("Error detecting leaks", e);
        }
    }
    
    /**
     * Get the total number of tracked allocations.
     */
    public long getTotalAllocations() {
        return totalAllocations.get();
    }
    
    /**
     * Get the total number of collected objects.
     */
    public long getTotalCollected() {
        return totalCollected.get();
    }
    
    /**
     * Get the current number of tracked objects.
     */
    public int getTrackedObjectCount() {
        return trackedObjects.size();
    }
    
    /**
     * Get a map of allocation counts by type.
     */
    public Map<Class<?>, Long> getAllocationsByType() {
        Map<Class<?>, Long> result = new HashMap<>();
        allocationsByType.forEach((type, count) -> result.put(type, count.get()));
        return result;
    }
    
    /**
     * Get information about tracked objects.
     */
    public String getTrackedObjectsInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tracked objects: ").append(trackedObjects.size()).append("\n");
        sb.append("Total allocations: ").append(totalAllocations.get()).append("\n");
        sb.append("Total collected: ").append(totalCollected.get()).append("\n");
        
        // Add allocation counts by type
        sb.append("Allocations by type:\n");
        allocationsByType.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
            .limit(20)
            .forEach(entry -> {
                sb.append("  ").append(entry.getKey().getName())
                  .append(": ").append(entry.getValue().get()).append("\n");
            });
        
        return sb.toString();
    }
    
    /**
     * Shutdown the tracker.
     */
    public void shutdown() {
        setEnabled(false);
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            scheduler.shutdownNow();
        }
        
        trackedObjects.clear();
        allocationsByType.clear();
        leakSuspects.clear();
        LOG.info("AllocationTracker shutdown");
    }
}
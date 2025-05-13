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

package org.apache.jena.delta.server.conflict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.conflict.Conflict;
import org.apache.jena.delta.conflict.ConflictDetector;
import org.apache.jena.delta.conflict.ConflictResolver;
import org.apache.jena.delta.conflict.ConflictType;
import org.apache.jena.delta.conflict.ResolutionResult;
import org.apache.jena.delta.conflict.ResolutionStatus;
import org.apache.jena.delta.conflict.ResolutionStrategy;
import org.apache.jena.delta.server.PatchLogServer;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.system.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * A patch log server wrapper that adds conflict detection and resolution.
 * 
 * This class wraps an existing PatchLogServer implementation and adds conflict
 * detection and resolution capabilities. It intercepts append operations to
 * check for conflicts and apply resolution strategies.
 */
public class ConflictAwarePatchLogServer implements PatchLogServer, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ConflictAwarePatchLogServer.class);
    
    private final PatchLogServer baseServer;
    private final ConflictDetector detector;
    private final ConflictResolver resolver;
    private final MeterRegistry registry;
    private final ScheduledExecutorService executor;
    
    // Cache of recently processed patches for conflict detection
    private final Map<String, Map<Id, RDFPatch>> recentPatchesCache = new HashMap<>();
    private final long cacheExpiryMs;
    
    // Metrics
    private final Counter conflictsDetected;
    private final Counter conflictsResolved;
    private final Counter resolutionRejected;
    private final Timer conflictDetectionTime;
    
    /**
     * Create a new ConflictAwarePatchLogServer with default settings.
     * 
     * @param baseServer The underlying patch log server
     */
    public ConflictAwarePatchLogServer(PatchLogServer baseServer) {
        this(baseServer, new ConflictDetector(), new ConflictResolver(), null, 60000);
    }
    
    /**
     * Create a new ConflictAwarePatchLogServer with specific settings.
     * 
     * @param baseServer The underlying patch log server
     * @param detector The conflict detector to use
     * @param resolver The conflict resolver to use
     * @param registry Registry for metrics (can be null)
     * @param cacheExpiryMs Time in milliseconds to keep patches in the cache
     */
    public ConflictAwarePatchLogServer(
            PatchLogServer baseServer,
            ConflictDetector detector,
            ConflictResolver resolver,
            MeterRegistry registry,
            long cacheExpiryMs) {
        
        this.baseServer = baseServer;
        this.detector = detector;
        this.resolver = resolver;
        this.registry = registry;
        this.cacheExpiryMs = cacheExpiryMs;
        this.executor = Executors.newScheduledThreadPool(1);
        
        // Initialize metrics
        if (registry != null) {
            this.conflictsDetected = Counter.builder("delta_server_conflicts_detected")
                .description("Number of conflicts detected by the server")
                .register(registry);
            
            this.conflictsResolved = Counter.builder("delta_server_conflicts_resolved")
                .description("Number of conflicts resolved by the server")
                .register(registry);
            
            this.resolutionRejected = Counter.builder("delta_server_resolutions_rejected")
                .description("Number of conflict resolutions rejected by the server")
                .register(registry);
            
            this.conflictDetectionTime = Timer.builder("delta_server_conflict_detection_time")
                .description("Time spent detecting conflicts")
                .register(registry);
        } else {
            this.conflictsDetected = null;
            this.conflictsResolved = null;
            this.resolutionRejected = null;
            this.conflictDetectionTime = null;
        }
        
        // Schedule cleanup of expired patches
        executor.scheduleAtFixedRate(this::cleanupExpiredPatches, cacheExpiryMs, cacheExpiryMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Set the resolution strategy for a specific conflict type.
     * 
     * @param type The conflict type
     * @param strategy The resolution strategy
     * @return This server
     */
    public ConflictAwarePatchLogServer setResolutionStrategy(ConflictType type, ResolutionStrategy strategy) {
        resolver.setStrategy(type, strategy);
        return this;
    }
    
    /**
     * Clean up expired patches from the cache.
     */
    private void cleanupExpiredPatches() {
        try {
            long now = System.currentTimeMillis();
            
            synchronized (recentPatchesCache) {
                // Remove patches older than the expiry time
                // This is a placeholder for a real implementation that would track timestamps
                // In a real implementation, each patch would have a timestamp
                
                // For now, just clear the cache periodically
                recentPatchesCache.clear();
            }
        } catch (Exception e) {
            LOG.error("Error cleaning up expired patches", e);
        }
    }
    
    @Override
    public List<LogEntry> listPatchLogs() {
        return baseServer.listPatchLogs();
    }
    
    @Override
    public Id createPatchLog(String name) {
        return baseServer.createPatchLog(name);
    }
    
    @Override
    public LogEntry getPatchLogInfo(String name) {
        return baseServer.getPatchLogInfo(name);
    }
    
    @Override
    public Id append(String name, RDFPatch patch, Id expected) {
        // Check for conflicts
        if (conflictDetectionTime != null) {
            return conflictDetectionTime.record(() -> appendWithConflictDetection(name, patch, expected));
        } else {
            return appendWithConflictDetection(name, patch, expected);
        }
    }
    
    /**
     * Internal method to append a patch with conflict detection.
     */
    private Id appendWithConflictDetection(String name, RDFPatch patch, Id expected) {
        // Get the current head
        LogEntry info = baseServer.getPatchLogInfo(name);
        if (info == null) {
            throw new DeltaException("Dataset not found: " + name);
        }
        
        Id head = info.getHead();
        
        // Check if the patch is already the head (idempotent)
        if (patch.getId().equals(head)) {
            return head;
        }
        
        // Check if the patch's previous matches the expected
        if (expected != null && !expected.equals(head)) {
            // Get the most recent patches
            Map<Id, RDFPatch> recentPatches = getRecentPatches(name, head);
            
            // Get the patch that the client was expecting as the head
            RDFPatch expectedPatch = getHeadPatch(name, expected);
            
            if (expectedPatch != null) {
                // Detect conflicts
                List<Conflict> conflicts = detector.detectConflicts(expectedPatch, patch);
                
                if (!conflicts.isEmpty()) {
                    if (conflictsDetected != null) {
                        conflictsDetected.increment(conflicts.size());
                    }
                    
                    LOG.debug("Detected {} conflicts in patch for {}", conflicts.size(), name);
                    
                    // Resolve conflicts
                    ResolutionResult result = resolver.resolveConflicts(expectedPatch, patch);
                    
                    if (result.isSuccess()) {
                        if (conflictsResolved != null) {
                            conflictsResolved.increment();
                        }
                        
                        LOG.debug("Resolved conflicts for {}: {}", name, result.getMessage());
                        
                        // Use the resolved patch
                        RDFPatch resolvedPatch = result.getResolvedPatch();
                        
                        // Append the resolved patch
                        return baseServer.append(name, resolvedPatch, head);
                    } else {
                        if (resolutionRejected != null) {
                            resolutionRejected.increment();
                        }
                        
                        LOG.warn("Conflict resolution rejected for {}: {}", name, result.getMessage());
                        
                        if (result.getStatus() == ResolutionStatus.REJECTED) {
                            throw new DeltaException("Conflict detected and resolution rejected: " + result.getMessage());
                        } else {
                            throw new DeltaException("Error resolving conflict: " + result.getMessage());
                        }
                    }
                }
            }
            
            // If we got here with a version mismatch, it's a concurrent modification
            throw new DeltaException(String.format(
                "Expected version %s does not match current version %s for patch log %s",
                expected, head, name));
        }
        
        // No conflicts or version match, append normally
        Id newHead = baseServer.append(name, patch, expected);
        
        // Cache the patch for future conflict detection
        cacheRecentPatch(name, patch);
        
        return newHead;
    }
    
    /**
     * Get recent patches for a dataset.
     */
    private Map<Id, RDFPatch> getRecentPatches(String name, Id head) {
        synchronized (recentPatchesCache) {
            return recentPatchesCache.computeIfAbsent(name, k -> new HashMap<>());
        }
    }
    
    /**
     * Get the head patch for a dataset.
     */
    private RDFPatch getHeadPatch(String name, Id head) {
        // First check the cache
        synchronized (recentPatchesCache) {
            Map<Id, RDFPatch> patches = recentPatchesCache.get(name);
            if (patches != null && patches.containsKey(head)) {
                return patches.get(head);
            }
        }
        
        // If not in cache, get from the server
        return baseServer.getPatch(name, head);
    }
    
    /**
     * Cache a recent patch for future conflict detection.
     */
    private void cacheRecentPatch(String name, RDFPatch patch) {
        synchronized (recentPatchesCache) {
            Map<Id, RDFPatch> patches = recentPatchesCache.computeIfAbsent(name, k -> new HashMap<>());
            
            // Add the new patch
            patches.put(patch.getId(), patch);
            
            // TODO: Limit the cache size (not needed for the demo)
        }
    }
    
    @Override
    public Iterable<RDFPatch> getPatches(String name, Id start) {
        return baseServer.getPatches(name, start);
    }
    
    @Override
    public RDFPatch getPatch(String name, Id id) {
        // First check the cache
        synchronized (recentPatchesCache) {
            Map<Id, RDFPatch> patches = recentPatchesCache.get(name);
            if (patches != null && patches.containsKey(id)) {
                return patches.get(id);
            }
        }
        
        // If not in cache, get from the server
        return baseServer.getPatch(name, id);
    }
    
    @Override
    public void close() throws Exception {
        // Shutdown the executor
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Close the base server if it's AutoCloseable
        if (baseServer instanceof AutoCloseable) {
            ((AutoCloseable) baseServer).close();
        }
    }
}
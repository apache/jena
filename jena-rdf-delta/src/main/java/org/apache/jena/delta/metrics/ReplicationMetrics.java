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
import io.micrometer.core.instrument.Timer.Sample;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.delta.DeltaLink;
import org.apache.jena.delta.Id;
import org.apache.jena.delta.client.DeltaLinkHTTP;
import org.apache.jena.delta.client.Syncer;
import org.apache.jena.rdfpatch.RDFPatch;

/**
 * Metrics collection for dataset replication and synchronization.
 * <p>
 * This class collects metrics related to:
 * <ul>
 * <li>Replication lag and sync status</li>
 * <li>Sync operations and performance</li>
 * <li>Patch application success/failure</li>
 * <li>Network operations between client and server</li>
 * </ul>
 */
public class ReplicationMetrics {
    
    // Operation counters and timers
    private final Counter syncCounter;
    private final Counter patchAppliedCounter;
    private final Counter patchFailedCounter;
    private final Timer syncTimer;
    private final Timer patchApplyTimer;
    
    // State gauges
    private final Map<String, AtomicLong> replicationLags = new ConcurrentHashMap<>();
    
    // The client being monitored
    private final String clientName;
    
    /**
     * Create a new metrics collector for dataset replication.
     * 
     * @param registry The meter registry to use
     * @param clientName The client name (used for tagging)
     */
    public ReplicationMetrics(MeterRegistry registry, String clientName) {
        this.clientName = clientName;
        
        Tag clientTag = DeltaMetrics.tag(DeltaMetrics.TAG_COMPONENT, clientName);
        
        // Create operation counters
        syncCounter = DeltaMetrics.createCounter(registry, 
            "replication.sync", 
            "Number of sync operations performed",
            clientTag);
            
        patchAppliedCounter = DeltaMetrics.createCounter(registry, 
            "replication.patch.applied", 
            "Number of patches successfully applied",
            clientTag);
            
        patchFailedCounter = DeltaMetrics.createCounter(registry, 
            "replication.patch.failed", 
            "Number of patches that failed to apply",
            clientTag);
            
        // Create operation timers
        syncTimer = DeltaMetrics.createTimer(registry, 
            "replication.sync.time", 
            "Time taken to perform sync operations",
            clientTag);
            
        patchApplyTimer = DeltaMetrics.createTimer(registry, 
            "replication.patch.apply.time", 
            "Time taken to apply patches",
            clientTag);
    }
    
    /**
     * Record a sync operation start.
     * 
     * @param datasetName The dataset name
     * @return A timing sample that should be stopped when the operation completes
     */
    public Sample recordSyncStart(String datasetName) {
        syncCounter.increment();
        return Timer.start();
    }
    
    /**
     * Record completion of a sync operation.
     * 
     * @param sample The timing sample from recordSyncStart
     * @param datasetName The dataset name
     * @param patchesApplied Number of patches applied during sync
     * @param success Whether the operation succeeded
     */
    public void recordSyncComplete(Sample sample, String datasetName, 
                                    int patchesApplied, boolean success) {
        sample.stop(syncTimer);
        if (success) {
            patchAppliedCounter.increment(patchesApplied);
        } else {
            patchFailedCounter.increment();
        }
    }
    
    /**
     * Record a patch apply operation start.
     * 
     * @param datasetName The dataset name
     * @return A timing sample that should be stopped when the operation completes
     */
    public Sample recordPatchApplyStart(String datasetName) {
        return Timer.start();
    }
    
    /**
     * Record completion of a patch apply operation.
     * 
     * @param sample The timing sample from recordPatchApplyStart
     * @param success Whether the operation succeeded
     */
    public void recordPatchApplyComplete(Sample sample, boolean success) {
        sample.stop(patchApplyTimer);
        if (success) {
            patchAppliedCounter.increment();
        } else {
            patchFailedCounter.increment();
        }
    }
    
    /**
     * Register and update replication lag metric for a dataset.
     * 
     * @param registry The meter registry
     * @param datasetName The dataset name
     * @param syncer The syncer for this dataset
     * @param link The delta link to the server
     */
    public void registerReplicationLag(MeterRegistry registry, String datasetName, 
                                       Syncer syncer, DeltaLink link) {
        AtomicLong lagCounter = new AtomicLong(0);
        replicationLags.put(datasetName, lagCounter);
        
        // Register a gauge for this dataset's replication lag
        DeltaMetrics.createGauge(
            registry,
            "replication.lag", 
            "Number of patches behind the server",
            lagCounter, 
            AtomicLong::doubleValue,
            DeltaMetrics.tag(DeltaMetrics.TAG_COMPONENT, clientName),
            DeltaMetrics.tag(DeltaMetrics.TAG_DATASET, datasetName));
            
        // Start a background thread to update the lag
        Thread lagMonitor = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    updateLag(datasetName, syncer, link, lagCounter);
                    Thread.sleep(5000); // Update every 5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Log error but continue monitoring
                }
            }
        });
        lagMonitor.setDaemon(true);
        lagMonitor.setName("LagMonitor-" + datasetName);
        lagMonitor.start();
    }
    
    /**
     * Update the replication lag for a dataset.
     */
    private void updateLag(String datasetName, Syncer syncer, DeltaLink link, AtomicLong lagCounter) {
        try {
            Id localVersion = syncer.getCurrentVersion();
            Id remoteVersion = link.getRemoteVersionLatest(datasetName);
            
            if (localVersion != null && remoteVersion != null) {
                long local = localVersion.value();
                long remote = remoteVersion.value();
                
                // Update the lag counter
                lagCounter.set(Math.max(0, remote - local));
            }
        } catch (Exception e) {
            // Ignore and try again next time
        }
    }
    
    /**
     * Remove replication lag monitoring for a dataset.
     * 
     * @param datasetName The dataset name
     */
    public void removeReplicationLag(String datasetName) {
        replicationLags.remove(datasetName);
    }
    
    /**
     * Get the current replication lag for a dataset.
     * 
     * @param datasetName The dataset name
     * @return The current lag, or -1 if not being monitored
     */
    public long getReplicationLag(String datasetName) {
        AtomicLong lag = replicationLags.get(datasetName);
        return lag != null ? lag.get() : -1;
    }
    
    /**
     * Get a map of datasets and their current replication lags.
     * 
     * @return Map of dataset names to replication lags
     */
    public Map<String, Long> getAllReplicationLags() {
        Map<String, Long> result = new HashMap<>();
        replicationLags.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
}
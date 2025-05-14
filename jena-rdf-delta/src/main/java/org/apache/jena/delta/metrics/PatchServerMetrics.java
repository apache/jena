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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.delta.Id;
import org.apache.jena.delta.PatchLogInfo;
import org.apache.jena.delta.server.PatchLogServer;
import org.apache.jena.delta.server.PatchStore;
import org.apache.jena.delta.server.local.PatchLog;
import org.apache.jena.delta.server.local.PatchStoreLocal;
import org.apache.jena.rdfpatch.RDFPatch;

/**
 * Metrics collection for Patch Server operations.
 * <p>
 * This class collects metrics related to:
 * <ul>
 * <li>Patch operations (append, fetch)</li>
 * <li>Patch size distribution</li>
 * <li>Operation latency</li>
 * <li>Active logs and datasets</li>
 * <li>Failure rates</li>
 * </ul>
 */
public class PatchServerMetrics {
    
    // Operation counters and timers
    private final Counter appendCounter;
    private final Counter fetchCounter;
    private final Counter createLogCounter;
    private final Timer appendTimer;
    private final Timer fetchTimer;
    private final DistributionSummary patchSizeSummary;
    private final Counter errorCounter;
    
    // Operational state gauges
    private final AtomicLong activeLogs = new AtomicLong(0);
    private final Map<String, AtomicLong> datasetPatchCounts = new ConcurrentHashMap<>();
    
    // The server being monitored
    private final PatchLogServer server;
    
    /**
     * Create a new metrics collector for a patch server.
     * 
     * @param registry The meter registry to use
     * @param server The server to monitor
     * @param serverName The server name (used for tagging)
     */
    public PatchServerMetrics(MeterRegistry registry, PatchLogServer server, String serverName) {
        this.server = server;
        
        Tag serverTag = DeltaMetrics.tag(DeltaMetrics.TAG_SERVER, serverName);
        
        // Create operation counters
        appendCounter = DeltaMetrics.createCounter(registry, 
            "server.patch.append", 
            "Number of patches appended to logs",
            serverTag);
            
        fetchCounter = DeltaMetrics.createCounter(registry, 
            "server.patch.fetch", 
            "Number of patches fetched from logs",
            serverTag);
            
        createLogCounter = DeltaMetrics.createCounter(registry, 
            "server.log.create", 
            "Number of patch logs created",
            serverTag);
            
        // Create operation timers
        appendTimer = DeltaMetrics.createTimer(registry, 
            "server.patch.append.time", 
            "Time taken to append patches to logs",
            serverTag);
            
        fetchTimer = DeltaMetrics.createTimer(registry, 
            "server.patch.fetch.time", 
            "Time taken to fetch patches from logs",
            serverTag);
            
        // Patch size summary
        patchSizeSummary = DeltaMetrics.createDistributionSummary(registry,
            "server.patch.size", 
            "Distribution of patch sizes in bytes",
            serverTag);
            
        // Error counter
        errorCounter = DeltaMetrics.createCounter(registry, 
            "server.errors", 
            "Number of errors in server operations",
            serverTag);
            
        // Register gauges for operational state
        DeltaMetrics.createGauge(registry,
            "server.logs.active", 
            "Number of active patch logs",
            activeLogs, 
            AtomicLong::doubleValue,
            serverTag);
            
        // Update active logs count
        updateActiveLogs();
    }
    
    /**
     * Record an append operation.
     * 
     * @param datasetName The dataset name
     * @param patch The patch being appended
     * @return A timing sample that should be stopped when the operation completes
     */
    public Sample recordAppendStart(String datasetName, RDFPatch patch) {
        appendCounter.increment();
        updatePatchCountForDataset(datasetName);
        if (patch != null && patch.estimatedSize() > 0) {
            patchSizeSummary.record(patch.estimatedSize());
        }
        return Timer.start();
    }
    
    /**
     * Record completion of an append operation.
     * 
     * @param sample The timing sample from recordAppendStart
     * @param success Whether the operation succeeded
     */
    public void recordAppendComplete(Sample sample, boolean success) {
        sample.stop(appendTimer);
        if (!success) {
            errorCounter.increment();
        }
    }
    
    /**
     * Record a fetch operation.
     * 
     * @param datasetName The dataset name
     * @return A timing sample that should be stopped when the operation completes
     */
    public Sample recordFetchStart(String datasetName) {
        fetchCounter.increment();
        return Timer.start();
    }
    
    /**
     * Record completion of a fetch operation.
     * 
     * @param sample The timing sample from recordFetchStart
     * @param success Whether the operation succeeded
     */
    public void recordFetchComplete(Sample sample, boolean success) {
        sample.stop(fetchTimer);
        if (!success) {
            errorCounter.increment();
        }
    }
    
    /**
     * Record creation of a new patch log.
     * 
     * @param datasetName The dataset name
     */
    public void recordLogCreation(String datasetName) {
        createLogCounter.increment();
        updateActiveLogs();
        
        // Initialize patch count for this dataset
        datasetPatchCounts.computeIfAbsent(datasetName, k -> {
            AtomicLong counter = new AtomicLong(0);
            // Register a gauge for this dataset's patch count
            DeltaMetrics.createGauge(
                DeltaMetrics.getRegistry(),
                "server.dataset.patches", 
                "Number of patches in dataset",
                counter, 
                AtomicLong::doubleValue,
                DeltaMetrics.tag(DeltaMetrics.TAG_SERVER, server.getLabel()),
                DeltaMetrics.tag(DeltaMetrics.TAG_DATASET, datasetName));
            return counter;
        });
    }
    
    /**
     * Record a generic error in server operation.
     * 
     * @param operation The operation that failed
     */
    public void recordError(String operation) {
        errorCounter.increment();
    }
    
    /**
     * Update the count of active logs.
     */
    private void updateActiveLogs() {
        if (server != null) {
            activeLogs.set(server.listDatasets().size());
        }
    }
    
    /**
     * Update the patch count for a dataset.
     * 
     * @param datasetName The dataset name
     */
    private void updatePatchCountForDataset(String datasetName) {
        // Increment the patch count for this dataset
        datasetPatchCounts.computeIfPresent(datasetName, (k, v) -> {
            v.incrementAndGet();
            return v;
        });
    }
    
    /**
     * Get a map of datasets and their current patch counts.
     * 
     * @return Map of dataset names to patch counts
     */
    public Map<String, Long> getDatasetPatchCounts() {
        Map<String, Long> result = new HashMap<>();
        datasetPatchCounts.forEach((k, v) -> result.put(k, v.get()));
        return Collections.unmodifiableMap(result);
    }
}
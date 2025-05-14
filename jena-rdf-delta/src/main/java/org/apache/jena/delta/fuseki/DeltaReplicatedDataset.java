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

package org.apache.jena.delta.fuseki;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.client.DeltaClient;
import org.apache.jena.delta.client.DeltaLink;
import org.apache.jena.delta.client.PatchLogListener;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.changes.RDFChangesCollector;
import org.apache.jena.rdfpatch.system.Id;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * A dataset that is replicated using RDF Delta, specifically designed for Fuseki integration.
 * <p>
 * This extends DatasetGraphWrapper and implements the Transactional interface
 * to integrate with Fuseki. It synchronizes changes with a patch log server
 * and updates the local copy when changes are detected on the server.
 */
public class DeltaReplicatedDataset extends DatasetGraphWrapper implements Transactional, PatchLogListener {
    private static final Logger LOG = LoggerFactory.getLogger(DeltaReplicatedDataset.class);
    
    private final DeltaClient client;
    private final String datasetName;
    private final DeltaClient.PatchLog patchLog;
    private final MeterRegistry registry;
    
    // Per-transaction state
    private ThreadLocal<TxnState> txnState = new ThreadLocal<>();
    
    // Metrics
    private final Counter patchesIn;
    private final Counter patchesOut;
    private final Counter transactionCount;
    private final Timer applyPatchTime;
    private final Timer createPatchTime;
    private final Timer transactionTime;
    
    // Status
    private Id currentVersion;
    private long lastSyncTime;
    private final AtomicLong patchCounter = new AtomicLong(0);
    
    /**
     * Create a new DeltaReplicatedDataset.
     * 
     * @param client The DeltaClient
     * @param datasetName The name of the dataset
     * @param base The underlying dataset
     */
    public DeltaReplicatedDataset(DeltaClient client, String datasetName, DatasetGraph base) {
        super(base);
        
        this.client = Objects.requireNonNull(client, "DeltaClient must not be null");
        this.datasetName = Objects.requireNonNull(datasetName, "Dataset name must not be null");
        this.patchLog = client.getPatchLog(datasetName);
        this.currentVersion = this.patchLog.getCurrentVersion();
        this.lastSyncTime = System.currentTimeMillis();
        
        // Register for patch log change notifications
        this.patchLog.register(this);
        
        // Set up metrics
        this.registry = new SimpleMeterRegistry();
        
        this.patchesIn = Counter.builder("delta_dataset_patches_in")
            .description("Number of patches received from the server")
            .tags(Tags.of("dataset", datasetName))
            .register(registry);
        
        this.patchesOut = Counter.builder("delta_dataset_patches_out")
            .description("Number of patches sent to the server")
            .tags(Tags.of("dataset", datasetName))
            .register(registry);
        
        this.transactionCount = Counter.builder("delta_dataset_transactions")
            .description("Number of transactions processed")
            .tags(Tags.of("dataset", datasetName))
            .register(registry);
        
        this.applyPatchTime = Timer.builder("delta_dataset_apply_patch_time")
            .description("Time to apply a patch to the dataset")
            .tags(Tags.of("dataset", datasetName))
            .register(registry);
        
        this.createPatchTime = Timer.builder("delta_dataset_create_patch_time")
            .description("Time to create a patch from dataset changes")
            .tags(Tags.of("dataset", datasetName))
            .register(registry);
        
        this.transactionTime = Timer.builder("delta_dataset_transaction_time")
            .description("Time to complete a transaction")
            .tags(Tags.of("dataset", datasetName))
            .register(registry);
        
        LOG.info("Created DeltaReplicatedDataset for {} with initial version {}", datasetName, currentVersion);
    }
    
    /**
     * Get the name of the dataset.
     */
    public String getDatasetName() {
        return datasetName;
    }
    
    /**
     * Get the current version of the dataset.
     */
    public Id getCurrentVersion() {
        return currentVersion;
    }
    
    /**
     * Get the timestamp of the last synchronization with the server.
     */
    public long getLastSyncTime() {
        return lastSyncTime;
    }
    
    /**
     * Get the number of patches applied since this dataset was created.
     */
    public long getPatchCount() {
        return patchCounter.get();
    }
    
    /**
     * Explicitly check for updates from the patch log server.
     * This is normally done automatically by the DeltaClient polling.
     */
    public void checkForUpdates() {
        try {
            Id serverVersion = client.getDeltaLink().getDatasetVersion(datasetName);
            if (!serverVersion.equals(currentVersion)) {
                LOG.debug("Manual check detected new version for {}: {} -> {}", 
                          datasetName, currentVersion, serverVersion);
                
                // Fetch and apply patches
                Iterable<RDFPatch> patches = client.getDeltaLink().getPatches(datasetName, currentVersion);
                for (RDFPatch patch : patches) {
                    applyPatch(patch);
                }
            }
        } catch (Exception e) {
            LOG.error("Error checking for updates for {}", datasetName, e);
        }
    }
    
    @Override
    public void patchLogChanged(RDFPatch patch) {
        LOG.debug("Patch log {} changed: {}", datasetName, patch.getId());
        
        // Apply the patch to the dataset
        applyPatch(patch);
    }
    
    /**
     * Apply a patch to the dataset.
     */
    private void applyPatch(RDFPatch patch) {
        // Skip if we've already applied this patch or earlier ones
        if (patch.getId().equals(currentVersion)) {
            return;
        }
        
        // Measure the time to apply the patch
        applyPatchTime.record(() -> {
            // Apply the patch in a transaction
            Txn.executeWrite(getWrapped(), () -> {
                LOG.debug("Applying patch {} to {}", patch.getId(), datasetName);
                
                try {
                    // Apply the patch
                    RDFPatchOps.applyChange(patch, getWrapped());
                    
                    // Update the current version
                    currentVersion = patch.getId();
                    lastSyncTime = System.currentTimeMillis();
                    patchCounter.incrementAndGet();
                    patchesIn.increment();
                } catch (Exception e) {
                    LOG.error("Error applying patch {} to {}", patch.getId(), datasetName, e);
                    throw new DeltaException("Error applying patch: " + e.getMessage(), e);
                }
            });
        });
    }

    // ==== Transactional interface implementation ====
    
    @Override
    public void begin(ReadWrite readWrite) {
        transactionTime.record(() -> {
            // Check if already in a transaction
            TxnState state = txnState.get();
            if (state != null) {
                throw new JenaTransactionException("Already in a transaction");
            }
            
            // Create a new transaction state
            state = new TxnState(readWrite);
            txnState.set(state);
            
            // If this is a write transaction, set up the change collector
            if (readWrite == ReadWrite.WRITE) {
                state.collector = new RDFChangesCollector();
                LOG.debug("Started write transaction for {}", datasetName);
            }
            
            // Begin the transaction on the wrapped dataset
            getWrapped().begin(readWrite);
        });
    }

    @Override
    public void begin(TxnType type) {
        transactionTime.record(() -> {
            // Check if already in a transaction
            TxnState state = txnState.get();
            if (state != null) {
                throw new JenaTransactionException("Already in a transaction");
            }
            
            // Create a new transaction state
            ReadWrite readWrite = TxnType.READ == type ? ReadWrite.READ : ReadWrite.WRITE;
            state = new TxnState(readWrite);
            state.type = type;
            txnState.set(state);
            
            // If this is a write transaction, set up the change collector
            if (readWrite == ReadWrite.WRITE || type == TxnType.READ_PROMOTE || type == TxnType.READ_COMMITTED_PROMOTE) {
                state.collector = new RDFChangesCollector();
                LOG.debug("Started write transaction (type={}) for {}", type, datasetName);
            }
            
            // Begin the transaction on the wrapped dataset
            getWrapped().begin(type);
        });
    }

    @Override
    public boolean promote(Promote mode) {
        // Get the transaction state
        TxnState state = txnState.get();
        if (state == null) {
            throw new JenaTransactionException("Not in a transaction");
        }
        
        if (state.readWrite == ReadWrite.WRITE) {
            return true;  // Already a write transaction
        }
        
        // Try to promote the wrapped transaction
        boolean success = getWrapped().promote(mode);
        
        if (success) {
            // Update the transaction state
            state.readWrite = ReadWrite.WRITE;
            
            // Set up the change collector if needed
            if (state.collector == null) {
                state.collector = new RDFChangesCollector();
                LOG.debug("Promoted transaction to write for {}", datasetName);
            }
        }
        
        return success;
    }

    @Override
    public void commit() {
        transactionTime.record(() -> {
            // Get the transaction state
            TxnState state = txnState.get();
            if (state == null) {
                throw new JenaTransactionException("Not in a transaction");
            }
            
            try {
                // If this is a write transaction, publish the changes
                if (state.readWrite == ReadWrite.WRITE && state.collector != null) {
                    
                    // Measure the time to create the patch
                    RDFPatch patch = createPatchTime.record(() -> {
                        // Create a patch from the changes
                        return state.collector.getRDFPatch();
                    });
                    
                    // Check if there are any actual changes
                    if (!RDFPatchOps.isEmptyPatch(patch)) {
                        try {
                            // Commit the transaction on the wrapped dataset
                            getWrapped().commit();
                            
                            // Publish the patch to the Delta server
                            DeltaLink link = client.getDeltaLink();
                            
                            Id newId = Id.fromString(link.append(datasetName, patch));
                            currentVersion = newId;
                            lastSyncTime = System.currentTimeMillis();
                            patchCounter.incrementAndGet();
                            patchesOut.increment();
                            
                            LOG.debug("Published patch for {}: {}", datasetName, newId);
                        } catch (Exception e) {
                            LOG.error("Error publishing patch for {}", datasetName, e);
                            throw new DeltaException("Error publishing patch: " + e.getMessage(), e);
                        }
                    } else {
                        // No changes, just commit the wrapped transaction
                        getWrapped().commit();
                    }
                } else {
                    // Not a write transaction, just commit the wrapped transaction
                    getWrapped().commit();
                }
                
                // Update metrics
                transactionCount.increment();
                
            } finally {
                // Clean up the transaction state
                txnState.remove();
            }
        });
    }

    @Override
    public void abort() {
        // Get the transaction state
        TxnState state = txnState.get();
        if (state == null) {
            throw new JenaTransactionException("Not in a transaction");
        }
        
        try {
            // Abort the transaction on the wrapped dataset
            getWrapped().abort();
        } finally {
            // Clean up the transaction state
            txnState.remove();
        }
    }

    @Override
    public void end() {
        // Get the transaction state
        TxnState state = txnState.get();
        if (state == null) {
            return;  // Not in a transaction
        }
        
        try {
            // End the transaction on the wrapped dataset
            getWrapped().end();
        } finally {
            // Clean up the transaction state
            txnState.remove();
        }
    }

    @Override
    public ReadWrite transactionMode() {
        TxnState state = txnState.get();
        if (state == null) {
            return null;
        }
        return state.readWrite;
    }

    @Override
    public TxnType transactionType() {
        TxnState state = txnState.get();
        if (state == null) {
            return null;
        }
        return state.type;
    }

    @Override
    public boolean isInTransaction() {
        return txnState.get() != null;
    }
    
    /**
     * Class to hold the state of a transaction.
     */
    private static class TxnState {
        ReadWrite readWrite;
        TxnType type;
        RDFChangesCollector collector;
        
        TxnState(ReadWrite readWrite) {
            this.readWrite = readWrite;
            this.type = readWrite == ReadWrite.READ ? TxnType.READ : TxnType.WRITE;
        }
    }
}
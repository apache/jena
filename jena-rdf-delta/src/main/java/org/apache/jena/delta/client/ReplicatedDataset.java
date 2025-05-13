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

package org.apache.jena.delta.client;

import java.util.Objects;

import org.apache.jena.delta.DeltaException;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.changes.RDFChangesCollector;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dataset that is replicated using RDF Delta.
 * <p>
 * This wraps an underlying dataset and synchronizes changes with a patch log server.
 * All changes to the dataset are recorded as patches and published to the patch log server.
 * The dataset also receives and applies changes from other instances.
 */
public class ReplicatedDataset extends DatasetGraphWrapper implements PatchLogListener {
    private static final Logger LOG = LoggerFactory.getLogger(ReplicatedDataset.class);
    
    private final DeltaClient client;
    private final String datasetName;
    private final DeltaClient.PatchLog patchLog;
    private RDFChangesCollector collector;
    
    /**
     * Create a new ReplicatedDataset.
     * @param client The DeltaClient
     * @param datasetName The name of the dataset
     * @param base The underlying dataset
     */
    private ReplicatedDataset(DeltaClient client, String datasetName, DatasetGraph base) {
        super(base);
        this.client = Objects.requireNonNull(client, "DeltaClient must not be null");
        this.datasetName = Objects.requireNonNull(datasetName, "Dataset name must not be null");
        this.patchLog = client.getPatchLog(datasetName);
        this.patchLog.register(this);
    }
    
    /**
     * Create a new ReplicatedDataset.
     * @param client The DeltaClient
     * @param datasetName The name of the dataset
     * @param base The underlying dataset
     * @return The new ReplicatedDataset
     */
    public static DatasetGraph create(DeltaClient client, String datasetName, DatasetGraph base) {
        return new ReplicatedDataset(client, datasetName, base);
    }
    
    /**
     * Get the name of the dataset.
     */
    public String getDatasetName() {
        return datasetName;
    }
    
    /**
     * Get the DeltaClient.
     */
    public DeltaClient getClient() {
        return client;
    }
    
    /**
     * Get the PatchLog.
     */
    public DeltaClient.PatchLog getPatchLog() {
        return patchLog;
    }
    
    @Override
    public void begin(TxnType type) {
        // If this is a write transaction, set up the change collector
        if (type == TxnType.WRITE || type == TxnType.READ_PROMOTE || type == TxnType.READ_COMMITTED_PROMOTE) {
            collector = new RDFChangesCollector();
        }
        super.begin(type);
    }
    
    @Override
    public void begin(ReadWrite readWrite) {
        // If this is a write transaction, set up the change collector
        if (readWrite == ReadWrite.WRITE) {
            collector = new RDFChangesCollector();
        }
        super.begin(readWrite);
    }
    
    @Override
    public void commit() {
        // If this is a write transaction, publish the changes
        if (collector != null) {
            // Create a patch from the changes
            RDFPatch patch = collector.getRDFPatch();
            
            // Check if there are any actual changes
            if (RDFPatchOps.isNoop(patch)) {
                // No changes, just commit the underlying transaction
                super.commit();
                collector = null;
                return;
            }
            
            try {
                // Commit the underlying transaction
                super.commit();
                
                // Publish the patch
                client.getDeltaLink().append(datasetName, patch, patchLog.getCurrentVersion());
                
                LOG.debug("Published patch for {}: {}", datasetName, patch.getId());
            } catch (Exception e) {
                // Log the error and rethrow
                LOG.error("Error publishing patch for {}", datasetName, e);
                throw new DeltaException("Error publishing patch", e);
            } finally {
                collector = null;
            }
        } else {
            // Not a write transaction, just commit
            super.commit();
        }
    }
    
    @Override
    public void abort() {
        // Clear the collector if this is a write transaction
        if (collector != null) {
            collector = null;
        }
        super.abort();
    }
    
    @Override
    public void end() {
        // Clear the collector if this is a write transaction
        if (collector != null) {
            collector = null;
        }
        super.end();
    }
    
    @Override
    public void patchLogChanged(RDFPatch patch) {
        LOG.debug("Patch log {} changed: {}", datasetName, patch.getId());
        
        // Apply the patch to the underlying dataset
        // We need to do this in a transaction
        super.begin(ReadWrite.WRITE);
        try {
            // Apply the patch
            RDFPatchOps.applyChange(patch, getWrapped());
            
            // Commit the changes
            super.commit();
        } catch (Exception e) {
            LOG.error("Error applying patch to {}", datasetName, e);
            super.abort();
            throw new DeltaException("Error applying patch", e);
        }
    }
}
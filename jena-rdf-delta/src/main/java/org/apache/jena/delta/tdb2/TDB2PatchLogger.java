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

package org.apache.jena.delta.tdb2;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionListener;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.client.DeltaLink;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfpatch.PatchHeader;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.changes.RDFChangesCollector;
import org.apache.jena.sys.SystemARQ;
import org.apache.jena.system.ThreadAction;
import org.apache.jena.system.ThreadTxn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener for TDB2 transactions that logs changes as RDF patches to a patch log server.
 * This class combines the functionality of a TransactionListener and RDFChanges to track
 * changes made to a TDB2 dataset and send them to an RDF Delta server.
 */
public class TDB2PatchLogger implements TransactionListener, RDFChanges {
    private static final Logger LOG = LoggerFactory.getLogger(TDB2PatchLogger.class);
    
    // The link to the patch log server
    private final DeltaLink deltaLink;
    // The ID of the dataset in the patch log server
    private final String datasetId;
    // Collector for changes made in the current transaction
    private ThreadLocal<RDFChangesCollector> collectors = new ThreadLocal<>();
    // Tracks the status of the current transaction
    private ThreadLocal<AtomicBoolean> txnActive = new ThreadLocal<>();

    /**
     * Create a TDB2PatchLogger that logs changes to a patch log server.
     * 
     * @param deltaLink The link to the patch log server
     * @param datasetId The ID of the dataset in the patch log server
     */
    public TDB2PatchLogger(DeltaLink deltaLink, String datasetId) {
        this.deltaLink = deltaLink;
        this.datasetId = datasetId;
    }

    // TransactionListener methods
    
    @Override
    public void notifyTxnStart(Transaction transaction) {
        // Initialize the collector for this transaction
        RDFChangesCollector collector = new RDFChangesCollector();
        collectors.set(collector);
        // Mark transaction as active
        AtomicBoolean active = new AtomicBoolean(true);
        txnActive.set(active);
        // Start the transaction in the collector
        collector.txnBegin();
    }

    @Override
    public void notifyCommitStart(Transaction transaction) {
        // Nothing to do
    }

    @Override
    public void notifyCommitFinish(Transaction transaction) {
        try {
            RDFChangesCollector collector = collectors.get();
            if (collector != null && isActive()) {
                collector.txnCommit();
                
                // Create a patch from the collected changes
                RDFPatch patch = collector.getRDFPatch();
                
                // If there are actual changes, send them to the patch log server
                if (!RDFPatchOps.isEmptyPatch(patch)) {
                    try {
                        // Add any system metadata to the patch header
                        PatchHeader header = PatchHeader.create();
                        
                        // Apply the header to the patch
                        patch = RDFPatchOps.withHeader(patch, header);
                        
                        // Send the patch to the Delta server
                        deltaLink.append(datasetId, patch);
                        
                        if (LOG.isDebugEnabled())
                            LOG.debug("Appended patch to Delta server for dataset " + datasetId);
                    } catch (Exception ex) {
                        LOG.error("Failed to append patch to Delta server: " + ex.getMessage(), ex);
                        throw new DeltaException("Failed to append patch: " + ex.getMessage(), ex);
                    }
                }
            }
        } finally {
            // Clean up the thread locals
            collectors.remove();
            txnActive.remove();
        }
    }

    @Override
    public void notifyAbortStart(Transaction transaction) {
        // Nothing to do
    }

    @Override
    public void notifyAbortFinish(Transaction transaction) {
        try {
            RDFChangesCollector collector = collectors.get();
            if (collector != null && isActive()) {
                collector.txnAbort();
            }
        } finally {
            // Clean up the thread locals
            collectors.remove();
            txnActive.remove();
        }
    }

    // RDFChanges methods
    
    @Override
    public void txnBegin() {
        // Handled by notifyTxnStart
    }

    @Override
    public void txnCommit() {
        // Handled by notifyCommitFinish
    }

    @Override
    public void txnAbort() {
        // Handled by notifyAbortFinish
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        if (isActive()) {
            RDFChangesCollector collector = collectors.get();
            if (collector != null) {
                collector.add(g, s, p, o);
            }
        }
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        if (isActive()) {
            RDFChangesCollector collector = collectors.get();
            if (collector != null) {
                collector.delete(g, s, p, o);
            }
        }
    }

    @Override
    public void addPrefix(Node gn, String prefix, String uriStr) {
        if (isActive()) {
            RDFChangesCollector collector = collectors.get();
            if (collector != null) {
                collector.addPrefix(gn, prefix, uriStr);
            }
        }
    }

    @Override
    public void deletePrefix(Node gn, String prefix) {
        if (isActive()) {
            RDFChangesCollector collector = collectors.get();
            if (collector != null) {
                collector.deletePrefix(gn, prefix);
            }
        }
    }

    @Override
    public void header(String field, Node value) {
        if (isActive()) {
            RDFChangesCollector collector = collectors.get();
            if (collector != null) {
                collector.header(field, value);
            }
        }
    }

    @Override
    public void segment() {
        if (isActive()) {
            RDFChangesCollector collector = collectors.get();
            if (collector != null) {
                collector.segment();
            }
        }
    }

    /**
     * Check if the current transaction is active
     */
    private boolean isActive() {
        AtomicBoolean active = txnActive.get();
        return active != null && active.get();
    }
}
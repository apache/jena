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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.TransactionalSystem;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.system.Id;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for RDF Delta patch log server.
 */
public class DeltaClient implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DeltaClient.class);
    
    private final DeltaLink link;
    private final Map<String, PatchLog> patchLogs = new HashMap<>();
    private final ScheduledExecutorService executor;
    private final String zone;
    
    /**
     * Create a new DeltaClient.
     * @param link The DeltaLink to use for communication
     */
    private DeltaClient(DeltaLink link) {
        this(link, "client-" + System.currentTimeMillis());
    }
    
    /**
     * Create a new DeltaClient with a specific zone.
     * @param link The DeltaLink to use for communication
     * @param zone The zone for this client
     */
    private DeltaClient(DeltaLink link, String zone) {
        this.link = Objects.requireNonNull(link, "DeltaLink must not be null");
        this.zone = zone;
        this.executor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "DeltaClient-" + zone);
            t.setDaemon(true);
            return t;
        });
        
        // Start the polling for changes
        executor.scheduleWithFixedDelay(this::pollForChanges, 1, 1, TimeUnit.SECONDS);
    }
    
    /**
     * Create a new DeltaClient.
     * @param link The DeltaLink to use for communication
     * @return The new DeltaClient
     */
    public static DeltaClient create(DeltaLink link) {
        return new DeltaClient(link);
    }
    
    /**
     * Create a new DeltaClient with a specific zone.
     * @param link The DeltaLink to use for communication
     * @param zone The zone for this client
     * @return The new DeltaClient
     */
    public static DeltaClient create(DeltaLink link, String zone) {
        return new DeltaClient(link, zone);
    }
    
    /**
     * Create a DeltaLink to a patch log server.
     * @param serverURL The URL of the patch log server
     * @return The new DeltaLink
     */
    public static DeltaLinkHTTP createDeltaLink(String serverURL) {
        return new DeltaLinkHTTP(serverURL);
    }
    
    /**
     * Get the DeltaLink used by this client.
     */
    public DeltaLink getDeltaLink() {
        return link;
    }
    
    /**
     * Get the zone for this client.
     */
    public String getZone() {
        return zone;
    }
    
    /**
     * Get a list of all available datasets.
     */
    public List<String> listDatasets() {
        return link.listDatasets();
    }
    
    /**
     * Get a PatchLog for a dataset.
     * @param dsName The dataset name
     * @return The PatchLog
     */
    public synchronized PatchLog getPatchLog(String dsName) {
        return patchLogs.computeIfAbsent(dsName, name -> {
            PatchLogInfo info = link.getPatchLogInfo(name);
            if (info == null) {
                throw new DeltaException("Dataset not found: " + name);
            }
            return new PatchLog(this, name, info.getVersion());
        });
    }
    
    /**
     * Create a new dataset.
     * @param dsName The dataset name
     * @return The initial version
     */
    public Id createDataset(String dsName) {
        return link.createDataset(dsName);
    }
    
    /**
     * Poll for changes to the patch logs.
     */
    private void pollForChanges() {
        try {
            synchronized (this) {
                for (PatchLog patchLog : patchLogs.values()) {
                    patchLog.checkForChanges();
                }
            }
        } catch (Exception e) {
            LOG.error("Error polling for changes", e);
        }
    }
    
    @Override
    public void close() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        link.close();
    }
    
    /**
     * Class representing a patch log.
     */
    public class PatchLog {
        private final DeltaClient client;
        private final String name;
        private Id currentVersion;
        
        /**
         * Create a new PatchLog.
         * @param client The DeltaClient
         * @param name The dataset name
         * @param initialVersion The initial version
         */
        private PatchLog(DeltaClient client, String name, Id initialVersion) {
            this.client = client;
            this.name = name;
            this.currentVersion = initialVersion;
        }
        
        /**
         * Get the name of the dataset.
         */
        public String getName() {
            return name;
        }
        
        /**
         * Get the current version of the patch log.
         */
        public Id getCurrentVersion() {
            return currentVersion;
        }
        
        /**
         * Register a listener for patch log changes.
         */
        public void register(PatchLogListener listener) {
            client.link.register(listener);
        }
        
        /**
         * Unregister a listener for patch log changes.
         */
        public void unregister(PatchLogListener listener) {
            client.link.unregister(listener);
        }
        
        /**
         * Check for changes to the patch log.
         */
        void checkForChanges() {
            PatchLogInfo info = client.link.getPatchLogInfo(name);
            if (info == null) {
                LOG.error("Dataset no longer exists: {}", name);
                return;
            }
            
            if (!info.getVersion().equals(currentVersion)) {
                LOG.debug("Patch log {} has changed from {} to {}", name, currentVersion, info.getVersion());
                
                // Fetch the new patches
                List<RDFPatch> patches = client.link.fetch(name, currentVersion);
                
                // Process the patches
                for (RDFPatch patch : patches) {
                    // Update the current version
                    currentVersion = patch.getId();
                }
            }
        }
    }
}
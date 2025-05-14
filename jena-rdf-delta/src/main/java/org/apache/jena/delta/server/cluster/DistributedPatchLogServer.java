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

package org.apache.jena.delta.server.cluster;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.client.DeltaLink;
import org.apache.jena.delta.client.DeltaLinkHTTP;
import org.apache.jena.delta.server.PatchLogServer;
import org.apache.jena.delta.server.cluster.ZooKeeperCoordinator.ServerInfo;
import org.apache.jena.delta.server.local.FileStore;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.system.Id;
import org.apache.jena.riot.web.HttpOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * A distributed patch log server that uses ZooKeeper for coordination.
 * 
 * This server can operate in a cluster of servers, with each server
 * responsible for a subset of datasets. Leadership for each dataset
 * is managed by ZooKeeper, and operations are forwarded to the leader
 * when necessary.
 */
public class DistributedPatchLogServer implements PatchLogServer, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DistributedPatchLogServer.class);
    
    private final String serverId;
    private final String serverUrl;
    private final ZooKeeperCoordinator zkCoordinator;
    private final PatchLogServer localServer;
    private final Map<String, Boolean> leadershipStatus = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor;
    private final Duration syncInterval;
    
    // Metrics
    private final MeterRegistry registry;
    private final Map<String, Timer> operationTimers = new HashMap<>();
    private final Map<String, Counter> operationCounters = new HashMap<>();
    
    /**
     * Create a new DistributedPatchLogServer.
     * 
     * @param zkConnectString ZooKeeper connection string
     * @param zkSessionTimeout ZooKeeper session timeout in milliseconds
     * @param serverUrl URL where this server can be accessed
     * @param localStorePath Path to local storage for patch logs
     * @param registry Meter registry for metrics
     */
    public DistributedPatchLogServer(String zkConnectString, int zkSessionTimeout, 
                                     String serverUrl, String localStorePath,
                                     MeterRegistry registry) {
        this.serverId = generateServerId();
        this.serverUrl = serverUrl;
        this.registry = registry;
        this.localServer = new FileStore(localStorePath);
        this.executor = Executors.newScheduledThreadPool(2);
        this.syncInterval = Duration.ofSeconds(30);
        
        // Initialize metrics
        initMetrics();
        
        // Connect to ZooKeeper
        this.zkCoordinator = new ZooKeeperCoordinator(zkConnectString, zkSessionTimeout, serverId, serverUrl);
        
        // Register existing datasets
        registerExistingDatasets();
        
        // Start background synchronization
        startBackgroundSync();
        
        LOG.info("DistributedPatchLogServer started: id={}, url={}", serverId, serverUrl);
    }
    
    /**
     * Generate a unique server ID.
     */
    private String generateServerId() {
        return "server-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Initialize metrics.
     */
    private void initMetrics() {
        // Operation timers
        operationTimers.put("list", Timer.builder("rdf_delta_server_list_time")
            .description("Time to list patch logs")
            .register(registry));
        
        operationTimers.put("create", Timer.builder("rdf_delta_server_create_time")
            .description("Time to create a patch log")
            .register(registry));
        
        operationTimers.put("info", Timer.builder("rdf_delta_server_info_time")
            .description("Time to get patch log info")
            .register(registry));
        
        operationTimers.put("append", Timer.builder("rdf_delta_server_append_time")
            .description("Time to append a patch")
            .register(registry));
        
        operationTimers.put("getPatches", Timer.builder("rdf_delta_server_get_patches_time")
            .description("Time to get patches")
            .register(registry));
        
        operationTimers.put("getPatch", Timer.builder("rdf_delta_server_get_patch_time")
            .description("Time to get a patch")
            .register(registry));
        
        // Operation counters
        operationCounters.put("list", Counter.builder("rdf_delta_server_list_count")
            .description("Count of list operations")
            .register(registry));
        
        operationCounters.put("create", Counter.builder("rdf_delta_server_create_count")
            .description("Count of create operations")
            .register(registry));
        
        operationCounters.put("info", Counter.builder("rdf_delta_server_info_count")
            .description("Count of info operations")
            .register(registry));
        
        operationCounters.put("append", Counter.builder("rdf_delta_server_append_count")
            .description("Count of append operations")
            .register(registry));
        
        operationCounters.put("getPatches", Counter.builder("rdf_delta_server_get_patches_count")
            .description("Count of get patches operations")
            .register(registry));
        
        operationCounters.put("getPatch", Counter.builder("rdf_delta_server_get_patch_count")
            .description("Count of get patch operations")
            .register(registry));
    }
    
    /**
     * Register existing datasets with ZooKeeper.
     */
    private void registerExistingDatasets() {
        // Get existing datasets
        List<LogEntry> logs = localServer.listPatchLogs();
        
        // Register each one
        for (LogEntry log : logs) {
            String name = log.getName();
            Id version = log.getHead();
            
            // Register with ZooKeeper
            zkCoordinator.registerDataset(name, version);
            
            // Set up watcher
            zkCoordinator.watchLeadership(name, this::onLeadershipChange);
        }
    }
    
    /**
     * Handle a change in leadership status.
     */
    private void onLeadershipChange(String datasetName, boolean isLeader) {
        Boolean currentStatus = leadershipStatus.get(datasetName);
        if (currentStatus == null || currentStatus != isLeader) {
            leadershipStatus.put(datasetName, isLeader);
            
            if (isLeader) {
                LOG.info("Became leader for dataset: {}", datasetName);
            } else {
                LOG.info("Lost leadership for dataset: {}", datasetName);
            }
        }
    }
    
    /**
     * Start background synchronization of datasets.
     */
    private void startBackgroundSync() {
        executor.scheduleWithFixedDelay(this::synchronizeDatasets, 
                                       syncInterval.toMillis(), 
                                       syncInterval.toMillis(), 
                                       TimeUnit.MILLISECONDS);
    }
    
    /**
     * Synchronize datasets with other servers in the cluster.
     */
    private void synchronizeDatasets() {
        try {
            // Get local datasets
            List<LogEntry> localLogs = localServer.listPatchLogs();
            Map<String, Id> localVersions = new HashMap<>();
            
            for (LogEntry log : localLogs) {
                localVersions.put(log.getName(), log.getHead());
            }
            
            // Get other servers
            List<ServerInfo> servers = zkCoordinator.listServers();
            
            // Sync with each server
            for (ServerInfo server : servers) {
                // Skip ourselves
                if (serverId.equals(server.getId())) {
                    continue;
                }
                
                try {
                    // Connect to the server
                    DeltaLink link = DeltaLinkHTTP.connect(server.getUrl());
                    
                    // Get remote datasets
                    List<String> remoteDatasets = link.listDatasets();
                    
                    for (String datasetName : remoteDatasets) {
                        // Check if we have this dataset
                        if (!localVersions.containsKey(datasetName)) {
                            // New dataset, create it locally
                            createPatchLog(datasetName);
                            localVersions.put(datasetName, localServer.getPatchLogInfo(datasetName).getHead());
                        }
                        
                        // Get the local version
                        Id localVersion = localVersions.get(datasetName);
                        
                        // Check if we're the leader
                        if (isLeader(datasetName)) {
                            // We're the leader, we don't need to sync
                            continue;
                        }
                        
                        // Get the remote version
                        Id remoteVersion = Id.fromString(link.getDatasetVersion(datasetName));
                        
                        // Check if we need to sync
                        if (!remoteVersion.equals(localVersion)) {
                            // Get missing patches
                            Iterable<RDFPatch> patches = link.getPatches(datasetName, localVersion);
                            
                            // Apply missing patches
                            for (RDFPatch patch : patches) {
                                // Skip if we already have this patch
                                if (localServer.getPatch(datasetName, patch.getId()) == null) {
                                    localServer.append(datasetName, patch, localVersion);
                                    localVersion = patch.getId();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to sync with server: {}", server.getId(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Error during dataset synchronization", e);
        }
    }
    
    /**
     * Check if this server is the leader for a dataset.
     */
    private boolean isLeader(String name) {
        Boolean status = leadershipStatus.get(name);
        return status != null && status;
    }
    
    /**
     * Get the leader for a dataset.
     */
    private ServerInfo getDatasetLeader(String name) {
        return zkCoordinator.getLeader(name);
    }
    
    /**
     * Forward an operation to the leader.
     */
    private <T> T forwardToLeader(String name, String operation, ForwardedOperation<T> op) {
        ServerInfo leader = getDatasetLeader(name);
        
        if (leader == null) {
            throw new DeltaException("No leader found for dataset: " + name);
        }
        
        if (serverId.equals(leader.getId())) {
            throw new DeltaException("This server should be the leader, but leadership check failed");
        }
        
        LOG.debug("Forwarding {} operation for {} to leader: {}", operation, name, leader.getId());
        
        try {
            DeltaLink link = DeltaLinkHTTP.connect(leader.getUrl());
            return op.execute(link);
        } catch (Exception e) {
            LOG.error("Failed to forward {} operation for {} to leader", operation, name, e);
            throw new DeltaException("Failed to forward operation to leader: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<LogEntry> listPatchLogs() {
        operationCounters.get("list").increment();
        return operationTimers.get("list").record(() -> {
            // Always use local server for list operation
            return localServer.listPatchLogs();
        });
    }
    
    @Override
    public Id createPatchLog(String name) {
        operationCounters.get("create").increment();
        return operationTimers.get("create").record(() -> {
            // Create locally
            Id id = localServer.createPatchLog(name);
            
            // Register with ZooKeeper
            zkCoordinator.registerDataset(name, id);
            
            // Set up watcher
            zkCoordinator.watchLeadership(name, this::onLeadershipChange);
            
            // Try to become leader
            zkCoordinator.tryBecomeLeader(name);
            
            return id;
        });
    }
    
    @Override
    public LogEntry getPatchLogInfo(String name) {
        operationCounters.get("info").increment();
        return operationTimers.get("info").record(() -> {
            // Always use local server for info operation
            return localServer.getPatchLogInfo(name);
        });
    }
    
    @Override
    public Id append(String name, RDFPatch patch, Id expected) {
        operationCounters.get("append").increment();
        return operationTimers.get("append").record(() -> {
            // Check if we're the leader
            if (!isLeader(name)) {
                // Forward to leader
                return forwardToLeader(name, "append", link -> {
                    return Id.fromString(link.append(name, patch));
                });
            }
            
            // We're the leader, append locally
            Id id = localServer.append(name, patch, expected);
            
            // Update version in ZooKeeper
            zkCoordinator.updateDatasetVersion(name, id);
            
            return id;
        });
    }
    
    @Override
    public Iterable<RDFPatch> getPatches(String name, Id start) {
        operationCounters.get("getPatches").increment();
        return operationTimers.get("getPatches").record(() -> {
            // Try to get patches locally first
            try {
                return localServer.getPatches(name, start);
            } catch (Exception e) {
                // If local retrieval fails, try to get from leader
                if (!isLeader(name)) {
                    return forwardToLeader(name, "getPatches", link -> {
                        return link.getPatches(name, start);
                    });
                }
                throw e;
            }
        });
    }
    
    @Override
    public RDFPatch getPatch(String name, Id id) {
        operationCounters.get("getPatch").increment();
        return operationTimers.get("getPatch").record(() -> {
            // Try to get patch locally first
            RDFPatch patch = localServer.getPatch(name, id);
            
            if (patch == null && !isLeader(name)) {
                // If not found and we're not the leader, try to get from leader
                patch = forwardToLeader(name, "getPatch", link -> {
                    try {
                        return link.getPatch(name, id);
                    } catch (Exception e) {
                        return null;
                    }
                });
                
                // If patch was found from leader, store it locally
                if (patch != null) {
                    LogEntry logInfo = localServer.getPatchLogInfo(name);
                    if (logInfo != null) {
                        try {
                            localServer.append(name, patch, null);
                        } catch (Exception e) {
                            LOG.warn("Failed to store patch locally: dataset={}, id={}", name, id, e);
                        }
                    }
                }
            }
            
            return patch;
        });
    }
    
    @Override
    public void close() throws Exception {
        if (executor != null) {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
        
        if (zkCoordinator != null) {
            zkCoordinator.close();
        }
    }
    
    /**
     * Interface for operations that are forwarded to the leader.
     */
    private interface ForwardedOperation<T> {
        T execute(DeltaLink link) throws Exception;
    }
}
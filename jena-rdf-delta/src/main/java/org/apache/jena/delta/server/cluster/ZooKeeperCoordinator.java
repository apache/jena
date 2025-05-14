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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.rdfpatch.system.Id;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates RDF Delta servers in a cluster using ZooKeeper.
 * 
 * This class:
 * 1. Manages server registration in the cluster
 * 2. Tracks active servers
 * 3. Elects a leader for each dataset
 * 4. Provides coordination for distributed operations
 */
public class ZooKeeperCoordinator implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperCoordinator.class);
    
    private static final String ZK_ROOT = "/jena-delta";
    private static final String ZK_SERVERS = ZK_ROOT + "/servers";
    private static final String ZK_DATASETS = ZK_ROOT + "/datasets";
    private static final String ZK_LEADERS = ZK_ROOT + "/leaders";
    
    private final ZooKeeper zk;
    private final String serverId;
    private final String serverPath;
    private final String serverUrl;
    private final Map<String, BiConsumer<String, Boolean>> datasetWatchers = new HashMap<>();
    
    /**
     * Create a new ZooKeeperCoordinator and connect to ZooKeeper.
     * 
     * @param connectString ZooKeeper connection string (e.g., "localhost:2181")
     * @param sessionTimeout ZooKeeper session timeout in milliseconds
     * @param serverId Unique ID for this server
     * @param serverUrl URL where this server can be accessed
     */
    public ZooKeeperCoordinator(String connectString, int sessionTimeout, String serverId, String serverUrl) {
        this.serverId = serverId;
        this.serverUrl = serverUrl;
        this.serverPath = ZK_SERVERS + "/" + serverId;
        
        try {
            // Connect to ZooKeeper
            CountDownLatch connectedSignal = new CountDownLatch(1);
            this.zk = new ZooKeeper(connectString, sessionTimeout, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            });
            
            // Wait for connection
            if (!connectedSignal.await(sessionTimeout, TimeUnit.MILLISECONDS)) {
                throw new DeltaException("Failed to connect to ZooKeeper: " + connectString);
            }
            
            // Initialize ZooKeeper structure
            initZkStructure();
            
            // Register this server
            registerServer();
            
            LOG.info("Connected to ZooKeeper cluster: {}", connectString);
        } catch (Exception e) {
            throw new DeltaException("Failed to initialize ZooKeeper coordinator", e);
        }
    }
    
    /**
     * Initialize the ZooKeeper directory structure.
     */
    private void initZkStructure() throws KeeperException, InterruptedException {
        // Create root node if it doesn't exist
        createIfNotExists(ZK_ROOT, null, CreateMode.PERSISTENT);
        
        // Create servers node if it doesn't exist
        createIfNotExists(ZK_SERVERS, null, CreateMode.PERSISTENT);
        
        // Create datasets node if it doesn't exist
        createIfNotExists(ZK_DATASETS, null, CreateMode.PERSISTENT);
        
        // Create leaders node if it doesn't exist
        createIfNotExists(ZK_LEADERS, null, CreateMode.PERSISTENT);
    }
    
    /**
     * Helper method to create a ZooKeeper node if it doesn't exist.
     */
    private void createIfNotExists(String path, byte[] data, CreateMode mode) throws KeeperException, InterruptedException {
        try {
            zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
        } catch (KeeperException.NodeExistsException e) {
            // Node already exists, ignore
        }
    }
    
    /**
     * Register this server in the cluster.
     */
    private void registerServer() throws KeeperException, InterruptedException {
        // Create server info
        JsonObject serverInfo = new JsonObject();
        serverInfo.put("url", serverUrl);
        serverInfo.put("timestamp", System.currentTimeMillis());
        
        byte[] data = serverInfo.toString().getBytes(StandardCharsets.UTF_8);
        
        // Register server with ephemeral node
        try {
            zk.create(serverPath, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            LOG.info("Registered server in cluster: {}", serverId);
        } catch (KeeperException.NodeExistsException e) {
            // Node already exists, update it
            zk.setData(serverPath, data, -1);
            LOG.info("Updated server registration in cluster: {}", serverId);
        }
    }
    
    /**
     * Get a list of all servers in the cluster.
     */
    public List<ServerInfo> listServers() {
        try {
            List<String> serverIds = zk.getChildren(ZK_SERVERS, false);
            List<ServerInfo> servers = new ArrayList<>();
            
            for (String id : serverIds) {
                String path = ZK_SERVERS + "/" + id;
                byte[] data = zk.getData(path, false, null);
                
                if (data != null && data.length > 0) {
                    JsonObject json = JSON.parse(new String(data, StandardCharsets.UTF_8));
                    String url = json.get("url").getAsString().value();
                    long timestamp = json.get("timestamp").getAsNumber().value().longValue();
                    
                    servers.add(new ServerInfo(id, url, timestamp));
                }
            }
            
            return servers;
        } catch (Exception e) {
            LOG.error("Failed to list servers", e);
            throw new DeltaException("Failed to list servers", e);
        }
    }
    
    /**
     * Register a dataset in the cluster.
     * 
     * @param datasetName Name of the dataset
     * @param version Current version (head) of the dataset
     */
    public void registerDataset(String datasetName, Id version) {
        try {
            String datasetPath = ZK_DATASETS + "/" + datasetName;
            
            // Create dataset info
            JsonObject datasetInfo = new JsonObject();
            datasetInfo.put("name", datasetName);
            datasetInfo.put("version", version.toString());
            datasetInfo.put("timestamp", System.currentTimeMillis());
            
            byte[] data = datasetInfo.toString().getBytes(StandardCharsets.UTF_8);
            
            // Register dataset
            try {
                zk.create(datasetPath, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOG.info("Registered dataset in cluster: {}", datasetName);
            } catch (KeeperException.NodeExistsException e) {
                // Node already exists, update it
                zk.setData(datasetPath, data, -1);
                LOG.info("Updated dataset registration in cluster: {}", datasetName);
            }
            
            // Try to become leader for this dataset
            tryBecomeLeader(datasetName);
            
        } catch (Exception e) {
            LOG.error("Failed to register dataset: {}", datasetName, e);
            throw new DeltaException("Failed to register dataset: " + datasetName, e);
        }
    }
    
    /**
     * Update the version of a dataset in the cluster.
     * 
     * @param datasetName Name of the dataset
     * @param version New version (head) of the dataset
     */
    public void updateDatasetVersion(String datasetName, Id version) {
        try {
            String datasetPath = ZK_DATASETS + "/" + datasetName;
            
            // Get current data
            byte[] currentData = zk.getData(datasetPath, false, null);
            JsonObject datasetInfo;
            
            if (currentData != null && currentData.length > 0) {
                datasetInfo = JSON.parse(new String(currentData, StandardCharsets.UTF_8));
            } else {
                datasetInfo = new JsonObject();
                datasetInfo.put("name", datasetName);
            }
            
            // Update version and timestamp
            datasetInfo.put("version", version.toString());
            datasetInfo.put("timestamp", System.currentTimeMillis());
            
            byte[] data = datasetInfo.toString().getBytes(StandardCharsets.UTF_8);
            
            // Update dataset
            zk.setData(datasetPath, data, -1);
            LOG.debug("Updated dataset version in cluster: {}, version={}", datasetName, version);
            
        } catch (Exception e) {
            LOG.error("Failed to update dataset version: {}", datasetName, e);
            throw new DeltaException("Failed to update dataset version: " + datasetName, e);
        }
    }
    
    /**
     * Try to become the leader for a dataset.
     * 
     * @param datasetName Name of the dataset
     * @return true if this server is now the leader, false otherwise
     */
    public boolean tryBecomeLeader(String datasetName) {
        try {
            String leaderPath = ZK_LEADERS + "/" + datasetName;
            
            // Create leader info
            JsonObject leaderInfo = new JsonObject();
            leaderInfo.put("serverId", serverId);
            leaderInfo.put("url", serverUrl);
            leaderInfo.put("timestamp", System.currentTimeMillis());
            
            byte[] data = leaderInfo.toString().getBytes(StandardCharsets.UTF_8);
            
            // Try to become leader
            try {
                zk.create(leaderPath, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                LOG.info("Became leader for dataset: {}", datasetName);
                return true;
            } catch (KeeperException.NodeExistsException e) {
                // Another server is already the leader
                return false;
            }
            
        } catch (Exception e) {
            LOG.error("Failed to try become leader for dataset: {}", datasetName, e);
            throw new DeltaException("Failed to try become leader for dataset: " + datasetName, e);
        }
    }
    
    /**
     * Check if this server is the leader for a dataset.
     * 
     * @param datasetName Name of the dataset
     * @return true if this server is the leader, false otherwise
     */
    public boolean isLeader(String datasetName) {
        try {
            String leaderPath = ZK_LEADERS + "/" + datasetName;
            
            byte[] data = zk.getData(leaderPath, false, null);
            if (data == null || data.length == 0) {
                return false;
            }
            
            JsonObject leaderInfo = JSON.parse(new String(data, StandardCharsets.UTF_8));
            String leaderId = leaderInfo.get("serverId").getAsString().value();
            
            return serverId.equals(leaderId);
            
        } catch (KeeperException.NoNodeException e) {
            // No leader yet
            return false;
        } catch (Exception e) {
            LOG.error("Failed to check leader for dataset: {}", datasetName, e);
            throw new DeltaException("Failed to check leader for dataset: " + datasetName, e);
        }
    }
    
    /**
     * Get the leader info for a dataset.
     * 
     * @param datasetName Name of the dataset
     * @return ServerInfo of the leader, or null if there is no leader
     */
    public ServerInfo getLeader(String datasetName) {
        try {
            String leaderPath = ZK_LEADERS + "/" + datasetName;
            
            byte[] data = zk.getData(leaderPath, false, null);
            if (data == null || data.length == 0) {
                return null;
            }
            
            JsonObject leaderInfo = JSON.parse(new String(data, StandardCharsets.UTF_8));
            String leaderId = leaderInfo.get("serverId").getAsString().value();
            String url = leaderInfo.get("url").getAsString().value();
            long timestamp = leaderInfo.get("timestamp").getAsNumber().value().longValue();
            
            return new ServerInfo(leaderId, url, timestamp);
            
        } catch (KeeperException.NoNodeException e) {
            // No leader yet
            return null;
        } catch (Exception e) {
            LOG.error("Failed to get leader for dataset: {}", datasetName, e);
            throw new DeltaException("Failed to get leader for dataset: " + datasetName, e);
        }
    }
    
    /**
     * Watch for changes to dataset leadership.
     * 
     * @param datasetName Name of the dataset to watch
     * @param callback Callback function that receives (datasetName, isLeader)
     */
    public void watchLeadership(String datasetName, BiConsumer<String, Boolean> callback) {
        try {
            // Store callback
            datasetWatchers.put(datasetName, callback);
            
            // Set up watcher
            watchLeadershipInternal(datasetName);
            
        } catch (Exception e) {
            LOG.error("Failed to watch leadership for dataset: {}", datasetName, e);
            throw new DeltaException("Failed to watch leadership for dataset: " + datasetName, e);
        }
    }
    
    /**
     * Internal method to set up a leadership watcher.
     */
    private void watchLeadershipInternal(String datasetName) {
        try {
            String leaderPath = ZK_LEADERS + "/" + datasetName;
            
            // Create watcher
            Watcher watcher = new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeCreated || 
                        event.getType() == Event.EventType.NodeDeleted ||
                        event.getType() == Event.EventType.NodeDataChanged) {
                        
                        // Check if we're the leader
                        boolean isLeader = isLeader(datasetName);
                        
                        // Notify callback
                        BiConsumer<String, Boolean> callback = datasetWatchers.get(datasetName);
                        if (callback != null) {
                            callback.accept(datasetName, isLeader);
                        }
                        
                        // Re-establish watcher
                        watchLeadershipInternal(datasetName);
                    }
                }
            };
            
            // Set watcher
            try {
                Stat stat = zk.exists(leaderPath, watcher);
                
                // If node doesn't exist, try to become leader
                if (stat == null) {
                    tryBecomeLeader(datasetName);
                }
                
            } catch (Exception e) {
                LOG.error("Error watching leadership for dataset: {}", datasetName, e);
            }
            
        } catch (Exception e) {
            LOG.error("Failed to watch leadership for dataset: {}", datasetName, e);
        }
    }
    
    @Override
    public void close() throws Exception {
        if (zk != null) {
            try {
                zk.close();
                LOG.info("Closed ZooKeeper connection");
            } catch (Exception e) {
                LOG.error("Error closing ZooKeeper connection", e);
            }
        }
    }
    
    /**
     * Information about a server in the cluster.
     */
    public static class ServerInfo {
        private final String id;
        private final String url;
        private final long timestamp;
        
        public ServerInfo(String id, String url, long timestamp) {
            this.id = id;
            this.url = url;
            this.timestamp = timestamp;
        }
        
        public String getId() {
            return id;
        }
        
        public String getUrl() {
            return url;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return "ServerInfo{id='" + id + "', url='" + url + "'}";
        }
    }
}
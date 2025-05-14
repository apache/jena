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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.DeltaVocab;
import org.apache.jena.delta.client.DeltaClient;
import org.apache.jena.delta.client.DeltaLink;
import org.apache.jena.delta.client.DeltaLinkHTTP;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.tdb2.TDB2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fuseki module for RDF Delta integration.
 * 
 * This module:
 * 1. Reads Delta configuration from Fuseki configuration
 * 2. Sets up connections to Delta patch log servers
 * 3. Manages dataset synchronization
 * 4. Provides high-availability capabilities to Fuseki
 */
public class DeltaFusekiModule implements FusekiModule {
    private static final Logger LOG = LoggerFactory.getLogger(DeltaFusekiModule.class);
    
    // System properties
    public static final String DELTA_ZONE = "delta.fuseki.zone";
    public static final String DELTA_SERVERS = "delta.fuseki.servers";
    
    // Default values
    private static final String DEFAULT_ZONE = generateZoneName();
    
    // Module state
    private Map<String, DeltaClient> clients = new HashMap<>();
    private Map<String, DeltaDatasetStatus> datasetStatus = new HashMap<>();
    private String zone;
    
    /**
     * Generate a zone name for this Fuseki server.
     * Uses the hostname plus a random UUID suffix.
     */
    private static String generateZoneName() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            return hostname + "-" + uuid;
        } catch (UnknownHostException e) {
            // Fall back to a UUID
            return "fuseki-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
    
    @Override
    public void prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) {
        // Initialize logging
        FusekiLogging.setLogging();
        
        // Get the zone from system property or use a default
        zone = System.getProperty(DELTA_ZONE, DEFAULT_ZONE);
        LOG.info("RDF Delta Fuseki module initialized with zone: {}", zone);
        
        // Get Delta server URLs from system property
        String deltaServers = System.getProperty(DELTA_SERVERS);
        if (deltaServers != null) {
            LOG.info("RDF Delta Fuseki module using servers: {}", deltaServers);
            // Connect to all specified servers
            String[] urls = deltaServers.split(",");
            for (String url : urls) {
                connectToDeltaServer(url.trim());
            }
        }
        
        // Get Delta server URLs from configuration
        getServersFromConfig(configModel);
    }
    
    /**
     * Extract Delta server URLs from Fuseki configuration.
     */
    private void getServersFromConfig(Model configModel) {
        StmtIterator it = configModel.listStatements(null, DeltaVocab.pDeltaServer, (RDFNode)null);
        while (it.hasNext()) {
            Node serverNode = it.next().getObject().asNode();
            if (serverNode.isURI()) {
                String serverUrl = serverNode.getURI();
                connectToDeltaServer(serverUrl);
            }
        }
    }
    
    /**
     * Connect to a Delta patch log server.
     */
    private void connectToDeltaServer(String serverUrl) {
        try {
            if (clients.containsKey(serverUrl)) {
                return;
            }
            
            // Create a connection to the server
            DeltaLink link = DeltaLinkHTTP.connect(serverUrl);
            DeltaClient client = DeltaClient.create(link, zone);
            
            clients.put(serverUrl, client);
            LOG.info("Connected to RDF Delta server: {}", serverUrl);
            
        } catch (Exception e) {
            LOG.error("Failed to connect to RDF Delta server: {}", serverUrl, e);
        }
    }

    @Override
    public void configured(FusekiServer.Builder serverBuilder, DataAccessPointRegistry dapRegistry, Model configModel) {
        // Look for Delta-enabled datasets in the configuration
        StmtIterator it = configModel.listStatements(null, DeltaVocab.datasetName, (RDFNode)null);
        while (it.hasNext()) {
            Resource dataset = it.next().getSubject();
            String datasetName = GraphUtils.getStringValue(dataset, DeltaVocab.datasetName);
            String serverUrl = GraphUtils.getStringValue(dataset, DeltaVocab.pDeltaServer);
            
            if (datasetName == null || serverUrl == null) {
                continue;
            }
            
            // Get the DeltaClient for this server
            DeltaClient client = clients.get(serverUrl);
            if (client == null) {
                // Try to connect to the server
                connectToDeltaServer(serverUrl);
                client = clients.get(serverUrl);
                
                if (client == null) {
                    LOG.error("No connection to RDF Delta server for dataset {}: {}", datasetName, serverUrl);
                    continue;
                }
            }
            
            // Find the corresponding DataAccessPoint
            DataAccessPoint dap = findDataAccessPoint(dapRegistry, datasetName);
            if (dap == null) {
                LOG.error("Dataset not found in Fuseki registry: {}", datasetName);
                continue;
            }
            
            // Get the dataset graph
            DatasetGraph dsg = dap.getDataService().getDataset();
            
            // Make sure the Delta dataset exists on the server
            ensureDeltaDatasetExists(client, datasetName);
            
            // Create a replicated dataset
            DeltaReplicatedDataset replicatedDataset = new DeltaReplicatedDataset(client, datasetName, dsg);
            
            // Replace the dataset in the DataService
            DataService dataService = dap.getDataService();
            dataService.setDataset(replicatedDataset);
            
            // Store status for monitoring
            datasetStatus.put(datasetName, new DeltaDatasetStatus(datasetName, serverUrl));
            
            LOG.info("Enabled RDF Delta replication for dataset {} with server {}", datasetName, serverUrl);
        }
    }
    
    /**
     * Find a DataAccessPoint by name.
     */
    private DataAccessPoint findDataAccessPoint(DataAccessPointRegistry dapRegistry, String name) {
        // Try with and without leading slash
        DataAccessPoint dap = dapRegistry.get(name);
        if (dap != null) {
            return dap;
        }
        
        // Try with leading slash
        if (!name.startsWith("/")) {
            dap = dapRegistry.get("/" + name);
            if (dap != null) {
                return dap;
            }
        }
        
        // Try without leading slash
        if (name.startsWith("/")) {
            dap = dapRegistry.get(name.substring(1));
            if (dap != null) {
                return dap;
            }
        }
        
        return null;
    }
    
    /**
     * Ensure a Delta dataset exists on the server.
     */
    private void ensureDeltaDatasetExists(DeltaClient client, String datasetName) {
        try {
            // Check if the dataset exists
            List<String> datasets = client.listDatasets();
            if (!datasets.contains(datasetName)) {
                // Create the dataset
                client.createDataset(datasetName);
                LOG.info("Created new Delta dataset: {}", datasetName);
            }
        } catch (Exception e) {
            LOG.error("Failed to ensure Delta dataset exists: {}", datasetName, e);
            throw new DeltaException("Failed to ensure Delta dataset exists: " + datasetName, e);
        }
    }

    @Override
    public void serverBeforeStarting(FusekiServer server) {
        // Nothing to do here
    }

    @Override
    public void serverAfterStarting(FusekiServer server) {
        LOG.info("RDF Delta Fuseki module started");
        LOG.info("Datasets with Delta replication: {}", datasetStatus.keySet());
    }

    @Override
    public void serverStopped(FusekiServer server) {
        // Close all Delta clients
        for (DeltaClient client : clients.values()) {
            try {
                client.close();
            } catch (Exception e) {
                LOG.error("Error closing Delta client", e);
            }
        }
        clients.clear();
        LOG.info("RDF Delta Fuseki module stopped");
    }
    
    /**
     * Status information for a Delta-enabled dataset.
     */
    private static class DeltaDatasetStatus {
        private final String name;
        private final String serverUrl;
        
        public DeltaDatasetStatus(String name, String serverUrl) {
            this.name = name;
            this.serverUrl = serverUrl;
        }
        
        public String getName() {
            return name;
        }
        
        public String getServerUrl() {
            return serverUrl;
        }
    }
}
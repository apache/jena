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

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.DeltaVocab;
import org.apache.jena.delta.client.DeltaClient;
import org.apache.jena.delta.client.DeltaLink;
import org.apache.jena.delta.client.DeltaLinkHTTP;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assembler for creating Delta-enabled datasets.
 * 
 * This assembler reads RDF statements to create datasets that are
 * synchronized with a Delta patch log server.
 */
public class DeltaDatasetAssembler extends AssemblerBase implements Assembler {
    private static final Logger LOG = LoggerFactory.getLogger(DeltaDatasetAssembler.class);
    
    @Override
    public Dataset open(Assembler a, Resource root, Mode mode) {
        // Get required properties
        String serverUrl = GraphUtils.getStringValue(root, DeltaVocab.pDeltaServer);
        if (serverUrl == null) {
            throw new DeltaException("Missing required property: " + DeltaVocab.pDeltaServer);
        }
        
        String datasetName = GraphUtils.getStringValue(root, DeltaVocab.pDatasetName);
        if (datasetName == null) {
            throw new DeltaException("Missing required property: " + DeltaVocab.pDatasetName);
        }
        
        // Get the base dataset resource
        Resource baseDatasetRes = GraphUtils.getResourceValue(root, DeltaVocab.pDataset);
        if (baseDatasetRes == null) {
            throw new DeltaException("Missing required property: " + DeltaVocab.pDataset);
        }
        
        // Optional zone name
        String zone = GraphUtils.getStringValue(root, DeltaVocab.zone);
        
        // Create the base dataset
        Dataset baseDataset = (Dataset)a.open(baseDatasetRes);
        
        // Connect to the Delta server
        DeltaLink link = DeltaLinkHTTP.connect(serverUrl);
        
        // Create the DeltaClient
        DeltaClient client;
        if (zone != null) {
            client = DeltaClient.create(link, zone);
        } else {
            client = DeltaClient.create(link);
        }
        
        // Make sure the Delta dataset exists
        try {
            if (!client.listDatasets().contains(datasetName)) {
                client.createDataset(datasetName);
                LOG.info("Created new Delta dataset: {}", datasetName);
            }
        } catch (Exception e) {
            throw new DeltaException("Failed to ensure Delta dataset exists: " + datasetName, e);
        }
        
        // Create the replicated dataset
        DatasetGraph baseGraph = baseDataset.asDatasetGraph();
        DeltaReplicatedDataset deltaDataset = new DeltaReplicatedDataset(client, datasetName, baseGraph);
        
        LOG.info("Created Delta-enabled dataset: {} with server {}", datasetName, serverUrl);
        return DatasetFactory.wrap(deltaDataset);
    }
}
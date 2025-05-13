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

package org.apache.jena.delta.tdb2.assembler;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.DeltaVocab;
import org.apache.jena.delta.client.DeltaClient;
import org.apache.jena.delta.client.DeltaLink;
import org.apache.jena.delta.client.DeltaLinkHTTP;
import org.apache.jena.delta.tdb2.TDB2DeltaConnection;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.tdb2.assembler.VocabTDB2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assembler for creating TDB2 datasets with RDF Delta change tracking.
 * This assembler extends the standard TDB2 dataset assembler to add capabilities
 * for connecting the dataset to an RDF Delta patch log server.
 */
public class TDB2DeltaAssembler extends AssemblerBase implements Assembler {
    private static final Logger LOG = LoggerFactory.getLogger(TDB2DeltaAssembler.class);
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode) {
        // Get the base TDB2 dataset
        Resource tdb2Resource = GraphUtils.getResourceValue(root, DeltaVocab.pDataset);
        if (tdb2Resource == null)
            throw new DeltaException("No tdb2:Dataset specified for delta:TDB2Dataset: " + root);
        
        // Get the assembler for TDB2 datasets
        Assembler tdb2Assembler = Assembler.general.open(root, VocabTDB2.tDatasetAssembler);
        if (tdb2Assembler == null)
            throw new DeltaException("No TDB2 dataset assembler found");
        
        // Create the TDB2 dataset
        Dataset ds = (Dataset)tdb2Assembler.open(a, tdb2Resource, mode);
        DatasetGraph dsg = ds.asDatasetGraph();
        
        // Get the Delta server URL
        String deltaServerURL = GraphUtils.getStringValue(root, DeltaVocab.pDeltaServer);
        if (deltaServerURL == null)
            throw new DeltaException("No delta:server specified for delta:TDB2Dataset: " + root);
        
        // Get the Delta dataset ID
        String datasetId = GraphUtils.getStringValue(root, DeltaVocab.pDatasetName);
        if (datasetId == null)
            throw new DeltaException("No delta:name specified for delta:TDB2Dataset: " + root);
        
        // Create the Delta link
        DeltaLink deltaLink = DeltaLinkHTTP.connect(deltaServerURL);
        
        // Connect the TDB2 dataset to the Delta server
        DatasetGraph dsgConnected = TDB2DeltaConnection.connect(dsg, deltaLink, datasetId);
        
        // Create a new dataset with the connected dataset graph
        Dataset dsConnected = DatasetFactory.wrap(dsgConnected);
        
        LOG.info("Created TDB2 dataset with Delta connection: {}", datasetId);
        return dsConnected;
    }
}
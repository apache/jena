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

package org.apache.jena.delta.server.assembler;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.delta.DeltaVocab;
import org.apache.jena.delta.client.DeltaClient;
import org.apache.jena.delta.client.DeltaLinkHTTP;
import org.apache.jena.delta.client.ReplicatedDataset;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;

/**
 * Assembler for creating replicated datasets with RDF Delta synchronization.
 */
public class ReplicatedDatasetAssembler extends AssemblerBase implements DatasetAssembler {
    
    public static class Factory implements org.apache.jena.assembler.AssemblerFactory {
        @Override
        public Assembler open(Assembler a, Resource root, Mode mode) {
            return new ReplicatedDatasetAssembler();
        }
    }
    
    @Override
    public Dataset createDataset(Assembler a, Resource root, Mode mode) {
        // Get the patch log server URL
        String patchLogServerURL = getStringValue(root, DeltaVocab.patchLogServer);
        if (patchLogServerURL == null)
            throw new AssemblerException(root, "No patch log server URL specified for ReplicatedDataset");
        
        // Get the dataset name
        String datasetName = getStringValue(root, DeltaVocab.datasetName);
        if (datasetName == null)
            throw new AssemblerException(root, "No dataset name specified for ReplicatedDataset");
        
        // Get the storage resource
        Statement storageStmt = root.getProperty(DeltaVocab.storage);
        if (storageStmt == null)
            throw new AssemblerException(root, "No storage specified for ReplicatedDataset");
        
        Resource storageResource = storageStmt.getResource();
        
        // Create the underlying dataset
        DatasetGraph dsg = createUnderlyingDataset(a, storageResource, mode);
        
        // Create the delta client
        DeltaLinkHTTP deltaLink = DeltaClient.createDeltaLink(patchLogServerURL);
        DeltaClient deltaClient = DeltaClient.create(deltaLink);
        
        // Create the replicated dataset
        DatasetGraph replicated = ReplicatedDataset.create(deltaClient, datasetName, dsg);
        
        return AssemblerUtils.makeDataset(replicated);
    }
    
    /**
     * Create the underlying dataset from the storage resource.
     */
    private DatasetGraph createUnderlyingDataset(Assembler a, Resource storageResource, Mode mode) {
        // Use the general assembler to create the underlying dataset
        Object obj = a.open(storageResource, mode);
        if (obj instanceof Dataset) {
            return ((Dataset)obj).asDatasetGraph();
        } else if (obj instanceof DatasetGraph) {
            return (DatasetGraph)obj;
        } else {
            throw new AssemblerException(storageResource, "Storage resource does not assemble to a dataset");
        }
    }
    
    /**
     * Get a string value from a property.
     */
    private String getStringValue(Resource root, org.apache.jena.rdf.model.Property property) {
        Statement stmt = root.getProperty(property);
        if (stmt == null)
            return null;
        
        if (stmt.getObject().isLiteral())
            return stmt.getString();
        
        if (stmt.getObject().isURIResource())
            return stmt.getResource().getURI();
        
        return stmt.getObject().toString();
    }
}
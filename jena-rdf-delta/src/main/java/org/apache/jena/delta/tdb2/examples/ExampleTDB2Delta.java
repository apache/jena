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

package org.apache.jena.delta.tdb2.examples;

import org.apache.jena.delta.client.DeltaLink;
import org.apache.jena.delta.client.DeltaLinkHTTP;
import org.apache.jena.delta.tdb2.TDB2DeltaConnection;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.vocabulary.RDF;

/**
 * Example showing how to use TDB2 with RDF Delta change tracking.
 * This example demonstrates:
 * 1. Creating a TDB2 dataset and connecting it to a Delta patch log server
 * 2. Making changes to the dataset that will be automatically logged
 * 3. Creating another replica dataset that syncs with the same patch log
 */
public class ExampleTDB2Delta {
    
    public static void main(String[] args) {
        // URL of the Delta patch log server
        String deltaServerURL = "http://localhost:1066/";
        
        // ID of the dataset in the patch log server
        String datasetId = "example";
        
        // Connect to the Delta server
        DeltaLink deltaLink = DeltaLinkHTTP.connect(deltaServerURL);
        
        // Create the dataset if it doesn't exist
        if (!deltaLink.listDatasets().contains(datasetId)) {
            deltaLink.newDataset(datasetId);
            System.out.println("Created new dataset in patch log: " + datasetId);
        }
        
        // ----------------
        // Writer process - this would typically run on one server
        // ----------------
        
        // Create a TDB2 dataset and connect it to the Delta server
        DatasetGraph dsgWriter = TDB2Factory.connectDataset("/tmp/tdb2-delta-example1");
        DatasetGraph dsgConnected = TDB2DeltaConnection.connect(dsgWriter, deltaLink, datasetId);
        Dataset dsWriter = DatasetFactory.wrap(dsgConnected);
        
        // Make some changes to the dataset
        dsWriter.begin(org.apache.jena.query.ReadWrite.WRITE);
        try {
            // Add some triples to the default graph
            dsWriter.getDefaultModel().add(
                dsWriter.getDefaultModel().createResource("http://example.org/resource1"),
                RDF.type,
                dsWriter.getDefaultModel().createResource("http://example.org/Type")
            );
            
            // Add some quads to a named graph
            dsgConnected.add(new Quad(
                NodeFactory.createURI("http://example.org/graph1"),
                NodeFactory.createURI("http://example.org/resource2"),
                RDF.type.asNode(),
                NodeFactory.createURI("http://example.org/Type")
            ));
            
            // These changes will be automatically logged to the Delta server when the transaction is committed
            dsWriter.commit();
            System.out.println("Writer committed changes");
        } finally {
            dsWriter.end();
        }
        
        // ----------------
        // Reader process - this would typically run on another server
        // ----------------
        
        // Create another TDB2 dataset connected to the same patch log
        DatasetGraph dsgReader = TDB2Factory.connectDataset("/tmp/tdb2-delta-example2");
        DatasetGraph dsgReader2 = TDB2DeltaConnection.connect(dsgReader, deltaLink, datasetId);
        Dataset dsReader = DatasetFactory.wrap(dsgReader2);
        
        // Read from the dataset - changes made by the writer will be automatically synchronized
        dsReader.begin(org.apache.jena.query.ReadWrite.READ);
        try {
            // Check if the changes are visible
            boolean hasTriple = dsReader.getDefaultModel().contains(
                dsReader.getDefaultModel().createResource("http://example.org/resource1"),
                RDF.type,
                dsReader.getDefaultModel().createResource("http://example.org/Type")
            );
            
            System.out.println("Reader sees the changes: " + hasTriple);
            
            // Count all triples in the dataset
            long tripleCount = dsReader.getDefaultModel().size();
            System.out.println("Default graph triple count: " + tripleCount);
            
            // Count all quads in the named graph
            long quadCount = dsReader.getNamedModel("http://example.org/graph1").size();
            System.out.println("Named graph quad count: " + quadCount);
            
        } finally {
            dsReader.end();
        }
        
        // Close datasets
        dsWriter.close();
        dsReader.close();
    }
}
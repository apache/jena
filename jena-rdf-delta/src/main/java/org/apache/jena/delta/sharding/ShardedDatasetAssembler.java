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

package org.apache.jena.delta.sharding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.DeltaVocab;
import org.apache.jena.delta.client.DeltaLink;
import org.apache.jena.delta.client.DeltaLinkHTTP;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assembler for sharded datasets.
 */
public class ShardedDatasetAssembler extends AssemblerBase implements Assembler {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDatasetAssembler.class);
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode) {
        // Get the sharding strategy
        Resource strategyRes = GraphUtils.getResourceValue(root, ShardingVocab.shardingStrategy);
        if (strategyRes == null) {
            throw new DeltaException("No sharding strategy specified for sharded dataset");
        }
        
        // Create the appropriate sharding strategy
        ShardingStrategy shardingStrategy = createShardingStrategy(a, strategyRes);
        
        // Get the shards
        List<Resource> shardResources = new ArrayList<>();
        StmtIterator it = root.listProperties(ShardingVocab.shards);
        while (it.hasNext()) {
            Statement stmt = it.next();
            RDFNode obj = stmt.getObject();
            if (obj.isResource()) {
                shardResources.add(obj.asResource());
            }
        }
        
        // Check shard count
        if (shardResources.size() != shardingStrategy.getShardCount()) {
            throw new DeltaException("Shard count mismatch: strategy has " + 
                shardingStrategy.getShardCount() + " shards, but " + 
                shardResources.size() + " shards specified");
        }
        
        // Create the shards
        DatasetGraph[] shards = new DatasetGraph[shardResources.size()];
        for (int i = 0; i < shardResources.size(); i++) {
            Resource shardRes = shardResources.get(i);
            int shardIndex = GraphUtils.getIntValue(shardRes, ShardingVocab.shardIndex);
            
            if (shardIndex < 0 || shardIndex >= shards.length) {
                throw new DeltaException("Shard index out of range: " + shardIndex);
            }
            
            Resource datasetRes = GraphUtils.getResourceValue(shardRes, ShardingVocab.shardDataset);
            if (datasetRes == null) {
                throw new DeltaException("No dataset specified for shard " + shardIndex);
            }
            
            Dataset dataset = (Dataset) a.open(datasetRes);
            shards[shardIndex] = dataset.asDatasetGraph();
        }
        
        // Create the sharded dataset
        ShardedDatasetGraph shardedDataset = ShardedDatasetFactory.wrap(shardingStrategy, shards);
        
        LOG.info("Created sharded dataset with {} shards", shardingStrategy.getShardCount());
        return DatasetFactory.wrap(shardedDataset);
    }
    
    /**
     * Create a sharding strategy from a resource.
     */
    private ShardingStrategy createShardingStrategy(Assembler a, Resource strategyRes) {
        if (strategyRes.hasProperty(org.apache.jena.vocabulary.RDF.type, 
                                     ShardingVocab.SubjectShardingStrategy)) {
            // Create a subject-based sharding strategy
            int shardCount = GraphUtils.getIntValue(strategyRes, ShardingVocab.shardCount);
            if (shardCount <= 0) {
                throw new DeltaException("Invalid shard count: " + shardCount);
            }
            
            return new SubjectShardingStrategy(shardCount);
            
        } else if (strategyRes.hasProperty(org.apache.jena.vocabulary.RDF.type, 
                                           ShardingVocab.GraphShardingStrategy)) {
            // Create a graph-based sharding strategy
            int shardCount = GraphUtils.getIntValue(strategyRes, ShardingVocab.shardCount);
            if (shardCount <= 0) {
                throw new DeltaException("Invalid shard count: " + shardCount);
            }
            
            GraphShardingStrategy strategy = new GraphShardingStrategy(shardCount);
            
            // Add graph mappings
            StmtIterator it = strategyRes.listProperties(ShardingVocab.graphMapping);
            while (it.hasNext()) {
                Statement stmt = it.next();
                RDFNode obj = stmt.getObject();
                if (obj.isResource()) {
                    Resource mappingRes = obj.asResource();
                    Resource graphRes = GraphUtils.getResourceValue(mappingRes, ShardingVocab.graph);
                    int mappingShardIndex = GraphUtils.getIntValue(mappingRes, ShardingVocab.shardIndex);
                    
                    if (graphRes != null && mappingShardIndex >= 0 && mappingShardIndex < shardCount) {
                        strategy.addGraphMapping(graphRes.asNode(), mappingShardIndex);
                    }
                }
            }
            
            return strategy;
            
        } else if (strategyRes.hasProperty(org.apache.jena.vocabulary.RDF.type, 
                                           ShardingVocab.PredicateShardingStrategy)) {
            // Create a predicate-based sharding strategy
            int shardCount = GraphUtils.getIntValue(strategyRes, ShardingVocab.shardCount);
            if (shardCount <= 0) {
                throw new DeltaException("Invalid shard count: " + shardCount);
            }
            
            PredicateShardingStrategy strategy = new PredicateShardingStrategy(shardCount);
            
            // Add predicate mappings
            StmtIterator it = strategyRes.listProperties(ShardingVocab.predicateMapping);
            while (it.hasNext()) {
                Statement stmt = it.next();
                RDFNode obj = stmt.getObject();
                if (obj.isResource()) {
                    Resource mappingRes = obj.asResource();
                    Resource predicateRes = GraphUtils.getResourceValue(mappingRes, ShardingVocab.predicate);
                    int mappingShardIndex = GraphUtils.getIntValue(mappingRes, ShardingVocab.shardIndex);
                    
                    if (predicateRes != null && mappingShardIndex >= 0 && mappingShardIndex < shardCount) {
                        strategy.addPredicateMapping(predicateRes.asNode(), mappingShardIndex);
                    }
                }
            }
            
            return strategy;
        } else {
            throw new DeltaException("Unknown sharding strategy type: " + strategyRes);
        }
    }
}
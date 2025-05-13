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

import java.util.function.Function;

import org.apache.jena.delta.client.DeltaClient;
import org.apache.jena.delta.client.DeltaLink;
import org.apache.jena.delta.tdb2.TDB2DeltaConnection;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.TDB2Factory;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Factory for creating sharded datasets.
 */
public class ShardedDatasetFactory {
    
    /**
     * Create a sharded dataset with the specified sharding strategy.
     * 
     * @param shardingStrategy The sharding strategy to use
     * @param shardSupplier Function to create a dataset for each shard
     * @return A new sharded dataset
     */
    public static ShardedDatasetGraph create(ShardingStrategy shardingStrategy,
                                             Function<Integer, DatasetGraph> shardSupplier) {
        return create(shardingStrategy, shardSupplier, null);
    }
    
    /**
     * Create a sharded dataset with the specified sharding strategy and metrics.
     * 
     * @param shardingStrategy The sharding strategy to use
     * @param shardSupplier Function to create a dataset for each shard
     * @param registry Registry for metrics (can be null)
     * @return A new sharded dataset
     */
    public static ShardedDatasetGraph create(ShardingStrategy shardingStrategy,
                                             Function<Integer, DatasetGraph> shardSupplier,
                                             MeterRegistry registry) {
        int shardCount = shardingStrategy.getShardCount();
        DatasetGraph[] shards = new DatasetGraph[shardCount];
        
        for (int i = 0; i < shardCount; i++) {
            shards[i] = shardSupplier.apply(i);
        }
        
        return new ShardedDatasetGraph(shardingStrategy, shards, registry);
    }
    
    /**
     * Create a sharded TDB2 dataset.
     * 
     * @param shardingStrategy The sharding strategy to use
     * @param baseDirectory The base directory for TDB2 datasets
     * @return A new sharded TDB2 dataset
     */
    public static ShardedDatasetGraph createTDB2(ShardingStrategy shardingStrategy,
                                                 String baseDirectory) {
        return create(shardingStrategy, shardIndex -> {
            String directory = baseDirectory + "/shard" + shardIndex;
            return TDB2Factory.connectDataset(directory);
        });
    }
    
    /**
     * Create a sharded TDB2 dataset with Delta replication.
     * 
     * @param shardingStrategy The sharding strategy to use
     * @param baseDirectory The base directory for TDB2 datasets
     * @param deltaLink The connection to the Delta server
     * @param datasetNamePrefix The prefix for dataset names in the Delta server
     * @return A new sharded TDB2 dataset with Delta replication
     */
    public static ShardedDatasetGraph createTDB2WithDelta(ShardingStrategy shardingStrategy,
                                                          String baseDirectory,
                                                          DeltaLink deltaLink,
                                                          String datasetNamePrefix) {
        return create(shardingStrategy, shardIndex -> {
            String directory = baseDirectory + "/shard" + shardIndex;
            String datasetName = datasetNamePrefix + "_shard" + shardIndex;
            
            // Create the TDB2 dataset
            DatasetGraph dsg = TDB2Factory.connectDataset(directory);
            
            // Connect to Delta
            return TDB2DeltaConnection.connect(dsg, deltaLink, datasetName);
        });
    }
    
    /**
     * Create a sharded dataset from existing datasets.
     * 
     * @param shardingStrategy The sharding strategy to use
     * @param shards The existing datasets to use as shards
     * @return A new sharded dataset
     */
    public static ShardedDatasetGraph wrap(ShardingStrategy shardingStrategy,
                                          DatasetGraph[] shards) {
        return new ShardedDatasetGraph(shardingStrategy, shards);
    }
}
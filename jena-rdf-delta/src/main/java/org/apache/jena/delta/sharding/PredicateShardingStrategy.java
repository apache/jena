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

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 * A sharding strategy that distributes data based on the predicate of the quad.
 * 
 * This strategy is useful for datasets with a small number of distinct predicates,
 * where each predicate or group of predicates can be placed on a different shard.
 * This works well for vertical partitioning of data.
 */
public class PredicateShardingStrategy implements ShardingStrategy {
    private static final long serialVersionUID = 1L;
    
    private final int shardCount;
    private final Map<Node, Integer> predicateToShardMap;
    private final SubjectShardingStrategy.HashFunction hashFunction;
    
    /**
     * Create a new PredicateShardingStrategy.
     * 
     * @param shardCount The number of shards
     */
    public PredicateShardingStrategy(int shardCount) {
        this(shardCount, new HashMap<>(), new SubjectShardingStrategy.MurmurHash3());
    }
    
    /**
     * Create a new PredicateShardingStrategy with explicit predicate-to-shard mappings.
     * 
     * @param shardCount The number of shards
     * @param predicateToShardMap Map of predicate nodes to shard indices
     * @param hashFunction The hash function to use for unmapped predicates
     */
    public PredicateShardingStrategy(int shardCount, Map<Node, Integer> predicateToShardMap, 
                                     SubjectShardingStrategy.HashFunction hashFunction) {
        if (shardCount <= 0) {
            throw new IllegalArgumentException("Shard count must be positive");
        }
        this.shardCount = shardCount;
        this.predicateToShardMap = predicateToShardMap;
        this.hashFunction = hashFunction;
    }
    
    /**
     * Add a fixed mapping from a predicate to a shard.
     * 
     * @param predicate The predicate node
     * @param shardIndex The shard index
     */
    public void addPredicateMapping(Node predicate, int shardIndex) {
        if (shardIndex < 0 || shardIndex >= shardCount) {
            throw new IllegalArgumentException("Shard index out of range: " + shardIndex);
        }
        predicateToShardMap.put(predicate, shardIndex);
    }
    
    @Override
    public int getShardCount() {
        return shardCount;
    }
    
    @Override
    public int getShardForQuad(Quad quad) {
        Node predicate = quad.getPredicate();
        
        // Check if we have a fixed mapping for this predicate
        Integer shard = predicateToShardMap.get(predicate);
        if (shard != null) {
            return shard;
        }
        
        // For unmapped predicates, use hash-based sharding
        return Math.abs(hashFunction.hash(predicate.toString()) % shardCount);
    }
    
    @Override
    public int getShardForPattern(Node g, Node s, Node p, Node o) {
        // If the predicate is specified
        if (p != null && !p.isVariable()) {
            // Check if we have a fixed mapping for this predicate
            Integer shard = predicateToShardMap.get(p);
            if (shard != null) {
                return shard;
            }
            
            // For unmapped predicates, use hash-based sharding
            return Math.abs(hashFunction.hash(p.toString()) % shardCount);
        }
        
        // If the predicate is a variable, we need to query all shards
        return -1;
    }
    
    @Override
    public boolean requiresAllShards(Quad pattern) {
        Node predicate = pattern.getPredicate();
        
        // If the predicate is specific, we only need one shard
        return predicate == null || predicate.isVariable();
    }
}
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
 * A sharding strategy that distributes data based on the graph of the quad.
 * 
 * This strategy is useful for datasets with many named graphs, where each graph
 * can be placed on a different shard. Default graph triples are distributed based
 * on subject hash.
 */
public class GraphShardingStrategy implements ShardingStrategy {
    private static final long serialVersionUID = 1L;
    
    private final int shardCount;
    private final Map<Node, Integer> graphToShardMap;
    private final SubjectShardingStrategy.HashFunction hashFunction;
    
    /**
     * Create a new GraphShardingStrategy.
     * 
     * @param shardCount The number of shards
     */
    public GraphShardingStrategy(int shardCount) {
        this(shardCount, new HashMap<>(), new SubjectShardingStrategy.MurmurHash3());
    }
    
    /**
     * Create a new GraphShardingStrategy with explicit graph-to-shard mappings.
     * 
     * @param shardCount The number of shards
     * @param graphToShardMap Map of graph nodes to shard indices
     * @param hashFunction The hash function to use for default graph
     */
    public GraphShardingStrategy(int shardCount, Map<Node, Integer> graphToShardMap, 
                                  SubjectShardingStrategy.HashFunction hashFunction) {
        if (shardCount <= 0) {
            throw new IllegalArgumentException("Shard count must be positive");
        }
        this.shardCount = shardCount;
        this.graphToShardMap = graphToShardMap;
        this.hashFunction = hashFunction;
    }
    
    /**
     * Add a fixed mapping from a graph to a shard.
     * 
     * @param graph The graph node
     * @param shardIndex The shard index
     */
    public void addGraphMapping(Node graph, int shardIndex) {
        if (shardIndex < 0 || shardIndex >= shardCount) {
            throw new IllegalArgumentException("Shard index out of range: " + shardIndex);
        }
        graphToShardMap.put(graph, shardIndex);
    }
    
    @Override
    public int getShardCount() {
        return shardCount;
    }
    
    @Override
    public int getShardForQuad(Quad quad) {
        Node graph = quad.getGraph();
        
        // Check if we have a fixed mapping for this graph
        Integer shard = graphToShardMap.get(graph);
        if (shard != null) {
            return shard;
        }
        
        // For the default graph or unmapped graphs, use subject hashing
        Node subject = quad.getSubject();
        return Math.abs(hashFunction.hash(subject.toString()) % shardCount);
    }
    
    @Override
    public int getShardForPattern(Node g, Node s, Node p, Node o) {
        // If the graph is specified and we have a mapping for it
        if (g != null && !g.isVariable()) {
            Integer shard = graphToShardMap.get(g);
            if (shard != null) {
                return shard;
            }
        }
        
        // If the graph is a variable or not mapped, and the subject is specified
        if ((g == null || g.isVariable()) && s != null && !s.isVariable()) {
            return Math.abs(hashFunction.hash(s.toString()) % shardCount);
        }
        
        // Otherwise, we need to query all shards
        return -1;
    }
    
    @Override
    public boolean requiresAllShards(Quad pattern) {
        Node graph = pattern.getGraph();
        Node subject = pattern.getSubject();
        
        // If the graph is specific and mapped, we only need that shard
        if (graph != null && !graph.isVariable() && graphToShardMap.containsKey(graph)) {
            return false;
        }
        
        // If the subject is specific and either no graph is specified or it's not mapped,
        // we only need the shard for that subject
        if (subject != null && !subject.isVariable() && 
            (graph == null || graph.isVariable() || !graphToShardMap.containsKey(graph))) {
            return false;
        }
        
        // Otherwise, we need all shards
        return true;
    }
}
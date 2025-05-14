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

import java.io.Serializable;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 * Interface for dataset sharding strategies.
 * 
 * A sharding strategy determines how data is distributed across multiple shards.
 * Different strategies can be used based on the characteristics of the data and
 * the query patterns.
 */
public interface ShardingStrategy extends Serializable {
    
    /**
     * Get the number of shards.
     */
    int getShardCount();
    
    /**
     * Get the shard index for a quad.
     * 
     * @param quad The quad to shard
     * @return The shard index (0 to shard count - 1)
     */
    int getShardForQuad(Quad quad);
    
    /**
     * Get the shard index for a triple pattern.
     * 
     * @param g The graph node (can be null or variable)
     * @param s The subject node (can be null or variable)
     * @param p The predicate node (can be null or variable)
     * @param o The object node (can be null or variable)
     * @return The shard index, or -1 if multiple shards need to be queried
     */
    int getShardForPattern(Node g, Node s, Node p, Node o);
    
    /**
     * Check if a query needs to be executed on all shards.
     * 
     * @param pattern The quad pattern
     * @return true if all shards need to be queried
     */
    boolean requiresAllShards(Quad pattern);
}
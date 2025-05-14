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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for sharded dataset configuration.
 */
public class ShardingVocab {
    
    public static final String NS = "http://jena.apache.org/delta/sharding#";
    
    /**
     * Create a resource in the sharding namespace.
     */
    public static Resource resource(String localname) {
        return ResourceFactory.createResource(NS + localname);
    }
    
    /**
     * Create a property in the sharding namespace.
     */
    public static Property property(String localname) {
        return ResourceFactory.createProperty(NS + localname);
    }
    
    // --- Resources
    
    /** A sharded dataset. */
    public static final Resource ShardedDataset = resource("ShardedDataset");
    
    /** A subject-based sharding strategy. */
    public static final Resource SubjectShardingStrategy = resource("SubjectShardingStrategy");
    
    /** A graph-based sharding strategy. */
    public static final Resource GraphShardingStrategy = resource("GraphShardingStrategy");
    
    /** A predicate-based sharding strategy. */
    public static final Resource PredicateShardingStrategy = resource("PredicateShardingStrategy");
    
    // --- Properties
    
    /** Property specifying the sharding strategy. */
    public static final Property shardingStrategy = property("shardingStrategy");
    
    /** Property specifying the number of shards. */
    public static final Property shardCount = property("shardCount");
    
    /** Property specifying the base directory for shard datasets. */
    public static final Property baseDirectory = property("baseDirectory");
    
    /** Property specifying the shards. */
    public static final Property shards = property("shards");
    
    /** Property specifying the shard index. */
    public static final Property shardIndex = property("shardIndex");
    
    /** Property specifying the dataset for a shard. */
    public static final Property shardDataset = property("shardDataset");
    
    /** Property specifying graph-to-shard mappings. */
    public static final Property graphMapping = property("graphMapping");
    
    /** Property specifying predicate-to-shard mappings. */
    public static final Property predicateMapping = property("predicateMapping");
    
    /** Property specifying the graph for a mapping. */
    public static final Property graph = property("graph");
    
    /** Property specifying the predicate for a mapping. */
    public static final Property predicate = property("predicate");
}
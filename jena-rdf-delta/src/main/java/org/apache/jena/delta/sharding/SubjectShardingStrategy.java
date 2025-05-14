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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 * A sharding strategy that distributes data based on the subject of the quad.
 * 
 * This strategy is efficient for queries that specify the subject, but requires
 * querying all shards for queries that don't specify the subject.
 */
public class SubjectShardingStrategy implements ShardingStrategy {
    private static final long serialVersionUID = 1L;
    
    private final int shardCount;
    private final HashFunction hashFunction;
    
    /**
     * Create a new SubjectShardingStrategy.
     * 
     * @param shardCount The number of shards
     */
    public SubjectShardingStrategy(int shardCount) {
        this(shardCount, new MurmurHash3());
    }
    
    /**
     * Create a new SubjectShardingStrategy with a specific hash function.
     * 
     * @param shardCount The number of shards
     * @param hashFunction The hash function to use
     */
    public SubjectShardingStrategy(int shardCount, HashFunction hashFunction) {
        if (shardCount <= 0) {
            throw new IllegalArgumentException("Shard count must be positive");
        }
        this.shardCount = shardCount;
        this.hashFunction = hashFunction;
    }
    
    @Override
    public int getShardCount() {
        return shardCount;
    }
    
    @Override
    public int getShardForQuad(Quad quad) {
        Node subject = quad.getSubject();
        return Math.abs(hashFunction.hash(subject.toString()) % shardCount);
    }
    
    @Override
    public int getShardForPattern(Node g, Node s, Node p, Node o) {
        // If the subject is a variable or null, we need to query all shards
        if (s == null || s.isVariable()) {
            return -1;
        }
        
        // Otherwise, hash the subject
        return Math.abs(hashFunction.hash(s.toString()) % shardCount);
    }
    
    @Override
    public boolean requiresAllShards(Quad pattern) {
        Node subject = pattern.getSubject();
        return subject == null || subject.isVariable();
    }
    
    /**
     * Interface for hash functions.
     */
    public interface HashFunction {
        /**
         * Compute a hash for a string.
         */
        int hash(String input);
    }
    
    /**
     * Simple implementation of MurmurHash3 for string hashing.
     * This is a fast, high-quality hash function.
     */
    public static class MurmurHash3 implements HashFunction {
        private static final long serialVersionUID = 1L;
        
        private static final int C1 = 0xcc9e2d51;
        private static final int C2 = 0x1b873593;
        
        @Override
        public int hash(String input) {
            if (input == null) {
                return 0;
            }
            
            byte[] data = input.getBytes();
            int length = data.length;
            int seed = 104729; // Large prime number
            
            int h1 = seed;
            int roundedEnd = length & 0xfffffffc;  // Round down to multiple of 4
            
            for (int i = 0; i < roundedEnd; i += 4) {
                // Get four bytes
                int k1 = (data[i] & 0xff) | ((data[i + 1] & 0xff) << 8) | 
                         ((data[i + 2] & 0xff) << 16) | (data[i + 3] << 24);
                
                k1 *= C1;
                k1 = Integer.rotateLeft(k1, 15);
                k1 *= C2;
                
                h1 ^= k1;
                h1 = Integer.rotateLeft(h1, 13);
                h1 = h1 * 5 + 0xe6546b64;
            }
            
            // Handle the remaining bytes
            int k1 = 0;
            switch (length & 0x03) {
                case 3:
                    k1 ^= (data[roundedEnd + 2] & 0xff) << 16;
                    // Fall through
                case 2:
                    k1 ^= (data[roundedEnd + 1] & 0xff) << 8;
                    // Fall through
                case 1:
                    k1 ^= (data[roundedEnd] & 0xff);
                    k1 *= C1;
                    k1 = Integer.rotateLeft(k1, 15);
                    k1 *= C2;
                    h1 ^= k1;
            }
            
            // Finalization
            h1 ^= length;
            h1 ^= h1 >>> 16;
            h1 *= 0x85ebca6b;
            h1 ^= h1 >>> 13;
            h1 *= 0xc2b2ae35;
            h1 ^= h1 >>> 16;
            
            return h1;
        }
    }
}
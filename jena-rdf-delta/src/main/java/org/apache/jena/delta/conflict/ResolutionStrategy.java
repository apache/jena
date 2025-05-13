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

package org.apache.jena.delta.conflict;

/**
 * Resolution strategies for RDF patch conflicts.
 */
public enum ResolutionStrategy {
    /**
     * Last write wins: The patch with the most recent timestamp is applied.
     */
    LAST_WRITE_WINS("last-write-wins", "Apply the most recent patch"),
    
    /**
     * First write wins: The patch with the earliest timestamp is applied.
     */
    FIRST_WRITE_WINS("first-write-wins", "Apply the earliest patch"),
    
    /**
     * Server wins: The patch from the primary server is applied.
     */
    SERVER_WINS("server-wins", "Apply the patch from the primary server"),
    
    /**
     * Client wins: The patch from the client is applied.
     */
    CLIENT_WINS("client-wins", "Apply the patch from the client"),
    
    /**
     * Merge: Try to merge both patches by applying non-conflicting changes from both.
     */
    MERGE("merge", "Apply non-conflicting changes from both patches"),
    
    /**
     * Reject both: Both patches are rejected, and the conflict must be resolved manually.
     */
    REJECT_BOTH("reject-both", "Reject both patches"),
    
    /**
     * Keep both: Create versions for both patches and a branch point.
     */
    KEEP_BOTH("keep-both", "Create versions for both patches"),
    
    /**
     * Semantic: Use domain-specific rules to resolve the conflict.
     */
    SEMANTIC("semantic", "Apply domain-specific resolution rules");
    
    private final String name;
    private final String description;
    
    ResolutionStrategy(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    /**
     * Get the name of the resolution strategy.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the description of the resolution strategy.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get a resolution strategy by name.
     * 
     * @param name The name of the resolution strategy
     * @return The resolution strategy, or null if not found
     */
    public static ResolutionStrategy fromName(String name) {
        for (ResolutionStrategy strategy : values()) {
            if (strategy.getName().equals(name)) {
                return strategy;
            }
        }
        return null;
    }
}
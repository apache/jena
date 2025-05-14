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
 * Types of conflicts that can occur between RDF patches.
 */
public enum ConflictType {
    /**
     * Direct conflict: The same triple is both added and deleted.
     */
    DIRECT("direct", "Same triple modified in conflicting ways"),
    
    /**
     * Object conflict: Same subject-predicate is given different objects.
     */
    OBJECT("object", "Same subject-predicate with different objects"),
    
    /**
     * Subject conflict: Same predicate-object is linked to different subjects.
     */
    SUBJECT("subject", "Same predicate-object with different subjects"),
    
    /**
     * Graph conflict: Modifications to related parts of a graph.
     */
    GRAPH("graph", "Modifications to related parts of a graph"),
    
    /**
     * Semantic conflict: Changes that violate semantic constraints.
     */
    SEMANTIC("semantic", "Changes that violate semantic constraints");
    
    private final String name;
    private final String description;
    
    ConflictType(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    /**
     * Get the name of the conflict type.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the description of the conflict type.
     */
    public String getDescription() {
        return description;
    }
}
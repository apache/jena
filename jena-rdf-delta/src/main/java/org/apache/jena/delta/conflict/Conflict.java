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

import org.apache.jena.sparql.core.Quad;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;

/**
 * Represents a conflict between RDF patches.
 */
public class Conflict {
    private final ConflictType type;
    private final Quad quad;
    private final String message;
    private final long timestamp;
    
    /**
     * Create a new Conflict.
     * 
     * @param type The type of conflict
     * @param quad The quad involved in the conflict
     * @param message A description of the conflict
     */
    public Conflict(ConflictType type, Quad quad, String message) {
        this.type = type;
        this.quad = quad;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the type of conflict.
     */
    public ConflictType getType() {
        return type;
    }
    
    /**
     * Get the quad involved in the conflict.
     */
    public Quad getQuad() {
        return quad;
    }
    
    /**
     * Get a description of the conflict.
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Get the timestamp of when the conflict was detected.
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get a JSON representation of the conflict.
     */
    public JsonObject toJson() {
        JsonBuilder builder = new JsonBuilder();
        builder.startObject();
        
        builder.key("type").value(type.getName());
        builder.key("graph").value(quad.getGraph().toString());
        builder.key("subject").value(quad.getSubject().toString());
        builder.key("predicate").value(quad.getPredicate() != null ? quad.getPredicate().toString() : null);
        builder.key("object").value(quad.getObject() != null ? quad.getObject().toString() : null);
        builder.key("message").value(message);
        builder.key("timestamp").value(timestamp);
        
        builder.finishObject();
        return builder.build().getAsObject();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Conflict(type=").append(type.getName());
        sb.append(", quad=").append(quad);
        sb.append(", message=").append(message);
        sb.append(")");
        return sb.toString();
    }
}
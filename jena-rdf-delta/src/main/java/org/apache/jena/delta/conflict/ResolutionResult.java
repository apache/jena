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

import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdfpatch.RDFPatch;

/**
 * Result of conflict resolution.
 */
public class ResolutionResult {
    private final ResolutionStatus status;
    private final RDFPatch resolvedPatch;
    private final String message;
    private final long timestamp;
    
    /**
     * Create a new ResolutionResult.
     * 
     * @param status The resolution status
     * @param resolvedPatch The resolved patch (can be null)
     * @param message A message describing the resolution (can be null)
     */
    public ResolutionResult(ResolutionStatus status, RDFPatch resolvedPatch, String message) {
        this.status = status;
        this.resolvedPatch = resolvedPatch;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the resolution status.
     */
    public ResolutionStatus getStatus() {
        return status;
    }
    
    /**
     * Get the resolved patch.
     */
    public RDFPatch getResolvedPatch() {
        return resolvedPatch;
    }
    
    /**
     * Get a message describing the resolution.
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Get the timestamp of when the resolution was created.
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Check if the resolution was successful.
     */
    public boolean isSuccess() {
        return status == ResolutionStatus.NO_CONFLICT || 
               status == ResolutionStatus.RESOLVED || 
               status == ResolutionStatus.BRANCHED;
    }
    
    /**
     * Get a JSON representation of the resolution result.
     */
    public JsonObject toJson() {
        JsonBuilder builder = new JsonBuilder();
        builder.startObject();
        
        builder.key("status").value(status.name());
        if (resolvedPatch != null) {
            builder.key("patchId").value(resolvedPatch.getId().toString());
        }
        if (message != null) {
            builder.key("message").value(message);
        }
        builder.key("timestamp").value(timestamp);
        
        builder.finishObject();
        return builder.build().getAsObject();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ResolutionResult(status=").append(status);
        if (resolvedPatch != null) {
            sb.append(", patchId=").append(resolvedPatch.getId());
        }
        if (message != null) {
            sb.append(", message=").append(message);
        }
        sb.append(")");
        return sb.toString();
    }
}
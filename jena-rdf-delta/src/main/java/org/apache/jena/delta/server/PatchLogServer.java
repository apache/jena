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

package org.apache.jena.delta.server;

import java.util.List;

import org.apache.jena.delta.DeltaException;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.system.Id;

/**
 * Interface for a patch log server.
 */
public interface PatchLogServer {
    
    /**
     * List the available patch logs.
     */
    public List<LogEntry> listPatchLogs();
    
    /**
     * Create a new patch log.
     * @param name The name of the patch log
     * @return The Id of the initial patch
     * @throws DeltaException if the patch log already exists
     */
    public Id createPatchLog(String name);
    
    /**
     * Get information about a patch log.
     * @param name The name of the patch log
     * @return The patch log information, or null if it doesn't exist
     */
    public LogEntry getPatchLogInfo(String name);
    
    /**
     * Append a patch to a patch log.
     * @param name The name of the patch log
     * @param patch The patch to append
     * @param expected The expected version to append after
     * @return The Id of the newly appended patch
     * @throws DeltaException if the patch log doesn't exist
     * @throws DeltaException if the expected version doesn't match the current version
     */
    public Id append(String name, RDFPatch patch, Id expected);
    
    /**
     * Get patches from a patch log.
     * @param name The name of the patch log
     * @param start The Id to start from (inclusive)
     * @return An Iterable of patches
     * @throws DeltaException if the patch log doesn't exist
     */
    public Iterable<RDFPatch> getPatches(String name, Id start);
    
    /**
     * Get a specific patch from a patch log.
     * @param name The name of the patch log
     * @param id The Id of the patch
     * @return The patch, or null if it doesn't exist
     * @throws DeltaException if the patch log doesn't exist
     */
    public RDFPatch getPatch(String name, Id id);
    
    /**
     * Information about a patch log.
     */
    public static class LogEntry {
        private final String name;
        private final Id head;
        
        /**
         * Create a new LogEntry.
         * @param name The name of the patch log
         * @param head The Id of the head patch
         */
        public LogEntry(String name, Id head) {
            this.name = name;
            this.head = head;
        }
        
        /**
         * Get the name of the patch log.
         */
        public String getName() {
            return name;
        }
        
        /**
         * Get the Id of the head patch.
         */
        public Id getHead() {
            return head;
        }
    }
}
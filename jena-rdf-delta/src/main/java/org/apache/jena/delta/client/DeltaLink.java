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

package org.apache.jena.delta.client;

import java.util.List;

import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.system.Id;

/**
 * Interface for interacting with a patch log server.
 */
public interface DeltaLink {
    
    /**
     * Get the base URL of the patch log server.
     */
    public String getServerURL();
    
    /**
     * List the available patch logs on the server.
     */
    public List<String> listDatasets();
    
    /**
     * Get the current state of a patch log.
     */
    public PatchLogInfo getPatchLogInfo(String dsName);
    
    /**
     * Create a new patch log.
     * @param dsName Dataset name
     * @return Id of the initial patch
     */
    public Id createDataset(String dsName);
    
    /**
     * Append a patch to a patch log.
     * @param dsName Dataset name
     * @param patch The RDF patch to append
     * @param expected The expected version to append after, or null for no version check
     * @return The id of the newly appended patch
     */
    public Id append(String dsName, RDFPatch patch, Id expected);
    
    /**
     * Fetch patches for a dataset.
     * @param dsName Dataset name
     * @param version Starting version (inclusive)
     * @return List of patches
     */
    public List<RDFPatch> fetch(String dsName, Id version);
    
    /**
     * Get a specific patch.
     * @param dsName Dataset name
     * @param version The version to fetch
     * @return The patch or null if not found
     */
    public RDFPatch fetch(String dsName, Id version, boolean stopOnError);
    
    /**
     * Register a listener for patch log events.
     */
    public void register(PatchLogListener listener);
    
    /**
     * Unregister a listener for patch log events.
     */
    public void unregister(PatchLogListener listener);
    
    /**
     * Close the delta link and release any resources.
     */
    public void close();
}
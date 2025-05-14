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

package org.apache.jena.delta;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * RDF Delta vocabulary.
 */
public class DeltaVocab {
    
    public static final String NS = "http://jena.apache.org/delta#";
    
    /**
     * Create a resource in the Delta namespace.
     */
    public static Resource resource(String localname) {
        return ResourceFactory.createResource(NS + localname);
    }
    
    /**
     * Create a property in the Delta namespace.
     */
    public static Property property(String localname) {
        return ResourceFactory.createProperty(NS + localname);
    }
    
    // --- Resources
    
    /** A replicated dataset that uses RDF Delta for synchronization. */
    public static final Resource ReplicatedDataset = resource("ReplicatedDataset");
    
    /** A patch log server for RDF Delta. */
    public static final Resource PatchLogServer = resource("PatchLogServer");
    
    /** A patch log that records changes to a dataset. */
    public static final Resource PatchLog = resource("PatchLog");
    
    /** A TDB2 dataset with RDF Delta change tracking. */
    public static final Resource TDB2Dataset = resource("TDB2Dataset");
    
    // --- Properties
    
    /** Property linking a replicated dataset to its patch log server. */
    public static final Property patchLogServer = property("patchLogServer");
    
    /** Property specifying the name of a dataset. */
    public static final Property datasetName = property("datasetName");
    
    /** Property linking a replicated dataset to its underlying storage. */
    public static final Property storage = property("storage");
    
    /** Property specifying the zone for a client. */
    public static final Property zone = property("zone");
    
    /** Property specifying the URL of a Delta server. */
    public static final Property pDeltaServer = property("server");
    
    /** Property specifying the name of a dataset in a Delta server. */
    public static final Property pDatasetName = property("name");
    
    /** Property linking a TDB2Dataset to its underlying TDB2 dataset. */
    public static final Property pDataset = property("dataset");
    
    /** Property specifying the polling interval for checking updates (in milliseconds). */
    public static final Property pPollingInterval = property("pollingInterval");
}
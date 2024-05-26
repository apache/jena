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

package org.apache.jena.sparql.service.enhancer.assembler;

import org.apache.jena.assembler.JA;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;

/** Vocabulary for assembler-based configuration of the service enhancer plugin */
public class ServiceEnhancerVocab {
    public static final String NS = "http://jena.apache.org/service-enhancer#";

    public static String getURI() { return NS; }

    public static final Resource DatasetServiceEnhancer = ResourceFactory.createResource(NS + "DatasetServiceEnhancer");

    /** The id (a node) to which to resolve urn:x-arq:self */
    public static final Property datasetId = ResourceFactory.createProperty(NS + "datasetId");

    /** Enable privileged management functions; creates a wrapped dataset with a copied context */
    public static final Property enableMgmt = ResourceFactory.createProperty(NS + "enableMgmt");

    /** The term "baseDataset" is not officially in ja but it seems reasonable to eventually add it there.
     * So far ja only defines baseModel */
    public static final Property baseDataset = ResourceFactory.createProperty(JA.getURI() + "baseDataset");

    /** Maximum number of entries the service cache can hold */
    public static final Property cacheMaxEntryCount = ResourceFactory.createProperty(NS + "cacheMaxEntryCount");

    /** Number number of pages for bindings an individual cache entry can hold */
    public static final Property cacheMaxPageCount = ResourceFactory.createProperty(NS + "cacheMaxPageCount");

    /** Number of bindings a page can hold */
    public static final Property cachePageSize = ResourceFactory.createProperty(NS + "cachePageSize");

    /** Maximum size (in terms of input bindings) of bulk requests */
    public static final Property bulkMaxSize = ResourceFactory.createProperty(NS + "bulkMaxSize");

    /** Bulk size to use if no other is set. Capped by bulkMaxSize. */
    public static final Property bulkSize = ResourceFactory.createProperty(NS + "bulkSize");

    public static final Property bulkMaxOutOfBandSize = ResourceFactory.createProperty(NS + "bulkMaxOutOfBandSize");

    /** Adds the following prefix declarations to the given map thereby overrides existing ones:
     * <table style="border: 1px solid;">
     *   <tr><th>Prefix</th><th>IRI</th></tr>
     *   <tr><td>ja</td><td>{@value JA#uri}</td></tr>
     *   <tr><td>se</td><td>{@value #NS}</td></tr>
     * </table>
     */
    public PrefixMap addPrefixes(PrefixMap pm) {
        pm.add("ja", JA.getURI());
        pm.add("se", ServiceEnhancerVocab.getURI());
        return pm;
    }

    /** Adds the following prefix declarations to the given map thereby overrides existing ones:
     * <table style="border: 1px solid;">
     *   <tr><th>Prefix</th><th>IRI</th></tr>
     *   <tr><td>ja</td><td>{@value JA#uri}</td></tr>
     *   <tr><td>se</td><td>{@value #NS}</td></tr>
     * </table>
     */
    public PrefixMapping addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("ja", JA.getURI());
        pm.setNsPrefix("se", ServiceEnhancerVocab.getURI());
        return pm;
    }
}

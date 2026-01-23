/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.service.enhancer.init;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.service.enhancer.assembler.ServiceEnhancerVocab;
import org.apache.jena.sparql.util.Symbol;

public class ServiceEnhancerConstants {
    /** An IRI constant for referencing the active dataset within a SERVICE clause */
    public static final Node SELF = NodeFactory.createURI("urn:x-arq:self");

    /** An IRI constant for referencing the active dataset within a SERVICE clause */
    // FIXME SERVICE <urn:x-arq-self+bulk> is handled by the the bulk chain of the service executor which receives an iterator of the input bindings.
    //   In constrast SERVICE <urn:x-arq-self> is handled by the single chain which is fed each binding individually.
    //   Self bulk can thus perform more powerful bulk requests.
    public static final Node SELF_BULK = NodeFactory.createURI("urn:x-arq:self+bulk");

    /** Namespace for context symbols. Same as the assembler vocabulary. */
    public static final String NS = ServiceEnhancerVocab.NS;

    public static String getURI() { return NS; }

    /** Maximum number of bindings to group into a single bulk request; upper limit for serviceBulkRequestBindingCount */
    public static final Symbol serviceBulkMaxBindingCount = SystemARQ.allocSymbol(NS, "serviceBulkMaxBindingCount") ;

    /** Maximum number of out-of-band bindings that can be skipped over when forming an individual bulk request */
    public static final Symbol serviceBulkMaxOutOfBandBindingCount = SystemARQ.allocSymbol(NS, "serviceBulkMaxOutOfBandBindingCount") ;

    /** Number of bindings to group into a single bulk request */
    public static final Symbol serviceBulkBindingCount = SystemARQ.allocSymbol(NS, "serviceBulkBindingCount") ;

    /** Default number of slots when no explicit number is given.
     *  Subject to capping by {@link #serviceConcurrentMaxSlotCount}. */
    public static final Symbol serviceConcurrentDftSlotCount = SystemARQ.allocSymbol(NS, "serviceConcurrentDftSlotCount") ;

    public static final Symbol serviceConcurrentMaxSlotCount = SystemARQ.allocSymbol(NS, "serviceConcurrentMaxSlotCount") ;

    /** Default number of slots when no explicit number is given.
     *  Subject to capping by {@link #serviceConcurrentDftReadaheadCount}. */
    public static final Symbol serviceConcurrentDftReadaheadCount = SystemARQ.allocSymbol(NS, "serviceConcurrentDftReadaheadCount") ;

    public static final Symbol serviceConcurrentMaxReadaheadCount = SystemARQ.allocSymbol(NS, "serviceConcurrentMaxReadaheadCount") ;

    /** Symbol for the cache of services' result sets */
    public static final Symbol serviceCache = SystemARQ.allocSymbol(NS, "serviceCache") ;

    /** Factory for on-demand initialization of a serviceCache instance */
    // public static final Symbol serviceCacheFactory = SystemARQ.allocSymbol(NS, "serviceCacheFactory") ;

    // The following serviceCache* context symbols can be used to configure on-demand serviceCache instance creation.

    public static final Symbol serviceCacheMaxEntryCount = SystemARQ.allocSymbol(NS, "serviceCacheMaxEntryCount") ;
    public static final Symbol serviceCachePageSize = SystemARQ.allocSymbol(NS, "serviceCachePageSize") ;
    public static final Symbol serviceCacheMaxPageCount = SystemARQ.allocSymbol(NS, "serviceCacheMaxPageCount") ;

    /** Symbol for the cache of services' result set sizes */
    public static final Symbol serviceResultSizeCache = SystemARQ.allocSymbol(NS, "serviceResultSizeCache") ;

    /** Symbol with IRI (String) value. References to {@link #SELF} will be resolved to the given IRI when writing cache entries. */
    public static final Symbol datasetId = SystemARQ.allocSymbol(NS, "datasetId") ;

    /** This symbol must be set to true in the context in order to allow calling certain "privileged" SPARQL functions. */
    public static final Symbol enableMgmt = SystemARQ.allocSymbol(NS, "enableMgmt") ;

    /*
     * A guide number to limit bulk SERVICE requests to roughly this byte size.
     * Implementations may use a heuristic to estimate the number of bytes in order to avoid
     * excessive string serializations of query/algebra objects.
     * For example, an approach may just sum up Binding.toString().
     * The limit is ignored for the first binding added to such a request
     */
    // public static final Symbol serviceBulkRequestMaxByteSize = SystemARQ.allocSymbol("serviceBulkRequestMaxByteSize") ;
}

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

package org.apache.jena.sparql.service.enhancer.init;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.service.enhancer.assembler.ServiceEnhancerVocab;
import org.apache.jena.sparql.util.Symbol;

public class ServiceEnhancerConstants {
    /** An IRI constant for referencing the active dataset within a SERVICE clause */
    public static final Node SELF = NodeFactory.createURI("urn:x-arq:self");

    /** Namespace for context symbols. Same as the assembler vocabulary. */
    public static final String NS = ServiceEnhancerVocab.NS;

    public static String getURI() { return NS; }

    /** Maximum number of bindings to group into a single bulk request; restricts serviceBulkRequestItemCount */
    public static final Symbol serviceBulkMaxBindingCount = SystemARQ.allocSymbol(NS, "serviceBulkMaxBindingCount") ;

    /** Maximum number of out-of-band bindings that can be skipped over when forming an individual bulk request */
    public static final Symbol serviceBulkMaxOutOfBandBindingCount = SystemARQ.allocSymbol(NS, "serviceBulkMaxOutOfBandBindingCount") ;

    /** Number of bindings to group into a single bulk request */
    public static final Symbol serviceBulkBindingCount = SystemARQ.allocSymbol(NS, "serviceBulkBindingCount") ;

    /** Symbol for the cache of services' result sets */
    public static final Symbol serviceCache = SystemARQ.allocSymbol(NS, "serviceCache") ;

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

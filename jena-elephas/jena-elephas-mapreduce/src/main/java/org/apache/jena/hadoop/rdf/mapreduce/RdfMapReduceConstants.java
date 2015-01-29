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

package org.apache.jena.hadoop.rdf.mapreduce;

/**
 * RDF Map/Reduce related constants
 * 
 * 
 * 
 */
public class RdfMapReduceConstants {

    /**
     * Private constructor prevents instantiation
     */
    private RdfMapReduceConstants() {

    }

    /**
     * Configuration key used to set whether the behaviour of the filter mappers
     * is inverted. When enabled the filter mappers will invert their selection
     * i.e. tuples that would normally be accepted will be rejected and vice
     * versa.
     */
    public static final String FILTER_INVERT = "rdf.mapreduce.filter.invert";

    /**
     * Configuration key used to set a command separated list of predicate URIs
     * to filter upon
     */
    public static final String FILTER_PREDICATE_URIS = "rdf.mapreduce.filter.predicate.uris";

    /**
     * Configuration key used to set a command separated list of subject URIs to
     * filter upon
     */
    public static final String FILTER_SUBJECT_URIS = "rdf.mapreduce.filter.subject.uris";

    /**
     * Configuration key used to set a command separated list of object URIs to
     * filter upon
     */
    public static final String FILTER_OBJECT_URIS = "rdf.mapreduce.filter.object.uris";

    /**
     * Configuration key used to set a command separated list of graph URIs to
     * filter upon
     */
    public static final String FILTER_GRAPH_URIS = "rdf.mapreduce.filter.graph.uris";
}

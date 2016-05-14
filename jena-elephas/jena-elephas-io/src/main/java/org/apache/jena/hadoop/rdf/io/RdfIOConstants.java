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

package org.apache.jena.hadoop.rdf.io;

import java.io.IOException;

/**
 * RDF IO related constants
 * 
 * 
 * 
 */
public class RdfIOConstants {

    /**
     * Private constructor prevents instantiation
     */
    private RdfIOConstants() {
    }

    /**
     * Configuration key used to set whether bad tuples are ignored. This is the
     * default behaviour, when explicitly set to {@code false} bad tuples will
     * result in {@link IOException} being thrown by the relevant record
     * readers.
     */
    public static final String INPUT_IGNORE_BAD_TUPLES = "rdf.io.input.ignore-bad-tuples";

    /**
     * Configuration key used to set the batch size used for RDF output formats
     * that take a batched writing approach. Default value is given by the
     * constant {@link #DEFAULT_OUTPUT_BATCH_SIZE}.
     */
    public static final String OUTPUT_BATCH_SIZE = "rdf.io.output.batch-size";

    /**
     * Default batch size for batched output formats
     */
    public static final long DEFAULT_OUTPUT_BATCH_SIZE = 10000;

    /**
     * Configuration key used to control behaviour with regards to how blank
     * nodes are handled.
     * <p>
     * The default behaviour is that blank nodes are file scoped which is what
     * the RDF specifications require.
     * </p>
     * <p>
     * However in the case of a multi-stage pipeline this behaviour can cause
     * blank nodes to diverge over several jobs and introduce spurious blank
     * nodes over time. This is described in <a
     * href="https://issues.apache.org/jira/browse/JENA-820">JENA-820</a> and
     * enabling this flag for jobs in your pipeline allow you to work around
     * this problem.
     * </p>
     * <h3>Warning</h3> You should only enable this flag for jobs that take in
     * RDF output originating from previous jobs since our normal blank node
     * allocation policy ensures that blank nodes will be file scoped and unique
     * over all files (barring unfortunate hasing collisions). If you enable
     * this for jobs that take in RDF originating from other sources you may
     * incorrectly conflate blank nodes that are supposed to distinct and
     * separate nodes.
     */
    public static final String GLOBAL_BNODE_IDENTITY = "rdf.io.input.bnodes.global-identity";
}

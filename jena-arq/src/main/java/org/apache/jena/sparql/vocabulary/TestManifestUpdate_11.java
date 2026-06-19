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

package org.apache.jena.sparql.vocabulary ;

/* CVS $Id: $ */

import org.apache.jena.rdf.model.* ;

/**
 * Vocabulary definitions from test-update.n3
 */
public class TestManifestUpdate_11 {

    /** Optional: data for the update test (i.e. default graph in the graph store
     *  prior or after the update, depending on whether used within mf:action or within
     *  mf:result)
     */
    public static final Property data = ResourceFactory.createProperty( "http://www.w3.org/2009/sparql/tests/test-update#data" );

    public static final Property graph = ResourceFactory.createProperty( "http://www.w3.org/2009/sparql/tests/test-update#graph" );

    /** Optional: named-graph only data for the update test (i.e. named graph in the
     *  graph store prior or after the update, depending on whether used within mf:action
     *  or within mf:result)
     */
    public static final Property graphData = ResourceFactory.createProperty( "http://www.w3.org/2009/sparql/tests/test-update#graphData" );

    /** The update query to ask */
    public static final Property request = ResourceFactory.createProperty( "http://www.w3.org/2009/sparql/tests/test-update#request" );

    /** The class of all SPARQL 1.1 Update results */
    public static final Resource Result = ResourceFactory.createProperty( "http://www.w3.org/2009/sparql/tests/test-update#Result" );

    /** test failure, cf. http://www.w3.org/TR/sparql11-protocol/#update-fault-messages */
    public static final Resource graph_already_exists = ResourceFactory.createResource( "http://www.w3.org/2009/sparql/tests/test-update#graph-already-exists" );

    /** test failure, cf. http://www.w3.org/TR/sparql11-protocol/#update-fault-messages */
    public static final Resource graph_does_not_exist = ResourceFactory.createResource( "http://www.w3.org/2009/sparql/tests/test-update#graph-does-not-exist" );

    /** test failure, cf. http://www.w3.org/TR/sparql11-protocol/#update-fault-messages */
    public static final Resource malformed_update = ResourceFactory.createResource( "http://www.w3.org/2009/sparql/tests/test-update#malformed-update" );

    /** test shall pass without failure */
    public static final Resource success = ResourceFactory.createResource( "http://www.w3.org/2009/sparql/tests/test-update#success" );

    /** test failure, cf. http://www.w3.org/TR/sparql11-protocol/#update-fault-messages */
    public static final Resource update_request_refused = ResourceFactory.createResource( "http://www.w3.org/2009/sparql/tests/test-update#update-request-refused" );

}

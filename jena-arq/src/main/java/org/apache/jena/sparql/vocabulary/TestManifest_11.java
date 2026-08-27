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

import org.apache.jena.rdf.model.* ;
import org.apache.jena.vocabulary.TestManifest;

/**
 * Vocabulary definitions from test-manifest-1_1.ttl
 */
@Deprecated(forRemoval = true)
public class TestManifest_11 {

    public static final String NS = TestManifest.NS;

    /** A type of test specifically for syntax testing for SPARQL 1.1. */
    @Deprecated(forRemoval = true)
    public static final Resource PositiveSyntaxTest11 = ResourceFactory.createResource( NS+"PositiveSyntaxTest11" );

    @Deprecated(forRemoval = true)
    public static final Resource PositiveUpdateSyntaxTest11 = ResourceFactory.createResource( NS+"PositiveUpdateSyntaxTest11" );

    /** A type of test specifically for syntax testing for SPARQL 1.1. */
    @Deprecated(forRemoval = true)
    public static final Resource NegativeSyntaxTest11 = ResourceFactory.createResource( NS+"NegativeSyntaxTest11" );

    @Deprecated(forRemoval = true)
    public static final Resource NegativeUpdateSyntaxTest11 = ResourceFactory.createResource( NS+"NegativeUpdateSyntaxTest11" );

}


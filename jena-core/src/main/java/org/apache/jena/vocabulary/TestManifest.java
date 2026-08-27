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

package org.apache.jena.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class TestManifest {

    /** The namespace of the vocabulary as a string */
    public static final String NS = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#";

    /**
     * The namespace of the vocabulary as a string
     */
    public static String getURI() { return NS; }

    /** The namespace of the vocabulary as a resource */
    public static final Resource NAMESPACE = ResourceFactory.createResource(NS);

    /** Spec Version label */
    public static final Property specVersion = ResourceFactory.createProperty(NS+"specVersion");

    /** The expected outcome */
    public static final Property result = ResourceFactory.createProperty(NS+"result");

    /** Action to perform */
    public static final Property action = ResourceFactory.createProperty(NS+"action");

    /** Optional name of this entry */
    public static final Property name = ResourceFactory.createProperty(NS+"name");

    /** Connects the manifest resource to rdf:type list of entries */
    public static final Property entries = ResourceFactory.createProperty(NS+"entries");

    /** Connects the manifest resource to rdf:type list of manifests */
    public static final Property include = ResourceFactory.createProperty(NS+"include");

    /** Assumed base for the tests in the manifest */
    public static final Property assumedTestBase = ResourceFactory.createProperty(NS+"assumedTestBase");

    /**
     * The given mf:result for a mf:ReducedCardinalityTest is the results as if the
     * REDUCED keyword were omitted. To pass a mf:ReducedCardinalityTest, an
     * implementation must produce a result set with each solution in the expected
     * results appearing at least once and no more than the number of times it
     * appears in the expected results. Of course, there must also be no results
     * produced that are not in the expected results.
     */
    public static final Resource ReducedCardinalityTest = ResourceFactory.createResource(NS+"ReducedCardinalityTest");

    /**
     * A type of test specifically for query evaluation testing. Query evaluation
     * tests are required to have an associated input dataset, a query, and an
     * expected output dataset.
     */
    public static final Resource QueryEvaluationTest = ResourceFactory.createResource(NS+"QueryEvaluationTest");

    /**
     * A type of test specifically for syntax testing. Syntax tests are not required
     * to have an associated result, only an action. Negative syntax tests are tests
     * of which the result should be a parser error.
     */
    public static final Resource NegativeSyntaxTest = ResourceFactory.createResource(NS+"NegativeSyntaxTest");

    /**
     * A type of test specifically for syntax testing. Syntax tests are not required
     * to have an associated result, only an action.
     */
    public static final Resource PositiveSyntaxTest = ResourceFactory.createResource(NS+"PositiveSyntaxTest");

    /**
     * A type of test specifically for update syntax testing.
     */
    public static final Resource PositiveUpdateSyntaxTest = ResourceFactory.createResource(NS+"PositiveUpdateSyntaxTest");

    /**
     * A type of test specifically for update syntax testing.
     *  Negative syntax tests are tests of which the result should be a parser error.
     */
    public static final Resource NegativeUpdateSyntaxTest = ResourceFactory.createResource(NS+"NegativeUpdateSyntaxTest");

    /** The class of all SPARQL Update evaluation tests */
    public static final Resource UpdateEvaluationTest = ResourceFactory.createResource( NS+"UpdateEvaluationTest" );

    /** One entry in rdf:type list of entries */
    public static final Resource ManifestEntry = ResourceFactory.createResource(NS+"ManifestEntry");

    /** The class of manifests */
    public static final Resource Manifest = ResourceFactory.createResource(NS+"Manifest");

    public static final Resource accepted = ResourceFactory.createResource(NS+"accepted");

    public static final Resource proposed = ResourceFactory.createResource(NS+"proposed");

    public static final Resource rejected = ResourceFactory.createResource(NS+"rejected");

}

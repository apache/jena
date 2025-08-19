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

package org.apache.jena.sparql.vocabulary;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Additional or changed vocabulary for rdf-tests SPARQL area.
 */
public class TestManifest_12 {

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#";

    public static final Resource PositiveSyntaxTest12 = ResourceFactory.createResource(NS+"PositiveSyntaxTest12");

    public static final Resource NegativeSyntaxTest12 = ResourceFactory.createResource(NS+"NegativeSyntaxTest12");

    public static final Resource PositiveUpdateSyntaxTest = ResourceFactory.createResource(NS+"PositiveUpdateSyntaxTest");

    public static final Resource NegativeUpdateSyntaxTest = ResourceFactory.createResource(NS+"NegativeUpdateSyntaxTest");

}

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

import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;

/** More TestManifest */
public class TestManifestX {
    /** The namespace of the vocabulary as a string */
    public static final String NS = "http://jena.hpl.hp.com/2005/05/test-manifest-extra#";

    /** The namespace of the vocabulary as a strings
     *  @see #NS */
    public static String getURI() {return NS;}

    /** The namespace of the vocabulary as a resource */
    public static final Resource NAMESPACE = ResourceFactory.createResource( NS );

    /** Syntax of the query */
    public static final Property dataSyntax = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#dataSyntax" );

    /** Default type of a test */
    public static final Property defaultTestType = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#defaultTestType" );

    /** Include another manifest file. */
    public static final Property include = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#include" );

    /** Option for an action */
    public static final Property option = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#option" );

    /** Syntax of the query */
    public static final Property querySyntax = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#querySyntax" );

    /** Whether to create a text index */
    public static final Property textIndex = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#textIndex" );

    /** Syntax tests which expect a parse failure */
    public static final Resource NegativeSyntaxTestARQ = ResourceFactory.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#NegativeSyntaxTestARQ" );

    /** Syntax tests (query) */
    public static final Resource PositiveSyntaxTestARQ = ResourceFactory.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#PositiveSyntaxTestARQ" );

    /** Syntax tests which expect a parse failure */
    public static final Resource NegativeUpdateSyntaxTestARQ = ResourceFactory.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#NegativeUpdateSyntaxTestARQ" );

    /** Syntax tests (query) */
    public static final Resource PositiveUpdateSyntaxTestARQ = ResourceFactory.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#PositiveUpdateSyntaxTestARQ" );

    /** The class of test that are Query tests (query, data, results) */
    public static final Resource TestQuery = ResourceFactory.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#TestQuery" );

    /** Query serialization tests */
    public static final Resource TestSerialization = ResourceFactory.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#TestSerialization" );

    /** Query test not to be run */
    public static final Resource TestSurpressed = ResourceFactory.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#TestSurpressed" );

    /** CSV Test (i.e query, with results in CSV) - an extension */
    public static final Resource CSVResultFormatTest = ResourceFactory.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#CSVResultFormatTest" );
}

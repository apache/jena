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

package com.hp.hpl.jena.vocabulary ;

/* CVS $Id: TestManifest.java,v 1.1 2009-06-29 08:55:36 castagna Exp $ */
 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from test-manifest.n3
 */
public class TestManifest {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>The test statusThe expected outcome</p> */
    public static final Property result = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#result" );
    
    /** <p>Action to perform</p> */
    public static final Property action = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#action" );
    
    /** <p>Optional name of this entry</p> */
    public static final Property name = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#name" );
    
    /** <p>Connects the manifest resource to rdf:type list of entries</p> */
    public static final Property entries = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#entries" );
    
    /** <p>Connects the manifest resource to rdf:type list of manifests</p> */
    public static final Property include = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#include" );
    
    /** <p>A type of test specifically for query evaluation testing. Query evaluation 
     *  tests are required to have an associated input dataset, a query, and an expected 
     *  output dataset.</p>
     */
    public static final Resource QueryEvaluationTest = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#QueryEvaluationTest" );
    
    /** <p>A type of test specifically for syntax testing. Syntax tests are not required 
     *  to have an associated result, only an action. Negative syntax tests are tests 
     *  of which the result should be a parser error.</p>
     */
    public static final Resource NegativeSyntaxTest = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#NegativeSyntaxTest" );
    
    /** <p>A type of test specifically for syntax testing. Syntax tests are not required 
     *  to have an associated result, only an action.</p>
     */
    public static final Resource PositiveSyntaxTest = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#PositiveSyntaxTest" );
    
    /** <p>One entry in rdf:type list of entries</p> */
    public static final Resource ManifestEntry = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#ManifestEntry" );
    
    /** <p>The class of manifests</p> */
    public static final Resource Manifest = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#Manifest" );
    
    public static final Resource accepted = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#accepted" );
    
    public static final Resource proposed = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#proposed" );
    
    public static final Resource rejected = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#rejected" );
    
}

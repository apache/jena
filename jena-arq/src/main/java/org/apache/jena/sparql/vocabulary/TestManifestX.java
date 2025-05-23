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

package org.apache.jena.sparql.vocabulary ;

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;

/** More TestManifest */
public class TestManifestX {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://jena.hpl.hp.com/2005/05/test-manifest-extra#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p>Syntax of the query</p> */
    public static final Property dataSyntax = m_model.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#dataSyntax" );

    /** <p>Default type of a test</p> */
    public static final Property defaultTestType = m_model.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#defaultTestType" );

    /** <p>Include another manifest file.</p> */
    public static final Property include = m_model.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#include" );

    /** <p>Option for an action</p> */
    public static final Property option = m_model.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#option" );

    /** <p>Syntax of the query</p> */
    public static final Property querySyntax = m_model.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#querySyntax" );

    /** <p>Whether to create a text index</p> */
    public static final Property textIndex = m_model.createProperty( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#textIndex" );

    /** <p>Syntax tests which expect a parse failure</p> */
    public static final Resource NegativeSyntaxTestARQ = m_model.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#NegativeSyntaxTestARQ" );

    /** <p>Syntax tests (query)</p> */
    public static final Resource PositiveSyntaxTestARQ = m_model.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#PositiveSyntaxTestARQ" );

    /** <p>Syntax tests which expect a parse failure</p> */
    public static final Resource NegativeUpdateSyntaxTestARQ = m_model.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#NegativeUpdateSyntaxTestARQ" );

    /** <p>Syntax tests (query)</p> */
    public static final Resource PositiveUpdateSyntaxTestARQ = m_model.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#PositiveUpdateSyntaxTestARQ" );

    /** <p>The class of test that are Query tests (query, data, results)</p> */
    public static final Resource TestQuery = m_model.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#TestQuery" );

    /** <p>Query serialization tests</p> */
    public static final Resource TestSerialization = m_model.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#TestSerialization" );

    /** <p>Query test not to be run</p> */
    public static final Resource TestSurpressed = m_model.createResource( "http://jena.hpl.hp.com/2005/05/test-manifest-extra#TestSurpressed" );

}

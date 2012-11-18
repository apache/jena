/**
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

package org.apache.jena.riot.langsuite;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Resource ;

public class VocabLangRDF
{
    public static String assumedBaseURI = "http://example/base/" ;

    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/ns/rdftest#" ;

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    public static final Resource TestPositiveSyntaxTTL      = m_model.createResource( NS+"TestTurtlePositiveSyntax" );

    public static final Resource TestNegativeSyntaxTTL      = m_model.createResource( NS+"TestTurtleNegativeSyntax" );

    public static final Resource TestEvalTTL                = m_model.createResource( NS+"TestTurtleEval" );

    public static final Resource TestNegativeEvalTTL        = m_model.createResource( NS+"TestTurtleNegativeEval" );

    public static final Resource TestPositiveSyntaxNT       = m_model.createResource( NS+"TestNTriplesPositiveSyntax" );

    public static final Resource TestNegativeSyntaxNT       = m_model.createResource( NS+"TestNTriplesNegativeSyntax" );

    public static final Resource TestEvalNT                 = m_model.createResource( NS+"TestNTriplesEval" );

    public static final Resource TestNegativeEvalNT         = m_model.createResource( NS+"TestNTriplesNegativeEval" );

    public static final Resource TestPositiveSyntaxRJ       = m_model.createResource( NS+"TestRDFJSONPositiveSyntax" );

    public static final Resource TestNegativeSyntaxRJ       = m_model.createResource( NS+"TestRDFJSONNegativeSyntax" );

    public static final Resource TestEvalRJ                 = m_model.createResource( NS+"TestRDFJSONEval" );

    public static final Resource TestNegativeEvalRJ         = m_model.createResource( NS+"TestRDFJSONNegativeEval" );

    public static final Resource TestPositiveSyntaxNQ       = m_model.createResource( NS+"TestNQuadsPositiveSyntax" );

    public static final Resource TestNegativeSyntaxNQ       = m_model.createResource( NS+"TestNQuadsNegativeSyntax" );

    public static final Resource TestEvalNQ                 = m_model.createResource( NS+"TestNQuadsEval" );

    public static final Resource TestNegativeEvalNQ         = m_model.createResource( NS+"TestNQuadsNegativeEval" );

    public static final Resource TestPositiveSyntaxTriG     = m_model.createResource( NS+"TestTrigPositiveSyntax" );

    public static final Resource TestNegativeSyntaxTriG     = m_model.createResource( NS+"TestTrigNegativeSyntax" );

    public static final Resource TestEvalTriG               = m_model.createResource( NS+"TestTrigEval" );

    public static final Resource TestNegativeEvalTriG       = m_model.createResource( NS+"TestTrigNegativeEval" );

    public static final Resource TestSurpressed             = m_model.createResource( NS+"Test" );
}


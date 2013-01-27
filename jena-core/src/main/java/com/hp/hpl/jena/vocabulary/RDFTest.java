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

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;
 
/**
 * The vocabulary used by the RDFCore working group to define test manifests.
 * <p>
 * Vocabulary definitions from file:data/testOntology.rdf
 */
public class RDFTest {
    
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabalary as a string ({@value})</p> */
    public static final String NS = "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#";
    
    /** <p>The namespace of the vocabalary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabalary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>Contains a reference to the minutes of the WG meeting where the test case 
     *  status was last changed.</p>
     */
    public static final Property approval = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#approval" );
    
    /** <p>A human-readable summary of the test case.</p> */
    public static final Property description = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#description" );
    
    /** <p>Contains a pointer to other discussion surrounding this test case or the associated 
     *  issue.</p>
     */
    public static final Property discussion = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#discussion" );
    
    /** <p>The rules for determining entailment - presently RDF alone or RDF and RDFS; 
     *  in addition, the requirement for datatype support machinery can be indicated.</p>
     */
    public static final Property entailmentRules = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#entailmentRules" );
    
    /** <p>A test case input document.</p> */
    public static final Property inputDocument = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#inputDocument" );
    
    /** <p>Contains a pointer to the associated issue, such as is listed on the RDF Core 
     *  WG Tracking document.</p>
     */
    public static final Property issue = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#issue" );
    
    /** <p>A test case output document</p> */
    public static final Property outputDocument = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#outputDocument" );
    
    /** <p>Indicates the status of the test within a process, such as the RDF Core WG 
     *  process.</p>
     */
    public static final Property status = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#status" );
    
    /** <p>Indicates that while the test should pass, it may generate a warning.</p> */
    public static final Property warning = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#warning" );
    
    /** <p>A premise document of an entailment.</p> */
    public static final Property premiseDocument = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#premiseDocument" );
    
    /** <p>A conclusion document of an entailment.</p> */
    public static final Property conclusionDocument = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#conclusionDocument" );
    
    /** <p>A test case document for a miscellaneous test.</p> */
    public static final Property document = m_model.createProperty( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#document" );
    
    /** <p>This manifest entry is used to describe test cases that do not fall into one 
     *  of the categories. It may have several associated files, indicated in <test:document> 
     *  elements.</p>
     */
    public static final Resource MiscellaneousTest = m_model.createResource( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#MiscellaneousTest" );
    
    /** <p></p> */
    public static final Resource NT_Document = m_model.createResource( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#NT-Document" );
    
    /** <p>These tests consist of one or more premise documents, and a consequent document. 
     *  An inference engine is considered to pass the test if it correctly holds that 
     *  the expressions in the premise documents do not entail those in the the conclusion 
     *  document.</p>
     */
    public static final Resource NegativeEntailmentTest = m_model.createResource( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#NegativeEntailmentTest" );
    
    /** <p>These tests consist of one input document. The document is not legal RDF/XML. 
     *  A parser is considered to pass the test if it correctly holds the input document 
     *  to be in error.</p>
     */
    public static final Resource NegativeParserTest = m_model.createResource( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#NegativeParserTest" );
    
    /** <p>These tests are specified by one or more premise documents (in RDF/XML or 
     *  N-Triples) together with a single conclusion document. In addition, the rules 
     *  used for determining entailment are specified by test:entailmentRules elements.</p>
     */
    public static final Resource PositiveEntailmentTest = m_model.createResource( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#PositiveEntailmentTest" );
    
    /** <p>These tests consist of one (or more) input documents in RDF/XML as revised. 
     *  The expected result is defined using the N-Triples syntax.</p>
     */
    public static final Resource PositiveParserTest = m_model.createResource( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#PositiveParserTest" );
    
    /** <p></p> */
    public static final Resource RDF_XML_Document = m_model.createResource( "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#RDF-XML-Document" );
    

}

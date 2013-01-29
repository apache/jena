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
 * The vocabulary used by the WebOnt working group to define test manifests.
 * <p>
 * Vocabulary definitions from file:data/testOntology.rdf
 */
public class OWLTest  {

	/** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabalary as a string ({@value})</p> */
    public static final String NS = "http://www.w3.org/2002/03owlt/testOntology#";
    
    /** <p>The namespace of the vocabalary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabalary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>This property relates a test to a language feature. The language feature is 
     *  usually indicated by a class or property.</p>
     */
    public static final Property feature = m_model.createProperty( "http://www.w3.org/2002/03owlt/testOntology#feature" );
    
    /** <p>The object is a datatype that appears in one of the test files in the subject 
     *  test.</p>
     */
    public static final Property usedDatatype = m_model.createProperty( "http://www.w3.org/2002/03owlt/testOntology#usedDatatype" );
    
    /** <p>The subject test is valid only when the object datatype is included in the 
     *  datatype theory.</p>
     */
    public static final Property supportedDatatype = m_model.createProperty( "http://www.w3.org/2002/03owlt/testOntology#supportedDatatype" );
    
    /** <p>Despite the property URI, the document indicated by this property may or may 
     *  not be imported into the test.</p>
     */
    public static final Property importedPremiseDocument = m_model.createProperty( "http://www.w3.org/2002/03owlt/testOntology#importedPremiseDocument" );
    
    /** <p>Indicates the conformance level of a document or test in the OWL test suite.</p> */
    public static final Property level = m_model.createProperty( "http://www.w3.org/2002/03owlt/testOntology#level" );

	/** <p>One of the conformance levels  in the OWL test suite.</p> */
	public static final Resource Lite = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#Lite" );
	/** <p>One of the conformance levels  in the OWL test suite.</p> */
	public static final Resource DL = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#DL" );
	/** <p>One of the conformance levels  in the OWL test suite.</p> */
	public static final Resource Full = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#Full" );
	
    /** <p>A string valued property that gives a numeral (or some other quasi-numeric 
     *  string) associated with an issue.</p>
     */
    public static final Property issueNumber = m_model.createProperty( "http://www.w3.org/2002/03owlt/testOntology#issueNumber" );
	public static final Property size = m_model.createProperty( "http://www.w3.org/2002/03owlt/testOntology#size" );
	public static final Resource Large = m_model.createProperty( "http://www.w3.org/2002/03owlt/testOntology#Large" );
    
    public static final Resource Test = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#Test" );
    
    /** <p>This is a positive entailment test according to the OWL entailment rules.</p> */
    public static final Resource PositiveEntailmentTest = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#PositiveEntailmentTest" );
    
    /** <p>This is a negative entailment test according to the OWL entailment rules.</p> */
    public static final Resource NegativeEntailmentTest = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#NegativeEntailmentTest" );
    
    /** <p>The conclusions follow from the empty premises.</p> */
    public static final Resource TrueTest = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#TrueTest" );
    
    /** <p>Illustrative of the use of OWL to describe OWL Full.</p> */
    public static final Resource OWLforOWLTest = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#OWLforOWLTest" );
    
    /** <p>These tests use two documents. One is named importsNNN.rdf, the other is named 
     *  mainNNN.rdf. These tests indicate the interaction between owl:imports and 
     *  the sublanguage levels of the main document.</p>
     */
    public static final Resource ImportLevelTest = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#ImportLevelTest" );
    
    /** <p>This is a negative test. The input document contains some use of the OWL namespace 
     *  which is not a feature of OWL. These typically show DAML+OIL features that 
     *  are not being carried forward into OWL.</p>
     */
    public static final Resource NotOwlFeatureTest = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#NotOwlFeatureTest" );
    
    /** <p>The premise document, and its imports closure, entails the conclusion document.</p> */
    public static final Resource ImportEntailmentTest = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#ImportEntailmentTest" );
    
    /** <p>An inconsistent OWL document. (One that entails falsehood).</p> */
    public static final Resource InconsistencyTest = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#InconsistencyTest" );
    
    /** <p>A consistent OWL document. (One that does not entail falsehood).</p> */
    public static final Resource ConsistencyTest = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#ConsistencyTest" );
    
    /** <p>A member of this class is an issue in some issue list.</p> */
    public static final Resource Issue = m_model.createResource( "http://www.w3.org/2002/03owlt/testOntology#Issue" );
    
}

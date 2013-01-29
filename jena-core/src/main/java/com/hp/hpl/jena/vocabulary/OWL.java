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

// Package
///////////////////////////////////////
package com.hp.hpl.jena.vocabulary;


// Imports
///////////////////////////////////////
import com.hp.hpl.jena.rdf.model.*;




/**
 * Vocabulary definitions from file:vocabularies/owl.owl
 */
public class OWL {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string ({@value})</p> */
    public static final String NS = "http://www.w3.org/2002/07/owl#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** A resource that denotes the OWL-full sublanguage of OWL */
    public static final Resource FULL_LANG = m_model.getResource( getURI() );
    
    /** A resource, not officially sanctioned by WebOnt, that denotes the OWL-DL sublanguage of OWL */
    public static final Resource DL_LANG = m_model.getResource( "http://www.w3.org/TR/owl-features/#term_OWLDL" );
    
    /** A resource, not officially sanctioned by WebOnt, that denotes the OWL-Lite sublanguage of OWL */
    public static final Resource LITE_LANG = m_model.getResource( "http://www.w3.org/TR/owl-features/#term_OWLLite" );

    // Vocabulary properties
    ///////////////////////////

    public static final Property maxCardinality = m_model.createProperty( "http://www.w3.org/2002/07/owl#maxCardinality" );
    
    public static final Property versionInfo = m_model.createProperty( "http://www.w3.org/2002/07/owl#versionInfo" );
    
    public static final Property equivalentClass = m_model.createProperty( "http://www.w3.org/2002/07/owl#equivalentClass" );
    
    public static final Property distinctMembers = m_model.createProperty( "http://www.w3.org/2002/07/owl#distinctMembers" );
    
    public static final Property oneOf = m_model.createProperty( "http://www.w3.org/2002/07/owl#oneOf" );
    
    public static final Property sameAs = m_model.createProperty( "http://www.w3.org/2002/07/owl#sameAs" );
    
    public static final Property incompatibleWith = m_model.createProperty( "http://www.w3.org/2002/07/owl#incompatibleWith" );
    
    public static final Property minCardinality = m_model.createProperty( "http://www.w3.org/2002/07/owl#minCardinality" );
    
    public static final Property complementOf = m_model.createProperty( "http://www.w3.org/2002/07/owl#complementOf" );
    
    public static final Property onProperty = m_model.createProperty( "http://www.w3.org/2002/07/owl#onProperty" );
    
    public static final Property equivalentProperty = m_model.createProperty( "http://www.w3.org/2002/07/owl#equivalentProperty" );
    
    public static final Property inverseOf = m_model.createProperty( "http://www.w3.org/2002/07/owl#inverseOf" );
    
    public static final Property backwardCompatibleWith = m_model.createProperty( "http://www.w3.org/2002/07/owl#backwardCompatibleWith" );
    
    public static final Property differentFrom = m_model.createProperty( "http://www.w3.org/2002/07/owl#differentFrom" );
    
    public static final Property priorVersion = m_model.createProperty( "http://www.w3.org/2002/07/owl#priorVersion" );
    
    public static final Property imports = m_model.createProperty( "http://www.w3.org/2002/07/owl#imports" );
    
    public static final Property allValuesFrom = m_model.createProperty( "http://www.w3.org/2002/07/owl#allValuesFrom" );
    
    public static final Property unionOf = m_model.createProperty( "http://www.w3.org/2002/07/owl#unionOf" );
    
    public static final Property hasValue = m_model.createProperty( "http://www.w3.org/2002/07/owl#hasValue" );
    
    public static final Property someValuesFrom = m_model.createProperty( "http://www.w3.org/2002/07/owl#someValuesFrom" );
    
    public static final Property disjointWith = m_model.createProperty( "http://www.w3.org/2002/07/owl#disjointWith" );
    
    public static final Property cardinality = m_model.createProperty( "http://www.w3.org/2002/07/owl#cardinality" );
    
    public static final Property intersectionOf = m_model.createProperty( "http://www.w3.org/2002/07/owl#intersectionOf" );
    

    // Vocabulary classes
    ///////////////////////////

    public static final Resource Thing = m_model.createResource( "http://www.w3.org/2002/07/owl#Thing" );
    
    public static final Resource DataRange = m_model.createResource( "http://www.w3.org/2002/07/owl#DataRange" );
    
    public static final Resource Ontology = m_model.createResource( "http://www.w3.org/2002/07/owl#Ontology" );
    
    public static final Resource DeprecatedClass = m_model.createResource( "http://www.w3.org/2002/07/owl#DeprecatedClass" );
    
    public static final Resource AllDifferent = m_model.createResource( "http://www.w3.org/2002/07/owl#AllDifferent" );
    
    public static final Resource DatatypeProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#DatatypeProperty" );
    
    public static final Resource SymmetricProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#SymmetricProperty" );
    
    public static final Resource TransitiveProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#TransitiveProperty" );
    
    public static final Resource DeprecatedProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#DeprecatedProperty" );
    
    public static final Resource AnnotationProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#AnnotationProperty" );
    
    public static final Resource Restriction = m_model.createResource( "http://www.w3.org/2002/07/owl#Restriction" );
    
    public static final Resource Class = m_model.createResource( "http://www.w3.org/2002/07/owl#Class" );
    
    public static final Resource OntologyProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#OntologyProperty" );
    
    public static final Resource ObjectProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#ObjectProperty" );
    
    public static final Resource FunctionalProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#FunctionalProperty" );
    
    public static final Resource InverseFunctionalProperty = m_model.createResource( "http://www.w3.org/2002/07/owl#InverseFunctionalProperty" );
    
    public static final Resource Nothing = m_model.createResource( "http://www.w3.org/2002/07/owl#Nothing" );
    

    // Vocabulary individuals
    ///////////////////////////

}

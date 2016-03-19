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
package org.apache.jena.vocabulary;

// Imports
///////////////////////////////////////
import org.apache.jena.rdf.model.* ;

/**
 * Vocabulary definitions from file:vocabularies/owl.owl
 */
public class OWL {
    // Using ResourceFactory to avoid initialization circularity problems.
    // OWL is a central place where other classes go to get their constants
    // causing potential circularity of initialization.
    // If OWL starts the Jena initialization process
    // (ModelFactory.createDefaultModel calls JenaSystem.init)
    // then classes can easily see uninitialized constants. 

    // Remove after Jena 3.0.1 or later.
//    // ModelFactory.createDefaultModel calls JenaSystem.init
//    /** <p>The RDF model that holds the vocabulary terms</p> */
//    private static final Model m_model = ModelFactory.createDefaultModel();
//
//    protected static final Resource resource( String uri )
//    { return m_model.createResource( uri ); }
//
//    protected static final Property property( String uri )
//    { return m_model.createProperty( uri ); }

    // These will use ResourceFactory which creates Resource etc without a specific model.
    // This is safer for complex initialization paths.
    protected static final Resource resource( String uri )
    { return ResourceFactory.createResource( uri ); }

    protected static final Property property( String uri )
    { return ResourceFactory.createProperty( uri ); }

    /** <p>The namespace of the vocabulary as a string ({@value})</p> */
    public static final String NS = "http://www.w3.org/2002/07/owl#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = resource( NS );
    
    /** A resource that denotes the OWL-full sublanguage of OWL */
    public static final Resource FULL_LANG = resource( getURI() );
    
    /** A resource, not officially sanctioned by WebOnt, that denotes the OWL-DL sublanguage of OWL */
    public static final Resource DL_LANG = resource( "http://www.w3.org/TR/owl-features/#term_OWLDL" );
    
    /** A resource, not officially sanctioned by WebOnt, that denotes the OWL-Lite sublanguage of OWL */
    public static final Resource LITE_LANG = resource( "http://www.w3.org/TR/owl-features/#term_OWLLite" );

    // Vocabulary properties
    ///////////////////////////

    public static final Property maxCardinality = property( "http://www.w3.org/2002/07/owl#maxCardinality" );
    
    public static final Property versionInfo = property( "http://www.w3.org/2002/07/owl#versionInfo" );
    
    public static final Property equivalentClass = property( "http://www.w3.org/2002/07/owl#equivalentClass" );
    
    public static final Property distinctMembers = property( "http://www.w3.org/2002/07/owl#distinctMembers" );
    
    public static final Property oneOf = property( "http://www.w3.org/2002/07/owl#oneOf" );
    
    public static final Property sameAs = property( "http://www.w3.org/2002/07/owl#sameAs" );
    
    public static final Property incompatibleWith = property( "http://www.w3.org/2002/07/owl#incompatibleWith" );
    
    public static final Property minCardinality = property( "http://www.w3.org/2002/07/owl#minCardinality" );
    
    public static final Property complementOf = property( "http://www.w3.org/2002/07/owl#complementOf" );
    
    public static final Property onProperty = property( "http://www.w3.org/2002/07/owl#onProperty" );
    
    public static final Property equivalentProperty = property( "http://www.w3.org/2002/07/owl#equivalentProperty" );
    
    public static final Property inverseOf = property( "http://www.w3.org/2002/07/owl#inverseOf" );
    
    public static final Property backwardCompatibleWith = property( "http://www.w3.org/2002/07/owl#backwardCompatibleWith" );
    
    public static final Property differentFrom = property( "http://www.w3.org/2002/07/owl#differentFrom" );
    
    public static final Property priorVersion = property( "http://www.w3.org/2002/07/owl#priorVersion" );
    
    public static final Property imports = property( "http://www.w3.org/2002/07/owl#imports" );
    
    public static final Property allValuesFrom = property( "http://www.w3.org/2002/07/owl#allValuesFrom" );
    
    public static final Property unionOf = property( "http://www.w3.org/2002/07/owl#unionOf" );
    
    public static final Property hasValue = property( "http://www.w3.org/2002/07/owl#hasValue" );
    
    public static final Property someValuesFrom = property( "http://www.w3.org/2002/07/owl#someValuesFrom" );
    
    public static final Property disjointWith = property( "http://www.w3.org/2002/07/owl#disjointWith" );
    
    public static final Property cardinality = property( "http://www.w3.org/2002/07/owl#cardinality" );
    
    public static final Property intersectionOf = property( "http://www.w3.org/2002/07/owl#intersectionOf" );

    // Vocabulary classes
    ///////////////////////////

    public static final Resource Thing = resource( "http://www.w3.org/2002/07/owl#Thing" );
    
    public static final Resource DataRange = resource( "http://www.w3.org/2002/07/owl#DataRange" );
    
    public static final Resource Ontology = resource( "http://www.w3.org/2002/07/owl#Ontology" );
    
    public static final Resource DeprecatedClass = resource( "http://www.w3.org/2002/07/owl#DeprecatedClass" );
    
    public static final Resource AllDifferent = resource( "http://www.w3.org/2002/07/owl#AllDifferent" );
    
    public static final Resource DatatypeProperty = resource( "http://www.w3.org/2002/07/owl#DatatypeProperty" );
    
    public static final Resource SymmetricProperty = resource( "http://www.w3.org/2002/07/owl#SymmetricProperty" );
    
    public static final Resource TransitiveProperty = resource( "http://www.w3.org/2002/07/owl#TransitiveProperty" );
    
    public static final Resource DeprecatedProperty = resource( "http://www.w3.org/2002/07/owl#DeprecatedProperty" );
    
    public static final Resource AnnotationProperty = resource( "http://www.w3.org/2002/07/owl#AnnotationProperty" );
    
    public static final Resource Restriction = resource( "http://www.w3.org/2002/07/owl#Restriction" );
    
    public static final Resource Class = resource( "http://www.w3.org/2002/07/owl#Class" );
    
    public static final Resource OntologyProperty = resource( "http://www.w3.org/2002/07/owl#OntologyProperty" );
    
    public static final Resource ObjectProperty = resource( "http://www.w3.org/2002/07/owl#ObjectProperty" );
    
    public static final Resource FunctionalProperty = resource( "http://www.w3.org/2002/07/owl#FunctionalProperty" );
    
    public static final Resource InverseFunctionalProperty = resource( "http://www.w3.org/2002/07/owl#InverseFunctionalProperty" );
    
    public static final Resource Nothing = resource( "http://www.w3.org/2002/07/owl#Nothing" );

    // Vocabulary individuals
    ///////////////////////////
}

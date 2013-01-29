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
import com.hp.hpl.jena.ontology.*;



/**
 * Vocabulary definitions from file:vocabularies/ont-event.rdf
 */
public class OntEventsVocab {
    /** <p>The ontology model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
    
    /** <p>The namespace of the vocabulary as a string ({@value})</p> */
    public static final String NS = "http://jena.hpl.hp.com/schemas/2003/03/ont-event#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    

    // Vocabulary properties
    ///////////////////////////


    // Vocabulary classes
    ///////////////////////////

    /** <p>A class representing observable events in an ontology model</p> */
    public static final OntClass OntEvent = m_model.createClass( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#OntEvent" );
    

    // Vocabulary individuals
    ///////////////////////////

    /** <p>Event representing the declaration of one ontology individual being related 
     *  to another by some named predicate.</p>
     */
    public static final Individual related = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#related", OntEvent );
    
    /** <p>Event representing the declaration of a property as having a given class, 
     *  datatype or datarange as the range</p>
     */
    public static final Individual range = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#range", OntEvent );
    
    /** <p>Event representing the declaration of a resource as an ontology Class.</p> */
    public static final Individual classDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#classDeclaration", OntEvent );
    
    /** <p>Event representing the declaration that a restriction applies to a given property</p> */
    public static final Individual onProperty = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#onProperty", OntEvent );
    
    /** <p>Event representing the declaration of that a qualified restriction has the 
     *  given class or datatype for the qualification restriction</p>
     */
    public static final Individual hasClassQ = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#hasClassQ", OntEvent );
    
    /** <p>Event representing the declaration of one class being the sub-class of another.</p> */
    public static final Individual subClassOf = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#subClassOf", OntEvent );
    
    /** <p>Event representing the declaration of a class expression being composed of 
     *  a finite enumeration of identified individuals.</p>
     */
    public static final Individual oneOf = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#oneOf", OntEvent );
    
    /** <p>Event representing the declaration of a property as being the inverse of another 
     *  property</p>
     */
    public static final Individual inverseOf = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#inverseOf", OntEvent );
    
    /** <p>Event representing the declaration of a resource of type owl:Ontology, 
     *  representing meta-data about the ontology.</p>
     */
    public static final Individual ontologyDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#ontologyDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of a property as being functional.</p> */
    public static final Individual functionalPropertyDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#functionalPropertyDeclaration", OntEvent );
    
    /** <p>Event representing the declaration that a restriction constrains the property 
     *  to have a given value</p>
     */
    public static final Individual hasValue = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#hasValue", OntEvent );
    
    /** <p>Event representing the declaration of that a restriction has the given minimum 
     *  cardinality on the restricted property</p>
     */
    public static final Individual minCardinality = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#minCardinality", OntEvent );
    
    /** <p>Event representing a label on an ontology element</p> */
    public static final Individual label = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#label", OntEvent );
    
    /** <p>Event representing the declaration of a class as being deprecated.</p> */
    public static final Individual DeprecatedClass = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#DeprecatedClass", OntEvent );
    
    /** <p>Event representing the declaration of a property as having a given class, 
     *  datatype or datarange as the domain</p>
     */
    public static final Individual domain = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#domain", OntEvent );
    
    /** <p>Event representing the declaration of one class expression being disjoint 
     *  with another.</p>
     */
    public static final Individual disjointWith = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#disjointWith", OntEvent );
    
    /** <p>Event representing the declaration that a restriction constrains at least 
     *  one value of the property to have some class or datatype</p>
     */
    public static final Individual someValuesFrom = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#someValuesFrom", OntEvent );
    
    /** <p>Event representing the declaration that one ontology is imported into another 
     *  ontology.</p>
     */
    public static final Individual imports = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#imports", OntEvent );
    
    /** <p>Event representing the declaration of a class expression being a union of 
     *  class descriptions.</p>
     */
    public static final Individual unionOf = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#unionOf", OntEvent );
    
    /** <p>Event representing the declaration of an ontology property.</p> */
    public static final Individual ontologyPropertyDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#ontologyPropertyDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of a property as being deprecated.</p> */
    public static final Individual DeprecatedProperty = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#DeprecatedProperty", OntEvent );
    
    /** <p>Event representing the declaration of a resource being a Restriction</p> */
    public static final Individual restrictionDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#restrictionDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of a property as being symmetric</p> */
    public static final Individual symmetricPropertyDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#symmetricPropertyDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of that a restriction has the given maximum 
     *  cardinality on the restricted property</p>
     */
    public static final Individual maxCardinality = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#maxCardinality", OntEvent );
    
    /** <p>Event representing the declaration of one ontology individual being distinct 
     *  from another</p>
     */
    public static final Individual differentFrom = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#differentFrom", OntEvent );
    
    /** <p>Event representing a comment on an ontology element</p> */
    public static final Individual comment = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#comment", OntEvent );
    
    /** <p>Event representing the declaration of one class expression being equivalent 
     *  to another.</p>
     */
    public static final Individual equivalentClass = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#equivalentClass", OntEvent );
    
    /** <p>Event representing a catch-all category of user-specified data, ie triples 
     *  in the graph that relate to the use of ontology terms on instances, rather 
     *  than the definition of ontology terms.</p>
     */
    public static final Individual userData = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#userData", OntEvent );
    
    /** <p>Event representing the declaration of a class expression being an intersection 
     *  of class descriptions.</p>
     */
    public static final Individual intersectionOf = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#intersectionOf", OntEvent );
    
    /** <p>Event representing the declaration of that a restriction has the given cardinality 
     *  on the restricted property</p>
     */
    public static final Individual cardinality = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#cardinality", OntEvent );
    
    /** <p>Event representing the declaration of a resource as a Datarange.</p> */
    public static final Individual datarangeDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#datarangeDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of a prior version of a given ontology, 
     *  which the ontology is compatible with.</p>
     */
    public static final Individual backwardCompatibleWith = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#backwardCompatibleWith", OntEvent );
    
    /** <p>Event representing the declaration of a prior version of a given ontology, 
     *  which the ontology is not compatible with.</p>
     */
    public static final Individual incompatibleWith = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#incompatibleWith", OntEvent );
    
    /** <p>Event representing the declaration of one ontology individual being the same 
     *  as another</p>
     */
    public static final Individual sameIndividualAs = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#sameIndividualAs", OntEvent );
    
    /** <p>Event representing the declaration of a set of individuals being pairwise 
     *  distinct.</p>
     */
    public static final Individual allDifferentDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#allDifferentDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of a resource as an annotation property.</p> */
    public static final Individual annotationPropertyDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#annotationPropertyDeclaration", OntEvent );
    
    /** <p>Event representing the identification of a set of individuals that are in 
     *  the scope of an AllDifferent declaration.</p>
     */
    public static final Individual distinctMembers = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#distinctMembers", OntEvent );
    
    /** <p>Event representing the declaration of an ontology individual</p> */
    public static final Individual individualDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#individualDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of version information on an ontology resource.</p> */
    public static final Individual versionInfo = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#versionInfo", OntEvent );
    
    /** <p>Event representing the declaration of a resource as a plain property.</p> */
    public static final Individual propertyDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#propertyDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of that a qualified restriction has the 
     *  given cardinality on the restricted property</p>
     */
    public static final Individual cardinalityQ = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#cardinalityQ", OntEvent );
    
    /** <p>Event representing the declaration of a resource as an object property.</p> */
    public static final Individual objectPropertyDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#objectPropertyDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of a prior version of a given ontology.</p> */
    public static final Individual priorVersion = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#priorVersion", OntEvent );
    
    /** <p>Event representing the declaration of a property as being equivalent to another 
     *  property</p>
     */
    public static final Individual equivalentProperty = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#equivalentProperty", OntEvent );
    
    /** <p>Event representing the declaration of that a qualified restriction has the 
     *  given minimum cardinality on the restricted property</p>
     */
    public static final Individual minCardinalityQ = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#minCardinalityQ", OntEvent );
    
    /** <p>Event representing the declaration of a property as being the sub-property 
     *  of another property</p>
     */
    public static final Individual subPropertyOf = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#subPropertyOf", OntEvent );
    
    /** <p>Event representing the declaration of a class expression being the complement 
     *  of another class description.</p>
     */
    public static final Individual complementOf = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#complementOf", OntEvent );
    
    /** <p>Event representing a declaration that one resource is the same as another.</p> */
    public static final Individual sameAs = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#sameAs", OntEvent );
    
    /** <p>Event representing the declaration that a restriction constrains all values 
     *  of the property to have some class or datatype</p>
     */
    public static final Individual allValuesFrom = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#allValuesFrom", OntEvent );
    
    /** <p>Event representing the declaration of a property as being inverse functional.</p> */
    public static final Individual inverseFunctionalPropertyDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#inverseFunctionalPropertyDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of a property as being transitive.</p> */
    public static final Individual transitivePropertyDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#transitivePropertyDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of a resource as a datatype property.</p> */
    public static final Individual datatypePropertyDeclaration = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#datatypePropertyDeclaration", OntEvent );
    
    /** <p>Event representing the declaration of that a qualified restriction has the 
     *  given maximum cardinality on the restricted property</p>
     */
    public static final Individual maxCardinalityQ = m_model.createIndividual( "http://jena.hpl.hp.com/schemas/2003/03/ont-event#maxCardinalityQ", OntEvent );
    
}

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
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import java.io.OutputStream ;
import java.io.Writer ;
import java.util.List ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/**
 * <p>
 * An enhanced view of a Jena model that is known to contain ontology
 * data, under a given ontology {@link Profile vocabulary} (such as OWL).
 * This class does not by itself compute the deductive extension of the graph
 * under the semantic rules of the language.  Instead, we wrap an underlying
 * model with this ontology interface, that presents a convenience syntax for accessing
 * the language elements. Depending on the inference capability of the underlying model,
 * the OntModel will appear to contain more or less triples. For example, if
 * this class is used to wrap a plain memory or database model, only the
 * relationships asserted by the document will be reported through this
 * convenience API. Alternatively, if the OntModel wraps an OWL inferencing model,
 * the inferred triples from the extension will be reported as well. For
 * example, assume the following ontology fragment:
 * <code><pre>
 *      :A rdf:type owl:Class .
 *      :B rdf:type owl:Class ; rdfs:subClassOf :A .
 *      :widget rdf:type :B .
 * </pre></code>
 * In a non-inferencing model, the <code>rdf:type</code> of the widget will be
 * reported as class <code>:B</code> only.  In a model that can process the OWL
 * semantics, the widget's types will include <code>:B</code>, <code>:A</code>,
 * and <code>owl:Thing</code>.
 * </p>
 * <p>
 * <strong>Note:</strong> that <code>OntModel</code> is an extension to the
 * {@link InfModel} interface.  This is to support the case where an ontology model
 * wraps an inference graph, and we want to make the special capabilities of the
 * <code>InfModel</code>, for example global consistency checking, accessible to
 * client programs.  Since not all ont models use a reasoner, using these methods
 * may result in a runtime exception, though the typical behaviour is that such
 * calls will be silently ignored.
 * </p>
 */
public interface OntModel
    extends InfModel
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer an iterator that ranges over the ontology resources in this model, i&#046;e&#046;
     * the resources with <code>rdf:type Ontology</code> or equivalent. These resources
     * typically contain metadata about the ontology document that contains them.
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds
     * to the value given in the ontology vocabulary associated with this model, see
     * {@link Profile#ONTOLOGY}.
     * </p>
     *
     * @return An iterator over ontology resources.
     */
    public ExtendedIterator<Ontology> listOntologies();


    /**
     * <p>
     * Answer an iterator that ranges over the property resources in this model, i&#046;e&#046;
     * the resources with <code>rdf:type Property</code> or equivalent.  An <code>OntProperty</code>
     * is equivalent to an <code>rdfs:Property</code> in a normal RDF graph; this type is
     * provided as a common super-type for the more specific {@link ObjectProperty} and
     * {@link DatatypeProperty} property types.
     * </p>
     * <p><strong>Note</strong> This method searches for nodes in the underlying model whose
     * <code>rdf:type</code> is <code>rdf:Property</code>. This type is <em>entailed</em> by
     * specific property sub-types, such as <code>owl:ObjectProperty</code>. An important
     * consequence of this is that in <em>models without an attached reasoner</em> (e.g. in the
     * <code>OWL_MEM</code> {@link OntModelSpec}), the entailed type will not be present
     * and this method will omit such properties from the returned iterator. <br />
     * <strong>Solution</strong> There are two
     * ways to address to this issue: either use a reasoning engine to ensure that type entailments
     * are taking place correctly, or call {@link #listAllOntProperties()}. Note
     * that <code>listAllOntProperties</code> is potentially less efficient than this method.</p>
     * <p>
     * The resources returned by this iterator will those whose type corresponds
     * to the value given in the ontology vocabulary associated with this model.
     * </p>
     *
     * @return An iterator over property resources.
     */
    public ExtendedIterator<OntProperty> listOntProperties();


    /**
     * <p>Answer an iterator over all of the ontology properties in this model, including
     * object properties, datatype properties, annotation properties, etc. This method
     * takes a different approach to calculating the set of property resources to return,
     * and is robust against the absence of a reasoner attached to the model (see note
     * in {@link #listOntProperties()} for explanation). However, the calculation used by
     * this method is potentially less efficient than the alternative <code>listOntProperties()</code>.
     * Users whose models have an attached reasoner are recommended to use
     * {@link #listOntProperties()}.</p>
     * @return An iterator over all available properties in a model, irrespective of
     * whether a reasoner is available to perform <code>rdf:type</code> entailments.
     * Each property will appear exactly once in the iterator.
     */
    public ExtendedIterator<OntProperty> listAllOntProperties();

    /**
     * <p>
     * Answer an iterator that ranges over the object property resources in this model, i&#046;e&#046;
     * the resources with <code>rdf:type ObjectProperty</code> or equivalent.  An object
     * property is a property that is defined in the ontology language semantics as a
     * one whose range comprises individuals (rather than datatyped literals).
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#OBJECT_PROPERTY}.
     * </p>
     *
     * @return An iterator over object property resources.
     */
    public ExtendedIterator<ObjectProperty> listObjectProperties();


    /**
     * <p>
     * Answer an iterator that ranges over the datatype property resources in this model, i&#046;e&#046;
     * the resources with <code>rdf:type DatatypeProperty</code> or equivalent.  An datatype
     * property is a property that is defined in the ontology language semantics as a
     * one whose range comprises datatyped literals (rather than individuals).
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#DATATYPE_PROPERTY}.
     * </p>
     *
     * @return An iterator over datatype property resources.
     */
    public ExtendedIterator<DatatypeProperty> listDatatypeProperties();


    /**
     * <p>
     * Answer an iterator that ranges over the functional property resources in this model, i&#046;e&#046;
     * the resources with <code>rdf:type FunctionalProperty</code> or equivalent.  A functional
     * property is a property that is defined in the ontology language semantics as having
     * a unique domain element for each instance of the relationship.
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#FUNCTIONAL_PROPERTY}.
     * </p>
     *
     * @return An iterator over functional property resources.
     */
    public ExtendedIterator<FunctionalProperty> listFunctionalProperties();


    /**
     * <p>
     * Answer an iterator that ranges over the transitive property resources in this model, i&#046;e&#046;
     * the resources with <code>rdf:type TransitiveProperty</code> or equivalent.
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#TRANSITIVE_PROPERTY}.
     * </p>
     *
     * @return An iterator over transitive property resources.
     */
    public ExtendedIterator<TransitiveProperty> listTransitiveProperties();


    /**
     * <p>
     * Answer an iterator that ranges over the symmetric property resources in this model, i&#046;e&#046;
     * the resources with <code>rdf:type SymmetricProperty</code> or equivalent.
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#SYMMETRIC_PROPERTY}.
     * </p>
     *
     * @return An iterator over symmetric property resources.
     */
    public ExtendedIterator<SymmetricProperty> listSymmetricProperties();


    /**
     * <p>
     * Answer an iterator that ranges over the inverse functional property resources in this model, i&#046;e&#046;
     * the resources with <code>rdf:type InverseFunctionalProperty</code> or equivalent.
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#INVERSE_FUNCTIONAL_PROPERTY}.
     * </p>
     *
     * @return An iterator over inverse functional property resources.
     */
    public ExtendedIterator<InverseFunctionalProperty> listInverseFunctionalProperties();


    /**
     * <p>
     * Answer an iterator that ranges over the individual resources in this model, i&#046;e&#046;
     * the resources with <code>rdf:type</code> corresponding to a class defined
     * in the ontology.
     * </p>
     *
     * @return An iterator over individual resources.
     */
    public ExtendedIterator<Individual> listIndividuals();


    /**
     * <p>
     * Answer an iterator that ranges over the resources in this model that are
     * instances of the given class.
     * </p>
     *
     * @return An iterator over individual resources whose <code>rdf:type</code>
     * is <code>cls</code>.
     */
    public ExtendedIterator<Individual> listIndividuals( Resource cls );


    /**
     * <p>
     * Answer an iterator that ranges over all of the various forms of class description resource
     * in this model.  Class descriptions include {@linkplain #listEnumeratedClasses enumerated}
     * classes, {@linkplain #listUnionClasses union} classes, {@linkplain #listComplementClasses complement}
     * classes, {@linkplain #listIntersectionClasses intersection} classes, {@linkplain #listClasses named}
     * classes and {@linkplain #listRestrictions property restrictions}.
     * </p>
     * @return An iterator over class description resources.
     */
    public ExtendedIterator<OntClass> listClasses();


    /**
     * <p>Answer an iterator over the classes in this ontology model that represent
     * the uppermost nodes of the class hierarchy.  Depending on the underlying
     * reasoner configuration, if any, these will be calculated as the classes
     * that have Top (i.e. <code>owl:Thing</code>)
     * as a direct super-class, or the classes which have no declared super-class.</p>
     * @return An iterator of the root classes in the local class hierarchy
     */
    public ExtendedIterator<OntClass> listHierarchyRootClasses();


    /**
     * <p>
     * Answer an iterator that ranges over the enumerated class class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>oneOf</code> (or equivalent) and a list of values.
     * </p>
     *
     * @return An iterator over enumerated class resources.
     * @see Profile#ONE_OF
     */
    public ExtendedIterator<EnumeratedClass> listEnumeratedClasses();


    /**
     * <p>
     * Answer an iterator that ranges over the union class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>unionOf</code> (or equivalent) and a list of values.
     * </p>
     *
     * @return An iterator over union class resources.
     * @see Profile#UNION_OF
     */
    public ExtendedIterator<UnionClass> listUnionClasses();


    /**
     * <p>
     * Answer an iterator that ranges over the complement class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>complementOf</code> (or equivalent) and a list of values.
     * </p>
     *
     * @return An iterator over complement class resources.
     * @see Profile#COMPLEMENT_OF
     */
    public ExtendedIterator<ComplementClass> listComplementClasses();


    /**
     * <p>
     * Answer an iterator that ranges over the intersection class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>intersectionOf</code> (or equivalent) and a list of values.
     * </p>
     *
     * @return An iterator over complement class resources.
     * @see Profile#INTERSECTION_OF
     */
    public ExtendedIterator<IntersectionClass> listIntersectionClasses();


    /**
     * <p>
     * Answer an iterator that ranges over the named class-descriptions
     * in this model, i&#046;e&#046; resources with <code>rdf:type
     * Class</code> (or equivalent) and a node URI.
     * </p>
     *
     * @return An iterator over named class resources.
     */
    public ExtendedIterator<OntClass> listNamedClasses();


    /**
     * <p>
     * Answer an iterator that ranges over the property restriction class-descriptions
     * in this model, i&#046;e&#046; resources with <code>rdf:type
     * Restriction</code> (or equivalent).
     * </p>
     *
     * @return An iterator over restriction class resources.
     * @see Profile#RESTRICTION
     */
    public ExtendedIterator<Restriction> listRestrictions();


    /**
     * <p>
     * Answer an iterator that ranges over the properties in this model that are declared
     * to be annotation properties. Not all supported languages define annotation properties
     * (the category of annotation properties is chiefly an OWL innovation).
     * </p>
     *
     * @return An iterator over annotation properties.
     * @see Profile#getAnnotationProperties()
     */
    public ExtendedIterator<AnnotationProperty> listAnnotationProperties();


    /**
     * <p>
     * Answer an iterator that ranges over the nodes that denote pair-wise disjointness between
     * sets of classes.
     * </p>
     *
     * @return An iterator over AllDifferent nodes.
     */
    public ExtendedIterator<AllDifferent> listAllDifferent();


    /**
     * <p>Answer an iterator over the DataRange objects in this ontology, if there
     * are any.</p>
     * @return An iterator, whose values are {@link DataRange} objects.
     */
    public ExtendedIterator<DataRange> listDataRanges();


    /**
     * <p>
     * Answer a resource that represents an ontology description node in this model. If a resource
     * with the given URI exists in the model, and can be viewed as an Ontology, return the
     * Ontology facet, otherwise return null.
     * </p>
     *
     * @param uri The URI for the ontology node. Conventionally, this corresponds to the base URI
     * of the document itself.
     * @return An Ontology resource or null.
     */
    public Ontology getOntology( String uri );


    /**
     * <p>
     * Answer a resource that represents an Individual node in this model. If a resource
     * with the given URI exists in the model, and can be viewed as an Individual, return the
     * Individual facet, otherwise return null.
     * </p>
     *
     * @param uri The URI for the required individual
     * @return An Individual resource or null.
     */
    public Individual getIndividual( String uri );


    /**
     * <p>
     * Answer a resource representing an generic property in this model. If a property
     * with the given URI exists in the model, return the
     * OntProperty facet, otherwise return null.
     * </p>
     *
     * @param uri The URI for the property.
     * @return An OntProperty resource or null.
     */
    public OntProperty getOntProperty( String uri );


    /**
     * <p>
     * Answer a resource representing an object property in this model. If a resource
     * with the given URI exists in the model, and can be viewed as an ObjectProperty, return the
     * ObjectProperty facet, otherwise return null.
     * </p>
     *
     * @param uri The URI for the object property. May not be null.
     * @return An ObjectProperty resource or null.
     */
    public ObjectProperty getObjectProperty( String uri );


    /**
     * <p>Answer a resource representing a transitive property. If a resource
     * with the given URI exists in the model, and can be viewed as a TransitiveProperty, return the
     * TransitiveProperty facet, otherwise return null. </p>
     * @param uri The URI for the property. May not be null.
     * @return A TransitiveProperty resource or null
     */
    public TransitiveProperty getTransitiveProperty( String uri );


    /**
     * <p>Answer a resource representing a symmetric property. If a resource
     * with the given URI exists in the model, and can be viewed as a SymmetricProperty, return the
     * SymmetricProperty facet, otherwise return null. </p>
     * @param uri The URI for the property. May not be null.
     * @return A SymmetricProperty resource or null
     */
    public SymmetricProperty getSymmetricProperty( String uri );


    /**
     * <p>Answer a resource representing an inverse functional property. If a resource
     * with the given URI exists in the model, and can be viewed as a InverseFunctionalProperty, return the
     * InverseFunctionalProperty facet, otherwise return null. </p>
     * @param uri The URI for the property. May not be null.
     * @return An InverseFunctionalProperty resource or null
     */
    public InverseFunctionalProperty getInverseFunctionalProperty( String uri );


    /**
     * <p>
     * Answer a resource that represents datatype property in this model. . If a resource
     * with the given URI exists in the model, and can be viewed as a DatatypeProperty, return the
     * DatatypeProperty facet, otherwise return null.
     * </p>
     *
     * @param uri The URI for the datatype property. May not be null.
     * @return A DatatypeProperty resource or null
     */
    public DatatypeProperty getDatatypeProperty( String uri );


    /**
     * <p>
     * Answer a resource that represents an annotation property in this model. If a resource
     * with the given URI exists in the model, and can be viewed as an AnnotationProperty, return the
     * AnnotationProperty facet, otherwise return null.
     * </p>
     *
     * @param uri The URI for the annotation property. May not be null.
     * @return An AnnotationProperty resource or null
     */
    public AnnotationProperty getAnnotationProperty( String uri );

    /**
     * <p>Answer a resource presenting the {@link OntResource} facet, which has the given
     * URI. If no such resource is currently present in the model, return null.</p>
     * @param uri The URI of a resource
     * @return An OntResource with the given URI, or null
     */
    public OntResource getOntResource( String uri );

    /**
     * <p>Answer a resource presenting the {@link OntResource} facet, which
     * corresponds to the given resource but attached to this model.</p>
     * @param res An existing resource
     * @return An OntResource attached to this model that has the same URI
     * or anonID as the given resource
     */
    public OntResource getOntResource( Resource res );

    /**
     * <p>
     * Answer a resource that represents a class description node in this model. If a resource
     * with the given URI exists in the model, and can be viewed as an OntClass, return the
     * OntClass facet, otherwise return null.
     * </p>
     *
     * @param uri The URI for the class node, or null for an anonymous class.
     * @return An OntClass resource or null.
     */
    public OntClass getOntClass( String uri );


    /**
     * <p>Answer a resource representing the class that is the complement of another class. If a resource
     * with the given URI exists in the model, and can be viewed as a ComplementClass, return the
     * ComplementClass facet, otherwise return null. </p>
     * @param uri The URI of the new complement class.
     * @return A complement class or null
     */
    public ComplementClass getComplementClass( String uri );


    /**
     * <p>Answer a resource representing the class that is the enumeration of a list of individuals. If a resource
     * with the given URI exists in the model, and can be viewed as an EnumeratedClass, return the
     * EnumeratedClass facet, otherwise return null. </p>
     * @param uri The URI of the new enumeration class.
     * @return An enumeration class or null
     */
    public EnumeratedClass getEnumeratedClass( String uri );


    /**
     * <p>Answer a resource representing the class that is the union of a list of class descriptions. If a resource
     * with the given URI exists in the model, and can be viewed as a UnionClass, return the
     * UnionClass facet, otherwise return null. </p>
     * @param uri The URI of the new union class.
     * @return A union class description or null
     */
    public UnionClass getUnionClass( String uri );


    /**
     * <p>Answer a resource representing the class that is the intersection of a list of class descriptions. If a resource
     * with the given URI exists in the model, and can be viewed as a IntersectionClass, return the
     * IntersectionClass facet, otherwise return null. </p>
     * @param uri The URI of the new intersection class.
     * @return An intersection class description or null
     */
    public IntersectionClass getIntersectionClass( String uri );


    /**
     * <p>
     * Answer a resource that represents a property restriction in this model. If a resource
     * with the given URI exists in the model, and can be viewed as a Restriction, return the
     * Restriction facet, otherwise return null.
     * </p>
     *
     * @param uri The URI for the restriction node.
     * @return A Restriction resource or null
     */
    public Restriction getRestriction( String uri );


    /**
     * <p>Answer a class description defined as the class of those individuals that have the given
     * resource as the value of the given property. If a resource
     * with the given URI exists in the model, and can be viewed as a HasValueRestriction, return the
     * HasValueRestriction facet, otherwise return null. </p>
     *
     * @param uri The URI for the restriction
     * @return A resource representing a has-value restriction or null
     */
    public HasValueRestriction getHasValueRestriction( String uri );


    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * one property with a value belonging to the given class. If a resource
     * with the given URI exists in the model, and can be viewed as a SomeValuesFromRestriction, return the
     * SomeValuesFromRestriction facet, otherwise return null. </p>
     *
     * @param uri The URI for the restriction
     * @return A resource representing a some-values-from restriction, or null
     */
    public SomeValuesFromRestriction getSomeValuesFromRestriction( String uri );


    /**
     * <p>Answer a class description defined as the class of those individuals for which all values
     * of the given property belong to the given class. If a resource
     * with the given URI exists in the model, and can be viewed as an AllValuesFromResriction, return the
     * AllValuesFromRestriction facet, otherwise return null. </p>
     *
     * @param uri The URI for the restriction
     * @return A resource representing an all-values-from restriction or null
     */
    public AllValuesFromRestriction getAllValuesFromRestriction( String uri );


    /**
     * <p>Answer a class description defined as the class of those individuals that have exactly
     * the given number of values for the given property. If a resource
     * with the given URI exists in the model, and can be viewed as a CardinalityRestriction, return the
     * CardinalityRestriction facet, otherwise return null. </p>
     *
     * @param uri The URI for the restriction
     * @return A resource representing a has-value restriction, or null
     */
    public CardinalityRestriction getCardinalityRestriction( String uri );


    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * the given number of values for the given property. If a resource
     * with the given URI exists in the model, and can be viewed as a MinCardinalityRestriction, return the
     * MinCardinalityRestriction facet, otherwise return null. </p>
     *
     * @param uri The URI for the restriction
     * @return A resource representing a minimum cardinality restriction, or null
     */
    public MinCardinalityRestriction getMinCardinalityRestriction( String uri );


    /**
     * <p>Answer a class description defined as the class of those individuals that have at most
     * the given number of values for the given property. If a resource
     * with the given URI exists in the model, and can be viewed as a MaxCardinalityRestriction, return the
     * MaxCardinalityRestriction facet, otherwise return null.</p>
     *
     * @param uri The URI for the restriction
     * @return A resource representing a max-cardinality restriction, or null
     */
    public MaxCardinalityRestriction getMaxCardinalityRestriction( String uri );


    /**
     * <p>Answer a class description defined as the class of those individuals that have a property
     * p, all values of which are members of a given class. Typically used with a cardinality constraint.
     * If a resource
     * with the given URI exists in the model, and can be viewed as a QualifiedRestriction, return the
     * QualifiedRestriction facet, otherwise return null.</p>
     *
     * @param uri The URI for the restriction
     * @return A resource representing a qualified restriction, or null
     */
    public QualifiedRestriction getQualifiedRestriction( String uri );


    /**
     * <p>Answer a class description defined as the class of those individuals that have a property
     * p, with cardinality N, all values of which are members of a given class.
     * If a resource
     * with the given URI exists in the model, and can be viewed as a CardinalityQRestriction, return the
     * CardinalityQRestriction facet, otherwise return null.</p>
     *
     * @param uri The URI for the restriction
     * @return A resource representing a qualified cardinality restriction, or null
     */
    public CardinalityQRestriction getCardinalityQRestriction( String uri );


    /**
     * <p>Answer a class description defined as the class of those individuals that have a property
     * p, with minimum cardinality N, all values of which are members of a given class.
     * If a resource
     * with the given URI exists in the model, and can be viewed as a MinCardinalityQRestriction, return the
     * MinCardinalityQRestriction facet, otherwise return null.</p>
     *
     * @param uri The URI for the restriction
     * @return A resource representing a qualified minimum cardinality restriction, or null
     */
    public MinCardinalityQRestriction getMinCardinalityQRestriction( String uri );


    /**
     * <p>Answer a class description defined as the class of those individuals that have a property
     * p, with max cardinality N, all values of which are members of a given class.
     * If a resource
     * with the given URI exists in the model, and can be viewed as a MaxCardinalityQRestriction, return the
     * MaxCardinalityQRestriction facet, otherwise return null.</p>
     *
     * @param uri The URI for the restriction
     * @return A resource representing a qualified max cardinality restriction, or null
     */
    public MaxCardinalityQRestriction getMaxCardinalityQRestriction( String uri );


    /**
     * <p>
     * Answer a resource that represents an ontology description node in this model. If a resource
     * with the given URI exists in the model, it will be re-used.  If not, a new one is created in
     * the writable sub-model of the ontology model.
     * </p>
     *
     * @param uri The URI for the ontology node. Conventionally, this corresponds to the base URI
     * of the document itself.
     * @return An Ontology resource.
     */
    public Ontology createOntology( String uri );


    /**
     * <p>
     * Answer a resource that represents an <code>Individual</code> node in this model. A new anonymous resource
     * will be created in the writable sub-model of the ontology model.
     * </p>
     *
     * @param cls Resource representing the ontology class to which the individual belongs
     * @return A new anonymous Individual of the given class.
     */
    public Individual createIndividual( Resource cls );


    /**
     * <p>
     * Answer a resource that represents an Individual node in this model. If a resource
     * with the given URI exists in the model, it will be re-used.  If not, a new one is created in
     * the writable sub-model of the ontology model.
     * </p>
     *
     * @param cls Resource representing the ontology class to which the individual belongs
     * @param uri The URI for the individual, or null for an anonymous individual.
     * @return An Individual resource.
     */
    public Individual createIndividual( String uri, Resource cls );


    /**
     * <p>
     * Answer a resource representing an generic property in this model.  Effectively
     * this method is an alias for {@link #createProperty( String )}, except that
     * the return type is {@link OntProperty}, which allow more convenient access to
     * a property's position in the property hierarchy, domain, range, etc.
     * </p>
     *
     * @param uri The URI for the property. May not be null.
     * @return An OntProperty resource.
     */
    public OntProperty createOntProperty( String uri );


    /**
     * <p>
     * Answer a resource representing an object property in this model,
     * and that is not a functional property.
     * </p>
     *
     * @param uri The URI for the object property. May not be null.
     * @return An ObjectProperty resource.
     * @see #createObjectProperty( String, boolean )
     */
    public ObjectProperty createObjectProperty( String uri );


    /**
     * <p>
     * Answer a resource that represents an object property in this model.  An object property
     * is defined to have a range of individuals, rather than datatypes.
     * If a resource
     * with the given URI exists in the model, it will be re-used.  If not, a new one is created in
     * the writable sub-model of the ontology model.
     * </p>
     *
     * @param uri The URI for the object property. May not be null.
     * @param functional If true, the resource will also be typed as a {@link FunctionalProperty},
     * that is, a property that has a unique range value for any given domain value.
     * @return An ObjectProperty resource, optionally also functional.
     */
    public ObjectProperty createObjectProperty( String uri, boolean functional );


    /**
     * <p>Answer a resource representing a transitive property</p>
     * @param uri The URI for the property. May not be null.
     * @return An TransitiveProperty resource
     * @see #createTransitiveProperty( String, boolean )
     */
    public TransitiveProperty createTransitiveProperty( String uri );


    /**
     * <p>Answer a resource representing a transitive property, which is optionally
     * also functional. <strong>Note:</strong> although it is permitted in OWL full
     * to have functional transitive properties, it makes the language undecidable.
     * Functional transitive properties are not permitted in OWL-Lite or OWL DL.</p>
     * @param uri The URI for the property. May not be null.
     * @param functional If true, the property is also functional
     * @return An TransitiveProperty resource, optionally also functional.
     */
    public TransitiveProperty createTransitiveProperty( String uri, boolean functional );


    /**
     * <p>Answer a resource representing a symmetric property</p>
     * @param uri The URI for the property. May not be null.
     * @return An SymmetricProperty resource
     * @see #createSymmetricProperty( String, boolean )
     */
    public SymmetricProperty createSymmetricProperty( String uri );


    /**
     * <p>Answer a resource representing a symmetric property, which is optionally
     * also functional.</p>
     * @param uri The URI for the property. May not be null.
     * @param functional If true, the property is also functional
     * @return An SymmetricProperty resource, optionally also functional.
     */
    public SymmetricProperty createSymmetricProperty( String uri, boolean functional );


    /**
     * <p>Answer a resource representing an inverse functional property</p>
     * @param uri The URI for the property. May not be null.
     * @return An InverseFunctionalProperty resource
     * @see #createInverseFunctionalProperty( String, boolean )
     */
    public InverseFunctionalProperty createInverseFunctionalProperty( String uri );


    /**
     * <p>Answer a resource representing an inverse functional property, which is optionally
     * also functional.</p>
     * @param uri The URI for the property. May not be null.
     * @param functional If true, the property is also functional
     * @return An InverseFunctionalProperty resource, optionally also functional.
     */
    public InverseFunctionalProperty createInverseFunctionalProperty( String uri, boolean functional );

    /**
     * <p>
     * Answer a resource that represents datatype property in this model, and that is
     * not a functional property.
     * </p>
     *
     * @param uri The URI for the datatype property. May not be null.
     * @return A DatatypeProperty resource.
     * @see #createDatatypeProperty( String, boolean )
     */
    public DatatypeProperty createDatatypeProperty( String uri );


    /**
     * <p>
     * Answer a resource that represents datatype property in this model. A datatype property
     * is defined to have a range that is a concrete datatype, rather than an individual.
     * If a resource
     * with the given URI exists in the model, it will be re-used.  If not, a new one is created in
     * the writable sub-model of the ontology model.
     * </p>
     *
     * @param uri The URI for the datatype property. May not be null.
     * @param functional If true, the resource will also be typed as a {@link FunctionalProperty},
     * that is, a property that has a unique range value for any given domain value.
     * @return A DatatypeProperty resource.
     */
    public DatatypeProperty createDatatypeProperty( String uri, boolean functional );


    /**
     * <p>
     * Answer a resource that represents an annotation property in this model. If a resource
     * with the given URI exists in the model, it will be re-used.  If not, a new one is created in
     * the writable sub-model of the ontology model.
     * </p>
     *
     * @param uri The URI for the annotation property. May not be null.
     * @return An AnnotationProperty resource.
     */
    public AnnotationProperty createAnnotationProperty( String uri );


    /**
     * <p>
     * Answer a resource that represents an anonymous class description in this model. A new
     * anonymous resource of <code>rdf:type C</code>, where C is the class type from the
     * language profile.
     * </p>
     *
     * @return An anonymous Class resource.
     */
    public OntClass createClass();


    /**
     * <p>
     * Answer a resource that represents a class description node in this model. If a resource
     * with the given URI exists in the model, it will be re-used.  If not, a new one is created in
     * the writable sub-model of the ontology model.
     * </p>
     *
     * @param uri The URI for the class node, or null for an anonymous class.
     * @return A Class resource.
     */
    public OntClass createClass( String uri );


    /**
     * <p>Answer a resource representing the class that is the complement of the given argument class</p>
     * @param uri The URI of the new complement class, or null for an anonymous class description.
     * @param cls Resource denoting the class that the new class is a complement of
     * @return A complement class
     */
    public ComplementClass createComplementClass( String uri, Resource cls );


    /**
     * <p>Answer a resource representing the class that is the enumeration of the given list of individuals</p>
     * @param uri The URI of the new enumeration class, or null for an anonymous class description.
     * @param members An optional list of resources denoting the individuals in the enumeration, or null.
     * @return An enumeration class
     */
    public EnumeratedClass createEnumeratedClass( String uri, RDFList members );


    /**
     * <p>Answer a resource representing the class that is the union of the given list of class descriptions</p>
     * @param uri The URI of the new union class, or null for an anonymous class description.
     * @param members A list of resources denoting the classes that comprise the union
     * @return A union class description
     */
    public UnionClass createUnionClass( String uri, RDFList members );


    /**
     * <p>Answer a resource representing the class that is the intersection of the given list of class descriptions.</p>
     * @param uri The URI of the new intersection class, or null for an anonymous class description.
     * @param members A list of resources denoting the classes that comprise the intersection
     * @return An intersection class description
     */
    public IntersectionClass createIntersectionClass( String uri, RDFList members );


    /**
     * <p>
     * Answer a resource that represents an anonymous property restriction in this model. A new
     * anonymous resource of <code>rdf:type R</code>, where R is the restriction type from the
     * language profile.
     * </p>
     * @param p The property that is restricted by this restriction, or null to omit from the restriction
     * @return An anonymous Restriction resource.
     */
    public Restriction createRestriction( Property p );


    /**
     * <p>
     * Answer a resource that represents a property restriction in this model. If a resource
     * with the given URI exists in the model, it will be re-used.  If not, a new one is created in
     * the writable sub-model of the ontology model.
     * </p>
     *
     * @param uri The URI for the restriction node, or null for an anonymous restriction.
     * @param p The property that is restricted by this restriction, or null to omit from the restriction
     * @return A Restriction resource.
     */
    public Restriction createRestriction( String uri, Property p );


    /**
     * <p>Answer a class description defined as the class of those individuals that have the given
     * resource as the value of the given property</p>
     *
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param value The value of the property, as a resource or RDF literal
     * @return A new resource representing a has-value restriction
     */
    public HasValueRestriction createHasValueRestriction( String uri, Property prop, RDFNode value );


    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * one property with a value belonging to the given class</p>
     *
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cls The class to which at least one value of the property belongs
     * @return A new resource representing a some-values-from restriction
     */
    public SomeValuesFromRestriction createSomeValuesFromRestriction( String uri, Property prop, Resource cls );


    /**
     * <p>Answer a class description defined as the class of those individuals for which all values
     * of the given property belong to the given class</p>
     *
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cls The class to which any value of the property belongs
     * @return A new resource representing an all-values-from restriction
     */
    public AllValuesFromRestriction createAllValuesFromRestriction( String uri, Property prop, Resource cls );


    /**
     * <p>Answer a class description defined as the class of those individuals that have exactly
     * the given number of values for the given property.</p>
     *
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The exact cardinality of the property
     * @return A new resource representing a cardinality restriction
     */
    public CardinalityRestriction createCardinalityRestriction( String uri, Property prop, int cardinality );


    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * the given number of values for the given property.</p>
     *
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The minimum cardinality of the property
     * @return A new resource representing a min-cardinality restriction
     */
    public MinCardinalityRestriction createMinCardinalityRestriction( String uri, Property prop, int cardinality );


    /**
     * <p>Answer a class description defined as the class of those individuals that have at most
     * the given number of values for the given property.</p>
     *
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The maximum cardinality of the property
     * @return A new resource representing a max-cardinality restriction
     */
    public MaxCardinalityRestriction createMaxCardinalityRestriction( String uri, Property prop, int cardinality );


    /**
     * <p>Answer a class description defined as the class of those individuals that have at most
     * the given number of values for the given property, all values of which belong to the given
     * class.</p>
     *
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The maximum cardinality of the property
     * @param cls The class to which all values of the restricted property should belong
     * @return A new resource representing a max-cardinality-q restriction
     */
    public MaxCardinalityQRestriction createMaxCardinalityQRestriction( String uri, Property prop, int cardinality, OntClass cls );


    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * the given number of values for the given property, all values of which belong to the given
     * class.</p>
     *
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The minimum cardinality of the property
     * @param cls The class to which all values of the restricted property should belong
     * @return A new resource representing a min-cardinality-q restriction
     */
    public MinCardinalityQRestriction createMinCardinalityQRestriction( String uri, Property prop, int cardinality, OntClass cls );


    /**
     * <p>Answer a class description defined as the class of those individuals that have exactly
     * the given number of values for the given property, all values of which belong to the given
     * class.</p>
     *
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The cardinality of the property
     * @param cls The class to which all values of the restricted property should belong
     * @return A new resource representing a cardinality-q restriction
     */
    public CardinalityQRestriction createCardinalityQRestriction( String uri, Property prop, int cardinality, OntClass cls );


    /**
     * <p>Answer a data range defined as the given set of concrete data values.  DataRange resources
     * are necessarily bNodes.</p>
     *
     * @param literals A list of literals that will be the members of the data range,
     *                 or null to define an empty data range
     * @return A new data range containing the given literals as permissible values
     */
    public DataRange createDataRange( RDFList literals );


    /**
     * <p>
     * Answer a new, anonymous node representing the fact that a given set of classes are all
     * pair-wise distinct.  <code>AllDifferent</code> is a feature of OWL only, and is something
     * of an anomaly in that it exists only to give a place to anchor the <code>distinctMembers</code>
     * property, which is the actual expression of the fact.
     * </p>
     *
     * @return A new AllDifferent resource
     */
    public AllDifferent createAllDifferent();


    /**
     * <p>
     * Answer a new, anonymous node representing the fact that a given set of classes are all
     * pair-wise distinct.  <code>AllDifferent</code> is a feature of OWL only, and is something
     * of an anomaly in that it exists only to give a place to anchor the <code>distinctMembers</code>
     * property, which is the actual expression of the fact.
     * </p>
     * @param differentMembers A list of the class expressions that denote a set of mutually disjoint classes
     * @return A new AllDifferent resource
     */
    public AllDifferent createAllDifferent( RDFList differentMembers );


    /**
     * <p>
     * Answer a resource that represents a generic ontology node in this model. If a resource
     * with the given URI exists in the model, it will be re-used.  If not, a new one is created in
     * the writable sub-model of the ontology model.
     * </p>
     * <p>
     * This is a generic method for creating any known ontology value.  The selector that determines
     * which resource to create is the same as as the argument to the {@link RDFNode#as as()}
     * method: the Java class object of the desired abstraction.  For example, to create an
     * ontology class via this mechanism, use:
     * <code><pre>
     *     OntClass c = (OntClass) myModel.createOntResource( OntClass.class, null,
     *                                                        "http://example.org/ex#Parrot" );
     * </pre></code>
     * </p>
     *
     * @param javaClass The Java class object that represents the ontology abstraction to create
     * @param rdfType Optional resource denoting the ontology class to which an individual or
     * axiom belongs, if that is the type of resource being created.
     * @param uri The URI for the ontology resource, or null for an anonymous resource.
     * @return An ontology resource, of the type specified by the <code>javaClass</code>
     */
    public <T extends OntResource> T createOntResource( Class<T> javaClass, Resource rdfType, String uri );

    /**
     * <p>Answer a resource presenting the {@link OntResource} facet, which has the
     * given URI.</p>
     * @param uri The URI of the resource, or null for an anonymous resource (i.e. <em>bNode</em>)
     * @return An OntResource with the given URI
     */
    public OntResource createOntResource( String uri );

    /**
     * <p>Determine which models this model imports (by looking for, for example,
     * <code>owl:imports</code> statements, and load each of those models as an
     * import. A check is made to determine if a model has already been imported,
     * if so, the import is ignored. Thus this method is safe against circular
     * sets of import statements. Note that actual implementation is delegated to
     * the associated {@link OntDocumentManager}.
     */
    public void loadImports();

    /**
     * <p>
     * Answer a list of the imported URI's in this ontology model. Detection of <code>imports</code>
     * statements will be according to the local language profile.  Note that, in order to allow this
     * method to be called during the imports closure process, we <b>only query the base model</b>,
     * thus side-stepping the any attached reasoner.
     * </p>
     *
     * @return The imported ontology URI's as a set. Note that since the underlying graph is
     * not ordered, the order of values in the list in successive calls to this method is
     * not guaranteed to be preserved.
     */
    public Set<String> listImportedOntologyURIs();


    /**
     * <p>
     * Answer a list of the imported URI's in this ontology model, and optionally in the closure
     * of this model's imports. Detection of <code>imports</code>
     * statements will be according to the local language profile.  Note that, in order to allow this
     * method to be called during the imports closure process, we <b>only query the base model</b>,
     * thus side-stepping the any attached reasoner.
     * </p>
     * @param closure If true, the set of URI's returned will include not only those directly
     * imported by this model, but those imported by the model's imports, and so on transitively.
     * @return A set of imported ontology URIs. Note that since the underlying graph is
     * not ordered, the order of values in the list in successive calls to this method is
     * not guaranteed to be preserved.
     */
    public Set<String> listImportedOntologyURIs( boolean closure );


    /**
     * <p>
     * Answer true if this model has had the given URI document imported into it. This is
     * important to know since an import only occurs once, and we also want to be able to
     * detect cycles of imports.
     * </p>
     *
     * @param uri An ontology URI
     * @return True if the document corresponding to the URI has been successfully loaded
     * into this model
     */
    public boolean hasLoadedImport( String uri );


    /**
     * <p>
     * Record that this model has now imported the document with the given
     * URI, so that it will not be re-imported in the future.
     * </p>
     *
     * @param uri A document URI that has now been imported into the model.
     */
    public void addLoadedImport( String uri );


    /**
     * <p>
     * Record that this model no longer imports the document with the given
     * URI.
     * </p>
     *
     * @param uri A document URI that is no longer imported into the model.
     */
    public void removeLoadedImport( String uri );


    /**
     * <p>
     * Answer the language profile (for example, OWL or DAML+OIL) that this model is
     * working to.
     * </p>
     *
     * @return A language profile
     */
    public Profile getProfile();


    /**
     * <p>
     * Answer the model maker associated with this model (used for constructing the
     * constituent models of the imports closure).
     * </p>
     * @deprecated use getImportModelMaker instead for consistency with name
     * changes to OntModelSpec to avoid ambiguity with base vs import makers.
     *
     * @return The local model maker
     */
    @Deprecated
    public ModelMaker getModelMaker();

    /**
     * <p>
     * Answer the model maker associated with this model (used for constructing the
     * constituent models of the imports closure).
     * </p>
     *
     * @return The local model maker
     */
    public ModelMaker getImportModelMaker();

    /**
     * <p>i.next()
     * Answer the sub-graphs of this model. A sub-graph is defined as a graph that
     * is used to contain the triples from an imported document.
     * </p>
     *
     * @return A list of graphs that are contained in this ontology model
     */
    public List<Graph> getSubGraphs();


    /**
     * <p>Answer an iterator over the ontologies that this ontology imports,
     * each of which will have been wrapped as an ontology model using the same
     * {@link OntModelSpec} as this model.  If this model has no imports,
     * the iterator will be non-null but will not have any values.</p>
     * @return An iterator, each value of which will be an <code>OntModel</code>
     * representing an imported ontology.
     * @deprecated This method has been re-named to <code>listSubModels</code>,
     * but note that to obtain the same behaviour as <code>listImportedModels</code>
     * from Jena 2.4 and earlier, callers should invoke {@link #listSubModels(boolean)}
     * with parameter <code>true</code>.
     * @see #listSubModels()
     * @see #listSubModels(boolean)
     */
    @Deprecated
    public ExtendedIterator<OntModel> listImportedModels();


    /**
     * <p>Answer an iterator over the ontology models that are sub-models of
     * this model. Sub-models are used, for example, to represent composite
     * documents such as the imports of a model. So if ontology A imports
     * ontologies B and C, each of B and C will be available as one of
     * the sub-models of the model containing A. This method replaces the
     * older {@link #listImportedModels}. Note that to fully replicate
     * the behaviour of <code>listImportedModels</code>, the
     * <code>withImports</code> flag must be set to true. Each model
     * returned by this method will have been wrapped as an ontology model using the same
     * {@link OntModelSpec} as this model.  If this model has no sub-models,
     * the returned iterator will be non-null but will not have any values.</p>
     *
     * @param withImports If true, each sub-model returned by this method
     * will also include its import models. So if model A imports D, and D
     * imports D, when called with <code>withImports</code> set to true, the
     * return value for <code>modelA.listSubModels(true)</code> will be an
     * iterator, whose only value is a model for D, and that model will contain
     * a sub-model representing the import of E. If <code>withImports</code>
     * is false, E will not be included as a sub-model of D.
     * @return An iterator, each value of which will be an <code>OntModel</code>
     * representing a sub-model of this ontology.
     */
    public ExtendedIterator<OntModel> listSubModels( boolean withImports );


    /**
     * <p>Answer an iterator over the ontology models that are sub-models of
     * this model. Sub-models are used, for example, to represent composite
     * documents such as the imports of a model. So if ontology A imports
     * ontologies B and C, each of B and C will be available as one of
     * the sub-models of the model containing A.
     * <strong>Important note on behaviour change:</strong> please see
     * the comment on {@link #listSubModels(boolean)} for explanation
     * of the <code>withImports</code> flag. This zero-argument form
     * of <code>listSubModels</code> sets <code>withImports</code> to
     * false, so the returned models will not themselves contain imports.
     * This behaviour differs from the zero-argument method
     * {@link #listImportedModels()} in Jena 2.4 an earlier.</p>
     * @return An iterator, each value of which will be an <code>OntModel</code>
     * representing a sub-model of this ontology.
     * @see #listSubModels(boolean)
     */
    public ExtendedIterator<OntModel> listSubModels();

    /**
     * <p>Answer the number of sub-models of this model, not including the
     * base model.</p>
     * @return The number of sub-models, &ge; zero.
     */
    public int countSubModels();

    /**
     * <p>Answer an <code>OntModel</code> representing the imported ontology
     * with the given URI. If an ontology with that URI has not been imported,
     * answer null.</p>
     * @param uri The URI of an ontology that may have been imported into the
     * ontology represented by this model
     * @return A model representing the imported ontology with the given URI, or
     * null.
     */
    public OntModel getImportedModel( String uri );


    /**
     * <p>
     * Answer the base model of this model. The base model is the model
     * that contains the triples read from the source document for this
     * ontology.  It is therefore this base model that will be updated if statements are
     * added to a model that is built from a union of documents (via the
     * <code>imports</code> statements in the source document).
     * </p>
     *
     * @return The base model for this ontology model
     */
    public Model getBaseModel();


    /**
     * <p>
     * Add the given model as one of the sub-models of the enclosed ontology union model.    Will
     * cause the associated inference engine (if any) to update, so this may be
     * an expensive operation in some cases.
     * </p>
     *
     * @param model A sub-model to add
     * @see #addSubModel( Model, boolean )
     */
    public void addSubModel( Model model );


    /**
     * <p>
     * Add the given model as one of the sub-models of the enclosed ontology union model.
     * </p>
     *
     * @param model A sub-model to add
     * @param rebind If true, rebind any associated inferencing engine to the new data (which
     * may be an expensive operation)
     */
    public void addSubModel( Model model, boolean rebind );


    /**
     * <p>
     * Remove the given model as one of the sub-models of the enclosed ontology union model.    Will
     * cause the associated inference engine (if any) to update, so this may be
     * an expensive operation in some cases.
     * </p>
     *
     * @param model A sub-model to remove
     * @see #addSubModel( Model, boolean )
     */
    public void removeSubModel( Model model );


    /**
     * <p>
     * Remove the given model as one of the sub-models of the enclosed ontology union model.
     * </p>
     *
     * @param model A sub-model to remove
     * @param rebind If true, rebind any associated inferencing engine to the new data (which
     * may be an expensive operation)
     */
    public void removeSubModel( Model model, boolean rebind );


    /**
     * <p>Answer true if the given node is a member of the base model of this ontology model.
     * This is an important distinction, because only the base model receives updates when the
     * ontology model is updated. Thus, removing properties of a resource that is not in the base
     * model will not actually side-effect the overall model.</p>
     * @param node An RDF node (Resource, Property or Literal) to test
     * @return True if the given node is from the base model
     */
    public boolean isInBaseModel( RDFNode node );


    /**
     * <p>Answer true if the given statement is defined in the base model of this ontology model.
     * This is an important distinction, because only the base model receives updates when the
     * ontology model is updated. Thus, removing a statement that is not in the base
     * model will not actually side-effect the overall model.</p>
     * @param stmt A statement to test
     * @return True if the given statement is from the base model
     */
    public boolean isInBaseModel( Statement stmt );


    /**
     * <p>
     * Answer true if this model is currently in <i>strict checking mode</i>. Strict
     * mode means
     * that converting a common resource to a particular language element, such as
     * an ontology class, will be subject to some simple syntactic-level checks for
     * appropriateness.
     * </p>
     *
     * @return True if in strict checking mode
     */
    public boolean strictMode();


    /**
     * <p>
     * Set the checking mode to strict or non-strict.
     * </p>
     *
     * @param strict
     * @see #strictMode()
     */
    public void setStrictMode( boolean strict );


    /**
     * <p>Set the flag that controls whether adding or removing <i>imports</i>
     * statements into the
     * model will result in the imports closure changing dynamically.</p>
     * @param dynamic If true, adding or removing an imports statement to the
     * model will result in a change in the imports closure.  If false, changes
     * to the imports are not monitored dynamically. Default false.
     */
    public void setDynamicImports( boolean dynamic );


    /**
     * <p>Answer true if the imports closure of the model will be dynamically
     * updated as imports statements are added and removed.</p>
     * @return True if the imports closure is updated dynamically.
     */
    public boolean getDynamicImports();


    /**
     * <p>
     * Answer a reference to the document manager that this model is using to manage
     * ontology &lt;-&gt; mappings, and to load the imports closure. <strong>Note</strong>
     * by default, an ontology model is constructed with a reference to the shared,
     * global document manager.  Thus changing the settings via this model's document
     * manager may affect other models also using the same instance.
     * </p>
     * @return A reference to this model's document manager
     */
    public OntDocumentManager getDocumentManager();


    /**
     * <p>Answer the ontology model specification that was used to construct this model</p>
     * @return An ont model spec instance.
     */
    public OntModelSpec getSpecification();


    // output operations

    /**
     * <p>Write the model as an XML document.
     * It is often better to use an OutputStream rather than a Writer, since this
     * will avoid character encoding errors.
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write
     * all triples, including imported data and inferred triples, use
     * {@link #writeAll( Writer, String, String ) writeAll }.
     * </p>
     *
     * @param writer A writer to which the XML will be written
     * @return this model
     */
    @Override
    public Model write( Writer writer ) ;

    /**
     * <p>Write a serialized representation of a model in a specified language.
     * It is often better to use an OutputStream rather than a Writer, since this
     * will avoid character encoding errors.
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write
     * all triples, including imported data and inferred triples, use
     * {@link #writeAll( Writer, String, String ) writeAll }.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "TURTLE".  The default value,
     * represented by <code>null</code> is "RDF/XML".</p>
     * @param writer The output writer
     * @param lang The output language
     * @return this model
     */
    @Override
    public Model write( Writer writer, String lang ) ;

    /**
     * <p>Write a serialized representation of a model in a specified language.
     * It is often better to use an OutputStream rather than a Writer,
     * since this will avoid character encoding errors.
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write
     * all triples, including imported data and inferred triples, use
     * {@link #writeAll( Writer, String, String ) writeAll }.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "TURTLE".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param writer The output writer
     * @param base The base URI for relative URI calculations.
     * <code>null</code> means use only absolute URI's.
     * @param lang The language in which the RDF should be written
     * @return this model
     */
    @Override
    public Model write( Writer writer, String lang, String base );

    /**
     * <p>Write a serialization of this model as an XML document.
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write
     * all triples, including imported data and inferred triples, use
     * {@link #writeAll( OutputStream, String, String ) writeAll }.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "TURTLE".  The default value is
     * represented by <code>null</code> is "RDF/XML".</p>
     * @param out The output stream to which the XML will be written
     * @return This model
     */
    @Override
    public Model write( OutputStream out );

    /**
     * <p>Write a serialized representation of this model in a specified language.
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write
     * all triples, including imported data and inferred triples, use
     * {@link #writeAll( OutputStream, String, String ) writeAll }.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "TURTLE".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param out The output stream to which the RDF is written
     * @param lang The output language
     * @return This model
     */
    @Override
    public Model write( OutputStream out, String lang );

    /**
     * <p>Write a serialized representation of a model in a specified language.
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write
     * all triples, including imported data and inferred triples, use
     * {@link #writeAll( OutputStream, String, String ) writeAll }.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "TURTLE".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param out The output stream to which the RDF is written
     * @param base The base URI to use when writing relative URI's. <code>null</code>
     * means use only absolute URI's.
     * @param lang The language in which the RDF should be written
     * @return This model
     */
    @Override
    public Model write( OutputStream out, String lang, String base );

    /**
     * <p>Write a serialized representation of all of the contents of the model,
     * including inferred statements and statements imported from other
     * documents.  To write only the data asserted in the base model, use
     * {@link #write( Writer, String, String ) write}.
     * It is often better to use an OutputStream rather than a Writer,
     * since this will avoid character encoding errors.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "TURTLE".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param writer The output writer
     * @param lang The language in which the RDF should be written
     * @param base The base URI for relative URI calculations.
     * <code>null</code> means use only absolute URI's.
     * @return This model
     */
    public Model writeAll( Writer writer, String lang, String base );

    /**
     * <p>Write a serialized representation of all of the contents of the model,
     * including inferred statements and statements imported from other
     * documents.  To write only the data asserted in the base model, use
     * {@link #write( OutputStream, String, String ) write}.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "TURTLE".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param out The output stream to which the RDF is written
     * @param lang The language in which the RDF should be written
     * @param base The base URI to use when writing relative URI's. <code>null</code>
     * means use only absolute URI's.
     * @return This model
     */
    public Model writeAll( OutputStream out, String lang, String base );
    
    /**
     * <p>Write a serialized representation of all of the contents of the model,
     * including inferred statements and statements imported from other
     * documents.  To write only the data asserted in the base model, use
     * {@link #write( Writer, String ) write}.
     * It is often better to use an OutputStream rather than a Writer,
     * since this will avoid character encoding errors.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "TURTLE".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param writer The output writer
     * @param lang The language in which the RDF should be written
     * @return This model
     */
    public Model writeAll( Writer writer, String lang );

    /**
     * <p>Write a serialized representation of all of the contents of the model,
     * including inferred statements and statements imported from other
     * documents.  To write only the data asserted in the base model, use
     * {@link #write( OutputStream, String ) write}.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "TURTLE".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param out The output stream to which the RDF is written
     * @param lang The language in which the RDF should be written
     * @return This model
     */
    public Model writeAll( OutputStream out, String lang );
}

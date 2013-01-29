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
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.Iterator;



/**
 * <p>
 * Interface that encapsulates the elements of a general vocabulary
 * corresponding to a particular ontology language.  The intent is that, using
 * a given vocabulary, a given RDF model can be processed as an ontology
 * description, without binding knowledge of the vocabulary into this Java
 * package. For tractability, this limits the vocabularies that can easily be
 * represented to those that are similar to OWL and DAML+OIL.
 * </p>
 */
public interface Profile
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////


    /**
     * <p>
     * Answer the string that is the namespace prefix for this vocabulary
     * </p>
     * 
     * @return The namespace prefix, for example <code>http://www.w3c.org/2002/07/owl#</code>
     */
    public String NAMESPACE();
    
    
    // Language classes 
    ////////////////////////////////
    
    
    /**
     * <p>
     * Answer the resource that represents the class 'class' in this vocabulary.
     * </p>
     * 
     * @return The resource that represents the concept of a class
     */
    public Resource CLASS();
    
    
    /**
     * <p>
     * Answer the resource that represents the a class formed by placing
     * constraints (restrictions) on the values of a property.
     * </p>
     * 
     * @return The resource that represents the concept of a restriction
     */
    public Resource RESTRICTION();
    
    
    /**
     * <p>
     * Answer the resource that represents the class all individuals.
     * </p>
     * 
     * @return The resource that represents the concept of the <i>top</i> class
     */
    public Resource THING();
    
    
    /**
     * <p>
     * Answer the resource that represents the necessarily empty class.
     * </p>
     * 
     * @return The resource that represents the concept the <i>bottom</i> class.
     */
    public Resource NOTHING();
    
    
    /**
     * <p>
     * Answer the resource that represents the general class of properties. This will
     * typically be <code>rdf:Property</code>.
     * </p>
     * 
     * @return The resource that represents the concept of a property.
     */
    public Resource PROPERTY();
    
    
    /**
     * <p>
     * Answer the resource that represents the class of properties whose range
     * elements are individuals (not literals)
     * </p>
     * 
     * @return The resource that represents the concept of an object (individual) property.
     */
    public Resource OBJECT_PROPERTY();
    
    
    /**
     * <p>
     * Answer the resource that represents the class of properties whose range
     * elements are literals (not individuals)
     * </p>
     * 
     * @return The resource that represents the concept of an object (individual) property.
     */
    public Resource DATATYPE_PROPERTY();
    
    
    /**
     * <p>
     * Answer the resource that represents the class of properties that apply <i>transitively</i>.
     * </p>
     * 
     * @return The resource that represents the concept of a transitive property.
     */
    public Resource TRANSITIVE_PROPERTY();
    
    
    /**
     * <p>
     * Answer the resource that represents the class of properties that are <i>symmetric</i>.
     * </p>
     * 
     * @return The resource that represents the concept of a symmetric property.
     */
    public Resource SYMMETRIC_PROPERTY();
    
    
    /**
     * <p>
     * Answer the resource that represents the class of properties that are <i>functional</i>,
     * i&#046;e&#046; whose range is unique for a given domain element.
     * </p>
     * 
     * @return The resource that represents the concept of a functional property.
     */
    public Resource FUNCTIONAL_PROPERTY();
    
    
    /**
     * <p>
     * Answer the resource that represents the class of properties that are 
     * <i>inverse functional</i>,
     * i&#046;e&#046; whose domain is unique for a given range element.
     * </p>
     * 
     * @return The resource that represents the concept of an inverse functional property.
     */
    public Resource INVERSE_FUNCTIONAL_PROPERTY();
    
    
    /**
     * <p>
     * Answer the resource that represents the class of axioms denoting that a set of
     * individuals are pairwise distinct. 
     * </p>
     * 
     * @return The resource that represents the concept of an all-different axiom.
     */
    public Resource ALL_DIFFERENT();
    
    
    /**
     * <p>
     * Answer the resource that represents the class of ontology header elements. Individuals
     * of this class typically associate meta-data about an ontology document with the 
     * classes and properties in the document.
     * </p>
     * 
     * @return The resource that represents the concept of an ontology header element.
     */
    public Resource ONTOLOGY();
    
    
    /**
     * <p>
     * Answer the resource that represents the documentation class of deprecated 
     * classes.  Belonging to this class is a hint to developers that a given class
     * has been superceded in a later revision of the ontology.
     * </p>
     * 
     * @return The resource that represents the concept of a deprecated class.
     */
    public Resource DEPRECATED_CLASS();
    
    
    /**
     * <p>
     * Answer the resource that represents the documentation class of deprecated 
     * properties.  Belonging to this class is a hint to developers that a given property
     * has been superceded in a later revision of the ontology.
     * </p>
     * 
     * @return The resource that represents the concept of a deprecated property.
     */
    public Resource DEPRECATED_PROPERTY();
    
    
    /**
     * <p>
     * Answer the class that denotes an annotation property
     * </p>
     * 
     * @return The AnnotationProperty class
     */
    public Resource ANNOTATION_PROPERTY();
    
    /**
     * <p>
     * Answer the class that denotes an ontology property
     * </p>
     * 
     * @return The OntologyProperty class
     */
    public Resource ONTOLOGY_PROPERTY();
    
    /**
     * <p>
     * Answer the class that defines a closed range of concrete data values.
     * </p>
     * @return The DataRange class
     */
    public Resource DATARANGE();
    
    /**
     * <p>
     * Answer the predicate that denotes that one property has the same property
     * extension as another.
     * </p>
     * 
     * @return The property that denotes equivalence between two property resources.
     */
    public Property EQUIVALENT_PROPERTY();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one class has the same extension as another.
     * </p>
     * 
     * @return The property that denotes equivalence between two class expressions.
     */
    public Property EQUIVALENT_CLASS();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one class has no individuals in its 
     * extension in common with another class.
     * </p>
     * 
     * @return The property that denotes disjointness between two class expressions.
     */
    public Property DISJOINT_WITH();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one resource represents the same
     * individual as another.
     * </p>
     * 
     * @return The property that denotes equivalence between two resources denoting 
     * individuals.
     */
    public Property SAME_INDIVIDUAL_AS();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one resource represents the same
     * ontology object as another.
     * </p>
     * 
     * @return The property that denotes equivalence between two resources.
     */
    public Property SAME_AS();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one resource represents a different
     * individual than another resource.
     * </p>
     * 
     * @return The property that denotes distinctness between two individuals.
     */
    public Property DIFFERENT_FROM();
    
    
    /**
     * <p>
     * Answer the predicate that maps from an {@link #ALL_DIFFERENT} 
     * axiom to the set of individuals that are pair-wise different from
     * each other.
     * </p>
     * 
     * @return The property that introduces a list of individuals that are distinct.
     */
    public Property DISTINCT_MEMBERS();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one class is formed from the union
     * (disjunction) of a set of others.
     * </p>
     * 
     * @return The property that denotes a class defined by a union of class expressions.
     */
    public Property UNION_OF();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one class is formed from the intersection
     * (conjunction) of a set of others.
     * </p>
     * 
     * @return The property that denotes a class defined by an intersection of class expressions.
     */
    public Property INTERSECTION_OF();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one class comprises the individuals that are
     * not in a second class.
     * </p>
     * 
     * @return The property that denotes a class defined by the complement of a class expression.
     */
    public Property COMPLEMENT_OF();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that a class comprises exactly one of a given
     * closed set individuals.
     * </p>
     * 
     * @return The property that denotes a class defined its members being one of a give set.
     */
    public Property ONE_OF();
    
    
    /**
     * <p>
     * Answer the predicate that maps from a {@link #RESTRICTION} to a property that it is
     * a restriction on.
     * </p>
     * 
     * @return The property that denotes a property that a restriction applies to.
     */
    public Property ON_PROPERTY();
    
    
    /**
     * <p>
     * Answer the predicate that denotes a restriction on a given property to 
     * have only values from the given class expression.
     * </p>
     * 
     * @return The property that denotes a local property range restriction.
     */
    public Property ALL_VALUES_FROM();
    
    
    /**
     * <p>
     * Answer the predicate that denotes a restriction on a given property to 
     * have a given value.
     * </p>
     * 
     * @return The property that denotes a local property value restriction.
     */
    public Property HAS_VALUE();
    
    
    /**
     * <p>
     * Answer the predicate that denotes a restriction on a given property to 
     * have at least one value from the given class expression.
     * </p>
     * 
     * @return The property that denotes a local property range restriction.
     */
    public Property SOME_VALUES_FROM();
    
    
    /**
     * <p>
     * Answer the predicate that denotes a restriction on a given property to 
     * have at least a certain number of values
     * </p>
     * 
     * @return The property that denotes a local property cardinality lower bound.
     */
    public Property MIN_CARDINALITY();
    
    
    /**
     * <p>
     * Answer the predicate that denotes a restriction on a given property to 
     * have at most a certain number of values
     * </p>
     * 
     * @return The property that denotes a local property cardinality upper bound.
     */
    public Property MAX_CARDINALITY();
    
    
    /**
     * <p>
     * Answer the predicate that denotes a restriction on a given property to 
     * have exactly a certain number of values
     * </p>
     * 
     * @return The property that denotes a local property cardinality.
     */
    public Property CARDINALITY();
    
    
    /**
     * <p>
     * Answer the predicate that denotes a qualified restriction on a given property to 
     * have at least a certain number of values
     * </p>
     * 
     * @return The property that denotes a local property cardinality lower bound.
     */
    public Property MIN_CARDINALITY_Q();
    
    
    /**
     * <p>
     * Answer the predicate that denotes a qualified restriction on a given property to 
     * have at most a certain number of values
     * </p>
     * 
     * @return The property that denotes a local property cardinality upper bound.
     */
    public Property MAX_CARDINALITY_Q();
    
    
    /**
     * <p>
     * Answer the predicate that denotes a qualified restriction on a given property to 
     * have exactly a certain number of values
     * </p>
     * 
     * @return The property that denotes a local property cardinality.
     */
    public Property CARDINALITY_Q();
    
    
    /**
     * <p>
     * Answer the predicate that denotes a the class in a qualified restriction.
     * </p>
     * 
     * @return The property that denotes the class of all values in a qualified restriction.
     */
    public Property HAS_CLASS_Q();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one property is the inverse of another
     * </p>
     * 
     * @return The property that denotes the inverse relationship between properties
     */
    public Property INVERSE_OF();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one ontology document imports another.
     * </p>
     * 
     * @return The property that denotes ontology importing.
     */
    public Property IMPORTS();
    
    
    /**
     * <p>
     * Answer the predicate that denotes version-info metadata on an ontology header
     * </p>
     * 
     * @return The property that denotes ontology version information.
     */
    public Property VERSION_INFO();
    
    
    /**
     * <p>
     * Answer the predicate that documents that one ontology is a prior version
     * of another.
     * </p>
     * 
     * @return The property that denotes ontology versioning
     */
    public Property PRIOR_VERSION();
    
    
    /**
     * <p>
     * Answer the predicate that documents that one ontology resource is backwards
     * compatible with another.
     * </p>
     * 
     * @return The property that denotes ontology element backwards compatability.
     */
    public Property BACKWARD_COMPATIBLE_WITH();
    
    
    /**
     * <p>
     * Answer the predicate that documents that one ontology resource is not backwards
     * compatible with another.
     * </p>
     * 
     * @return The property that denotes ontology element backwards incompatability.
     */
    public Property INCOMPATIBLE_WITH();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one class is a sub-class of another.
     * </p>
     * 
     * @return The property that the sub-class relationship.
     */
    public Property SUB_CLASS_OF();
    
    
    /**
     * <p>
     * Answer the predicate that denotes that one property is a sub-property of another.
     * </p>
     * 
     * @return The property that denotes the sub-property relationship.
     */
    public Property SUB_PROPERTY_OF();
    
    
    /**
     * <p>
     * Answer the predicate that denotes the domain of a property.
     * </p>
     * 
     * @return The property that denotes a property domain
     */
    public Property DOMAIN();
    
    
    /**
     * <p>
     * Answer the predicate that denotes the range of a property
     * </p>
     * 
     * @return The property that denotes the property range
     */
    public Property RANGE();
    
    
    /**
     * <p>
     * Answer the predicate that denotes <code>label</code> annotation on an ontology element
     * </p>
     * 
     * @return The property that denotes the label annotation
     */
    public Property LABEL();
    
    
    /**
     * <p>
     * Answer the predicate that denotes <code>comment</code> annotation on an ontology element
     * </p>
     * 
     * @return The property that denotes the comment annotation
     */
    public Property COMMENT();
    
    
    /**
     * <p>
     * Answer the predicate that denotes <code>seeAlso</code> annotation on an ontology element
     * </p>
     * 
     * @return The property that denotes the seeAlso annotation
     */
    public Property SEE_ALSO();
    
    
    /**
     * <p>
     * Answer the predicate that denotes <code>isDefinedBy</code> annotation on an ontology element
     * </p>
     * 
     * @return The property that denotes the isDefiendBy annotation
     */
    public Property IS_DEFINED_BY();
   
    // List vocabulary
    
    /**
     * <p>The property that denotes the head of a list</p>
     * @return The property that maps from a cell in a list to its value
     */
    public Property FIRST();
    
    /**
     * <p>The property that denotes the tail of a list</p>
     * @return The property that maps from a cell in a list to the remainder of the list
     */
    public Property REST();
    
    /**
     * <p>The <code>rdf:type</code> for cells in this list</p>
     * @return The list rdf:type resource
     */
    public Resource LIST();
    
    /**
     * <p>The resource that denotes the end of the list</p>
     */
    public Resource NIL();

    
    // Particular language syntax categories
    
    /**
     * <p>
     * Answer an iterator over the rdf:types in this language that denote stand-alone
     * axioms.
     * </p>
     * 
     * @return An iterator over axiom types.
     */
    public Iterator<Resource> getAxiomTypes();
    
    
    /**
     * <p>
     * Answer an iterator over the properties in this language that are denoted
     * annotation properties.  Not all languages have distinguished annotation
     * properties.
     * </p>
     * 
     * @return An iterator over annotation properties.
     */
    public Iterator<Resource> getAnnotationProperties();
    
    
    /**
     * <p>
     * Answer an iterator over the various types of class description defined
     * in the language.
     * </p>
     * 
     * @return An iterator over the various rdf:types of class descriptions.
     */
    public Iterator<Resource> getClassDescriptionTypes();
    
    
    // Alias management
    
    /**
     * <p>
     * Answer true if the given resource has an alias in this profile.
     * </p>
     * 
     * @param res A resource (including properties) to test for an alias
     * @return True if there is an alias for <code>res</code>
     */
    public boolean hasAliasFor( Resource res );
    
    /**
     * <p>
     * Answer an alias for the given resource.  If there is more than
     * one such alias, a choice is made non-deterministically between the
     * alternatives.
     * </p>
     * 
     * @param res A resource (including properties) to test for an alias
     * @return The alias for <code>res</code>, or one of the aliases for <code>res</code> if more
     * than one is defined, or null if no alias is defined for <code>res</code>.
     * 
     */
    public Resource getAliasFor( Resource res );
    
    /**
     * <p>
     * Answer an iterator over the defined aliases for a resource.
     * </p>
     * 
     * @param res A resource (including properties)
     * @return An iterator over the aliases for <code>res</code>. If there are
     * no aliases, the empty iterator is returned.
     */
    public Iterator<Resource> listAliasesFor( Resource res );
    
    
    /**
     * <p>
     * Answer true if the given graph supports a view of this node as the given 
     * language element, according to the semantic constraints of the profile.
     * If strict checking on the ontology model is turned off, this check is
     * skipped.
     * </p>
     * 
     * @param n A node to test
     * @param g The enhanced graph containing <code>n</code>, which is assumed to
     * be an {@link OntModel}.
     * @param type A class indicating the facet that we are testing against.
     * @return True if strict checking is off, or if <code>n</code> can be 
     * viewed according to the facet resource <code>res</code>
     */
    public <T> boolean isSupported( Node n, EnhGraph g, Class<T> type );
    
    // Other stuff
    
    /**
     * <p>
     * Answer a descriptive string for this profile, for use in debugging and other output.
     * </p>
     */
    public String getLabel();
}

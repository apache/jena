/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: Profile.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-04-01 10:31:04 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved. 
 * (see footer for full conditions)
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;



// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * Interface that encapsulates the elements of a general vocabulary
 * corresponding to a particular ontology language.  The intent is that, using
 * a given vocabulary, a given RDF model can be processed as an ontology
 * description, without binding knowledge of the vocabulary into this Java
 * package. For tractability, this limits the vocabularies that can easily be
 * represented to those that are similar to OWL and DAML+OIL.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: Profile.java,v 1.4 2003-04-01 10:31:04 ian_dickinson Exp $
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
    
    
    
}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLClass.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-06-18 22:30:45 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved. 
 * (see footer for full conditions)
  *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////
import java.util.Iterator;

import com.hp.hpl.jena.ontology.OntClass;



/**
 * <p>Java representation of a DAML ontology Class. Note that the ontology classes are
 * not the same as Java classes: think of classifications rather than active data structures.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLClass.java,v 1.7 2003-06-18 22:30:45 ian_dickinson Exp $
 */
public interface DAMLClass
    extends DAMLCommon, OntClass
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


    /**
     * <p>Property accessor for the <code>daml:subClassOf</code> property of a class. This
     * denotes a class that is a super-class of this class.
     *
     * @return Property accessor for <code>daml:subClassOf</code>.
     */
    public PropertyAccessor prop_subClassOf();


    /**
     * <p>Property accessor for the <code>daml:disjointWith</code> property of a class. This
     * denotes a class with which this class has no instances in common.</p>
     *
     * @return Property accessor for <code>daml:disjointWith</code>.
     */
    public PropertyAccessor prop_disjointWith();


    /**
     * <p>Property accessor for the <code>daml:disjointUnionOf</code> property of a class. This
     * denotes a list of classes that are each pair-wise disjoint, and whose
     * union describes this class.</p>
     *
     * @return Property accessor for <code>daml:disjointUnionOf</code>.
     */
    public PropertyAccessor prop_disjointUnionOf();


    /**
     * <p>Property accessor for the <code>daml:sameClassAs</code> property of a DAML class. This
     * denotes a class whose instances are the same those of this class.</p>
     *
     * @return Property accessor for <code>daml:sameClassAs</code>.
     */
    public PropertyAccessor prop_sameClassAs();


    /**
     * <p>Property accessor for the property <code>daml:unionOf</code>, which denotes a class
     * expression consisting of the union (disjunction) of a list of classes.</p>
     *
     * @return Property accessor for <code>daml:unionOf</code>.
     */
    public PropertyAccessor prop_unionOf();


    /**
     * <p>Property accessor for the property <code>daml:intersectionOf</code>, which denotes an
     * intersection (conjunction) of a list of classes.</p>
     *
     * @return Property accessor for <code>daml:intersectionOf</code>.
     */
    public PropertyAccessor prop_intersectionOf();


    /**
     * <p>Property accessor for the property <code>daml:compelementOf</code>, which denotes the
     * class whose members are the individuals not in the given class.</p>
     *
     * @return Property accessor for <code>daml:compelementOf</code>.
     */
    public PropertyAccessor prop_complementOf();


    /**
     * <p>Property accessor for the <code>daml:oneOf</code> property, which defines a class expression
     * denoting that the class is exactly one of the given list of classes.</p>
     *
     * @return Property accessor for <code>daml:oneOf</code>.
     */
    public PropertyAccessor prop_oneOf();


    /**
     * <p>Answer an iterator over the DAML classes
     * that mention this class as one of its super-classes.  Will return
     * all available sub-classes (see {@link #getSubClasses(boolean)} for
     * more details). The elements
     * of the iterator will be {@link DAMLClass} objects.</p>
     * @return An iterator over all available sub-classes of this class
     */
    public Iterator getSubClasses();


    /**
     * <p>Answer an iterator over the DAML classes
     * that mention this class as one of its super-classes.
     * The members of the iterator will be {@link DAMLClass} objects
     * </p>
     * <p><strong>Note:</strong> In a change to the Jena 1 DAML API, whether
     * this iterator includes <em>inferred</em> sub-classes is determined
     * not by a flag at the API level, but by the construction of the DAML
     * model itself.  See {@linkplain com.hp.hpl.jena.rdf.model.ModelFactory the model factory} 
     * for details. The boolean parameter
     * <code>closed</code> is now re-interpreted to mean the inverse of <code>
     * direct</code>, see {@link OntClass#listSubClasses(boolean)} for more details.
     * </p>
     * 
     * @param closed If true, return all available values; otherwise, return
     * only local (direct) sub-classes. See note for details.
     * @return An iterator over this class's sub-classes.
     */
    public Iterator getSubClasses( boolean closed );


    /**
     * <p>Answer an iterator over the DAML classes
     * that are super-classes of this class.  Will return
     * all available super-classes (see {@link #getSuperClasses(boolean)} for
     * more details). The elements
     * of the iterator will be {@link DAMLClass} objects.</p>
     * @return An iterator over all available super-classes of this class
     */
    public Iterator getSuperClasses();


    /**
     * <p>Answer an iterator over the DAML classes
     * that are super-classes of this class.
     * The members of the iterator will be {@link DAMLClass} objects
     * </p>
     * <p><strong>Note:</strong> In a change to the Jena 1 DAML API, whether
     * this iterator includes <em>inferred</em> super-classes is determined
     * not by a flag at the API level, but by the construction of the DAML
     * model itself.  See {@linkplain com.hp.hpl.jena.rdf.model.ModelFactory the model factory} 
     * for details. The boolean parameter
     * <code>closed</code> is now re-interpreted to mean the inverse of <code>
     * direct</code>, see {@link OntClass#listSubClasses(boolean)} for more details.
     * </p>
     * 
     * @param closed If true, return all available values; otherwise, return
     * only local (direct) super-classes. See note for details.
     * @return an iterator over this class's super-classes.
     */
    public Iterator getSuperClasses( boolean closed );


    /**
     * <p>Answer an iterator over all of the DAML classes that are equivalent to this
     * value under the <code>daml:sameClassAs</code> relation.  Note: only considers
     * <code>daml:sameClassAs</code>, for general equivalence, see
     * {@link #getEquivalentValues}.  Note also that the first member of the iteration is
     * always the DAMLClass on which the method is invoked: trivially, a DAMLClass is
     * a member of the set of DAMLClasses equivalent to itself.  If the caller wants
     * the set of classes equivalent to this one, not including itself, simply ignore
     * the first element of the iteration.</p>
     *
     * @return an iterator ranging over every equivalent DAML classes
     */
    public Iterator getSameClasses();


    /**
     * <p>Answer an iterator over all of the DAML objects that are equivalent to this
     * class, which will be the union of <code>daml:equivalentTo</code> and
     * <code>daml:sameClassAs</code>.</p>
     *
     * @return an iterator ranging over every equivalent DAML class
     */
    public Iterator getEquivalentValues();


    /**
     * <p>Answer an iterator over the instances of this class that currently exist
     * in the model.<p>
     *
     * @return An iterator over those instances that have this class as one of
     *         the classes to which they belong
     * @see com.hp.hpl.jena.ontology.daml.DAMLCommon#getRDFTypes
     */
    public Iterator getInstances();


    /**
     * <p>Answer an iteration of the properties that may be used for
     * instances of this class: i&#046;e&#046; the properties that have this class,
     * or one of its super-classes, as domain.<p>
     *
     * @return An iteration of the properties that have this class in the domain
     */
    public Iterator getDefinedProperties();


    /**
     * <p>Answer an iteration of the properties that may be used for
     * instances of this class: i&#046;e&#046; the properties that have this class,
     * or optionally one of its super-classes, as domain.</p>
     * <p><strong>Note:</strong> In a change to the Jena 1 DAML API, whether
     * this iterator includes the defined properties for <em>inferred</em> 
     * super-classes is determined
     * not by a flag at the API level, but by the construction of the DAML
     * model itself.  See {@linkplain com.hp.hpl.jena.rdf.model.ModelFactory the model factory} 
     * for details. The boolean parameter
     * <code>closed</code> is now re-interpreted to mean the inverse of <code>
     * direct</code>, see {@link OntClass#listSubClasses(boolean)} for more details.
     * </p>
     *
     * @param closed If true, use all available information from the class hierarchy;
     * if false, only use local properties.
     * @return An iteration of the properties that have this class as domain
     */
    public Iterator getDefinedProperties( boolean closed );
    
    
    /**
     * <p>Answer true if this class is an enumeration (i&#046;e&#046; has a property
     * <code>daml:oneOf</code> with a list of values).  This is not an exclusive property, a class
     * can be an enumeration at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.</p>
     *
     * @return True if this class expression is an enumeration.
     */
    public boolean isEnumeration();


    /**
     * <p>Answer true if this class is a named class (i&#046;e&#046; is not an anonymous
     * class expression).  This is not an exclusive property, a class
     * can be named at the same time as one of the other kinds
     * of class expression.</p>
     *
     * @return True if this class expression is a named class.
     */
    public boolean isNamedClass();


    /**
     * <p>Answer true if this class is an property restriction (i&#046;e&#046; is a
     * <code>daml:Restriction</code> instance).  This is not an exclusive property: a class
     * expression can be a property restriction at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return True if this class is a property restriction.
     */
    public boolean isRestriction();


    /**
     * <p>Answer true if this class is an intersection of a list
     * of classes.  This is not an exclusive property: a class
     * expression can be an intersection at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.</p>
     *
     * @return True if this class is an intersection class expression.
     */
    public boolean isIntersection();


    /**
     * <p>Answer true if this class is a union of a list
     * of classes.  This is not an exclusive property, a class
     * expression can be a union at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.</p>
     *
     * @return True if this class is a union class expression.
     */
    public boolean isUnion();


    /**
     * <p>Answer true if this class is a disjoint union of a list
     * of classes.  This is not an exclusive property, a class
     * expression can be a disjoint union at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.</p>
     *
     * @return True if this class is a disjoint union expression.
     */
    public boolean isDisjointUnion();


    /**
     * <p>Answer true if this class expression is an boolean complement of another
     * class.  This is not an exclusive property, a class
     * expression can be an complement at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return True if this class is a complement class expression.
     */
    public boolean isComplement();

}


/*
    (c) Copyright Hewlett-Packard Company 2001-2003
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


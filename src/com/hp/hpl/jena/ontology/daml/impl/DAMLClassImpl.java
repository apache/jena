/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLClassImpl.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-05-21 15:33:14 $
 *               by   $Author: chris-dollin $
 *
 * (c) Copyright Hewlett-Packard Company 2001
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.RDFException;

import com.hp.hpl.jena.ontology.daml.DAMLClass;
import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.PropertyAccessor;
import com.hp.hpl.jena.ontology.daml.PropertyIterator;

import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.util.iterator.ConcatenatedIterator;

import com.hp.hpl.jena.shared.*;

/**
 * Java representation of a DAML ontology Class. Note that the ontology classes are
 * not the same as Java classes: think of classifications rather than active data structures.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLClassImpl.java,v 1.2 2003-05-21 15:33:14 chris-dollin Exp $
 */
public class DAMLClassImpl
    extends DAMLCommonImpl
    implements DAMLClass
{
    // Constants
    //////////////////////////////////



    // Static variables
    //////////////////////////////////



    // Instance variables
    //////////////////////////////////

    /** Property accessor for onProperty */
    private PropertyAccessor m_propSubClassOf = new PropertyAccessorImpl( getVocabulary().subClassOf(), this );

    /** Property accessor for disjointWith */
    private PropertyAccessor m_propDisjointWith = null;

    /** Property accessor for disjointUnionOf */
    private PropertyAccessor m_propDisjointUnionOf = null;

    /** Property accessor for sameClassAs */
    private PropertyAccessor m_propSameClassAs = null;

    /** Property accessor for oneOf */
    private PropertyAccessor m_propOneOf = null;

    /** Property accessor for unionOf */
    private PropertyAccessor m_propUnionOf = null;

    /** Property accessor for intersectionOf */
    private PropertyAccessor m_propIntersectionOf = null;

    /** Property accessor for complementOf */
    private PropertyAccessor m_propComplementOf = null;


    // Constructors
    //////////////////////////////////

    /**
     * Constructor, takes the name and namespace for this class, and the underlying
     * model it will be attached to.
     *
     * @param namespace The namespace the class inhabits, or null
     * @param name The name of the class
     * @param model Reference to the DAML model that will contain statements about this DAML class.
     * @param vocabulary Reference to the DAML vocabulary used by this class.
     */
    public DAMLClassImpl( String namespace, String name, DAMLModel model, DAMLVocabulary vocabulary ) {
        super( namespace, name, model, vocabulary );
        setRDFType( RDFS.Class );
    }


    /**
     * Constructor, takes URI for this class, and the underlying
     * model it will be attached to.
     *
     * @param uri The URI of the class
     * @param store Reference to the DAML store that will contain statements about this DAML class.
     * @param vocabulary Reference to the DAML vocabulary used by this class.
     */
    public DAMLClassImpl( String uri, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( uri, store, vocabulary );
        setRDFType( RDFS.Class );
    }



    // External signature methods
    //////////////////////////////////


    /**
     * Property accessor for the 'subClassOf' property of a class. This
     * denotes a class-expression that is a super-class of this class.
     *
     * @return Property accessor for 'onProperty'.
     */
    public PropertyAccessor prop_subClassOf() {
        return m_propSubClassOf;
    }


    /**
     * Property accessor for the 'disjointWith' property of a class. This
     * denotes a class-expression with which this class has no instances in common.
     *
     * @return Property accessor for 'disjointWith'.
     */
    public PropertyAccessor prop_disjointWith() {
        if (m_propDisjointWith == null) {
            m_propDisjointWith = new PropertyAccessorImpl( getVocabulary().disjointWith(), this );
        }

        return m_propDisjointWith;
    }


    /**
     * Property accessor for the 'disjointUnionOf' property of a class. This
     * denotes a list of class expressions that are each pair-wise disjoint, and whose
     * union describes this class.
     *
     * @return Property accessor for 'disjointUnionOf'.
     */
    public PropertyAccessor prop_disjointUnionOf() {
        if (m_propDisjointUnionOf == null) {
            m_propDisjointUnionOf = new PropertyAccessorImpl( getVocabulary().disjointUnionOf(), this );
        }

        return m_propDisjointUnionOf;
    }


    /**
     * Property accessor for the 'sameClassAs' property of a class. This
     * denotes a class-expression whose instances are the same those of this class.
     *
     * @return Property accessor for 'sameClassAs'.
     */
    public PropertyAccessor prop_sameClassAs() {
        if (m_propSameClassAs == null) {
            m_propSameClassAs = new PropertyAccessorImpl( getVocabulary().sameClassAs(), this );
        }

        return m_propSameClassAs;
    }


    /**
     * Property accessor for the property 'unionOf', which is one element of the range
     * of boolean expressions over classes permitted by DAML.
     *
     * @return property accessor for 'unionOf'.
     */
    public PropertyAccessor prop_unionOf() {
        if (m_propUnionOf == null) {
            m_propUnionOf = new PropertyAccessorImpl( getVocabulary().unionOf(), this );
        }

        return m_propUnionOf;
    }


    /**
     * Property accessor for the property 'intersectionOf', which is one element of the range
     * of boolean expressions over classes permitted by DAML.
     *
     * @return property accessor for 'intersectionOf'.
     */
    public PropertyAccessor prop_intersectionOf() {
        if (m_propIntersectionOf == null) {
            m_propIntersectionOf = new PropertyAccessorImpl( getVocabulary().intersectionOf(), this );
        }

        return m_propIntersectionOf;
    }


    /**
     * Property accessor for the property 'compelementOf', which is one element of the range
     * of boolean expressions over classes permitted by DAML.
     *
     * @return property accessor for 'complementOf'.
     */
    public PropertyAccessor prop_complementOf() {
        if (m_propComplementOf == null) {
            m_propComplementOf = new PropertyAccessorImpl( getVocabulary().complementOf(), this );
        }

        return m_propComplementOf;
    }


    /**
     * Property accessor for the 'oneOf' property, which defines a class expression
     * denoting that the class is exactly one of the given list of class expressions.
     *
     * @return property accessor for 'oneOf'
     */
    public PropertyAccessor prop_oneOf() {
        if (m_propOneOf == null) {
            m_propOneOf = new PropertyAccessorImpl( getVocabulary().oneOf(), this );
        }

        return m_propOneOf;
    }


    /**
     * Answer true if this class expression is an enumeration (i&#046;e&#046; has a property
     * 'oneOf' with a list of values).  This is not an exclusive property, a class
     * expression can be an enumeration at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is an enumeration.
     */
    public boolean isEnumeration() {
        try {
            return hasProperty( getVocabulary().oneOf() );
        }
        catch (JenaException e) {
            Log.severe( "RDF exception " + e, e );
            throw new RuntimeException( "RDF Exception " + e );
        }
    }


    /**
     * Answer true if this class expression is an named class (i&#046;e&#046; is not an anonymous
     * class expression).  This is not an exclusive property, a class
     * expression can be named at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is a named class.
     */
    public boolean isNamedClass() {
        return !isAnon();
    }


    /**
     * Answer true if this class expression is an property restriction (i&#046;e&#046; is a
     * Restriction value).  This is not an exclusive property, a class
     * expression can be a property restriction at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return necessarily false, since restrictions are represented by a different Java class (see
     *         {@link com.hp.hpl.jena.ontology.daml.DAMLRestriction}).
     */
    public boolean isRestriction() {
        return false;
    }


    /**
     * Answer true if this class expression is an boolean intersection of a list
     * of class expressions.  This is not an exclusive property, a class
     * expression can be an intersection at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is an intersection.
     */
    public boolean isIntersection() {
        try {
            return hasProperty( getVocabulary().intersectionOf() );
        }
        catch (JenaException e) {
            Log.severe( "RDF exception " + e, e );
            throw new RuntimeException( "RDF Exception " + e );
        }
    }


    /**
     * Answer true if this class expression is an boolean union of a list
     * of class expressions.  This is not an exclusive property, a class
     * expression can be an union at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is a union.
     */
    public boolean isUnion() {
        try {
            return hasProperty( getVocabulary().unionOf() );
        }
        catch (JenaException e) {
            Log.severe( "RDF exception " + e, e );
            throw new RuntimeException( "RDF Exception " + e );
        }
    }


    /**
     * Answer true if this class expression is a disjoint union of a list
     * of class expressions.  This is not an exclusive property, a class
     * expression can be a disjoint union at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is a disjoint union.
     */
    public boolean isDisjointUnion() {
        try {
            return hasProperty( getVocabulary().disjointUnionOf() );
        }
        catch (JenaException e) {
            Log.severe( "RDF exception " + e, e );
            throw new RuntimeException( "RDF Exception " + e );
        }
    }


    /**
     * Answer true if this class expression is an boolean complement of a list
     * of class expressions.  This is not an exclusive property, a class
     * expression can be an complement at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is a complement.
     */
    public boolean isComplement() {
        try {
            return hasProperty( getVocabulary().complementOf() );
        }
        catch (JenaException e) {
            Log.severe( "RDF exception " + e, e );
            throw new RuntimeException( "RDF Exception " + e );
        }
    }


    /**
     * Answer an iterator over the DAML classes (or, strictly, class expressions)
     * that mention this class as one of its super-classes.   Will generate the
     * closure of the iteration over the sub-class relationship.
     *
     * @return an iterator over this class's sub-classes. The members of the
     *         iteration will be DAMLClass objects.
     */
    public Iterator getSubClasses() {
        return getSubClasses( true );
    }


    /**
     * Answer an iterator over the DAML classes (or, strictly, class expressions)
     * that mention this class as one of its super-classes.
     *
     * @param closed If true, close the iteration over the sub-class relation: i&#046;e&#046;
     *               return the sub-classes of the sub-classes, etc.
     * @return an iterator over this class's sub-classes. The members of the
     *         iteration will be DAMLClass objects.
     */
    public Iterator getSubClasses( boolean closed ) {
        return new PropertyIterator( this, null, getVocabulary().subClassOf(), closed, false );
    }


    /**
     * Answer an iterator over the DAML classes (or, strictly, class expressions)
     * that mention this class as one of its sub-classes.  Will generate the
     * closure of the iteration over the super-class relationship.
     *
     * @return an iterator over this class's super-classes. The members of the
     *         iteration will be DAMLClass objects.
     */
    public Iterator getSuperClasses() {
        return getSuperClasses( true );
    }


    /**
     * Answer an iterator over the DAML classes (or, strictly, class expressions)
     * that mention this class as one of its sub-classes.
     *
     * @param closed If true, close the iteration over the super-class relation: i&#046;e&#046;
     *               return the super-classes of the super-classes, etc.
     * @return an iterator over this class's sub-classes. The members of the
     *         iteration will be DAMLClass objects.
     */
    public Iterator getSuperClasses( boolean closed ) {
        Iterator i = new PropertyIterator( this, getVocabulary().subClassOf(), null, closed, false );

        // ensure that the types always include Thing in the closure
        if (closed) {
            ((PropertyIterator) i).setDefaultValue( getVocabulary().Thing() );
        }

        return i;
    }


    /**
     * Answer a key that can be used to index collections of this DAML class for
     * easy access by iterators.  Package access only.
     *
     * @return a key object.
     */
    Object getKey() {
        return DAML_OIL.Class.getURI();
    }


    /**
     * Answer an iterator over all of the DAML classes that are equivalent to this
     * value under the <code>daml:sameClassAs</code> relation.  Note: only considers
     * <code>daml:sameClassAs</code>, for general equivalence, see
     * {@link #getEquivalentValues}.
     *
     * @return an iterator ranging over every equivalent DAML class - each value of
     *         the iteration will be a DAMLClass object.
     */
    public Iterator getSameClasses() {
        return new PropertyIterator( this, getVocabulary().sameClassAs(), getVocabulary().sameClassAs(), true, true );
    }



    /**
     * Answer an iterator over all of the DAML objects that are equivalent to this
     * class, which will be the union of <code>daml:equivalentTo</code> and
     * <code>daml:sameClassAs</code>.
     *
     * @return an iterator ranging over every equivalent DAML class - each value of
     *         the iteration should be a DAMLClass object.
     */
    public Iterator getEquivalentValues() {
        ConcatenatedIterator i = new ConcatenatedIterator(
                       // first the iterator over the equivalentTo values
                       super.getEquivalentValues(),
                       // followed by the sameClassAs values
                       new PropertyIterator( this, getVocabulary().sameClassAs(), getVocabulary().sameClassAs(), true, false, false ) );

        // ensure that the iteration includes self
        i.setDefaultValue( this );

        return i;
    }


    /**
     * Answer true if the given class is a sub-class of this class, using information
     * from the <code>rdf:subClassOf</code> or <code>daml:subClassOf</code> relation.
     *
     * @param cls A DAMLClass object
     * @return True if this class is a super-class of the given class.
     */
    public boolean hasSubClass( DAMLClass cls ) {
        for (Iterator i = getSubClasses();  i.hasNext();  ) {
            // note that we don't need to test for class equivalence here - that is taken care
            // of by the sub-class iterator
            if (((DAMLClass) i.next()).equals( cls )) {
                return true;
            }
        }

        return false;
    }


    /**
     * Answer true if the given class is a super-class of this class, using information
     * from the <code>rdf:subClassOf</code> or <code>daml:subClassOf</code> relation.
     *
     * @param cls A DAMLClass object
     * @return True if this class is a sub-class of the given class.
     */
    public boolean hasSuperClass( DAMLClass cls ) {
        for (Iterator i = getSuperClasses();  i.hasNext();  ) {
            // note that we don't need to test for class equivalence here - that is taken care
            // of by the sub-class iterator
            if (((DAMLClass) i.next()).equals( cls )) {
                return true;
            }
        }

        return false;
    }


    /**
     * Answer an iterator over the instances of this class that currently exist
     * in the model.
     *
     * @return An iterator over those instances that have this class as one of
     *         the classes to which they belong
     * @see com.hp.hpl.jena.ontology.daml.DAMLCommon#getRDFTypes
     */
    public Iterator getInstances() {
        return new PropertyIterator( new ConcatenatedIterator( getSelfIterator(), getSubClasses() ),
                                     null, RDF.type, false, false, true );
    }


    /**
     * Answer an iteration of the properties that may be used for
     * instances of this class: i&#046;e&#046; the properties that have this class,
     * or one of its super-classes, as domain.
     *
     * @return An iteration of the properties that have this class as domain
     */
    public Iterator getDefinedProperties() {
        return getDefinedProperties( true );
    }


    /**
     * Answer an iteration of the properties that may be used for
     * instances of this class: i&#046;e&#046; the properties that have this class,
     * or optionally one of its super-classes, as domain.  <b>Note:</b>
     * does not include the sub-properties of the defined properties as
     * part of the iteration.
     *
     * @param closed If true, close the iteration over the super-classes
     *               of this class.
     * @return An iteration of the properties that have this class as domain
     */
    public Iterator getDefinedProperties( boolean closed ) {
        // select either this class or this class and all of its superclasses
        Iterator roots = closed ? new ConcatenatedIterator( getSelfIterator(), getSuperClasses( true ) ) :
                                  getSelfIterator();

        // now look for properties that mention the root class(es) as domain
        return new PropertyIterator( roots, null, getVocabulary().domain(), false, false, true );
    }


    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}

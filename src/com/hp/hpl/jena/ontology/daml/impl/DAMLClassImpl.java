/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLClassImpl.java,v $
 * Revision           $Revision: 1.11 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-12-11 22:55:09 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.ontology.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.util.iterator.*;



/**
 * <p>Java representation of a DAML ontology Class. Note that the ontology classes are
 * not the same as Java classes: think of classifications rather than active data structures.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLClassImpl.java,v 1.11 2003-12-11 22:55:09 ian_dickinson Exp $
 */
public class DAMLClassImpl
    extends OntClassImpl
    implements DAMLClass
{
    // Constants
    //////////////////////////////////



    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating DAMLClass facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new DAMLClassImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to DAMLClass" );
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an DAMLClass facet if it has rdf:type owl:Class or equivalent
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, DAMLClass.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    /** Property accessor for onProperty */
    private PropertyAccessor m_propSubClassOf = new PropertyAccessorImpl( DAML_OIL.subClassOf, this );

    /** Property accessor for disjointWith */
    private PropertyAccessor m_propDisjointWith = new PropertyAccessorImpl( DAML_OIL.disjointWith, this );

    /** Property accessor for disjointUnionOf */
    private PropertyAccessor m_propDisjointUnionOf = new PropertyAccessorImpl( DAML_OIL.disjointUnionOf, this );

    /** Property accessor for sameClassAs */
    private PropertyAccessor m_propSameClassAs = new PropertyAccessorImpl( DAML_OIL.sameClassAs, this );

    /** Property accessor for oneOf */
    private PropertyAccessor m_propOneOf = new PropertyAccessorImpl( DAML_OIL.oneOf, this );

    /** Property accessor for unionOf */
    private PropertyAccessor m_propUnionOf = new PropertyAccessorImpl( DAML_OIL.unionOf, this );

    /** Property accessor for intersectionOf */
    private PropertyAccessor m_propIntersectionOf = new PropertyAccessorImpl( DAML_OIL.intersectionOf, this );

    /** Property accessor for complementOf */
    private PropertyAccessor m_propComplementOf = new PropertyAccessorImpl( DAML_OIL.complementOf, this );

    /** DAML common delegate */
    protected DAMLCommon m_common = null;
    
    /** Vocabulary - this is really obsoleted by the profile mechanism */
    protected DAMLVocabulary m_vocabulary = VocabularyManager.getDefaultVocabulary();
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a DAML class represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public DAMLClassImpl( Node n, EnhGraph g ) {
        super( n, g );
        m_common = new DAMLCommonImpl( n, g );
    }



    // External signature methods
    //////////////////////////////////

    // delegate to DAMLCommon what we can
    /** @deprecated */
    public void setRDFType( Resource rdfClass, boolean replace ) { m_common.setRDFType( rdfClass, replace ); }
    public DAMLModel getDAMLModel()                              { return m_common.getDAMLModel(); }
    public ExtendedIterator getRDFTypes( boolean complete )      { return m_common.getRDFTypes( complete ); }
    public DAMLVocabulary getVocabulary()                        { return m_vocabulary; }
    public LiteralAccessor prop_label()                          { return m_common.prop_label(); }
    public LiteralAccessor prop_comment()                        { return m_common.prop_comment(); }
    public PropertyAccessor prop_equivalentTo()                  { return m_common.prop_equivalentTo(); }
    public PropertyAccessor prop_type()                          { return m_common.prop_type(); }
    
    /**
     * <p>Answer an iterator over all of the DAML objects that are equivalent to this
     * class, which will be the union of <code>daml:equivalentTo</code> and
     * <code>daml:sameClassAs</code>.</p>
     *
     * @return an iterator ranging over every equivalent DAML class
     */
    public ExtendedIterator getEquivalentValues() {
        ConcatenatedIterator i = new ConcatenatedIterator(
                       // first the iterator over the equivalentTo values
                       m_common.getEquivalentValues(),
                       // followed by the sameClassAs values
                       getSameClasses() );

        return UniqueExtendedIterator.create( i ).mapWith( new AsMapper( DAMLClass.class ) );
    }


    /**
     * Answer the set of equivalent values to this value, but not including the
     * value itself.  The iterator will range over a set: each element occurs only
     * once.
     *
     * @return An iteration ranging over the set of values that are equivalent to this
     *         value, but not itself.
     */
    public ExtendedIterator getEquivalenceSet() {
        Set s = new HashSet();

        s.add( this );
        for (Iterator i = getEquivalentValues();  i.hasNext();  s.add( i.next() ) );
        s.remove( this );
        
        return WrappedIterator.create( s.iterator() );
    }



    /**
     * <p>Property accessor for the <code>daml:subClassOf</code> property of a class. This
     * denotes a class that is a super-class of this class.
     *
     * @return Property accessor for <code>daml:subClassOf</code>.
     */
    public PropertyAccessor prop_subClassOf() {
        return m_propSubClassOf;
    }


    /**
     * <p>Property accessor for the <code>daml:disjointWith</code> property of a class. This
     * denotes a class with which this class has no instances in common.</p>
     *
     * @return Property accessor for <code>daml:disjointWith</code>.
     */
    public PropertyAccessor prop_disjointWith() {
        return m_propDisjointWith;
    }


    /**
     * <p>Property accessor for the <code>daml:disjointUnionOf</code> property of a class. This
     * denotes a list of classes that are each pair-wise disjoint, and whose
     * union describes this class.</p>
     *
     * @return Property accessor for <code>daml:disjointUnionOf</code>.
     */
    public PropertyAccessor prop_disjointUnionOf() {
        return m_propDisjointUnionOf;
    }


    /**
     * <p>Property accessor for the <code>daml:sameClassAs</code> property of a DAML class. This
     * denotes a class whose instances are the same those of this class.</p>
     *
     * @return Property accessor for <code>daml:sameClassAs</code>.
     */
    public PropertyAccessor prop_sameClassAs() {
        return m_propSameClassAs;
    }


    /**
     * <p>Property accessor for the property <code>daml:unionOf</code>, which denotes a class
     * expression consisting of the union (disjunction) of a list of classes.</p>
     *
     * @return Property accessor for <code>daml:unionOf</code>.
     */
    public PropertyAccessor prop_unionOf() {
        return m_propUnionOf;
    }


    /**
     * <p>Property accessor for the property <code>daml:intersectionOf</code>, which denotes an
     * intersection (conjunction) of a list of classes.</p>
     *
     * @return Property accessor for <code>daml:intersectionOf</code>.
     */
    public PropertyAccessor prop_intersectionOf() {
        return m_propIntersectionOf;
    }


    /**
     * <p>Property accessor for the property <code>daml:compelementOf</code>, which denotes the
     * class whose members are the individuals not in the given class.</p>
     *
     * @return Property accessor for <code>daml:compelementOf</code>.
     */
    public PropertyAccessor prop_complementOf() {
        return m_propComplementOf;
    }


    /**
     * <p>Property accessor for the <code>daml:oneOf</code> property, which defines a class expression
     * denoting that the class is exactly one of the given list of classes.</p>
     *
     * @return Property accessor for <code>daml:oneOf</code>.
     */
    public PropertyAccessor prop_oneOf() {
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
        return hasProperty( getVocabulary().oneOf() );
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
        return hasProperty( RDF.type, getProfile().RESTRICTION() );
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
        return hasProperty( getVocabulary().intersectionOf() );
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
        return hasProperty( getVocabulary().unionOf() );
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
        return hasProperty( getVocabulary().disjointUnionOf() );
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
        return hasProperty( getVocabulary().complementOf() );
    }


    /**
     * <p>Answer an iterator over the DAML classes
     * that mention this class as one of its super-classes.  Will return
     * all available sub-classes (see {@link #getSubClasses(boolean)} for
     * more details). The elements
     * of the iterator will be {@link DAMLClass} objects.</p>
     * @return An iterator over all available sub-classes of this class
     */
    public ExtendedIterator getSubClasses() {
        return getSubClasses( true );
    }


    /**
     * <p>Answer an iterator over the DAML classes
     * that mention this class as one of its super-classes.
     * The members of the iterator will be {@link DAMLClass} objects
     * </p>
     * <p><strong>Note:</strong> In a change to the Jena 1 DAML API, whether
     * this iterator includes <em>inferred</em> sub-classes is determined
     * not by a flag at the API level, but by the construction of the DAML
     * model itself.  See {@link ModelFactory} for details. The boolean parameter
     * <code>closed</code> is now re-interpreted to mean the inverse of <code>
     * direct</code>, see {@link OntClass#listSubClasses(boolean)} for more details.
     * </p>
     * 
     * @param closed If true, return all available values; otherwise, return
     * only local (direct) sub-classes. See note for details.
     * @return An iterator over this class's sub-classes.
     */
    public ExtendedIterator getSubClasses( boolean closed ) {
        return WrappedIterator.create( super.listSubClasses( !closed ) ).mapWith( new AsMapper( DAMLClass.class ) );
    }


    /**
     * <p>Answer an iterator over the DAML classes
     * that are super-classes of this class.  Will return
     * all available super-classes (see {@link #getSuperClasses(boolean)} for
     * more details). The elements
     * of the iterator will be {@link DAMLClass} objects.</p>
     * @return An iterator over all available super-classes of this class
     */
    public ExtendedIterator getSuperClasses() {
        return getSuperClasses( true );
    }


    /**
     * <p>Answer an iterator over the DAML classes
     * that are super-classes of this class.
     * The members of the iterator will be {@link DAMLClass} objects
     * </p>
     * <p><strong>Note:</strong> In a change to the Jena 1 DAML API, whether
     * this iterator includes <em>inferred</em> super-classes is determined
     * not by a flag at the API level, but by the construction of the DAML
     * model itself.  See {@link ModelFactory} for details. The boolean parameter
     * <code>closed</code> is now re-interpreted to mean the inverse of <code>
     * direct</code>, see {@link OntClass#listSubClasses(boolean)} for more details.
     * </p>
     * 
     * @param closed If true, return all available values; otherwise, return
     * only local (direct) super-classes. See note for details.
     * @return an iterator over this class's super-classes.
     */
    public ExtendedIterator getSuperClasses( boolean closed ) {
        return WrappedIterator.create( super.listSuperClasses( !closed ) ).mapWith( new AsMapper( DAMLClass.class ) );
    }


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
    public ExtendedIterator getSameClasses() {
        return WrappedIterator.create( super.listEquivalentClasses() ).mapWith( new AsMapper( DAMLClass.class ) );
    }



    /**
     * <p>Answer an iterator over the instances of this class that currently exist
     * in the model.<p>
     *
     * @return An iterator over those instances that have this class as one of
     *         the classes to which they belong
     * @see com.hp.hpl.jena.ontology.daml.DAMLCommon#getRDFTypes
     */
    public ExtendedIterator getInstances() {
        return WrappedIterator.create( listInstances() ).mapWith( new AsMapper( DAMLInstance.class ) );
    }


    /**
     * <p>Answer an iteration of the properties that may be used for
     * instances of this class: i&#046;e&#046; the properties that have this class,
     * or one of its super-classes, as domain.<p>
     *
     * @return An iteration of the properties that have this class in the domain
     */
    public ExtendedIterator getDefinedProperties() {
        return getDefinedProperties( true );
    }


    /**
     * <p>Answer an iteration of the properties that may be used for
     * instances of this class: i&#046;e&#046; the properties that have this class,
     * or optionally one of its super-classes, as domain.</p>
     * <p><strong>Note:</strong> In a change to the Jena 1 DAML API, whether
     * this iterator includes the defined properties for <em>inferred</em> 
     * super-classes is determined
     * not by a flag at the API level, but by the construction of the DAML
     * model itself.  See {@link ModelFactory} for details. The boolean parameter
     * <code>closed</code> is now re-interpreted to mean the inverse of <code>
     * direct</code>, see {@link OntClass#listSubClasses(boolean)} for more details.
     * </p>
     *
     * @param closed If true, use all available information from the class hierarchy;
     * if false, only use local properties.
     * @return An iteration of the properties that have this class as domain
     */
    public ExtendedIterator getDefinedProperties( boolean closed ) {
        return WrappedIterator.create( listDeclaredProperties( closed ) ).mapWith( new AsMapper( DAMLProperty.class ) );
    }


    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}

/*
    (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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


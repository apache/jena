/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLRestrictionImpl.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-02-03 22:49:39 $
 *               by   $Author: ian_dickinson $
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
package com.hp.hpl.jena.ontology.daml.common;


// Imports
///////////////

import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.DAMLRestriction;
import com.hp.hpl.jena.ontology.daml.PropertyAccessor;
import com.hp.hpl.jena.ontology.daml.IntLiteralAccessor;

import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.DAMLVocabulary;


/**
 * Java representation of a DAML Restriction.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLRestrictionImpl.java,v 1.3 2003-02-03 22:49:39 ian_dickinson Exp $
 */
public class DAMLRestrictionImpl
    extends DAMLClassImpl
    implements DAMLRestriction
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** Property accessor for onProperty */
    private PropertyAccessor m_propOnProperty = null;

    /** Property accessor for hasClass */
    private PropertyAccessor m_propHasClass = null;

    /** Property accessor for toClass */
    private PropertyAccessor m_propToClass = null;

    /** Property accessor for hasValue */
    private PropertyAccessor m_propHasValue = null;

    /** Property accessor for hasClassQ */
    private PropertyAccessor m_propHasClassQ = null;

    /** Property accessor for cardinality */
    private IntLiteralAccessor m_propCardinality = null;

    /** Property accessor for minCardinality */
    private IntLiteralAccessor m_propMinCardinality = null;

    /** Property accessor for maxCardinality */
    private IntLiteralAccessor m_propMaxCardinality = null;

    /** Property accessor for cardinalityQ */
    private IntLiteralAccessor m_propCardinalityQ = null;

    /** Property accessor for minCardinalityQ */
    private IntLiteralAccessor m_propMinCardinalityQ = null;

    /** Property accessor for maxCardinalityQ */
    private IntLiteralAccessor m_propMaxCardinalityQ = null;



    // Constructors
    //////////////////////////////////

    /**
     * Constructor, takes the name and namespace for this restriction, and the underlying
     * model it will be attached to.
     *
     * @param namespace The namespace the restriction inhabits, or null
     * @param name The name of the restriction
     * @param store The RDF store that contains the RDF statements defining the properties of the restriction
     * @param vocabulary Reference to the DAML vocabulary used by this restriction.
     */
    public DAMLRestrictionImpl( String namespace, String name, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( namespace, name, store, vocabulary );

        setRDFType( getVocabulary().Restriction() );
    }


    /**
     * Constructor, takes the URI for this restriction, and the underlying
     * model it will be attached to.
     *
     * @param uri The URI of the restriction
     * @param store The RDF store that contains the RDF statements defining the properties of the restriction
     * @param vocabulary Reference to the DAML vocabulary used by this restriction.
     */
    public DAMLRestrictionImpl( String uri, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( uri, store, vocabulary );

        setRDFType( getVocabulary().Restriction() );
    }



    // External signature methods
    //////////////////////////////////


    /**
     * Answer true if this class expression is an enumeration (i&#046;e&#046; has a property
     * 'oneOf' with a list of values).  This is not an exclusive property, a class
     * expression can be an enumeration at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return necessarily false, since enumerations only appear in {@link com.hp.hpl.jena.ontology.daml.DAMLClass}
     *         expressions.
     */
    public boolean isEnumeration() {
        return false;
    }


    /**
     * Answer true if this class expression is an named class (i&#046;e&#046; is not an anonymous
     * class expression).  This is not an exclusive property, a class
     * expression can be named at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return necessarily false, since restrictions are necessarily constructed
     *         expressions. Note: this is not the same as testing whether the restriction
     *         itself has a name (so that it can be referenced elsewhere).
     */
    public boolean isNamedClass() {
        return false;
    }


    /**
     * Answer true if this class expression is an property restriction (i&#046;e&#046; is a
     * Restriction value).  This is not an exclusive property, a class
     * expression can be a property restriction at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return necessarily true.
     */
    public boolean isRestriction() {
        return true;
    }


    /**
     * Answer true if this class expression is an boolean intersection of a list
     * of class expressions.  This is not an exclusive property, a class
     * expression can be an intersection at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return necessarily false, since interesections only appear in {@link com.hp.hpl.jena.ontology.daml.DAMLClass}
     *         expressions.
     */
    public boolean isIntersection() {
        return false;
    }


    /**
     * Answer true if this class expression is an boolean union of a list
     * of class expressions.  This is not an exclusive property, a class
     * expression can be an union at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return necessarily false, since unions only appear in {@link com.hp.hpl.jena.ontology.daml.DAMLClass}
     *         expressions.
     */
    public boolean isUnion() {
        return false;
    }


    /**
     * Answer true if this class expression is an boolean complement of a list
     * of class expressions.  This is not an exclusive property, a class
     * expression can be an complement at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return necessarily false, since complements only appear in {@link com.hp.hpl.jena.ontology.daml.DAMLClass}
     *         expressions, as opposed to compliments, which you only get on your birthday.
     */
    public boolean isComplement() {
        return false;
    }


    /**
     * Property accessor for the 'onProperty' property of a restriction. This
     * denotes the property to which the restriction applies, and there is normally
     * exactly one of them.
     *
     * @return Property accessor for 'onProperty'.
     */
    public PropertyAccessor prop_onProperty() {
        if (m_propOnProperty == null) {
            m_propOnProperty = new PropertyAccessorImpl( getVocabulary().onProperty(), this );
        }

        return m_propOnProperty;
    }


    /**
     * Property accessor for the 'toClass' property of a restriction. This denotes
     * the class for which the restricted property always maps to instances that
     * belong to the class given by this property.
     *
     * @return Property accessor for 'toClass'
     */
    public PropertyAccessor prop_toClass() {
        if (m_propToClass == null) {
            m_propToClass = new PropertyAccessorImpl( getVocabulary().toClass(), this );
        }

        return m_propToClass;
    }


    /**
     * Property accessor for the 'hasValue' property of a restriction. This denotes
     * the class for which the restricted property sometimes maps to the instance given
     * here.
     *
     * @return Property accessor for 'hasValue'
     */
    public PropertyAccessor prop_hasValue() {
        if (m_propHasValue == null) {
            m_propHasValue = new PropertyAccessorImpl( getVocabulary().hasValue(), this );
        }

        return m_propHasValue;
    }


    /**
     * Property accessor for the 'hasClass' property of a restriction. This denotes
     * the class for which the restricted property sometimes maps to the instances that
     * belong to the class given here.
     *
     * @return Property accessor for 'hasClass'
     */
    public PropertyAccessor prop_hasClass() {
        if (m_propHasClass == null) {
            m_propHasClass = new PropertyAccessorImpl( getVocabulary().hasClass(), this );
        }

        return m_propHasClass;
    }


    /**
     * Property accessor for the 'hasClassQ' property of a restriction. This denotes
     * the class for which the restricted property sometimes maps to the instances that
     * belong to the class given here, and which obey given cardinality constraints.
     *
     * @return Property accessor for 'hasClassQ'
     */
    public PropertyAccessor prop_hasClassQ() {
        if (m_propHasClassQ == null) {
            m_propHasClassQ = new PropertyAccessorImpl( getVocabulary().hasClassQ(), this );
        }

        return m_propHasClassQ;
    }


    /**
     * Property accessor for the 'cardinality' property of a restriction. This denotes
     * the combination of minCardinality and maxCardinality to the same value.
     *
     * @return Property accessor for 'cardinality'
     */
    public IntLiteralAccessor prop_cardinality() {
        if (m_propCardinality == null) {
            m_propCardinality = new IntLiteralAccessorImpl( getVocabulary().cardinality(), this );
        }

        return m_propCardinality;
    }


    /**
     * Property accessor for the 'minCardinality' property of a restriction. This denotes
     * the class of instances that have at least N distict values for the property.
     *
     * @return Property accessor for 'minCardinality'
     */
    public IntLiteralAccessor prop_minCardinality() {
        if (m_propMinCardinality == null) {
            m_propMinCardinality = new IntLiteralAccessorImpl( getVocabulary().minCardinality(), this );
        }

        return m_propMinCardinality;
    }


    /**
     * Property accessor for the 'maxCardinality' property of a restriction. This denotes
     * the class of instances that have at most N distict values for the property.
     *
     * @return Property accessor for 'maxCardinality'
     */
    public IntLiteralAccessor prop_maxCardinality() {
        if (m_propMaxCardinality == null) {
            m_propMaxCardinality = new IntLiteralAccessorImpl( getVocabulary().maxCardinality(), this );
        }

        return m_propMaxCardinality;
    }


    /**
     * Property accessor for the 'cardinalityQ' property of a restriction. This denotes
     * the combination of minCardinalityQ and maxCardinalityQ to the same value.
     *
     * @return Property accessor for 'cardinalityQ'
     */
    public IntLiteralAccessor prop_cardinalityQ() {
        if (m_propCardinalityQ == null) {
            m_propCardinalityQ = new IntLiteralAccessorImpl( getVocabulary().cardinalityQ(), this );
        }

        return m_propCardinalityQ;
    }


    /**
     * Property accessor for the 'minCardinalityQ' property of a restriction. This denotes
     * the class of instances that have at least N distict values of the class denoted by
     * 'hasClassQ' for the property.
     *
     * @return Property accessor for 'minCardinalityQ'
     */
    public IntLiteralAccessor prop_minCardinalityQ() {
        if (m_propMinCardinalityQ == null) {
            m_propMinCardinalityQ = new IntLiteralAccessorImpl( getVocabulary().minCardinalityQ(), this );
        }

        return m_propMinCardinalityQ;
    }


    /**
     * Property accessor for the 'maxCardinalityQ' property of a restriction. This denotes
     * the class of instances that have at most N distict values of the class denoted by
     * 'hasClassQ' for the property.
     *
     * @return Property accessor for 'maxCardinalityQ'
     */
    public IntLiteralAccessor prop_maxCardinalityQ() {
        if (m_propMaxCardinalityQ == null) {
            m_propMaxCardinalityQ = new IntLiteralAccessorImpl( getVocabulary().maxCardinalityQ(), this );
        }

        return m_propMaxCardinalityQ;
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


    // Internal implementation methods
    //////////////////////////////////



    //==============================================================================
    // Inner class definitions
    //==============================================================================

}

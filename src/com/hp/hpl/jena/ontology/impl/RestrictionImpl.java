/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            31-Mar-2003
 * Filename           $RCSfile: RestrictionImpl.java,v $
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-05-23 11:12:51 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;



// Imports
///////////////
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.path.*;


/**
 * <p>
 * Implementation of the ontology abstraction representing restrictions.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: RestrictionImpl.java,v 1.8 2003-05-23 11:12:51 ian_dickinson Exp $
 */
public class RestrictionImpl 
    extends OntClassImpl
    implements Restriction 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating Restriction facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new RestrictionImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to Restriction");
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an Restriction facet if it has rdf:type owl:Restriction or equivalent
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, Restriction.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a restriction node represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public RestrictionImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>onProperty</code>
     * property of a restriction. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the imports of an ontology element
     */
    public PathSet p_onProperty() {
        return asPathSet( getProfile().ON_PROPERTY(), "ON_PROPERTY" );
    }
    

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>allValuesFrom</code>
     * property of a restriction. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the imports of an ontology element
     */
    public PathSet p_allValuesFrom() {
        return asPathSet( getProfile().ALL_VALUES_FROM(), "ALL_VALUES_FROM" );
    }
    

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>someValuesFrom</code>
     * property of a restriction. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the imports of an ontology element
     */
    public PathSet p_someValuesFrom() {
        return asPathSet( getProfile().SOME_VALUES_FROM(), "SOME_VALUES_FROM" );
    }
    

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>hasValue</code>
     * property of a restriction. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the imports of an ontology element
     */
    public PathSet p_hasValue() {
        return asPathSet( getProfile().HAS_VALUE(), "HAS_VALUE" );
    }
    

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>cardinality</code>
     * property of a restriction. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the imports of an ontology element
     */
    public PathSet p_cardinality() {
        return asPathSet( getProfile().CARDINALITY(), "CARDINALITY" );
    }
    

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>minCardinality</code>
     * property of a restriction. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the imports of an ontology element
     */
    public PathSet p_minCardinality() {
        return asPathSet( getProfile().MIN_CARDINALITY(), "MIN_CARDINALITY" );
    }
    

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>maxCardinality</code>
     * property of a restriction. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the imports of an ontology element
     */
    public PathSet p_maxCardinality() {
        return asPathSet( getProfile().MAX_CARDINALITY(), "MAX_CARDINALITY" );
    }
    

    /** 
     * <p>Answer a view of this restriction as an all values from  expression</p>
     * @return This class, but viewed as an AllValuesFromRestriction node
     * @exception ConversionException if the class cannot be converted to an all values from restriction
     * given the lanuage profile and the current state of the underlying model.
     */
    public AllValuesFromRestriction asAllValuesFromRestriction() {
        return (AllValuesFromRestriction) as( AllValuesFromRestriction.class );
    }
         
    /** 
     * <p>Answer a view of this restriction as a some values from  expression</p>
     * @return This class, but viewed as a SomeValuesFromRestriction node
     * @exception ConversionException if the class cannot be converted to an all values from restriction
     * given the lanuage profile and the current state of the underlying model.
     */
    public SomeValuesFromRestriction asSomeValuesFromRestriction() {
        return (SomeValuesFromRestriction) as( SomeValuesFromRestriction.class );
    }
         
    /** 
     * <p>Answer a view of this restriction as a has value expression</p>
     * @return This class, but viewed as a HasValueRestriction node
     * @exception ConversionException if the class cannot be converted to a has value restriction
     * given the lanuage profile and the current state of the underlying model.
     */
    public HasValueRestriction asHasValueRestriction() {
        return (HasValueRestriction) as( HasValueRestriction.class );
    }
         
    /** 
     * <p>Answer a view of this restriction as a cardinality restriction class expression</p>
     * @return This class, but viewed as a CardinalityRestriction node
     * @exception ConversionException if the class cannot be converted to a cardinality restriction
     * given the lanuage profile and the current state of the underlying model.
     */
    public CardinalityRestriction asCardinalityRestriction() {
        return (CardinalityRestriction) as( CardinalityRestriction.class );
    }

    /** 
     * <p>Answer a view of this restriction as a min cardinality restriction class expression</p>
     * @return This class, but viewed as a MinCardinalityRestriction node
     * @exception ConversionException if the class cannot be converted to a min cardinality restriction
     * given the lanuage profile and the current state of the underlying model.
     */
    public MinCardinalityRestriction asMinCardinalityRestriction() {
        return (MinCardinalityRestriction) as( MinCardinalityRestriction.class );
    }

    /** 
     * <p>Answer a view of this restriction as a max cardinality restriction class expression</p>
     * @return This class, but viewed as a MaxCardinalityRestriction node
     * @exception ConversionException if the class cannot be converted to a max cardinality restriction
     * given the lanuage profile and the current state of the underlying model.
     */
    public MaxCardinalityRestriction asMaxCardinalityRestriction() {
        return (MaxCardinalityRestriction) as( MaxCardinalityRestriction.class );
    }



    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

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



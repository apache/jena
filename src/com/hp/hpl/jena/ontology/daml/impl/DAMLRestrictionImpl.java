/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLRestrictionImpl.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-06-18 21:56:07 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved. 
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.vocabulary.*;


/**
 * <p>Java encapsulation of a DAML Restriction.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLRestrictionImpl.java,v 1.4 2003-06-18 21:56:07 ian_dickinson Exp $
 */
public class DAMLRestrictionImpl
    extends DAMLClassImpl
    implements DAMLRestriction
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating DAMLRestriction facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new DAMLRestrictionImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to DAMLRestriction" );
            } 
        }
            
        public boolean canWrap( Node n, EnhGraph g ) {
            return hasType( n, g, DAML_OIL.Restriction );
        }
    };

    // Instance variables
    //////////////////////////////////

    /** Property accessor for onProperty */
    private PropertyAccessor m_propOnProperty = new PropertyAccessorImpl( getVocabulary().onProperty(), this );

    /** Property accessor for hasClass */
    private PropertyAccessor m_propHasClass = new PropertyAccessorImpl( getVocabulary().hasClass(), this );

    /** Property accessor for toClass */
    private PropertyAccessor m_propToClass = new PropertyAccessorImpl( getVocabulary().toClass(), this );

    /** Property accessor for hasValue */
    private PropertyAccessor m_propHasValue = new PropertyAccessorImpl( getVocabulary().hasValue(), this );

    /** Property accessor for hasClassQ */
    private PropertyAccessor m_propHasClassQ = new PropertyAccessorImpl( getVocabulary().hasClassQ(), this );

    /** Property accessor for cardinality */
    private IntLiteralAccessor m_propCardinality = new IntLiteralAccessorImpl( getVocabulary().cardinality(), this );

    /** Property accessor for minCardinality */
    private IntLiteralAccessor m_propMinCardinality = new IntLiteralAccessorImpl( getVocabulary().minCardinality(), this );

    /** Property accessor for maxCardinality */
    private IntLiteralAccessor m_propMaxCardinality = new IntLiteralAccessorImpl( getVocabulary().maxCardinality(), this );

    /** Property accessor for cardinalityQ */
    private IntLiteralAccessor m_propCardinalityQ = new IntLiteralAccessorImpl( getVocabulary().cardinalityQ(), this );

    /** Property accessor for minCardinalityQ */
    private IntLiteralAccessor m_propMinCardinalityQ = new IntLiteralAccessorImpl( getVocabulary().minCardinalityQ(), this );

    /** Property accessor for maxCardinalityQ */
    private IntLiteralAccessor m_propMaxCardinalityQ = new IntLiteralAccessorImpl( getVocabulary().maxCardinalityQ(), this );



    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a DAML restriction represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public DAMLRestrictionImpl( Node n, EnhGraph g ) {
        super( n, g );
    }
    


    // External signature methods
    //////////////////////////////////


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


    public PropertyAccessor prop_onProperty()        {        return m_propOnProperty;    }
    public PropertyAccessor prop_toClass()           {        return m_propToClass;    }
    public PropertyAccessor prop_hasValue()          {        return m_propHasValue;    }
    public PropertyAccessor prop_hasClass()          {        return m_propHasClass;    }
    public IntLiteralAccessor prop_cardinality()     {        return m_propCardinality;    }
    public IntLiteralAccessor prop_minCardinality()  {        return m_propMinCardinality;    }
    public IntLiteralAccessor prop_maxCardinality()  {        return m_propMaxCardinality;    }
    public PropertyAccessor prop_hasClassQ()         {        return m_propHasClassQ;    }
    public IntLiteralAccessor prop_cardinalityQ()    {        return m_propCardinalityQ;    }
    public IntLiteralAccessor prop_minCardinalityQ() {        return m_propMinCardinalityQ;    }
    public IntLiteralAccessor prop_maxCardinalityQ() {        return m_propMaxCardinalityQ;    }


    // Internal implementation methods
    //////////////////////////////////



    //==============================================================================
    // Inner class definitions
    //==============================================================================

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


/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLRestriction.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-06-17 21:56:29 $
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


/**
 * <p>Java encapsulation of a DAML Restriction.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLRestriction.java,v 1.2 2003-06-17 21:56:29 ian_dickinson Exp $
 */
public interface DAMLRestriction
    extends DAMLClass
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


    /**
     * <p>Property accessor for the <code>onProperty</code> property of a restriction. This
     * denotes the property to which the restriction applies, and there is normally
     * exactly one of them.
     *
     * @return Property accessor for <code>daml:onProperty</code>.
     */
    public PropertyAccessor prop_onProperty();


    /**
     * <p>Property accessor for the <code>toClass</code> property of a restriction. This denotes
     * the class for which the restricted property always maps to instances that
     * belong to the class given by this property.</p>
     *
     * @return Property accessor for <code>toClass</code>
     */
    public PropertyAccessor prop_toClass();


    /**
     * <p>Property accessor for the <code>hasValue</code> property of a restriction. This is
     * used to construct a class expression in which the restricted property has the
     * value indicated by this property.</p>
     *
     * @return Property accessor for <code>hasValue</code>
     */
    public PropertyAccessor prop_hasValue();


    /**
     * <p>Property accessor for the <code>hasClass</code> property of a restriction. This is
     * used to construct a class expression in which the restricted property has at least
     * one value belonging to the class indicated by this property.</p>
     *
     * @return Property accessor for <code>hasClass</code>
     */
    public PropertyAccessor prop_hasClass();


    /**
     * <p>Property accessor for the <code>hasClassQ</code> property of a restriction. This is
     * used to construct a class expression in which a cardinality constraint is combined
     * with a has-class restriction.</p>
     *
     * @return Property accessor for <code>hasClassQ</code>
     */
    public PropertyAccessor prop_hasClassQ();


    /**
     * <p>Property accessor for the <code>cardinality</code> property of a restriction. This denotes
     * the combination of minCardinality and maxCardinality to the same value.</p>
     *
     * @return Property accessor for <code>cardinality</code>
     */
    public IntLiteralAccessor prop_cardinality();


    /**
     * <p>Property accessor for the <code>minCardinality</code> property of a restriction. This denotes
     * the class of instances that have at least this number of distict values for the property.</p>
     *
     * @return Property accessor for <code>minCardinality</code>
     */
    public IntLiteralAccessor prop_minCardinality();


    /**
     * <p>Property accessor for the <code>maxCardinality</code> property of a restriction. This denotes
     * the class of instances that have at most this number of distict values for the property.</p>
     *
     * @return Property accessor for <code>maxCardinality</code>
     */
    public IntLiteralAccessor prop_maxCardinality();


    /**
     * <p>Property accessor for the <code>cardinalityQ</code> property of a restriction. This denotes
     * the combination of minCardinalityQ and maxCardinalityQ to the same value.</p>
     *
     * @return Property accessor for <code>cardinalityQ</code>
     */
    public IntLiteralAccessor prop_cardinalityQ();


    /**
     * <p>Property accessor for the <code>minCardinalityQ</code> property of a restriction. This denotes
     * the class of instances that have at least this many distinct values of the class denoted by
     * <code>hasClassQ</code> for the property.</p>
     *
     * @return Property accessor for <code>minCardinalityQ</code>
     */
    public IntLiteralAccessor prop_minCardinalityQ();


    /**
     * <p>Property accessor for the <code>maxCardinalityQ</code> property of a restriction. This denotes
     * the class of instances that have at most this many distinct values of the class denoted by
     * <code>hasClassQ</code> for the property.
     *
     * @return Property accessor for <code>maxCardinalityQ</code>
     */
    public IntLiteralAccessor prop_maxCardinalityQ();

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


/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: OntProperty.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-05-08 16:57:11 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved. (see
 * footer for full conditions)
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import com.hp.hpl.jena.ontology.path.PathSet;
import com.hp.hpl.jena.rdf.model.*;



/**
 * <p>
 * Interface encapsulating a property in an ontology.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntProperty.java,v 1.3 2003-05-08 16:57:11 ian_dickinson Exp $
 */
public interface OntProperty
    extends OntResource, Property
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>subPropertyOf</code>
     * property of a property description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the subPropertyOf relation on properties
     */
    public PathSet p_subPropertyOf();


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>domain</code>
     * property of a property description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the domain of a property
     */
    public PathSet p_domain();


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>range</code>
     * property of a property description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the range of a property
     */
    public PathSet p_range();


    // relationships between properties

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>equivalentProperty</code>
     * property of a property description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for property equivalence
     */
    public PathSet p_equivalentProperty();


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>inverseOf</code>
     * property of a property description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for property invserses
     */
    public PathSet p_inverseOf();


    /** 
     * <p>Answer a view of this property as a functional property</p>
     * @return This property, but viewed as a FunctionalProperty node
     * @exception ConversionException if the resource cannot be converted to a functional property
     * given the lanuage profile and the current state of the underlying model.
     */
    public FunctionalProperty asFunctionalProperty();

    /** 
     * <p>Answer a view of this property as a datatype property</p>
     * @return This property, but viewed as a DatatypeProperty node
     * @exception ConversionException if the resource cannot be converted to a datatype property
     * given the lanuage profile and the current state of the underlying model.
     */
    public DatatypeProperty asDatatypeProperty();

    /** 
     * <p>Answer a view of this property as an object property</p>
     * @return This property, but viewed as an ObjectProperty node
     * @exception ConversionException if the resource cannot be converted to an object property
     * given the lanuage profile and the current state of the underlying model.
     */
    public ObjectProperty asObjectProperty();
    
    /** 
     * <p>Answer a view of this property as a transitive property</p>
     * @return This property, but viewed as a TransitiveProperty node
     * @exception ConversionException if the resource cannot be converted to a transitive property
     * given the lanuage profile and the current state of the underlying model.
     */
    public TransitiveProperty asTransitiveProperty();
    
    /** 
     * <p>Answer a view of this property as an inverse functional property</p>
     * @return This property, but viewed as an InverseFunctionalProperty node
     * @exception ConversionException if the resource cannot be converted to an inverse functional property
     * given the lanuage profile and the current state of the underlying model.
     */
    public InverseFunctionalProperty asInverseFunctionalProperty();
    
    /** 
     * <p>Answer a view of this property as a symmetric property</p>
     * @return This property, but viewed as a SymmetricProperty node
     * @exception ConversionException if the resource cannot be converted to a symmetric property
     * given the lanuage profile and the current state of the underlying model.
     */
    public SymmetricProperty asSymmetricProperty();
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


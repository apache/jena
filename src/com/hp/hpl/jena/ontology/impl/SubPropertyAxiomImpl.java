/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            29-Apr-2003
 * Filename           $RCSfile: SubPropertyAxiomImpl.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-04-30 09:59:25 $
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
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * Implementation of the sub-property-of axiom.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: SubPropertyAxiomImpl.java,v 1.1 2003-04-30 09:59:25 ian_dickinson Exp $
 */
public class SubPropertyAxiomImpl
    extends PropertyAxiomImpl
    implements SubPropertyAxiom 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a subPropertyOf axiom.
     * </p>
     * 
     * @param subProperty The property resource that is the sub-property in this axiom
     * @param superProperty The property resource that is the super-property in this axiom
     */
    public SubPropertyAxiomImpl(  Resource subProperty, Resource superProperty ) {
        super( subProperty, ((OntModel) subProperty.getModel()).getProfile().SUB_PROPERTY_OF(), superProperty  );
    }


    /**
     * <p>
     * Construct a subPropertyOf axiom more efficiently. Package access only.
     * </p>
     * 
     * @param subProperty The property resource that is the sub-property in this axiom
     * @param subPropertyP Assumed to be the subPropertyOf predicate (not tested, taken on faith).
     * @param superProperty The property resource that is the super-property in this axiom
     */
    SubPropertyAxiomImpl(  Resource subProperty, Property subPropertyP, Resource superProperty ) {
        super( subProperty, subPropertyP, superProperty  );
    }



    // External signature methods
    //////////////////////////////////

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

/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            29-Apr-2003
 * Filename           $RCSfile: SubClassAxiomImpl.java,v $
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
 * Implementation of the sub-class-of axiom 
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: SubClassAxiomImpl.java,v 1.1 2003-04-30 09:59:25 ian_dickinson Exp $
 */
public class SubClassAxiomImpl 
    extends ClassAxiomImpl
    implements SubClassAxiom
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
     * Construct a subClassOf axiom.
     * </p>
     * 
     * @param subClass The class resource that is the sub-class in this axiom
     * @param superClass The class resource that is the super-class in this axiom
     */
    public SubClassAxiomImpl(  Resource subClass, Resource superClass ) {
        super( subClass, ((OntModel) subClass.getModel()).getProfile().SUB_CLASS_OF(), superClass  );
    }


    /**
     * <p>
     * Construct a subClassOf axiom more efficiently. Package access only.
     * </p>
     * 
     * @param subClass The class resource that is the sub-class in this axiom
     * @param subClassP Assumed to be the subClassOf predicate (not tested, taken on faith).
     * @param superClass The class resource that is the super-class in this axiom
     */
    SubClassAxiomImpl(  Resource subClass, Property subClassP, Resource superClass ) {
        super( subClass, subClassP, superClass  );
    }


    // External signature methods
    //////////////////////////////////

    /** 
     * <p>
     * Answer true if the axiom has modality <i>complete</i>. This is only properly
     * defined for the distinction between <code>subClassOf</code> and <code>equivalentClass</code>,
     * but by default is assumed true.
     * </p>
     * 
     * @return False (sub-class is a partial, not complete, modality)
     */
    public boolean isComplete() {
        return false;
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

/* TODO delete me
public class SubClassAxiomImpl{

}

*/
/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            31-Mar-2003
 * Filename           $RCSfile: IndividualImpl.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-08 21:29:44 $
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
import java.util.Iterator;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;


/**
 * <p>
 * Implementation for the ontology abstraction representing ontology class descriptions.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: IndividualImpl.java,v 1.7 2003-06-08 21:29:44 ian_dickinson Exp $
 */
public class IndividualImpl
    extends OntResourceImpl
    implements Individual 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /** 
     * A factory for generating Individual facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { return new IndividualImpl( n, eg ); }
        public boolean canWrap( Node n, EnhGraph eg ) { return true; }
    };
    
    



    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an individual represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public IndividualImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>Assert equivalence between the given individual and this individual. Any existing 
     * statements for <code>sameIndividualAs</code> will be removed.</p>
     * <p>Note that <code>sameAs</code> and <code>sameIndividualAs</code> are aliases.</p>
     * @param res The resource that declared to be the same as this individual
     * @exception OntProfileException If the sameIndividualAs property is not supported in the current language profile.   
     */ 
    public void setSameIndividualAs( Resource res ) {
        setPropertyValue( getProfile().SAME_INDIVIDUAL_AS(), "SAME_INDIVIDUAL_AS", res );
    }

    /**
     * <p>Add an individual that is declared to be equivalent to this individual.</p>
     * <p>Note that <code>sameAs</code> and <code>sameIndividualAs</code> are aliases.</p>
     * @param res A resource that declared to be the same as this individual
     * @exception OntProfileException If the sameIndividualAs property is not supported in the current language profile.   
     */ 
    public void addSameIndividualAs( Resource res ) {
        addPropertyValue( getProfile().SAME_INDIVIDUAL_AS(), "SAME_INDIVIDUAL_AS", res );
    }

    /**
     * <p>Answer a resource that is declared to be the same as this individual. If there are
     * more than one such resource, an arbitrary selection is made.</p>
     * <p>Note that <code>sameAs</code> and <code>sameIndividualAs</code> are aliases.</p>
     * @return res An ont resource that declared to be the same as this individual
     * @exception OntProfileException If the sameIndividualAs property is not supported in the current language profile.   
     */ 
    public OntResource getSameIndividualAs() {
        return objectAsResource( getProfile().SAME_INDIVIDUAL_AS(), "SAME_INDIVIDUAL_AS" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to be equivalent to
     * this individual. Each elemeent of the iterator will be an {@link OntResource}.</p>
     * <p>Note that <code>sameAs</code> and <code>sameIndividualAs</code> are aliases.</p>
     * @return An iterator over the resources equivalent to this individual.
     * @exception OntProfileException If the sameIndividualAs property is not supported in the current language profile.   
     */ 
    public Iterator listSameIndividualAs() {
        return listAs( getProfile().SAME_INDIVIDUAL_AS(), "SAME_INDIVIDUAL_AS", OntResource.class );
    }

    /**
     * <p>Answer true if this individual is the same as the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared the same via a <code>sameIndividualAs</code> statement.
     */
    public boolean isSameIndividualAs( Resource res ) {
        return hasPropertyValue( getProfile().SAME_INDIVIDUAL_AS(), "SAME_INDIVIDUAL_AS", res );
    }
    
    /**
     * <p>Remove the statement that this individual is the same as the given individual.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to be the sameIndividualAs this resource
     */
    public void removeSameIndividualAs( Resource res ) {
        removePropertyValue( getProfile().SAME_INDIVIDUAL_AS(), "SAME_INDIVIDUAL_AS", res );
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


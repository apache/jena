/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: Individual.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-21 12:35:39 $
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
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Interface that encapsulates an <i>individual</i> in an ontology, sometimes
 * referred to as a fact or assertion, or a member of the <i>a-box</i>. In order
 * to be recognised as an individual, rather than a generic resource,
 * at least one <code>rdf:type</code> statement, referring to a known class,
 * must  be present in the model.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: Individual.java,v 1.7 2003-06-21 12:35:39 ian_dickinson Exp $
 */
public interface Individual
    extends OntResource
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>Assert equivalence between the given individual and this individual. Any existing 
     * statements for <code>sameIndividualAs</code> will be removed.</p>
     * <p>Note that <code>sameAs</code> and <code>sameIndividualAs</code> are aliases.</p>
     * @param res The resource that declared to be the same as this individual
     * @exception OntProfileException If the sameIndividualAs property is not supported in the current language profile.   
     */ 
    public void setSameIndividualAs( Resource res );

    /**
     * <p>Add an individual that is declared to be equivalent to this individual.</p>
     * <p>Note that <code>sameAs</code> and <code>sameIndividualAs</code> are aliases.</p>
     * @param res A resource that declared to be the same as this individual
     * @exception OntProfileException If the sameIndividualAs property is not supported in the current language profile.   
     */ 
    public void addSameIndividualAs( Resource res );

    /**
     * <p>Answer a resource that is declared to be the same as this individual. If there are
     * more than one such resource, an arbitrary selection is made.</p>
     * <p>Note that <code>sameAs</code> and <code>sameIndividualAs</code> are aliases.</p>
     * @return res An ont resource that declared to be the same as this individual
     * @exception OntProfileException If the sameIndividualAs property is not supported in the current language profile.   
     */ 
    public OntResource getSameIndividualAs();

    /**
     * <p>Answer an iterator over all of the resources that are declared to be equivalent to
     * this individual. Each elemeent of the iterator will be an {@link OntResource}.</p>
     * <p>Note that <code>sameAs</code> and <code>sameIndividualAs</code> are aliases.</p>
     * @return An iterator over the resources equivalent to this individual.
     * @exception OntProfileException If the sameIndividualAs property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listSameIndividualAs();

    /**
     * <p>Answer true if this individual is the same as the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared the same via a <code>sameIndividualAs</code> statement.
     */
    public boolean isSameIndividualAs( Resource res );
    
    /**
     * <p>Remove the statement that this individual is the same as the given individual.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to be the sameIndividualAs this resource
     */
    public void removeSameIndividualAs( Resource res );
    


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


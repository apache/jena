/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: AllDifferent.java,v $
 * Revision           $Revision: 1.10 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-21 12:35:39 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved. 
 * (see footer for full conditions)
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;



// Imports
///////////////
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Interface defining an individual in which all members of a collection are
 * declared pair-wise disjoint.  This allows ontologies that wish to support the
 * unique names assumption to add this condition in languages (like OWL) that
 * do not make the same assumption, with a minimum number of statements.
 * Instances of the all different axiom are expected to have a property
 * (e.g. <code>owl:distinctMembers</code> defining the list of distinct
 * individuals in the ontology.  For a given vocabulary, this will be defined by
 * the {@linkplain Profile#DISTINCT_MEMBERS distinctMembers} entry.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: AllDifferent.java,v 1.10 2003-06-21 12:35:39 ian_dickinson Exp $
 */
public interface AllDifferent
    extends OntResource
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>Assert that the list of distinct individuals in this AllDifferent declaration
     * is the given list. Any existing 
     * statements for <code>distinctMembers</code> will be removed.</p>
     * @param members A list of the members that are declared to be distinct.
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */ 
    public void setDistinct( RDFList members );

    /**
     * <p>Add the given individual to the list of distinct members of this AllDifferent declaration.</p>
     * @param res A resource that will be added to the list of all different members.
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */ 
    public void addDistinct( Resource res );

    /**
     * <p>Add the given individuals to the list of distinct members of this AllDifferent declaration.</p>
     * @param individuals An iterator over the distinct invididuals that will be added 
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */ 
    public void addDistinct( Iterator individuals );

    /**
     * <p>Answer the list of distinct members for this AllDifferent declaration.</p>
     * @return The list of individuals declared distinct by this AllDifferent declaration.
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */ 
    public RDFList getDistinct();

    /**
     * <p>Answer an iterator over all of the individuals that are declared to be distinct by
     * this AllDifferent declaration. Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over distinct individuals.
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listDistinct();

    /**
     * <p>Answer true if this AllDifferent declaration includes <code>res</code> as one of the distinct individuals.</p>
     * @param res A resource to test against
     * @return True if <code>res</code> is declared to be distinct from the other individuals in this declation.
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */
    public boolean hasDistinctMember( Resource res );
    
    /**
     * <p>Remove the given resource from the list of distinct individuals.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that is no longer distinct from the other listed individuals
     */
    public void removeDistinctMember( Resource res );
    
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


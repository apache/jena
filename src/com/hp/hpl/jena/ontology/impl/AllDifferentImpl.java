/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            01-Apr-2003
 * Filename           $RCSfile: AllDifferentImpl.java,v $
 * Revision           $Revision: 1.16 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:06:14 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import java.util.Iterator;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Implementation of the abstraction of axioms that denote the single name assumption.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: AllDifferentImpl.java,v 1.16 2005-02-21 12:06:14 andy_seaborne Exp $
 */
public class AllDifferentImpl
    extends OntResourceImpl
    implements AllDifferent 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating AllDifferent facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new AllDifferentImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to AllDifferent");
            } 
        }
        
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an AllDifferent facet if it has rdf:type owl:AllDifferent or equivalent
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, AllDifferent.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an all different axiom represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the axiom
     * @param g The enhanced graph that contains n
     */
    public AllDifferentImpl( Node n, EnhGraph g ) {
        super( n, g );
    }



    // External signature methods
    //////////////////////////////////

    /**
     * <p>Assert that the list of distinct individuals in this AllDifferent declaration
     * is the given list. Any existing 
     * statements for <code>distinctMembers</code> will be removed.</p>
     * @param members A list of the members that are declared to be distinct.
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */ 
    public void setDistinctMembers( RDFList members ) {
        setPropertyValue( getProfile().DISTINCT_MEMBERS(), "DISTINCT_MEMBERS", members );
    }

    /**
     * <p>Add the given individual to the list of distinct members of this AllDifferent declaration.</p>
     * @param res A resource that will be added to the list of all different members.
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */ 
    public void addDistinctMember( Resource res ) {
        addListPropertyValue( getProfile().DISTINCT_MEMBERS(), "DISTINCT_MEMBERS", res );
    }

    /**
     * <p>Add the given individuals to the list of distinct members of this AllDifferent declaration.</p>
     * @param individuals An iterator over the distinct invididuals that will be added 
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */ 
    public void addDistinctMembers( Iterator individuals ) {
        while (individuals.hasNext()) {
            addDistinctMember( (Resource) individuals.next() );
        }
    }

    /**
     * <p>Answer the list of distinct members for this AllDifferent declaration.</p>
     * @return The list of individuals declared distinct by this AllDifferent declaration.
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */ 
    public RDFList getDistinctMembers() {
        return (RDFList) objectAs( getProfile().DISTINCT_MEMBERS(), "DISTINCT_MEMBERS", RDFList.class );
    }

    /**
     * <p>Answer an iterator over all of the individuals that are declared to be distinct by
     * this AllDifferent declaration. Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over distinct individuals.
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listDistinctMembers() {
        return getDistinctMembers().iterator();
    }

    /**
     * <p>Answer true if this AllDifferent declaration includes <code>res</code> as one of the distinct individuals.</p>
     * @param res A resource to test against
     * @return True if <code>res</code> is declared to be distinct from the other individuals in this declation.
     * @exception OntProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.   
     */
    public boolean hasDistinctMember( Resource res ) {
        return getDistinctMembers().contains( res );
    }
    
    /**
     * <p>Remove the given resource from the list of distinct individuals.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that is no longer distinct from the other listed individuals
     */
    public void removeDistinctMember( Resource res ) {
        setDistinctMembers( getDistinctMembers().remove( res ) );
    }
    


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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


/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            28-Apr-2003
 * Filename           $RCSfile: EnumeratedClassImpl.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-16 13:40:12 $
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

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * Implementation of a node representing an enumerated class description.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: EnumeratedClassImpl.java,v 1.9 2003-06-16 13:40:12 ian_dickinson Exp $
 */
public class EnumeratedClassImpl 
    extends OntClassImpl
    implements EnumeratedClass
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating EnumeratedClass facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new EnumeratedClassImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to EnumeratedClass");
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an EnumeratedClass facet if it has rdf:type owl:Class and an owl:oneOf statement (or equivalents) 
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  
                   profile.isSupported( node, eg, OntClass.class )  &&
                   eg.asGraph().contains( node, profile.ONE_OF().asNode(), null );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an enumerated class node represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public EnumeratedClassImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    // oneOf
    
    /**
     * <p>Assert that this class is exactly the enumeration of the given individuals. Any existing 
     * statements for <code>oneOf</code> will be removed.</p>
     * @param A list of individuals that defines the class extension for this class
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public void setOneOf( RDFList enum ) {
        setPropertyValue( getProfile().ONE_OF(), "ONE_OF", enum );
    }

    /**
     * <p>Add an individual to the enumeration that defines the class extension of this class.</p>
     * @param res An individual to add to the enumeration
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public void addOneOf( Resource res ) {
        addListPropertyValue( getProfile().ONE_OF(), "ONE_OF", res );
    }

    /**
     * <p>Add each individual from the given iteratation to the 
     * enumeration that defines the class extension of this class.</p>
     * @param individuals An iterator over individuals
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public void addOneOf( Iterator individuals ) {
        while( individuals.hasNext() ) {
            addOneOf( (Resource) individuals.next() );
        }
    }

    /**
     * <p>Answer a list of individuals that defines the extension of this class.</p>
     * @return A list of individuals that is the class extension
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public RDFList getOneOf() {
        return (RDFList) objectAs( getProfile().ONE_OF(), "ONE_OF", RDFList.class );
    }

    /**
     * <p>Answer an iterator over all of the individuals that are declared to be the class extension for
     * this class. Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the individuals in the class extension
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public Iterator listOneOf() {
        return getOneOf().iterator();
    }

    /**
     * <p>Answer true if the given individual is one of the enumerated individuals in the class extension
     * of this class.</p>
     * @param res An individual to test
     * @return True if the given individual is in the class extension for this class.
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */
    public boolean hasOneOf( Resource res ) {
        return getOneOf().contains( res );
    }
    
    /**
     * <p>Remove the statement that this enumeration includes <code>res</code> among its members.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to be part of this enumeration, and which is
     * no longer one of the enumeration values.
     */
    public void removeOneOf( Resource res ) {
        setOneOf( getOneOf().remove( res ) );
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



/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            19-Aug-2003
 * Filename           $RCSfile: DataRangeImpl.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-08-19 16:50:30 $
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
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Default implementation of the interface that defines a closed enumeration
 * of concrete values for the range of a property.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DataRangeImpl.java,v 1.1 2003-08-19 16:50:30 ian_dickinson Exp $
 */
public class DataRangeImpl 
    extends OntResourceImpl
    implements DataRange
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating DataRange facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new DataRangeImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to DataRange");
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an DataRange facet if it has rdf:type owl:Datarange and is a bNode
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, DataRange.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a data range node represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public DataRangeImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    // oneOf
    
    /**
     * <p>Assert that this data range is exactly the enumeration of the given individuals. Any existing 
     * statements for <code>oneOf</code> will be removed.</p>
     * @param emum A list of literals that defines the permissible values for this datarange
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public void setOneOf( RDFList enum ) {
        setPropertyValue( getProfile().ONE_OF(), "ONE_OF", enum );
    }

    /**
     * <p>Add a literal to the enumeration that defines the permissible values of this class.</p>
     * @param lit A literal to add to the enumeration
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public void addOneOf( Literal lit ) {
        addListPropertyValue( getProfile().ONE_OF(), "ONE_OF", lit );
    }

    /**
     * <p>Add each literal from the given iteratation to the 
     * enumeration that defines the permissible values of this datarange.</p>
     * @param literals An iterator over literals
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public void addOneOf( Iterator literals ) {
        while( literals.hasNext() ) {
            addOneOf( (Literal) literals.next() );
        }
    }

    /**
     * <p>Answer a list of literals that defines the extension of this datarange.</p>
     * @return A list of literals that is the permissible values
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public RDFList getOneOf() {
        return (RDFList) objectAs( getProfile().ONE_OF(), "ONE_OF", RDFList.class );
    }

    /**
     * <p>Answer an iterator over all of the literals that are declared to be the permissible values for
     * this class. Each element of the iterator will be an {@link Literal}.</p>
     * @return An iterator over the literals that are the permissible values
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listOneOf() {
        return getOneOf().iterator().mapWith( new AsMapper( Literal.class ) );
    }

    /**
     * <p>Answer true if the given literal is one of the enumerated literals that are the permissible values
     * of this datarange.</p>
     * @param lit A literal to test
     * @return True if the given literal is in the permissible values for this class.
     * @exception OntProfileException If the {@link Profile#ONE_OF()} property is not supported in the current language profile.   
     */
    public boolean hasOneOf( Literal lit ) {
        return getOneOf().contains( lit );
    }
    
    /**
     * <p>Remove the statement that this enumeration includes <code>lit</code> among its members.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A literal that may be declared to be part of this data range, and which is
     * no longer to be one of the data range values.
     */
    public void removeOneOf( Literal lit ) {
        setOneOf( getOneOf().remove( lit ) );
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


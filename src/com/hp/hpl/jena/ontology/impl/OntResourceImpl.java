/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            25-Mar-2003
 * Filename           $RCSfile: OntResourceImpl.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-04-07 09:34:34 $
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
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.path.*;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;


/**
 * <p>
 * Abstract base class to provide shared implementation for implementations of ontology
 * resources.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntResourceImpl.java,v 1.3 2003-04-07 09:34:34 ian_dickinson Exp $
 */
public abstract class OntResourceImpl
    extends ResourceImpl
    implements OntResource 
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
     * Construct an ontology resource represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public OntResourceImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer the ontology language profile that governs the ontology model to which
     * this ontology resource is attached.  
     * </p>
     * 
     * @return The language profile for this ontology resource
     */
    public Profile getProfile() {
        return ((OntModel) getModel()).getProfile();
    }


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>sameAs</code>
     * property of any instance. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * <b>Note:</b> that any ontology resource can be declared to be the same as another. However,
     * in the case of OWL, doing so for class or property resources necessarily implies that
     * OWL Full is being used, since in OWL DL and Lite classes and properties cannot be used
     * as instances.
     * </p>
     * 
     * @return An abstract accessor for identity between individuals
     */
    public PathSet p_sameAs() {
        return asPathSet( getProfile().SAME_AS() );
    }


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>sameIndidualAs</code>
     * property of any instance. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * A synonym for {@link #p_sameAs sameAs}.
     * </p>
     * 
     * @return An abstract accessor for identity between individuals
     */
    public PathSet p_sameIndividualAs() {
        return asPathSet( getProfile().SAME_INDIVIDUAL_AS() );
    }


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>differentFrom</code>
     * property of any instance. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for asserting non-identity between individuals
     */
    public PathSet p_differentFrom() {
        return asPathSet( getProfile().DIFFERENT_FROM() );
    }


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>versionInfo</code>
     * property of any instance. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the owl:versionInfo annotation property
     */
    public PathSet p_versionInfo() {
        return asPathSet( getProfile().VERSION_INFO() );
    }


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>label</code>
     * property of any instance. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the rdfs:label annotation property
     */
    public PathSet p_label() {
        return asPathSet( getProfile().LABEL() );
    }


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>comment</code>
     * property of any instance. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the rdfs:comment annotation property
     */
    public PathSet p_comment() {
        return asPathSet( getProfile().COMMENT() );
    }


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>seeAlso</code>
     * property of any instance. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the rdfs:seeAlso annotation property
     */
    public PathSet p_seeAlso() {
        return asPathSet( getProfile().SEE_ALSO() );
    }


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>isDefinedBy</code>
     * property of any instance. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the rdfs:isDefinedBy annotation property
     */
    public PathSet p_isDefinedBy() {
        return asPathSet( getProfile().IS_DEFINED_BY() );
    }
    
    

    // Internal implementation methods
    //////////////////////////////////


    protected PathSet asPathSet( Property p ) {
        if (p == null) {
            // TODO ideally should name the property to be helpful here
            throw new OntologyException( "This property is not defined in the current language profile" );
        }
        else {
            return new PathSet( this, PathFactory.unit( p ) );
        }
    }
    
    
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

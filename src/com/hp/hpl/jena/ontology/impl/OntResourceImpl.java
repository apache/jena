/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            25-Mar-2003
 * Filename           $RCSfile: OntResourceImpl.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-04-16 11:36:55 $
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
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.util.iterator.UniqueExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;


/**
 * <p>
 * Abstract base class to provide shared implementation for implementations of ontology
 * resources.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntResourceImpl.java,v 1.4 2003-04-16 11:36:55 ian_dickinson Exp $
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
    
    
    /**
     * <p>
     * Answer an {@link PathSet accessor} for the given
     * property of any ontology value. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @param p A property
     * @return An abstract accessor for the property p
     */
    public PathSet accessor( Property p ) {
        return asPathSet( p );
    }
    
    
    /**
     * <p>
     * Set the value of the given property of this ontology resource to the given
     * value, encoded as an RDFNode.  Maintains the invariant that there is
     * at most one value of the property for a given resource, so existing
     * property values are first removed.  To add multiple properties, use
     * {@link #addProperty( Property, RDFNode ) addProperty}.
     * </p>
     * 
     * @param property The property to update
     * @param value The new value of the property as an RDFNode, or null to
     *              effectively remove this property.
     */
    public void setPropertyValue( Property property, RDFNode value ) {
        // if there is an existing property, remove it
        removeAll( property );

        // now set the new value
        addProperty( property, value );
    }


    /**
     * <p>
     * Remove any values for a given property from this resource.
     * </p>
     *
     * @param property The RDF resource that defines the property to be removed
     */
    public void removeAll( Property property ) {
        for (StmtIterator i = listProperties( property );  i.hasNext();  ) {
            i.next();
            i.remove();
        }
    }


    /**
     * <p>Set the RDF type property for this node in the underlying model, replacing any
     * existing <code>rdf:type</code> property.  
     * To add a second or subsequent type statement to a resource,
     * use {@link #setRDFType( Resource, boolean ) setRDFType( Resource, false ) }.
     * </p>
     * 
     * @param ontClass The RDF resource denoting the new value for the rdf:type property,
     *                 which will replace any existing type property.
     */
    public void setRDFType( Resource ontClass ) {
        setRDFType( ontClass, true );
    }


    /**
     * <p>
     * Add an RDF type property for this node in the underlying model. If the replace flag
     * is true, this type will replace any current type property for the node. Otherwise,
     * the type will be in addition to any existing type property.
     * </p>
     * 
     * @param ontClass The RDF resource denoting the class that will be the value 
     * for a new <code>rdf:type</code> property.
     * @param replace  If true, the given class will replace any existing 
     * <code>rdf:type</code> property for this
     *                 value, otherwise it will be added as an extra type statement.
     */
    public void setRDFType( Resource ontClass, boolean replace ) {
        // first remove any existing values, if required
        if (replace) {
            removeAll( RDF.type );
            
            Property typeAlias = (Property) getProfile().getAliasFor( RDF.type );
            if (typeAlias != null) {
                removeAll( typeAlias );
            }
        }
        
        
        addProperty( RDF.type, ontClass );
    }


    /**
     * <p>
     * Answer true if this DAML value is a member of the class denoted by the given URI.
     * </p>
     *
     * @param classURI String denoting the URI of the class to test against
     * @return True if it can be shown that this DAML value is a member of the class, via
     *         <code>rdf:type</code>.
     */
    public boolean hasRDFType( String classURI ) {
        return hasRDFType( getModel().getResource( classURI ) );
    }


    /**
     * <p>
     * Answer true if this ontology value is a member of the class denoted by the
     * given class resource.
     * </p>
     * 
     * @param ontClass Denotes a class to which this value may belong
     * @return True if <code><i>this</i> rdf:type <i>ontClass</i></code> is
     * a valid entailment in the model.
     */
    public boolean hasRDFType( Resource ontClass ) {
        return getModel().listStatements( this, RDF.type, ontClass ).hasNext() ||
               (getProfile().hasAliasFor( RDF.type ) && 
                getModel().listStatements( this, (Property) getProfile().getAliasFor( RDF.type), ontClass ).hasNext() );
    }


    /**
     * <p>
     * Answer an iterator over all of the RDF types to which this class belongs.
     * </p>
     *
     * @param closed TODO Not used in the current implementation  - fix
     * @return an iterator over the set of this ressource's classes
     */
    public Iterator getRDFTypes( boolean closed ) {
        Map1 mObject = new Map1() {  public Object map1( Object x ) { return ((Statement) x).getObject();  } };
        
        // make sure that we have an extneded iterator
        Iterator i = listProperties( RDF.type );
        ExtendedIterator ei = (i instanceof ExtendedIterator) ? (ExtendedIterator) i : WrappedIterator.create( i );
        
        // aliases to cope with?
        if (getProfile().hasAliasFor( RDF.type )) {
            ei = ei.andThen( WrappedIterator.create( listProperties( (Property) getProfile().getAliasFor( RDF.type ) ) ) );
        }
        
        // we only want the objects of the statements, and we only want one of each
        return new UniqueExtendedIterator( ei.mapWith( mObject ) );
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

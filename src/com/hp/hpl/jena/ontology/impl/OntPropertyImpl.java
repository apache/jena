/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            31-Mar-2003
 * Filename           $RCSfile: OntPropertyImpl.java,v $
 * Revision           $Revision: 1.10 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-06 11:07:02 $
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
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;



/**
 * <p>
 * Implementation of the abstraction representing a general ontology property.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntPropertyImpl.java,v 1.10 2003-06-06 11:07:02 ian_dickinson Exp $
 */
public class OntPropertyImpl
    extends OntResourceImpl
    implements OntProperty 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating OntProperty facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new OntPropertyImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to OntProperty");
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an OntProperty facet if it has rdf:type owl:Property or equivalent
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, OntProperty.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an ontology property represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public OntPropertyImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer true to indicate that this resource is an RDF property.
     * </p>
     * 
     * @return True.
     */
    public boolean isProperty() {
        return true;
    }
    
    
    /**
     * @see Property#getOrdinal()
     */
    public int getOrdinal() {
        return ((Property) as( Property.class )).getOrdinal();
    }
    
    
    // subPropertyOf
    
    /**
     * <p>Assert that this property is sub-property of the given property. Any existing 
     * statements for <code>subPropertyOf</code> will be removed.</p>
     * @param prop The property that this property is a sub-property of
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public void setSuperProperty( Property prop ) {
        setPropertyValue( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF", prop );
    }

    /**
     * <p>Add a super-property of this property.</p>
     * @param prop A property that is a super-property of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public void addSuperProperty( Property prop ) {
        addPropertyValue( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF", prop );
    }

    /**
     * <p>Answer a property that is the super-property of this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A super-property of this property
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public OntProperty getSuperProperty() {
        return objectAsProperty( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF" );
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be super-properties of
     * this property. Each elemeent of the iterator will be an {@link #OntProperty}.</p>
     * @return An iterator over the super-properties of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public Iterator listSuperProperties() {
        return listSuperProperties( false );
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be super-properties of
     * this property. Each elemeent of the iterator will be an {@link #OntProperty}.</p>
     * @param direct If true, only answer the direcly adjacent properties in the
     * property hierarchy: i&#046;e&#046; eliminate any property for which there is a longer route
     * to reach that child under the super-property relation.
     * @return An iterator over the super-properties of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public Iterator listSuperProperties( boolean direct ) {
        return listDirectPropertyValues( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF", OntProperty.class, direct, false );
    }

    /**
     * <p>Answer true if the given property is a super-property of this property.</p>
     * @param prop A property to test.
     * @param direct If true, only consider the direcly adjacent properties in the
     * property hierarchy
     * @return True if the given property is a super-property of this property.
     */
    public boolean hasSuperProperty( Property prop, boolean direct ) {
        return hasPropertyValue( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF", prop );
    }
    

    /**
     * <p>Assert that this property is super-property of the given property. Any existing 
     * statements for <code>subPropertyOf</code> on <code>prop</code> will be removed.</p>
     * @param prop The property that is a sub-property of this property
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public void setSubProperty( Property prop ) {
        // first we have to remove all of the inverse sub-prop links
        checkProfile( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF" );
        for (StmtIterator i = getModel().listStatements( null, getProfile().SUB_PROPERTY_OF(), this );  i.hasNext(); ) {
            i.nextStatement().remove();
        }
        
        ((OntProperty) prop.as( OntProperty.class )).addSuperProperty( this );
    }

    /**
     * <p>Add a sub-property of this property.</p>
     * @param prop A property that is a sub-property of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public void addSubProperty( Property prop ) {
        ((OntProperty) prop.as( OntProperty.class )).addSuperProperty( this );
    }

    /**
     * <p>Answer a property that is the sub-property of this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A sub-property of this property
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public OntProperty getSubProperty() {
        checkProfile( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF" );
        return (OntProperty) getModel().listStatements( null, getProfile().SUB_PROPERTY_OF(), this )
                             .nextStatement()
                             .getSubject()
                             .as( OntProperty.class );                  
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be sub-properties of
     * this property. Each element of the iterator will be an {@link #OntProperty}.</p>
     * @return An iterator over the sub-properties of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public Iterator listSubProperties() {
        return listSubProperties( false );
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be sub-properties of
     * this property. Each element of the iterator will be an {@link #OntProperty}.</p>
     * @param direct If true, only answer the direcly adjacent properties in the
     * property hierarchy: i&#046;e&#046; eliminate any property for which there is a longer route
     * to reach that child under the sub-property relation.
     * @return An iterator over the sub-properties of this property.
     * @exception OntProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.   
     */ 
    public Iterator listSubProperties( boolean direct ) {
        return listDirectPropertyValues( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF", OntProperty.class, direct, true );
    }

    /**
     * <p>Answer true if the given property is a sub-property of this property.</p>
     * @param prop A property to test.
     * @param direct If true, only consider the direcly adjacent properties in the
     * property hierarchy
     * @return True if the given property is a sub-property of this property.
     */
    public boolean hasSubProperty( Property prop, boolean direct ) {
        return ((OntProperty) prop.as( OntProperty.class )).hasSuperProperty( this, direct );
    }
    
    // domain
    
    /**
     * <p>Assert that the given resource represents the class of individuals that form the 
     * domain of this property. Any existing <code>domain</code> statements for this property are removed.</p>
     * @param res The resource that represents the domain class for this property.
     * @exception OntProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.   
     */ 
    public void setDomain( Resource res ) {
        setPropertyValue( getProfile().DOMAIN(), "DOMAIN", res );
    }

    /**
     * <p>Add a resource representing the domain of this property.</p>
     * @param res A resource that represents a domain class for this property. 
     * @exception OntProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.   
     */ 
    public void addDomain( Resource res ) {
        addPropertyValue( getProfile().DOMAIN(), "DOMAIN", res );
    }

    /**
     * <p>Answer a resource that represents the domain class of this property. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An resource representing the class that forms the domain of this property
     * @exception OntProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.   
     */ 
    public OntResource getDomain() {
        return objectAsResource( getProfile().DOMAIN(), "DOMAIN" );
    }

    /**
     * <p>Answer an iterator over all of the declared domain classes of this property.
     * Each elemeent of the iterator will be an {@link #OntResource}.</p>
     * @return An iterator over the classes that form the domain of this property.
     * @exception OntProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.   
     */ 
    public Iterator listDomain() {
        return listAs( getProfile().DOMAIN(), "DOMAIN", OntResource.class );
    }

    /**
     * <p>Answer true if the given resource a class specifying the domain of this property.</p>
     * @param res A resource representing a class
     * @return True if the given resource is one of the domain classes of this property.
     */
    public boolean hasDomain( Resource res ) {
        return hasPropertyValue( getProfile().DOMAIN(), "DOMAIN", res );
    }
    

    // range
    
    /**
     * <p>Assert that the given resource represents the class of individuals that form the 
     * range of this property. Any existing <code>range</code> statements for this property are removed.</p>
     * @param res The resource that represents the range class for this property.
     * @exception OntProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.   
     */ 
    public void setRange( Resource res ) {
        setPropertyValue( getProfile().RANGE(), "RANGE", res );
    }

    /**
     * <p>Add a resource representing the range of this property.</p>
     * @param res A resource that represents a range class for this property. 
     * @exception OntProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.   
     */ 
    public void addRange( Resource res ) {
        addPropertyValue( getProfile().RANGE(), "RANGE", res );
    }

    /**
     * <p>Answer a resource that represents the range class of this property. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An resource representing the class that forms the range of this property
     * @exception OntProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.   
     */ 
    public OntResource getRange() {
        return objectAsResource( getProfile().RANGE(), "RANGE" );
    }

    /**
     * <p>Answer an iterator over all of the declared range classes of this property.
     * Each elemeent of the iterator will be an {@link #OntResource}.</p>
     * @return An iterator over the classes that form the range of this property.
     * @exception OntProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.   
     */ 
    public Iterator listRange() {
        return listAs( getProfile().RANGE(), "RANGE", OntResource.class );
    }

    /**
     * <p>Answer true if the given resource a class specifying the range of this property.</p>
     * @param res A resource representing a class
     * @return True if the given resource is one of the range classes of this property.
     */
    public boolean hasRange( Resource res ) {
        return hasPropertyValue( getProfile().RANGE(), "RANGE", res );
    }
    

    // relationships between properties

    // equivalentProperty
    
    /**
     * <p>Assert that the given property is equivalent to this property. Any existing 
     * statements for <code>equivalentProperty</code> will be removed.</p>
     * @param prop The property that this property is a equivalent to.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.   
     */ 
    public void setEquivalentProperty( Property prop ) {
        setPropertyValue( getProfile().EQUIVALENT_PROPERTY(), "EQUIVALENT_PROPERTY", prop );
    }

    /**
     * <p>Add a property that is equivalent to this property.</p>
     * @param prop A property that is equivalent to this property.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.   
     */ 
    public void addEquivalentProperty( Property prop ) {
        addPropertyValue( getProfile().EQUIVALENT_PROPERTY(), "EQUIVALENT_PROPERTY", prop );
    }

    /**
     * <p>Answer a property that is equivalent to this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A property equivalent to this property
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.   
     */ 
    public OntProperty getEquivalentProperty() {
        return objectAsProperty( getProfile().EQUIVALENT_PROPERTY(), "EQUIVALENT_PROPERTY" );
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be equivalent properties to
     * this property. Each elemeent of the iterator will be an {@link #OntProperty}.</p>
     * @return An iterator over the properties equivalent to this property.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.   
     */ 
    public Iterator listEquivalentProperties() {
        return listAs( getProfile().EQUIVALENT_PROPERTY(), "EQUIVALENT_PROPERTY", OntProperty.class );
    }

    /**
     * <p>Answer true if the given property is equivalent to this property.</p>
     * @param prop A property to test for
     * @return True if the given property is equivalent to this property.
     */
    public boolean hasEquivalentProperty( Property prop ) {
        return hasPropertyValue( getProfile().EQUIVALENT_PROPERTY(), "EQUIVALENT_PROPERTY", prop );
    }
    
    // inverseProperty
    
    /**
     * <p>Assert that the given property is the inverse of this property. Any existing 
     * statements for <code>inverseOf</code> will be removed.</p>
     * @param prop The property that this property is a inverse to.
     * @exception OntProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.   
     */ 
    public void setInverseOf( Property prop ) {
        setPropertyValue( getProfile().INVERSE_OF(), "INVERSE_OF", prop );
    }

    /**
     * <p>Add a property that is the inverse of this property.</p>
     * @param prop A property that is the inverse of this property.
     * @exception OntProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.   
     */ 
    public void addInverseOf( Property prop ) {
        addPropertyValue( getProfile().INVERSE_OF(), "INVERSE_OF", prop );
    }

    /**
     * <p>Answer a property that is an inverse of this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A property inverse to this property
     * @exception OntProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.   
     */ 
    public OntProperty getInverseOf() {
        return objectAsProperty( getProfile().INVERSE_OF(), "INVERSE_OF" );
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be inverse properties of
     * this property. Each elemeent of the iterator will be an {@link #OntProperty}.</p>
     * @return An iterator over the properties inverse to this property.
     * @exception OntProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.   
     */ 
    public Iterator listInverseOf() {
        return listAs( getProfile().INVERSE_OF(), "INVERSE_OF", OntProperty.class );
    }

    /**
     * <p>Answer true if this property is the inverse of the given property.</p>
     * @param prop A property to test for
     * @return True if the this property is the inverse of the the given property.
     */
    public boolean isInverseOf( Property prop ) {
        return hasPropertyValue( getProfile().INVERSE_OF(), "INVERSE_OF", prop );
    }
    

    /** 
     * <p>Answer a view of this property as a functional property</p>
     * @return This property, but viewed as a FunctionalProperty node
     * @exception ConversionException if the resource cannot be converted to a functional property
     * given the lanuage profile and the current state of the underlying model.
     */
    public FunctionalProperty asFunctionalProperty() {
        return (FunctionalProperty) as( FunctionalProperty.class );
    }

    /** 
     * <p>Answer a view of this property as a datatype property</p>
     * @return This property, but viewed as a DatatypeProperty node
     * @exception ConversionException if the resource cannot be converted to a datatype property
     * given the lanuage profile and the current state of the underlying model.
     */
    public DatatypeProperty asDatatypeProperty() {
        return (DatatypeProperty) as( DatatypeProperty.class );
    }

    /** 
     * <p>Answer a view of this property as an object property</p>
     * @return This property, but viewed as an ObjectProperty node
     * @exception ConversionException if the resource cannot be converted to an object property
     * given the lanuage profile and the current state of the underlying model.
     */
    public ObjectProperty asObjectProperty() {
        return (ObjectProperty) as( ObjectProperty.class );
    }
    
    /** 
     * <p>Answer a view of this property as a transitive property</p>
     * @return This property, but viewed as a TransitiveProperty node
     * @exception ConversionException if the resource cannot be converted to a transitive property
     * given the lanuage profile and the current state of the underlying model.
     */
    public TransitiveProperty asTransitiveProperty() {
        return (TransitiveProperty) as( TransitiveProperty.class );
    }
    
    /** 
     * <p>Answer a view of this property as an inverse functional property</p>
     * @return This property, but viewed as an InverseFunctionalProperty node
     * @exception ConversionException if the resource cannot be converted to an inverse functional property
     * given the lanuage profile and the current state of the underlying model.
     */
    public InverseFunctionalProperty asInverseFunctionalProperty() {
        return (InverseFunctionalProperty) as( InverseFunctionalProperty.class );
    }
    
    /** 
     * <p>Answer a view of this property as a symmetric property</p>
     * @return This property, but viewed as a SymmetricProperty node
     * @exception ConversionException if the resource cannot be converted to a symmetric property
     * given the lanuage profile and the current state of the underlying model.
     */
    public SymmetricProperty asSymmetricProperty() {
        return (SymmetricProperty) as( SymmetricProperty.class );
    }

    // conversion functions
    
    /** 
     * <p>Answer a facet of this property as a functional property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a FunctionalProperty facet
     */
    public FunctionalProperty convertToFunctionalProperty() {
        return (FunctionalProperty) convertToType( getProfile().FUNCTIONAL_PROPERTY(), "FUNCTIONAL_PROPERTY", FunctionalProperty.class );
    }

    /** 
     * <p>Answer a facet of this property as a datatype property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a DatatypeProperty facet
     */
    public DatatypeProperty convertToDatatypeProperty() {
        return (DatatypeProperty) convertToType( getProfile().DATATYPE_PROPERTY(), "DATATYPE_PROPERTY", DatatypeProperty.class );
    }

    /** 
     * <p>Answer a facet of this property as an object property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to an ObjectProperty facet
     */
    public ObjectProperty convertToObjectProperty() {
        return (ObjectProperty) convertToType( getProfile().OBJECT_PROPERTY(), "OBJECT_PROPERTY", ObjectProperty.class );
    }
    
    /** 
     * <p>Answer a facet of this property as a transitive property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a TransitiveProperty facet
     */
    public TransitiveProperty convertToTransitiveProperty() {
        return (TransitiveProperty) convertToType( getProfile().TRANSITIVE_PROPERTY(), "TRANSITIVE_PROPERTY", TransitiveProperty.class );
    }
    
    /** 
     * <p>Answer a facet of this property as an inverse functional property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to an InverseFunctionalProperty facet
     */
    public InverseFunctionalProperty convertToInverseFunctionalProperty() {
        return (InverseFunctionalProperty) convertToType( getProfile().INVERSE_FUNCTIONAL_PROPERTY(), "INVERSE_FUNCTIONAL_PROPERTY", InverseFunctionalProperty.class );
    }
    
    /** 
     * <p>Answer a facet of this property as a symmetric property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a SymmetricProperty facet
     */
    public SymmetricProperty convertToSymmetricProperty() {
        return (SymmetricProperty) convertToType( getProfile().SYMMETRIC_PROPERTY(), "SYMMETRIC_PROPERTY", SymmetricProperty.class );
    }
    
    
    // tests on property sub-types
    
    /** 
     * <p>Answer true if this property is a functional property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a functional property.
     */
    public boolean isFunctionalProperty() {
        return hasRDFType( getProfile().FUNCTIONAL_PROPERTY(), "FUNCTIONAL_PROPERTY" );
    }

    /** 
     * <p>Answer true if this property is a datatype property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a datatype property.
     */
    public boolean isDatatypeProperty() {
        return hasRDFType( getProfile().DATATYPE_PROPERTY(), "DATATYPE_PROPERTY" );
    }

    /** 
     * <p>Answer true if this property is an object property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as an object property.
     */
    public boolean isObjectProperty() {
        return hasRDFType( getProfile().OBJECT_PROPERTY(), "OBJECT_PROPERTY" );
    }
    
    /** 
     * <p>Answer true if this property is a transitive property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a transitive property.
     */
    public boolean isTransitiveProperty() {
        return hasRDFType( getProfile().TRANSITIVE_PROPERTY(), "TRANSITIVE_PROPERTY" );
    }
    
    /** 
     * <p>Answer true if this property is an inverse functional property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as an inverse functional property.
     */
    public boolean isInverseFunctionalProperty() {
        return hasRDFType( getProfile().INVERSE_FUNCTIONAL_PROPERTY(), "INVERSE_FUNCTIONAL_PROPERTY" );
    }
    
    /** 
     * <p>Answer true if this property is a symmetric property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a symmetric property.
     */
    public boolean isSymmetricProperty() {
        return hasRDFType( getProfile().SYMMETRIC_PROPERTY(), "SYMMETRIC_PROPERTY" );
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


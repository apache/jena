/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;



/**
 * <p>
 * Implementation of the abstraction representing a general ontology property.
 * </p>
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
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new OntPropertyImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to OntProperty");
            }
        }

        @Override
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
    @Override
    public boolean isProperty() {
        return true;
    }


    /**
     * @see Property#getOrdinal()
     */
    @Override
    public int getOrdinal() {
        return (as( Property.class )).getOrdinal();
    }


    // subPropertyOf

    /**
     * <p>Assert that this property is sub-property of the given property. Any existing
     * statements for <code>subPropertyOf</code> will be removed.</p>
     * @param prop The property that this property is a sub-property of
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public void setSuperProperty( Property prop ) {
        setPropertyValue( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF", prop );
    }

    /**
     * <p>Add a super-property of this property.</p>
     * @param prop A property that is a super-property of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public void addSuperProperty( Property prop ) {
        addPropertyValue( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF", prop );
    }

    /**
     * <p>Answer a property that is the super-property of this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A super-property of this property
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public OntProperty getSuperProperty() {
        return objectAsProperty( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF" );
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be super-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the super-properties of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntProperty> listSuperProperties() {
        return listSuperProperties( false );
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be super-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @param direct If true, only answer the directly adjacent properties in the
     * property hierarchy: i&#046;e&#046; eliminate any property for which there is a longer route
     * to reach that child under the super-property relation.
     * @return An iterator over the super-properties of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntProperty> listSuperProperties( boolean direct ) {
        return listDirectPropertyValues( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF", OntProperty.class, getProfile().SUB_PROPERTY_OF(), direct, false )
                        .filterDrop( new SingleEqualityFilter<OntProperty>( this ) );
    }

    /**
     * <p>Answer true if the given property is a super-property of this property.</p>
     * @param prop A property to test.
     * @param direct If true, only consider the directly adjacent properties in the
     * property hierarchy
     * @return True if the given property is a super-property of this property.
     */
    @Override
    public boolean hasSuperProperty( Property prop, boolean direct ) {
        return hasPropertyValue( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF", prop );
    }

    /**
     * <p>Remove the given property from the super-properties of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property to be removed from the super-properties of this property
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public void removeSuperProperty( Property prop ) {
        removePropertyValue( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF", prop );
    }


    /**
     * <p>Assert that this property is super-property of the given property. Any existing
     * statements for <code>subPropertyOf</code> on <code>prop</code> will be removed.</p>
     * @param prop The property that is a sub-property of this property
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public void setSubProperty( Property prop ) {
        // first we have to remove all of the inverse sub-prop links
        checkProfile( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF" );
        for (StmtIterator i = getModel().listStatements( null, getProfile().SUB_PROPERTY_OF(), this );  i.hasNext(); ) {
            i.removeNext();
        }

        prop.as( OntProperty.class ).addSuperProperty( this );
    }

    /**
     * <p>Add a sub-property of this property.</p>
     * @param prop A property that is a sub-property of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public void addSubProperty( Property prop ) {
        prop.as( OntProperty.class ).addSuperProperty( this );
    }

    /**
     * <p>Answer a property that is the sub-property of this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A sub-property of this property
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public OntProperty getSubProperty() {
        checkProfile( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF" );
        return getModel().listStatements( null, getProfile().SUB_PROPERTY_OF(), this )
                             .nextStatement()
                             .getSubject()
                             .as( OntProperty.class );
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be sub-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the sub-properties of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntProperty> listSubProperties() {
        return listSubProperties( false );
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be sub-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @param direct If true, only answer the direcly adjacent properties in the
     * property hierarchy: i&#046;e&#046; eliminate any property for which there is a longer route
     * to reach that child under the sub-property relation.
     * @return An iterator over the sub-properties of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntProperty> listSubProperties( boolean direct ) {
        return listDirectPropertyValues( getProfile().SUB_PROPERTY_OF(), "SUB_PROPERTY_OF", OntProperty.class, getProfile().SUB_PROPERTY_OF(), direct, true );
    }

    /**
     * <p>Answer true if the given property is a sub-property of this property.</p>
     * @param prop A property to test.
     * @param direct If true, only consider the direcly adjacent properties in the
     * property hierarchy
     * @return True if the given property is a sub-property of this property.
     */
    @Override
    public boolean hasSubProperty( Property prop, boolean direct ) {
        return prop.as( OntProperty.class ).hasSuperProperty( this, direct );
    }

    /**
     * <p>Remove the given property from the sub-properties of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property to be removed from the sub-properties of this property
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    @Override
    public void removeSubProperty( Property prop ) {
        prop.as( OntProperty.class ).removeSuperProperty( this );
    }

    // domain

    /**
     * <p>Assert that the given resource represents the class of individuals that form the
     * domain of this property. Any existing <code>domain</code> statements for this property are removed.</p>
     * @param res The resource that represents the domain class for this property.
     * @exception ProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.
     */
    @Override
    public void setDomain( Resource res ) {
        setPropertyValue( getProfile().DOMAIN(), "DOMAIN", res );
    }

    /**
     * <p>Add a resource representing the domain of this property.</p>
     * @param res A resource that represents a domain class for this property.
     * @exception ProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.
     */
    @Override
    public void addDomain( Resource res ) {
        addPropertyValue( getProfile().DOMAIN(), "DOMAIN", res );
    }

    /**
     * <p>Answer a resource that represents the domain class of this property. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An resource representing the class that forms the domain of this property
     * @exception ProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.
     */
    @Override
    public OntResource getDomain() {
        return objectAsResource( getProfile().DOMAIN(), "DOMAIN" );
    }

    /**
     * <p>Answer an iterator over all of the declared domain classes of this property.
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the classes that form the domain of this property.
     * @exception ProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntClass> listDomain() {
        return listAs( getProfile().DOMAIN(), "DOMAIN", OntClass.class );
    }

    /**
     * <p>Answer true if the given resource a class specifying the domain of this property.</p>
     * @param res A resource representing a class
     * @return True if the given resource is one of the domain classes of this property.
     */
    @Override
    public boolean hasDomain( Resource res ) {
        return hasPropertyValue( getProfile().DOMAIN(), "DOMAIN", res );
    }

    /**
     * <p>Remove the given class from the stated domain(s) of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class to be removed from the declared domain(s) of this property
     * @exception ProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.
     */
    @Override
    public void removeDomain( Resource cls ) {
        removePropertyValue( getProfile().DOMAIN(), "DOMAIN", cls );
    }


    // range

    /**
     * <p>Assert that the given resource represents the class of individuals that form the
     * range of this property. Any existing <code>range</code> statements for this property are removed.</p>
     * @param res The resource that represents the range class for this property.
     * @exception ProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.
     */
    @Override
    public void setRange( Resource res ) {
        setPropertyValue( getProfile().RANGE(), "RANGE", res );
    }

    /**
     * <p>Add a resource representing the range of this property.</p>
     * @param res A resource that represents a range class for this property.
     * @exception ProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.
     */
    @Override
    public void addRange( Resource res ) {
        addPropertyValue( getProfile().RANGE(), "RANGE", res );
    }

    /**
     * <p>Answer a resource that represents the range class of this property. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An resource representing the class that forms the range of this property
     * @exception ProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.
     */
    @Override
    public OntResource getRange() {
        return objectAsResource( getProfile().RANGE(), "RANGE" );
    }

    /**
     * <p>Answer an iterator over all of the declared range classes of this property.
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the classes that form the range of this property.
     * @exception ProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntClass> listRange() {
        return listAs( getProfile().RANGE(), "RANGE", OntClass.class );
    }

    /**
     * <p>Answer true if the given resource a class specifying the range of this property.</p>
     * @param res A resource representing a class
     * @return True if the given resource is one of the range classes of this property.
     */
    @Override
    public boolean hasRange( Resource res ) {
        return hasPropertyValue( getProfile().RANGE(), "RANGE", res );
    }

    /**
     * <p>Remove the given class from the stated range(s) of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class to be removed from the declared range(s) of this property
     * @exception ProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.
     */
    @Override
    public void removeRange( Resource cls ) {
        removePropertyValue( getProfile().RANGE(), "RANGE", cls );
    }


    // relationships between properties

    // equivalentProperty

    /**
     * <p>Assert that the given property is equivalent to this property. Any existing
     * statements for <code>equivalentProperty</code> will be removed.</p>
     * @param prop The property that this property is a equivalent to.
     * @exception ProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.
     */
    @Override
    public void setEquivalentProperty( Property prop ) {
        setPropertyValue( getProfile().EQUIVALENT_PROPERTY(), "EQUIVALENT_PROPERTY", prop );
    }

    /**
     * <p>Add a property that is equivalent to this property.</p>
     * @param prop A property that is equivalent to this property.
     * @exception ProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.
     */
    @Override
    public void addEquivalentProperty( Property prop ) {
        addPropertyValue( getProfile().EQUIVALENT_PROPERTY(), "EQUIVALENT_PROPERTY", prop );
    }

    /**
     * <p>Answer a property that is equivalent to this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A property equivalent to this property
     * @exception ProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.
     */
    @Override
    public OntProperty getEquivalentProperty() {
        return objectAsProperty( getProfile().EQUIVALENT_PROPERTY(), "EQUIVALENT_PROPERTY" );
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be equivalent properties to
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the properties equivalent to this property.
     * @exception ProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntProperty> listEquivalentProperties() {
        return listAs( getProfile().EQUIVALENT_PROPERTY(), "EQUIVALENT_PROPERTY", OntProperty.class );
    }

    /**
     * <p>Answer true if the given property is equivalent to this property.</p>
     * @param prop A property to test for
     * @return True if the given property is equivalent to this property.
     */
    @Override
    public boolean hasEquivalentProperty( Property prop ) {
        return hasPropertyValue( getProfile().EQUIVALENT_PROPERTY(), "EQUIVALENT_PROPERTY", prop );
    }

    /**
     * <p>Remove the statement that this property and the given property are
     * equivalent.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property that may be declared to be equivalent to this property
     * @exception ProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.
     */
    @Override
    public void removeEquivalentProperty( Property prop ) {
        removePropertyValue( getProfile().EQUIVALENT_PROPERTY(), "EQUIVALENT_PROPERTY", prop  );
    }

    // inverseProperty

    /**
     * <p>Assert that the given property is the inverse of this property. Any existing
     * statements for <code>inverseOf</code> will be removed.</p>
     * @param prop The property that this property is a inverse to.
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    @Override
    public void setInverseOf( Property prop ) {
        setPropertyValue( getProfile().INVERSE_OF(), "INVERSE_OF", prop );
    }

    /**
     * <p>Add a property that is the inverse of this property.</p>
     * @param prop A property that is the inverse of this property.
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    @Override
    public void addInverseOf( Property prop ) {
        addPropertyValue( getProfile().INVERSE_OF(), "INVERSE_OF", prop );
    }

    /**
     * <p>Answer a property that is an inverse of this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A property inverse to this property
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    @Override
    public OntProperty getInverseOf() {
        return objectAsProperty( getProfile().INVERSE_OF(), "INVERSE_OF" );
    }

    /**
     * <p>Answer an iterator over all of the properties that are declared to be inverse properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the properties inverse to this property.
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<? extends OntProperty> listInverseOf() {
        return listAs( getProfile().INVERSE_OF(), "INVERSE_OF", OntProperty.class );
    }

    /**
     * <p>Answer true if this property is the inverse of the given property.</p>
     * @param prop A property to test for
     * @return True if the this property is the inverse of the the given property.
     */
    @Override
    public boolean isInverseOf( Property prop ) {
        return hasPropertyValue( getProfile().INVERSE_OF(), "INVERSE_OF", prop );
    }

    /**
     * <p>Remove the statement that this property is the inverse of the given property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property that may be declared to be inverse to this property
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    @Override
    public void removeInverseProperty( Property prop ) {
        removePropertyValue( getProfile().INVERSE_OF(), "INVERSE_OF", prop );
    }


    /**
     * <p>Answer a view of this property as a functional property</p>
     * @return This property, but viewed as a FunctionalProperty node
     * @exception ConversionException if the resource cannot be converted to a functional property
     * given the language profile and the current state of the underlying model.
     */
    @Override
    public FunctionalProperty asFunctionalProperty() {
        return as( FunctionalProperty.class );
    }

    /**
     * <p>Answer a view of this property as a datatype property</p>
     * @return This property, but viewed as a DatatypeProperty node
     * @exception ConversionException if the resource cannot be converted to a datatype property
     * given the language profile and the current state of the underlying model.
     */
    @Override
    public DatatypeProperty asDatatypeProperty() {
        return as( DatatypeProperty.class );
    }

    /**
     * <p>Answer a view of this property as an object property</p>
     * @return This property, but viewed as an ObjectProperty node
     * @exception ConversionException if the resource cannot be converted to an object property
     * given the language profile and the current state of the underlying model.
     */
    @Override
    public ObjectProperty asObjectProperty() {
        return as( ObjectProperty.class );
    }

    /**
     * <p>Answer a view of this property as a transitive property</p>
     * @return This property, but viewed as a TransitiveProperty node
     * @exception ConversionException if the resource cannot be converted to a transitive property
     * given the language profile and the current state of the underlying model.
     */
    @Override
    public TransitiveProperty asTransitiveProperty() {
        return as( TransitiveProperty.class );
    }

    /**
     * <p>Answer a view of this property as an inverse functional property</p>
     * @return This property, but viewed as an InverseFunctionalProperty node
     * @exception ConversionException if the resource cannot be converted to an inverse functional property
     * given the language profile and the current state of the underlying model.
     */
    @Override
    public InverseFunctionalProperty asInverseFunctionalProperty() {
        return as( InverseFunctionalProperty.class );
    }

    /**
     * <p>Answer a view of this property as a symmetric property</p>
     * @return This property, but viewed as a SymmetricProperty node
     * @exception ConversionException if the resource cannot be converted to a symmetric property
     * given the language profile and the current state of the underlying model.
     */
    @Override
    public SymmetricProperty asSymmetricProperty() {
        return as( SymmetricProperty.class );
    }

    // conversion functions

    /**
     * <p>Answer a facet of this property as a functional property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a FunctionalProperty facet
     */
    @Override
    public FunctionalProperty convertToFunctionalProperty() {
        return convertToType( getProfile().FUNCTIONAL_PROPERTY(), "FUNCTIONAL_PROPERTY", FunctionalProperty.class );
    }

    /**
     * <p>Answer a facet of this property as a datatype property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a DatatypeProperty facet
     */
    @Override
    public DatatypeProperty convertToDatatypeProperty() {
        return convertToType( getProfile().DATATYPE_PROPERTY(), "DATATYPE_PROPERTY", DatatypeProperty.class );
    }

    /**
     * <p>Answer a facet of this property as an object property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to an ObjectProperty facet
     */
    @Override
    public ObjectProperty convertToObjectProperty() {
        return convertToType( getProfile().OBJECT_PROPERTY(), "OBJECT_PROPERTY", ObjectProperty.class );
    }

    /**
     * <p>Answer a facet of this property as a transitive property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a TransitiveProperty facet
     */
    @Override
    public TransitiveProperty convertToTransitiveProperty() {
        return convertToType( getProfile().TRANSITIVE_PROPERTY(), "TRANSITIVE_PROPERTY", TransitiveProperty.class );
    }

    /**
     * <p>Answer a facet of this property as an inverse functional property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to an InverseFunctionalProperty facet
     */
    @Override
    public InverseFunctionalProperty convertToInverseFunctionalProperty() {
        return convertToType( getProfile().INVERSE_FUNCTIONAL_PROPERTY(), "INVERSE_FUNCTIONAL_PROPERTY", InverseFunctionalProperty.class );
    }

    /**
     * <p>Answer a facet of this property as a symmetric property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a SymmetricProperty facet
     */
    @Override
    public SymmetricProperty convertToSymmetricProperty() {
        return convertToType( getProfile().SYMMETRIC_PROPERTY(), "SYMMETRIC_PROPERTY", SymmetricProperty.class );
    }


    // tests on property sub-types

    /**
     * <p>Answer true if this property is a functional property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a functional property.
     */
    @Override
    public boolean isFunctionalProperty() {
        return hasRDFType( getProfile().FUNCTIONAL_PROPERTY(), "FUNCTIONAL_PROPERTY", false );
    }

    /**
     * <p>Answer true if this property is a datatype property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a datatype property.
     */
    @Override
    public boolean isDatatypeProperty() {
        return hasRDFType( getProfile().DATATYPE_PROPERTY(), "DATATYPE_PROPERTY", false );
    }

    /**
     * <p>Answer true if this property is an object property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as an object property.
     */
    @Override
    public boolean isObjectProperty() {
        return hasRDFType( getProfile().OBJECT_PROPERTY(), "OBJECT_PROPERTY", false );
    }

    /**
     * <p>Answer true if this property is a transitive property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a transitive property.
     */
    @Override
    public boolean isTransitiveProperty() {
        return hasRDFType( getProfile().TRANSITIVE_PROPERTY(), "TRANSITIVE_PROPERTY", false );
    }

    /**
     * <p>Answer true if this property is an inverse functional property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as an inverse functional property.
     */
    @Override
    public boolean isInverseFunctionalProperty() {
        return hasRDFType( getProfile().INVERSE_FUNCTIONAL_PROPERTY(), "INVERSE_FUNCTIONAL_PROPERTY", false );
    }

    /**
     * <p>Answer true if this property is a symmetric property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a symmetric property.
     */
    @Override
    public boolean isSymmetricProperty() {
        return hasRDFType( getProfile().SYMMETRIC_PROPERTY(), "SYMMETRIC_PROPERTY", false );
    }


    /**
     * <p>Answer the property that is the inverse of this property.  If no such property is defined,
     * return null.  If more than one inverse is defined, return an abritrary selection.</p>
     * @return The property that is the inverse of this property, or null.
     */
    @Override
    public OntProperty getInverse() {
        ExtendedIterator<OntProperty> i = listInverse();
        OntProperty p = i.hasNext() ? i.next() : null;
        i.close();

        return p;
    }

    /**
     * <p>Answer an iterator over the properties that are defined to be inverses of this property.</p>
     * @return An iterator over the properties that declare themselves the <code>inverseOf</code> this property.
     */
    @Override
    public ExtendedIterator<OntProperty> listInverse() {
        return getModel().listStatements( null, getProfile().INVERSE_OF(), this ).mapWith( new SubjectAsMapper<>( OntProperty.class ) );
    }

    /**
     * <p>Answer true if there is at least one inverse property for this property.</p>
     * @return True if property has an inverse.
     */
    @Override
    public boolean hasInverse() {
        ExtendedIterator<OntProperty> i = listInverse();
        boolean hasInv = i.hasNext();
        i.close();

        return hasInv;
    }


    /**
     * <p>Answer an iterator of all of the classes in this ontology, such
     * that each returned class has this property as one of its
     * properties in {@link OntClass#listDeclaredProperties()}. This
     * simulates a frame-like view of properties and classes; for more
     * details see the <a href="../../../../../../how-to/rdf-frames.html">
     * RDF frames howto</a>.</p>
     * @return An iterator of the classes having this property as one
     * of their declared properties
     */
    @Override
    public ExtendedIterator<OntClass> listDeclaringClasses() {
        return listDeclaringClasses( false );
    }

    /**
     * <p>Answer an iterator of all of the classes in this ontology, such
     * that each returned class has this property as one of its
     * properties in {@link OntClass#listDeclaredProperties(boolean)}. This
     * simulates a frame-like view of properties and classes; for more
     * details see the <a href="../../../../../../how-to/rdf-frames.html">
     * RDF frames howto</a>.</p>
     * @param direct If true, use only </em>direct</em> associations between classes
     * and properties
     * @return An iterator of the classes having this property as one
     * of their declared properties
     */
    @Override
    public ExtendedIterator<OntClass> listDeclaringClasses( boolean direct ) {
        // first list the candidate classes, which will also help us
        // work out whether this is a "global" property or not
        Set<OntClass> cands = new HashSet<>();
        for (Iterator<OntClass> i = listDomain(); i.hasNext(); ) {
            // the candidates include this class and it sub-classes
            List<OntClass> q = new ArrayList<>();
            q.add( i.next() );

            while (!q.isEmpty()) {
                OntClass c = q.remove( 0 );

                if (!c.isOntLanguageTerm() && !cands.contains( c )) {
                    // a new value that is not just a term from OWL or RDFS
                    cands.add( c );
                    for (Iterator<OntClass> j = c.listSubClasses(); j.hasNext(); ) {
                        q.add( j.next() );
                    }
                }
            }
        }

        if (cands.isEmpty()) {
            // no declared non-global domain, so this is a global prop
            if (!direct) {
                // in the non-direct case, global properties appear in the ldp
                // of all classes, but we ignore the built-in classes
                return ((OntModel) getModel()).listClasses()
                                              .filterDrop( new Filter<OntClass>() {
                                                @Override
                                                public boolean accept( OntClass c ) {
                                                    return c.isOntLanguageTerm();
                                                }} );
            }
            else {
                // in the direct case, global properties only attach to the
                // local hierarchy roots
                return ((OntModel) getModel()).listHierarchyRootClasses();
            }
        }
        else {
            // not a global property
            // pick out classes from the domain for which this is a declared prop
            return WrappedIterator.create( cands.iterator() )
                                  .filterKeep( new FilterDeclaringClass( this, direct ));
        }
    }


    /**
     * <p>Answer an iterator over any restrictions that mention this property as
     * the property that the restriction is adding some constraint to. For example:</p>
     * <code><pre>&lt;owl:Restriction&gt;
     *     &lt;owl:onProperty rdf:resource=&quot;#childOf&quot; /&gt;
     *     &lt;owl:hasValue rdf:resource=&quot;#ian&quot; /&gt;
     * &lt;/owl:Restriction&gt;</pre></code>
     * <p><strong>Note</strong> that any such restrictions do not affect the global
     * semantics of this property itself. Restrictions define new class expressions,
     * and the property constraints are local to that class expression. This method
     * is provided as a convenience to assist callers to navigate the relationships
     * in the model.</p>
     * @return An iterator whose values are the restrictions from the local
     * model that reference this property.
     */
    @Override
    public ExtendedIterator<Restriction> listReferringRestrictions() {
        return getModel().listStatements( null, getProfile().ON_PROPERTY(), this )
                         .mapWith( new SubjectAsMapper<>( Restriction.class ) );
    }



    // Internal implementation methods
    //////////////////////////////////

    /**
     * <p>Answer a property that is attached to the given model, which will either
     * be this property or a new property object with the same URI in the given
     * model. If the given model is an ontology model, make the new property object
     * an ontproperty.</p>
     * @param m A model
     * @return A property equal to this property that is attached to m.
     */
    @Override public Property inModel( Model m ) {
        return (getModel() == m) ? this : m.createProperty( getURI() );
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /**
     * <p>Filter that accepts classes which have the given property as one of
     * their declared properties.</p>
     */
    private class FilterDeclaringClass extends Filter<OntClass>
    {
        private boolean m_direct;
        private Property m_prop;

        private FilterDeclaringClass( Property prop, boolean direct ) {
            m_prop = prop;
            m_direct = direct;
        }

        @Override public boolean accept( OntClass o ) {
            return o.hasDeclaredProperty( m_prop, m_direct );
        }

    }
}

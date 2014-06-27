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
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;



/**
 * <p>
 * Interface encapsulating a property in an ontology. This is an extension to the
 * standard {@link Property} interface, adding a collection of convenience methods
 * for accessing the additional semantic features of properties in OWL and RDFS
 * such as domain, range, inverse, etc.  Not all such capabilities exist in all
 * supported ontology languages.
 * </p>
 */
public interface OntProperty
    extends OntResource, Property
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    // subPropertyOf

    /**
     * <p>Assert that this property is sub-property of the given property. Any existing
     * statements for <code>subPropertyOf</code> will be removed.</p>
     * @param prop The property that this property is a sub-property of
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public void setSuperProperty( Property prop );

    /**
     * <p>Add a super-property of this property.</p>
     * @param prop A property that is a super-property of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public void addSuperProperty( Property prop );

    /**
     * <p>Answer a property that is the super-property of this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A super-property of this property
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public OntProperty getSuperProperty();

    /**
     * <p>Answer an iterator over all of the properties that are declared to be super-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the super-properties of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends OntProperty> listSuperProperties();

    /**
     * <p>Answer an iterator over all of the properties that are declared to be super-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @param direct If true, only answer the directly adjacent properties in the
     * property hierarchy: i&#046;e&#046; eliminate any property for which there is a longer route
     * to reach that child under the super-property relation.
     * @return An iterator over the super-properties of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends OntProperty> listSuperProperties( boolean direct );

    /**
     * <p>Answer true if the given property is a super-property of this property.</p>
     * @param prop A property to test.
     * @param direct If true, only consider the directly adjacent properties in the
     * property hierarchy
     * @return True if the given property is a super-property of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public boolean hasSuperProperty( Property prop, boolean direct );

    /**
     * <p>Remove the given property from the super-properties of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property to be removed from the super-properties of this property
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public void removeSuperProperty( Property prop );

    /**
     * <p>Assert that this property is super-property of the given property. Any existing
     * statements for <code>subPropertyOf</code> on <code>prop</code> will be removed.</p>
     * @param prop The property that is a sub-property of this property
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public void setSubProperty( Property prop );

    /**
     * <p>Add a sub-property of this property.</p>
     * @param prop A property that is a sub-property of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public void addSubProperty( Property prop );

    /**
     * <p>Answer a property that is the sub-property of this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A sub-property of this property
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public OntProperty getSubProperty();

    /**
     * <p>Answer an iterator over all of the properties that are declared to be sub-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the sub-properties of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends OntProperty> listSubProperties();

    /**
     * <p>Answer an iterator over all of the properties that are declared to be sub-properties of
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @param direct If true, only answer the directly adjacent properties in the
     * property hierarchy: i&#046;e&#046; eliminate any property for which there is a longer route
     * to reach that child under the sub-property relation.
     * @return An iterator over the sub-properties of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends OntProperty> listSubProperties( boolean direct );

    /**
     * <p>Answer true if the given property is a sub-property of this property.</p>
     * @param prop A property to test.
     * @param direct If true, only consider the directly adjacent properties in the
     * property hierarchy
     * @return True if the given property is a sub-property of this property.
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public boolean hasSubProperty( Property prop, boolean direct );

    /**
     * <p>Remove the given property from the sub-properties of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property to be removed from the sub-properties of this property
     * @exception ProfileException If the {@link Profile#SUB_PROPERTY_OF()} property is not supported in the current language profile.
     */
    public void removeSubProperty( Property prop );

    // domain

    /**
     * <p>Assert that the given resource represents the class of individuals that form the
     * domain of this property. Any existing <code>domain</code> statements for this property are removed.</p>
     * @param res The resource that represents the domain class for this property.
     * @exception ProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.
     */
    public void setDomain( Resource res );

    /**
     * <p>Add a resource representing the domain of this property.</p>
     * @param res A resource that represents a domain class for this property.
     * @exception ProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.
     */
    public void addDomain( Resource res );

    /**
     * <p>Answer a resource that represents the domain class of this property. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An resource representing the class that forms the domain of this property
     * @exception ProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.
     */
    public OntResource getDomain();

    /**
     * <p>Answer an iterator over all of the declared domain classes of this property.
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the classes that form the domain of this property.
     * @exception ProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends OntResource> listDomain();

    /**
     * <p>Answer true if the given resource a class specifying the domain of this property.</p>
     * @param res A resource representing a class
     * @return True if the given resource is one of the domain classes of this property.
     * @exception ProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.
     */
    public boolean hasDomain( Resource res );

    /**
     * <p>Remove the given class from the stated domain(s) of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class to be removed from the declared domain(s) of this property
     * @exception ProfileException If the {@link Profile#DOMAIN()} property is not supported in the current language profile.
     */
    public void removeDomain( Resource cls );


    // range

    /**
     * <p>Assert that the given resource represents the class of individuals that form the
     * range of this property. Any existing <code>range</code> statements for this property are
     * first removed. Therefore, if the property is known not to have a range declaration, it
     * is more efficient to use {@link #addRange} since no removal step is necessary.
     * See {@link #addRange} for additional usage notes on the value of <code>res</code>.</p>
     * @param res The resource that represents the range class for this property.
     * @exception ProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.
     */
    public void setRange( Resource res );

    /**
     * <p>Add a resource representing the range of this property. The resource denotes the class
     * or datatype that objects of statements using this property as predicate should
     * belong to. For datatype properties, XML Schema Datatype names are pre-declared
     * as resources in the {@link com.hp.hpl.jena.vocabulary.XSD XSD} vocabulary class. For object properties,
     * the resource should be represent the range class. Note that {@link OntClass} is
     * a Java sub-class of {@link Resource}, so OntClass objects can be passed directly.</p>
     * @param res A resource that represents a range class for this property.
     * @exception ProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.
     */
    public void addRange( Resource res );

    /**
     * <p>Answer a resource that represents the range class of this property. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An resource representing the class that forms the range of this property
     * @exception ProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.
     */
    public OntResource getRange();

    /**
     * <p>Answer an iterator over all of the declared range classes of this property.
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the classes that form the range of this property.
     * @exception ProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends OntResource> listRange();

    /**
     * <p>Answer true if the given resource a class specifying the range of this property.</p>
     * @param res A resource representing a class
     * @return True if the given resource is one of the range classes of this property.
     * @exception ProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.
     */
    public boolean hasRange( Resource res );

    /**
     * <p>Remove the given class from the stated range(s) of this property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class to be removed from the declared range(s) of this property
     * @exception ProfileException If the {@link Profile#RANGE()} property is not supported in the current language profile.
     */
    public void removeRange( Resource cls );


    // relationships between properties

    // equivalentProperty

    /**
     * <p>Assert that the given property is equivalent to this property. Any existing
     * statements for <code>equivalentProperty</code> will be removed.</p>
     * @param prop The property that this property is a equivalent to.
     * @exception ProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.
     */
    public void setEquivalentProperty( Property prop );

    /**
     * <p>Add a property that is equivalent to this property.</p>
     * @param prop A property that is equivalent to this property.
     * @exception ProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.
     */
    public void addEquivalentProperty( Property prop );

    /**
     * <p>Answer a property that is equivalent to this property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * @return A property equivalent to this property
     * @exception ProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.
     */
    public OntProperty getEquivalentProperty();

    /**
     * <p>Answer an iterator over all of the properties that are declared to be equivalent properties to
     * this property. Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the properties equivalent to this property.
     * @exception ProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends OntProperty> listEquivalentProperties();

    /**
     * <p>Answer true if the given property is equivalent to this property.</p>
     * @param prop A property to test for
     * @return True if the given property is equivalent to this property.
     * @exception ProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.
     */
    public boolean hasEquivalentProperty( Property prop );

    /**
     * <p>Remove the statement that this property and the given property are
     * equivalent.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property that may be declared to be equivalent to this property
     * @exception ProfileException If the {@link Profile#EQUIVALENT_PROPERTY()} property is not supported in the current language profile.
     */
    public void removeEquivalentProperty( Property prop );


    // inverseProperty

    /**
     * <p>Assert that this property is the inverse of the given property. Any existing
     * statements for <code>inverseOf</code> will be removed.</p>
     * @param prop The property that this property is a inverse to.
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    public void setInverseOf( Property prop );

    /**
     * <p>Add a property that this property is the inverse of.</p>
     * @param prop A property that is the inverse of this property.
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    public void addInverseOf( Property prop );

    /**
     * <p>Answer a property of which this property is the inverse, if known,
     * or null if there is no such property. If there is
     * more than one such property, an arbitrary selection is made.</p>
     * <p>Note that this method is slightly different to {@link #getInverse}. See
     * the Javadoc on {@link #getInverse} for a detailed explanation.</p>
     * @return A property which this property is the inverse of, or null.
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    public OntProperty getInverseOf();

    /**
     * <p>Answer an iterator over all of the properties that this property is declared to be the inverse of.
     * Each element of the iterator will be an {@link OntProperty}.</p>
     * @return An iterator over the properties inverse to this property.
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends OntProperty> listInverseOf();

    /**
     * <p>Answer true if this property is the inverse of the given property.</p>
     * @param prop A property to test for
     * @return True if the this property is the inverse of the the given property.
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    public boolean isInverseOf( Property prop );

    /**
     * <p>Remove the statement that this property is the inverse of the given property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param prop A property that may be declared to be inverse to this property
     * @exception ProfileException If the {@link Profile#INVERSE_OF()} property is not supported in the current language profile.
     */
    public void removeInverseProperty( Property prop );


    // view conversion functions

    /**
     * <p>Answer a view of this property as a functional property</p>
     * @return This property, but viewed as a FunctionalProperty node
     * @exception ConversionException if the resource cannot be converted to a functional property
     * given the language profile and the current state of the underlying model.
     */
    public FunctionalProperty asFunctionalProperty();

    /**
     * <p>Answer a view of this property as a datatype property</p>
     * @return This property, but viewed as a DatatypeProperty node
     * @exception ConversionException if the resource cannot be converted to a datatype property
     * given the language profile and the current state of the underlying model.
     */
    @Override
    public DatatypeProperty asDatatypeProperty();

    /**
     * <p>Answer a view of this property as an object property</p>
     * @return This property, but viewed as an ObjectProperty node
     * @exception ConversionException if the resource cannot be converted to an object property
     * given the language profile and the current state of the underlying model.
     */
    @Override
    public ObjectProperty asObjectProperty();

    /**
     * <p>Answer a view of this property as a transitive property</p>
     * @return This property, but viewed as a TransitiveProperty node
     * @exception ConversionException if the resource cannot be converted to a transitive property
     * given the language profile and the current state of the underlying model.
     */
    public TransitiveProperty asTransitiveProperty();

    /**
     * <p>Answer a view of this property as an inverse functional property</p>
     * @return This property, but viewed as an InverseFunctionalProperty node
     * @exception ConversionException if the resource cannot be converted to an inverse functional property
     * given the language profile and the current state of the underlying model.
     */
    public InverseFunctionalProperty asInverseFunctionalProperty();

    /**
     * <p>Answer a view of this property as a symmetric property</p>
     * @return This property, but viewed as a SymmetricProperty node
     * @exception ConversionException if the resource cannot be converted to a symmetric property
     * given the language profile and the current state of the underlying model.
     */
    public SymmetricProperty asSymmetricProperty();

    // conversion functions

    /**
     * <p>Answer a facet of this property as a functional property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a FunctionalProperty facet
     */
    public FunctionalProperty convertToFunctionalProperty();

    /**
     * <p>Answer a facet of this property as a datatype property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a DatatypeProperty facet
     */
    public DatatypeProperty convertToDatatypeProperty();

    /**
     * <p>Answer a facet of this property as an object property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to an ObjectProperty facet
     */
    public ObjectProperty convertToObjectProperty();

    /**
     * <p>Answer a facet of this property as a transitive property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a TransitiveProperty facet
     */
    public TransitiveProperty convertToTransitiveProperty();

    /**
     * <p>Answer a facet of this property as an inverse functional property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to an InverseFunctionalProperty facet
     */
    public InverseFunctionalProperty convertToInverseFunctionalProperty();

    /**
     * <p>Answer a facet of this property as a symmetric property, adding additional information to the model if necessary.</p>
     * @return This property, but converted to a SymmetricProperty facet
     */
    public SymmetricProperty convertToSymmetricProperty();


    // tests on property sub-types

    /**
     * <p>Answer true if this property is a functional property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a functional property.
     */
    public boolean isFunctionalProperty();

    /**
     * <p>Answer true if this property is a datatype property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a datatype property.
     */
    @Override
    public boolean isDatatypeProperty();

    /**
     * <p>Answer true if this property is an object property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as an object property.
     */
    @Override
    public boolean isObjectProperty();

    /**
     * <p>Answer true if this property is a transitive property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a transitive property.
     */
    public boolean isTransitiveProperty();

    /**
     * <p>Answer true if this property is an inverse functional property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as an inverse functional property.
     */
    public boolean isInverseFunctionalProperty();

    /**
     * <p>Answer true if this property is a symmetric property</p>
     * @return True if this this property has an <code>rdf:type</code> that defines it as a symmetric property.
     */
    public boolean isSymmetricProperty();

    /**
     * <p>Answer the property that has declared itself to be the inverse of this property,
     * if any such property is defined.
     * If no such property is defined,
     * return null.  If more than one inverse is defined, return an arbitrary selection.</p>
     * <p>Note that this method is slightly different from {@link #getInverseOf}. Suppose that
     * we have:</p>
     * <pre>
     * :p an owl:ObjectProperty.
     * :q an owl:ObjectProperty.
     * :p owl:inverseOf :q.
     * </pre>
     * <p>Then, <code>getInverse()</code> called on the <code>OntProperty</code>
     * corresponding to <code>q</code> will return <code>p</code>, since <code>q</code>
     * declares itself to be <code>p</code>'s inverse.  However, <em>unless an OWL
     * reasoner is used</em>, calling <code>getInverse()</code> on <code>p</code>
     * will return <code>null</code>. Conversely, absent a reasoner,
     * <code>{@link #getInverseOf}</code> on <code>p</code> will return <code>q</code>,
     * but will return null when called on <code>q</code>.  In the presence of an OWL
     * reasoner, <code>getInverse()</code> and <code>getInverseOf()</code> are
     * equivalent, since <code>owl:inverseOf</code> is a symmetric property.
     * </p>
     *
     * @return The property that is the inverse of this property, or null.
     */
    public OntProperty getInverse();

    /**
     * <p>Answer an iterator over the properties that are defined to be inverses of this property.</p>
     * @return An iterator over the properties that declare themselves the <code>inverseOf</code> this property.
     */
    public ExtendedIterator<? extends OntProperty> listInverse();

    /**
     * <p>Answer true if there is at least one inverse property for this property.</p>
     * @return True if property has an inverse.
     */
    public boolean hasInverse();

    // Miscellaneous other properties

    /**
     * <p>Answer an iterator of all of the classes in this ontology, such
     * that each returned class has this property as one of its
     * properties in {@link OntClass#listDeclaredProperties()}. This
     * simulates a frame-like view of properties and classes; for more
     * details see the <a href="../../../../../../how-to/rdf-frames.html">
     * RDF frames how-to</a>.</p>
     * @return An iterator of the classes having this property as one
     * of their declared properties
     */
    public ExtendedIterator<? extends OntClass> listDeclaringClasses();

    /**
     * <p>Answer an iterator of all of the classes in this ontology, such
     * that each returned class has this property as one of its
     * properties in {@link OntClass#listDeclaredProperties(boolean)}. This
     * simulates a frame-like view of properties and classes; for more
     * details see the <a href="../../../../../../how-to/rdf-frames.html">
     * RDF frames how-to</a>.</p>
     * @param direct If true, use only </em>direct</em> associations between classes
     * and properties
     * @return An iterator of the classes having this property as one
     * of their declared properties
     */
    public ExtendedIterator<? extends OntClass> listDeclaringClasses( boolean direct );

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
    public ExtendedIterator<Restriction> listReferringRestrictions();

}

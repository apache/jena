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
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;



/**
 * <p>
 * Provides a common super-type for all of the abstractions in this ontology
 * representation package.
 * </p>
 */
public interface OntResource
    extends Resource
{
    // Constants
    //////////////////////////////////



    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the model that this resource is attached to, assuming that it
     * is an {@link OntModel}. If this resource is not attached to any model,
     * or is (unusually) attached to a model that is not an <code>OntModel</code>,
     * answer null.</p>
     * @return The ont model that this resource is attached to, or null.
     */
    public OntModel getOntModel();

    /**
     * <p>
     * Answer the ontology language profile that governs the ontology model to which
     * this ontology resource is attached.
     * </p>
     *
     * @return The language profile for this ontology resource
     * @throws JenaException if the resource is not bound to an {@link OntModel}, since
     * that's the only way to get the profile for the resource
     */
    public Profile getProfile();

    /**
     * <p>Answer true if this resource is a symbol in one of the standard ontology
     * languages supported by Jena: RDF, RDFS or OWL. Since these languages
     * have restricted namespaces, this check is simply a convenient way of testing whether
     * this resource is in one of those pre-declared namespaces.</p>
     * @return True if this is a term in the language namespace for OWL, RDF or RDFS.
     */
    public boolean isOntLanguageTerm();


    // sameAs

    /**
     * <p>Assert equivalence between the given resource and this resource. Any existing
     * statements for <code>sameAs</code> will be removed.</p>
     * @param res The resource that is declared to be the same as this resource
     * @exception ProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.
     */
    public void setSameAs( Resource res );

    /**
     * <p>Add a resource that is declared to be equivalent to this resource.</p>
     * @param res A resource that declared to be the same as this resource
     * @exception ProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.
     */
    public void addSameAs( Resource res );

    /**
     * <p>Answer a resource that is declared to be the same as this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An ont resource that declared to be the same as this resource
     * @exception ProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.
     */
    public OntResource getSameAs();

    /**
     * <p>Answer an iterator over all of the resources that are declared to be the same as
     * this resource. Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the resources equivalent to this resource.
     * @exception ProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends Resource> listSameAs();

    /**
     * <p>Answer true if this resource is the same as the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared the same via a <code>sameAs</code> statement.
     * @exception ProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.
     */
    public boolean isSameAs( Resource res );

    /**
     * <p>Remove the statement that this resource is the same as the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to be the sameAs this resource
     */
    public void removeSameAs( Resource res );

    // differentFrom

    /**
     * <p>Assert that the given resource and this resource are distinct. Any existing
     * statements for <code>differentFrom</code> will be removed.</p>
     * @param res The resource that is declared to be distinct from this resource
     * @exception ProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.
     */
    public void setDifferentFrom( Resource res );

    /**
     * <p>Add a statement declaring that this resource is distinct from the given resource.</p>
     * @param res A resource that declared to be distinct from this resource
     * @exception ProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.
     */
    public void addDifferentFrom( Resource res );

    /**
     * <p>Answer a resource that is declared to be distinct from this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that declared to be different from this resource
     * @exception ProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.
     */
    public OntResource getDifferentFrom();

    /**
     * <p>Answer an iterator over all of the resources that are declared to be different from
     * this resource. Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the resources different from this resource.
     * @exception ProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends Resource> listDifferentFrom();

    /**
     * <p>Answer true if this resource is different from the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared to be distinct via a <code>differentFrom</code> statement.
     */
    public boolean isDifferentFrom( Resource res );

    /**
     * <p>Remove the statement that this resource is different the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to be differentFrom this resource
     */
    public void removeDifferentFrom( Resource res );

    // seeAlso

    /**
     * <p>Assert that the given resource provides additional information about the definition of this resource</p>
     * @param res A resource that can provide additional information about this resource
     * @exception ProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.
     */
    public void setSeeAlso( Resource res );

    /**
     * <p>Add a resource that is declared to provided additional information about the definition of this resource</p>
     * @param res A resource that provides extra information on this resource
     * @exception ProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.
     */
    public void addSeeAlso( Resource res );

    /**
     * <p>Answer a resource that provides additional information about this resource. If more than one such resource
     * is defined, make an arbitrary choice.</p>
     * @return res A resource that provides additional information about this resource
     * @exception ProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.
     */
    public Resource getSeeAlso();

    /**
     * <p>Answer an iterator over all of the resources that are declared to provide additional
     * information about this resource.</p>
     * @return An iterator over the resources providing additional information on this resource.
     * @exception ProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.
     */
    public ExtendedIterator<RDFNode> listSeeAlso();

    /**
     * <p>Answer true if this resource has the given resource as a source of additional information.</p>
     * @param res A resource to test against
     * @return True if the <code>res</code> provides more information on this resource.
     */
    public boolean hasSeeAlso( Resource res );

    /**
     * <p>Remove the statement indicating the given resource as a source of additional information
     * about this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to provide additional information about this resource
     */
    public void removeSeeAlso( Resource res );

    // is defined by

    /**
     * <p>Assert that the given resource provides a source of definitions about this resource. Any existing
     * statements for <code>isDefinedBy</code> will be removed.</p>
     * @param res The resource that is declared to be a definition of this resource.
     * @exception ProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.
     */
    public void setIsDefinedBy( Resource res );

    /**
     * <p>Add a resource that is declared to provide a definition of this resource.</p>
     * @param res A defining resource
     * @exception ProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.
     */
    public void addIsDefinedBy( Resource res );

    /**
     * <p>Answer a resource that is declared to provide a definition of this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that is declared to provide a definition of this resource
     * @exception ProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.
     */
    public Resource getIsDefinedBy();

    /**
     * <p>Answer an iterator over all of the resources that are declared to define
     * this resource. </p>
     * @return An iterator over the resources defining this resource.
     * @exception ProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.
     */
    public ExtendedIterator<RDFNode> listIsDefinedBy();

    /**
     * <p>Answer true if this resource is defined by the given resource.</p>
     * @param res A resource to test against
     * @return True if <code>res</code> defines this resource.
     */
    public boolean isDefinedBy( Resource res );

    /**
     * <p>Remove the statement that this resource is defined by the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to define this resource
     */
    public void removeDefinedBy( Resource res );

    // version info

    /**
     * <p>Assert that the given string is the value of the version info for this resource. Any existing
     * statements for <code>versionInfo</code> will be removed.</p>
     * @param info The version information for this resource
     * @exception ProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.
     */
    public void setVersionInfo( String info );

    /**
     * <p>Add the given version information to this resource.</p>
     * @param info A version information string for this resource
     * @exception ProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.
     */
    public void addVersionInfo( String info );

    /**
     * <p>Answer the version information string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return A version info string
     * @exception ProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.
     */
    public String getVersionInfo();

    /**
     * <p>Answer an iterator over all of the version info strings for this resource.</p>
     * @return An iterator over the version info strings for this resource.
     * @exception ProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.
     */
    public ExtendedIterator<String> listVersionInfo();

    /**
     * <p>Answer true if this resource has the given version information</p>
     * @param info Version information to test for
     * @return True if this resource has <code>info</code> as version information.
     */
    public boolean hasVersionInfo( String info );

    /**
     * <p>Remove the statement that the given string provides version information about
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param info A version information string to be removed
     */
    public void removeVersionInfo( String info );

    // label

    /**
     * <p>Assert that the given string is the value of the label for this resource. Any existing
     * statements for <code>label</code> will be removed.</p>
     * @param label The label for this resource
     * @param lang The language attribute for this label (EN, FR, etc) or null if not specified.
     * @exception ProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.
     */
    public void setLabel( String label, String lang );

    /**
     * <p>Add the given label to this resource.</p>
     * @param label A label string for this resource
     * @param lang The language attribute for this label (EN, FR, etc) or null if not specified.
     * @exception ProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.
     */
    public void addLabel( String label, String lang );

    /**
     * <p>Add the given label to this resource.</p>
     * @param label The literal label
     * @exception ProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.
     */
    public void addLabel( Literal label );

    /**
     * <p>Answer the label string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @param lang The language attribute for the desired label (EN, FR, etc) or null for don't care. Will
     * attempt to retrieve the most specific label matching the given language</p>
     * @return A label string matching the given language, or null if there is no matching label.
     * @exception ProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.
     */
    public String getLabel( String lang );

    /**
     * <p>Answer an iterator over all of the label literals for this resource.</p>
     * @param lang The language tag to restrict the listed labels to, or null to select all labels
     * @return An iterator over RDF {@link Literal}'s.
     * @exception ProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.
     */
    public ExtendedIterator<RDFNode> listLabels( String lang );

    /**
     * <p>Answer true if this resource has the given label</p>
     * @param label The label to test for
     * @param lang The optional language tag, or null for don't care.
     * @return True if this resource has <code>label</code> as a label.
     */
    public boolean hasLabel( String label, String lang );

    /**
     * <p>Answer true if this resource has the given label</p>
     * @param label The label to test for
     * @return True if this resource has <code>label</code> as a label.
     */
    public boolean hasLabel( Literal label );

    /**
     * <p>Remove the statement that the given string is a label for
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param label A label string to be removed
     * @param lang A lang tag, or null if not specified
     */
    public void removeLabel( String label, String lang );

    /**
     * <p>Remove the statement that the given string is a label for
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param label A label literal to be removed
     */
    public void removeLabel( Literal label );

    // comment

    /**
     * <p>Assert that the given string is the comment on this resource. Any existing
     * statements for <code>comment</code> will be removed.</p>
     * @param comment The comment for this resource
     * @param lang The language attribute for this comment (EN, FR, etc) or null if not specified.
     * @exception ProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.
     */
    public void setComment( String comment, String lang );

    /**
     * <p>Add the given comment to this resource.</p>
     * @param comment A comment string for this resource
     * @param lang The language attribute for this comment (EN, FR, etc) or null if not specified.
     * @exception ProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.
     */
    public void addComment( String comment, String lang );

    /**
     * <p>Add the given comment to this resource.</p>
     * @param comment The literal comment
     * @exception ProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.
     */
    public void addComment( Literal comment );

    /**
     * <p>Answer the comment string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @param lang The language attribute for the desired comment (EN, FR, etc) or null for don't care. Will
     * attempt to retrieve the most specific comment matching the given language</p>
     * @return A comment string matching the given language, or null if there is no matching comment.
     * @exception ProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.
     */
    public String getComment( String lang );

    /**
     * <p>Answer an iterator over all of the comment literals for this resource.</p>
     * @param lang The language tag to restrict the listed comments to, or null to select all comments
     * @return An iterator over RDF {@link Literal}'s.
     * @exception ProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.
     */
    public ExtendedIterator<RDFNode> listComments( String lang );

    /**
     * <p>Answer true if this resource has the given comment.</p>
     * @param comment The comment to test for
     * @param lang The optional language tag, or null for don't care.
     * @return True if this resource has <code>comment</code> as a comment.
     */
    public boolean hasComment( String comment, String lang );

    /**
     * <p>Answer true if this resource has the given comment.</p>
     * @param comment The comment to test for
     * @return True if this resource has <code>comment</code> as a comment.
     */
    public boolean hasComment( Literal comment );

    /**
     * <p>Remove the statement that the given string is a comment on
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param comment A comment string to be removed
     * @param lang A lang tag, or null if not specified
     */
    public void removeComment( String comment, String lang );

    /**
     * <p>Remove the statement that the given string is a comment on
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param comment A comment literal to be removed
     */
    public void removeComment( Literal comment );


    // rdf:type

    /**
     * <p>Set the RDF type (i.e. the class) for this resource, replacing any
     * existing <code>rdf:type</code> property. Any existing statements for the RDF type
     * will first be removed.</p>
     *
     * @param cls The RDF resource denoting the new value for the <code>rdf:type</code> property,
     *                 which will replace any existing type property.
     * @see Individual#setOntClass(Resource)
     */
    public void setRDFType( Resource cls );

    /**
     * <p>Add the given class as one of the <code>rdf:type</code>'s for this resource.</p>
     *
     * @param cls An RDF resource denoting a new value for the <code>rdf:type</code> property.
     * @see Individual#addOntClass(Resource)
     */
    public void addRDFType( Resource cls );

    /**
     * <p>
     * Answer the <code>rdf:type</code> (i.e. the class) of this resource. If there
     * is more than one type for this resource, the return value will be one of
     * the values, but it is not specified which one (nor that it will consistently
     * be the same one each time). Equivalent to <code>getRDFType( false )</code>.
     * </p>
     *
     * @return A resource that is the rdf:type for this resource, or one of them if
     * more than one is defined.
     * @see Individual#getOntClass()
     */
    public Resource getRDFType();

    /**
     * <p>
     * Answer the <code>rdf:type</code> (i.e. the class) of this resource. If there
     * is more than one type for this resource, the return value will be one of
     * the values, but it is not specified which one (nor that it will consistently
     * be the same one each time).
     * </p>
     *
     * @param direct If true, only consider the direct types of this resource, and not
     * the super-classes of the type(s).
     * @return A resource that is the rdf:type for this resource, or one of them if
     * more than one is defined.
     * @see Individual#getOntClass(boolean)
     */
    public Resource getRDFType( boolean direct );

    /**
     * <p>
     * Answer an iterator over the RDF classes to which this resource belongs.
     * </p>
     *
     * @param direct If true, only answer those resources that are direct types
     * of this resource, not the super-classes of the class etc.
     * @return An iterator over the set of this resource's classes. Each member
     * of the iteration will be an {@link Resource}.  Use
     * <code>.as(&nbsp;OntClass.class&nbsp;)</code> to map this resource to
     * an OntClass.
     * @see Individual#listOntClasses(boolean)
     */
    public ExtendedIterator<Resource> listRDFTypes( boolean direct );

    /**
     * <p>
     * Answer true if this resource is a member of the class denoted by the
     * given class resource.
     * </p>
     *
     * @param ontClass Denotes a class to which this value may belong
     * @param direct If true, only consider the direct types of this resource, ignoring
     * the super-classes of the stated types.
     * @return True if this resource has the given class as one of its <code>rdf:type</code>'s.
     * @see Individual#hasOntClass(Resource, boolean)
     */
    public boolean hasRDFType( Resource ontClass, boolean direct );

    /**
     * <p>
     * Answer true if this resource is a member of the class denoted by the
     * given class resource.  Includes all available types, so is equivalent to
     * <code><pre>
     * hasRDF( ontClass, false );
     * </pre></code>
     * </p>
     *
     * @param ontClass Denotes a class to which this value may belong
     * @return True if this resource has the given class as one of its <code>rdf:type</code>'s.
     * @see Individual#hasOntClass(Resource)
     */
    public boolean hasRDFType( Resource ontClass );

    /**
     * <p>Remove the statement that this resource is of the given RDF type.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A resource denoting a class that that is to be removed from the classes of this resource
     * @see Individual#removeOntClass(Resource)
     */
    public void removeRDFType( Resource cls );

    /**
     * <p>
     * Answer true if this resource is a member of the class denoted by the
     * given URI.</p>
     *
     * @param uri Denotes the URI of a class to which this value may belong
     * @return True if this resource has the given class as one of its <code>rdf:type</code>'s.
     * @see Individual#hasOntClass(String)
     */
    public boolean hasRDFType( String uri );



    // other utility methods

    /**
     * <p>Answer the cardinality of the given property on this resource. The cardinality
     * is the number of distinct values there are for the property.</p>
     * @param p A property
     * @return The cardinality for the property <code>p</code> on this resource, as an
     * integer greater than or equal to zero.
     */
    public int getCardinality( Property p );

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
    public void setPropertyValue( Property property, RDFNode value );


    /**
     * <p>Answer the value of a given RDF property for this  resource, or null
     * if it doesn't have one.  The value is returned as an RDFNode, from which
     * the concrete data value can be extracted for literals. If the value is
     * a resource, it will present the {@link OntResource} facet.
     * If there is more than one RDF
     * statement with the given property for the current value, it is not defined
     * which of the values will be returned.</p>
     *
     * @param property An RDF property
     * @return An RDFNode whose value is the value, or one of the values, of the
     *         given property. If the property is not defined the method returns null.
     */
   public RDFNode getPropertyValue( Property property );


    /**
     * <p>Answer an iterator over the values for a given RDF property. Each
     * value in the iterator will be an {@link RDFNode}.</p>
     *
     * @param property The property whose values are sought
     * @return An Iterator over the values of the property
     */
    public NodeIterator listPropertyValues( Property property );

    /**
     * <p>Remove the specific property-value pair from this resource.</p>
     *
     * @param property The property to be removed
     * @param value The specific value of the property to be removed
     */
    public void removeProperty( Property property, RDFNode value );

    /**
     * <p>Removes this resource from the ontology by deleting any statements that refer to it,
     * as either statement-subject or statement-object.
     * If this resource is a property, this method will <strong>not</strong> remove statements
     * whose predicate is this property.</p>
     * <p><strong>Caveat:</strong> Jena RDF models contain statements, not resources <em>per se</em>,
     * so this method simulates removal of an object by removing all of the statements that have
     * this resource as subject or object, with one exception. If the resource is referenced
     * in an RDF List, i.e. as the object of an <code>rdf:first</code> statement in a list cell,
     * this reference is <strong>not</strong> removed.  Removing an arbitrary <code>rdf:first</code>
     * statement from the midst of a list, without doing other work to repair the list, would
     * leave an ill-formed list in the model.  Therefore, if this resource is known to appear
     * in a list somewhere in the model, it should be separately deleted from that list before
     * calling this remove method.
     * </p>
     */
    public void remove();



    // Conversion methods

    /**
     * <p>Answer a view of this resource as a property</p>
     * @return This resource, but viewed as an OntProperty
     * @exception ConversionException if the resource cannot be converted to a property
     */
    public OntProperty asProperty();

    /**
     * <p>Answer a view of this resource as an annotation property</p>
     * @return This resource, but viewed as an AnnotationProperty
     * @exception ConversionException if the resource cannot be converted to an annotation property
     */
    public AnnotationProperty asAnnotationProperty();

    /**
     * <p>Answer a view of this resource as an object property</p>
     * @return This resource, but viewed as an ObjectProperty
     * @exception ConversionException if the resource cannot be converted to an object property
     */
    public ObjectProperty asObjectProperty();

    /**
     * <p>Answer a view of this resource as a datatype property</p>
     * @return This resource, but viewed as a DatatypeProperty
     * @exception ConversionException if the resource cannot be converted to a datatype property
     */
    public DatatypeProperty asDatatypeProperty();

    /**
     * <p>Answer a view of this resource as an individual</p>
     * @return This resource, but viewed as an Individual
     * @exception ConversionException if the resource cannot be converted to an individual
     */
    public Individual asIndividual();

    /**
     * <p>Answer a view of this resource as a class</p>
     * @return This resource, but viewed as an OntClass
     * @exception ConversionException if the resource cannot be converted to a class
     */
    public OntClass asClass();

    /**
     * <p>Answer a view of this resource as an ontology description node</p>
     * @return This resource, but viewed as an Ontology
     * @exception ConversionException if the resource cannot be converted to an ontology description node
     */
    public Ontology asOntology();

    /**
     * <p>Answer a view of this resource as a data range</p>
     * @return This resource, but viewed as a DataRange
     * @exception ConversionException if the resource cannot be converted to a data range
     */
    public DataRange asDataRange();

    /**
     * <p>Answer a view of this resource as an 'all different' declaration</p>
     * @return This resource, but viewed as an AllDifferent node
     * @exception ConversionException if the resource cannot be converted to an all different declaration
     */
    public AllDifferent asAllDifferent();


    // Conversion test methods

    /**
     * <p>Answer true if this resource can be viewed as a property</p>
     * @return True if this resource can be viewed as an OntProperty
     */
    public boolean isProperty();

    /**
     * <p>Answer true if this resource can be viewed as an annotation property</p>
     * @return True if this resource can be viewed as an AnnotationProperty
     */
    public boolean isAnnotationProperty();

    /**
     * <p>Answer true if this resource can be viewed as an object property</p>
     * @return True if this resource can be viewed as an ObjectProperty
     */
    public boolean isObjectProperty();

    /**
     * <p>Answer true if this resource can be viewed as a datatype property</p>
     * @return True if this resource can be viewed as a DatatypeProperty
     */
    public boolean isDatatypeProperty();

    /**
     * <p>Answer true if this resource can be viewed as an individual</p>
     * @return True if this resource can be viewed as an Individual
     */
    public boolean isIndividual();

    /**
     * <p>Answer true if this resource can be viewed as a class</p>
     * @return True if this resource can be viewed as an OntClass
     */
    public boolean isClass();

    /**
     * <p>Answer true if this resource can be viewed as an ontology description node</p>
     * @return True if this resource can be viewed as an Ontology
     */
    public boolean isOntology();

    /**
     * <p>Answer true if this resource can be viewed as a data range</p>
     * @return True if this resource can be viewed as a DataRange
     */
    public boolean isDataRange();

    /**
     * <p>Answer true if this resource can be viewed as an 'all different' declaration</p>
     * @return True if this resource can be viewed as an AllDifferent node
     */
    public boolean isAllDifferent();


}

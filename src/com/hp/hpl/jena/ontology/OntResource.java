/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: OntResource.java,v $
 * Revision           $Revision: 1.12 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-02 10:21:45 $
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
import com.hp.hpl.jena.ontology.path.PathSet;
import com.hp.hpl.jena.rdf.model.*;

import java.util.Iterator;



/**
 * <p>
 * Provides a common super-type for all of the abstractions in this ontology
 * representation package. 
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntResource.java,v 1.12 2003-06-02 10:21:45 ian_dickinson Exp $
 */
public interface OntResource
    extends Resource
{
    // Constants
    //////////////////////////////////



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
    public Profile getProfile();
    
    // sameAs
    
    /**
     * <p>Assert equivalence between the given resource and this resource. Any existing 
     * statements for <code>sameAs</code> will be removed.</p>
     * @param res The resource that is declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public void setSameAs( Resource res );

    /**
     * <p>Add a resource that is declared to be equivalent to this resource.</p>
     * @param res A resource that declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public void addSameAs( Resource res );

    /**
     * <p>Answer a resource that is declared to be the same as this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An ont resource that declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public OntResource getSameAs();

    /**
     * <p>Answer an iterator over all of the resources that are declared to be the same as
     * this resource. Each elemeent of the iterator will be an {@link #OntResource}.</p>
     * @return An iterator over the resources equivalent to this resource.
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public Iterator listSameAs();

    /**
     * <p>Answer true if this resource is the same as the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared the same via a <code>sameAs</code> statement.
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */
    public boolean isSameAs( Resource res );
    
    
    // differentFrom
    
    /**
     * <p>Assert that the given resource and this resource are distinct. Any existing 
     * statements for <code>differentFrom</code> will be removed.</p>
     * @param res The resource that is declared to be distinct from this resource
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public void setDifferentFrom( Resource res );

    /**
     * <p>Add a resource that is declared to be equivalent to this resource.</p>
     * @param res A resource that declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public void addDifferentFrom( Resource res );

    /**
     * <p>Answer a resource that is declared to be distinct from this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that declared to be different from this resource
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public OntResource getDifferentFrom();

    /**
     * <p>Answer an iterator over all of the resources that are declared to be different from
     * this resource. Each elemeent of the iterator will be an {@link #OntResource}.</p>
     * @return An iterator over the resources different from this resource.
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public Iterator listDifferentFrom();

    /**
     * <p>Answer true if this resource is different from the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared to be distinct via a <code>differentFrom</code> statement.
     */
    public boolean isDifferentFrom( Resource res );
    
    // seeAlso
    
    /**
     * <p>Assert that the given resource provides additional information about the definition of this resource</p>
     * @param res A resource that can provide additional information about this resource
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public void setSeeAlso( Resource res );

    /**
     * <p>Add a resource that is declared to provided additional information about the definition of this resource</p>
     * @param res A resource that provides extra information on this resource
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public void addSeeAlso( Resource res );

    /**
     * <p>Answer a resource that provides additional information about this resource. If more than one such resource
     * is defined, make an arbitrary choice.</p>
     * @return res A resource that provides additional information about this resource
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public Resource getSeeAlso();

    /**
     * <p>Answer an iterator over all of the resources that are declared to provide addition
     * information about this resource.</p>
     * @return An iterator over the resources providing additional definition on this resource.
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public Iterator listSeeAlso();

    /**
     * <p>Answer true if this resource has the given resource as a source of additional information.</p>
     * @param res A resource to test against
     * @return True if the <code>res</code> provides more information on this resource.
     */
    public boolean hasSeeAlso( Resource res );
    
    // is defined by
    
    /**
     * <p>Assert that the given resource provides a source of definitions about this resource. Any existing 
     * statements for <code>isDefinedBy</code> will be removed.</p>
     * @param res The resource that is declared to be a definition of this resource.
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public void setIsDefinedBy( Resource res );

    /**
     * <p>Add a resource that is declared to provide a definition of this resource.</p>
     * @param res A defining resource 
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public void addIsDefinedBy( Resource res );

    /**
     * <p>Answer a resource that is declared to provide a definition of this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that is declared to provide a definition of this resource
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public Resource getIsDefinedBy();

    /**
     * <p>Answer an iterator over all of the resources that are declared to define
     * this resource. </p>
     * @return An iterator over the resources defining this resource.
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public Iterator listIsDefinedBy();

    /**
     * <p>Answer true if this resource is defined by the given resource.</p>
     * @param res A resource to test against
     * @return True if <code>res</code> defines this resource.
     */
    public boolean isDefinedBy( Resource res );
    
    // version info

    /**
     * <p>Assert that the given string is the value of the version info for this resource. Any existing 
     * statements for <code>versionInfo</code> will be removed.</p>
     * @param info The version information for this resource
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
    public void setVersionInfo( String info );

    /**
     * <p>Add the given version information to this resource.</p>
     * @param info A version information string for this resource 
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
    public void addVersionInfo( String info );

    /**
     * <p>Answer the version information string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return A version info string
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
    public String getVersionInfo();

    /**
     * <p>Answer an iterator over all of the version info strings for this resource.</p>
     * @return An iterator over the version info strings for this resource.
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
    public Iterator listVersionInfo();

    /**
     * <p>Answer true if this resource has the given version information</p>
     * @param info Version information to test for
     * @return True if this resource has <code>info</code> as version information.
     */
    public boolean hasVersionInfo( String info );
    
    // label
    
    /**
     * <p>Assert that the given string is the value of the label for this resource. Any existing 
     * statements for <code>label</code> will be removed.</p>
     * @param label The label for this resource
     * @param lang The language attribute for this label (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public void setLabel( String label, String lang );

    /**
     * <p>Add the given label to this resource.</p>
     * @param label A label string for this resource
     * @param lang The language attribute for this label (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public void addLabel( String label, String lang );

    /**
     * <p>Add the given label to this resource.</p>
     * @param label The literal label
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public void addLabel( Literal label );

    /**
     * <p>Answer the label string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @param lang The language attribute for the desired label (EN, FR, etc) or null for don't care. Will 
     * attempt to retreive the most specific label matching the given language</p>
     * @return A label string matching the given language, or null if there is no matching label.
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public String getLabel( String lang );

    /**
     * <p>Answer an iterator over all of the label literals for this resource.</p>
     * @param lang The language tag to restric the listed comments to, or null to select all comments
     * @return An iterator over RDF {@link Literal}'s.
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public Iterator listLabels( String lang );

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

    // comment

    /**
     * <p>Assert that the given string is the comment on this resource. Any existing 
     * statements for <code>comment</code> will be removed.</p>
     * @param comment The comment for this resource
     * @param lang The language attribute for this comment (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public void setComment( String comment, String lang );

    /**
     * <p>Add the given comment to this resource.</p>
     * @param comment A comment string for this resource
     * @param lang The language attribute for this comment (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public void addComment( String comment, String lang );

    /**
     * <p>Add the given comment to this resource.</p>
     * @param comment The literal comment
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public void addComment( Literal comment );

    /**
     * <p>Answer the comment string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @param lang The language attribute for the desired comment (EN, FR, etc) or null for don't care. Will 
     * attempt to retreive the most specific comment matching the given language</p>
     * @return A comment string matching the given language, or null if there is no matching comment.
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public String getComment( String lang );

    /**
     * <p>Answer an iterator over all of the comment literals for this resource.</p>
     * @param lang The language tag to restric the listed comments to, or null to select all comments
     * @return An iterator over RDF {@link Literal}'s.
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public Iterator listComments( String lang );

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
     * <p>Answer the cardinality of the given property on this resource. The cardinality
     * is the number of distinct values there are for the property.</p>
     * @param p A property
     * @return The cardinality for the property <code>p</code> on this resource, as an
     * integer greater than or equal to zero.
     */
    public int getCardinality( Property p );
    
    /**
     * <p>
     * Answer an {@link PathSet accessor} for the given
     * property of any ontology value. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @param p A property
     * @param name A string name for the property, in case an error must be reported and the property is null
     * @return An abstract accessor for the property p
     */
    public PathSet accessor( Property p, String name );
    
    
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
    public PathSet accessor( Property p );
    
    
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
     * <p>
     * Remove any values for a given property from this resource.
     * </p>
     *
     * @param property The RDF resource that defines the property to be removed
     */
    public void removeAll( Property property );


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
    public void setRDFType( Resource ontClass );


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
    public void setRDFType( Resource ontClass, boolean replace );


    /**
     * <p>
     * Answer true if this resource is a member of the class denoted by the given URI.
     * </p>
     *
     * @param classURI String denoting the URI of the class to test against
     * @return True if it can be shown that this ontology resource has an
     *         <code>rdf:type</code> of the given URI.
     */
    public boolean hasRDFType( String classURI );


    /**
     * <p>
     * Answer true if this resource is a member of the class denoted by the
     * given class resource.
     * </p>
     * 
     * @param ontClass Denotes a class to which this value may belong
     * @return True if <code><i>this</i> rdf:type <i>ontClass</i></code> is
     * true of the current model.
     */
    public boolean hasRDFType( Resource ontClass );


    /**
     * <p>
     * Answer an iterator over all of the RDF types to which this class belongs.
     * </p>
     *
     * @param closed TODO Not used in the current implementation  - fix
     * @return an iterator over the set of this ressource's classes
     */
    public Iterator getRDFTypes( boolean closed );


    // Conversion methods
    
    /** 
     * <p>Answer a view of this resource as an annotation property</p>
     * @return This resource, but viewed as an AnnotationProperty
     * @exception ConversionException if the resource cannot be converted to an annotation property
     */
    public AnnotationProperty asAnnotationProperty();
    
    /** 
     * <p>Answer a view of this resource as a list </p>
     * @return This resource, but viewed as an OntList
     * @exception ConversionException if the resource cannot be converted to a list
     */
    public OntList asList();
    
    /** 
     * <p>Answer a view of this resource as a property</p>
     * @return This resource, but viewed as an OntProperty
     * @exception ConversionException if the resource cannot be converted to a property
     */
    public OntProperty asProperty();
    
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
     * <p>Answer a view of this resource as an 'all different' declaration</p>
     * @return This resource, but viewed as an AllDifferent node
     * @exception ConversionException if the resource cannot be converted to an all different declaration
     */
    public AllDifferent asAllDifferent();


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

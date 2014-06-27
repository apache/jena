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
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Abstract base class to provide shared implementation for implementations of ontology
 * resources.
 * </p>
 */
public class OntResourceImpl
    extends ResourceImpl
    implements OntResource
{
    // Constants
    //////////////////////////////////

    /** List of namespaces that are reserved for known ontology languages */
    public static final String[] KNOWN_LANGUAGES = new String[] {OWL.NS,
                                                                 RDF.getURI(),
                                                                 RDFS.getURI(),
                                                                 XSDDatatype.XSD};

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating OntResource facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new OntResourceImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to OntResource");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an OntResource facet if it is a uri or bnode
            return node.isURI() || node.isBlank();
        }
    };

    private static final Logger log = LoggerFactory.getLogger( OntResourceImpl.class );

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
     * <p>Answer the model that this resource is attached to, assuming that it
     * is an {@link OntModel}. If this resource is not attached to any model,
     * or is (unusually) attached to a model that is not an <code>OntModel</code>,
     * answer null.</p>
     * @return The ont model that this resource is attached to, or null.
     */
    @Override
    public OntModel getOntModel() {
        Model m = getModel();
        return (m instanceof OntModel) ? (OntModel) m : null;
    }

    /**
     * <p>
     * Answer the ontology language profile that governs the ontology model to which
     * this ontology resource is attached.
     * </p>
     *
     * @return The language profile for this ontology resource
     * @throws JenaException if the resource is not bound to an OntModel, since
     * that's the only way to get the profile for the resource
     */
    @Override
    public Profile getProfile() {
        try {
            return ((OntModel) getModel()).getProfile();
        }
        catch (ClassCastException e) {
            throw new JenaException( "Resource " + toString() + " is not attached to an OntModel, so cannot access its language profile" );
        }
    }

    /**
     * <p>Answer true if this resource is a symbol in one of the standard ontology
     * languages supported by Jena: RDF, RDFS, OWL or DAML+OIL. Since these languages
     * have restricted namespaces, this check is simply a convenient way of testing whether
     * this resource is in one of those pre-declared namespaces.</p>
     * @return True if this is a term in the language namespace for OWL, RDF, RDFS or DAML+OIL.
     */
    @Override
    public boolean isOntLanguageTerm() {
        if (!isAnon()) {
            for ( String KNOWN_LANGUAGE : KNOWN_LANGUAGES )
            {
                if ( getURI().startsWith( KNOWN_LANGUAGE ) )
                {
                    return true;
                }
            }
        }
        return false;
    }


    // sameAs

    /**
     * <p>Assert equivalence between the given resource and this resource. Any existing
     * statements for <code>sameAs</code> will be removed.</p>
     * @param res The resource that is declared to be the same as this resource
     * @exception ProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.
     */
    @Override
    public void setSameAs( Resource res ) {
        setPropertyValue( getProfile().SAME_AS(), "SAME_AS", res );
    }

    /**
     * <p>Add a resource that is declared to be equivalent to this resource.</p>
     * @param res A resource that declared to be the same as this resource
     * @exception ProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.
     */
    @Override
    public void addSameAs( Resource res ) {
        addPropertyValue( getProfile().SAME_AS(), "SAME_AS", res );
    }

    /**
     * <p>Answer a resource that is declared to be the same as this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that declared to be the same as this resource
     * @exception ProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.
     */
    @Override
    public OntResource getSameAs() {
        return objectAsResource( getProfile().SAME_AS(), "SAME_AS" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to be the same as
     * this resource. Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the resources equivalent to this resource.
     * @exception ProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntResource> listSameAs() {
        return listAs( getProfile().SAME_AS(), "SAME_AS", OntResource.class );
    }

    /**
     * <p>Answer true if this resource is the same as the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared the same via a <code>sameAs</code> statement.
     */
    @Override
    public boolean isSameAs( Resource res ) {
        return hasPropertyValue( getProfile().SAME_AS(), "SAME_AS", res );
    }

    /**
     * <p>Remove the statement that this resource is the same as the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to be the sameAs this resource
     */
    @Override
    public void removeSameAs( Resource res ) {
        removePropertyValue( getProfile().SAME_AS(), "SAME_AS", res );
    }

    // differentFrom

    /**
     * <p>Assert that the given resource and this resource are distinct. Any existing
     * statements for <code>differentFrom</code> will be removed.</p>
     * @param res The resource that is declared to be distinct from this resource
     * @exception ProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.
     */
    @Override
    public void setDifferentFrom( Resource res ) {
        setPropertyValue( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", res );
    }

    /**
     * <p>Add a statement declaring that this resource is distinct from the given resource.</p>
     * @param res A resource that declared to be distinct from this resource
     * @exception ProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.
     */
    @Override
    public void addDifferentFrom( Resource res ) {
        addPropertyValue( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", res );
    }

    /**
     * <p>Answer a resource that is declared to be distinct from this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that declared to be different from this resource
     * @exception ProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.
     */
    @Override
    public OntResource getDifferentFrom() {
        return objectAsResource( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to be different from
     * this resource. Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the resources different from this resource.
     * @exception ProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntResource> listDifferentFrom() {
        return listAs( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", OntResource.class );
    }

    /**
     * <p>Answer true if this resource is different from the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared to be distinct via a <code>differentFrom</code> statement.
     */
    @Override
    public boolean isDifferentFrom( Resource res ) {
        return hasPropertyValue( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", res );
    }

    /**
     * <p>Remove the statement that this resource is different the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to be differentFrom this resource
     */
    @Override
    public void removeDifferentFrom( Resource res ) {
        removePropertyValue( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", res );
    }

    // seeAlso

    /**
     * <p>Assert that the given resource provides additional information about the definition of this resource</p>
     * @param res A resource that can provide additional information about this resource
     * @exception ProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.
     */
    @Override
    public void setSeeAlso( Resource res ) {
        setPropertyValue( getProfile().SEE_ALSO(), "SEE_ALSO", res );
    }

    /**
     * <p>Add a resource that is declared to provided additional information about the definition of this resource</p>
     * @param res A resource that provides extra information on this resource
     * @exception ProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.
     */
    @Override
    public void addSeeAlso( Resource res ) {
        addPropertyValue( getProfile().SEE_ALSO(), "SEE_ALSO", res );
    }

    /**
     * <p>Answer a resource that provides additional information about this resource. If more than one such resource
     * is defined, make an arbitrary choice.</p>
     * @return res A resource that provides additional information about this resource
     * @exception ProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.
     */
    @Override
    public Resource getSeeAlso() {
        return objectAsResource( getProfile().SEE_ALSO(), "SEE_ALSO" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to provide addition
     * information about this resource.</p>
     * @return An iterator over the resources providing additional definition on this resource.
     * @exception ProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<RDFNode> listSeeAlso() {
        checkProfile( getProfile().SEE_ALSO(), "SEE_ALSO" );
        return WrappedIterator.create( listProperties( getProfile().SEE_ALSO() ) )
               .mapWith( new ObjectAsOntResourceMapper() );
    }

    /**
     * <p>Answer true if this resource has the given resource as a source of additional information.</p>
     * @param res A resource to test against
     * @return True if the <code>res</code> provides more information on this resource.
     */
    @Override
    public boolean hasSeeAlso( Resource res ) {
        return hasPropertyValue( getProfile().SEE_ALSO(), "SEE_ALSO", res );
    }

    /**
     * <p>Remove the statement indicating the given resource as a source of additional information
     * about this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to provide additional information about this resource
     */
    @Override
    public void removeSeeAlso( Resource res ) {
        removePropertyValue( getProfile().SEE_ALSO(), "SEE_ALSO", res );
    }

    // is defined by

    /**
     * <p>Assert that the given resource provides a source of definitions about this resource. Any existing
     * statements for <code>isDefinedBy</code> will be removed.</p>
     * @param res The resource that is declared to be a definition of this resource.
     * @exception ProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.
     */
    @Override
    public void setIsDefinedBy( Resource res ) {
        setPropertyValue( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY", res );
    }

    /**
     * <p>Add a resource that is declared to provide a definition of this resource.</p>
     * @param res A defining resource
     * @exception ProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.
     */
    @Override
    public void addIsDefinedBy( Resource res ) {
        addPropertyValue( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY", res );
    }

    /**
     * <p>Answer a resource that is declared to provide a definition of this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that is declared to provide a definition of this resource
     * @exception ProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.
     */
    @Override
    public Resource getIsDefinedBy() {
        return objectAsResource( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to define
     * this resource. </p>
     * @return An iterator over the resources defining this resource.
     * @exception ProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<RDFNode> listIsDefinedBy() {
        checkProfile( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY" );
        return WrappedIterator.create( listProperties( getProfile().IS_DEFINED_BY() ) )
               .mapWith( new ObjectAsOntResourceMapper() );
    }

    /**
     * <p>Answer true if this resource is defined by the given resource.</p>
     * @param res A resource to test against
     * @return True if <code>res</code> defines this resource.
     */
    @Override
    public boolean isDefinedBy( Resource res ) {
        return hasPropertyValue( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY", res );
    }

    /**
     * <p>Remove the statement that this resource is defined by the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to define this resource
     */
    @Override
    public void removeDefinedBy( Resource res ) {
        removePropertyValue( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY", res );
    }


    // version info

    /**
     * <p>Assert that the given string is the value of the version info for this resource. Any existing
     * statements for <code>versionInfo</code> will be removed.</p>
     * @param info The version information for this resource
     * @exception ProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.
     */
    @Override
    public void setVersionInfo( String info ) {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        removeAll( getProfile().VERSION_INFO() );
        addVersionInfo( info );
    }

    /**
     * <p>Add the given version information to this resource.</p>
     * @param info A version information string for this resource
     * @exception ProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.
     */
    @Override
    public void addVersionInfo( String info ) {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        addProperty( getProfile().VERSION_INFO(), getModel().createLiteral( info ) );
    }

    /**
     * <p>Answer the version information string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return A version info string
     * @exception ProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.
     */
    @Override
    public String getVersionInfo() {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        try {
            return getRequiredProperty( getProfile().VERSION_INFO() ).getString();
        }
        catch (PropertyNotFoundException ignore) {
            return null;
        }
    }

    /**
     * <p>Answer an iterator over all of the version info strings for this resource.</p>
     * @return An iterator over the version info strings for this resource.
     * @exception ProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<String> listVersionInfo() {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        return WrappedIterator.create( listProperties( getProfile().VERSION_INFO() ) )
               .mapWith( new ObjectAsStringMapper() );
    }

    /**
     * <p>Answer true if this resource has the given version information</p>
     * @param info Version information to test for
     * @return True if this resource has <code>info</code> as version information.
     */
    @Override
    public boolean hasVersionInfo( String info ) {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        return hasProperty( getProfile().VERSION_INFO(), info );
    }

    /**
     * <p>Remove the statement that the given string provides version information about
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param info A version information string to be removed
     */
    @Override
    public void removeVersionInfo( String info ) {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        Literal infoAsLiteral = ResourceFactory.createPlainLiteral( info );
        getModel().remove( this, getProfile().VERSION_INFO(), infoAsLiteral );
    }

    // label

    /**
     * <p>Assert that the given string is the value of the label for this resource. Any existing
     * statements for <code>label</code> will be removed.</p>
     * @param label The label for this resource
     * @param lang The language attribute for this label (EN, FR, etc) or null if not specified.
     * @exception ProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.
     */
    @Override
    public void setLabel( String label, String lang ) {
        checkProfile( getProfile().LABEL(), "LABEL" );
        removeAll( getProfile().LABEL() );
        addLabel( label, lang );
    }

    /**
     * <p>Add the given label to this resource.</p>
     * @param label A label string for this resource
     * @param lang The language attribute for this label (EN, FR, etc) or null if not specified.
     * @exception ProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.
     */
    @Override
    public void addLabel( String label, String lang ) {
        addLabel( getModel().createLiteral( label, lang ) );
    }

    /**
     * <p>Add the given label to this resource.</p>
     * @param label The literal label
     * @exception ProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.
     */
    @Override
    public void addLabel( Literal label ) {
        addPropertyValue( getProfile().LABEL(), "LABEL", label );
    }

    /**
     * <p>Answer the label string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @param lang The language attribute for the desired label (EN, FR, etc) or null for don't care. Will
     * attempt to retreive the most specific label matching the given language</p>
     * @return A label string matching the given language, or null if there is no matching label.
     * @exception ProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.
     */
    @Override
    public String getLabel( String lang ) {
        checkProfile( getProfile().LABEL(), "LABEL" );
        if (lang == null || lang.length() == 0) {
            // don't care which language version we get
            try {
                return getRequiredProperty( getProfile().LABEL() ).getString();
            }
            catch (PropertyNotFoundException ignore) {
                return null;
            }
        }
        else {
            // search for the best match for the specified language
            return selectLang( listProperties( getProfile().LABEL() ), lang );
        }
    }

    /**
     * <p>Answer an iterator over all of the label literals for this resource.</p>
     * @param lang The language to restrict any label values to, or null to select all languages
     * @return An iterator over RDF {@link Literal}'s.
     * @exception ProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<RDFNode> listLabels( String lang ) {
        checkProfile( getProfile().LABEL(), "LABEL" );
        return WrappedIterator.create( listProperties( getProfile().LABEL() ) )
               .filterKeep( new LangTagFilter( lang ) )
               .mapWith( new ObjectMapper() );
    }

    /**
     * <p>Answer true if this resource has the given label</p>
     * @param label The label to test for
     * @param lang The optional language tag, or null for don't care.
     * @return True if this resource has <code>label</code> as a label.
     */
    @Override
    public boolean hasLabel( String label, String lang ) {
        return hasLabel( getModel().createLiteral( label, lang ) );
    }

    /**
     * <p>Answer true if this resource has the given label</p>
     * @param label The label to test for
     * @return True if this resource has <code>label</code> as a label.
     */
    @Override
    public boolean hasLabel( Literal label ) {
        boolean found = false;

        ExtendedIterator<RDFNode> i = listLabels( label.getLanguage() );
        while (!found && i.hasNext()) {
            found = label.equals( i.next() );
        }

        i.close();
        return found;
    }

    /**
     * <p>Remove the statement that the given string is a label for
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param label A label string to be removed
     * @param lang A lang tag
     */
    @Override
    public void removeLabel( String label, String lang ) {
        removeLabel( getModel().createLiteral( label, lang ) );
    }

    /**
     * <p>Remove the statement that the given string is a label for
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param label A label literal to be removed
     */
    @Override
    public void removeLabel( Literal label ) {
        removePropertyValue( getProfile().LABEL(), "LABEL", label );
    }

    // comment

    /**
     * <p>Assert that the given string is the comment on this resource. Any existing
     * statements for <code>comment</code> will be removed.</p>
     * @param comment The comment for this resource
     * @param lang The language attribute for this comment (EN, FR, etc) or null if not specified.
     * @exception ProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.
     */
    @Override
    public void setComment( String comment, String lang ) {
        checkProfile( getProfile().COMMENT(), "COMMENT" );
        removeAll( getProfile().COMMENT() );
        addComment( comment, lang );
    }

    /**
     * <p>Add the given comment to this resource.</p>
     * @param comment A comment string for this resource
     * @param lang The language attribute for this comment (EN, FR, etc) or null if not specified.
     * @exception ProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.
     */
    @Override
    public void addComment( String comment, String lang ) {
        addComment( getModel().createLiteral( comment, lang ) );
    }

    /**
     * <p>Add the given comment to this resource.</p>
     * @param comment The literal comment
     * @exception ProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.
     */
    @Override
    public void addComment( Literal comment ) {
        checkProfile( getProfile().COMMENT(), "COMMENT" );
        addProperty( getProfile().COMMENT(), comment );
    }

    /**
     * <p>Answer the comment string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @param lang The language attribute for the desired comment (EN, FR, etc) or null for don't care. Will
     * attempt to retreive the most specific comment matching the given language</p>
     * @return A comment string matching the given language, or null if there is no matching comment.
     * @exception ProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.
     */
    @Override
    public String getComment( String lang ) {
        checkProfile( getProfile().COMMENT(), "COMMENT" );
        if (lang == null) {
            // don't care which language version we get
            try {
                return getRequiredProperty( getProfile().COMMENT() ).getString();
            }
            catch (PropertyNotFoundException ignore) {
                // no comment :-)
                return null;
            }
        }
        else {
            // search for the best match for the specified language
            return selectLang( listProperties( getProfile().COMMENT() ), lang );
        }
    }

    /**
     * <p>Answer an iterator over all of the comment literals for this resource.</p>
     * @return An iterator over RDF {@link Literal}'s.
     * @exception ProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<RDFNode> listComments( String lang ) {
        checkProfile( getProfile().COMMENT(), "COMMENT" );
        return WrappedIterator.create( listProperties( getProfile().COMMENT() ) )
               .filterKeep( new LangTagFilter( lang ) )
               .mapWith( new ObjectMapper() );
    }

    /**
     * <p>Answer true if this resource has the given comment.</p>
     * @param comment The comment to test for
     * @param lang The optional language tag, or null for don't care.
     * @return True if this resource has <code>comment</code> as a comment.
     */
    @Override
    public boolean hasComment( String comment, String lang ) {
        return hasComment( getModel().createLiteral( comment, lang ) );
    }

    /**
     * <p>Answer true if this resource has the given comment.</p>
     * @param comment The comment to test for
     * @return True if this resource has <code>comment</code> as a comment.
     */
    @Override
    public boolean hasComment( Literal comment ) {
        boolean found = false;

        ExtendedIterator<RDFNode> i = listComments( comment.getLanguage() );
        while (!found && i.hasNext()) {
            found = comment.equals( i.next() );
        }

        i.close();
        return found;
    }

    /**
     * <p>Remove the statement that the given string is a comment on
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param comment A comment string to be removed
     * @param lang A lang tag
     */
    @Override
    public void removeComment( String comment, String lang ) {
        removeComment( getModel().createLiteral( comment, lang ) );
    }

    /**
     * <p>Remove the statement that the given string is a comment on
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param comment A comment literal to be removed
     */
    @Override
    public void removeComment( Literal comment ) {
        removePropertyValue( getProfile().COMMENT(), "COMMENT", comment );
    }


    // rdf:type

    /**
     * <p>Set the RDF type (i.e. the class) for this resource, replacing any
     * existing <code>rdf:type</code> property. Any existing statements for the RDF type
     * will first be removed.</p>
     *
     * @param cls The RDF resource denoting the new value for the <code>rdf:type</code> property,
     *                 which will replace any existing type property.
     */
    @Override
    public void setRDFType( Resource cls ) {
        setPropertyValue( RDF.type, "rdf:type", cls );
    }

    /**
     * <p>Add the given class as one of the <code>rdf:type</code>'s for this resource.</p>
     *
     * @param cls An RDF resource denoting a new value for the <code>rdf:type</code> property.
     */
    @Override
    public void addRDFType( Resource cls ) {
        addPropertyValue( RDF.type, "rdf:type", cls );
    }

    /**
     * <p>
     * Answer the <code>rdf:type</code> (ie the class) of this resource. If there
     * is more than one type for this resource, the return value will be one of
     * the values, but it is not specified which one (nor that it will consistently
     * be the same one each time). Equivalent to <code>getRDFType( false )</code>.
     * </p>
     *
     * @return A resource that is the rdf:type for this resource, or one of them if
     * more than one is defined.
     */
    @Override
    public Resource getRDFType() {
        return getRDFType( false );
    }

    /**
     * <p>
     * Answer the <code>rdf:type</code> (ie the class) of this resource. If there
     * is more than one type for this resource, the return value will be one of
     * the values, but it is not specified which one (nor that it will consistently
     * be the same one each time).
     * </p>
     *
     * @param direct If true, only consider the direct types of this resource, and not
     * the super-classes of the type(s).
     * @return A resource that is the rdf:type for this resource, or one of them if
     * more than one is defined.
     */
    @Override
    public Resource getRDFType( boolean direct ) {
        ExtendedIterator<Resource> i = null;
        try {
            i = listRDFTypes( direct );
            return i.hasNext() ? i.next(): null;
        }
        finally {
            if ( i != null ) i.close();
        }
    }

    /**
     * <p>
     * Answer an iterator over the RDF classes to which this resource belongs.
     * </p>
     *
     * @param direct If true, only answer those resources that are direct types
     * of this resource, not the super-classes of the class etc.
     * @return An iterator over the set of this resource's classes, each of which
     * will be a {@link Resource}.
     */
    @Override
    public ExtendedIterator<Resource> listRDFTypes( boolean direct ) {
        ExtendedIterator<Resource> i = listDirectPropertyValues( RDF.type, "rdf:type", Resource.class, getProfile().SUB_CLASS_OF(), direct, false );
        // we only want each result once
        return i.filterKeep( new UniqueFilter<Resource>());
    }

    /**
     * <p>
     * Answer true if this resource is a member of the class denoted by the
     * given URI.</p>
     *
     * @param uri Denotes the URI of a class to which this value may belong
     * @return True if this resource has the given class as one of its <code>rdf:type</code>'s.
     */
    @Override
    public boolean hasRDFType( String uri ) {
        return hasRDFType( getModel().getResource( uri ) );
    }

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
     */
    @Override
    public boolean hasRDFType( Resource ontClass ) {
        return hasRDFType( ontClass, "unknown", false );
    }

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
     */
    @Override
    public boolean hasRDFType( Resource ontClass, boolean direct ) {
        return hasRDFType( ontClass, "unknown", direct );
    }

    protected boolean hasRDFType( Resource ontClass, String name, boolean direct ) {
        checkProfile( ontClass, name );

        if (!direct) {
            // just an ordinary query - we can answer this directly (more efficient)
            return hasPropertyValue( RDF.type, "rdf:type", ontClass );
        }
        else {
            // need the direct version - not so efficient
            ExtendedIterator<Resource> i = null;
            try {
                i = listRDFTypes( true );
                while (i.hasNext()) {
                    if (ontClass.equals( i.next() )) {
                        return true;
                    }
                }

                return false;
            }
            finally {
                if ( i != null ) i.close();
            }
        }
    }

    /**
     * <p>Remove the statement that this resource is of the given RDF type.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A resource denoting a class that that is to be removed from the classes of this resource
     */
    @Override
    public void removeRDFType( Resource cls ) {
        removePropertyValue( RDF.type, "rdf:type", cls );
    }

    // utility methods

    /**
     * <p>Answer the cardinality of the given property on this resource. The cardinality
     * is the number of distinct values there are for the property.</p>
     * @param p A property
     * @return The cardinality for the property <code>p</code> on this resource, as an
     * integer greater than or equal to zero.
     */
    @Override
    public int getCardinality( Property p ) {
        int n = 0;
        for (Iterator<RDFNode> i =listPropertyValues( p ).filterKeep( new UniqueFilter<RDFNode>());  i.hasNext(); n++) {
        	  i.next();
        }

        return n;
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
    @Override
    public void setPropertyValue( Property property, RDFNode value ) {
        // if there is an existing property, remove it
        removeAll( property );

        // now set the new value
        if (value != null) {
            addProperty( property, value );
        }
    }


    /**
     * <p>Answer the value of a given RDF property for this ontology resource, or null
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
    @Override
    public RDFNode getPropertyValue( Property property ) {
        Statement s = getProperty( property );
        if (s == null) {
            return null;
        }
        else {
            return asOntResource( s.getObject() );
        }
    }


    /**
     * <p>Answer an iterator over the set of all values for a given RDF property. Each
     * value in the iterator will be an RDFNode, representing the value (object) of
     * each statement in the underlying model.</p>
     *
     * @param property The property whose values are sought
     * @return An Iterator over the values of the property
     */
    @Override
    public NodeIterator listPropertyValues( Property property ) {
        return new NodeIteratorImpl( listProperties( property ).mapWith( new ObjectAsOntResourceMapper() ), null );
    }

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
    @Override
    public void remove() {
        Set<Statement> stmts = new HashSet<>();
        List<Resource> lists = new ArrayList<>();
        List<Statement> skip = new ArrayList<>();
        Property first = getProfile().FIRST();

        // collect statements mentioning this object
        for (StmtIterator i = listProperties();  i.hasNext(); ) {
            stmts.add( i.next() );
        }
        for (StmtIterator i = getModel().listStatements( null, null, this ); i.hasNext(); ) {
            stmts.add( i.next() );
        }

        // check for lists
        for ( Statement s : stmts )
        {
            if ( s.getPredicate().equals( first ) && s.getObject().equals( this ) )
            {
                // _this_ is referenced from inside a list
                // we don't delete this reference, since it would make the list ill-formed
                log.debug( toString() + " is referened from an RDFList, so will not be fully removed" );
                skip.add( s );
            }
            else if ( s.getObject() instanceof Resource )
            {
                // check for list-valued properties
                Resource obj = s.getResource();
                if ( obj.canAs( RDFList.class ) )
                {
                    // this value is a list, so we will want to remove all of the elements
                    lists.add( obj );
                }
            }
        }

        // add in the contents of the lists to the statements to be removed
        for ( Resource r : lists )
        {
            stmts.addAll( ( (RDFListImpl) r.as( RDFList.class ) ).collectStatements() );
        }

        // skip the contents of the skip list
        stmts.removeAll( skip );

        // and then remove the remainder
        for ( Statement stmt : stmts )
        {
            stmt.remove();
        }
    }


    /**
     * <p>Remove the specific RDF property-value pair from this DAML resource.</p>
     *
     * @param property The property to be removed
     * @param value The specific value of the property to be removed
     */
    @Override
    public void removeProperty( Property property, RDFNode value ) {
        getModel().remove( this, property, value );
    }


    /**
     * <p>Answer a view of this resource as an annotation property</p>
     * @return This resource, but viewed as an AnnotationProperty
     * @exception ConversionException if the resource cannot be converted to an annotation property
     */
    @Override
    public AnnotationProperty asAnnotationProperty() {
        return as( AnnotationProperty.class );
    }

    /**
     * <p>Answer a view of this resource as a property</p>
     * @return This resource, but viewed as an OntProperty
     * @exception ConversionException if the resource cannot be converted to a property
     */
    @Override
    public OntProperty asProperty() {
        return as( OntProperty.class );
    }

    /**
     * <p>Answer a view of this resource as an object property</p>
     * @return This resource, but viewed as an ObjectProperty
     * @exception ConversionException if the resource cannot be converted to an object property
     */
    @Override
    public ObjectProperty asObjectProperty() {
        return as( ObjectProperty.class );
    }

    /**
     * <p>Answer a view of this resource as a datatype property</p>
     * @return This resource, but viewed as a DatatypeProperty
     * @exception ConversionException if the resource cannot be converted to a datatype property
     */
    @Override
    public DatatypeProperty asDatatypeProperty() {
        return as( DatatypeProperty.class );
    }

    /**
     * <p>Answer a view of this resource as an individual</p>
     * @return This resource, but viewed as an Individual
     * @exception ConversionException if the resource cannot be converted to an individual
     */
    @Override
    public Individual asIndividual() {
        return as( Individual.class );
    }

    /**
     * <p>Answer a view of this resource as a class</p>
     * @return This resource, but viewed as an OntClass
     * @exception ConversionException if the resource cannot be converted to a class
     */
    @Override
    public OntClass asClass() {
        return as( OntClass.class );
    }

    /**
     * <p>Answer a view of this resource as an ontology description node</p>
     * @return This resource, but viewed as an Ontology
     * @exception ConversionException if the resource cannot be converted to an ontology description node
     */
    @Override
    public Ontology asOntology() {
        return as( Ontology.class );
    }

    /**
     * <p>Answer a view of this resource as an 'all different' declaration</p>
     * @return This resource, but viewed as an AllDifferent node
     * @exception ConversionException if the resource cannot be converted to an all different declaration
     */
    @Override
    public AllDifferent asAllDifferent() {
        return as( AllDifferent.class );
    }

    /**
     * <p>Answer a view of this resource as a data range</p>
     * @return This resource, but viewed as a DataRange
     * @exception ConversionException if the resource cannot be converted to a data range
     */
    @Override
    public DataRange asDataRange() {
        return as( DataRange.class );
    }


    // Conversion test methods

    /**
     * <p>Answer true if this resource can be viewed as an annotation property</p>
     * @return True if this resource can be viewed as an AnnotationProperty
     */
    @Override
    public boolean isAnnotationProperty() {
        return getProfile().ANNOTATION_PROPERTY() != null && canAs( AnnotationProperty.class );
    }

    /**
     * <p>Answer true if this resource can be viewed as a property</p>
     * @return True if this resource can be viewed as an OntProperty
     */
    @Override
    public boolean isProperty() {
        return canAs( OntProperty.class );
    }

    /**
     * <p>Answer true if this resource can be viewed as an object property</p>
     * @return True if this resource can be viewed as an ObjectProperty
     */
    @Override
    public boolean isObjectProperty() {
        return getProfile().OBJECT_PROPERTY() != null && canAs( ObjectProperty.class );
    }

    /**
     * <p>Answer true if this resource can be viewed as a datatype property</p>
     * @return True if this resource can be viewed as a DatatypeProperty
     */
    @Override
    public boolean isDatatypeProperty() {
        return getProfile().DATATYPE_PROPERTY() != null && canAs( DatatypeProperty.class );
    }

    /**
     * <p>Answer true if this resource can be viewed as an individual</p>
     * @return True if this resource can be viewed as an Individual
     */
    @Override
    public boolean isIndividual() {
        OntModel m = (getModel() instanceof OntModel) ? (OntModel) getModel() : null;
        if ( m == null )
            return false ;

        // can we use the reasoner's native abilities to do the instance test?
        boolean useInf = false;
        useInf = m.getProfile().THING() != null &&
                 m.getReasoner() != null &&
                 m.getReasoner().supportsProperty( ReasonerVocabulary.individualAsThingP );

        StmtIterator i = null, j = null;
        try {
            if (!useInf) {
                // either not using the OWL reasoner, or not using OWL
                // look for an rdf:type of this resource that is a class
                for (i = listProperties( RDF.type ); i.hasNext(); ) {
                    Resource rType = i.nextStatement().getResource();
                    if (rType.equals( m.getProfile().THING() )) {
                        // the resource has rdf:type owl:Thing (or equivalent)
                        return true;
                    }

                    // if the type itself is OWL.Class or similar, we should ignore it ... this may
                    // arise in cases where the user has materialised results of inference and is then
                    // accessing them from a plain model
                    // JENA-3: we also ignore if the type is rdfs:Resource or similar, since it's not informative
                    if (rType.equals( getProfile().CLASS() ) ||
                        rType.equals( RDFS.Resource ) ||
                        rType.equals( RDF.Property ) ||
                        rType.equals( RDFS.Datatype ) ||
                        rType.equals( RDF.List ))
                    {
                        continue;
                    }

                    // otherwise, we check to see if the given type is known to be a class
                    for (j = rType.listProperties( RDF.type ); j.hasNext(); ) {
                        if (j.nextStatement().getResource().equals( getProfile().CLASS() )) {
                            // we have found an rdf:type of the subject that is an owl, rdfs or daml Class
                            // therefore this is an individual
                            return true;
                        }
                    }
                }

                // apparently not an instance
                return false;
            }
            else {
                // using the rule reasoner on an OWL graph, so we can determine
                // individuals as those things that have rdf:type owl:Thing
                return hasProperty( RDF.type, getProfile().THING() );
            }
        }
        finally {
            if (i != null) { i.close(); }
            if (j != null) { j.close(); }
        }
    }

    /**
     * <p>Answer true if this resource can be viewed as a class</p>
     * @return True if this resource can be viewed as an OntClass
     */
    @Override
    public boolean isClass() {
        return canAs( OntClass.class );
    }

    /**
     * <p>Answer true if this resource can be viewed as an ontology description node</p>
     * @return True if this resource can be viewed as an Ontology
     */
    @Override
    public boolean isOntology() {
        return getProfile().ONTOLOGY() != null && canAs( Ontology.class );
    }

    /**
     * <p>Answer true if this resource can be viewed as a data range</p>
     * @return True if this resource can be viewed as a DataRange
     */
    @Override
    public boolean isDataRange() {
        return getProfile().DATARANGE() != null && canAs( DataRange.class );
    }

    /**
     * <p>Answer true if this resource can be viewed as an 'all different' declaration</p>
     * @return True if this resource can be viewed as an AllDifferent node
     */
    @Override
    public boolean isAllDifferent() {
        return getProfile().ALL_DIFFERENT() != null && canAs( AllDifferent.class );
    }



    // Internal implementation methods
    //////////////////////////////////


    /** Answer true if the node has the given type in the graph */
    protected static boolean hasType( Node n, EnhGraph g, Resource type ) {
        // TODO this method doesn't seem to be called anywhere.
        boolean hasType = false;
        ClosableIterator<Triple> i = g.asGraph().find( n, RDF.type.asNode(), type.asNode() );
        hasType = i.hasNext();
        i.close();
        return hasType;
    }

    /**
     * Throw an exception if a term is not in the profile
     * @param term The term being checked
     * @param name The name of the term
     * @exception ProfileException if term is null (indicating it is not in the profile)
     **/
    protected void checkProfile( Object term, String name ) {
        if (term == null) {
            throw new ProfileException( name, getProfile() );
        }
    }


    /**
     * <p>Answer the literal with the language tag that best matches the required language</p>
     * @param stmts A StmtIterator over the candidates
     * @param lang The language we're searching for, assumed non-null.
     * @return The literal value that best matches the given language tag, or null if there are no matches
     */
    protected String selectLang( StmtIterator stmts, String lang ) {
        String found = null;

        while (stmts.hasNext()) {
            RDFNode n = stmts.nextStatement().getObject();

            if (n instanceof Literal) {
                Literal l = (Literal) n;
                String lLang = l.getLanguage();

                // is this a better match?
                if (lang.equalsIgnoreCase( lLang )) {
                    // exact match
                    found = l.getString();
                    break;
                }
                else if (lLang != null && lLang.length() > 1 && lang.equalsIgnoreCase( lLang.substring( 0, 2 ) )) {
                    // partial match - want EN, found EN-GB
                    // keep searching in case there's a better
                    found = l.getString();
                }
                else if (found == null && lLang == null) {
                    // found a string with no (i.e. default) language - keep this unless we've got something better
                    found = l.getString();
                }
            }
        }

        stmts.close();
        return found;
    }

    /** Answer true if the desired lang tag matches the target lang tag */
    protected boolean langTagMatch( String desired, String target ) {
        return (desired == null) ||
               (desired.equalsIgnoreCase( target )) ||
               (target.length() > desired.length() && desired.equalsIgnoreCase( target.substring( desired.length() ) ));
    }

    /** Answer the object of a statement with the given property, .as() the given class */
    protected <T extends RDFNode> T objectAs( Property p, String name, Class<T> asClass ) {
        checkProfile( p, name );
        try {
            return getRequiredProperty( p ).getObject().as( asClass );
        }
        catch (PropertyNotFoundException e) {
            return null;
        }
    }


    /** Answer the object of a statement with the given property, .as() an OntResource */
    protected OntResource objectAsResource( Property p, String name ) {
        return objectAs( p, name, OntResource.class );
    }


    /** Answer the object of a statement with the given property, .as() an OntProperty */
    protected OntProperty objectAsProperty( Property p, String name ) {
        return objectAs( p, name, OntProperty.class );
    }


    /** Answer the int value of a statement with the given property */
    protected int objectAsInt( Property p, String name ) {
        checkProfile( p, name );
        return getRequiredProperty( p ).getInt();
    }


    /** Answer an iterator for the given property, whose values are .as() some class */
    protected <T extends RDFNode> ExtendedIterator<T> listAs( Property p, String name, Class<T> cls ) {
        checkProfile( p, name );
        return WrappedIterator.create( listProperties( p ) ).mapWith( new ObjectAsMapper<>( cls ) );
    }


    /** Add the property value, checking that it is supported in the profile */
    protected void addPropertyValue( Property p, String name, RDFNode value ) {
        checkProfile( p, name );
        addProperty( p, value );
    }

    /** Set the property value, checking that it is supported in the profile */
    protected void setPropertyValue( Property p, String name, RDFNode value ) {
        checkProfile( p, name );
        removeAll( p );
        addProperty( p, value );
    }

    /** Answer true if the given property is defined in the profile, and has the given value */
    protected boolean hasPropertyValue( Property p, String name, RDFNode value ) {
        checkProfile( p, name );
        return hasProperty( p, value );
    }

    /** Add the given value to a list which is the value of the given property */
    protected void addListPropertyValue( Property p, String name, RDFNode value ) {
        checkProfile( p, name );

        // get the list value
        if (hasProperty( p )) {
            RDFNode cur = getRequiredProperty( p ).getObject();
            if (!cur.canAs( RDFList.class )) {
                throw new OntologyException( "Tried to add a value to a list-valued property " + p +
                                             " but the current value is not a list: " + cur );
            }

            RDFList values = cur.as( RDFList.class );

            // now add our value to the list
            if (!values.contains( value )){
                RDFList newValues = values.with( value );

                // if the previous values was nil, the return value will be a new list
                if (newValues != values) {
                    removeAll( p );
                    addProperty( p, newValues );
                }
            }
        }
        else {
            // create a new list to hold the only value we know so far
            addProperty( p, ((OntModel) getModel()).createList( new RDFNode[] {value} ) );
        }
    }

    /** Convert this resource to the facet denoted by cls, by adding rdf:type type if necessary */
    protected <T extends RDFNode> T convertToType( Resource type, String name, Class<T> cls ) {
        checkProfile( type, name );
        if (canAs( cls )) {
            // don't need to update the model, we already can do the given facet
            return as( cls );
        }

        // we're told that adding this rdf:type will make the as() possible - let's see
        addProperty( RDF.type, type );
        return as( cls );
    }

    /**
     * <p>Return an iterator of values, respecting the 'direct' modifier</p>
     * @param p The property whose values are required
     * @param name The short name of the property (for generating error messages)
     * @param cls Class object denoting the facet to map the returned values to
     * @param orderRel If direct, and we are not using an inference engine, this is the property
     *                 to use to define the maximal lower elements of the partial order
     * @param direct If true, only return the direct (adjacent) values
     * @param inverse If true, use the inverse of p rather than p
     * @return An iterator of nodes that are in relation p to this resource (possibly inverted), which
     * have been mapped to the facet denoted by <code>cls</code>.
     */
    protected <T extends Resource> ExtendedIterator<T> listDirectPropertyValues( Property p, String name, Class<T> cls, Property orderRel, boolean direct, boolean inverse ) {
        Iterator<T> i = null;
        checkProfile( p, name );

        Property sc = p;

        // check for requesting direct versions of these properties
        if (direct) {
            sc = getModel().getProperty( ReasonerRegistry.makeDirect( sc.asNode() ).getURI() );
        }

        // determine the subject and object pairs for the list statements calls
        Resource subject = inverse ? null : this;
        Resource object  = inverse ? this : null;
        Map1<Statement, T> mapper      = inverse ? new SubjectAsMapper<>( cls ) : new ObjectAsMapper<>( cls );

        // are we working on an inference graph?
        OntModel m = (OntModel) getGraph();
        InfGraph ig = null;
        if (m.getGraph() instanceof InfGraph) {
            ig = (InfGraph) m.getGraph();
        }

        // can we go direct to the graph?
        if (!direct || ((ig != null) && ig.getReasoner().supportsProperty( sc ))) {
            // either not direct, or the direct sc property is supported
            // ensure we have an extended iterator of statements  this rdfs:subClassOf _x
            // NB we only want the subjects or objects of the statements
            i = getModel().listStatements( subject, sc, object ).mapWith( mapper );
        }
        else {
            i = computeDirectValues( p, orderRel, inverse, subject, object, mapper );
        }

        return WrappedIterator.create(i).filterKeep( new UniqueFilter<T>());
    }


    /**
     * <p>In the absence of a reasoner that can compute direct (adjacent) property values,
     * we must perform the calculation of the direct values computationally here.</p>
     * @param p
     * @param orderRel
     * @param inverse
     * @param subject
     * @param object
     * @param mapper
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T extends Resource> Iterator<T> computeDirectValues( Property p, Property orderRel, boolean inverse, Resource subject, Resource object, Map1<Statement, T> mapper ) {
        // graph does not support direct directly
        ExtendedIterator<T> j = getModel().listStatements( subject, p, object )
                                       .mapWith( mapper );

        // collect a list of the candidates
        List<T> s = new ArrayList<>();
        for( ; j.hasNext(); ) {
            s.add( j.next() );
        }

        // we need to keep this node out of the iterator for now, else it will spoil the maximal
        // generator compression (since all the (e.g.) sub-classes will be sub-classes of this node
        // and so will be excluded from the maximal lower elements calculation)
        ResourceUtils.removeEquiv( s, orderRel, this );
        boolean withheld = s.remove( this );

        // we now compress the list by reducing all equivalent values to a single representative

        // first partition the list by equivalence under orderRel
        List<List<T>> partition = ResourceUtils.partition( s, orderRel );
        Map<Resource, List<T>> equivSets = new HashMap<>();

        // then reduce each part of the partition to a singleton, but remember the others
        s.clear();
        for ( List<T> part : partition )
        {
            // if this is a singleton we just add it to the compressed candidates
            if ( part.size() == 1 )
            {
                s.add( part.get( 0 ) );
            }
            else
            {
                // we select a single representative
                T r = part.remove( 0 );
                // remember the other equivalent values
                equivSets.put( r, part );
                s.add( r );
            }
        }

        // now s1 contains a reduced set of nodes, in which any fully-connected sub-graph under
        // orderRel has been reduced to a single representative

        // generate the short list as the maximal bound under the given partial order
        s = ResourceUtils.maximalLowerElements( s, orderRel, inverse );

        // create a list of these values lower elements, plus their equivalents (if any)
        List<T> s2 = new ArrayList<>();
        for ( T r : s )
        {
            s2.add( r );
            if ( equivSets.containsKey( r ) )
            {
                s2.addAll( equivSets.get( r ) );
            }
        }

        // put myself back if needed
        if (withheld) {
            s2.add( (T) this );
        }

        return s2.iterator();
    }


    /** Remove a specified property-value pair, if it exists */
    protected void removePropertyValue( Property prop, String name, RDFNode value ) {
        checkProfile( prop, name );
        getModel().remove( this, prop, value );
    }

    /** Answer the given node presenting the OntResource facet if it can */
    private static RDFNode asOntResource( RDFNode n ) {
        return n.isResource() ? n.as( OntResource.class ) : n;
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /** Implementation of Map1 that performs as( Class ) for a given class, against an argument {@link RDFNode} */
    protected static class AsMapper<T extends RDFNode> implements Map1<RDFNode, T>
    {
        private Class<T> m_as;
        public AsMapper( Class<T> as ) { m_as = as; }
        @Override
        public T map1( RDFNode x ) { return x.as( m_as ); }
    }

    /** Implementation of Map1 that performs as( Class ) for a given class, against an argument {@link Resource} */
    protected static class ResourceAsMapper<T extends RDFNode> implements Map1<Resource, T>
    {
        private Class<T> m_as;
        public ResourceAsMapper( Class<T> as ) { m_as = as; }
        @Override
        public T map1( Resource x ) { return x.as( m_as ); }
    }

    /** Implementation of Map1 that performs as( Class ) for a given class, on the subject of a statement */
    protected static class SubjectAsMapper<T extends RDFNode> implements Map1<Statement, T>
    {
        private Class<T> m_as;
        public SubjectAsMapper( Class<T> as ) { m_as = as; }
        @Override
        public T map1( Statement x ) {
            return x.getSubject().as( m_as );
        }
    }

    /** Implementation of Map1 that extracts the subject of a statement */
    protected static class SubjectMapper implements Map1<Statement, Resource>
    {
        @Override
        public Resource map1( Statement x ) {
            return x.getSubject();
        }
    }

    /** Implementation of Map1 that performs as( Class ) for a given class, on the object of a statement */
    protected static class ObjectAsMapper<T extends RDFNode> implements Map1<Statement, T>
    {
        private Class<T> m_as;
        public ObjectAsMapper( Class<T> as )
            { m_as = as; }
        @Override
        public T map1( Statement x ) {
            return x.getObject().as( m_as );
        }
    }

    /** Implementation of Map1 that performs getString on the object of a statement */
    protected class ObjectAsStringMapper implements Map1<Statement, String>
    {
        @Override
        public String map1( Statement x ) { return x.getString(); }
    }

    /** Implementation of Map1 that returns the object of a statement */
    protected static class ObjectMapper implements Map1<Statement, RDFNode>
    {
        @Override
        public RDFNode map1( Statement x ) { return x.getObject(); }
    }

    /** Implementation of Map1 that returns the object of a statement as an ont resource */
    protected static class ObjectAsOntResourceMapper implements Map1<Statement, RDFNode>
    {
        @Override
        public RDFNode map1( Statement x ) {
            return asOntResource( x.getObject() );
        }
    }

    /** Filter for matching language tags on the objects of statements */
    protected class LangTagFilter extends Filter<Statement>
    {
        protected String m_lang;
        public LangTagFilter( String lang ) { m_lang = lang; }
        @Override
        public boolean accept( Statement x ) {
            RDFNode o = x.getObject();
            return o.isLiteral() && langTagMatch( m_lang, ((Literal) o).getLanguage() );
        }
    }

    /** Filter for accepting only the given value, based on .equals() */
    protected class SingleEqualityFilter<T>
        extends Filter<T>
    {
        private T m_obj;
        public SingleEqualityFilter( T x ) { m_obj = x; }
        @Override public boolean accept( T x ) {return m_obj.equals( x );}
    }
}

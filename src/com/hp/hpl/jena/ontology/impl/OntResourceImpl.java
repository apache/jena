/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            25-Mar-2003
 * Filename           $RCSfile: OntResourceImpl.java,v $
 * Revision           $Revision: 1.41 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-08 10:48:24 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.path.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.NodeIteratorImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>
 * Abstract base class to provide shared implementation for implementations of ontology
 * resources.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntResourceImpl.java,v 1.41 2003-12-08 10:48:24 andy_seaborne Exp $
 */
public class OntResourceImpl
    extends ResourceImpl
    implements OntResource 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////
    
    /**
     * A factory for generating OntResource facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new OntResourceImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to OntResource");
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an OntResource facet if it is a uri or bnode
            return node.isURI() || node.isBlank();
        }
    };


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


    // sameAs
    
    /**
     * <p>Assert equivalence between the given resource and this resource. Any existing 
     * statements for <code>sameAs</code> will be removed.</p>
     * @param res The resource that is declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public void setSameAs( Resource res ) {
        setPropertyValue( getProfile().SAME_AS(), "SAME_AS", res );
    }

    /**
     * <p>Add a resource that is declared to be equivalent to this resource.</p>
     * @param res A resource that declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public void addSameAs( Resource res ) {
        addPropertyValue( getProfile().SAME_AS(), "SAME_AS", res );
    }

    /**
     * <p>Answer a resource that is declared to be the same as this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public OntResource getSameAs() {
        return objectAsResource( getProfile().SAME_AS(), "SAME_AS" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to be the same as
     * this resource. Each elemeent of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the resources equivalent to this resource.
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listSameAs() {
        return listAs( getProfile().SAME_AS(), "SAME_AS", OntResource.class );
    }

    /**
     * <p>Answer true if this resource is the same as the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared the same via a <code>sameAs</code> statement.
     */
    public boolean isSameAs( Resource res ) {
        return hasPropertyValue( getProfile().SAME_AS(), "SAME_AS", res );
    }

    /**
     * <p>Remove the statement that this resource is the same as the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to be the sameAs this resource
     */
    public void removeSameAs( Resource res ) {
        removePropertyValue( getProfile().SAME_AS(), "SAME_AS", res );
    }
    
    // differentFrom
    
    /**
     * <p>Assert that the given resource and this resource are distinct. Any existing 
     * statements for <code>differentFrom</code> will be removed.</p>
     * @param res The resource that is declared to be distinct from this resource
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public void setDifferentFrom( Resource res ) {
        setPropertyValue( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", res );
    }

    /**
     * <p>Add a resource that is declared to be equivalent to this resource.</p>
     * @param res A resource that declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public void addDifferentFrom( Resource res ) {
        addPropertyValue( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", res );
    }

    /**
     * <p>Answer a resource that is declared to be distinct from this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that declared to be different from this resource
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public OntResource getDifferentFrom() {
        return objectAsResource( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to be different from
     * this resource. Each elemeent of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the resources different from this resource.
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listDifferentFrom() {
        return listAs( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", OntResource.class );
    }

    /**
     * <p>Answer true if this resource is different from the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared to be distinct via a <code>differentFrom</code> statement.
     */
    public boolean isDifferentFrom( Resource res ) {
        return hasPropertyValue( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", res );
    }
    
    /**
     * <p>Remove the statement that this resource is different the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to be differentFrom this resource
     */
    public void removeDifferentFrom( Resource res ) {
        removePropertyValue( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", res );
    }
    
    // seeAlso
    
    /**
     * <p>Assert that the given resource provides additional information about the definition of this resource</p>
     * @param res A resource that can provide additional information about this resource
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public void setSeeAlso( Resource res ) {
        setPropertyValue( getProfile().SEE_ALSO(), "SEE_ALSO", res );
    }

    /**
     * <p>Add a resource that is declared to provided additional information about the definition of this resource</p>
     * @param res A resource that provides extra information on this resource
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public void addSeeAlso( Resource res ) {
        addPropertyValue( getProfile().SEE_ALSO(), "SEE_ALSO", res );
    }

    /**
     * <p>Answer a resource that provides additional information about this resource. If more than one such resource
     * is defined, make an arbitrary choice.</p>
     * @return res A resource that provides additional information about this resource
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public Resource getSeeAlso() {
        return objectAsResource( getProfile().SEE_ALSO(), "SEE_ALSO" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to provide addition
     * information about this resource.</p>
     * @return An iterator over the resources providing additional definition on this resource.
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listSeeAlso() {
        checkProfile( getProfile().SEE_ALSO(), "SEE_ALSO" );
        return WrappedIterator.create( listProperties( getProfile().SEE_ALSO() ) )
               .mapWith( new ObjectMapper() );
    }

    /**
     * <p>Answer true if this resource has the given resource as a source of additional information.</p>
     * @param res A resource to test against
     * @return True if the <code>res</code> provides more information on this resource.
     */
    public boolean hasSeeAlso( Resource res ) {
        return hasPropertyValue( getProfile().SEE_ALSO(), "SEE_ALSO", res );
    }
    
    /**
     * <p>Remove the statement indicating the given resource as a source of additional information
     * about this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to provide additional information about this resource
     */
    public void removeSeeAlso( Resource res ) {
        removePropertyValue( getProfile().SEE_ALSO(), "SEE_ALSO", res );
    }
    
    // is defined by
    
    /**
     * <p>Assert that the given resource provides a source of definitions about this resource. Any existing 
     * statements for <code>isDefinedBy</code> will be removed.</p>
     * @param res The resource that is declared to be a definition of this resource.
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public void setIsDefinedBy( Resource res ) {
        setPropertyValue( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY", res );
    }

    /**
     * <p>Add a resource that is declared to provide a definition of this resource.</p>
     * @param res A defining resource 
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public void addIsDefinedBy( Resource res ) {
        addPropertyValue( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY", res );
    }

    /**
     * <p>Answer a resource that is declared to provide a definition of this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that is declared to provide a definition of this resource
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public Resource getIsDefinedBy() {
        return objectAsResource( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to define
     * this resource. </p>
     * @return An iterator over the resources defining this resource.
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listIsDefinedBy() {
        checkProfile( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY" );
        return WrappedIterator.create( listProperties( getProfile().IS_DEFINED_BY() ) )
               .mapWith( new ObjectMapper() );
    }

    /**
     * <p>Answer true if this resource is defined by the given resource.</p>
     * @param res A resource to test against
     * @return True if <code>res</code> defines this resource.
     */
    public boolean isDefinedBy( Resource res ) {
        return hasPropertyValue( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY", res );
    }
    
    /**
     * <p>Remove the statement that this resource is defined by the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that may be declared to define this resource
     */
    public void removeDefinedBy( Resource res ) {
        removePropertyValue( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY", res );
    }
    

    // version info

    /**
     * <p>Assert that the given string is the value of the version info for this resource. Any existing 
     * statements for <code>versionInfo</code> will be removed.</p>
     * @param info The version information for this resource
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
    public void setVersionInfo( String info ) {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        removeAll( getProfile().VERSION_INFO() );
        addVersionInfo( info );
    }

    /**
     * <p>Add the given version information to this resource.</p>
     * @param info A version information string for this resource 
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
    public void addVersionInfo( String info ) {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        addProperty( getProfile().VERSION_INFO(), getModel().createLiteral( info ) );
    }

    /**
     * <p>Answer the version information string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return A version info string
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
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
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listVersionInfo() {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        return WrappedIterator.create( listProperties( getProfile().VERSION_INFO() ) )
               .mapWith( new ObjectAsStringMapper() );
    }

    /**
     * <p>Answer true if this resource has the given version information</p>
     * @param info Version information to test for
     * @return True if this resource has <code>info</code> as version information.
     */
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
    public void removeVersionInfo( String info ) {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        
        StmtIterator i = getModel().listStatements( this, getProfile().VERSION_INFO(), info );
        if (i.hasNext()) {
            i.nextStatement().remove();
        }
        
        i.close();
    }
    
    // label
    
    /**
     * <p>Assert that the given string is the value of the label for this resource. Any existing 
     * statements for <code>label</code> will be removed.</p>
     * @param label The label for this resource
     * @param lang The language attribute for this label (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public void setLabel( String label, String lang ) {
        checkProfile( getProfile().LABEL(), "LABEL" );
        removeAll( getProfile().LABEL() );
        addLabel( label, lang );
    }

    /**
     * <p>Add the given label to this resource.</p>
     * @param label A label string for this resource
     * @param lang The language attribute for this label (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public void addLabel( String label, String lang ) {
        addLabel( getModel().createLiteral( label, lang ) );
    }

    /**
     * <p>Add the given label to this resource.</p>
     * @param label The literal label
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public void addLabel( Literal label ) {
        addPropertyValue( getProfile().LABEL(), "LABEL", label );
    }

    /**
     * <p>Answer the label string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @param lang The language attribute for the desired label (EN, FR, etc) or null for don't care. Will 
     * attempt to retreive the most specific label matching the given language</p>
     * @return A label string matching the given language, or null if there is no matching label.
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public String getLabel( String lang ) {
        checkProfile( getProfile().LABEL(), "LABEL" );
        if (lang == null) {
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
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listLabels( String lang ) {
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
    public boolean hasLabel( String label, String lang ) {
        return hasLabel( getModel().createLiteral( label, lang ) );
    }
    
    /**
     * <p>Answer true if this resource has the given label</p>
     * @param label The label to test for
     * @return True if this resource has <code>label</code> as a label.
     */
    public boolean hasLabel( Literal label ) {
        boolean found = false;
        
        ExtendedIterator i = listLabels( label.getLanguage() );
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
    public void removeLabel( String label, String lang ) {
        removeLabel( getModel().createLiteral( label, lang ) );
    }
    
    /**
     * <p>Remove the statement that the given string is a label for
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param label A label literal to be removed
     */
    public void removeLabel( Literal label ) {
        removePropertyValue( getProfile().LABEL(), "LABEL", label );
    }
    
    // comment

    /**
     * <p>Assert that the given string is the comment on this resource. Any existing 
     * statements for <code>comment</code> will be removed.</p>
     * @param comment The comment for this resource
     * @param lang The language attribute for this comment (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public void setComment( String comment, String lang ) {
        checkProfile( getProfile().COMMENT(), "COMMENT" );
        removeAll( getProfile().COMMENT() );
        addComment( comment, lang );
    }

    /**
     * <p>Add the given comment to this resource.</p>
     * @param comment A comment string for this resource
     * @param lang The language attribute for this comment (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public void addComment( String comment, String lang ) {
        addComment( getModel().createLiteral( comment, lang ) );
    }

    /**
     * <p>Add the given comment to this resource.</p>
     * @param comment The literal comment
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
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
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
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
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listComments( String lang ) {
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
    public boolean hasComment( String comment, String lang ) {
        return hasComment( getModel().createLiteral( comment, lang ) );
    }
    
    /**
     * <p>Answer true if this resource has the given comment.</p>
     * @param comment The comment to test for
     * @return True if this resource has <code>comment</code> as a comment.
     */
    public boolean hasComment( Literal comment ) {
        boolean found = false;
        
        ExtendedIterator i = listComments( comment.getLanguage() );
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
    public void removeComment( String comment, String lang ) {
        removeComment( getModel().createLiteral( comment, lang ) );
    }
    
    /**
     * <p>Remove the statement that the given string is a comment on
     * this resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param comment A comment literal to be removed
     */
    public void removeComment( Literal comment ) {
        removePropertyValue( getProfile().COMMENT(), "COMMENT", comment );
    }
    
    
    // rdf:type 
    
    /**
     * <p>Set the RDF type (ie the class) for this resource, replacing any
     * existing <code>rdf:type</code> property. Any existing statements for the RDF type
     * will first be removed.</p>
     * 
     * @param cls The RDF resource denoting the new value for the <code>rdf:type</code> property,
     *                 which will replace any existing type property.
     */
    public void setRDFType( Resource cls ) {
        setPropertyValue( RDF.type, "rdf:type", cls );
    }

    /**
     * <p>Add the given class as one of the <code>rdf:type</code>'s for this resource.</p>
     * 
     * @param cls An RDF resource denoting a new value for the <code>rdf:type</code> property.
     */
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
    public Resource getRDFType( boolean direct ) {
        ExtendedIterator i = null;
        try {
            i = listRDFTypes( direct );
            return i.hasNext() ? (Resource) i.next(): null;
        }
        finally {
            i.close();
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
    public ExtendedIterator listRDFTypes( boolean direct ) {
        Iterator i = listDirectPropertyValues( RDF.type, "rdf:type", null, getProfile().SUB_CLASS_OF(), direct, false );
        ExtendedIterator j = WrappedIterator.create( i );
        
        // we only want each result once
        return new UniqueExtendedIterator( j );
    }

    /**
     * <p>
     * Answer true if this resource is a member of the class denoted by the
     * given URI.</p>
     * 
     * @param uri Denotes the URI of a class to which this value may belong
     * @return True if this resource has the given class as one of its <code>rdf:type</code>'s.
     */
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
            ExtendedIterator i = null;
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
                i.close();
            }
        }
    }

    /**
     * <p>Remove the statement that this resource is of the given RDF type.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A resource denoting a class that that is to be removed from the classes of this resource
     */
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
    public int getCardinality( Property p ) {
        int n = 0;
        for (Iterator i = new UniqueExtendedIterator( listProperties( p ) );  i.hasNext(); n++) {
            i.next(); 
        }
        
        return n;
    }
    
    
    /**
     * <p>
     * Answer an {@link PathSet accessor} for the given
     * property of any ontology value. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @param p A property
     * @param name The name of the property, so that an appropriate message can be printed if not in the profile
     * @return An abstract accessor for the property p
     */
    public PathSet accessor( Property p, String name ) {
        return asPathSet( p, name );
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
        return asPathSet( p, "unknown property" );
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
        if (value != null) {
            addProperty( property, value );
        } 
    }


    /**
     * <p>Answer the value of a given RDF property for this DAML value, or null
     * if it doesn't have one.  The value is returned as an RDFNode, from which
     * the value can be extracted for literals.  If there is more than one RDF
     * statement with the given property for the current value, it is not defined
     * which of the values will be returned.</p>
     *
     * @param property An RDF property
     * @return An RDFNode whose value is the value, or one of the values, of the
     *         given property. If the property is not defined, or an error occurs,
     *         returns null.
     */
    public RDFNode getPropertyValue( Property property ) {
        try {
            return getRequiredProperty( property ).getObject();
        }
        catch (PropertyNotFoundException ignore) {
            return null;
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
    public NodeIterator listPropertyValues( Property property ) {
        return new NodeIteratorImpl( listProperties( property ).mapWith( new ObjectMapper() ), null );
    }
    
    /** 
     * <p>Removes this resource from the ontology by deleting any statements that refer to it.
     * If this resource is a property, this method will <strong>not</strong> remove instances
     * of the property from the model.</p>
     */
    public void remove() {
        List stmts = new ArrayList();
        List skip = new ArrayList();
        
        // collect statements mentioning this object
        for (StmtIterator i = listProperties();  i.hasNext();  stmts.add( i.next() ) );
        for (StmtIterator i = getModel().listStatements( null, null, this ); i.hasNext(); stmts.add( i.next() ) );
        
        // check for lists
        for (Iterator i = stmts.iterator(); i.hasNext(); ) {
            Statement s = (Statement) i.next();
            if (s.getPredicate().equals( RDF.first )) {
                // this object is referenced from inside a list
                // we don't delete this, since it would make the list ill-formed
                String me = isAnon() ? ("Anon object " + getId()) : getURI();
                LogFactory.getLog( getClass() ).warn( me + " is referened from an RDFList, so will not be fully removed");
                skip.add( s );
            }
            else if (s.getObject() instanceof Resource){
                // check for list-valued properties
                Resource obj = s.getResource();
                if (obj.hasProperty( RDF.type, RDF.List ) || obj.hasProperty( RDF.first )) {
                    // this value is a list, so remove all of the elements
                    ((RDFList) obj.as( RDFList.class )).removeAll();
                }
            }
        }
        
        // skip the contents of the skip list
        for (Iterator i = skip.iterator(); i.hasNext(); stmts.remove( i.next() ));
        
        // and then remove the remainder
        for (Iterator i = stmts.iterator();  i.hasNext();  ((Statement) i.next()).remove() );
    }
    

    /**
     * <p>Remove the specific RDF property-value pair from this DAML resource.</p>
     *
     * @param property The property to be removed
     * @param value The specific value of the property to be removed
     */
    public void removeProperty( Property property, RDFNode value ) {
        // have to do this in two phases to avoid concurrent modification exception
        Set s = new HashSet();
        for (StmtIterator i = getModel().listStatements( this, property, value ); i.hasNext(); s.add( i.nextStatement() ) );
        for (Iterator i = s.iterator(); i.hasNext(); ((Statement) i.next()).remove() );
    }


    /** 
     * <p>Answer a view of this resource as an annotation property</p>
     * @return This resource, but viewed as an AnnotationProperty
     * @exception ConversionException if the resource cannot be converted to an annotation property
     */
    public AnnotationProperty asAnnotationProperty() {
        return (AnnotationProperty) as( AnnotationProperty.class );
    }
    
    /** 
     * <p>Answer a view of this resource as a property</p>
     * @return This resource, but viewed as an OntProperty
     * @exception ConversionException if the resource cannot be converted to a property
     */
    public OntProperty asProperty() {
        return (OntProperty) as( OntProperty.class );
    }
    
    /** 
     * <p>Answer a view of this resource as an individual</p>
     * @return This resource, but viewed as an Individual
     * @exception ConversionException if the resource cannot be converted to an individual
     */
    public Individual asIndividual() {
        return (Individual) as( Individual.class );
    }
    
    /** 
     * <p>Answer a view of this resource as a class</p>
     * @return This resource, but viewed as an OntClass
     * @exception ConversionException if the resource cannot be converted to a class
     */
    public OntClass asClass() {
        return (OntClass) as( OntClass.class );
    }
    
    /** 
     * <p>Answer a view of this resource as an ontology description node</p>
     * @return This resource, but viewed as an Ontology
     * @exception ConversionException if the resource cannot be converted to an ontology description node
     */
    public Ontology asOntology() {
        return (Ontology) as( Ontology.class );
    }
    
    /** 
     * <p>Answer a view of this resource as an 'all different' declaration</p>
     * @return This resource, but viewed as an AllDifferent node
     * @exception ConversionException if the resource cannot be converted to an all different declaration
     */
    public AllDifferent asAllDifferent() {
        return (AllDifferent) as( AllDifferent.class );
    }
    


    // Internal implementation methods
    //////////////////////////////////


    protected PathSet asPathSet( Property p, String name ) {
        if (p == null) {
            throw new ProfileException( name, getProfile() );
        }
        else {
            return new PathSet( this, PathFactory.unit( p ) );
        }
    }
    
    /** Answer true if the node has the given type in the graph */
    protected static boolean hasType( Node n, EnhGraph g, Resource type ) {
        boolean hasType = false;
        ClosableIterator i = g.asGraph().find( n, RDF.type.asNode(), type.asNode() );
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
                else if (lang.equalsIgnoreCase( lLang.substring( 0, 2 ) )) {
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
    protected Object objectAs( Property p, String name, Class asClass ) {
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
        return (OntResource) objectAs( p, name, OntResource.class );
    }

    
    /** Answer the object of a statement with the given property, .as() an OntProperty */
    protected OntProperty objectAsProperty( Property p, String name ) {
        return (OntProperty) objectAs( p, name, OntProperty.class );
    }

    
    /** Answer the int value of a statement with the given property */
    protected int objectAsInt( Property p, String name ) {
        checkProfile( p, name );
        return getRequiredProperty( p ).getInt();
    }

    
    /** Answer an iterator for the given property, whose values are .as() some class */
    protected ExtendedIterator listAs( Property p, String name, Class cls ) {
        checkProfile( p, name );
        return WrappedIterator.create( listProperties( p ) ).mapWith( new ObjectAsMapper( cls ) );
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
            
            RDFList values = (RDFList) cur.as( RDFList.class );
        
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
    protected RDFNode convertToType( Resource type, String name, Class cls ) {
        checkProfile( type, name );
        if (canAs( cls )) {
            // don't need to update the model, we already can do the given facet
            return as( cls );
        }
        
        // we're told that adding this rdf:type will make the as() possible - let's see
        addProperty( RDF.type, type );
        return as( cls );
    }
    
    /** Return an iterator of values, respecting the 'direct' modifier */
    protected ExtendedIterator listDirectPropertyValues( Property p, String name, Class cls, Property orderRel, boolean direct, boolean inverse ) {
        ExtendedIterator i = null;
        checkProfile( p, name );
        
        Property sc = p;
        
        // check for requesting direct versions of these properties
        if (direct) {
            sc = getModel().getProperty( ReasonerRegistry.makeDirect( sc.getNode() ).getURI() );
        }
        
        // determine the subject and object pairs for the list statements calls
        Resource subject = inverse ? null : this;
        Resource object  = inverse ? this : null;
        Map1 mapper      = inverse ? (Map1) new SubjectAsMapper( cls ) : (Map1) new ObjectAsMapper( cls );
        
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
            i = getModel().listStatements( subject, sc, object );
    
            // we only want the subjects or objects of the statements
            return new UniqueExtendedIterator( i ).mapWith( mapper );
        }
        else {
            // graph does not support direct directly
            i = getModel().listStatements( subject, p, object );
            
            // we need to keep this node out of the iterator for now, else it will spoil the maximal 
            // generator compression (since all the (e.g.) sub-classes will be sub-classes of this node
            // and so will be excluded from the maximal lower elements calculation)
            Collection s = new ArrayList();
            for( i = i.mapWith( mapper ); i.hasNext();  s.add( i.next() ) );
            boolean withheld = s.remove( this );
            
            // generate the short list as the maximal bound under the given partial order
            s = ResourceUtils.maximalLowerElements( s, orderRel, inverse );
            
            // put myself back if needed
            if (withheld) {
                s.add( this );
            }
            
            return new UniqueExtendedIterator( s.iterator() ).mapWith( mapper );
        }
    }
    
    /** Remove a specified property-value pair, if it exists */
    protected void removePropertyValue( Property prop, String name, RDFNode value ) {
        checkProfile( prop, name );
        
        StmtIterator i = getModel().listStatements( this, prop, value );
        if (i.hasNext()) {
            i.nextStatement().remove();
        }
        
        i.close();
    }
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /** Implementation of Map1 that performs as( Class ) for a given class */
    protected class AsMapper
        implements Map1
    {
        private Class m_as;
        public AsMapper( Class as ) { m_as = as; }
        public Object map1( Object x ) { return (x instanceof Resource) ? ((Resource) x).as( m_as ) : x; }
    }
    
    /** Implementation of Map1 that performs as( Class ) for a given class, on the subject of a statement */
    protected class SubjectAsMapper
        implements Map1
    {
        private Class m_as;
        public SubjectAsMapper( Class as ) { m_as = as; }
        public Object map1( Object x ) { 
            if (x instanceof Statement) {
                RDFNode subj = ((Statement) x).getSubject(); 
                return (m_as == null) ? subj : subj.as( m_as );
            }
            else {
                return x;
            }
        }
    }
    
    /** Implementation of Map1 that extracts the subject of a statement */
    protected class SubjectMapper
        implements Map1
    {
        public Object map1( Object x ) { 
            return (x instanceof Statement) ? ((Statement) x).getSubject() : x; 
        }
    }
    
    /** Implementation of Map1 that performs as( Class ) for a given class, on the object of a statement */
    protected class ObjectAsMapper
        implements Map1
    {
        private Class m_as;
        public ObjectAsMapper( Class as ) { m_as = as; }
        public Object map1( Object x ) { 
            if (x instanceof Statement) {
                RDFNode obj = ((Statement) x).getObject(); 
                return (m_as == null) ? obj : obj.as( m_as );
            }
            else {
                return x;
            }
        }
    }
    
    /** Implementation of Map1 that performs getString on the object of a statement */
    protected class ObjectAsStringMapper
        implements Map1
    {
        public Object map1( Object x ) { return (x instanceof Statement) ? ((Statement) x).getString() : x; }
    }
    
    /** Implementation of Map1 that returns the object of a statement */
    protected class ObjectMapper
        implements Map1
    {
        public ObjectMapper() {}
        public Object map1( Object x ) { return (x instanceof Statement) ? ((Statement) x).getObject() : x; }
    }
    
    /** Filter for matching language tags on literals */
    protected class LangTagFilter 
        implements Filter
    {
        protected String m_lang;
        public LangTagFilter( String lang ) { m_lang = lang; }
        public boolean accept( Object x ) {
            if (x instanceof Literal) {
                return langTagMatch( m_lang, ((Literal) x).getLanguage() );
            }
            else if (x instanceof Statement) {
                // we assume for a statement that we're filtering on the object of the statement
                return accept( ((Statement) x).getObject() );
            }
            else {
                return false;
            }
        }
    }
    
    /** Filter for accepting only the given value, based on .equals() */
    protected class SingleEqualityFilter
        implements Filter
    {
        private Object m_obj;
        public SingleEqualityFilter( Object x ) { m_obj = x; }
        public boolean accept( Object x ) {return m_obj.equals( x );}
    }
}


/*
    (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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

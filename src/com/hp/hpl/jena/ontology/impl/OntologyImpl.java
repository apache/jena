/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            25-Mar-2003
 * Filename           $RCSfile: OntologyImpl.java,v $
 * Revision           $Revision: 1.11 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-06 13:50:08 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Implementation of the Ontology interface, encapsulating nodes that hold the
 * meta-data about whole ontologies.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntologyImpl.java,v 1.11 2004-12-06 13:50:08 andy_seaborne Exp $
 */
public class OntologyImpl
    extends OntResourceImpl
    implements Ontology 
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating Ontology facets from nodes in enhanced graphs.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new OntologyImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to Ontology");
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an Ontology facet if it has rdf:type owl:Ontology or equivalent
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, Ontology.class );
        }
    };



    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an ontology metadata node represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public OntologyImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    // imports
    
    /**
     * <p>Assert that this ontology imports only the given ontology. Any existing 
     * statements for <code>sameAs</code> will be removed.</p>
     * @param res Represents a resource that this ontology imports.
     * @exception OntProfileException If the {@link Profile#IMPORTS()()} property is not supported in the current language profile.   
     */ 
    public void setImport( Resource res ) {
        setPropertyValue( getProfile().IMPORTS(), "IMPORTS", res );
    }

    /**
     * <p>Add a resource representing an ontology that this ontology 
     * (strictly, the ontology reprsented by this node) imports.</p>
     * @param res Represents a resource that this ontology imports.
     * @exception OntProfileException If the {@link Profile#IMPORTS()()} property is not supported in the current language profile.   
     */ 
    public void addImport( Resource res ) {
        addPropertyValue( getProfile().IMPORTS(), "IMPORTS", res );
    }

    /**
     * <p>Answer a resource that represents an ontology imported by this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An ont resource representing a resource that this ontology imports
     * @exception OntProfileException If the {@link Profile#IMPORTS()()} property is not supported in the current language profile.   
     */ 
    public OntResource getImport() {
        return objectAsResource( getProfile().IMPORTS(), "IMPORTS" );
    }

    /**
     * <p>Answer an iterator over all of the resources representing ontologies imported by this ontology. 
     * Each elemeent of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the ontology import resources
     * @exception OntProfileException If the {@link Profile#IMPORTS()()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listImports() {
        return listAs( getProfile().IMPORTS(), "IMPORTS", OntResource.class );
    }

    /**
     * <p>Answer true if this ontology (the ontology represented by this 
     * resource) imports the given resource.</p>
     * @param res A resource to test against
     * @return True if this ontology imports the ontology represented by <code>res</code>
     */
    public boolean imports( Resource res ) {
        return hasPropertyValue( getProfile().IMPORTS(), "IMPORTS", res );
    }
    
    /**
     * <p>Remove the statement that this ontology imports the ontology represented by the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that represents an ontology that is no longer to be imported
     */
    public void removeImport( Resource res ) {
        removePropertyValue( getProfile().IMPORTS(), "IMPORTS", res );
    }
    

    // backwardCompatibleWith
    
    /**
     * <p>Assert that this ontology is backward compatible with the given ontology. Any existing 
     * statements for <code>sameAs</code> will be removed.</p>
     * @param res Represents a resource that this ontology is compatible with.
     * @exception OntProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public void setBackwardCompatibleWith( Resource res ) {
        setPropertyValue( getProfile().BACKWARD_COMPATIBLE_WITH(), "BACKWARD_COMPATIBLE_WITH", res );
    }

    /**
     * <p>Add a resource representing an ontology that this ontology 
     * (strictly, the ontology reprsented by this node) is backwards compatible with.</p>
     * @param res Represents a resource that this ontology is compatible with.
     * @exception OntProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public void addBackwardCompatibleWith( Resource res ) {
        addPropertyValue( getProfile().BACKWARD_COMPATIBLE_WITH(), "BACKWARD_COMPATIBLE_WITH", res );
    }

    /**
     * <p>Answer a resource that represents an ontology that is backwards compatible with this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An ont resource representing an ontology that this ontology is compatible with
     * @exception OntProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public OntResource getBackwardCompatibleWith() {
        return objectAsResource( getProfile().BACKWARD_COMPATIBLE_WITH(), "BACKWARD_COMPATIBLE_WITH" );
    }

    /**
     * <p>Answer an iterator over all of the resources representing 
     * ontologies that this ontology is backwards compatible with. 
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the ontology resources compatible with this ontology
     * @exception OntProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listBackwardCompatibleWith() {
        return listAs( getProfile().BACKWARD_COMPATIBLE_WITH(), "BACKWARD_COMPATIBLE_WITH", OntResource.class );
    }

    /**
     * <p>Answer true if this ontology (the ontology represented by this 
     * resource) is backward compatible with the given resource.</p>
     * @param res A resource to test against
     * @return True if this ontology is compatible with the ontology represented by <code>res</code>
     */
    public boolean isBackwardCompatibleWith( Resource res ) {
        return hasPropertyValue( getProfile().BACKWARD_COMPATIBLE_WITH(), "BACKWARD_COMPATIBLE_WITH", res );
    }
    
    /**
     * <p>Remove the statement that this ontology is backwards compatible with
     * the ontology represented by the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that represents an ontology that is no longer to be imported
     */
    public void removeBackwardCompatibleWith( Resource res ) {
        removePropertyValue( getProfile().BACKWARD_COMPATIBLE_WITH(), "BACKWARD_COMPATIBLE_WITH", res );
    }
    

    // priorVersion
    
    /**
     * <p>Assert that this ontology is a new version of the given ontology. Any existing 
     * statements for <code>priorVersion</code> will be removed.</p>
     * @param res Represents a resource that this ontology supercedes.
     * @exception OntProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.   
     */ 
    public void setPriorVersion( Resource res ) {
        setPropertyValue( getProfile().PRIOR_VERSION(), "PRIOR_VERSION", res );
    }

    /**
     * <p>Add a resource representing an ontology that this ontology 
     * (strictly, the ontology reprsented by this node) supercedes.</p>
     * @param res Represents a resource that this ontology supercedes.
     * @exception OntProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.   
     */ 
    public void addPriorVersion( Resource res ) {
        addPropertyValue( getProfile().PRIOR_VERSION(), "PRIOR_VERSION", res );
    }

    /**
     * <p>Answer a resource that represents an ontology that is superceded by this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An ont resource representing an ontology that this ontology supercedes
     * @exception OntProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.   
     */ 
    public OntResource getPriorVersion() {
        return objectAsResource( getProfile().PRIOR_VERSION(), "PRIOR_VERSION" );
    }

    /**
     * <p>Answer an iterator over all of the resources representing 
     * ontologies that this ontology supercedes. 
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the ontology resources superceded by this ontology
     * @exception OntProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listPriorVersion() {
        return listAs( getProfile().PRIOR_VERSION(), "PRIOR_VERSION", OntResource.class );
    }

    /**
     * <p>Answer true if this ontology (the ontology represented by this 
     * resource) supercedes the given resource.</p>
     * @param res A resource to test against
     * @return True if this ontology supercedes the ontology represented by <code>res</code>
     */
    public boolean hasPriorVersion( Resource res ) {
        return hasPropertyValue( getProfile().PRIOR_VERSION(), "PRIOR_VERSION", res );
    }
    
    /**
     * <p>Remove the statement that the given ontology is a prior version of this ontology.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that represents an ontology that is no longer a prior version of this ontology
     */
    public void removePriorVersion( Resource res ) {
        removePropertyValue( getProfile().PRIOR_VERSION(), "PRIOR_VERSION", res );
    }
    

    // incompatibleWith

    /**
     * <p>Assert that this ontology is incompatible with the given ontology. Any existing 
     * statements for <code>incompatibleWith</code> will be removed.</p>
     * @param res Represents a resource that this ontology is incompatible with.
     * @exception OntProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public void setIncompatibleWith( Resource res ) {
        setPropertyValue( getProfile().INCOMPATIBLE_WITH(), "INCOMPATIBLE_WITH", res );
    }

    /**
     * <p>Add a resource representing an ontology that this ontology 
     * (strictly, the ontology reprsented by this node) is incompatible with.</p>
     * @param res Represents a resource that this ontology is incompatible with.
     * @exception OntProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public void addIncompatibleWith( Resource res ) {
        addPropertyValue( getProfile().INCOMPATIBLE_WITH(), "INCOMPATIBLE_WITH", res );
    }

    /**
     * <p>Answer a resource that represents an ontology that is is incompatible with this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An ont resource representing an ontology that this ontology is incompatible with
     * @exception OntProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public OntResource getIncompatibleWith() {
        return objectAsResource( getProfile().INCOMPATIBLE_WITH(), "INCOMPATIBLE_WITH" );
    }

    /**
     * <p>Answer an iterator over all of the resources representing 
     * ontologies that this ontology is incompatible with. 
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the ontology resources that this ontology is incompatible with
     * @exception OntProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listIncompatibleWith() {
        return listAs( getProfile().INCOMPATIBLE_WITH(), "INCOMPATIBLE_WITH", OntResource.class );
    }

    /**
     * <p>Answer true if this ontology (the ontology represented by this 
     * resource) is incompatible with the given resource.</p>
     * @param res A resource to test against
     * @return True if this ontology is incompatible with the ontology represented by <code>res</code>
     */
    public boolean isIncompatibleWith( Resource res ) {
        return hasPropertyValue( getProfile().INCOMPATIBLE_WITH(), "INCOMPATIBLE_WITH", res );
    }
    
    /**
     * <p>Remove the statement that the given ontology is incompatible with this ontology.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that represents an ontology that is no longer incompatible with this ontology
     */
    public void removeIncompatibleWith( Resource res ) {
        removePropertyValue( getProfile().INCOMPATIBLE_WITH(), "INCOMPATIBLE_WITH", res );
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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

/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: Ontology.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-21 12:35:39 $
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
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Interface encapsulating the distinguished instance in a given ontology
 * document that presents meta-data and other processing data about the document
 * (including which other documents are imported by a document).
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: Ontology.java,v 1.6 2003-06-21 12:35:39 ian_dickinson Exp $
 */
public interface Ontology
    extends OntResource
{
    // Constants
    //////////////////////////////////



    // External signature methods
    //////////////////////////////////

    // imports
    
    /**
     * <p>Assert that this ontology imports only the given ontology. Any existing 
     * statements for <code>sameAs</code> will be removed.</p>
     * @param res Represents a resource that this ontology imports.
     * @exception OntProfileException If the {@link Profile#IMPORTS()()} property is not supported in the current language profile.   
     */ 
    public void setImport( Resource res );

    /**
     * <p>Add a resource representing an ontology that this ontology 
     * (strictly, the ontology reprsented by this node) imports.</p>
     * @param res Represents a resource that this ontology imports.
     * @exception OntProfileException If the {@link Profile#IMPORTS()()} property is not supported in the current language profile.   
     */ 
    public void addImport( Resource res );

    /**
     * <p>Answer a resource that represents an ontology imported by this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An ont resource representing a resource that this ontology imports
     * @exception OntProfileException If the {@link Profile#IMPORTS()()} property is not supported in the current language profile.   
     */ 
    public OntResource getImport();

    /**
     * <p>Answer an iterator over all of the resources representing ontologies imported by this ontology. 
     * Each elemeent of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the ontology import resources
     * @exception OntProfileException If the {@link Profile#IMPORTS()()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listImports();

    /**
     * <p>Answer true if this ontology (the ontology represented by this 
     * resource) imports the given resource.</p>
     * @param res A resource to test against
     * @return True if this ontology imports the ontology represented by <code>res</code>
     */
    public boolean imports( Resource res );
    
    /**
     * <p>Remove the statement that this ontology imports the ontology represented by the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that represents an ontology that is no longer to be imported
     */
    public void removeImport( Resource res );
    

    // backwardCompatibleWith
    
    /**
     * <p>Assert that this ontology is backward compatible with the given ontology. Any existing 
     * statements for <code>sameAs</code> will be removed.</p>
     * @param res Represents a resource that this ontology is compatible with.
     * @exception OntProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public void setBackwardCompatibleWith( Resource res );

    /**
     * <p>Add a resource representing an ontology that this ontology 
     * (strictly, the ontology reprsented by this node) is backwards compatible with.</p>
     * @param res Represents a resource that this ontology is compatible with.
     * @exception OntProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public void addBackwardCompatibleWith( Resource res );

    /**
     * <p>Answer a resource that represents an ontology that is backwards compatible with this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An ont resource representing an ontology that this ontology is compatible with
     * @exception OntProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public OntResource getBackwardCompatibleWith();

    /**
     * <p>Answer an iterator over all of the resources representing 
     * ontologies that this ontology is backwards compatible with. 
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the ontology resources compatible with this ontology
     * @exception OntProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listBackwardCompatibleWith();

    /**
     * <p>Answer true if this ontology (the ontology represented by this 
     * resource) is backward compatible with the given resource.</p>
     * @param res A resource to test against
     * @return True if this ontology is compatible with the ontology represented by <code>res</code>
     */
    public boolean isBackwardCompatibleWith( Resource res );
    
    /**
     * <p>Remove the statement that this ontology is backwards compatible with
     * the ontology represented by the given resource.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that represents an ontology that is no longer to be imported
     */
    public void removeBackwardCompatibleWith( Resource res );
    

    // priorVersion
    
    /**
     * <p>Assert that this ontology is a new version of the given ontology. Any existing 
     * statements for <code>priorVersion</code> will be removed.</p>
     * @param res Represents a resource that this ontology supercedes.
     * @exception OntProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.   
     */ 
    public void setPriorVersion( Resource res );

    /**
     * <p>Add a resource representing an ontology that this ontology 
     * (strictly, the ontology reprsented by this node) supercedes.</p>
     * @param res Represents a resource that this ontology supercedes.
     * @exception OntProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.   
     */ 
    public void addPriorVersion( Resource res );

    /**
     * <p>Answer a resource that represents an ontology that is superceded by this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An ont resource representing an ontology that this ontology supercedes
     * @exception OntProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.   
     */ 
    public OntResource getPriorVersion();

    /**
     * <p>Answer an iterator over all of the resources representing 
     * ontologies that this ontology supercedes. 
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the ontology resources superceded by this ontology
     * @exception OntProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listPriorVersion();

    /**
     * <p>Answer true if this ontology (the ontology represented by this 
     * resource) supercedes the given resource.</p>
     * @param res A resource to test against
     * @return True if this ontology supercedes the ontology represented by <code>res</code>
     */
    public boolean hasPriorVersion( Resource res );
    
    /**
     * <p>Remove the statement that the given ontology is a prior version of this ontology.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that represents an ontology that is no longer a prior version of this ontology
     */
    public void removePriorVersion( Resource res );
    
    // incompatibleWith

    /**
     * <p>Assert that this ontology is incompatible with the given ontology. Any existing 
     * statements for <code>incompatibleWith</code> will be removed.</p>
     * @param res Represents a resource that this ontology is incompatible with.
     * @exception OntProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public void setIncompatibleWith( Resource res );

    /**
     * <p>Add a resource representing an ontology that this ontology 
     * (strictly, the ontology reprsented by this node) is incompatible with.</p>
     * @param res Represents a resource that this ontology is incompatible with.
     * @exception OntProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public void addIncompatibleWith( Resource res );

    /**
     * <p>Answer a resource that represents an ontology that is is incompatible with this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An ont resource representing an ontology that this ontology is incompatible with
     * @exception OntProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public OntResource getIncompatibleWith();

    /**
     * <p>Answer an iterator over all of the resources representing 
     * ontologies that this ontology is incompatible with. 
     * Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over the ontology resources that this ontology is incompatible with
     * @exception OntProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listIncompatibleWith();

    /**
     * <p>Answer true if this ontology (the ontology represented by this 
     * resource) is incompatible with the given resource.</p>
     * @param res A resource to test against
     * @return True if this ontology is incompatible with the ontology represented by <code>res</code>
     */
    public boolean isIncompatibleWith( Resource res );
    
    /**
     * <p>Remove the statement that the given ontology is incompatible with this ontology.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that represents an ontology that is no longer incompatible with this ontology
     */
    public void removeIncompatibleWith( Resource res );
    
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

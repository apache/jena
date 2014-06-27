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
 * Interface encapsulating the distinguished instance in a given ontology
 * document that presents meta-data and other processing data about the document
 * (including which other documents are imported by a document).
 * </p>
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
     * @exception ProfileException If the {@link Profile#IMPORTS()} property is not supported in the current language profile.
     */
    public void setImport( Resource res );

    /**
     * <p>Add a resource representing an ontology that this ontology
     * (strictly, the ontology represented by this node) imports.</p>
     * @param res Represents a resource that this ontology imports.
     * @exception ProfileException If the {@link Profile#IMPORTS()} property is not supported in the current language profile.
     */
    public void addImport( Resource res );

    /**
     * <p>Answer a resource that represents an ontology imported by this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An {@link OntResource} representing a resource that this ontology imports
     * @exception ProfileException If the {@link Profile#IMPORTS()} property is not supported in the current language profile.
     */
    public OntResource getImport();

    /**
     * <p>Answer an iterator over all of the resources representing ontologies imported by this ontology.
     * </p>
     * @return An iterator over the ontology import resources
     * @exception ProfileException If the {@link Profile#IMPORTS()} property is not supported in the current language profile.
     */
    public ExtendedIterator<OntResource> listImports();

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
     * @exception ProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.
     */
    public void setBackwardCompatibleWith( Resource res );

    /**
     * <p>Add a resource representing an ontology that this ontology
     * (strictly, the ontology represented by this node) is backwards compatible with.</p>
     * @param res Represents a resource that this ontology is compatible with.
     * @exception ProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.
     */
    public void addBackwardCompatibleWith( Resource res );

    /**
     * <p>Answer a resource that represents an ontology that is backwards compatible with this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An {@link OntResource} representing an ontology that this ontology is compatible with
     * @exception ProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.
     */
    public OntResource getBackwardCompatibleWith();

    /**
     * <p>Answer an iterator over all of the resources representing
     * ontologies that this ontology is backwards compatible with.</p>
     * @return An iterator over the ontology resources compatible with this ontology
     * @exception ProfileException If the {@link Profile#BACKWARD_COMPATIBLE_WITH} property is not supported in the current language profile.
     */
    public ExtendedIterator<OntResource> listBackwardCompatibleWith();

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
     * @exception ProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.
     */
    public void setPriorVersion( Resource res );

    /**
     * <p>Add a resource representing an ontology that this ontology
     * (strictly, the ontology represented by this node) supercedes.</p>
     * @param res Represents a resource that this ontology supercedes.
     * @exception ProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.
     */
    public void addPriorVersion( Resource res );

    /**
     * <p>Answer a resource that represents an ontology that is superceded by this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An {@link OntResource} representing an ontology that this ontology supercedes
     * @exception ProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.
     */
    public OntResource getPriorVersion();

    /**
     * <p>Answer an iterator over all of the resources representing
     * ontologies that this ontology supercedes.</p>
     * @return An iterator over the ontology resources superceded by this ontology
     * @exception ProfileException If the {@link Profile#PRIOR_VERSION} property is not supported in the current language profile.
     */
    public ExtendedIterator<OntResource> listPriorVersion();

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
     * @exception ProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.
     */
    public void setIncompatibleWith( Resource res );

    /**
     * <p>Add a resource representing an ontology that this ontology
     * (strictly, the ontology represented by this node) is incompatible with.</p>
     * @param res Represents a resource that this ontology is incompatible with.
     * @exception ProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.
     */
    public void addIncompatibleWith( Resource res );

    /**
     * <p>Answer a resource that represents an ontology that is is incompatible with this ontology. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return An {@link OntResource} representing an ontology that this ontology is incompatible with
     * @exception ProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.
     */
    public OntResource getIncompatibleWith();

    /**
     * <p>Answer an iterator over all of the resources representing
     * ontologies that this ontology is incompatible with.</p>
     * @return An iterator over the ontology resources that this ontology is incompatible with
     * @exception ProfileException If the {@link Profile#INCOMPATIBLE_WITH} property is not supported in the current language profile.
     */
    public ExtendedIterator<OntResource> listIncompatibleWith();

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

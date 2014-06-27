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
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Interface defining an individual in which all members of a collection are
 * declared pair-wise disjoint.  This allows ontologies that wish to support the
 * unique names assumption to add this condition in languages (like OWL) that
 * do not make the same assumption, with a minimum number of statements.
 * Instances of the all different axiom are expected to have a property
 * (e.g. <code>owl:distinctMembers</code> defining the list of distinct
 * individuals in the ontology.  For a given vocabulary, this will be defined by
 * the {@linkplain Profile#DISTINCT_MEMBERS distinctMembers} entry.
 * </p>
 */
public interface AllDifferent
    extends OntResource
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>Assert that the list of distinct individuals in this AllDifferent declaration
     * is the given list. Any existing
     * statements for <code>distinctMembers</code> will be removed.</p>
     * @param members A list of the members that are declared to be distinct.
     * @exception ProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.
     */
    public void setDistinctMembers( RDFList members );

    /**
     * <p>Add the given individual to the list of distinct members of this AllDifferent declaration.</p>
     * @param res A resource that will be added to the list of all different members.
     * @exception ProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.
     */
    public void addDistinctMember( Resource res );

    /**
     * <p>Add the given individuals to the list of distinct members of this AllDifferent declaration.</p>
     * @param individuals An iterator over the distinct invididuals that will be added
     * @exception ProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.
     */
    public void addDistinctMembers( Iterator<? extends Resource> individuals );

    /**
     * <p>Answer the list of distinct members for this AllDifferent declaration.</p>
     * @return The list of individuals declared distinct by this AllDifferent declaration.
     * @exception ProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.
     */
    public RDFList getDistinctMembers();

    /**
     * <p>Answer an iterator over all of the individuals that are declared to be distinct by
     * this AllDifferent declaration. Each element of the iterator will be an {@link OntResource}.</p>
     * @return An iterator over distinct individuals.
     * @exception ProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.
     */
    public ExtendedIterator<? extends OntResource> listDistinctMembers();

    /**
     * <p>Answer true if this AllDifferent declaration includes <code>res</code> as one of the distinct individuals.</p>
     * @param res A resource to test against
     * @return True if <code>res</code> is declared to be distinct from the other individuals in this declation.
     * @exception ProfileException If the {@link Profile#DISTINCT_MEMBERS()} property is not supported in the current language profile.
     */
    public boolean hasDistinctMember( Resource res );

    /**
     * <p>Remove the given resource from the list of distinct individuals.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param res A resource that is no longer distinct from the other listed individuals
     */
    public void removeDistinctMember( Resource res );

}

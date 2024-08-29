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

package org.apache.jena.ontapi.model;

import org.apache.jena.vocabulary.OWL2;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Interface encapsulating an anonymous resource-collection of objects in an ontology with one of the following types:
 * {@code owl:AllDisjointProperties}, {@code owl:AllDisjointClasses}, {@code owl:AllDifferent}).
 * The resource is looks like this example:
 * {@code _:x rdf:type owl:AllDisjointProperties; _:x owl:members ( R1 ... Rn ).}.
 *
 * @param <O> - {@link OntIndividual individual}, {@link OntClass class expression},
 *            {@link OntObjectProperty object property expression} or {@link OntDataProperty data property}
 */
public interface OntDisjoint<O extends OntObject> extends OntObject, HasRDFNodeList<O> {

    /**
     * Lists all pair-wise disjoint members holding by this {@link OntDisjoint Ontology Disjoint} resource.
     * Note that the returned values are not necessarily the same as {@link OntList#members()} output:
     * some profiles (e.g., OWL2 QL) impose some restrictions.
     *
     * @return Stream (<b>not distinct</b>) of {@link OntObject}s
     */
    default Stream<O> members() {
        return getList().members();
    }

    /**
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Disjoint_Classes">9.1.3 Disjoint Classes</a>
     * @see OntModel#createDisjointClasses(Collection)
     */
    interface Classes extends OntDisjoint<OntClass>, SetComponents<OntClass, Classes> {
    }

    /**
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Individual_Inequality">9.6.2 Individual Inequality</a>
     * @see OntModel#createDifferentIndividuals(Collection)
     */
    interface Individuals extends OntDisjoint<OntIndividual>, SetComponents<OntIndividual, Individuals> {

        /**
         * Gets an {@link OntList ONT-List}.
         * Since both predicates {@link OWL2#members owl:members} and
         * {@link OWL2#distinctMembers owl:distinctMembers} are allowed by specification,
         * this method returns most bulky list.
         * In case both lists have the same dimension, the method chooses one that is on predicate {@code owl:members}.
         * The method {@link OntModel#createDifferentIndividuals(Collection)} also prefers {@code owl:members} predicate.
         * This was done for reasons of uniformity.
         *
         * @return {@link OntList ONT-List} of {@link OntIndividual individual}s
         * @see OntModel#createDifferentIndividuals(Collection)
         */
        @Override
        OntList<OntIndividual> getList();

        /**
         * Lists all members from []-list on predicate {@link OWL2#members owl:members}
         * with concatenation all members from []-list
         * on predicate {@link OWL2#distinctMembers owl:distinctMembers}.
         *
         * @return <b>not distinct</b> Stream of {@link OntIndividual individual}s
         */
        @Override
        Stream<OntIndividual> members();
    }

    /**
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Disjoint_Object_Properties">9.2.3 Disjoint Object Properties</a>
     * @see OntModel#createDisjointObjectProperties(Collection)
     */
    interface ObjectProperties extends Properties<OntObjectProperty>, SetComponents<OntObjectProperty, ObjectProperties> {
    }

    /**
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Disjoint_Data_Properties">9.3.3 Disjoint Data Properties</a>
     * @see OntModel#createDisjointDataProperties(Collection)
     */
    interface DataProperties extends Properties<OntDataProperty>, SetComponents<OntDataProperty, DataProperties> {
    }

    /**
     * Abstraction for Pairwise Disjoint Properties anonymous {@link OntObject Ontology Object}.
     *
     * @param <P> either {@link OntObjectProperty object property expression} or {@link OntDataProperty data property}
     */
    interface Properties<P extends OntRelationalProperty> extends OntDisjoint<P> {
    }
}

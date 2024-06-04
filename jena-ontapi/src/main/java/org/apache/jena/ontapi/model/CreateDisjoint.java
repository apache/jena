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

import java.util.Arrays;
import java.util.Collection;

/**
 * A technical interface to generate {@link OntDisjoint Disjoint Resource}s.
 */
interface CreateDisjoint {

    /**
     * Creates a Disjoint Classes Axiom Resource.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:AllDisjointClasses .
     * _:x owl:members ( C1 ... Cn ) .
     * }</pre>
     *
     * @param classes {@code Collection} of {@link OntClass Class Expression}s without {@code null}-elements
     * @return {@link OntDisjoint.Classes}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Disjoint_Classes">9.1.3 Disjoint Classes</a>
     */
    OntDisjoint.Classes createDisjointClasses(Collection<OntClass> classes);

    /**
     * Creates a Different Individuals Axiom Resource.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:AllDifferent .
     * _:x owl:members ( a1 ... an ).
     * }</pre>
     * Note: instead of {@link OWL2#members owl:members}, alternatively,
     * the predicate {@link OWL2#distinctMembers owl:distinctMembers} can be used.
     *
     * @param individuals {@code Collection} of {@link OntIndividual Individual}s without {@code null}-elements
     * @return {@link OntDisjoint.Individuals}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Individual_Inequality">9.6.2 Individual Inequality </a>
     */
    OntDisjoint.Individuals createDifferentIndividuals(Collection<OntIndividual> individuals);

    /**
     * Creates a Disjoint Object Properties Axiom Resource.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:AllDisjointProperties .
     * _:x owl:members ( P1 ... Pn ) .
     * }</pre>
     *
     * @param properties {@code Collection} of {@link OntObjectProperty object property expression}s without {@code null}-elements
     * @return {@link OntDisjoint.ObjectProperties}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Disjoint_Object_Properties">9.2.3 Disjoint Object Properties</a>
     */
    OntDisjoint.ObjectProperties createDisjointObjectProperties(Collection<OntObjectProperty> properties);

    /**
     * Creates a Disjoint Data Properties Axiom Resource.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:AllDisjointProperties .
     * _:x owl:members ( R1 ... Rn ) .
     * }</pre>
     *
     * @param properties {@code Collection} of {@link OntDataProperty data properties} without {@code null}-elements
     * @return {@link OntDisjoint.DataProperties}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Disjoint_Data_Properties">9.3.3 Disjoint Data Properties</a>
     */
    OntDisjoint.DataProperties createDisjointDataProperties(Collection<OntDataProperty> properties);

    /**
     * Creates a Disjoint Classes Axiom Resource.
     *
     * @param classes Array of {@link OntClass Class Expression}s without {@code null}-elements
     * @return {@link OntDisjoint.Classes}
     * @see #createDisjointClasses(Collection)
     */
    default OntDisjoint.Classes createDisjointClasses(OntClass... classes) {
        return createDisjointClasses(Arrays.asList(classes));
    }

    /**
     * Creates a Different Individuals Axiom Resource.
     *
     * @param individuals Array of {@link OntIndividual individual}s without {@code null}-elements
     * @return {@link OntDisjoint.Individuals}
     * @see #createDifferentIndividuals(Collection)
     */
    default OntDisjoint.Individuals createDifferentIndividuals(OntIndividual... individuals) {
        return createDifferentIndividuals(Arrays.asList(individuals));
    }

    /**
     * Creates a Disjoint Object Properties Axiom Resource.
     *
     * @param properties Array of {@link OntObjectProperty Object Property Expression}s without {@code null}-elements
     * @return {@link OntDisjoint.ObjectProperties}
     * @see #createDisjointObjectProperties(Collection)
     */
    default OntDisjoint.ObjectProperties createDisjointObjectProperties(OntObjectProperty... properties) {
        return createDisjointObjectProperties(Arrays.asList(properties));
    }

    /**
     * Creates a Disjoint Data Properties Axiom Resource.
     *
     * @param properties Array of {@link OntDataProperty Data Properties} without {@code null}-elements
     * @return {@link OntDisjoint.DataProperties}
     * @see #createDisjointDataProperties(Collection)
     */
    default OntDisjoint.DataProperties createDisjointDataProperties(OntDataProperty... properties) {
        return createDisjointDataProperties(Arrays.asList(properties));
    }
}

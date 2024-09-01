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

package org.apache.jena.ontapi;

import org.apache.jena.ontapi.common.OntConfig;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntID;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;

/**
 * Default settings for {@link OntConfig}.
 */
public enum OntModelControls {
    /**
     * If {@code true}, {@link OntID}
     * will be generated automatically if it is absent (as a b-node).
     * A valid OWL ontology must have a single ontology ID.
     */
    USE_GENERATE_ONTOLOGY_HEADER_IF_ABSENT_STRATEGY,
    /**
     * If {@code true}, a multiple ontology header is allowed.
     * Since a valid OWL ontology can have only a single ontology ID, the most suitable will be chosen.
     * Note that if there are several anonymous headers with similar contents,
     * then there is no guarantee that the same node will always be selected after restarting JVM.
     */
    USE_CHOOSE_MOST_SUITABLE_ONTOLOGY_HEADER_STRATEGY,
    /**
     * If this key is set to {@code true}, then the class/property hierarchies
     * (e.g., see {@link OntClass#subClasses()})
     * are to be inferred by the naked model itself using builtin algorithms.
     * Should not be used in conjunction with Reasoner.
     *
     * @see OntSpecification#OWL2_DL_MEM_BUILTIN_RDFS_INF
     */
    USE_BUILTIN_HIERARCHY_SUPPORT,
    /**
     * If this key is set to {@code true},
     * then {@code owl:DataRange} and {@code owl:distinctMembers} will also be considered,
     * although in OWL2 they are deprecated.
     * Applicable only for OWL2.
     */
    USE_OWL2_DEPRECATED_VOCABULARY_FEATURE,
    /**
     * If this key is set to {@code true},
     * then {@code owl:DataRange} (OWL1) instead of {@code rdfs:Datatype} (OWL2).
     */
    USE_OWL1_DATARANGE_DECLARATION_FEATURE,
    /**
     * If this key is set to {@code true},
     * then {@code owl:distinctMembers} (OWL1) is used instead of {@code owl:members} (OWL2).
     * E.g. {@code _:x rdf:type owl:AllDifferent . _:x owl:distinctMembers (a1...an). }
     */
    USE_OWL1_DISTINCT_MEMBERS_PREDICATE_FEATURE,
    /**
     * If this key is set to {@code true}, all class expressions are allowed to be named (can have URI).
     * This option is for compatibility with legacy {@code org.apache.jena.ontology.OntModel}.
     * In OWL2, complex class expression should be anonymous.
     */
    ALLOW_NAMED_CLASS_EXPRESSIONS,
    /**
     * If this key is set to {@code true}, there is a special type of class expressions,
     * which includes any structure declared as {@code owl:Class} or {@code owl:Restriction}
     * that cannot be classified as a specific type.
     * Casting such a construction to a particular class type
     * (e.g. {@code generic.as(OntClass.OneOf.class)}) will result in an exception,
     * but as a class expression, it can a type of the individual, can be a domain for property, etc.
     * This option is for compatibility with legacy {@code org.apache.jena.ontology.OntModel}.
     */
    ALLOW_GENERIC_CLASS_EXPRESSIONS,
    /**
     * Used while {@link OntModel#individuals()}.
     * If {@code true}, the class type is checked only by declaration
     * ({@code owl:Class} &amp; {@code owl:Restriction} for OWL profile, {@code rdfs:Class} for RDFS profile).
     * Otherwise, a full checking is performed.
     */
    USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS,
    /**
     * If {@code true},
     * named class testing is compatible with the legacy {@code org.apache.jena.ontology.OntModel},
     * otherwise, a strict check against the specification for the class declaration is performed
     * ({@code owl:Class} for OWL &amp; {@code rdfs:Class} for RDFS types are required).
     * Note that this only applies to
     * {@link org.apache.jena.enhanced.EnhNode#canAs EnhNode#canAs} and {@link org.apache.jena.enhanced.EnhNode#as EnhNode#as} methods;
     * iteration (e.g., methods {@code OntModel.ontObjects(OntClass.class)})
     * still does not take into account classes with incorrect or missing declarations.
     * For legacy Jena's casting rules see {@code org.apache.jena.ontology.Profile} impls.
     */
    USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY,
    /**
     * If this key is set to {@code true},
     * then {@link OWL2#NamedIndividual owl:NamedIndividual}
     * declaration is used for creating individuals.
     */
    USE_OWL2_NAMED_INDIVIDUAL_DECLARATION_FEATURE,
    /**
     * Controls {@link OWL2#hasKey owl:hasKey} functionality.
     * If disabled,
     * {@link OntClass#hasKeys() OntClass#hasKeys()} will return an empty {@code Stream},
     * modification operations, such as
     * {@link OntClass#addHasKey(OntRelationalProperty...) OntClass#addHasKey(OntRelationalProperty...)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL2_CLASS_HAS_KEY_FEATURE,
    /**
     * Controls {@link OWL2#disjointUnionOf owl:disjointUnionOf} functionality.
     * If disabled,
     * {@link OntClass.Named#disjointUnions() OntClass.Named#disjointUnions()}
     * will return an empty {@code Stream}, modification operations, such as
     * {@link OntClass.Named#addDisjointUnion(OntClass...) OntClass.Named#addDisjointUnion(OntClass...)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    @SuppressWarnings("javadoc")
    USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE,
    /**
     * Controls {@link OWL2#disjointWith owl:disjointWith} functionality.
     * If disabled,
     * {@link OntClass#disjoints() OntClass#disjoints()}
     * will return an empty {@code Stream}, modification operations, such as
     * {@link OntClass#addDisjointClass(OntClass) OntClass#addDisjointClass(OntClass)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_CLASS_DISJOINT_WITH_FEATURE,
    /**
     * Controls {@link OWL2#equivalentClass owl:equivalentClass} functionality.
     * If disabled,
     * {@link OntClass#equivalentClasses() OntClass#equivalentClasses()}
     * will return an empty {@code Stream}, modification operations, such as
     * {@link OntClass#addEquivalentClass(OntClass) OntClass#addEquivalentClass(OntClass)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_CLASS_EQUIVALENT_FEATURE,
    /**
     * Controls {@link OWL2#propertyDisjointWith owl:propertyDisjointWith} functionality.
     * If disabled,
     * {@link OntRelationalProperty#disjointProperties() OntRelationalProperty#disjointProperties()}
     * will return an empty {@code Stream}, modification operations, such as
     * {@link OntObjectProperty#addDisjointProperty(OntObjectProperty) OntObjectProperty#addDisjointProperty(OntObjectProperty)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL2_PROPERTY_DISJOINT_WITH_FEATURE,
    /**
     * Controls {@link OWL2#equivalentProperty owl:equivalentProperty} functionality.
     * If disabled,
     * {@link OntRelationalProperty#equivalentProperties() OntRelationalProperty#equivalentProperties()}
     * will return an empty {@code Stream}, modification operations, such as
     * {@link OntObjectProperty#addEquivalentProperty(OntObjectProperty) OntObjectProperty#addEquivalentProperty(OntObjectProperty)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_EQUIVALENT_FEATURE,
    /**
     * Controls data {@link OWL2#FunctionalProperty owl:FunctionalProperty} functionality.
     * If disabled,
     * {@link OntDataProperty#isFunctional() OntDataProperty#isFunctional()}
     * will return {@code false}, modification operations, such as
     * {@link OntDataProperty#setFunctional(boolean) OntDataProperty#setFunctional(boolean)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_DATA_PROPERTY_FUNCTIONAL_FEATURE,
    /**
     * Controls object {@link OWL2#FunctionalProperty owl:FunctionalProperty} functionality.
     * If disabled,
     * {@link OntObjectProperty#isFunctional() OntObjectProperty#isFunctional()}
     * will return {@code false}, modification operations, such as
     * {@link OntObjectProperty#setFunctional(boolean) OntObjectProperty#setFunctional(boolean)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_OBJECT_PROPERTY_FUNCTIONAL_FEATURE,
    /**
     * Controls {@link OWL2#InverseFunctionalProperty owl:InverseFunctionalProperty} functionality.
     * If disabled,
     * {@link OntObjectProperty#isInverseFunctional() OntObjectProperty#isInverseFunctional()}
     * will return {@code false}, modification operations, such as
     * {@link OntObjectProperty#setInverseFunctional(boolean) OntObjectProperty#setInverseFunctional(boolean)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_INVERSE_FUNCTIONAL_FEATURE,
    /**
     * Controls {@link OWL2#SymmetricProperty owl:SymmetricProperty} functionality.
     * If disabled,
     * {@link OntObjectProperty#isSymmetric() OntObjectProperty#isSymmetric()}
     * will return {@code false}, modification operations, such as
     * {@link OntObjectProperty#setSymmetric(boolean) OntObjectProperty#setSymmetric(boolean)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_SYMMETRIC_FEATURE,
    /**
     * Controls {@link OWL2#AsymmetricProperty owl:AsymmetricProperty} functionality.
     * If disabled,
     * {@link OntObjectProperty#isAsymmetric() OntObjectProperty#isAsymmetric()}
     * will return {@code false}, modification operations, such as
     * {@link OntObjectProperty#setAsymmetric(boolean) OntObjectProperty#setAsymmetric(boolean)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_ASYMMETRIC_FEATURE,
    /**
     * Controls {@link OWL2#TransitiveProperty owl:TransitiveProperty} functionality.
     * If disabled,
     * {@link OntObjectProperty#isTransitive() OntObjectProperty#isTransitive()}
     * will return {@code false}, modification operations, such as
     * {@link OntObjectProperty#setTransitive(boolean) OntObjectProperty#setTransitive(boolean)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_TRANSITIVE_FEATURE,
    /**
     * Controls {@link OWL2#ReflexiveProperty owl:ReflexiveProperty} functionality.
     * If disabled,
     * {@link OntObjectProperty#isReflexive() OntObjectProperty#isReflexive()}
     * will return {@code false}, modification operations, such as
     * {@link OntObjectProperty#setReflexive(boolean) OntObjectProperty#setReflexive(boolean)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_REFLEXIVE_FEATURE,
    /**
     * Controls {@link OWL2#IrreflexiveProperty owl:IrreflexiveProperty} functionality.
     * If disabled,
     * {@link OntObjectProperty#isIrreflexive() OntObjectProperty#isIrreflexive()}
     * will return {@code false}, modification operations, such as
     * {@link OntObjectProperty#setIrreflexive(boolean) OntObjectProperty#setIrreflexive(boolean)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_IRREFLEXIVE_FEATURE,
    /**
     * Controls {@link OWL2#inverseOf owl:inverseOf} functionality (InverseObjectProperty axiom).
     * If disabled,
     * {@link OntObjectProperty#inverseProperties() OntObjectProperty#inverseProperties()}
     * will return an empty {@code Stream}, modification operations, such as
     * {@link OntObjectProperty#addInverseProperty(OntObjectProperty) OntObjectProperty#addInverseProperty(OntObjectProperty)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_INVERSE_OBJECT_PROPERTIES_FEATURE,
    /**
     * Controls {@link OWL2#propertyChainAxiom owl:propertyChainAxiom} functionality.
     * If disabled,
     * {@link OntObjectProperty#propertyChains() OntObjectProperty#propertyChains()}
     * will return {@code false}, modification operations, such as
     * {@link OntObjectProperty#addPropertyChain(OntObjectProperty...) OntObjectProperty#addPropertyChain(OntObjectProperty...)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_CHAIN_AXIOM_FEATURE,
    /**
     * Controls {@link OWL2#sameAs owl:sameAs} functionality.
     * If disabled,
     * {@link OntIndividual#sameIndividuals() OntIndividual#sameIndividuals()}
     * will return {@code false}, modification operations, such as
     * {@link OntIndividual#addSameIndividual(OntIndividual) OntIndividual#addSameIndividual(OntIndividual)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_INDIVIDUAL_SAME_AS_FEATURE,
    /**
     * Controls {@link OWL2#differentFrom owl:differentFrom} functionality.
     * If disabled,
     * {@link OntIndividual#differentIndividuals() OntIndividual#differentIndividuals()}
     * will return {@code false}, modification operations, such as
     * {@link OntIndividual#removeDifferentIndividual(Resource) OntIndividual#removeDifferentIndividual(Resource)},
     * will throw {@link OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_INDIVIDUAL_DIFFERENT_FROM_FEATURE,
    /**
     * If this key is set to {@code true},
     * then {@link OWL2#qualifiedCardinality owl:qualifiedCardinality},
     * {@link OWL2#maxQualifiedCardinality owl:maxQualifiedCardinality},
     * {@link OWL2#minQualifiedCardinality owl:minQualifiedCardinality}
     * predicates are allowed for Cardinality restrictions.
     */
    USE_OWL2_QUALIFIED_CARDINALITY_RESTRICTION_FEATURE,
}

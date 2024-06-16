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

import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A common interface for any Ontology Object Property Expression.
 * In OWL2, there are two types of object property expressions:
 * named object property (entity) and InverseOf anonymous property expression.
 * Range values for this property expression are restricted to individuals
 * (as distinct from datatype valued {@link OntDataProperty properties}).
 */
public interface OntObjectProperty extends OntRelationalProperty, AsNamed<OntObjectProperty.Named>, HasDisjoint<OntObjectProperty> {

    /**
     * {@inheritDoc}
     *
     * @param direct {@code boolean} if {@code true} answers the directly adjacent properties in the sub-property relation:
     *               i.e., eliminate any properties for which
     *               there is a longer route to reach that parent under the sub-property relation
     * @return <b>distinct</b> {@code Stream} of object property expressions
     * @see #propertyChains()
     */
    @Override
    Stream<OntObjectProperty> subProperties(boolean direct);

    /**
     * {@inheritDoc}
     *
     * @param direct {@code boolean}: if {@code true} answers the directly adjacent properties in the super-property relation,
     *               i.e., eliminate any property for
     *               which there is a longer route to reach that parent under the super-property relation
     * @return <b>distinct</b> {@code Stream} of object property expressions
     * @see #propertyChains()
     */
    @Override
    Stream<OntObjectProperty> superProperties(boolean direct);


    /**
     * Returns a {@code Stream} over all property chain {@link OntList ontology list}s
     * that are attached to this Object Property Expression
     * on the predicate {@link OWL2#propertyChainAxiom owl:propertyChainAxiom}.
     *
     * @return {@code Stream} of {@link OntList}s with generic-parameter {@code OntOPE}
     */
    Stream<OntList<OntObjectProperty>> propertyChains();

    /**
     * Adds a negative property assertion ontology object.
     *
     * @param source {@link OntIndividual}
     * @param target {@link OntIndividual}
     * @return {@link OntNegativeAssertion.WithObjectProperty}
     * @see OntDataProperty#addNegativeAssertion(OntIndividual, Literal)
     */
    OntNegativeAssertion.WithObjectProperty addNegativeAssertion(OntIndividual source, OntIndividual target);

    /**
     * Creates a property chain logical constructions
     * as a {@link OntList ontology []-list} of {@link OntObjectProperty Object Property Expression}s
     * that is attached to this Object Property Expression
     * at object positions with the predicate {@link OWL2#propertyChainAxiom owl:propertyChainAxiom}.
     * The resulting rdf-list will consist of all the elements of the specified collection
     * in the given order with the possibility of duplication.
     * Note: Any {@code null}s in a collection will cause {@link OntJenaException.IllegalArgument exception}.
     *
     * @param properties {@link Collection} (preferably {@link List}) of {@link OntObjectProperty object property expression}s
     * @return {@link OntList} of {@link OntObjectProperty}s
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#a_SubObjectPropertyOfChain">9.2.1 Object Subproperties</a>
     * @see #addPropertyChainAxiomStatement(Collection)
     * @see #addPropertyChain(Collection)
     * @see #propertyChains()
     * @see #findPropertyChain(RDFNode)
     */
    @SuppressWarnings("javadoc")
    OntList<OntObjectProperty> createPropertyChain(Collection<OntObjectProperty> properties);

    /**
     * Deletes the given property chain list including all its annotations.
     *
     * @param list {@link Resource} can be {@link OntList} or {@link RDFList}
     * @return <b>this</b> instance to allow cascading calls
     * @throws OntJenaException if in the whole graph, there is no property chain with the specified []-list
     * @see #clearPropertyChains()
     * @see #createPropertyChain(Collection)
     */
    OntObjectProperty removePropertyChain(Resource list) throws OntJenaException;

    @Override
    default Named asNamed() {
        return as(Named.class);
    }

    /**
     * Returns all ranges.
     * The statement pattern is {@code P rdfs:range C}, where {@code P} is this object property,
     * and {@code C} is one of the return class expressions.
     *
     * @return {@code Stream} of {@link OntClass}s
     */
    @Override
    default Stream<OntClass> ranges() {
        return objects(RDFS.range, OntClass.class).filter(OntClass::canAsSuperClass).map(OntClass::asSuperClass);
    }

    /**
     * Gets all direct or indirect ranges that present in RDF graph.
     * Indirect ranges are calculated using {@code OntClass.subClasses(true)} relationship.
     * For example, consider the following statements (if someone has some dog, then this dog is a Dog):
     * <pre>
     * {@code
     * :Dog rdf:type owl:Class .
     * :Labrador rdf:type owl:Class .
     * :Labrador rdfs:subClassOf :Dog .
     * :hasDog rdf:type owl:ObjectProperty .
     * :hasDog rdfs:range :Dog .
     * }
     * </pre>
     * from these statements it can be derived that if someone has some dog, then this dog can be a Labrador:
     * <pre>
     * {@code
     * :hasDog rdfs:domain :Labrador .
     * }
     * </pre>
     *
     * @param direct {@code boolean}
     * @return {@code Stream} of {@link OntClass}es, distinct
     */
    default Stream<OntClass> ranges(boolean direct) {
        if (direct) return ranges();
        return ranges().flatMap(d -> Stream.concat(Stream.of(d), d.subClasses(false))).distinct();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The pattern is {@code Pi rdfs:subPropertyOf Pj} where {@code Pi, Pj} are object properties.
     *
     * @return {@code Stream} of {@link OntObjectProperty}s
     * @see #subProperties(boolean)
     * @see OntProperty#superProperties(boolean)
     * @see #propertyChains()
     */
    @Override
    default Stream<OntObjectProperty> subProperties() {
        return subProperties(false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The pattern is {@code Pi rdfs:subPropertyOf Pj} where {@code Pi, Pj} are object properties.
     *
     * @return {@code Stream} of {@link OntObjectProperty}s
     * @see #superProperties(boolean)
     * @see #addSuperProperty(OntObjectProperty)
     * @see OntProperty#removeSuperProperty(Resource)
     * @see OntProperty#superProperties(boolean)
     * @see #propertyChains()
     */
    @Override
    default Stream<OntObjectProperty> superProperties() {
        return superProperties(false);
    }

    /**
     * Lists all {@code OntDisjoint} sections where this object property is a member.
     *
     * @return a {@code Stream} of {@link OntDisjoint.ObjectProperties}
     */
    @Override
    default Stream<OntDisjoint.ObjectProperties> disjoints() {
        return getModel().ontObjects(OntDisjoint.ObjectProperties.class).filter(d -> d.members().anyMatch(this::equals));
    }

    /**
     * Returns disjoint properties (statement: {@code P1 owl:propertyDisjointWith P2}).
     *
     * @return {@code Stream} of {@link OntObjectProperty}s
     * @see OntDataProperty#disjointProperties()
     * @see OntDisjoint.ObjectProperties
     */
    @Override
    default Stream<OntObjectProperty> disjointProperties() {
        return objects(OWL2.propertyDisjointWith, OntObjectProperty.class);
    }

    /**
     * Returns all equivalent object properties
     * (i.e. {@code Pi owl:equivalentProperty Pj}, where {@code Pi} - this property).
     *
     * @return {@code Stream} of {@link OntObjectProperty}s.
     * @see OntDataProperty#equivalentProperties()
     */
    @Override
    default Stream<OntObjectProperty> equivalentProperties() {
        return objects(OWL2.equivalentProperty, OntObjectProperty.class);
    }

    /**
     * Lists all object properties from the right part of the statement {@code _:this owl:inverseOf P}.
     * Please note: the return list items are not required to be {@link Inverse Inverse Object Property Expression}s.
     *
     * @return {@code Stream} of {@link OntObjectProperty}s (either {@link Inverse} or {@link Named})
     * @see Named#createInverse()
     */
    default Stream<OntObjectProperty> inverseProperties() {
        return objects(OWL2.inverseOf, OntObjectProperty.class);
    }

    /**
     * Returns all associated negative object property assertions.
     *
     * @return {@code Stream} of {@link OntNegativeAssertion.WithObjectProperty}s
     * @see OntDataProperty#negativeAssertions()
     * @see OntIndividual#negativeAssertions()
     */
    @Override
    default Stream<OntNegativeAssertion.WithObjectProperty> negativeAssertions() {
        return getModel().ontObjects(OntNegativeAssertion.WithObjectProperty.class).filter(a -> OntObjectProperty.this.equals(a.getProperty()));
    }

    /**
     * Returns all associated negative object property assertions for the specified source individual.
     *
     * @param source {@link OntIndividual}
     * @return {@code Stream} of {@link OntNegativeAssertion.WithObjectProperty}s
     * @see OntDataProperty#negativeAssertions(OntIndividual)
     * @see OntIndividual#negativeAssertions(OntRelationalProperty)
     */
    default Stream<OntNegativeAssertion.WithObjectProperty> negativeAssertions(OntIndividual source) {
        return negativeAssertions().filter(a -> a.getSource().equals(source));
    }

    /**
     * Finds the <b>first</b> {@code PropertyChain} logical construction
     * attached to this property for the specified []-list as an object.
     *
     * @param list {@link RDFNode}
     * @return {@code Optional} around the {@link OntList ontology []-list}
     * with {@link OntObjectProperty object property expression}s as items
     */
    default Optional<OntList<OntObjectProperty>> findPropertyChain(RDFNode list) {
        try (Stream<OntList<OntObjectProperty>> res = propertyChains().filter(r -> Objects.equals(r, list))) {
            return res.findFirst();
        }
    }

    /**
     * Deletes all property chain lists including their annotations.
     *
     * @return <b>this</b> instance to allow cascading calls
     * @see #removePropertyChain(Resource)
     * @see #createPropertyChain(Collection)
     */
    default OntObjectProperty clearPropertyChains() {
        propertyChains().toList().forEach(this::removePropertyChain);
        return this;
    }

    /**
     * Lists all members from the right part of statement {@code P owl:propertyChainAxiom ( P1 ... Pn )},
     * where {@code P} is this property.
     * Note: the result ignores repetitions, e.g., for
     * {@code SubObjectPropertyOf( ObjectPropertyChain( :hasParent :hasParent ) :hasGrandparent )},
     * it returns only {@code :hasParent} property.
     *
     * @return <b>distinct</b> {@code Stream} of all sub-{@link OntObjectProperty object properties},
     * possible empty in case of nil-list or if there is no property-chains at all
     * @see #propertyChains()
     */
    default Stream<OntObjectProperty> fromPropertyChain() {
        return propertyChains().flatMap(OntList::members).distinct();
    }

    /**
     * Creates a property chain {@link OntList ontology list}
     * and returns the statement {@code P owl:propertyChainAxiom ( P1 ... Pn )} to allow the addition of annotations.
     * See RDF Graph annotation specification, for example,
     * <a href="https://www.w3.org/TR/owl2-mapping-to-rdf/#Translation_of_Annotations">2.3.1 Axioms that Generate a Main Triple</a>.
     *
     * @param properties Array of {@link OntObjectProperty}s without {@code null}s
     * @return {@link OntStatement} to provide the ability to add annotations subsequently
     * @see #createPropertyChain(Collection)
     * @see #addPropertyChainAxiomStatement(Collection)
     */
    default OntStatement addPropertyChainAxiomStatement(OntObjectProperty... properties) {
        return addPropertyChainAxiomStatement(Arrays.asList(properties));
    }

    /**
     * Adds a new sub-property-of chain statement.
     *
     * @param properties Collection of {@link OntObjectProperty}s
     * @return {@link OntStatement} (i.e. {@code _:this owl:propertyChainAxiom ( ... )})
     * to provide the ability to add annotations subsequently
     * @see #createPropertyChain(Collection)
     * @see #addPropertyChainAxiomStatement(OntObjectProperty...)
     * @see <a href="https://www.w3.org/TR/owl2-mapping-to-rdf/#Translation_of_Annotations">2.3.1 Axioms that Generate a Main Triple</a>
     */
    default OntStatement addPropertyChainAxiomStatement(Collection<OntObjectProperty> properties) {
        return createPropertyChain(properties).getMainStatement();
    }

    /**
     * Creates the {@code P rdf:type owl:InverseFunctionalProperty} property declaration statement,
     * where {@code P} is this property.
     *
     * @return {@link OntStatement} to allow the subsequent addition of annotations
     * @see #setInverseFunctional(boolean)
     */
    default OntStatement addInverseFunctionalDeclaration() {
        return addStatement(RDF.type, OWL2.InverseFunctionalProperty);
    }

    /**
     * Creates the {@code P rdf:type owl:TransitiveProperty} property declaration statement,
     * where {@code P} is this property.
     *
     * @return {@link OntStatement} to allow the subsequent addition of annotations
     * @see #setTransitive(boolean)
     */
    default OntStatement addTransitiveDeclaration() {
        return addStatement(RDF.type, OWL2.TransitiveProperty);
    }

    /**
     * Creates the {@code P rdf:type owl:SymmetricProperty} property declaration statement,
     * where {@code P} is this property.
     *
     * @return {@link OntStatement} to allow the subsequent addition of annotations
     * @see #setSymmetric(boolean)
     */
    default OntStatement addSymmetricDeclaration() {
        return addStatement(RDF.type, OWL2.SymmetricProperty);
    }

    /**
     * Creates the {@code P rdf:type owl:AsymmetricProperty} property declaration statement,
     * where {@code P} is this property.
     *
     * @return {@link OntStatement} to allow the subsequent addition of annotations
     * @see #setAsymmetric(boolean)
     */
    default OntStatement addAsymmetricDeclaration() {
        return addStatement(RDF.type, OWL2.AsymmetricProperty);
    }

    /**
     * Creates the {@code P rdf:type owl:ReflexiveProperty} property declaration statement,
     * where {@code P} is this property.
     *
     * @return {@link OntStatement} to allow the subsequent addition of annotations
     * @see #setReflexive(boolean)
     */
    default OntStatement addReflexiveDeclaration() {
        return addStatement(RDF.type, OWL2.ReflexiveProperty);
    }

    /**
     * Creates the {@code P rdf:type owl:IrreflexiveProperty} property declaration statement,
     * where {@code P} is this property.
     *
     * @return {@link OntStatement} to allow the subsequent addition of annotations
     * @see #setIrreflexive(boolean)
     */
    default OntStatement addIrreflexiveDeclaration() {
        return addStatement(RDF.type, OWL2.IrreflexiveProperty);
    }

    /**
     * Adds a new inverse-of statement.
     *
     * @param other {@link OntObjectProperty}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     * @see #addInverseProperty(OntObjectProperty)
     * @see #inverseProperties()
     */
    default OntStatement addInverseOfStatement(OntObjectProperty other) {
        return addStatement(OWL2.inverseOf, other);
    }

    /**
     * Creates and returns a new {@link OWL2#equivalentProperty owl:equivalentProperty} statement
     * with the given property as an object and this property as a subject.
     *
     * @param other {@link OntObjectProperty}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     * @see #addEquivalentProperty(OntObjectProperty)
     * @see #removeEquivalentProperty(Resource)
     * @see OntDataProperty#addEquivalentPropertyStatement(OntDataProperty)
     */
    default OntStatement addEquivalentPropertyStatement(OntObjectProperty other) {
        return addStatement(OWL2.equivalentProperty, other);
    }

    /**
     * Adds a disjoint object property (i.e., the {@code _:this owl:propertyDisjointWith @other} statement).
     *
     * @param other {@link OntObjectProperty}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     * @see #addDisjointProperty(OntObjectProperty)
     * @see #removeDisjointProperty(Resource)
     * @see OntDataProperty#addPropertyDisjointWithStatement(OntDataProperty)
     * @see OntDisjoint.ObjectProperties
     */
    default OntStatement addPropertyDisjointWithStatement(OntObjectProperty other) {
        return addStatement(OWL2.propertyDisjointWith, other);
    }

    /**
     * Adds the given property as super property returning this property itself.
     *
     * @param property {@link OntObjectProperty}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see OntProperty#removeSuperProperty(Resource)
     */
    default OntObjectProperty addSuperProperty(OntObjectProperty property) {
        addSubPropertyOfStatement(property);
        return this;
    }

    /**
     * Adds the given property as sub property returning this property itself.
     *
     * @param property {@link OntObjectProperty}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #removeSubProperty(Resource)
     */
    default OntObjectProperty addSubProperty(OntObjectProperty property) {
        property.addSubPropertyOfStatement(this);
        return this;
    }

    /**
     * Adds a statement with the {@link RDFS#range} as predicate
     * and the specified {@link OntClass class expression} as an object.
     *
     * @param range {@link OntClass}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addRangeStatement(Resource)
     */
    default OntObjectProperty addRange(OntClass range) {
        addRangeStatement(range);
        return this;
    }

    /**
     * Adds a statement with the {@link RDFS#domain} as predicate
     * and the specified {@link OntClass class expression} as an object.
     *
     * @param ce {@link OntClass}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addDomainStatement(Resource)
     */
    @Override
    default OntObjectProperty addDomain(OntClass ce) {
        addDomainStatement(ce);
        return this;
    }

    /**
     * Adds a new {@link OWL2#equivalentProperty owl:equivalentProperty} statement.
     *
     * @param other {@link OntObjectProperty}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addEquivalentPropertyStatement(OntObjectProperty)
     * @see OntRelationalProperty#removeEquivalentProperty(Resource)
     * @see OntDataProperty#addEquivalentProperty(OntDataProperty)
     */
    default OntObjectProperty addEquivalentProperty(OntObjectProperty other) {
        addEquivalentPropertyStatement(other);
        return this;
    }

    /**
     * Adds a new {@link OWL2#propertyDisjointWith owl:propertyDisjointWith} statement
     * for this and the specified property.
     *
     * @param other {@link OntDataProperty}, not {@code null}
     * @return {@link OntDataProperty} <b>this</b> instance to allow cascading calls
     * @see #addPropertyDisjointWithStatement(OntObjectProperty)
     * @see OntDataProperty#addDisjointProperty(OntDataProperty)
     * @see OntRelationalProperty#removeDisjointProperty(Resource)
     * @see OntDisjoint.ObjectProperties
     */
    default OntObjectProperty addDisjointProperty(OntObjectProperty other) {
        addPropertyDisjointWithStatement(other);
        return this;
    }

    /**
     * Adds a new inverse-of a statement, returns this property instance.
     *
     * @param other {@link OntObjectProperty}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addInverseOfStatement(OntObjectProperty)
     * @see #removeInverseProperty(Resource)
     */
    default OntObjectProperty addInverseProperty(OntObjectProperty other) {
        addInverseOfStatement(other);
        return this;
    }

    /**
     * Adds a new sub-property-of a chain statement and returns this object itself.
     * Note: the method saves a collection order with possible duplicates.
     *
     * @param properties an {@code Array} of {@link OntObjectProperty object properties}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addPropertyChainAxiomStatement(Collection)
     * @see #addPropertyChain(Collection)
     */
    default OntObjectProperty addPropertyChain(OntObjectProperty... properties) {
        return addPropertyChain(Arrays.asList(properties));
    }

    /**
     * Adds a new sub-property-of a chain statement and returns this object itself.
     * Note: the method saves a collection order with possible duplicates.
     *
     * @param properties a {@code Collection} of {@link OntObjectProperty object properties}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addPropertyChainAxiomStatement(Collection)
     * @see #addPropertyChain(OntObjectProperty...)
     */
    default OntObjectProperty addPropertyChain(Collection<OntObjectProperty> properties) {
        createPropertyChain(properties);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntObjectProperty removeSuperProperty(Resource property) {
        remove(RDFS.subPropertyOf, property);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntObjectProperty removeSubProperty(Resource property) {
        getModel().statements(property, RDFS.subPropertyOf, this).toList().forEach(s -> getModel().remove(s.clearAnnotations()));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntObjectProperty removeEquivalentProperty(Resource property) {
        remove(OWL2.equivalentProperty, property);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntObjectProperty removeDisjointProperty(Resource property) {
        remove(OWL2.propertyDisjointWith, property);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntObjectProperty removeDomain(Resource domain) {
        remove(RDFS.domain, domain);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntObjectProperty removeRange(Resource range) {
        remove(RDFS.range, range);
        return this;
    }

    /**
     * Removes the statement with the predicate {@link OWL2#inverseOf owl:inverseOf}
     * and the given object property as an object.
     * If the argument is {@code null}, all {@code owl:inverseOf} statements will be removed for this object property.
     * No-op in case there is no {@code owl:inverseOf} statements.
     *
     * @param other {@link OntObjectProperty} or {@code null}  to remove all {@code owl:inverseOf} statements
     * @return <b>this</b> instance to allow cascading calls
     * @see #addInverseProperty(OntObjectProperty)
     */
    default OntObjectProperty removeInverseProperty(Resource other) {
        remove(OWL2.inverseOf, other);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntObjectProperty setFunctional(boolean functional) {
        if (functional) {
            addFunctionalDeclaration();
        } else {
            remove(RDF.type, OWL2.FunctionalProperty);
        }
        return this;
    }

    /**
     * Finds the <b>first</b> object property
     * from the right part of the statements {@code _:x owl:inverseOf PN} or {@code P1 owl:inverseOf P2}.
     * Here {@code _:x} is an anonymous object property expression (i.e. {@link Inverse Inverse Object Property}),
     * {@code PN} is a {@link Named named object property}
     * and {@code P1}, {@code P2} are any object property expressions.
     * What exactly is the first statement is defined at the level of model; in general, it is unpredictable.
     *
     * @return {@code Optional} of {@link OntObjectProperty}
     * @see #inverseProperties()
     * @see Inverse#getDirect()
     */
    default Optional<OntObjectProperty> inverseProperty() {
        try (Stream<OntObjectProperty> res = inverseProperties()) {
            return res.findFirst();
        }
    }

    /**
     * @return {@code true} iff it is an inverse functional property
     */
    default boolean isInverseFunctional() {
        return hasType(OWL2.InverseFunctionalProperty);
    }

    /**
     * Adds or removes the {@code P rdf:type owl:InverseFunctionalProperty} statement depending on the given flag
     * and returns this property to allow cascading calls.
     * Note: the statement is removed along with all its annotations.
     *
     * @param inverseFunctional if {@code true} the property must be inverse-functional
     * @return <b>this</b> instance to allow cascading calls
     * @see #addInverseFunctionalDeclaration()
     */
    default OntObjectProperty setInverseFunctional(boolean inverseFunctional) {
        if (inverseFunctional) {
            addInverseFunctionalDeclaration();
        } else {
            remove(RDF.type, OWL2.InverseFunctionalProperty);
        }
        return this;
    }

    /**
     * @return {@code true} iff it is a transitive property
     */
    default boolean isTransitive() {
        return hasType(OWL2.TransitiveProperty);
    }

    /**
     * Adds or removes the {@code P rdf:type owl:TransitiveProperty} statement depending on the given flag
     * and returns this property to allow cascading calls.
     * Note: the statement is removed along with all its annotations.
     *
     * @param transitive if {@code true} the property must be transitive
     * @return <b>this</b> instance to allow cascading calls
     * @see #addTransitiveDeclaration()
     */
    default OntObjectProperty setTransitive(boolean transitive) {
        if (transitive) {
            addTransitiveDeclaration();
        } else {
            remove(RDF.type, OWL2.TransitiveProperty);
        }
        return this;
    }

    /**
     * @return {@code true} iff it is a symmetric property
     */
    default boolean isSymmetric() {
        return hasType(OWL2.SymmetricProperty);
    }

    /**
     * Adds or removes the {@code P rdf:type owl:SymmetricProperty} statement depending on the given flag
     * and returns this property to allow cascading calls.
     * Note: the statement is removed along with all its annotations.
     *
     * @param symmetric if {@code true} the property must be symmetric
     * @return <b>this</b> instance to allow cascading calls
     * @see #addSymmetricDeclaration()
     */
    default OntObjectProperty setSymmetric(boolean symmetric) {
        if (symmetric) {
            addSymmetricDeclaration();
        } else {
            remove(RDF.type, OWL2.SymmetricProperty);
        }
        return this;
    }

    /**
     * @return {@code true} iff it is an asymmetric property
     */
    default boolean isAsymmetric() {
        return hasType(OWL2.AsymmetricProperty);
    }

    /**
     * Adds or removes the {@code P rdf:type owl:AsymmetricProperty} statement depending on the given flag
     * and returns this property to allow cascading calls.
     * Note: the statement is removed along with all its annotations.
     *
     * @param asymmetric if {@code true} the property must be asymmetric
     * @return <b>this</b> instance to allow cascading calls
     * @see #addAsymmetricDeclaration()
     */
    default OntObjectProperty setAsymmetric(boolean asymmetric) {
        if (asymmetric) {
            addAsymmetricDeclaration();
        } else {
            remove(RDF.type, OWL2.AsymmetricProperty);
        }
        return this;
    }

    /**
     * @return {@code true} iff it is a reflexive property
     */
    default boolean isReflexive() {
        return hasType(OWL2.ReflexiveProperty);
    }

    /**
     * Adds or removes the {@code P rdf:type owl:ReflexiveProperty} statement depending on the given flag
     * and returns this property to allow cascading calls.
     * Note: the statement is removed along with all its annotations.
     *
     * @param reflexive if {@code true} the property must be reflexive
     * @return <b>this</b> instance to allow cascading calls
     * @see #addReflexiveDeclaration()
     */
    default OntObjectProperty setReflexive(boolean reflexive) {
        if (reflexive) {
            addReflexiveDeclaration();
        } else {
            remove(RDF.type, OWL2.ReflexiveProperty);
        }
        return this;
    }

    /**
     * @return {@code true} iff it is an irreflexive property
     */
    default boolean isIrreflexive() {
        return hasType(OWL2.IrreflexiveProperty);
    }

    /**
     * Adds or removes the {@code P rdf:type owl:IrreflexiveProperty} statement depending on the given flag
     * and returns this property to allow cascading calls.
     * Note: the statement is removed along with all its annotations.
     *
     * @param irreflexive if {@code true} the property must be irreflexive
     * @return <b>this</b> instance to allow cascading calls
     * @see #addIrreflexiveDeclaration()
     */
    default OntObjectProperty setIrreflexive(boolean irreflexive) {
        if (irreflexive) {
            addIrreflexiveDeclaration();
        } else {
            remove(RDF.type, OWL2.IrreflexiveProperty);
        }
        return this;
    }

    /**
     * Represents a <a href="http://www.w3.org/TR/owl2-syntax/#Inverse_Object_Properties">ObjectInverseOf</a>.
     * Anonymous triple {@code _:x owl:inverseOf PN} which is also object property expression.
     */
    interface Inverse extends OntObjectProperty {

        /**
         * Returns a named object property companion.
         * Every {@link Inverse} property has its own {@link Named} property.
         * The triple pattern is {@code _:x owl:inverseOf PN}.
         *
         * @return {@link OntDataProperty}, not {@code null}
         */
        Named getDirect();
    }

    /**
     * Interface encapsulating an Ontology Named Object Property.
     * It is a URI-{@link Resource Resource} and an extension to the standard jena {@link Property}.
     * Also, It is an {@link OntEntity OWL Entity} and {@link OntRelationalProperty real ontology property}.
     *
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Object_Properties">5.3 Object Properties</a>
     */
    interface Named extends OntObjectProperty, OntNamedProperty<Named> {

        /**
         * Creates or finds an inverse of this property.
         * The searching is performed only in the base graph,
         * so it is possible to have more than one anonymous object property expressions
         * in case the named companion belongs to some subgraph.
         * For a single-graph model a named object property can be answered
         * by one and only one {@code Inverse} object property expression.
         *
         * @return {@link Inverse} - an anonymous {@link OntObjectProperty} resource (fresh or existing)
         */
        Inverse createInverse();

        @Override
        default Named asNamed() {
            return this;
        }
    }
}

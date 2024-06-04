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

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.stream.Stream;

/**
 * Interface encapsulating the Ontology (Named) Data Property.
 * This is an extension to the standard jena {@link Property},
 * the {@link OntEntity OWL Entity} and the {@link OntRelationalProperty realarional property} interfaces.
 * Range values for this property are {@link OntDataRange datarange} values
 * (as distinct from object property expression valued {@link OntObjectProperty properties}).
 * In OWL2, a Data Property cannot be anonymous.
 *
 * @see <a href="https://www.w3.org/TR/owl2-syntax/#Data_Properties">5.4 Data Properties</a>
 */
public interface OntDataProperty extends OntRelationalProperty, OntNamedProperty<OntDataProperty>, HasDisjoint<OntDataProperty> {

    /**
     * Adds a negative data property assertion.
     *
     * @param source {@link OntIndividual}, the source
     * @param target {@link Literal}, the target
     * @return {@link OntNegativeAssertion.WithDataProperty}
     * @see OntObjectProperty#addNegativeAssertion(OntIndividual, OntIndividual)
     */
    OntNegativeAssertion.WithDataProperty addNegativeAssertion(OntIndividual source, Literal target);

    /**
     * {@inheritDoc}
     *
     * @param direct {@code boolean} if {@code true} answers the directly adjacent properties in the sub-property relation:
     *               i.e. eliminate any properties for which
     *               there is a longer route to reach that parent under the sub-property relation
     * @return <b>distinct</b> {@code Stream} of datatype properties
     */
    @Override
    Stream<OntDataProperty> subProperties(boolean direct);

    /**
     * {@inheritDoc}
     *
     * @param direct {@code boolean}: if {@code true} answers the directly adjacent properties in the super-property relation,
     *               i.e. eliminate any property for which
     *               there is a longer route to reach that parent under the super-property relation               
     * @return <b>distinct</b> {@code Stream} of datatype properties
     */
    @Override
    Stream<OntDataProperty> superProperties(boolean direct);

    /**
     * {@inheritDoc}
     *
     * @return {@code Stream} of {@link OntNegativeAssertion.WithDataProperty}s
     * @see OntObjectProperty#negativeAssertions()
     */
    @Override
    default Stream<OntNegativeAssertion.WithDataProperty> negativeAssertions() {
        return getModel().ontObjects(OntNegativeAssertion.WithDataProperty.class).filter(a -> OntDataProperty.this.equals(a.getProperty()));
    }

    /**
     * Returns all associated negative data property assertions for the specified source individual.
     *
     * @param source {@link OntIndividual}
     * @return {@code Stream} of {@link OntNegativeAssertion.WithDataProperty}s.
     * @see OntObjectProperty#negativeAssertions(OntIndividual)
     */
    default Stream<OntNegativeAssertion.WithDataProperty> negativeAssertions(OntIndividual source) {
        return negativeAssertions()
                .filter(a -> a.getSource().equals(source));
    }

    /**
     * Returns all-property ranges (the statement pattern: {@code R rdfs:range D}).
     *
     * @return {@code Stream} of {@link OntDataRange}s
     */
    @Override
    default Stream<OntDataRange> ranges() {
        return objects(RDFS.range, OntDataRange.class);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The pattern is {@code Ri rdfs:subPropertyOf Rj} where {@code Ri, Rj} are data properties.
     *
     * @return {@code Stream} of {@link OntDataProperty}s
     * @see #subProperties(boolean)
     */
    @Override
    default Stream<OntDataProperty> subProperties() {
        return subProperties(false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The pattern is {@code Ri rdfs:subPropertyOf Rj} where {@code Ri, Rj} are data properties.
     *
     * @return {@code Stream} of {@link OntDataProperty}s
     * @see #subProperties(boolean)
     */
    @Override
    default Stream<OntDataProperty> superProperties() {
        return superProperties(false);
    }

    /**
     * Lists all {@code OntDisjoint} sections where this data-property is a member.
     *
     * @return a {@code Stream} of {@link OntDisjoint.DataProperties}
     */
    @Override
    default Stream<OntDisjoint.DataProperties> disjoints() {
        return getModel().ontObjects(OntDisjoint.DataProperties.class).filter(d -> d.members().anyMatch(this::equals));
    }

    /**
     * Returns disjoint properties.
     * The statement pattern is: {@code Ri owl:propertyDisjointWith Rj}, where {@code Ri} - this property,
     * and {@code Rj} - the data property to return.
     *
     * @return {@code Stream} of {@link OntDataProperty}s
     * @see OntObjectProperty#disjointProperties()
     * @see OntDisjoint.DataProperties
     */
    @Override
    default Stream<OntDataProperty> disjointProperties() {
        return objects(OWL2.propertyDisjointWith, OntDataProperty.class);
    }

    /**
     * Returns all equivalent data properties
     * The statement pattern is {@code Ri owl:equivalentProperty Rj},
     * where {@code Ri} - this property, {@code Rj} - the data property to return.
     *
     * @return {@code Stream} of {@link OntDataProperty}s
     * @see OntObjectProperty#equivalentProperties()
     */
    @Override
    default Stream<OntDataProperty> equivalentProperties() {
        return objects(OWL2.equivalentProperty, OntDataProperty.class);
    }

    /**
     * Creates and returns a new {@link OWL2#equivalentProperty owl:equivalentProperty} statement
     * with the given property as an object and this property as a subject.
     *
     * @param other {@link OntDataProperty}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     * @see #addEquivalentProperty(OntDataProperty)
     * @see #removeEquivalentProperty(Resource)
     * @see OntObjectProperty#addEquivalentPropertyStatement(OntObjectProperty)
     */
    default OntStatement addEquivalentPropertyStatement(OntDataProperty other) {
        return addStatement(OWL2.equivalentProperty, other);
    }

    /**
     * Adds a disjoint object property (i.e. the {@code _:this owl:propertyDisjointWith @other} statement).
     *
     * @param other {@link OntDataProperty}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     * @see #addDisjointProperty(OntDataProperty)
     * @see #removeDisjointProperty(Resource)
     * @see OntObjectProperty#addPropertyDisjointWithStatement(OntObjectProperty)
     * @see OntDisjoint.ObjectProperties
     */
    default OntStatement addPropertyDisjointWithStatement(OntDataProperty other) {
        return addStatement(OWL2.propertyDisjointWith, other);
    }

    /**
     * Adds the given property as super property returning this property itself.
     *
     * @param property {@link OntDataProperty}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #removeSuperProperty(Resource)
     */
    default OntDataProperty addSuperProperty(OntDataProperty property) {
        addSubPropertyOfStatement(property);
        return this;
    }

    /**
     * Adds the given property as sub property returning this property itself.
     *
     * @param property {@link OntDataProperty}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #removeSubProperty(Resource)
     */
    default OntDataProperty addSubProperty(OntDataProperty property) {
        property.addSubPropertyOfStatement(this);
        return this;
    }

    /**
     * Adds a statement with the {@link RDFS#range} as predicate
     * and the specified {@link OntDataRange data range} as an object.
     *
     * @param range {@link OntDataRange}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addRangeStatement(Resource)
     */
    default OntDataProperty addRange(OntDataRange range) {
        addRangeStatement(range);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntDataProperty addDomain(OntClass ce) {
        addDomainStatement(ce);
        return this;
    }

    /**
     * Adds a new {@link OWL2#equivalentProperty owl:equivalentProperty} statement.
     *
     * @param other {@link OntDataProperty}, not {@code null}
     * @return {@link OntDataProperty} <b>this</b> instance to allow cascading calls
     * @see #addEquivalentPropertyStatement(OntDataProperty)
     * @see OntRelationalProperty#removeEquivalentProperty(Resource)
     * @see OntObjectProperty#addEquivalentProperty(OntObjectProperty)
     */
    default OntDataProperty addEquivalentProperty(OntDataProperty other) {
        addEquivalentPropertyStatement(other);
        return this;
    }

    /**
     * Adds a new {@link OWL2#propertyDisjointWith owl:propertyDisjointWith} statement
     * for this and the specified property.
     *
     * @param other {@link OntDataProperty}, not {@code null}
     * @return {@link OntDataProperty} <b>this</b> instance to allow cascading calls
     * @see #addPropertyDisjointWithStatement(OntDataProperty)
     * @see OntObjectProperty#addDisjointProperty(OntObjectProperty)
     * @see OntRelationalProperty#removeDisjointProperty(Resource)
     * @see OntDisjoint.DataProperties
     */
    default OntDataProperty addDisjointProperty(OntDataProperty other) {
        addPropertyDisjointWithStatement(other);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntDataProperty removeSuperProperty(Resource property) {
        remove(RDFS.subPropertyOf, property);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntDataProperty removeSubProperty(Resource property) {
        getModel().statements(property, RDFS.subPropertyOf, this).toList().forEach(s -> getModel().remove(s.clearAnnotations()));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntDataProperty removeDomain(Resource domain) {
        remove(RDFS.domain, domain);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntDataProperty removeRange(Resource range) {
        remove(RDFS.range, range);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntDataProperty removeEquivalentProperty(Resource property) {
        remove(OWL2.equivalentProperty, property);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntDataProperty removeDisjointProperty(Resource property) {
        remove(OWL2.propertyDisjointWith, property);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntDataProperty setFunctional(boolean functional) {
        if (functional) {
            addFunctionalDeclaration();
        } else {
            remove(RDF.type, OWL2.FunctionalProperty);
        }
        return this;
    }

}

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
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface for named and anonymous individuals.
 */
@SuppressWarnings("rawtypes")
public interface OntIndividual extends OntObject, AsNamed<OntIndividual.Named>, HasDisjoint<OntIndividual> {

    /**
     * Removes a class assertion statement for the given class.
     * Like others methods {@code #remove..(..)} (see {@link #remove(Property, RDFNode)}),
     * this operation does nothing in case no match found
     * and in case {@code null} is specified
     * it removes all class assertion statements including all their annotations.
     * To delete the individual with its content
     * the method {@link OntModel#removeOntObject(OntObject)} can be used.
     *
     * @param clazz {@link OntClass} or {@code null} to remove all class assertions
     * @return <b>this</b> instance to allow cascading calls
     * @see #attachClass(OntClass)
     * @see #addClassAssertion(OntClass)
     * @see OntModel#removeOntObject(OntObject)
     */
    OntIndividual detachClass(Resource clazz);

    /**
     * Answers a {@code Stream} over the class expressions to which this individual belongs,
     * including super-classes if the flag {@code direct} is {@code false}.
     * If the flag {@code direct} is {@code true}, then only direct types are returned,
     * and the method is effectively equivalent to the method {@link #classes()}.
     * See also {@link OntClass#superClasses(boolean)}.
     *
     * @param direct if {@code true}, only answers those {@link OntClass}s that are direct types of this individual,
     *               not the superclasses of the class etc
     * @return <b>distinct</b> {@code Stream} of {@link OntClass class expressions}
     * @see #classes()
     * @see OntClass#superClasses(boolean)
     */
    Stream<OntClass> classes(boolean direct);

    /**
     * {@inheritDoc}
     * For individuals, content also includes negative property assertion statements.
     *
     * @return {@code Stream} of content {@link OntStatement}s
     */
    @Override
    Stream<OntStatement> content();

    @Override
    default Named asNamed() {
        return as(Named.class);
    }

    /**
     * Returns all class types (direct and indirect).
     *
     * @return {@code Stream} of {@link OntClass}s
     */
    default Stream<OntClass> classes() {
        return classes(false);
    }

    /**
     * Answers {@code true} if the given class is in the class-type closure.
     *
     * @param clazz  {@link OntClass} to test
     * @param direct see {@link OntIndividual#classes(boolean)}
     * @return true if the specified class found
     */
    default boolean hasOntClass(OntClass clazz, boolean direct) {
        Objects.requireNonNull(clazz);
        if (!clazz.canAsAssertionClass()) {
            return false;
        }
        try (Stream<OntClass> classes = classes(direct)) {
            return classes.anyMatch(clazz::equals);
        }
    }

    /**
     * Answers a class to which this individual belongs,
     * If there is more than one such class, an arbitrary selection is made.
     *
     * @return {@link Optional} wrapping {@code OntClass}
     */
    default Optional<OntClass> ontClass() {
        try (Stream<OntClass> classes = objects(RDF.type, OntClass.class)) {
            return classes.findFirst();
        }
    }

    /**
     * Lists all same individuals.
     * The pattern to search for is {@code ai owl:sameAs aj}, where {@code ai} is this individual.
     *
     * @return {@code Stream} of {@link OntIndividual}s
     */
    default Stream<OntIndividual> sameIndividuals() {
        return objects(OWL2.sameAs, OntIndividual.class);
    }

    /**
     * Lists all {@code OntDisjoint} sections where this individual is a member.
     *
     * @return a {@code Stream} of {@link OntDisjoint.Individuals}
     */
    @Override
    default Stream<OntDisjoint.Individuals> disjoints() {
        return getModel().ontObjects(OntDisjoint.Individuals.class).filter(d -> d.members().anyMatch(this::equals));
    }

    /**
     * Lists all different individuals.
     * The pattern to search for is {@code thisIndividual owl:differentFrom otherIndividual},
     * where {@code otherIndividual} is one of the returned.
     *
     * @return {@code Stream} of {@link OntIndividual}s
     * @see OntDisjoint.Individuals
     */
    default Stream<OntIndividual> differentIndividuals() {
        return objects(OWL2.differentFrom, OntIndividual.class);
    }

    /**
     * Lists all positive assertions for this individual.
     *
     * @return {@code Stream} of {@link OntStatement}s
     */
    default Stream<OntStatement> positiveAssertions() {
        return statements().filter(s -> s.getPredicate().canAs(OntNamedProperty.class));
    }

    /**
     * Lists all positive property assertions for this individual and the given predicate.
     *
     * @param predicate {@link OntNamedProperty} or {@code null}
     * @return {@code Stream} of {@link OntStatement}s
     */
    default Stream<OntStatement> positiveAssertions(OntNamedProperty predicate) {
        return statements(predicate);
    }

    /**
     * Lists all negative property assertions for this individual.
     *
     * @return {@code Stream} of {@link OntNegativeAssertion negative property assertion}s
     */
    default Stream<OntNegativeAssertion> negativeAssertions() {
        return getModel().statements(null, OWL2.sourceIndividual, this)
                .map(x -> x.getSubject().getAs(OntNegativeAssertion.class))
                .filter(Objects::nonNull);
    }

    /**
     * Lists all negative property assertions for this individual and the given property.
     *
     * @param property {@link OntRelationalProperty} or {@code null}
     * @return {@code Stream} of {@link OntNegativeAssertion negative property assertion}s
     */
    default Stream<OntNegativeAssertion> negativeAssertions(OntRelationalProperty property) {
        Stream<OntNegativeAssertion> res = negativeAssertions();
        if (property == null) {
            return res;
        }
        return res.filter(x -> property.equals(x.getProperty()));
    }

    /**
     * Creates and returns a class-assertion statement {@code a rdf:type C}, where {@code a} is this individual.
     *
     * @param clazz {@link OntClass}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     * @see #attachClass(OntClass)
     * @see #detachClass(Resource)
     */
    default OntStatement addClassAssertion(OntClass clazz) {
        return addStatement(RDF.type, clazz);
    }

    /**
     * Adds a {@link OWL2#differentFrom owl:differentFrom} individual statement.
     *
     * @param other {@link OntIndividual}, not {@code null}
     * @return {@link OntStatement} to provide the ability to add annotations subsequently
     * @see #addDifferentIndividual(OntIndividual)
     * @see #removeDifferentIndividual(Resource)
     * @see OntDisjoint.Individuals
     */
    default OntStatement addDifferentFromStatement(OntIndividual other) {
        return addStatement(OWL2.differentFrom, other);
    }

    /**
     * Adds a same individual reference.
     *
     * @param other {@link OntIndividual}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     * @see #addSameIndividual(OntIndividual)
     * @see #removeSameIndividual(Resource)
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Individual_Equality">9.6.1 Individual Equality</a>
     */
    default OntStatement addSameAsStatement(OntIndividual other) {
        return addStatement(OWL2.sameAs, other);
    }

    /**
     * Adds a type (class expression) to this individual.
     *
     * @param clazz {@link OntClass}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addClassAssertion(OntClass)
     * @see #detachClass(Resource)
     */
    default OntIndividual attachClass(OntClass clazz) {
        addClassAssertion(clazz);
        return this;
    }

    /**
     * Adds a {@link OWL2#differentFrom owl:differentFrom} individual statement
     * and returns this object itself to allow cascading calls.
     *
     * @param other {@link OntIndividual}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addDifferentFromStatement(OntIndividual)
     * @see #removeDifferentIndividual(Resource)
     * @see OntDisjoint.Individuals
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Individual_Inequality">9.6.2 Individual Inequality</a>
     */
    default OntIndividual addDifferentIndividual(OntIndividual other) {
        addDifferentFromStatement(other);
        return this;
    }

    /**
     * Adds a {@link OWL2#sameAs owl:sameAs} individual statement
     * and returns this object itself to allow cascading calls.
     *
     * @param other other {@link OntIndividual}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addSameAsStatement(OntIndividual)
     * @see #removeSameIndividual(Resource)
     */
    default OntIndividual addSameIndividual(OntIndividual other) {
        addSameAsStatement(other);
        return this;
    }

    /**
     * Adds annotation assertion {@code AnnotationAssertion(A s t)}.
     * In general case it is {@code s A t}, where {@code s} is IRI or anonymous individual,
     * {@code A} - annotation property, and {@code t} - IRI, anonymous individual, or literal.
     *
     * @param property {@link OntAnnotationProperty}
     * @param value    {@link RDFNode} (IRI, anonymous individual, or literal)
     * @return this individual to allow cascading calls
     * @see #addAnnotation(OntAnnotationProperty, RDFNode)
     * @see #removeAssertion(OntNamedProperty, RDFNode)
     */
    default OntIndividual addAssertion(OntAnnotationProperty property, RDFNode value) {
        return addProperty(property, value);
    }

    /**
     * Adds a positive data property assertion {@code a R v}.
     *
     * @param property {@link OntDataProperty}
     * @param value    {@link Literal}
     * @return this individual to allow cascading calls
     * @see #removeAssertion(OntNamedProperty, RDFNode)
     */
    default OntIndividual addAssertion(OntDataProperty property, Literal value) {
        return addProperty(property, value);
    }

    /**
     * Adds a positive object property assertion {@code a1 PN a2}.
     *
     * @param property {@link OntObjectProperty.Named} named object property
     * @param value    {@link OntIndividual} other individual
     * @return this individual to allow cascading calls
     * @see #removeAssertion(OntNamedProperty, RDFNode)
     */
    default OntIndividual addAssertion(OntObjectProperty.Named property, OntIndividual value) {
        return addProperty(property, value);
    }

    /**
     * Adds a property assertion statement.
     * <b>Caution</b>: this method offers a way to add a statement that is contrary to the OWL2 specification.
     * For example, it is possible to add {@link OntObjectProperty.Named object property}-{@link Literal literal} pair,
     * that is not object property assertion.
     *
     * @param property {@link OntNamedProperty}, not {@code null}
     * @param value    {@link RDFNode}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see Resource#addProperty(Property, RDFNode)
     * @see #removeAssertion(OntNamedProperty, RDFNode)
     */
    default OntIndividual addProperty(OntNamedProperty property, RDFNode value) {
        addStatement(property, value);
        return this;
    }

    /**
     * Adds a negative object property assertion.
     * <pre>
     * Functional syntax: {@code NegativeObjectPropertyAssertion(P a1 a2)}
     * RDF Syntax:
     * {@code
     * _:x rdf:type owl:NegativePropertyAssertion .
     * _:x owl:sourceIndividual a1 .
     * _:x owl:assertionProperty P .
     * _:x owl:targetIndividual a2 .
     * }
     * </pre>
     *
     * @param property {@link OntObjectProperty}
     * @param value    {@link OntIndividual} other individual
     * @return <b>this</b> individual to allow cascading calls
     */
    default OntIndividual addNegativeAssertion(OntObjectProperty property, OntIndividual value) {
        property.addNegativeAssertion(this, value);
        return this;
    }

    /**
     * Adds a negative data property assertion.
     * <pre>
     * Functional syntax: {@code NegativeDataPropertyAssertion(R a v)}
     * RDF Syntax:
     * {@code
     * _:x rdf:type owl:NegativePropertyAssertion.
     * _:x owl:sourceIndividual a .
     * _:x owl:assertionProperty R .
     * _:x owl:targetValue v .
     * }
     * </pre>
     *
     * @param property {@link OntDataProperty}
     * @param value    {@link Literal}
     * @return <b>this</b> individual to allow cascading calls
     */
    default OntIndividual addNegativeAssertion(OntDataProperty property, Literal value) {
        property.addNegativeAssertion(this, value);
        return this;
    }

    /**
     * Removes a positive property assertion including its annotation.
     *
     * @param property {@link OntNamedProperty}, can be {@code null} to remove all positive property assertions
     * @param value    {@link RDFNode} (either {@link OntIndividual} or {@link Literal}),
     *                 can be {@code null} to remove all assertions for the predicate {@code property}
     * @return <b>this</b> instance to allow cascading calls
     * @see OntObject#remove(Property, RDFNode)
     * @see #addProperty(OntNamedProperty, RDFNode)
     */
    default OntIndividual removeAssertion(OntNamedProperty property, RDFNode value) {
        statements(property)
                .filter(x ->
                        x.getPredicate().canAs(OntNamedProperty.class) && (value == null || value.equals(x.getObject()))
                )
                .toList()
                .forEach(x -> x.getModel().remove(x.clearAnnotations()));
        return this;
    }

    /**
     * Removes a negative property assertion including its annotation.
     *
     * @param property {@link OntNamedProperty}, can be {@code null} to remove all negative property assertions
     * @param value    {@link RDFNode} (either {@link OntIndividual} or {@link Literal}),
     *                 can be {@code null} to remove all assertions for the predicate {@code property}
     * @return <b>this</b> instance to allow cascading calls
     */
    default OntIndividual removeNegativeAssertion(OntRelationalProperty property, RDFNode value) {
        negativeAssertions(property)
                .filter(x -> value == null || value.equals(x.getTarget()))
                .toList()
                .forEach(x -> getModel().removeOntObject(x));
        return this;
    }

    /**
     * Removes a different individual statement for this and specified individuals,
     * including the statement's annotation.
     * No-op in case no different individuals are found.
     * Removes all triples with the predicate {@code owl:differentFrom} if {@code null} is specified.
     *
     * @param other {@link Resource} or {@code null} to remove all different individuals
     * @return <b>this</b> instance to allow cascading calls
     * @see #addDifferentFromStatement(OntIndividual)
     * @see #addDifferentIndividual(OntIndividual)
     * @see OntDisjoint.Individuals
     */
    default OntIndividual removeDifferentIndividual(Resource other) {
        remove(OWL2.differentFrom, other);
        return this;
    }

    /**
     * Removes a same individual statement for this and specified individuals,
     * including the statement's annotation.
     * No-op in case no same individuals are found.
     * Removes all triples with the predicate {@code owl:sameAs} if {@code null} is specified.
     *
     * @param other {@link Resource} or {@code null} to remove all same individuals
     * @return <b>this</b> instance to allow cascading calls
     * @see #addSameAsStatement(OntIndividual)
     * @see #addSameIndividual(OntIndividual)
     */
    default OntIndividual removeSameIndividual(Resource other) {
        remove(OWL2.sameAs, other);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual addComment(String txt) {
        return addComment(txt, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual addComment(String txt, String lang) {
        return annotate(getModel().getRDFSComment(), txt, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual addLabel(String txt) {
        return addLabel(txt, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual addLabel(String txt, String lang) {
        return annotate(getModel().getRDFSLabel(), txt, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual annotate(OntAnnotationProperty predicate, String txt, String lang) {
        return annotate(predicate, getModel().createLiteral(txt, lang));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual annotate(OntAnnotationProperty predicate, RDFNode value) {
        addAnnotation(predicate, value);
        return this;
    }

    /**
     * An interface for <b>Named</b> Individual which is an {@link OWL2 Entity OntEntity}.
     *
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Named_Individuals">5.6.1 Named Individuals</a>
     */
    interface Named extends OntIndividual, OntEntity {

        @Override
        default Named asNamed() {
            return this;
        }
    }

    /**
     * An interface for Anonymous Individuals.
     * The anonymous individual is a blank node ({@code _:a}) which satisfies one of the following conditions:
     * <ul>
     * <li>it has a class declaration (i.e. there is a triple {@code _:a rdf:type C},
     * where {@code C} is a {@link OntClass class expression})</li>
     * <li>it is a subject or an object in a statement with predicate
     * {@link OWL2#sameAs owl:sameAs} or {@link OWL2#differentFrom owl:differentFrom}</li>
     * <li>it is contained in a {@code rdf:List} with predicate {@code owl:distinctMembers} or {@code owl:members}
     * in a blank node with {@code rdf:type = owl:AllDifferent}, see {@link OntDisjoint.Individuals}</li>
     * <li>it is contained in a {@code rdf:List} with predicate {@code owl:oneOf}
     * in a blank node with {@code rdf:type = owl:Class}, see {@link OntClass.OneOf}</li>
     * <li>it is a part of {@link OntNegativeAssertion owl:NegativePropertyAssertion} section with predicates
     * {@link OWL2#sourceIndividual owl:sourceIndividual} or {@link OWL2#targetIndividual owl:targetIndividual}</li>
     * <li>it is an object with predicate {@code owl:hasValue} inside {@code _:x rdf:type owl:Restriction}
     * (see {@link OntClass.ObjectHasValue Object Property HasValue Restriction})</li>
     * <li>it is a subject or an object in a statement where predicate is
     * an uri-resource with {@code rdf:type = owl:AnnotationProperty}
     * (i.e. {@link OntAnnotationProperty annotation property} assertion {@code s A t})</li>
     * <li>it is a subject in a triple which corresponds data property assertion {@code _:a R v}
     * (where {@code R} is a {@link OntDataProperty datatype property}, {@code v} is a {@link Literal literal})</li>
     * <li>it is a subject or an object in a triple which corresponds object property assertion {@code _:a1 PN _:a2}
     * (where {@code PN} is a {@link OntObjectProperty.Named named object property}, and {@code _:ai} are individuals)</li>
     * </ul>
     *
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Anonymous_Individuals">5.6.2 Anonymous Individuals</a>
     */
    interface Anonymous extends OntIndividual {

        /**
         * {@inheritDoc}
         * For an anonymous individual, a primary class assertion is also a definition, so its deletion is prohibited.
         *
         * @param clazz {@link OntClass}, not {@code null}
         * @return <b>this</b> instance to allow cascading calls
         * @throws OntJenaException in case the individual has only one class assertion, and it is for the given class
         */
        @Override
        Anonymous detachClass(Resource clazz) throws OntJenaException;
    }

}

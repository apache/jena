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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.stream.Stream;

/**
 * An abstraction for any Ontology Property Expression.
 * In OWL2 there are four such property expressions:
 * Data Property, Object Property (OWL Entity and InverseOf) and Annotation Property.
 *
 * @see <a href="https://www.w3.org/TR/owl2-quick-reference/">2.2 Properties</a>
 * @see OntObjectProperty
 * @see OntAnnotationProperty
 * @see OntDataProperty
 */
public interface OntProperty extends OntObject {

    /**
     * Returns a named part of this property expression.
     *
     * @return {@link Property}
     */
    Property asProperty();

    /**
     * Answers a {@code Stream} over all the properties that are declared to be sub-properties of this property.
     * Each element of the {@code Stream} will have the same type as this property instance:
     * if it is datatype property the method will return only data properties, etc.
     * The parameter {@code direct} controls selectivity over the properties that appear in the {@code Stream}.
     * Consider the following scenario:
     * <pre>{@code
     *  :D rdfs:subPropertyOf :C .
     *  :C rdfs:subPropertyOf :A .
     *  :B rdfs:subPropertyOf :A .
     * }</pre>
     * (so {@code A} has two sub-properties, {@code B} and {@code C}, and {@code C} has sub-property {@code D})
     * In a raw model, with no inference support, listing the sub-property of {@code A} will answer {@code B} and {@code C}.
     * In an inferencing model, {@code rdfs:subPropertyOf} is known to be transitive, so
     * the sub-properties iterator will include {@code D}.
     * The {@code direct} sub-properties are those members of the closure of the subPropertyOf relation,
     * restricted to properties that cannot be reached by a longer route,
     * i.e., the ones that are <em>directly</em> adjacent to the given root.
     * Thus, the direct sub-properties of {@code A} are {@code B} and {@code C} only, and not {@code D} - even in an inferencing graph.
     * Note that this is different from the entailments from the raw graph.
     * Suppose we add to this example:
     * <pre>{@code
     *   :D rdfs:subPropertyOf :A.
     * }</pre>
     * Now, in the raw graph, {@code A} has sub-property {@code C}.
     * But the direct sub-properties of {@code A} remain {@code B} and {@code C},
     * since there is a longer path {@code A-C-D} that means that {@code D} is not a direct sub-property of {@code A}.
     * The assertion in the raw graph that {@code A} has sub-property {@code D} is essentially redundant,
     * since this can be inferred from the closure of the graph.
     *
     * @param direct {@code boolean} - if {@code true} answers the directly adjacent properties in the sub-property relation:
     *               i.e. eliminate any properties for which there is a longer route to reach that parent under the sub-property relation;
     *               if {@code false} answers all sub-properties found by inferencer, which usually means entire hierarchy down the tree;
     *               this property is not included
     * @return <b>distinct</b> {@code Stream} of properties with the same type as this property
     * @see #superProperties(boolean)
     */
    Stream<? extends OntProperty> subProperties(boolean direct);

    /**
     * Answers a {@code Stream} over all the properties that are declared to be super-properties of this property.
     * Each element of the {@code Stream} will have the same type as this property instance:
     * if it is datatype property the method will return only data properties, etc.
     * The parameter {@code direct} controls selectivity over the properties that appear in the {@code Stream}.
     * Consider the following scenario:
     * <pre>{@code
     *  :A rdfs:subPropertyOf :B .
     *  :A rdfs:subPropertyOf :C .
     *  :C rdfs:subPropertyOf :D .
     * } </pre>
     * (so {@code A} has super-property {@code B} and {@code C}, and {@code C} has super-property {@code D})
     * In a raw model, with no inference support, listing the super-property of {@code A} will answer {@code B} and {@code C}.
     * In an inferencing model, {@code rdfs:subPropertyOf} is known to be transitive,
     * so the super-property iterator will include {@code D}.
     * The {@code direct} super-properties are those members of the closure of the inverse-subPropertyOf relation,
     * restricted to properties that cannot be reached by a longer route,
     * i.e., the ones that are <em>directly</em> adjacent to the given root.
     * Thus, the direct super-property of {@code A} are {@code B} and {@code C} only, and not {@code D} - even in an inferencing graph.
     * Note that this is different from the entailments from the raw graph.
     * Suppose we add to this example:
     * <pre>{@code
     *   :A rdfs:subPropertyOf :D .
     * }</pre>
     * Now, in the raw graph, {@code A} has super-property {@code B}, {@code C}, {@code D}.
     * But the direct super-property of {@code A} remain only {@code B} and C,
     * since there is a longer path {@code A-C-D} that means that {@code D} is not a direct super-property of {@code A}.
     * The assertion in the raw graph that {@code A} has super-property {@code D} is essentially redundant,
     * since this can be inferred from the closure of the graph.
     *
     * @param direct {@code boolean}: if {@code true} answers the directly adjacent properties in the super-property relation,
     *               i.e. eliminate any property for which there is a longer route to reach that parent under the super-property relation;
     *               if {@code false} answers all super-properties found by inferencer, which usually means entire hierarchy up the tree;
     *               this instance is not included
     * @return <b>distinct</b> {@code Stream} of properties with the same type as this property
     * @see #superProperties()
     * @see #subProperties(boolean)
     */
    Stream<? extends OntProperty> superProperties(boolean direct);

    /**
     * Lists all property domains.
     *
     * @return {@code Stream} of {@link Resource}s
     * @see OntAnnotationProperty#domains()
     * @see OntObjectProperty#domains()
     * @see OntDataProperty#domains()
     */
    Stream<? extends Resource> domains();

    /**
     * Lists all property ranges.
     *
     * @return {@code Stream} of {@link Resource}s
     * @see OntAnnotationProperty#ranges()
     * @see OntRelationalProperty#ranges()
     */
    Stream<? extends Resource> ranges();

    /**
     * Answers a {@code Stream} of all the classes in this ontology,
     * such that each returned class has this property as one of its properties in
     * {@link OntClass#declaredProperties(boolean)}.
     * This simulates a frame-like view of properties and classes;
     * for more details see the
     * <a href="https://jena.apache.org/documentation/notes/rdf-frames.html">Apache Jena: Presenting RDF as frames</a>
     *
     * @param direct {@code boolean} if {@code true}, use only <em>direct</em> associations between classes and properties
     * @return a {@code Stream} of the classes having this property as one of their declared properties
     */
    Stream<OntClass> declaringClasses(boolean direct);

    /**
     * Lists all the properties that are declared to be sub-properties of
     * this property (directly or indirectly).
     * Note: the return elements have the same type as this instance.
     * <p>
     * Equivalent to {@code this.subProperties(false)}.
     *
     * @return {@code Stream} of {@link OntProperty ont-properties}
     * @see #superProperties(boolean)
     * @see OntAnnotationProperty#superProperties()
     * @see OntRelationalProperty#superProperties()
     */
    default Stream<? extends OntProperty> subProperties() {
        return subProperties(false);
    }

    /**
     * Lists all the properties that are declared to be super-properties of
     * this property (directly or indirectly).
     * Note: the return elements have the same type as this instance.
     * <p>
     * Equivalent to {@code this.superProperties(false)}.
     *
     * @return {@code Stream} of {@link OntProperty ont-properties}
     * @see #superProperties(boolean)
     * @see OntAnnotationProperty#superProperties()
     * @see OntRelationalProperty#superProperties()
     */
    default Stream<? extends OntProperty> superProperties() {
        return superProperties(false);
    }

    /**
     * Answers {@code true}
     * if the given property is a sub-property of this property.
     * See {@link #subProperties(boolean)} for a full explanation of the direct parameter.
     *
     * @param property a {@link OntProperty} to test
     * @param direct {@code boolean}; If true, only search the properties
     *               that are directly adjacent to this property in the class hierarchy
     * @return {@code boolean}
     */
    default boolean hasSubProperty(OntProperty property, boolean direct) {
        return property.hasSuperProperty(this, direct);
    }

    /**
     * Answers {@code true}
     * if the given property is a super-property of this property.
     * See {@link #superProperties(boolean)} for a full explanation of the direct parameter.
     *
     * @param property a {@link OntProperty} to test
     * @param direct {@code boolean}; If true, only search the properties
     *               that are directly adjacent to this property in the class hierarchy
     * @return {@code boolean}
     */
    default boolean hasSuperProperty(OntProperty property, boolean direct) {
        return equals(property) || superProperties(direct).anyMatch(property::equals);
    }

    /**
     * Adds the given property as super property returning a new statement to annotate.
     * The triple pattern is {@code $this rdfs:subPropertyOf $property}).
     *
     * @param property {@link Resource}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     */
    default OntStatement addSubPropertyOfStatement(Resource property) {
        return addStatement(RDFS.subPropertyOf, property);
    }

    /**
     * Adds range statement {@code $this rdfs:range $range}.
     *
     * @param range {@link Resource}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     */
    default OntStatement addRangeStatement(Resource range) {
        return addStatement(RDFS.range, range);
    }

    /**
     * Adds a statement {@code $this rdfs:domain $domain}.
     *
     * @param domain {@link Resource}, not null
     * @return {@link OntStatement} to allow the subsequent addition of annotations
     */
    default OntStatement addDomainStatement(Resource domain) {
        return addStatement(RDFS.domain, domain);
    }

    /**
     * Removes the specified domain resource (predicate is {@link RDFS#domain rdfs:domain}),
     * including the corresponding statement's annotations.
     * No-op in case no such domain is found.
     * Removes all domains if {@code null} is specified.
     *
     * @param domain {@link Resource}, or {@code null} to remove all domains
     * @return <b>this</b> instance to allow cascading calls
     */
    default OntProperty removeDomain(Resource domain) {
        remove(RDFS.domain, domain);
        return this;
    }

    /**
     * Removes the specified range resource (predicate is {@link RDFS#range rdfs:range}),
     * including the corresponding statement's annotations.
     * No-op in case no such range is found.
     * Removes all ranges if {@code null} is specified.
     *
     * @param range {@link Resource}, or {@code null} to remove all ranges
     * @return <b>this</b> instance to allow cascading calls
     */
    default OntProperty removeRange(Resource range) {
        remove(RDFS.range, range);
        return this;
    }

    /**
     * Removes the specified super property (predicate is {@link RDFS#subPropertyOf rdfs:subPropertyOf}),
     * including the corresponding statement's annotations.
     * No-op in case no such super-property is found.
     * If {@code null} is specified,
     * the method removes all triples with predicate {@code rdfs:subPropertyOf} and this property as a subject.
     *
     * @param property {@link Resource} or {@code null} to remove all direct super properties
     * @return <b>this</b> instance to allow cascading calls
     */
    default OntProperty removeSuperProperty(Resource property) {
        remove(RDFS.subPropertyOf, property);
        return this;
    }

    /**
     * Removes the specified sub property (predicate is {@link RDFS#subPropertyOf rdfs:subPropertyOf}),
     * including the corresponding statement's annotations.
     * No-op in case no such super-property is found.
     * If {@code null} is specified,
     * the method removes all triples with predicate {@code rdfs:subPropertyOf} and this property as an object.
     *
     * @param property {@link Resource} or {@code null} to remove all direct super properties
     * @return <b>this</b> instance to allow cascading calls
     */
    default OntProperty removeSubProperty(Resource property) {
        getModel().statements(property, RDFS.subPropertyOf, this).toList().forEach(s -> getModel().remove(s.clearAnnotations()));
        return this;
    }
}

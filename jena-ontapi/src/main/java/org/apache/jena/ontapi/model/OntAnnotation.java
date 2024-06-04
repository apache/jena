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

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.OWL2;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A Bulk Annotation {@link OntObject Ontology Object}.
 * It's an anonymous jena-resource with one of the two types:
 * <ul>
 * <li>{@link OWL2#Axiom owl:Axiom} for root annotations, it is usually owned by axiomatic statements.</li>
 * <li>{@link OWL2#Annotation owl:Annotation} for sub-annotations,
 * and also for annotation of several specific axioms with main-statement {@code _:x rdf:type @type} where {@code @type} is
 * {@code owl:AllDisjointClasses}, {@code owl:AllDisjointProperties}, {@code owl:AllDifferent} or {@code owl:NegativePropertyAssertion}.</li>
 * </ul>
 * Example:
 * <pre>{@code
 * [ a                      owl:Axiom ;
 *   rdfs:comment           "some comment 1", "some comment 2"@fr ;
 *   owl:annotatedProperty  rdf:type ;
 *   owl:annotatedSource    <Class> ;
 *   owl:annotatedTarget    owl:Class
 * ] .
 * }</pre>
 * <p>
 *
 * @see OntStatement
 * @see <a href="https://www.w3.org/TR/owl2-mapping-to-rdf/#Translation_of_Annotations">2.2 Translation of Annotations</a>
 */
public interface OntAnnotation extends OntObject {

    /**
     * Returns the base statement, i.e. statement to which this bulk-annotation is attached.
     * In the example above it is the statement {@code <Class> rdf:type owl:Class}.
     * Notes:
     * Starting v.{@code 2.0.0} the presence of this statement in the Graph does not require anymore.
     * The result could be {@code null} in the special case of anonymous resource,
     * e.g. when {@code rdf:type} is {@code owl:AllDisjointClasses}, see class description.
     *
     * @return {@link OntStatement}, possibly {@code null}
     */
    OntStatement getBase();

    /**
     * Returns the annotations assertions attached to this annotation resource.
     * The annotation assertion is a statements with an {@link OntAnnotationProperty annotation property} as predicate.
     * The example above contains two such statements:
     * {@code _:x rdfs:comment "some comment 1"} and {@code _:x rdfs:comment "some comment 2"@fr}.
     *
     * @return {@code Stream} of annotation statements {@link OntStatement}s
     * @see OntObject#annotations()
     */
    Stream<OntStatement> assertions();

    /**
     * Lists all descendants of this ont-annotation resource.
     * The resulting resources must have {@link OWL2#Annotation owl:Annotation} type
     * and this object on predicate {@link OWL2#annotatedSource owl:annotatedSource}.
     * The method {@link #parent()} called on descendants must return the annotation equals to this.
     *
     * @return {@code Stream} of {@link OntAnnotation}s
     */
    Stream<OntAnnotation> descendants();

    /**
     * Just a synonym for {@link #assertions()}.
     *
     * @return {@code Stream} of annotation statements {@link OntStatement}s
     */
    @Override
    Stream<OntStatement> annotations();

    /**
     * Adds a new annotation assertion to this annotation resource.
     * If this {@link OntAnnotation} contains annotation property assertion {@code this x y}
     * and it does not have sub-annotations yet,
     * the given annotation property {@code p} and value {@code v} will produce following {@link OntAnnotation} object:
     * <pre>{@code
     * _:x rdf:type              owl:Annotation .
     * _:x p                     v .
     * _:x owl:annotatedSource   this .
     * _:x owl:annotatedProperty x .
     * _:x owl:annotatedTarget   y .
     * }</pre>
     * and this method will return {@code _:x p v} triple wrapped as {@link OntStatement}
     * to allow adding subsequent sub-annotations.
     * If this annotation object already has a sub-annotation for the statement {@code this x y},
     * the new triple will be added to the existing anonymous resource.
     *
     * @param property {@link OntAnnotationProperty}
     * @param value    {@link RDFNode}
     * @return {@link OntStatement} - an annotation assertion belonging to this object
     * @see OntStatement#addAnnotation(OntAnnotationProperty, RDFNode)
     * @see OntObject#addAnnotation(OntAnnotationProperty, RDFNode)
     */
    @Override
    OntStatement addAnnotation(OntAnnotationProperty property, RDFNode value);

    /**
     * Answers a parent {@code OntAnnotation}.
     * For non-empty result the {@code rdf:type} must be {@code owl:Annotation}.
     * For a root annotation an empty result is expected.
     *
     * @return {@code Optional} around of {@link OntAnnotation}
     */
    default Optional<OntAnnotation> parent() {
        return Optional.ofNullable(getBase()).map(x -> x.getSubject().getAs(OntAnnotation.class));
    }
}

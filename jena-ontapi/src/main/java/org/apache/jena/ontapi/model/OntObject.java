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

import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A base {@link OntResource Ontology Object RDF Resource}.
 * A common super-type for all the abstractions in the {@link OntModel Ontology RDF Model},
 * which support Jena Polymorphism,
 * can be annotated and have a structure strictly defined according to the OWL2 specification.
 */
public interface OntObject extends OntResource {

    /**
     * Gets a public ont-object type identifier.
     *
     * @return Class, the actual type of this object
     */
    Class<? extends OntObject> objectType();

    /**
     * Returns the main {@link OntStatement}
     * which determines the nature of this ontological resource.
     * In most cases it is a declaration and wraps a triple with predicate {@code rdf:type}.
     * <p>
     * The returned {@link OntStatement} differs from that
     * which could be obtained directly from the model using one of model's {@code statement(..)} methods:
     * the main statement annotations are added
     * in the form of annotation property assertions (so-called 'plain annotations'),
     * not as typed anonymous resources (so-called 'bulk annotations', {@link OntAnnotation}).
     * In ONT-API it is legal for a main statement to have both plain and bulk annotations.
     * Note: for anonymous ontology objects (i.e., not for OWL Entities) this behavior may not fully meet
     * OWL2 specification: the specification describes only bulk annotations
     * for all anonymous OWL2 components with except of an individual.
     * To get a common ontology statement that supports bulk annotations only,
     * the expression {@code getModel().asStatement(this.getMainStatement().asTriple())} can be used.
     *
     * @return {@link OntStatement} or {@code null} in some boundary cases (e.g., for builtins)
     * @see OntModel#asStatement(Triple)
     * @see OntStatement#addAnnotation(OntAnnotationProperty, RDFNode)
     */
    @Override
    OntStatement getMainStatement();

    /**
     * Lists all characteristic statements of the ontology resource, i.e.,
     * all those statements which completely determine this object nature according to the OWL2 specification.
     * For non-composite objects the result might contain only the {@link #getMainStatement() root statement}.
     * For composite objects (usually anonymous resources: disjoint sections, class expression, etc.)
     * the result would contain all statements in the graph directly related to the object
     * but without statements that relate to the object components.
     * The return stream is ordered and, in most cases,
     * the expression {@code this.spec().findFirst().get()} returns the same statement as {@code this.getRoot()}.
     * Object annotations are not included in the resultant stream.
     * <p>
     * For OWL Entities the returned stream will contain only a single main statement
     * (i.e. {@link #getMainStatement()}),
     * or even will be empty for built-in entities and individuals.
     *
     * @return {@code Stream} of {@link OntStatement Ontology Statement}s
     * @see #content()
     */
    @Override
    Stream<OntStatement> spec();

    /**
     * Lists the content of the object, i.e., all characteristic statements (see {@link #spec()}),
     * plus all additional statements in which this object is the subject,
     * minus those of them whose predicate is an annotation property (annotations are not included).
     *
     * @return {@code Stream} of {@link OntStatement Ontology Statement}s
     * @see #spec()
     */
    Stream<OntStatement> content();

    /**
     * Adds an ont-statement by attaching predicate and object (value) to this resource.
     *
     * @param property {@link Property} predicate, not {@code null}
     * @param value    {@link RDFNode} object, not {@code null}
     * @return {@link OntStatement}
     * @see Resource#addProperty(Property, RDFNode)
     */
    OntStatement addStatement(Property property, RDFNode value);

    /**
     * Deletes the specific property-value pair from this object.
     * All the corresponding statement's annotations are also deleted.
     * In case the given {@code object} is {@code null},
     * all statements with the {@code property}-predicate will be deleted.
     * No-op if no match found.
     *
     * @param property {@link Property} predicate, not {@code null}
     * @param object   {@link RDFNode} object, <b>can be {@code null}</b>
     * @return this object to allow cascading calls
     * @see #addStatement(Property, RDFNode)
     * @see OntStatement#clearAnnotations()
     */
    OntObject remove(Property property, RDFNode object);

    /**
     * Lists ont-statements by the predicate.
     *
     * @param property {@link Property}, predicate, can be {@code null}
     * @return {@code Stream} of {@link OntStatement}s
     */
    Stream<OntStatement> statements(Property property);

    /**
     * Lists all top-level statements related to this object (i.e., with subject={@code this}).
     *
     * @return {@code Stream} of all statements
     * @see #listProperties()
     */
    Stream<OntStatement> statements();

    /**
     * Returns the <b>first</b> statement for the specified property and object.
     * What exactly is the first triple is defined at the level of graph; in general, it is unpredictable.
     * Also note, the common jena implementation of in-memory graph does not allow duplicated triples,
     * and hence there can be at most one operator for a given {@code property} and {@code value}.
     *
     * @param property {@link Property}, the predicate
     * @param value    {@link RDFNode}, the object
     * @return {@link Optional} around {@link OntStatement}
     */
    Optional<OntStatement> statement(Property property, RDFNode value);

    /**
     * Returns the <b>first</b> statement for the specified property.
     * What is the first triple is defined at the level of graph; in general, it is unpredictable.
     *
     * @param property {@link Property}, can be {@code null}
     * @return {@link Optional} around {@link OntStatement}
     * @see Resource#getProperty(Property)
     */
    Optional<OntStatement> statement(Property property);

    /**
     * Lists all objects attached on the property to this object with the given type.
     *
     * @param predicate {@link Property} predicate, can be {@code null} for wildcard searching
     * @param type      Interface to find and cast
     * @param <O>       a class-type of rdf-node
     * @return {@code Stream} of {@link RDFNode RDF Node}s of the type {@code O}
     */
    <O extends RDFNode> Stream<O> objects(Property predicate, Class<O> type);

    /**
     * Lists all objects attached on the property to this object.
     *
     * @param predicate {@link Property} predicate, can be {@code null} for wildcard searching
     * @return {@code Stream} of {@link RDFNode RDF Node}s
     */
    Stream<RDFNode> objects(Property predicate);

    /**
     * Adds an annotation assertion with the given {@link OntAnnotationProperty annotation property} as predicate
     * and {@link RDFNode RDF Node} as value.
     * The method is equivalent to the expression {@code getRoot().addAnnotation(property, value)}.
     *
     * @param property {@link OntAnnotationProperty} - named annotation property
     * @param value    {@link RDFNode} - the value: uri-resource, literal or anonymous individual
     * @return {@link OntStatement} for newly added annotation
     * to provide the possibility of adding subsequent sub-annotations
     * @throws OntJenaException in case input is wrong
     * @see OntStatement#addAnnotation(OntAnnotationProperty, RDFNode)
     */
    OntStatement addAnnotation(OntAnnotationProperty property, RDFNode value);

    /**
     * Lists all top-level annotations attached to the root statement of this object.
     * Each annotation can be plain (annotation property assertion) or bulk
     * (anonymous resource with the type {@code owl:Axiom} or {@code owl:Annotation}, possibly with sub-annotations).
     * Sub-annotations are not included in the returned stream.
     * For non-built-in ontology objects this is equivalent to the expression {@code getRoot().annotations()}.
     *
     * @return {@code Stream} of {@link OntStatement}s that have an {@link OntAnnotationProperty annotation property} as predicate
     * @see OntStatement#annotations()
     * @see OntAnnotation#assertions()
     */
    Stream<OntStatement> annotations();

    /**
     * Lists all annotation literals for the given predicate and the language tag.
     * Literal tag comparison is case-insensitive.
     * Partial search is also allowed, for example,
     * a literal with the tag {@code en-GB} will list also if the input language tag is {@code en}.
     * An empty string as language tag means searching for plain no-language literals.
     *
     * @param predicate {@link OntAnnotationProperty}, not {@code null}
     * @param lang      String, the language tag to restrict the listed literals to,
     *                  or {@code null} to select all literals
     * @return {@code Stream} of String's, i.e., literal lexical forms
     * @see #annotationValues(OntAnnotationProperty)
     */
    Stream<String> annotationValues(OntAnnotationProperty predicate, String lang);

    /**
     * Removes all root annotations including their sub-annotations hierarchy.
     * Any non-root annotations are untouched.
     * For example, in case of deleting an OWL class,
     * if it is present on the left side of the {@code rdfs:subClassOf} statement,
     * all the annotations of that statement will remain in the graph,
     * but all main annotations (which belongs to the statement with the predicate {@code rdf:type})
     * will be deleted from the graph.
     * For non-built-in ontology objects this is equivalent to the expression {@code getRoot().clearAnnotations()}.
     *
     * @return this object to allow cascading calls
     * @see OntStatement#clearAnnotations()
     */
    OntObject clearAnnotations();

    /**
     * Returns the <b>first</b> statement for the specified property.
     * What exactly is the first triple is defined at the level of graph and, in general, it is unpredictable.
     *
     * @param property {@link Property}, the predicate
     * @return {@link OntStatement}
     * @throws org.apache.jena.shared.PropertyNotFoundException if no such statement found
     * @see Resource#getRequiredProperty(Property)
     */
    @Override
    OntStatement getRequiredProperty(Property property);

    /**
     * Answers {@code true} iff this object has the declaration triple {@code this rdf:type type},
     * where {@code type} is what specified as parameter.
     *
     * @param type {@link Resource} to test
     * @return {@code true} if the given type is present
     */
    default boolean hasType(Resource type) {
        try (Stream<Resource> types = types()) {
            return types.anyMatch(type::equals);
        }
    }

    /**
     * Lists all declarations (statements with {@code rdf:type} predicate).
     *
     * @return {@code Stream} of {@link Resource}s
     */
    default Stream<Resource> types() {
        return objects(RDF.type, Resource.class);
    }

    /**
     * Lists all annotation values for the given predicate.
     *
     * @param predicate {@link OntAnnotationProperty}, not {@code null}
     * @return {@code Stream} of {@link RDFNode}s
     * @see #annotations()
     */
    default Stream<RDFNode> annotationValues(OntAnnotationProperty predicate) {
        return annotations()
                .filter(s -> Objects.equals(predicate, s.getPredicate()))
                .map(Statement::getObject);
    }

    /**
     * Adds no-lang annotation assertion.
     *
     * @param predicate   {@link OntAnnotationProperty} predicate
     * @param lexicalForm String, the literal lexical form, not {@code null}
     * @return {@link OntStatement}
     */
    default OntStatement addAnnotation(OntAnnotationProperty predicate, String lexicalForm) {
        return addAnnotation(predicate, lexicalForm, null);
    }

    /**
     * Adds lang annotation assertion.
     *
     * @param predicate {@link OntAnnotationProperty} predicate
     * @param txt       String, the literal lexical form, not {@code null}
     * @param lang      String, the language tag, nullable
     * @return {@link OntStatement} - new statement: {@code @subject @predicate "txt"@lang}
     */
    default OntStatement addAnnotation(OntAnnotationProperty predicate, String txt, String lang) {
        return addAnnotation(predicate, getModel().createLiteral(txt, lang));
    }

    /**
     * Annotates the object with the given {@code predicate} and {@code value}.
     *
     * @param predicate {@link OntAnnotationProperty} - named annotation property, not {@code null}
     * @param value     {@link RDFNode} - the value: uri-resource, literal or anonymous individual, not {@code null}
     * @return this object to allow cascading calls
     * @see OntObject#addAnnotation(OntAnnotationProperty, RDFNode)
     */
    default OntObject annotate(OntAnnotationProperty predicate, RDFNode value) {
        addAnnotation(predicate, value);
        return this;
    }

    /**
     * Adds a language-tagged text for this object and the given {@code predicate}
     *
     * @param predicate {@link OntAnnotationProperty} - named annotation property, not {@code null}
     * @param txt       String, the literal lexical form, cannot be {@code null}
     * @param lang      String, the language tag, can be {@code null}
     * @return this object to allow cascading calls
     */
    default OntObject annotate(OntAnnotationProperty predicate, String txt, String lang) {
        return annotate(predicate, getModel().createLiteral(txt, lang));
    }

    /**
     * Creates {@code _:this rdfs:comment "txt"^^xsd:string} statement.
     *
     * @param txt String, not {@code null}
     * @return this object to allow cascading calls
     * @see OntModel#getRDFSComment()
     */
    default OntObject addComment(String txt) {
        return addComment(txt, null);
    }

    /**
     * Adds the given localized text annotation with builtin {@code rdfs:comment} predicate.
     *
     * @param txt  String, the literal lexical form, not {@code null}
     * @param lang String, the language tag, nullable
     * @return this object to allow cascading calls
     * @see OntModel#getRDFSComment()
     */
    default OntObject addComment(String txt, String lang) {
        addAnnotation(getModel().getRDFSComment(), txt, lang);
        return this;
    }

    /**
     * Creates {@code _:this rdfs:label "txt"^^xsd:string} statement.
     *
     * @param txt String, the literal lexical form, not {@code null}
     * @return this object to allow cascading calls
     * @see OntModel#getRDFSLabel()
     */
    default OntObject addLabel(String txt) {
        return addLabel(txt, null);
    }

    /**
     * Adds the given localized text annotation with builtin {@code rdfs:label} predicate.
     *
     * @param txt  String, the literal lexical form, not {@code null}
     * @param lang String, the language tag, nullable
     * @return this object to allow cascading calls
     * @see OntModel#getRDFSLabel()
     */
    default OntObject addLabel(String txt, String lang) {
        addAnnotation(getModel().getRDFSLabel(), txt, lang);
        return this;
    }

    /**
     * Answers the comment string for this object.
     * If there is more than one such resource, an arbitrary selection is made.
     *
     * @return a {@code rdfs:comment} string or {@code null} if there is no comments
     * @see OntModel#getRDFSComment()
     */
    default String getComment() {
        return getComment(null);
    }

    /**
     * Answers the comment string for this object.
     * If there is more than one such resource, an arbitrary selection is made.
     *
     * @param lang String, the language attribute for the desired comment (EN, FR, etc.) or {@code null} for don't care;
     *             will attempt to retrieve the most specific comment matching the given language;
     *             to get no-lang literal string an empty string can be used
     * @return a {@code rdfs:comment} string matching the given language,
     * or {@code null} if there is no matching comment
     * @see OntModel#getRDFSComment()
     */
    default String getComment(String lang) {
        try (Stream<String> res = annotationValues(getModel().getRDFSComment(), lang)) {
            return res.findFirst().orElse(null);
        }
    }

    /**
     * Answers the label string for this object.
     * If there is more than one such resource, an arbitrary selection is made.
     *
     * @return a {@code rdfs:label} string or {@code null} if there is no comments
     * @see OntModel#getRDFSLabel()
     */
    default String getLabel() {
        return getLabel(null);
    }

    /**
     * Answers the label string for this object.
     * If there is more than one such resource, an arbitrary selection is made.
     *
     * @param lang String, the language attribute for the desired comment (EN, FR, etc.) or {@code null} for don't care;
     *             will attempt to retrieve the most specific comment matching the given language;
     *             to get no-lang literal string an empty string can be used
     * @return a {@code rdfs:label} string matching the given language, or {@code null}  if there is no matching label
     * @see OntModel#getRDFSLabel()
     */
    default String getLabel(String lang) {
        try (Stream<String> res = annotationValues(getModel().getRDFSLabel(), lang)) {
            return res.findFirst().orElse(null);
        }
    }
}

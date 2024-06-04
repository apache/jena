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
import org.apache.jena.ontapi.UnionGraph;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.OWL2;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Interface encapsulating an Ontology Identifier.
 * Each {@link OntModel OWL2 Obtology} must have one and only one {@link OntID Ontology ID} inside.
 * <p>
 * Please note: the methods of this interface do not affect the hierarchical structure of the graph
 * to which this resource is attached, they only affect the structure of the graph itself.
 * In other words, calling the methods {@link #removeImport(String)} does not remove the sub-graph
 * from the main {@link UnionGraph Union Graph}.
 * Similar, calling the method {@link #addImport(String)} simply adds the corresponding triple to the base graph.
 *
 * @see <a href="https://www.w3.org/TR/owl-syntax/#Ontology_IRI_and_Version_IRI">3.1 Ontology IRI and Version IRI</a>
 */
public interface OntID extends OntObject {

    /**
     * Returns an IRI from the right side of {@code this owl:versionIRI IRI} statement if it is uniquely determined.
     *
     * @return String IRI or {@code null}
     */
    String getVersionIRI();

    /**
     * Assigns a new version IRI to this Ontology ID object.
     * A {@code null} argument means that current version IRI should be deleted.
     *
     * @param uri String, can be {@code null} to remove versionIRI
     * @return this ID-object to allow cascading calls
     * @throws OntJenaException if input is wrong
     */
    OntID setVersionIRI(String uri) throws OntJenaException;

    /**
     * Adds the triple {@code this owl:import uri} to this resource.
     *
     * @param uri String, not {@code null}
     * @return this ID-object to allow cascading calls
     * @throws OntJenaException if input is wrong
     */
    OntID addImport(String uri) throws OntJenaException;

    /**
     * Removes the triple {@code this owl:import uri} from this resource.
     *
     * @param uri String, not {@code null}
     * @return this ID-object to allow cascading calls
     */
    OntID removeImport(String uri);

    /**
     * Lists all {@code owl:import}s.
     *
     * @return {@code Stream} of Strings (IRIs)
     */
    Stream<String> imports();

    /**
     * Indicates whether the given {@link OntID Ontology ID} is equal to this one in OWL2 terms.
     * This means that the IDs must have the same IRI + version IRI pairs.
     * If the method returns {@code true}, then two ontologies can not be coexisting in the same scope.
     *
     * @param other {@link OntID}
     * @return {@code true} in case the IDs are the same, otherwise {@code false}
     */
    default boolean sameAs(OntID other) {
        return equals(other) && Objects.equals(getVersionIRI(), other.getVersionIRI());
    }

    /**
     * Returns an IRI that can be used to create {@link OWL2#imports owl:imports}
     * statement in another model to make a reference between a model to which this id belongs and another model.
     * According to the specification, a version IRI is primary.
     *
     * @return String or {@code null}
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Ontology_Documents">3.2 Ontology Documents</a>
     */
    default String getImportsIRI() {
        String res = getVersionIRI();
        if (res != null) return res;
        return getURI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntID addComment(String txt) {
        return addComment(txt, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntID addComment(String txt, String lang) {
        return annotate(getModel().getRDFSComment(), txt, lang);
    }

    /**
     * Adds a {@link OWL2#versionInfo owl:versionInfo} description.
     *
     * @param txt String, the literal lexical form, not {@code null}
     * @return this ID-object to allow cascading calls
     */
    default OntID addVersionInfo(String txt) {
        return addVersionInfo(txt, null);
    }

    /**
     * Annotates this object with {@link OWL2#versionInfo owl:versionInfo} predicate
     * and the specified language-tagged literal.
     *
     * @param txt  String, the literal lexical form, not {@code null}
     * @param lang String, the language tag, nullable
     * @return this ID-object to allow cascading calls
     */
    default OntID addVersionInfo(String txt, String lang) {
        return annotate(getModel().getAnnotationProperty(OWL2.versionInfo), txt, lang);
    }

    /**
     * Answers the version info string for this ontology id.
     * If there is more than one such resource, an arbitrary selection is made.
     *
     * @return a {@code owl:versionInfo} string or {@code null} if nothing is found
     */
    default String getVersionInfo() {
        return getVersionInfo(null);
    }

    /**
     * Answers the version info string for this ontology id.
     * If there is more than one such resource, an arbitrary selection is made.
     *
     * @param lang String, the language attribute for the desired comment (EN, FR, etc.) or {@code null} for don't care;
     *             will attempt to retrieve the most specific comment matching the given language;
     *             to get no-lang literal string an empty string can be used
     * @return a {@code owl:versionInfo} string matching the given language,
     * or {@code null} if there is no version info
     */
    default String getVersionInfo(String lang) {
        try (Stream<String> res = annotationValues(getModel().getAnnotationProperty(OWL2.versionInfo), lang)) {
            return res.findFirst().orElse(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntID annotate(OntAnnotationProperty predicate, String txt, String lang) {
        return annotate(predicate, getModel().createLiteral(txt, lang));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntID annotate(OntAnnotationProperty predicate, RDFNode value) {
        addAnnotation(predicate, value);
        return this;
    }
}

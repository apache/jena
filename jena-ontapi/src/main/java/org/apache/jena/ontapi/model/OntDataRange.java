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

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A base abstraction for Ontology Data Range Expressions (both named and anonymous).
 *
 * @see Named - a named data range (i.e. OWL Datatype)
 * @see <a href="https://www.w3.org/TR/owl2-quick-reference/#Data_Ranges">2.4 Data Ranges</a>
 * @see <a href="https://www.w3.org/TR/owl2-syntax/#Data_Ranges">7 Data Ranges</a>
 */
public interface OntDataRange extends OntObject, AsNamed<OntDataRange.Named> {

    @Override
    default Named asNamed() {
        return as(Named.class);
    }

    /**
     * Returns a data range arity.
     * OWL2 spec says:
     * <pre>{@code This specification currently does not define data ranges of arity more than one}.</pre>
     * So n-ary data ranges are not supported and, therefore, the method always returns {@code 1}.
     *
     * @return int, positive number
     */
    default int arity() {
        return 1;
    }

    /**
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Complement_of_Data_Ranges">7.3 Complement of Data Ranges</a>
     * @see OntModel#createDataComplementOf(OntDataRange)
     */
    interface ComplementOf extends OntDataRange, SetValue<OntDataRange, ComplementOf>, HasValue<OntDataRange> {
    }

    /**
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Intersection_of_Data_Ranges">7.1 Intersection of Data Ranges</a>
     * @see OntModel#createDataIntersectionOf(Collection)
     */
    interface IntersectionOf extends Combination<OntDataRange>, SetComponents<OntDataRange, IntersectionOf> {
    }

    /**
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Union_of_Data_Ranges">7.2 Union of Data Ranges</a>
     * @see OntModel#createDataUnionOf(Collection)
     */
    interface UnionOf extends Combination<OntDataRange>, SetComponents<OntDataRange, UnionOf> {
    }

    /**
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Enumeration_of_Literals">7.4 Enumeration of Literals</a>
     * @see OntModel#createDataOneOf(Collection)
     */
    interface OneOf extends Combination<Literal>, SetComponents<Literal, OneOf> {
    }

    /**
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Datatype_Restrictions">7.5 Datatype Restrictions</a>
     * @see OntFacetRestriction
     * @see OntModel#createFacetRestriction(Class, Literal)
     * @see OntModel#createDataRestriction(Named, Collection)
     */
    interface Restriction extends Combination<OntFacetRestriction>,
            SetComponents<OntFacetRestriction, Restriction>, SetValue<Named, Restriction>, HasValue<Named> {
        /**
         * {@inheritDoc}
         * The result stream for {@link Restriction Restriction Data Range} also includes
         * {@link OntFacetRestriction facet restrinction} definition triples.
         *
         * @return {@code Stream} of {@link OntStatement}s.
         */
        @Override
        Stream<OntStatement> spec();

        /**
         * Adds a facet restriction to the end of the []-list.
         *
         * @param type    subclass of {@link OntFacetRestriction}, not {@code null}
         * @param literal value, not {@code null}
         * @return <b>this</b> instance to allow cascading calls
         */
        default Restriction addFacet(Class<? extends OntFacetRestriction> type, Literal literal) {
            getList().add(getModel().createFacetRestriction(type, literal));
            return this;
        }
    }

    /**
     * An abstract datarange that unites DataRanges consisting of multiple components.
     *
     * @param <N> {@link RDFNode}
     */
    interface Combination<N extends RDFNode> extends OntDataRange, HasRDFNodeList<N> {
    }

    /**
     * Interface encapsulating an Ontology Datatype, {@link OntEntity OWL Entity},
     * a named {@link OntDataRange data range} expression.
     *
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Datatypes">5.2 Datatypes</a>
     */
    interface Named extends OntEntity, OntDataRange {

        @Override
        default Named asNamed() {
            return this;
        }

        /**
         * Lists all equivalent data ranges.
         * The pattern to search is {@code DN owl:equivalentClass D}, where {@code DN} is this {@link Named Data Type},
         * and {@code D} is a search object, the {@link OntDataRange data-range expression}.
         *
         * @return {@code Stream} of {@link OntDataRange}s
         * @see OntClass#equivalentClasses()
         */
        default Stream<OntDataRange> equivalentClasses() {
            return objects(OWL2.equivalentClass, OntDataRange.class);
        }

        /**
         * Creates an equivalent class statement with the given {@link OntDataRange Data Range expression}.
         *
         * @param other {@link OntDataRange}, not {@code null}
         * @return {@link OntStatement} to allow the subsequent annotations addition
         * @see #addEquivalentClass(OntDataRange)
         * @see #removeEquivalentClass(Resource)
         * @see OntClass#addEquivalentClassStatement(OntClass)
         */
        default OntStatement addEquivalentClassStatement(OntDataRange other) {
            return addStatement(OWL2.equivalentClass, other);
        }

        /**
         * Creates an equivalent class statement with the given {@link OntDataRange Data Range expression}.
         *
         * @param other {@link OntDataRange}, not {@code null}
         * @return <b>this</b> instance to allow cascading calls
         * @see #addEquivalentClassStatement(OntDataRange)
         * @see #removeEquivalentClass(Resource)
         * @see OntClass#addEquivalentClass(OntClass)
         */
        default Named addEquivalentClass(OntDataRange other) {
            addEquivalentClassStatement(other);
            return this;
        }

        /**
         * Removes the given equivalent data range,
         * that is attached to this data-type on predicate {@link OWL2#equivalentClass owl:equivalenrClass},
         * including all the statement's related annotations.
         * No-op in case nothing is found.
         * The {@code null} input means removing all {@link OWL2#equivalentClass owl:equivalentClass} statements
         * with all their annotations.
         *
         * @param other {@link Resource}, or {@code null} to remove all equivalent data ranges
         * @return <b>this</b> instance to allow cascading calls
         * @see #addEquivalentClass(OntDataRange)
         * @see #addEquivalentClassStatement(OntDataRange)
         * @see OntClass#removeEquivalentClass(Resource)
         */
        default Named removeEquivalentClass(Resource other) {
            remove(OWL2.equivalentClass, other);
            return this;
        }

        /**
         * Creates a Jena Datatype.
         *
         * @return {@link RDFDatatype Jena RDF Datatype}
         */
        default RDFDatatype toRDFDatatype() {
            return TypeMapper.getInstance().getSafeTypeByName(getURI());
        }

        /**
         * Builds a typed literal from its value form given as an object.
         * Note: there is no validation for lexical form.
         *
         * @param obj anything, not {@code null}
         * @return {@link Literal}
         */
        default Literal createLiteral(Object obj) {
            return createLiteral(String.valueOf(Objects.requireNonNull(obj)));
        }

        /**
         * Builds a typed literal from its value form.
         * Note: there is no validation for lexical form,
         * so it is possible to create an illegal literal, e.g. {@code "wrong"^^xsd:int}.
         *
         * @param lex String, lexical form of the result literal, not {@code null}
         * @return {@link Literal}
         * @see org.apache.jena.rdf.model.Model#createTypedLiteral(String, RDFDatatype)
         */
        default Literal createLiteral(String lex) {
            return getModel().createTypedLiteral(Objects.requireNonNull(lex), toRDFDatatype());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addComment(String txt) {
            return addComment(txt, null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addComment(String txt, String lang) {
            return annotate(getModel().getRDFSComment(), txt, lang);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addLabel(String txt) {
            return addLabel(txt, null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addLabel(String txt, String lang) {
            return annotate(getModel().getRDFSLabel(), txt, lang);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named annotate(OntAnnotationProperty predicate, String txt, String lang) {
            return annotate(predicate, getModel().createLiteral(txt, lang));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named annotate(OntAnnotationProperty predicate, RDFNode value) {
            addAnnotation(predicate, value);
            return this;
        }
    }
}
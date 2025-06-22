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

import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;

import java.util.Set;

/**
 * The base interface for OWL entities, which are always URI-{@link org.apache.jena.rdf.model.Resource}.
 * In OWL2 there are <b>6</b> types of entities, see below.
 *
 * @see OntClass.Named
 * @see OntDataRange.Named
 * @see OntIndividual.Named
 * @see OntAnnotationProperty
 * @see OntObjectProperty.Named
 * @see OntDataProperty
 */
public interface OntEntity extends OntObject {

    Set<Class<? extends OntEntity>> TYPES = Set.of(
            OntClass.Named.class,
            OntDataRange.Named.class,
            OntIndividual.Named.class,
            OntObjectProperty.Named.class,
            OntAnnotationProperty.class,
            OntDataProperty.class
    );

    /**
     * Lists all OWL entity types.
     *
     * @return an {@link ExtendedIterator} of OWL entity {@code Class}-types
     */
    static ExtendedIterator<Class<? extends OntEntity>> listEntityTypes() {
        return Iterators.of(
                OntClass.Named.class,
                OntDataRange.Named.class,
                OntIndividual.Named.class,
                OntObjectProperty.Named.class,
                OntAnnotationProperty.class,
                OntDataProperty.class
        );
    }

    /**
     * Determines if this is a builtin entity.
     * In a standard (default) OWL2 vocabulary an entity is builtin if it is:
     * <ul>
     * <li>a {@link OntClass.Named class} and its IRI is either {@code owl:Thing} or {@code owl:Nothing}</li>
     * <li>an {@link OntObjectProperty.Named object property} and its IRI is either {@code owl:topObjectProperty}
     * or {@code owl:bottomObjectProperty}</li>
     * <li>a {@link OntDataProperty data property} and its IRI is either {@code owl:topDataProperty}
     * or {@code owl:bottomDataProperty}</li>
     * <li>a {@link OntDataRange.Named datatype} and its IRI is either {@code rdfs:Literal},
     * or {@code rdf:PlainLiteral},
     * or it is from the OWL 2 datatype map</li>
     * <li>an {@link OntAnnotationProperty annotation property} and its IRI is one of the following:
     * <ul>
     * <li>{@code rdfs:label}</li>
     * <li>{@code rdfs:comment}</li>
     * <li>{@code rdfs:seeAlso}</li>
     * <li>{@code rdfs:isDefinedBy}</li>
     * <li>{@code owl:deprecated}</li>
     * <li>{@code owl:versionInfo}</li>
     * <li>{@code owl:priorVersion}</li>
     * <li>{@code owl:backwardCompatibleWith}</li>
     * <li>{@code owl:incompatibleWith}</li>
     * </ul>
     * </li>
     * </ul>
     * Note: all the listed above IRIs refer
     * to the default {@link OntPersonality.Builtins Builtins Vocabulary}.
     * A model with different {@code Builtins} vocabulary will naturally have a different {@code Set} of builtin IRIs,
     * and this method will return a different result.
     *
     * @return {@code true} if it is a built-in entity
     * @see OWL2
     * @see OntPersonality.Builtins
     */
    boolean isBuiltIn();

}

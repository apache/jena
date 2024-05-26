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

/**
 * A technical generic interface to provide a possibility to assign {@link RDFNode} value (so-called filler)
 * into a class expression.
 * A value can be either {@link OntClass}, {@link OntDataRange}, {@link OntIndividual}
 * or {@link org.apache.jena.rdf.model.Literal}, depending on a concrete {@link OntClass} or {@link OntDataRange} type.
 * This interface is used to construct {@link OntClass class expression}s and {@link OntDataRange data range}s as a base.
 *
 * @param <V> - any subtype of {@link RDFNode} ({@link OntClass}, {@link OntDataRange}, {@link OntIndividual}
 *            or {@link org.apache.jena.rdf.model.Literal}).
 * @param <R> - return type, a subtype of {@link OntClass} or {@link OntDataRange}
 * @see HasValue
 */
interface SetValue<V extends RDFNode, R extends OntObject> {
    /**
     * Sets the specified value (a filler in OWL-API terms)
     * into this {@link OntClass class} or {@link OntDataRange data range} expression.
     * <p>
     * A {@code value} can be {@code null} if this is a Cardinality Restriction
     * (the null-filler is considered as {@link OWL2#Thing owl:Thing}
     * for an object restriction and as {@link org.apache.jena.vocabulary.RDFS#Literal} for a data restriction).
     *
     * @param value {@code V}, possible {@code null} in case of Cardinality Restriction
     * @return <b>this</b> instance to allow cascading calls
     * @see HasValue#getValue()
     */
    R setValue(V value);
}

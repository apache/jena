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
 * A technical generic interface to provide {@link RDFNode} value,
 * which can be either {@link OntClass}, {@link OntDataRange}, {@link OntIndividual} or {@link org.apache.jena.rdf.model.Literal}.
 * This interface is used to construct {@link OntClass class expression}s and {@link OntDataRange data range}s as a base.
 *
 * @param <V> a subtype of {@link RDFNode}: {@link OntClass}, {@link OntDataRange}, {@link OntIndividual}
 *            or {@link org.apache.jena.rdf.model.Literal}
 * @see SetValue
 */
interface HasValue<V extends RDFNode> {

    /**
     * Gets an RDF-value (a filler in OWL-API terms) encapsulated by this expression
     * (that can be either {@link OntClass class} or {@link OntDataRange data range} expression).
     * <p>
     * The result is not {@code null} even if it is an Unqualified Cardinality Restriction,
     * that has no explicit filler in RDF
     * (the filler is expected to be either {@link OWL2#Thing owl:Thing}
     * for object restriction or {@link org.apache.jena.vocabulary.RDFS#Literal} for data restriction).
     *
     * @return {@code V}, not {@code null}
     * @see SetValue#setValue(RDFNode)
     */
    V getValue();
}

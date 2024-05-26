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
import org.apache.jena.rdf.model.RDFNode;

/**
 * Interface representing the Negative Property Assertion abstraction,
 * where predicate (property) is expected to be either ontology {@link OntDataProperty data property} ({@code R}) or
 * {@link OntObjectProperty object property exception} ({@code P}).
 * Assuming {@code _:x} is a blank node, {@code ai} is an individual and {@code v} is literal,
 * a Negative Object Property Assertion in Turtle syntax looks like this:
 * <pre>{@code
 * _:x rdf:type owl:NegativePropertyAssertion ;
 * _:x owl:sourceIndividual a1 ;
 * _:x owl:assertionProperty P ;
 * _:x owl:targetIndividual a2 .
 * }</pre>
 * In turn, a Negative Data Property Assertion looks like the following:
 * <pre>{@code
 * _:x rdf:type owl:NegativePropertyAssertion ;
 * _:x owl:sourceIndividual a ;
 * _:x owl:assertionProperty R ;
 * _:x owl:targetValue v .
 * }</pre>
 *
 * @param <P> - either {@link OntObjectProperty object property expression} or {@link OntDataProperty data property}
 * @param <V> - either {@link OntIndividual} or {@link Literal}
 */
public interface OntNegativeAssertion<P extends OntRelationalProperty, V extends RDFNode> extends OntObject {

    /**
     * Returns the source individual.
     *
     * @return {@link OntIndividual}
     */
    OntIndividual getSource();

    /**
     * Returns the assertion property.
     *
     * @return either {@link OntObjectProperty} or {@link OntDataProperty}
     */
    P getProperty();

    /**
     * Returns the target node.
     *
     * @return either {@link OntIndividual} or {@link Literal}
     */
    V getTarget();

    /**
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Negative_Object_Property_Assertions">9.6.5 Negative Object Property Assertions</a>
     */
    interface WithObjectProperty extends OntNegativeAssertion<OntObjectProperty, OntIndividual> {
    }

    /**
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Negative_Data_Property_Assertions">9.6.7 Negative Data Property Assertions</a>
     */
    interface WithDataProperty extends OntNegativeAssertion<OntDataProperty, Literal> {
    }
}

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

import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.XSD;

/**
 * A technical interface to provide a non-negative integer value, that is a restriction cardinality.
 *
 * @see SetCardinality
 */
interface HasCardinality {

    /**
     * Returns a cardinality number.
     *
     * @return int, a non-negative integer value
     * @see XSD#nonNegativeInteger
     */
    int getCardinality();

    /**
     * Determines if this restriction is qualified.
     * Qualified cardinality restrictions are defined to be cardinality restrictions
     * that have fillers which aren't TOP ({@link OWL2#Thing owl:Thing} or
     * {@link org.apache.jena.vocabulary.RDFS#Literal rdfs:Literal}).
     * An object restriction is unqualified if it has a filler that is {@code owl:Thing}.
     * A data restriction is unqualified
     * if it has a filler which is the top data type ({@code rdfs:Literal}).
     *
     * @return {@code true} if this restriction is qualified, or {@code false} if this restriction is unqualified
     */
    boolean isQualified();
}

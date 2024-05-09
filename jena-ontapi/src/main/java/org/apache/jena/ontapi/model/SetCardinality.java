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
import org.apache.jena.vocabulary.XSD;

/**
 * A technical interface to set new cardinality value.
 *
 * @param <R> - return type, a subtype of {@link OntClass.CardinalityRestriction}
 * @see HasCardinality
 */
interface SetCardinality<R extends OntClass.CardinalityRestriction<?, ?>> {

    /**
     * Sets a new cardinality value.
     *
     * @param cardinality int, a non-negative integer value
     * @return <b>this</b> instance to allow cascading calls
     * @throws OntJenaException in case of wrong input
     * @see XSD#nonNegativeInteger
     */
    R setCardinality(int cardinality);
}

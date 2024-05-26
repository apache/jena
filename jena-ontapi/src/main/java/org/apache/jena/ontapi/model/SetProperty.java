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

/**
 * A technical interface to provide a possibility to assign {@link OntRelationalProperty data or object} property
 * into {@link OntClass.Restriction restriction class expression}.
 *
 * @param <P> {@link OntRelationalProperty data or object} property expression
 * @param <R> - return type, a subtype of {@link OntClass.Restriction}
 * @see HasProperty
 */
interface SetProperty<P extends OntRelationalProperty, R extends OntClass.Restriction> {

    /**
     * Sets the given property into this Restriction
     * (as an object with predicate {@link OWL2#onProperty owl:onProperty}
     * if it is Unary Restriction).
     *
     * @param property {@code P}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     */
    R setProperty(P property);
}

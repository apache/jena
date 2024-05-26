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
 * A technical interface to access {@code P} property
 * on predicate {@link OWL2#onProperty owl:onProperty}.
 *
 * @param <P> - {@link OntRelationalProperty Data or Object} property expression
 * @see SetProperty
 */
interface HasProperty<P extends OntRelationalProperty> {
    /**
     * Gets the property,
     * that this {@link OntClass.UnaryRestriction} has inside its RDF structure
     * on predicate {@link OWL2#onProperty owl:onProperty}.
     *
     * @return {@code P}
     */
    P getProperty();

}

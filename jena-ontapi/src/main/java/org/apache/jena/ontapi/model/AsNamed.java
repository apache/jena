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

/**
 * Provides a "sugar" functionality to represent an expression as an entity.
 * Just for convenience’s sake.
 *
 * @param <E> - an {@link OntEntity} instance
 */
interface AsNamed<E extends OntEntity> {

    /**
     * Represents this OWL expression as a named OWL entity if it is possible, otherwise throws an exception.
     * Effectively equivalent to the expression {@code this.as(Named.class)}.
     *
     * @return {@code E}, never {@code null}
     * @throws org.apache.jena.enhanced.UnsupportedPolymorphismException if the expression is not named OWL entity
     * @see org.apache.jena.rdf.model.RDFNode#as(Class)
     */
    E asNamed();
}

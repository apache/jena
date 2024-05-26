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
 * A technical interface to provide a possibility to assign {@link OntDataProperty} properties
 * into {@link OntClass.NaryRestriction n-ary restriction class expression}
 * on predicate {@link OWL2#onProperties owl:onProperties}.
 *
 * @param <P> - any subtype of {@link OntRelationalProperty} in general case, but in the current model it can only be {@link OntDataProperty}
 * @param <R> - return type, a subtype of {@link OntClass.NaryRestriction}
 * @see HasProperties
 */
interface SetProperties<P extends OntRelationalProperty, R extends OntClass.NaryRestriction<?, ?>>
        extends SetComponents<P, R>, SetProperty<P, R> {

    /**
     * Sets the given property as the only member of the []-list.
     *
     * @param property {@code P}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see HasProperties#getProperty()
     */
    @SuppressWarnings("unchecked")
    @Override
    default R setProperty(P property) {
        getList().clear().add(property);
        return (R) this;
    }
}

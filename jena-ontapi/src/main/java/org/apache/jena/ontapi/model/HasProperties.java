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
import org.apache.jena.vocabulary.OWL2;

/**
 * A technical interface to access {@code P} properties from a []-list
 * on predicate {@link OWL2#onProperties owl:onProperties}.
 *
 * @param <P> - any subtype of {@link OntRelationalProperty} in general case,
 *            but in the current model it can only be {@link OntDataProperty}
 * @see SetProperties
 */
interface HasProperties<P extends OntRelationalProperty> extends HasRDFNodeList<P>, HasProperty<P> {

    /**
     * Gets the first property from {@code owl:onProperties} []-list.
     * Currently, in OWL2, a []-list from n-ary Restrictions may contain one and only one (data) property.
     *
     * @return {@code P}
     * @see OntDataRange#arity()
     */
    @Override
    default P getProperty() {
        return getList().first().orElseThrow(OntJenaException.IllegalState::new);
    }
}

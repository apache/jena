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
import org.apache.jena.rdf.model.RDFNode;

import java.util.Arrays;
import java.util.Collection;

/**
 * A technical interface to provide working with {@link OntList Ontology []-list}.
 *
 * @param <V> - {@link RDFNode}, a list's item type
 * @param <R> - {@link OntObject}, a return type
 * @see WithOntList
 * @see HasRDFNodeList
 */
interface SetComponents<V extends RDFNode, R extends OntObject> extends WithOntList<V> {

    /**
     * Replaces the existing []-list content with the specified one, that is given in the form of vararg array.
     *
     * @param values an {@code Array} of the type {@code V}
     * @return <b>this</b> instance to allow cascading calls
     */
    @SuppressWarnings("unchecked")
    default R setComponents(V... values) {
        return setComponents(Arrays.asList(values));
    }

    /**
     * Replaces the existing []-list content with the specified one, that is given in the form of {@link Collection}.
     * Nulls and self-references are not allowed.
     *
     * @param components a {@code Collection} of the type {@code V}
     * @return <b>this</b> instance to allow cascading calls
     * @throws OntJenaException in case of wrong input
     */
    @SuppressWarnings("unchecked")
    default R setComponents(Collection<V> components) {
        if (components.stream().peek(OntJenaException::notNull).anyMatch(SetComponents.this::equals)) {
            throw new OntJenaException.IllegalArgument();
        }
        getList().clear().addAll(components);
        return (R) this;
    }
}

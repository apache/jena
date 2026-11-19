/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdfs.engine;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.rdfs.setup.ConfigRDFS;

/**
 * Class to help implementations of RDFS inference.
 * Provides common constant terms and common accessor functions for triples/quads/tuples.
 */

/*package*/
public class CxtInf<X,T> {
    public final X ANY;
    public final X rdfType;
    public final X rdfsSubClassOf;
    public final X rdfsSubPropertyOf;
    public final X rdfsDomain;
    public final X rdfsRange;
    public final ConfigRDFS<X> setup;
    public final MapperX<X, T> mapper;

    protected CxtInf(ConfigRDFS<X> setup, MapperX<X, T> mapper) {
        this.setup = Objects.requireNonNull(setup);
        this.mapper = Objects.requireNonNull(mapper);
        this.ANY = mapper.fromNode(Node.ANY);
        this.rdfType = mapper.fromNode(ConstRDFS.rdfType);
        this.rdfsDomain = mapper.fromNode(ConstRDFS.rdfsDomain);
        this.rdfsRange = mapper.fromNode(ConstRDFS.rdfsRange);
        this.rdfsSubClassOf = mapper.fromNode(ConstRDFS.rdfsSubClassOf);
        this.rdfsSubPropertyOf = mapper.fromNode(ConstRDFS.rdfsSubPropertyOf);
    }

    protected X any(X x) {
        return ( x == null ) ? ANY : x;
    }

    protected boolean isANY(X x) {
        return ( x == null || x == ANY ) ;
    }

    protected boolean isTerm(X x) {
        return !isANY(x);
    }

    // A triple using a property also uses each of its super-properties
    // (rdfs:subPropertyOf), so the domains and ranges of the super-properties
    // apply to it as well.

    /** The domains of a property and of its super-properties. */
    protected Set<X> getDomainInc(X property) {
        return withSuperProperties(property, setup::getDomain);
    }

    /** The ranges of a property and of its super-properties. */
    protected Set<X> getRangeInc(X property) {
        return withSuperProperties(property, setup::getRange);
    }

    /** The properties with a given domain, and their sub-properties. */
    protected Set<X> getPropertiesByDomainInc(X type) {
        return withSubProperties(setup.getPropertiesByDomain(type));
    }

    /** The properties with a given range, and their sub-properties. */
    protected Set<X> getPropertiesByRangeInc(X type) {
        return withSubProperties(setup.getPropertiesByRange(type));
    }

    private Set<X> withSuperProperties(X property, Function<X, Set<X>> lookup) {
        Set<X> superProperties = setup.getSuperProperties(property);
        if ( superProperties.isEmpty() )
            return lookup.apply(property);
        Set<X> acc = new HashSet<>(lookup.apply(property));
        superProperties.forEach(p -> acc.addAll(lookup.apply(p)));
        return acc;
    }

    private Set<X> withSubProperties(Set<X> properties) {
        if ( properties.isEmpty() || ! setup.hasPropertyDeclarations() )
            return properties;
        Set<X> acc = new HashSet<>(properties);
        properties.forEach(p -> acc.addAll(setup.getSubProperties(p)));
        return acc;
    }
}

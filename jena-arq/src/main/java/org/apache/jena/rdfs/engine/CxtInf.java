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

package org.apache.jena.rdfs.engine;

import java.util.Objects;

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
}

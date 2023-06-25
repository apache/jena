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

package org.apache.jena.graph;

import java.util.Objects;

import org.apache.jena.shared.PrefixMapping;

/** RDF triples as RDF terms for RDF-star quoted triples. */
public class Node_Triple extends Node {

    private final Triple triple;

    public Node_Triple(Node s, Node p, Node o) {
        this(Triple.create(s, p, o));
    }

    public Node_Triple(Triple triple) {
        this.triple = Objects.requireNonNull(triple);
    }

    @Override
    public Triple getTriple() {
        return triple;
    }

    @Override
    public boolean isConcrete() {
        return getTriple().isConcrete();
    }

    @Override
    public boolean isNodeTriple() {
        return true;
    }

    @Override
    public Object visitWith(NodeVisitor v) {
        return v.visitTriple(this, triple);
    }

    // Only based on label.
    @Override
    public int hashCode() {
        return Node.hashNodeTriple + triple.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Node_Triple other = (Node_Triple)obj;
        return triple.equals(other.triple);
    }

    @Override
    public String toString(PrefixMapping pm) {
        return "<< " + triple.toString() + " >>";
    }

    @Override
    public String toString() {
        return triple.toString();
    }
}

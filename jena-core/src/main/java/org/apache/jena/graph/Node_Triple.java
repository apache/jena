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

import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PrefixMapping;

/** RDF triples as RDF terms. */
public class Node_Triple extends Node_Ext<Triple>{

    public static Triple tripleOrNull(Node n) {
        if ( n instanceof Node_Triple ) {
            Node_Triple nt = (Node_Triple)n;
            return nt.get();
        }
        return null;
    }

    /**
     * Return the triple from a Node_Triple.
     * Throws a {@link JenaException} if not a {@code Node_Triple}.
     * Pairs with {@link Node#isNodeTriple()}
     */
    public static Triple triple(Node n) {
        Objects.requireNonNull(n);
        try {
            // Instead of adding getTriple() to Node
            Node_Triple nt = (Node_Triple)n;
            return nt.get();
        } catch (ClassCastException ex) {
            throw new JenaNodeException("Not a Node_Triple: "+n);
        }
    }

    public static Node_Triple cast(Node n) {
        return (Node_Triple)n;
    }

    public Node_Triple(Node s, Node p, Node o) {
        this(Triple.create(s, p, o));
    }

    public Node_Triple(Triple triple) {
        super(triple);
    }

    @Override
    public boolean isConcrete() {
        return triple(this).isConcrete();
    }

    @Override
    public boolean isNodeTriple() {
        return true;
    }

    @Override
    public String toString(PrefixMapping pm, boolean quoting) {
        return "<< " + label.toString() + " >>";
    }
}

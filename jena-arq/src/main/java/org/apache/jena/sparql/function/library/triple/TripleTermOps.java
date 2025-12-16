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

package org.apache.jena.sparql.function.library.triple;

import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;

public class TripleTermOps {
    /**
     * Create a triple term.
     * Throws {@link ExprEvalException} if the predicate argument is not a URI.
     */
    public static NodeValue fnTriple(NodeValue nv1, NodeValue nv2, NodeValue nv3) {
        Node s = nv1.asNode();
        if ( ARQ.isStrictMode() ) {
            if ( s.isTripleTerm() )
                throw new ExprEvalException("triple: Subject is a triple term: "+nv1);
            if ( !s.isURI() && !s.isBlank() )
                throw new ExprEvalException("triple: Subject is not a URI or blank node: "+nv2);
        }

        Node p = nv2.asNode();
        if ( ! p.isURI() )
            throw new ExprEvalException("triple: Predicate not a URI: "+nv2);
        Node o = nv3.asNode();
        Node t = NodeFactory.createTripleTerm(s, p, o);
        return NodeValue.makeNode(t);
    }

    /** Test whether a {@link NodeValue} is an triple term. */
    public static NodeValue isTriple(NodeValue nv) {
        return NodeValue.booleanReturn(nv.asNode().isTripleTerm());
    }

    private static NodeValue tripleGetter(String name, NodeValue nv, Function<Triple, Node> accessor) {
        Node n = nv.asNode();
        if ( ! n.isTripleTerm() )
            throw new ExprEvalException(name+": Not a triple term: "+nv);
        Triple t = n.getTriple();
        Node x = accessor.apply(t);
        return NodeValue.makeNode(x);
    }

    /**
     * Return the subject of a triple term.
     * Throws {@link ExprEvalException} if the argument is not a triple term.
     */
    public static NodeValue tripleSubject(NodeValue nv) {
        return tripleGetter("subject",  nv, Triple::getSubject);
    }

    /**
     * Return the predicate of a triple term.
     * Throws {@link ExprEvalException} if the argument is not a triple term.
     */
    public static NodeValue triplePredicate(NodeValue nv) {
        return tripleGetter("predicate",  nv, Triple::getPredicate);
    }

    /**
     * Return the object of a triple term.
     * Throws {@link ExprEvalException} if the argument is not a triple term.
     */
    public static NodeValue tripleObject(NodeValue nv) {
        return tripleGetter("object",  nv, Triple::getObject);
    }
}

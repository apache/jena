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

package org.apache.jena.sparql.engine.main.solver;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Triple version of {@link StageMatchData}. This goes directly to the graph.
 * <p>
 * Plain matching - see {@link SolverRX3#rdfStarTriple} for matching with variables
 * in RDF-star embedded triples.
 */
public class StageMatchTriple {
    public static Iterator<Binding> accessTriple(Iterator<Binding> input, Graph graph, Triple pattern,
                                                 Predicate<Triple> filter, ExecutionContext execCxt) {
        if ( ! input.hasNext() )
            return Iter.nullIterator();

        return Iter.flatMap(input, binding -> {
            return accessTriple(binding, graph, pattern, filter, execCxt);
        });
    }

    private static Iterator<Binding> accessTriple(Binding binding, Graph graph, Triple pattern, Predicate<Triple> filter, ExecutionContext execCxt) {
        Node s = substituteFlat(pattern.getSubject(), binding) ;
        Node p = substituteFlat(pattern.getPredicate(), binding) ;
        Node o = substituteFlat(pattern.getObject(), binding) ;
        BindingBuilder resultsBuilder = Binding.builder(binding);
        Node s2 = tripleNode(s) ;
        Node p2 = tripleNode(p) ;
        Node o2 = tripleNode(o) ;
        ExtendedIterator<Triple> graphIter = graph.find(s2, p2, o2) ;
        ExtendedIterator<Binding> iter = graphIter.mapWith( r -> mapper(resultsBuilder, s, p, o, r)).filterDrop(Objects::isNull);
        return iter;
    }

    private static Node tripleNode(Node node) {
        if ( node.isVariable() )
            return Node.ANY;
        return node;
    }

    private static Binding mapper(BindingBuilder resultsBuilder, Node s, Node p, Node o, Triple r) {
        resultsBuilder.reset();
        if ( !insert(resultsBuilder, s, r.getSubject()) )
            return null;
        if ( !insert(resultsBuilder, p, r.getPredicate()) )
            return null;
        if ( !insert(resultsBuilder, o, r.getObject()) )
            return null;
        return resultsBuilder.build();
    }

    private static boolean insert(BindingBuilder results, Node patternNode, Node dataNode) {
        if ( !Var.isVar(patternNode) )
            return true;
        Var v = Var.alloc(patternNode);
        Node x = results.get(v);
        if ( x != null )
            return SolverLib.sameTermAs(dataNode, x);
        results.add(v, dataNode);
        return true;
    }

    // Variable or not a variable. Not <<?var>>
    private static Node substituteFlat(Node n, Binding binding) {
        return Var.lookup(binding::get, n);
    }
}

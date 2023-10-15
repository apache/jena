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

package org.apache.jena.shex.eval;

import static org.apache.jena.atlas.lib.StreamOps.toSet;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shex.ShexException;
import org.apache.jena.shex.ShexSchema;
import org.apache.jena.shex.expressions.*;
import org.apache.jena.shex.sys.ReportItem;
import org.apache.jena.shex.sys.ValidationContext;
import org.apache.jena.system.G;
import org.apache.jena.util.iterator.ExtendedIterator;

public class ShapeEval {

    static boolean DEBUG = false;
    static boolean DEBUG_eachOf = DEBUG;
    static boolean DEBUG_cardinalityOf = DEBUG;

    public static void debug(boolean debug) {
        DEBUG = debug;
        DEBUG_eachOf = debug;
        DEBUG_cardinalityOf = debug;
    }

    // With help from the ideas (not code) of:
    // https://github.com/hsolbrig/PyShEx/blob/master/pyshex/shape_expressions_language/p5_5_shapes_and_triple_expressions.py

    public static boolean matchesShapeExpr(ValidationContext vCxt, ShapeExpression shapeExpr, Node node) {
        return shapeExpr.satisfies(vCxt, node);
    }

    /*package*/ public static boolean matchesTripleExpr(ValidationContext vCxt, TripleExpression tripleExpr, Node node, Set<Node> extras, boolean closed) {
//        Set<Triple> neigh = new HashSet<>();
//        Set<Triple> arcsOut = new HashSet<>();
//        Set<Triple> arcsIn = new HashSet<>();
//        arcsOut(arcsOut, vCxt.getData(), node);
//        arcsIn(arcsIn, vCxt.getData(), node);
//        neigh.addAll(arcsOut);
//        neigh.addAll(arcsIn);

        Set<Triple> arcsOut = new HashSet<>();
        arcsOut(arcsOut, vCxt.getData(), node);
        Set<Node> predicates = findPredicates(vCxt, tripleExpr);
        Set<Triple> matchables = toSet(arcsOut.stream().filter(t->predicates.contains(t.getPredicate())));

        boolean b = matches(vCxt, matchables, node, tripleExpr, extras);
        if ( ! b )
            return false;
        if ( closed ) {
            // CLOSED : no other triples.
            Set<Triple> non_matchables = toSet(arcsOut.stream().filter(t->!matchables.contains(t)));
            if ( ! non_matchables.isEmpty() )
                return false;
        }
        return true;
    }

    static boolean matches(ValidationContext vCxt, Set<Triple> matchables, Node node, TripleExpression tripleExpr, Set<Node> extras) {
      return matchesExpr(vCxt, matchables, node, tripleExpr, extras);
  }

    private static boolean matchesExpr(ValidationContext vCxt, Set<Triple> T, Node node, TripleExpression tripleExpr, Set<Node> extras) {
        if ( tripleExpr instanceof TripleExprEachOf eachOf ) {
            return ShapeEvalEachOf.matchesEachOf(vCxt, T, node, eachOf, extras);
        }
        else if ( tripleExpr instanceof TripleExprOneOf oneOf ) {
            return ShapeEvalOneOf.matchesOneOf(vCxt, T, node, oneOf, extras);
        }
        else if ( tripleExpr instanceof TripleExprRef ref ) {
            return matchesTripleExprRef(vCxt, T, node, ref, extras);
        }
        else if ( tripleExpr instanceof TripleExprCardinality card) {
            return ShapeEvalCardinality.matchesCardinality(vCxt, T, node, card, extras);
        }
        else if ( tripleExpr instanceof TripleConstraint constraint) {
            return ShapeEvalTripleConstraint.matchesCardinalityTC(vCxt, T, node, constraint, extras);
        }
        else if ( tripleExpr instanceof TripleExprNone ) {
            return true;
        }
        throw new NotImplemented(tripleExpr.getClass().getSimpleName());
    }

    private static boolean matchesTripleExprRef(ValidationContext vCxt, Set<Triple> matchables, Node node, TripleExprRef ref, Set<Node> extras) {
        Node label = ref.ref();
        if ( label == null ) {}
        TripleExpression tripleExpr = vCxt.getTripleExpression(label);
        if ( tripleExpr == null ) {
            ReportItem rItem = new ReportItem("Failed to get triple expression from reference", label);
            vCxt.reportEntry(rItem);
        }
        return matches(vCxt, matchables, node, tripleExpr, extras);
    }

    // Recursive.
    private static TripleExprVisitor walk(ShexSchema shapes, TripleExprVisitor step) {
        //Walker
        return new TripleExprVisitor() {
            @Override
            public void visit(TripleExprCardinality expr) {
                expr.visit(step);
                expr.target().visit(this);
            }

            @Override
            public void visit(TripleExprEachOf expr) {
                expr.visit(step);
                expr.expressions().forEach(ex -> ex.visit(this));
            }

            @Override
            public void visit(TripleExprOneOf expr) {
                expr.visit(step);
                expr.expressions().forEach(ex -> ex.visit(this));
            }

            @Override
            public void visit(TripleExprNone expr) {
                expr.visit(step);
            }

            @Override
            public void visit(TripleExprRef expr) {
                expr.visit(step);
                if ( expr.ref() == null )
                    throw new ShexException("Failed to dereference : "+expr.ref());
                shapes.getTripleExpression(expr.ref()).visit(this);
            }

            @Override
            public void visit(TripleConstraint expr) {
                expr.visit(step);
            }
        };
    }

    /*package*/ static Set<TripleConstraint> findTripleConstraint(ValidationContext vCxt, TripleExpression tripleExpr) {
        Set<TripleConstraint> constraints = new HashSet<>();
        tripleExpr.visit(accumulator(vCxt.getShapes(), constraints, Function.identity()));
        return constraints;
    }

    /*package*/ static Set<Node> findPredicates(ValidationContext vCxt, TripleExpression tripleExpr) {
        Set<Node> predicates = new HashSet<>();
        tripleExpr.visit(accumulator(vCxt.getShapes(), predicates, TripleConstraint::getPredicate));
        return predicates;
    }

    private static <X> TripleExprVisitor accumulator(ShexSchema shapes, Set<X> acc, Function<TripleConstraint, X> mapper) {
        TripleExprVisitor step = new TripleExprVisitor() {
            @Override
            public void visit(TripleConstraint tripleConstraint) {
                acc.add(mapper.apply(tripleConstraint));
            }
        };
        return walk(shapes, step);
    }

    private static void arcsOut(Set<Triple> neigh, Graph graph, Node node) {
        ExtendedIterator<Triple> x = G.find(graph, node, null, null);
        x.forEach(neigh::add);
    }

    private static void arcsIn(Set<Triple> neigh, Graph graph, Node node) {
        ExtendedIterator<Triple> x = G.find(graph, null, null, node);
        x.forEach(neigh::add);
    }
}

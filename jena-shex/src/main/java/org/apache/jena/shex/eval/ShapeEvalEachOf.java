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

import java.util.*;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
import org.apache.jena.ext.com.google.common.collect.ListMultimap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shex.expressions.TripleExpression;
import org.apache.jena.shex.expressions.TripleExprEachOf;
import org.apache.jena.shex.sys.ValidationContext;

/*package*/ class ShapeEvalEachOf {
    // Sufficiently large and complex so separated from ShapeEval.

    /*package*/ static boolean matchesEachOf(ValidationContext vCxt, Set<Triple> matchables, Node node,
                                             TripleExprEachOf eachOf, Set<Node> extras) {
        /*
         * https://github.com/hsolbrig/PyShEx/blob/master/pyshex/utils/matchesEachOfEvaluator.py
         * Special cases:
            #       Case 1: predicate occurs in exactly one expression and expression references exactly one predicate
            #                   Evaluate and return false if fail
            #       Case 2: predicate occurs two or more expressions and all expressions reference exactly one predicate
            #                   Permute predicate over expressions until a passing condition is found
            #       Case 3: expression references two or more predicates and all referenced predicates occur only once
            #                   Evaluate with set of all predicates and return false if fail
            #       Case 4: predicate occurs in two or more expressions and at least one of the referenced expressions
         */

        // ---- Preparation
        // This can be done once on parsing except that imports must have been done.
        // Unused: List<Set<Node>> exprIdxToPredicates = new ArrayList<>();

        // We use indexing for expressions because a triple expression may occur twice in the list.
        ListMultimap<Node, Integer> predicateToTripleExprs = ArrayListMultimap.create();
        List<TripleExpression> tripleExprs = eachOf.expressions();

        int N = tripleExprs.size();
        for ( int i = 0 ; i < N ; i++ ) {
            TripleExpression tExpr = tripleExprs.get(i);
            Set<Node> pred = ShapeEval.findPredicates(vCxt, tExpr);
            //exprIdxToPredicates.add(pred);
            for ( Node p : pred )
                predicateToTripleExprs.put(p, i);
        }
        // -- end preparation.

        List<List<Set<Triple>>> partitions = partition(matchables, tripleExprs, /*exprIdxToPredicates,*/ predicateToTripleExprs);
        if ( ShapeEval.DEBUG_eachOf ) {
            System.out.println("EachOf: "+eachOf);
            if ( partitions == null  )
                System.out.println("<null>");
            else if ( partitions.isEmpty() )
                System.out.println("<empty>");
        }

        if ( partitions == null )
            // No partition possible. e.g.triple that can't be placed.
            return false;

        // And now eval.
        for ( List<Set<Triple>> partition : partitions ) {
            boolean success = true;
            for ( int i = 0 ; i < N ; i++ ) {
                Set<Triple> triples = partition.get(i);
                TripleExpression tripleExpr = tripleExprs.get(i);

                if ( ShapeEval.DEBUG_eachOf )
                    System.out.println("Partition: "+partition);

                boolean b = ShapeEval.matches(vCxt, triples, node, tripleExpr, extras);
                if ( ShapeEval.DEBUG_eachOf )
                    System.out.println("    "+b);
                if ( !b ) {
                    success = false;
                    break;
                }
            }
            // This partition works.
            if ( success )
                return true;
        }
        return false;
    }

    // ---- Partition generation
    // We have to use indexes for TripleExpression because a TripleExpression may occur twice so
    // a logical Map<TripleExpression instance, Set<Triple>> is List<Set<Triple>>

    private static List<List<Set<Triple>>> partition(Collection<Triple> triples,
                                                     List<TripleExpression> tripleExprs,
                                                     //List<Set<Node>> exprIdxToPredicates,
                                                     ListMultimap<Node, Integer> predicateToTripleExprs) {
        // Any unallocateables?
        // Can each triple be placed somewhere?
        for ( Triple t : triples ) {
            // Could do in the loop below.
            if ( ! predicateToTripleExprs.containsKey(t.getPredicate()) )
                // Predicate of a triple can't be placed.
                return null;
        }
//        if ( triples.stream().noneMatch(t->predicateToTripleExprs.containsKey(t.getPredicate())) )
//            return null;


        // Start. One empty partition
        List<Set<Triple>> emptyPartial = emptyPartition(tripleExprs);


        List<List<Set<Triple>>> partials = new ArrayList<>();
        partials.add(emptyPartial);

        for ( Triple t : triples ) {
//            if ( ! predicateToTripleExprs.containsKey(t.getPredicate()) )
//                // Predicate of a triple that can't be placed.
//                return null;

            List<List<Set<Triple>>> partials2 = new ArrayList<>();
            // foreach partial partition, create new partitions with the triple allocated to a slot.
            for ( List<Set<Triple>> onePartial : partials) {
                List<List<Set<Triple>>> p = partition(t, onePartial, tripleExprs, /*exprIdxToPredicates,*/ predicateToTripleExprs);
                partials2.addAll(p);
            }
            partials = partials2;
        }
        return partials;
    }

    private static List<List<Set<Triple>>> partition(Triple t, List<Set<Triple>> partial, List<TripleExpression> tripleExprs,
                                                     //List<Set<Node>> exprIdxToPredicates,
                                                     ListMultimap<Node, Integer> predicateToTripleExprs) {
        Node p = t.getPredicate();
        // Places triple t can go.
        List<Integer> places = predicateToTripleExprs.get(p);
        if ( places == null || places.isEmpty() )
            throw new InternalErrorException();
        List<List<Set<Triple>>> result = new ArrayList<>();

        for ( int i : places ) {
            // Copy backbone.
            List<Set<Triple>> partial2 = new ArrayList<>(partial);
            // Clone slot.
            Set<Triple> x = partial2.get(i);
            Set<Triple> x2 = new HashSet<>(x);
            // Add.
            x2.add(t);
            partial2.set(i, x2);
            result.add(partial2);
        }
        return result;
    }

    private static List<Set<Triple>> emptyPartition(List<TripleExpression> tripleExprs) {
        int N = tripleExprs.size();
        List<Set<Triple>> partition = new ArrayList<>(N);
        for ( int i = 0 ; i < N ; i++ ) {
            partition.add(new HashSet<>());
        }
        return partition;
    }

}
